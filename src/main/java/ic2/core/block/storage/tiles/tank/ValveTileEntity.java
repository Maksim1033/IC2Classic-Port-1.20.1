package ic2.core.block.storage.tiles.tank;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.apache.commons.lang3.mutable.MutableObject;

public class ValveTileEntity extends BaseValveTileEntity {
   @NetworkInfo
   public ValveTileEntity.RenderData render = new ValveTileEntity.RenderData();

   public ValveTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"render"});
      this.wrapper.setMapper(this::handleFluids);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ANIMATED_VALVE;
   }

   private MutableObject<Object> handleFluids(Capability<?> cap, MutableObject<Object> obj) {
      if (cap == ForgeCapabilities.FLUID_HANDLER && obj.getValue() instanceof IFluidHandler) {
         obj.setValue(new ValveTileEntity.LinkingTank((IFluidHandler)obj.getValue(), this.render));
      }

      return obj;
   }

   public IFluidHandler getHandler(IFluidHandler handler) {
      return new ValveTileEntity.LinkingTank(handler, this.render);
   }

   static class LinkingTank implements IFluidHandler {
      IFluidHandler master;
      ValveTileEntity.RenderData render;

      public LinkingTank(IFluidHandler master, ValveTileEntity.RenderData render) {
         this.master = master;
         this.render = render;
      }

      public int getTanks() {
         return this.master.getTanks();
      }

      public FluidStack getFluidInTank(int tank) {
         return this.master.getFluidInTank(tank);
      }

      public int getTankCapacity(int tank) {
         return this.master.getTankCapacity(tank);
      }

      public boolean isFluidValid(int tank, FluidStack stack) {
         return this.master.isFluidValid(tank, stack);
      }

      public int fill(FluidStack resource, FluidAction action) {
         int added = this.master.fill(resource, action);
         if (added > 0) {
            this.render.addFluid();
         }

         return added;
      }

      public FluidStack drain(FluidStack resource, FluidAction action) {
         return this.master.drain(resource, action);
      }

      public FluidStack drain(int maxDrain, FluidAction action) {
         return this.master.drain(maxDrain, action);
      }
   }

   public class RenderData implements INetworkDataBuffer {
      long lastTime;

      public void addFluid() {
         if (this.lastTime < ValveTileEntity.this.getLevel().getGameTime()) {
            this.lastTime = ValveTileEntity.this.getLevel().getGameTime();
            ValveTileEntity.this.updateTileField("render");
         }
      }

      @Override
      public void write(IOutputBuffer buffer) {
         buffer.writeLong(this.lastTime);
      }

      @Override
      public void read(IInputBuffer buffer) {
         this.lastTime = buffer.readLong();
      }

      public boolean shouldRender() {
         return ValveTileEntity.this.getLevel().getGameTime() <= this.lastTime + 20L;
      }
   }
}
