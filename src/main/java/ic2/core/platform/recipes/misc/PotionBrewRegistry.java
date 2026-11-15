package ic2.core.platform.recipes.misc;

import ic2.api.recipes.registries.IPotionBrewRegistry;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionBrewRegistry implements IPotionBrewRegistry, IRecipeList {
   List<Consumer<IPotionBrewRegistry>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Map<Item, IPotionBrewRegistry.PotionContainer> items = CollectionUtils.<Item, IPotionBrewRegistry.PotionContainer>createLinkedMap();
   Map<Item, Map<MobEffect, MobEffect>> effects = CollectionUtils.<Item, Map<MobEffect, MobEffect>>createLinkedMap();
   Map<MobEffect, String> names = CollectionUtils.<MobEffect, String>createLinkedMap();

   public PotionBrewRegistry(Consumer<IPotionBrewRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   public void registerListener(Consumer<IPotionBrewRegistry> listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   public void reload() {
      this.items.clear();
      this.effects.clear();
      int i = 0;

      for (int m = this.listeners.size(); i < m; i++) {
         this.listeners.get(i).accept(this);
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.items.size());

      for (Entry<Item, IPotionBrewRegistry.PotionContainer> entry : this.items.entrySet()) {
         IPotionBrewRegistry.PotionContainer item = entry.getValue();
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, entry.getKey());
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, item.getOutput());
         buffer.writeVarInt(item.getFluidUsage());
         buffer.writeFloat(item.getDurationEffectiveness());
      }

      buffer.writeVarInt(this.effects.size());

      for (Entry<Item, Map<MobEffect, MobEffect>> effect : this.effects.entrySet()) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, effect.getKey());
         Map<MobEffect, MobEffect> map = effect.getValue();
         buffer.writeVarInt(map.size());

         for (Entry<MobEffect, MobEffect> entry : map.entrySet()) {
            buffer.writeBoolean(entry.getKey() != null);
            if (entry.getKey() != null) {
               buffer.writeRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS, entry.getKey());
            }

            buffer.writeRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS, entry.getValue());
         }
      }

      buffer.writeVarInt(this.names.size());

      for (Entry<MobEffect, String> entry : this.names.entrySet()) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS, entry.getKey());
         buffer.writeUtf(entry.getValue());
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<Item, IPotionBrewRegistry.PotionContainer> newItems = CollectionUtils.<Item, IPotionBrewRegistry.PotionContainer>createLinkedMap();
      Map<Item, Map<MobEffect, MobEffect>> newEffects = CollectionUtils.<Item, Map<MobEffect, MobEffect>>createLinkedMap();
      Map<MobEffect, String> newNames = CollectionUtils.<MobEffect, String>createLinkedMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         Item container = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
         Item item = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
         newItems.put(container, new IPotionBrewRegistry.PotionContainer(item, buffer.readVarInt(), buffer.readFloat()));
      }

      size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         Map<MobEffect, MobEffect> effects = CollectionUtils.<MobEffect, MobEffect>createLinkedMap();
         newEffects.put((Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), effects);
         int subSize = buffer.readVarInt();

         for (int j = 0; j < subSize; j++) {
            MobEffect key = buffer.readBoolean() ? (MobEffect)buffer.readRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS) : null;
            effects.put(key, (MobEffect)buffer.readRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS));
         }
      }

      size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         newNames.put((MobEffect)buffer.readRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS), buffer.readUtf());
      }

      this.items = newItems;
      this.effects = newEffects;
      this.names = newNames;
   }

   @Override
   public void registerBrew(Item item, MobEffect fromEffect, MobEffect toEffect) {
      Map<MobEffect, MobEffect> map = this.effects.computeIfAbsent(item, k -> CollectionUtils.createMap());
      map.put(fromEffect, toEffect);
   }

   @Override
   public void removeBrew(Item item) {
      this.effects.remove(item);
   }

   @Override
   public MobEffect getEffect(Item item, MobEffect original) {
      Map<MobEffect, MobEffect> map = this.effects.get(item);
      return map == null ? null : map.get(original);
   }

   @Override
   public Map<Item, Map<MobEffect, MobEffect>> getEffects() {
      return new Object2ObjectLinkedOpenHashMap(this.effects);
   }

   @Override
   public List<Item> getRequiredItems(MobEffect desiredEffect) {
      List<Item> list = CollectionUtils.<Item>createList();

      while (desiredEffect != null) {
         boolean found = false;

         for (Entry<Item, Map<MobEffect, MobEffect>> entry : this.effects.entrySet()) {
            for (Entry<MobEffect, MobEffect> subEntry : entry.getValue().entrySet()) {
               if (desiredEffect == subEntry.getValue()) {
                  desiredEffect = subEntry.getKey();
                  found = true;
                  break;
               }
            }

            if (found) {
               list.add(entry.getKey());
               break;
            }
         }

         if (!found) {
            return ObjectLists.emptyList();
         }
      }

      return list;
   }

   @Override
   public void registerName(MobEffect effect, String name) {
      if (effect != null && name != null && !name.isEmpty()) {
         this.names.put(effect, name);
      }
   }

   @Override
   public String getName(MobEffect effect) {
      return this.names.get(effect);
   }

   @Override
   public void registerPotionContainer(Item input, IPotionBrewRegistry.PotionContainer output) {
      if (input != null && output != null && input != Items.AIR) {
         this.items.put(input, output);
      }
   }

   @Override
   public IPotionBrewRegistry.PotionContainer getPotionContainer(Item input) {
      return this.items.get(input);
   }

   @Override
   public Map<Item, IPotionBrewRegistry.PotionContainer> getContainers() {
      return new Object2ObjectLinkedOpenHashMap(this.items);
   }
}
