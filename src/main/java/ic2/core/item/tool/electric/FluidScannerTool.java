package ic2.core.item.tool.electric;

import ic2.api.items.electric.IFluidScanner;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.inv.inventory.FluidScannerInventory;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class FluidScannerTool extends ScannerTool implements IFluidScanner {
   public FluidScannerTool(String itemName, int cost, int radius, boolean ore, String texture) {
      super(itemName, cost, radius, ore, texture);
   }

   @Override
   public int getOreValue(ItemStack stack, BlockState state) {
      return this.isValuableFluid(stack, state.getFluidState()) ? 1 : 0;
   }

   public static class FluidScanner extends FluidScannerTool {
      public FluidScanner() {
         super("fluid_scanner", 100, 20, false, "fluid");
         this.tier = 1;
         this.transferLimit = 50;
         this.hasScan = false;
      }

      @Override
      public boolean isValuableFluid(ItemStack stack, FluidState state) {
         return state.getType() != Fluids.EMPTY && state.isSource();
      }

      @Override
      protected List<Block> getTargets(ItemStack stack) {
         List<Block> blocks = CollectionUtils.<Block>createList();

         for (Fluid fluid : ForgeRegistries.FLUIDS) {
            FluidState state = fluid.defaultFluidState();
            if (fluid != Fluids.EMPTY && state.isSource() && state.createLegacyBlock().getBlock() != Blocks.AIR) {
               blocks.add(state.createLegacyBlock().getBlock());
            }
         }

         return blocks;
      }
   }

   public static class FluidScannerFiltered extends FluidScannerTool implements IHasHeldSlotInventory {
      public FluidScannerFiltered() {
         super("fluid_scanner_filtered", 100, 20, false, "filtered_fluid");
         this.tier = 2;
         this.transferLimit = 100;
         this.hasScan = false;
      }

      @Override
      public boolean isValuableFluid(ItemStack stack, FluidState state) {
         if (!state.isEmpty() && state.isSource()) {
            Fluid fluid = state.getType();

            for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("fluids", 10), CompoundTag.class)) {
               Fluid stored = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(nbt.getString("fluid")));
               if (stored != Fluids.EMPTY && stored == fluid) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      protected List<Block> getTargets(ItemStack stack) {
         List<Block> blocks = CollectionUtils.<Block>createList();

         for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("fluids", 10), CompoundTag.class)) {
            FluidState state = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(nbt.getString("fluid"))).defaultFluidState();
            if (!state.isEmpty() && state.createLegacyBlock().getBlock() != Blocks.AIR) {
               blocks.add(state.createLegacyBlock().getBlock());
            }
         }

         return blocks;
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
         if (IC2.KEYBOARD.isSideInventoryKeyDown(playerIn)) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, stack));
            return InteractionResultHolder.success(stack);
         } else {
            return super.use(worldIn, playerIn, handIn);
         }
      }

      @Override
      public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
         return new FluidScannerInventory(player, this, stack, null).load(stack);
      }

      @Override
      public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
         return new FluidScannerInventory(player, this, stack, slot).load(stack);
      }
   }
}
