package ic2.core.block.generators.tiles;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.ISteamReactorChamber;
import ic2.api.util.DirectionList;
import ic2.core.block.base.tiles.impls.BaseReactorChamberTileEntity;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.platform.registries.IC2Tiles;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SteamReactorChamberTileEntity extends BaseReactorChamberTileEntity implements ISteamReactorChamber, IFluidPipe {
   public IReactor reactor;
   boolean addedToFluidNet = false;

   public SteamReactorChamberTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public IReactor getReactor() {
      if (this.reactor != null && ((BlockEntity)this.reactor).isRemoved()) {
         this.reactor = null;
      }

      if (this.reactor == null) {
         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = DirectionList.getNeighborTile(this, dir);
            if (tile instanceof ISteamReactor) {
               this.reactor = (IReactor)tile;
            }
         }
      }

      if (this.reactor == null) {
         this.getBlockState().neighborChanged(this.getLevel(), this.getBlockPos(), Blocks.AIR, this.getBlockPos(), false);
      }

      return this.reactor;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.STEAM_CHAMBER;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.addedToFluidNet && this.isSimulating()) {
         this.addedToFluidNet = true;
         FluidNet.INSTANCE.addPipe(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.addedToFluidNet && this.isSimulating()) {
         this.addedToFluidNet = false;
         FluidNet.INSTANCE.removePipe(this);
      }

      super.onUnloaded(chunk);
   }

   protected SteamReactorChamberTileEntity.ReactorInfo getReactorHandler() {
      IReactor handler = this.getReactor();
      return handler instanceof IFluidHandler ? new SteamReactorChamberTileEntity.ReactorInfo(handler, (IFluidHandler)handler, this.worldPosition) : null;
   }

   @Override
   public boolean hasAnchor(Direction side) {
      return false;
   }

   @Override
   public boolean addAnchor(Direction side) {
      return false;
   }

   @Override
   public boolean removeAnchor(Direction side) {
      return false;
   }

   @Override
   public boolean canPushFluid(Direction dir) {
      return true;
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return true;
   }

   @Override
   public List<IFluidPipe.FluidOutput> getOutputs() {
      SteamReactorChamberTileEntity.ReactorInfo info = this.getReactorHandler();
      return info == null ? Collections.emptyList() : Collections.singletonList(info.create(this));
   }

   @Override
   public Iterable<Direction> getEmitterSources() {
      return ObjectLists.emptyList();
   }

   protected static class ReactorInfo {
      IFluidHandler handler;
      BlockPos position;
      Direction direction;

      protected ReactorInfo(IReactor reactor, IFluidHandler handler, BlockPos source) {
         this.handler = handler;
         this.position = reactor.getPosition();
         BlockPos diff = this.position.subtract(source);
         this.direction = Direction.fromDelta(diff.getX(), diff.getY(), diff.getZ());
      }

      protected IFluidPipe.FluidOutput create(IFluidPipe pipe) {
         return new IFluidPipe.FluidOutput(pipe, this.position, this.direction, this.handler);
      }
   }
}
