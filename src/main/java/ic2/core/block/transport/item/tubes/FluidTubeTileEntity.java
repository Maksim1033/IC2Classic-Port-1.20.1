package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.FilteredCapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.block.transport.fluid.helper.FluidCache;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.FluidTubeContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidTubeTileEntity extends TubeTileEntity implements IFluidPipe, ITileGui, ICopyableSettings {
   public ICache<IFluidHandler> fluidCache = new FilteredCapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.FLUID_HANDLER, this::isValidPipe);
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int pipeConnections;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int emitters = 0;
   @NetworkInfo
   public FluidCache<FluidTubeTileEntity> cache = new FluidCache<>("cache", this, this.fluidCache, this::canConnect);

   public FluidTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCaches(new ICache[]{this.fluidCache});
      this.addNetworkFields(new String[]{"pipeConnections", "cache", "emitters"});
   }

   private boolean isValidPipe(BlockEntity tile) {
      if (tile instanceof IFluidPipe pipe && !pipe.isFluidAcceptor()) {
         return false;
      }

      return true;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FLUID_TUBE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "fluid_connectivity", this.pipeConnections, 0);
      NBTUtils.putByte(compound, "emitters", this.emitters, 0);
      this.cache.save(compound, true);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.pipeConnections = compound.getInt("fluid_connectivity");
      this.emitters = compound.getInt("emitters");
      this.cache.load(compound);
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      NBTUtils.putByte(compound, "emitters", this.emitters, 0);
      this.cache.save(compound, true);
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.emitters = compound.getInt("emitters");
      this.cache.load(compound);
      this.updateGuiField("emitters");
      this.cache.update();
   }

   @Override
   public int getConnectivity() {
      return super.getConnectivity() | this.pipeConnections | this.cache.getTanks();
   }

   @Override
   public int getValidDirections() {
      return this.connectivity | this.pipeConnections | this.cache.getTanks();
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("cache") || fields.contains("emitters") || fields.contains("pipeConnections")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ModelData getModelData() {
      Builder builder = ModelData.builder()
         .with(CableProperty.INSTANCE, (this.pipeConnections | this.connectivity) << 6 | this.anchors)
         .with(CableProperty.PIPE_EXTRACTION, this.emitters);
      if (this.foamed == 2) {
         builder.with(
            CamouflageProperty.INSTANCE,
            (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
         );
      }

      return builder.build();
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return !this.fluidCache.isEmpty()
         && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
         && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidTubeContainer(this, player, windowID);
   }

   @Override
   public void onLoaded() {
      this.cache.protect(() -> super.onLoaded());
      this.cache.add();
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.cache.remove();
      super.onUnloaded(chunk);
   }

   @Override
   public boolean canPushFluid(Direction dir) {
      return ((this.pipeConnections | this.cache.getTanks()) & 1 << dir.get3DDataValue()) != 0;
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return ((this.pipeConnections | this.cache.getTanks()) & 1 << dir.get3DDataValue()) != 0;
   }

   @Override
   public List<IFluidPipe.FluidOutput> getOutputs() {
      return this.cache.getOutputs();
   }

   @Override
   public Iterable<Direction> getEmitterSources() {
      return DirectionList.ofNumber(this.emitters);
   }

   public boolean canConnect(Direction dir, IFluidPipe pipe) {
      return !this.hasAnchor(dir) && !pipe.hasAnchor(dir.getOpposite());
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key >= 0 && key <= 3) {
         FluidTubeContainer container = this.getContainer(entity, FluidTubeContainer.class);
         if (container != null) {
            switch (key) {
               case 0:
                  container.data.setFacing(container, Mth.clamp(value, 0, 5));
                  return;
               case 1:
                  container.data.setPriority(value);
                  break;
               case 2:
                  container.data.setWhiteList(value == 1);
                  break;
               case 3:
                  Direction dir = Direction.from3DDataValue(container.data.getDir());
                  if (this.hasEmitter(dir)) {
                     this.removeEmitter(dir);
                  } else {
                     this.addEmitter(dir);
                  }
            }
         }
      }
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      int newValue = this.cache.updateConnectivity();
      if (newValue != this.pipeConnections) {
         this.pipeConnections = newValue;
         this.updateTileField("pipeConnections");
      }
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering() && this.emitters > 0) {
         if (this.cache.isAdded() && !this.invClock(5)) {
            for (Direction dir : DirectionList.ofNumber(this.emitters)) {
               IFluidHandler handler = this.fluidCache.getHandler(dir);
               if (handler != null) {
                  int drained = FluidNet.INSTANCE.sendFluid(this, dir, handler.drain(100, FluidAction.SIMULATE), 25);
                  if (drained > 0) {
                     handler.drain(drained, FluidAction.EXECUTE);
                  }
               }
            }
         }
      }
   }

   public boolean hasEmitter(Direction side) {
      return (this.emitters & 1 << side.get3DDataValue()) != 0;
   }

   public boolean addEmitter(Direction side) {
      if (!this.hasEmitter(side)) {
         this.emitters = this.emitters | 1 << side.get3DDataValue();
         this.updateTileField("emitters");
         this.cache.update();
         this.addToTick();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeEmitter(Direction side) {
      if (this.hasAnchor(side)) {
         this.anchors = this.anchors & ~(1 << side.get3DDataValue());
         this.updateTileField("emitters");
         this.cache.update();
         return true;
      } else {
         return false;
      }
   }
}
