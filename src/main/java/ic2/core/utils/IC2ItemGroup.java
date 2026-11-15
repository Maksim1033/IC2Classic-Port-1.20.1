package ic2.core.utils;

import ic2.core.IC2;
import ic2.core.item.base.IItemCategoryFiller;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class IC2ItemGroup extends CreativeModeTab {
   public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");
   public static final ResourceLocation DEFAULT_TABS_IMAGE = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");

   public IC2ItemGroup() {
      super(CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.ic2"))
         .withBackgroundLocation(DEFAULT_BACKGROUND)
         .withTabsImage(DEFAULT_TABS_IMAGE)
         .icon(() -> {
            ItemStack stack = new ItemStack(IC2Items.MINING_LASER);
            stack.getOrCreateTag().putBoolean("hide_charge_bar", true);
            return stack;
         })
         .displayItems((params, output) -> fillTab(IC2.IC2_MAIN_GROUP, output)));
   }

   protected IC2ItemGroup(CreativeModeTab.Builder builder) {
      super(builder);
   }

   @Override
   public ResourceLocation getBackgroundLocation() {
      ResourceLocation loc = super.getBackgroundLocation();
      return loc != null ? loc : DEFAULT_BACKGROUND;
   }

   @Override
   public ResourceLocation getTabsImage() {
      ResourceLocation loc = super.getTabsImage();
      return loc != null ? loc : DEFAULT_TABS_IMAGE;
   }

   public static void fillTab(CreativeModeTab tab, CreativeModeTab.Output output) {
      NonNullList<ItemStack> list = NonNullList.create();
      for (Item item : ForgeRegistries.ITEMS) {
         if (item != Items.AIR) {
            list.clear();
            if (item instanceof IItemCategoryFiller filler) {
               if (filler.allowedIn(tab)) {
                  filler.m_6787_(tab, list);
                  list.removeIf(ItemStack::isEmpty);
                  output.acceptAll(list);
               }
            } else {
               ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item);
               if (loc != null && loc.getNamespace().equals("ic2") && tab == IC2.IC2_MAIN_GROUP) {
                  output.accept(item);
               }
            }
         }
      }
   }

   public static class IC2CFoamItemGroup extends IC2ItemGroup {
      public IC2CFoamItemGroup() {
         super(CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ic2.cfoam"))
            .withBackgroundLocation(DEFAULT_BACKGROUND)
            .withTabsImage(DEFAULT_TABS_IMAGE)
            .icon(() -> new ItemStack(IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY))
            .displayItems((params, output) -> fillTab(IC2.CFOAM_GROUP, output)));
      }
   }

   public static class IC2FoodAndDinkItemGroup extends IC2ItemGroup {
      public IC2FoodAndDinkItemGroup() {
         super(CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ic2.food_and_drink"))
            .withBackgroundLocation(DEFAULT_BACKGROUND)
            .withTabsImage(DEFAULT_TABS_IMAGE)
            .icon(() -> new ItemStack(IC2Items.MUG_TEA))
            .displayItems((params, output) -> fillTab(IC2.FOOD_AND_DRINK_GROUP, output)));
      }
   }
}
