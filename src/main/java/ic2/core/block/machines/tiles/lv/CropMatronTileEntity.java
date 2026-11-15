package ic2.core.block.machines.tiles.lv;

import ic2.api.util.DirectionList;
import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.crops.CropTileEntity;
import ic2.core.block.machines.containers.mv.CropmatronContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.geometry.Box;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CropMatronTileEntity extends BaseElectricTileEntity implements ITickListener, ITileGui, IAreaOfEffect {
   Iterator<BlockPos> area = null;
   FluidTank tank = new FluidTank(16000);
   int networkId = -1;
   public IFilter[] filters;

   public CropMatronTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 9, 32, 1500);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new TankComparator("tank", ComparatorNames.TANK, this.tank));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      this.filters = new IFilter[]{new SimpleFilter(IC2Items.FERTILIZER), new SimpleFilter(IC2Items.HYDRATION_CELL), new SimpleFilter(IC2Items.WEEDEX)};
      int[] array = MathUtils.fromTo(0, 9);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerSlotAccess(AccessRule.BOTH, array);
      handler.registerSlotsForSide(DirectionList.ALL, array);
      handler.registerInputFilter(this.filters[0], 0, 1, 2);
      handler.registerInputFilter(this.filters[1], 3, 4, 5);
      handler.registerInputFilter(this.filters[2], 6, 7, 8);
      handler.registerNamedSlot(SlotType.FERTILIZER, 0, 1, 2);
      handler.registerNamedSlot(SlotType.HYDRATION, 3, 4, 5);
      handler.registerNamedSlot(SlotType.WEED_EX, 6, 7, 8);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("tank", this.tank.writeToNBT(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CropmatronContainer(this, player, windowID);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CROP_MATRON;
   }

   public void setWaterlogged(boolean drained) {
      this.withState(BlockStateProperties.WATERLOGGED, Boolean.valueOf(drained));
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      if (this.hasEnergy(31) && !this.isRedstonePowered()) {
         if (this.area == null) {
            this.area = Box.fromPos(this.worldPosition.offset(-4, -2, -4), this.worldPosition.offset(4, 2, 4)).asInfiniteIterator();
         }

         this.useEnergy(1);
         this.setWaterlogged(this.tank.drain(1, FluidAction.EXECUTE).getAmount() > 0);
         if (this.getLevel().getBlockEntity(this.area.next()) instanceof CropTileEntity crop) {
            if (((ItemStack)this.inventory.get(0)).getItem() == IC2Items.FERTILIZER && crop.applyFertilizer(false)) {
               this.useEnergy(10);
               ((ItemStack)this.inventory.get(0)).shrink(1);
               this.refillTopSlot(0);
            }

            if (crop.applyAutomatedHydration(this.tank)
               || ((ItemStack)this.inventory.get(3)).getItem() == IC2Items.HYDRATION_CELL && crop.applyAutomatedHydration((ItemStack)this.inventory.get(3))) {
               this.useEnergy(10);
               this.refillTopSlot(3);
               this.refillCell();
            }

            if (((ItemStack)this.inventory.get(6)).getItem() == IC2Items.WEEDEX && crop.applyWeedEx(false)) {
               this.useEnergy(10);
               if (((ItemStack)this.inventory.get(6)).hurt(1, this.getLevel().random, null)) {
                  ((ItemStack)this.inventory.get(6)).shrink(1);
               }

               this.refillTopSlot(6);
            }
         }

         this.handleComparators();
      } else {
         this.handleComparators();
      }
   }

   public void refillCell() {
      int damage = ((ItemStack)this.inventory.get(3)).getDamageValue();
      if (damage > 0) {
         FluidStack drained = this.tank.drain(damage, FluidAction.EXECUTE);
         if (drained.getAmount() > 0) {
            ((ItemStack)this.inventory.get(3)).hurt(-drained.getAmount(), this.getLevel().random, null);
         }
      }
   }

   public void refillTopSlot(int topSlot) {
      this.transferStack(topSlot + 1, topSlot);
      this.transferStack(topSlot + 2, topSlot + 1);
   }

   @Override
   public AABB getAreaOfEffect() {
      return new AABB(this.worldPosition.offset(-4, -2, -4), this.worldPosition.offset(5, 3, 5));
   }

   @Override
   public int getAreaOfEffectColor() {
      return -2131783674;
   }

   @Override
   public void setVisualizationId(int id) {
      this.networkId = id;
   }

   @Override
   public int getVisualizationId() {
      return this.networkId;
   }
}
