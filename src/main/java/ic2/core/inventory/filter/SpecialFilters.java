package ic2.core.inventory.filter;

import ic2.api.crops.ICropSeed;
import ic2.api.items.IAutoEatable;
import ic2.api.items.IFuelableItem;
import ic2.api.items.ITagBlock;
import ic2.api.items.ITagItem;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.IWindmillBlade;
import ic2.api.items.ItemRegistries;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.items.readers.ICropReader;
import ic2.api.items.readers.IEUReader;
import ic2.api.items.readers.IThermometer;
import ic2.api.recipes.registries.IUUMatterRegistry;
import ic2.core.IC2;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.item.base.features.IHandlerItem;
import ic2.core.item.tool.PainterTool;
import ic2.core.item.upgrades.special.UpgradeContainerItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SpecialFilters {
   static IFilter CHARGE_FILTER_CACHE;
   static IFilter[] DYE_FILTER = null;
   public static final IFilter ALWAYS_FALSE = T -> false;
   public static final IFilter ALWAYS_TRUE = T -> true;
   public static final IFilter ANY_ITEM = T -> !T.isEmpty();
   public static final IFilter NO_TAGS = T -> !T.isEmpty() && !(T.getItem() instanceof ITagItem);
   public static final IFilter TOOL_BOX = T -> ItemRegistries.isBoxable(T) || T.is(IC2Tags.TOOLBOX);
   public static final IFilter BLOCK_FILTER = T -> Block.byItem(T.getItem()) != Blocks.AIR;
   public static final IFilter BLOCK_TAG_FILTER = T -> Block.byItem(T.getItem()) != Blocks.AIR || T.getItem() instanceof ITagBlock;
   public static final IFilter CROP_FILTER = T -> T.getItem() instanceof ICropSeed;
   public static final IFilter CROP_FILTER_SCANNED = T -> T.getItem() instanceof ICropSeed && ((ICropSeed)T.getItem()).getScanLevel(T) >= 4;
   public static final IFilter CROP_HARVESTER_FILTER = T -> {
      Item item = T.getItem();
      return item == IC2Items.OVERCLOCKER_UPGRADE
         || item == IC2Items.QUANTUM_OVERCLOCKER_UPGRADE
         || item == IC2Items.FIELD_PAD_UPGRADE
         || item == IC2Items.ADVANCED_FIELD_PAD_UPGRADE
         || item == IC2Items.BASIC_FIELD_PAD_UPGRADE;
   };
   public static final IFilter BOOK_FILTER = T -> T.getItem() == Items.BOOK || T.getItem() == Items.ENCHANTED_BOOK;
   public static final IFilter ENCHANT_FILTER = T -> T.getItem() == Items.BOOK
         || T.getItem() == Items.ENCHANTED_BOOK
         || T.getItem().isEnchantable(T)
         || T.getItem() instanceof IElectricEnchantable;
   public static final IFilter AUTO_EATABLE = T -> {
      if (T.getItem() instanceof IAutoEatable eatable && eatable.canAutoEat(T)) {
         return true;
      }

      return false;
   };
   public static final IFilter UU_OUTPUT = T -> {
      for (IUUMatterRegistry.UUMatterEntry entry : IC2.RECIPES.get().UU.getEntries()) {
         if (StackUtil.isStackEqual(entry.getStack(), T)) {
            return true;
         }
      }

      return false;
   };
   public static final IFilter HANDLER_ITEM = T -> T.getItem() instanceof IHandlerItem;
   public static final IFilter FUEL_CAN = T -> T.getItem() instanceof IFuelableItem && ((IFuelableItem)T.getItem()).hasFuel(T);
   public static final IFilter EU_READER = IEUReader::isEUReaderImpl;
   public static final IFilter THERMOMETER = IThermometer::isThermometerImpl;
   public static final IFilter CROP_SCANNER = ICropReader::isCropReaderImpl;
   public static final IFilter WINDMILL_ROTOR = T -> T.getItem() instanceof IWindmillBlade;
   public static final IFilter EMPTY_PAINTER = T -> T.getItem() instanceof PainterTool;
   public static final IFilter PAINTER = T -> T.getItem() instanceof PainterTool && ((PainterTool)T.getItem()).getColor() != null;
   public static final IFilter UPGRADE_CONTAINER = T -> T.getItem() instanceof IUpgradeItem && !(T.getItem() instanceof UpgradeContainerItem);

   public static IFilter createChargeFilter() {
      if (CHARGE_FILTER_CACHE == null) {
         CHARGE_FILTER_CACHE = new ArrayOrFilter(
            ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER, new SimpleFilter(Items.REDSTONE), new SimpleFilter(IC2Items.SU_BATTERY)
         );
      }

      return CHARGE_FILTER_CACHE;
   }

   public static IFilter createDyeFilter(DyeColor dye) {
      if (DYE_FILTER == null) {
         DYE_FILTER = new IFilter[16];

         for (DyeColor color : DyeColor.values()) {
            DYE_FILTER[color.getId()] = new TagFilter(ItemTags.create(new ResourceLocation("forge", "dyes/" + color.getName())));
         }
      }

      return DYE_FILTER[dye.getId()];
   }

   public static DyeColor getNextColor(DyeColor prev, boolean allowNull) {
      if (prev == null) {
         if (!allowNull) {
            throw new IllegalStateException("Null Color is not allowed");
         } else {
            return DyeColor.WHITE;
         }
      } else {
         return allowNull && prev == DyeColor.BLACK ? null : DyeColor.byId(prev.getId() + 1);
      }
   }

   public static DyeColor getPrevColor(DyeColor next, boolean allowNull) {
      if (next == null) {
         if (!allowNull) {
            throw new IllegalStateException("Null Color is not allowed");
         } else {
            return DyeColor.BLACK;
         }
      } else {
         return next == DyeColor.WHITE ? (allowNull ? null : DyeColor.BLACK) : DyeColor.byId(next.getId() - 1);
      }
   }

   public static ItemStack createDyeItem(DyeColor dye) {
      if (dye == null) {
         return new ItemStack(IC2Items.PAINTER);
      } else {
         switch (dye) {
            case BLACK:
               return new ItemStack(IC2Items.PAINTER_BLACK);
            case BLUE:
               return new ItemStack(IC2Items.PAINTER_BLUE);
            case BROWN:
               return new ItemStack(IC2Items.PAINTER_BROWN);
            case CYAN:
               return new ItemStack(IC2Items.PAINTER_CYAN);
            case GRAY:
               return new ItemStack(IC2Items.PAINTER_GRAY);
            case GREEN:
               return new ItemStack(IC2Items.PAINTER_GREEN);
            case LIGHT_BLUE:
               return new ItemStack(IC2Items.PAINTER_LIGHT_BLUE);
            case LIGHT_GRAY:
               return new ItemStack(IC2Items.PAINTER_LIGHT_GRAY);
            case LIME:
               return new ItemStack(IC2Items.PAINTER_LIME);
            case MAGENTA:
               return new ItemStack(IC2Items.PAINTER_MAGENTA);
            case ORANGE:
               return new ItemStack(IC2Items.PAINTER_ORANGE);
            case PINK:
               return new ItemStack(IC2Items.PAINTER_PINK);
            case PURPLE:
               return new ItemStack(IC2Items.PAINTER_PURPLE);
            case RED:
               return new ItemStack(IC2Items.PAINTER_RED);
            case WHITE:
               return new ItemStack(IC2Items.PAINTER_WHITE);
            case YELLOW:
               return new ItemStack(IC2Items.PAINTER_YELLOW);
            default:
               return ItemStack.EMPTY;
         }
      }
   }
}
