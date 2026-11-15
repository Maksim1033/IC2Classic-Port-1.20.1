package ic2.core.audio;

import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.audio.Library.Pool;
import ic2.core.IC2;
import ic2.core.audio.providers.ProviderEntity;
import ic2.core.audio.providers.ProviderTileEntity;
import ic2.core.audio.providers.SimpleAudioProvider;
import ic2.core.audio.providers.SimplePosition;
import ic2.core.audio.tracker.BatteryTracker;
import ic2.core.audio.tracker.ChainsawTracker;
import ic2.core.audio.tracker.IAudioTracker;
import ic2.core.audio.tracker.JetpackTracker;
import ic2.core.audio.tracker.NanosaberTracker;
import ic2.core.platform.corehacks.mixins.client.audio.SoundEngineMixin;
import ic2.core.platform.corehacks.mixins.client.audio.SoundHandlerMixin;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class AudioManagerClient extends AudioManager {
   static final Function<ISoundProvider, List<AudioSourceClient>> CREATOR = T -> CollectionUtils.createList();
   ChannelAccess manager;
   SoundBufferLibrary loader;
   boolean enabled = true;
   boolean markForRemoval = false;
   List<IAudioTracker> trackers = CollectionUtils.<IAudioTracker>createList();
   Long2ObjectMap<EnumMap<AudioManager.SoundType, AudioEffect>> modifiers = new Long2ObjectLinkedOpenHashMap();
   Object2ObjectMap<ISoundProvider, List<AudioSourceClient>> providers = CollectionUtils.<ISoundProvider, List<AudioSourceClient>>createLinkedMap();
   RandomSource random = SoundInstance.createUnseededRandom();
   boolean dirtyModifiers = false;

   @Override
   public void init() {
      super.init();
      MinecraftForge.EVENT_BUS.register(this);
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onEntitySpawn(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Player player) {
         this.addTracker(new BatteryTracker(player));
         this.addTracker(new JetpackTracker(player));
         this.addTracker(new ChainsawTracker(player, IC2Items.CHAINSAW));
         this.addTracker(new ChainsawTracker(player, IC2Items.CHAINSAW_ADVANCED));
         this.addTracker(new NanosaberTracker(player));
      }
   }

   @Override
   public void addTracker(IAudioTracker tracker) {
      this.trackers.add(tracker);
   }

   @Override
   public void addModifier(BlockPos pos, EnumMap<AudioManager.SoundType, AudioEffect> modifiers) {
      if (!modifiers.isEmpty()) {
         this.modifiers.put(pos.asLong(), modifiers);
         this.dirtyModifiers = true;
      }
   }

   @Override
   public void removeModifier(BlockPos pos) {
      this.modifiers.remove(pos.asLong());
      this.dirtyModifiers = true;
   }

   public boolean didModifiersChange() {
      return this.dirtyModifiers;
   }

   public float getModifier(AudioManager.SoundType type, Vec3 currentPosition) {
      float modifier = 1.0F;
      MutableBlockPos mutable = new MutableBlockPos();
      ObjectIterator var5 = Long2ObjectMaps.fastIterable(this.modifiers).iterator();

      while (var5.hasNext()) {
         Entry<EnumMap<AudioManager.SoundType, AudioEffect>> entry = (Entry<EnumMap<AudioManager.SoundType, AudioEffect>>)var5.next();
         AudioEffect effect = (AudioEffect)((EnumMap)entry.getValue()).get(type);
         if (effect != null) {
            mutable.set(entry.getLongKey());
            if (Math.sqrt(mutable.distToCenterSqr(currentPosition)) <= (double)(effect.getRadius() + 0.5F)) {
               modifier *= effect.getEffect();
               if (modifier <= 0.0F) {
                  return 0.0F;
               }
            }
         }
      }

      return modifier;
   }

   public boolean isValid() {
      return this.enabled && this.manager != null && this.loader != null;
   }

   @Override
   public IAudioSource createSource(Object obj, ResourceLocation location) {
      return this.createSource(obj, location, AudioManager.SoundType.STATIC, 1.0F, true, false);
   }

   @Override
   public IAudioSource createSource(Object obj, ResourceLocation location, AudioManager.SoundType type, float volume, boolean loop, boolean priority) {
      if (this.enabled && location != null && !(Math.min(this.getMasterVolume(), Math.min(this.getVolumeForType(type), volume)) <= 0.0F)) {
         if (!this.isValid()) {
            this.getSoundSystem();
            if (!this.isValid()) {
               return null;
            }
         }

         location = this.convertVanilla(location);
         if (Minecraft.getInstance().getResourceManager().getResource(location).isEmpty()) {
            IC2.LOGGER.info("Couldn't find the Sound File: {}", location);
            return null;
         } else {
            ISoundProvider provider = this.createProvider(obj, type);
            if (provider == null) {
               IC2.LOGGER.info("Couldn't create a Provider: {}", obj);
               return null;
            } else {
               AudioSourceClient sound = new AudioSourceClient(this, location, provider.getPosition(), type, volume, 1.0F, loop, priority);
               ((List)this.providers.computeIfAbsent(provider, CREATOR)).add(sound);
               return sound;
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public void playSound(Object obj, ResourceLocation location) {
      this.playSound(obj, location, AudioManager.SoundType.STATIC);
   }

   @Override
   public void playSound(Object obj, ResourceLocation location, AudioManager.SoundType type) {
      this.playSound(obj, location, type, 1.0F, 1.0F);
   }

   @Override
   public void playSound(Object obj, ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
      if (this.enabled && location != null && !(Math.min(this.getMasterVolume(), Math.min(this.getVolumeForType(type), volume)) <= 0.0F)) {
         if (!this.isValid()) {
            this.getSoundSystem();
            if (!this.isValid()) {
               return;
            }
         }

         volume *= this.getMasterVolume();
         volume *= this.getVolumeForType(type);
         ResourceLocation final_location = this.convertVanilla(location);
         if (Minecraft.getInstance().getResourceManager().getResource(final_location).isEmpty()) {
            IC2.LOGGER.info("Couldn't find the Sound File: {}", location);
         } else {
            ISoundProvider provider = this.createProvider(obj, type);
            if (provider == null) {
               IC2.LOGGER.info("Couldn't create a Provider: {}", obj);
            } else {
               float finalVolume = volume * this.getModifier(type, provider.getPosition().getPosition());
               this.createEntry().thenAccept(T -> T.execute(source -> {
                     source.attachStaticBuffer(this.createBuffer(final_location));
                     source.setLooping(false);
                     source.linearAttenuation(16.0F * Math.max(0.2F, finalVolume));
                     source.setRelative(false);
                     source.setSelfPosition(provider.getPosition().getPosition());
                     source.setVolume(finalVolume);
                     source.setPitch(pitch);
                     source.play();
                  }));
            }
         }
      }
   }

   public ResourceLocation convertVanilla(ResourceLocation location) {
      WeighedSoundEvents event = Minecraft.getInstance().getSoundManager().getSoundEvent(location);
      if (event == null) {
         return location;
      } else {
         Sound sound = event.getSound(this.random);
         return sound == SoundManager.EMPTY_SOUND ? location : sound.getPath();
      }
   }

   private ISoundProvider createProvider(Object obj, AudioManager.SoundType spec) {
      if (obj instanceof ISoundProvider) {
         return (ISoundProvider)obj;
      } else if (obj instanceof SimplePosition) {
         return new SimpleAudioProvider((IAudioPosition)obj);
      } else if (obj instanceof BlockEntity) {
         return new ProviderTileEntity((BlockEntity)obj);
      } else {
         return obj instanceof Entity ? new ProviderEntity((Entity)obj, spec) : null;
      }
   }

   @Override
   public float getMasterVolume() {
      return (float)IC2.CONFIG.masterVolume.get();
   }

   @Override
   public float getVolumeForType(AudioManager.SoundType type) {
      switch (type) {
         case BACKPACK:
            return (float)IC2.CONFIG.backVolume.get();
         case ITEM:
            return (float)IC2.CONFIG.itemVolume.get();
         case STATIC:
            return (float)IC2.CONFIG.blockVolume.get();
         default:
            return 1.0F;
      }
   }

   @Override
   public void removeSource(Object obj) {
      if (this.isValid()) {
         List<AudioSourceClient> clients = (List<AudioSourceClient>)this.providers.remove(this.createProvider(obj, AudioManager.SoundType.STATIC));
         if (clients != null) {
            for (AudioSourceClient client : clients) {
               client.remove();
            }
         }
      }
   }

   @Override
   public void resetAll() {
      this.markForRemoval = true;
   }

   public void onTick() {
      Minecraft minecraft = Minecraft.getInstance();
      Level world = minecraft.level;
      Player player = minecraft.player;
      if (this.isValid()) {
         if (!this.markForRemoval && world != null && player != null) {
            List<ISoundProvider> toRemove = new ObjectArrayList();
            List<AudioSourceClient> sounds = new ObjectArrayList();
            ObjectIterator index = Object2ObjectMaps.fastIterable(this.providers).iterator();

            while (index.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<ISoundProvider, List<AudioSourceClient>> entry = (it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<ISoundProvider, List<AudioSourceClient>>)index.next();
               if (((ISoundProvider)entry.getKey()).isValid(world) && !((List)entry.getValue()).isEmpty()) {
                  Iterator<AudioSourceClient> iter = ((List)entry.getValue()).iterator();

                  while (iter.hasNext()) {
                     AudioSourceClient sound = iter.next();
                     if (!sound.isValid()) {
                        iter.remove();
                     } else {
                        sound.updatePosition();
                        sound.updateVolume(player);
                        if (sound.isEnabled() && (sound.isPlaying() || sound.isPaused()) && sound.getPosition().isInSameWorld(world)) {
                           sounds.add(sound);
                        }
                     }
                  }
               } else {
                  toRemove.add((ISoundProvider)entry.getKey());
               }
            }

            this.dirtyModifiers = false;

            for (ISoundProvider prov : toRemove) {
               this.removeSource(prov);
            }

            sounds.sort(AudioManagerClient.AudioSorter.SORTER);
            int indexx = 0;

            for (AudioSourceClient client : sounds) {
               if (indexx < 20) {
                  client.ensureValid();
               } else {
                  client.disable();
               }

               indexx++;
            }
         } else {
            ObjectListIterator toRemove = new ObjectArrayList(this.providers.keySet()).iterator();

            while (toRemove.hasNext()) {
               ISoundProvider prov = (ISoundProvider)toRemove.next();
               this.removeSource(prov);
            }

            this.markForRemoval = false;
         }
      }
   }

   public void onPostTick() {
      Level world = Minecraft.getInstance().level;
      Iterator<IAudioTracker> iter = this.trackers.iterator();

      while (iter.hasNext()) {
         IAudioTracker tracker = iter.next();
         if (tracker.isValid(world)) {
            tracker.onTick();
         } else {
            iter.remove();
         }
      }
   }

   public void getSoundSystem() {
      SoundEngine engine = ((SoundHandlerMixin)Minecraft.getInstance().getSoundManager()).getSoundEngine();
      if (engine != null) {
         this.manager = ((SoundEngineMixin)engine).getChannels();
         this.loader = ((SoundEngineMixin)engine).getAudioBuffers();
      }
   }

   protected CompletableFuture<ChannelHandle> createEntry() {
      return this.manager.createHandle(Pool.STATIC);
   }

   protected SoundBuffer createBuffer(ResourceLocation location) {
      return (SoundBuffer)this.loader.getCompleteBuffer(location).join();
   }

   @OnlyIn(Dist.CLIENT)
   public static class AudioSorter implements Comparator<AudioSourceClient> {
      public static final AudioManagerClient.AudioSorter SORTER = new AudioManagerClient.AudioSorter();

      public int compare(AudioSourceClient o1, AudioSourceClient o2) {
         int result = Boolean.compare(o2.isPriority(), o1.isPriority());
         return result != 0 ? result : Float.compare(o2.getRealVolume(), o1.getRealVolume());
      }
   }
}
