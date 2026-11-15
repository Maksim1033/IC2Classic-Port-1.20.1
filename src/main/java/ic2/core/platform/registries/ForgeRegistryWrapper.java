package ic2.core.platform.registries;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class ForgeRegistryWrapper {
   @Nonnull
   public static <T> ResourceLocation getKey(IForgeRegistry<T> registry, T element) {
      return Objects.requireNonNull(registry.getKey(element));
   }

   public static ResourceLocation getBlockKey(Block element) {
      return getKey(ForgeRegistries.BLOCKS, element);
   }

   public static ResourceLocation getItemKey(Item element) {
      return getKey(ForgeRegistries.ITEMS, element);
   }

   public static ResourceLocation getFluidKey(Fluid element) {
      return getKey(ForgeRegistries.FLUIDS, element);
   }

   public static ResourceLocation getMobEffectKey(MobEffect element) {
      return getKey(ForgeRegistries.MOB_EFFECTS, element);
   }

   public static ResourceLocation getBlockEntityTypeKey(BlockEntityType<?> element) {
      return getKey(ForgeRegistries.BLOCK_ENTITY_TYPES, element);
   }

   public static ResourceLocation getEnchantmentKey(Enchantment element) {
      return getKey(ForgeRegistries.ENCHANTMENTS, element);
   }

   public static ResourceLocation getVillageProfessionKey(VillagerProfession element) {
      return getKey(ForgeRegistries.VILLAGER_PROFESSIONS, element);
   }

   @Nonnull
   public static Block getBlockValue(ResourceLocation key) {
      return Objects.requireNonNull((Block)ForgeRegistries.BLOCKS.getValue(key));
   }

   @Nonnull
   public static Item getItemValue(ResourceLocation key) {
      return Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(key));
   }

   @Nonnull
   public static Fluid getFluidValue(ResourceLocation key) {
      return Objects.requireNonNull((Fluid)ForgeRegistries.FLUIDS.getValue(key));
   }
}
