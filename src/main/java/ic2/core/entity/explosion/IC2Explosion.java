package ic2.core.entity.explosion;

import com.mojang.authlib.GameProfile;
import ic2.api.blocks.ExplosionWhitelist;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.providers.SimplePosition;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.ExplosionPacket;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Potions;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.network.PacketDistributor.TargetPoint;

public class IC2Explosion {
   public static final ResourceLocation NUKE_EXPLOSION = new ResourceLocation("ic2", "sounds/misc/nuke.ogg");
   public static final ResourceLocation ELECTRIC_EXPLOSION = new ResourceLocation("ic2", "sounds/misc/overload.ogg");
   public static final double PI_2 = 6.283185307179586;
   public static final Predicate<LivingEntity> EXPLOSION = T -> {
      if (T instanceof Player player && (player.isSpectator() || player.getAbilities().invulnerable)) {
         return true;
      }

      return T.isAlive();
   };
   RandomSource rand;
   Level world;
   Vec3 origin;
   Entity exploder;
   DamageSource source;
   GameProfile igniter;
   float power;
   float dropRate;
   Long2LongMap destroyedBlocks = new Long2LongLinkedOpenHashMap();
   List<Entity> affectedEntities = CollectionUtils.<Entity>createList();
   Int2ObjectMap<Vec3> motions = new Int2ObjectLinkedOpenHashMap();
   public AABB box;
   private Explosion fakeExplosion;
   public boolean isBeacon;

   public IC2Explosion(Level world, Entity exploder, Vec3 origin, float power, float dropRate, DamageSource source) {
      this.world = world;
      this.rand = world.random;
      this.exploder = exploder;
      this.origin = origin;
      this.source = source;
      this.power = power;
      this.dropRate = dropRate;
      this.fakeExplosion = new Explosion(world, exploder, source, null, origin.x, origin.y, origin.z, power / 0.4F, false, BlockInteraction.DESTROY);
      if (this.source == null) {
         this.source = this.world.damageSources().explosion(this.fakeExplosion);
      }
   }

   public IC2Explosion setIgniter(Player player) {
      return player == null ? this : this.setIgniter(player.getGameProfile());
   }

   public IC2Explosion setIgniter(GameProfile profile) {
      this.igniter = profile;
      return this;
   }

   public void doExplosion() {
      if (!(this.power <= 0.0F) && !ForgeEventFactory.onExplosionStart(this.world, this.fakeExplosion)) {
         double max = (double)this.power / 0.4;
         this.box = new AABB(BlockPos.containing(this.origin)).inflate(max);
         int steps = (int)Math.ceil(3.141592653589793 / Math.atan(1.0 / max));
         this.affectedEntities.addAll(this.world.getEntitiesOfClass(LivingEntity.class, this.box, EXPLOSION));
         if (this.exploder != null) {
            this.affectedEntities.remove(this.exploder);
         }

         MinecraftForge.EVENT_BUS.post(new Detonate(this.world, this.fakeExplosion, this.affectedEntities));
         int i = 0;

         for (int m = steps * 2; i < m; i++) {
            double phi = 6.283185307179586 / (double)steps * (double)i;

            for (int y = 0; y < steps; y++) {
               double theta = 3.141592653589793 / (double)steps * (double)y;
               this.shootRay(this.origin.x, this.origin.y, this.origin.z, phi, theta, (double)this.power, i % 8 == 0 && y % 8 == 0);
            }
         }

         if (this.source == IC2DamageSource.NUKE) {
            for (LivingEntity entity : this.world
               .getEntitiesOfClass(
                  LivingEntity.class,
                  new AABB(
                     this.origin.x - 100.0, this.origin.y - 100.0, this.origin.z - 100.0, this.origin.x + 100.0, this.origin.y + 100.0, this.origin.z + 100.0
                  )
               )) {
               if (entity instanceof Player && this.isBeacon) {
                  entity.hurt(IC2DamageSource.NUKE, 1000.0F);
               }

               double distance = Math.sqrt(entity.distanceToSqr(this.origin));
               int hungerLength = (int)(120.0 * (100.0 - distance));
               int poisonLength = (int)(80.0 * (30.0 - distance));
               if (hungerLength >= 0) {
                  entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, hungerLength, 0));
               }

               if (poisonLength >= 0) {
                  entity.addEffect(new MobEffectInstance(IC2Potions.RADIATION, poisonLength, 0));
               }
            }

            for (Entity entry : this.affectedEntities) {
               if (entry instanceof Player) {
                  ((Player)entry).awardStat(IC2Stats.NUKES_SURVIVED);
               }
            }
         }

         if (this.source == IC2DamageSource.NUKE && this.exploder != null) {
            IC2.AUDIO.playSound(this.exploder, NUKE_EXPLOSION, AudioManager.SoundType.STATIC, 4.0F, 1.0F);
         } else if (this.source == IC2DamageSource.ELECTRICITY) {
            IC2.AUDIO.playSound(new SimplePosition(this.world, this.origin), ELECTRIC_EXPLOSION, AudioManager.SoundType.STATIC, 12.0F, 1.0F);
         } else {
            this.world
               .playSound(
                  null,
                  this.origin.x,
                  this.origin.y,
                  this.origin.z,
                  SoundEvents.GENERIC_EXPLODE,
                  SoundSource.BLOCKS,
                  4.0F,
                  (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F
               );
         }

         List<BlockPos> checkList = this.fakeExplosion.getToBlow();
         MinecraftForge.EVENT_BUS.post(new Detonate(this.world, this.fakeExplosion, CollectionUtils.createList()));
         LongSet toKeep = new LongOpenHashSet(checkList.size());
         int ix = 0;
         int m = checkList.size();

         while (ix < m) {
            toKeep.add(checkList.get(ix++).asLong());
         }

         List<ExplosionPacket.ParticleSpawn> spawn = CollectionUtils.<ExplosionPacket.ParticleSpawn>createList();
         Long2ObjectMap<List<ItemStack>> drops = new Long2ObjectLinkedOpenHashMap();
         MutableBlockPos position = new MutableBlockPos();
         ObjectIterator var32 = Long2LongMaps.fastIterable(this.destroyedBlocks).iterator();

         while (var32.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2LongMap.Entry entryx = (it.unimi.dsi.fastutil.longs.Long2LongMap.Entry)var32.next();
            long posLong = entryx.getLongKey();
            if (toKeep.contains(posLong)) {
               BlockPos pos = position.set(posLong).immutable();
               long value = entryx.getLongValue();
               BlockState state = Block.stateById(MathUtils.getIntKey(value));
               if (MathUtils.getIntValue(value) > 0 && state.canDropFromExplosion(this.world, pos, this.fakeExplosion)) {
                  spawn.add(new ExplosionPacket.ParticleSpawn(entryx.getLongKey(), this.power));
                  if (this.world instanceof ServerLevel && this.rand.nextFloat() <= this.dropRate) {
                     LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.world);
                     builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
                     builder.withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, this.world.getBlockEntity(pos));
                     List<ItemStack> resultDrops = state.getDrops(builder);
                     if (!resultDrops.isEmpty()) {
                        drops.put(entryx.getLongKey(), resultDrops);
                     }
                  }
               }

               state.onBlockExploded(this.world, pos, this.fakeExplosion);
            }
         }

         var32 = this.motions.int2ObjectEntrySet().iterator();

         while (var32.hasNext()) {
            Entry<Vec3> motion = (Entry<Vec3>)var32.next();
            motion.setValue(MathUtils.clamp((Vec3)motion.getValue(), -500.0, 500.0));
         }

         PacketManager.INSTANCE
            .sendToNearby(
               new TargetPoint(this.origin.x, this.origin.y, this.origin.z, 64.0, this.world.dimension()),
               new ExplosionPacket(spawn, this.motions, this.origin)
            );
         var32 = drops.long2ObjectEntrySet().iterator();

         while (var32.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<List<ItemStack>> entryx = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<List<ItemStack>>)var32.next();
            position.set(entryx.getLongKey());

            for (ItemStack item : entryx.getValue()) {
               if (!item.isEmpty()) {
                  Block.popResource(this.world, position, item);
               }
            }
         }
      }
   }

   protected void shootRay(double x, double y, double z, double phi, double theta, double power, boolean killEntities) {
      double deltaX = Math.sin(theta) * Math.cos(phi);
      double deltaY = Math.cos(theta);
      double deltaZ = Math.sin(theta) * Math.sin(phi);
      int step = 0;

      do {
         Vec3 vec = new Vec3(x, y, z);
         if (!this.box.contains(vec)) {
            break;
         }

         BlockPos pos = BlockPos.containing(x, y, z);
         BlockState state = this.world.getBlockState(pos);
         boolean isAir = state.isAir();
         double absorption = 0.5;
         if (!isAir) {
            double resistance = (double)state.getExplosionResistance(this.world, pos, this.fakeExplosion) * 0.4;
            if (resistance < 0.0) {
               break;
            }

            absorption = (resistance + 4.0) * 0.3;
         }

         if (absorption > 1000.0 && !ExplosionWhitelist.isWhitelisted(state.getBlock())) {
            absorption = 0.5;
         } else {
            if (absorption > power) {
               break;
            }

            if (!isAir) {
               long position = pos.asLong();
               long entry = this.destroyedBlocks.get(position);
               if (entry == 0L || power > 8.0 && MathUtils.getIntValue(entry) > 0) {
                  this.destroyedBlocks.put(position, MathUtils.putIntoLong((long)Block.getId(state), power <= 8.0 ? 1L : 0L));
                  if (entry == 0L) {
                     this.fakeExplosion.getToBlow().add(pos);
                  }
               }
            }
         }

         if (killEntities && step % 8 == 0 && !this.affectedEntities.isEmpty() && power >= 0.25) {
            List<Entity> alive = CollectionUtils.<Entity>createList();
            boolean nuke = this.source == IC2DamageSource.NUKE;

            for (Entity entity : this.affectedEntities) {
               if (!entity.ignoreExplosion() && entity.position().distanceTo(vec) <= power / 1.5) {
                  Vec3 entityDistance = entity.position().subtract(this.origin);
                  double distance = Math.sqrt(entity.distanceToSqr(this.origin));
                  double distanceFactor = power / 2.0 / (Math.pow(distance, 0.8) + 1.0);
                  entity.hurt(this.source, (float)Math.pow(distanceFactor * 3.0, 2.0));
                  if (!entity.isAlive()) {
                     if (this.igniter != null) {
                        if (nuke && entity instanceof Player) {
                           Player player = (Player)entity;
                           if (player.getGameProfile().equals(this.igniter)) {
                              IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:nuke_death"));
                           }
                        }

                        if (entity instanceof WitherBoss) {
                           Player player = this.world.getPlayerByUUID(this.igniter.getId());
                           if (player != null) {
                              player.awardStat(IC2Stats.WITHERS_NUKED);
                              IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:nuke_wither"));
                           }
                        }
                     }

                     this.motions.remove(entity.getId());
                  } else {
                     alive.add(entity);
                     Vec3 motion = (Vec3)this.motions.getOrDefault(entity.getId(), entity.getDeltaMovement());
                     this.motions
                        .put(entity.getId(), motion.add(MathUtils.divide(entityDistance, distance).multiply(distanceFactor, distanceFactor, distanceFactor)));
                  }
               } else {
                  alive.add(entity);
               }
            }

            this.affectedEntities = alive;
         }

         if (absorption > 10.0) {
            for (int i = 0; i < 5; i++) {
               this.shootRay(x, y, z, this.rand.nextDouble() * 6.283185307179586, this.rand.nextDouble() * 3.141592653589793, power - absorption, false);
            }
         }

         power -= absorption;
         x += deltaX;
         y += deltaY;
         z += deltaZ;
         step++;
      } while (y < (double)this.world.getMaxBuildHeight() && y > (double)this.world.getMinBuildHeight());
   }
}
