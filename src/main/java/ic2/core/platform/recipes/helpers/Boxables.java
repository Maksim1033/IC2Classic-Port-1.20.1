package ic2.core.platform.recipes.helpers;

import ic2.api.items.IBoxableItem;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.ItemRegistries;
import ic2.api.items.electric.ICustomElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.core.item.food_and_drink.GlassItem;
import ic2.core.item.food_and_drink.MugItem;
import ic2.core.item.food_and_drink.TinCanItem;
import ic2.core.item.misc.CellItem;
import ic2.core.item.misc.CoinItem;
import ic2.core.item.misc.FuelCanItem;
import ic2.core.item.misc.IC2BoatItem;
import ic2.core.item.tool.PainterTool;
import ic2.core.item.tool.ToolBoxTool;
import ic2.core.item.wearable.base.IC2ShieldBase;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class Boxables implements IBoxableItem {
   public static final Boxables INSTANCE = new Boxables();
   Set<Item> validItems = CollectionUtils.<Item>createSet();
   Set<String> prefixes = CollectionUtils.<String>createLinkedSet();

   public static void loadBoxableItems() {
      INSTANCE.init();
   }

   private void init() {
      this.registerItem(IC2Items.WIKI_ITEM);
      this.registerItem(IC2Items.TREETAP);
      this.registerItem(IC2Items.EU_READER);
      this.registerItem(Items.BUCKET);
      this.registerItem(Items.FLINT_AND_STEEL);
      this.registerItem(Blocks.TORCH.asItem());
      this.registerItem(IC2Blocks.SCAFFOLD_WOOD.asItem());
      this.registerItem(IC2Blocks.SCAFFOLD_IRON.asItem());
      this.registerItem(Items.ELYTRA);
      this.registerItem(IC2Items.CROP_STICKS);
      this.registerItem(Items.CLOCK);
      this.registerItem(Items.COMPASS);
      this.registerItem(Items.BOWL);
      this.registerItem(IC2Items.MINER_REMOTE);
      this.registerItem(IC2Items.CUTTER);
      this.registerItem(IC2Blocks.MINING_PIPE_SHAFT.asItem());
      this.registerItem(IC2Items.HYDRATION_CELL);
      this.registerItem(IC2Items.WEEDEX);
      this.registerItem(Items.CARROT_ON_A_STICK);
      this.registerItem(Items.WARPED_FUNGUS_ON_A_STICK);
      this.registerItem(IC2Items.MEMORY_STICK);
      this.registerItem(IC2Items.STICKY_DYNAMITE);
      this.registerItem(IC2Items.CF_SPRAYER);
      this.registerItem(IC2Items.DYNAMITE_REMOTE);
      this.registerItem(IC2Items.FREQUENCY_TRANSMITTER);
      this.registerItem(Items.MILK_BUCKET);
      this.registerItem(IC2Items.CELL_AIR);
      this.registerItem(Items.SHEARS);
      this.registerItem(IC2Items.TIN_CAN);
      this.registerItem(IC2Blocks.PIPE.asItem());
      this.registerItem(IC2Blocks.DETECTOR_PIPE.asItem());
      this.registerItem(IC2Blocks.SPLITTER_PIPE.asItem());
      this.registerItem(IC2Blocks.SIMPLE_TUBE.asItem());
      this.registerItem(IC2Blocks.SPEED_TUBE.asItem());
      this.registerItem(IC2Blocks.TRANSPORT_TUBE.asItem());
      this.registerItem(IC2Blocks.VOID_TUBE.asItem());
      this.registerItem(IC2Blocks.HOVER_TUBE.asItem());
      this.registerItem(IC2Blocks.DIRECTIONAL_TUBE.asItem());
      this.registerItem(IC2Blocks.FILTER_TUBE.asItem());
      this.registerItem(IC2Blocks.INSERTION_TUBE.asItem());
      this.registerItem(IC2Blocks.EXTRACTOR_TUBE.asItem());
      this.registerItem(IC2Blocks.FILTERED_EXTRACTION_TUBE.asItem());
      this.registerItem(IC2Blocks.STACKING_TUBE.asItem());
      this.registerItem(IC2Blocks.PICKUP_TUBE.asItem());
      this.registerItem(IC2Blocks.REDSTONE_TUBE.asItem());
      this.registerItem(IC2Blocks.SWITCH_TUBE.asItem());
      this.registerItem(IC2Blocks.ROUND_ROBIN_TUBE.asItem());
      this.registerItem(IC2Blocks.TELEPORT_TUBE.asItem());
      this.registerItem(IC2Blocks.DROPPING_TUBE.asItem());
      this.registerItem(IC2Blocks.DYEING_TUBE.asItem());
      this.registerItem(IC2Blocks.COLOR_FILTER_TUBE.asItem());
      this.registerItem(IC2Blocks.REQUEST_TUBE.asItem());
      this.registerItem(IC2Blocks.PROVIDER_TUBE.asItem());
      this.registerItem(IC2Blocks.FLUID_TUBE.asItem());
      this.registerPrefix(
         "tool",
         "wrench",
         "wand",
         "rod",
         "scepter",
         "screwdriver",
         "meter",
         "hammer",
         "magnifying",
         "pipette",
         "grafter",
         "grafter",
         "reader",
         "soldering",
         "meter",
         "handsaw",
         "gun",
         "key",
         "chisel",
         "probe",
         "scoop"
      );

      for (Item item : ForgeRegistries.ITEMS) {
         if (item.isEdible()
            || item instanceof TinCanItem
            || item instanceof MugItem
            || item instanceof GlassItem
            || item instanceof FuelCanItem
            || item instanceof CellItem
            || item instanceof CoinItem
            || item instanceof IC2ShieldBase
            || item instanceof BoatItem
            || item instanceof IC2BoatItem
            || item instanceof MinecartItem
            || item instanceof PainterTool
            || item instanceof PotionItem
            || item instanceof ArrowItem
            || item instanceof SwordItem
            || item instanceof IElectricItem
            || item instanceof ICustomElectricItem
            || item instanceof IUpgradeItem
            || item instanceof DiggerItem
            || item instanceof ArmorItem
            || item instanceof BucketItem
            || item instanceof CrossbowItem
            || item instanceof BowItem
            || item instanceof FishingRodItem
            || this.containsPrefix(item)) {
            this.registerItem(item);
         }
      }
   }

   public void registerItem(Item item) {
      if (!(item instanceof ToolBoxTool)) {
         ItemRegistries.registerBoxableItems(this, item);
         this.validItems.add(item);
      }
   }

   public void registerPrefix(String... s) {
      this.prefixes.addAll(ObjectArrayList.wrap(s));
   }

   private boolean containsPrefix(Item item) {
      String s = ForgeRegistryWrapper.getItemKey(item).getPath();

      for (String value : this.prefixes) {
         if (s.contains(value)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canStoreIntoBox(ItemStack stack) {
      return this.validItems.contains(stack.getItem());
   }
}
