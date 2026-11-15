package ic2.probeplugin.base;

import ic2.api.tiles.readers.IEUStorage;
import ic2.core.block.machines.tiles.hv.TeleporterTileEntity;
import ic2.core.inventory.handler.IHasInventoryHandler;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.utils.collection.CollectionUtils;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.GridElement;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.override.styles.ITextStyleBuilder;
import ic2.probeplugin.styles.IC2Styles;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

public class ProbePluginHelper {
   public static final DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
   public static final DecimalFormat THERMAL_GEN = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
   public static final DecimalFormat NATURAL = new DecimalFormat("###,##0", new DecimalFormatSymbols(Locale.US));
   public static final DecimalFormat DECIMAL = new DecimalFormat(".#########", new DecimalFormatSymbols(Locale.US));
   public static final String SUFFIX = "kmbt";

   public static IExpandedProbeInfo addTanks(BlockEntity tile, IExpandedProbeInfo element, boolean showEmpty) {
      if (tile instanceof IFluidHandler) {
         addTanks((IFluidHandler)tile, element, showEmpty);
      } else {
         LazyOptional<IFluidHandler> optional = tile.getCapability(ForgeCapabilities.FLUID_HANDLER);
         if (optional.isPresent()) {
            addTanks((IFluidHandler)optional.orElse(EmptyFluidHandler.INSTANCE), element, showEmpty);
         }
      }

      return element;
   }

   public static void addTanks(IFluidHandler fluidHandler, IExpandedProbeInfo element, boolean showEmpty) {
      for (int i = 0; i < fluidHandler.getTanks(); i++) {
         FluidStack fluidStack = fluidHandler.getFluidInTank(i);
         if (fluidStack.isEmpty() && showEmpty || !fluidStack.isEmpty()) {
            element.tankSimple(
               fluidHandler.getTankCapacity(i),
               fluidStack,
               IC2Styles.tank(
                  fluidStack.getFluid() == Fluids.LAVA ? -29925 : IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor() | 0xFF000000,
                  fluidStack.isEmpty() ? Component.literal("Empty") : (MutableComponent)fluidStack.getDisplayName(),
                  fluidStack.getAmount(),
                  fluidHandler.getTankCapacity(i)
               )
            );
         }
      }
   }

   public static String formatInt(int number, int digits) {
      return formatInt(number, digits, false);
   }

   public static String formatNumber(double number, int digits) {
      return formatNumber(number, digits, false);
   }

   public static String formatInt(int number, int digits, boolean fixedLength) {
      return formatNumber((double)number, digits, fixedLength);
   }

   public static String formatNumber(double number, int digits, boolean fixedLength) {
      String suffix = "";
      boolean allow = (number >= 1.0E9 ? String.valueOf((long)number) : String.valueOf(number)).length() > digits;
      double outputNumber = number;

      for (int i = 0; i < "kmbt".length() && outputNumber >= 1000.0 && allow; i++) {
         outputNumber /= 1000.0;
         suffix = Character.toString("kmbt".charAt(i));
      }

      int actualDigits = digits - suffix.length();
      if (outputNumber % 1.0 == 1.0) {
         actualDigits++;
      }

      int naturalLength = NATURAL.format((long)((int)outputNumber)).length();
      int decimalLength = DECIMAL.format(outputNumber - (double)((int)outputNumber)).length();
      StringBuilder patternBuilder = new StringBuilder();

      for (int i = 1; actualDigits > 1 && naturalLength > 1; i++) {
         patternBuilder.insert(0, "#");
         actualDigits--;
         naturalLength--;
         if (i % 2 == 0 && actualDigits > 1 && naturalLength > 1) {
            if (actualDigits == 2 || naturalLength == 2) {
               break;
            }

            patternBuilder.insert(0, ",");
            actualDigits--;
            naturalLength--;
         }
      }

      patternBuilder.append("0");
      if (actualDigits > 1 && decimalLength > 0) {
         patternBuilder.append(".");
         actualDigits--;

         while (actualDigits > 0 && decimalLength > 0) {
            patternBuilder.append("#");
            actualDigits--;
            decimalLength--;
         }
      }

      String pattern = patternBuilder.toString();
      String output = new DecimalFormat(pattern + suffix, US).format(outputNumber);
      String fill = "";
      int length = output.length();
      if (fixedLength && output.length() < digits) {
         for (int ix = 0; ix < digits - length; ix++) {
            fill = fill.concat(" ");
         }

         output = fill + output;
      }

      return output;
   }

   public static IElement generateEUBar(BlockEntity tile) {
      return generateEUBar(tile, 118, 12, false);
   }

   public static IElement generateHiddenBar(BlockEntity tile) {
      return generateEUBar(tile, 118, 12, true);
   }

   public static IElement generateEUBar(BlockEntity tile, int width) {
      return generateEUBar(tile, width, 12, false);
   }

   public static IElement generateEUBar(BlockEntity tile, int width, int height, boolean hideMax) {
      if (!(tile instanceof IEUStorage eu)) {
         if (tile instanceof TeleporterTileEntity eux) {
            long current = eux.getAvailableEnergy();
            long max = eux.getMaxEnergy();
            int[] range = generateDigitRange(max);
            return new ElementProgress(
               current,
               max,
               IC2Styles.EU_BAR
                  .copy()
                  .numberFormat(NumberFormat.NONE)
                  .prefix("ic2.probe.eu.storage.name", new Object[]{formatNumber((double)current, range[0]) + "/" + formatNumber((double)max, range[1])})
                  .bounds(width, height)
            );
         } else {
            return null;
         }
      } else if ((hideMax || eu.getStoredEU() == eu.getMaxEU() && eu.getMaxEU() < 100000000) && eu.getStoredEU() > 0) {
         return new ElementProgress(
            (long)eu.getStoredEU(),
            (long)eu.getMaxEU(),
            IC2Styles.EU_BAR
               .copy()
               .numberFormat(NumberFormat.NONE)
               .prefix("ic2.probe.eu.storage.name", new Object[]{formatNumber((double)eu.getStoredEU(), 10)})
               .bounds(width, height)
         );
      } else {
         int[] range = generateDigitRange((long)eu.getMaxEU());
         return new ElementProgress(
            (long)eu.getStoredEU(),
            (long)eu.getMaxEU(),
            IC2Styles.EU_BAR
               .copy()
               .numberFormat(NumberFormat.NONE)
               .prefix("ic2.probe.eu.storage.name", new Object[]{formatInt(eu.getStoredEU(), range[0]) + "/" + formatInt(eu.getMaxEU(), range[1])})
               .bounds(width, height)
         );
      }
   }

   private static int[] generateDigitRange(long maxEU) {
      maxEU %= 1000L;
      if (maxEU >= 100L) {
         return new int[]{4, 3};
      } else {
         return maxEU >= 10L ? new int[]{5, 3} : new int[]{6, 2};
      }
   }

   public static Map<Component, List<ItemStack>> generateSlots(BlockEntity tile, boolean showEmpty, SlotType... types) {
      Map<Component, List<ItemStack>> resultMap = CollectionUtils.<Component, List<ItemStack>>createLinkedMap();
      if (tile instanceof IHasInventoryHandler) {
         InventoryHandler inventoryHandler = ((IHasInventoryHandler)tile).getInventoryHandler();

         for (SlotType type : types) {
            List<ItemStack> stacks = CollectionUtils.<ItemStack>createList();
            IntListIterator var10 = inventoryHandler.getAllOfType(type).iterator();

            while (var10.hasNext()) {
               int slot = (Integer)var10.next();
               ItemStack inputStack = inventoryHandler.getInventory().getStackInSlot(slot);
               if (!inputStack.isEmpty()) {
                  stacks.add(inputStack);
               }
            }

            if (showEmpty || !stacks.isEmpty()) {
               if (stacks.isEmpty()) {
                  stacks.add(ItemStack.EMPTY);
               }

               resultMap.put(type.getName().copy().append(": "), stacks);
            }
         }
      }

      return resultMap;
   }

   public static void generateAnySlots(BlockEntity tile, boolean showEmpty, IProbeInfo elements) {
      Map<Component, List<ItemStack>> slots = CollectionUtils.<Component, List<ItemStack>>createLinkedMap();
      if (tile instanceof IHasInventoryHandler) {
         InventoryHandler inventoryHandler = ((IHasInventoryHandler)tile).getInventoryHandler();
         ObjectIterator var5 = inventoryHandler.getSlotsOfType(SlotType.ROOT).keySet().iterator();

         while (var5.hasNext()) {
            SlotType type = (SlotType)var5.next();
            List<ItemStack> stacks = CollectionUtils.<ItemStack>createList();
            IntListIterator var8 = inventoryHandler.getAllOfType(type).iterator();

            while (var8.hasNext()) {
               int slot = (Integer)var8.next();
               ItemStack inputStack = inventoryHandler.getInventory().getStackInSlot(slot);
               if (!inputStack.isEmpty() || showEmpty) {
                  stacks.add(inputStack);
               }
            }

            if (showEmpty || !stacks.isEmpty()) {
               if (stacks.isEmpty()) {
                  stacks.add(ItemStack.EMPTY);
               }

               slots.put(type.getName().copy().append(": "), stacks);
            }
         }
      }

      if (!slots.isEmpty()) {
         Panel mainContainer = new Panel(Panel.Type.VERTICAL);
         mainContainer.text(translate("ic2.probe.slots"), ITextStyleBuilder.create().bottomPadding(2));
         mainContainer.vertical(IC2Styles.SLOT_CONTAINER_STYLE).vertical(IC2Styles.SLOTS_STYLE).element(new GridElement(slots));
         elements.element(mainContainer);
      }
   }

   public static void generateDefaultSlots(BlockEntity tile, boolean showEmpty, IProbeInfo elements) {
      generateSlots(tile, showEmpty, elements, SlotType.INPUTS, SlotType.OUTPUTS, SlotType.BATTERY, SlotType.FUEL, SlotType.CHARGE);
   }

   public static void generateSlots(BlockEntity tile, boolean showEmpty, IProbeInfo elements, SlotType... types) {
      generateSlotInfo(generateSlots(tile, showEmpty, types), elements);
   }

   public static void generateSlotInfo(Component name, List<ItemStack> slots, boolean showEmpty, IProbeInfo info) {
      if (!slots.isEmpty() || showEmpty) {
         generateSlotInfo(Object2ObjectMaps.singleton(name, slots), info);
      }
   }

   public static void generateSlotInfo(Map<Component, List<ItemStack>> slots, IProbeInfo info) {
      if (!slots.isEmpty()) {
         Panel mainContainer = new Panel(Panel.Type.VERTICAL);
         mainContainer.text(translate("ic2.probe.slots"), ITextStyleBuilder.create().bottomPadding(2));
         mainContainer.vertical(IC2Styles.SLOT_CONTAINER_STYLE).vertical(IC2Styles.SLOTS_STYLE).element(new GridElement(slots));
         info.element(mainContainer);
      }
   }

   static Component translate(String s) {
      return Component.translatable(s);
   }
}
