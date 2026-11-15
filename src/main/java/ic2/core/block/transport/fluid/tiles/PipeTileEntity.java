package ic2.core.block.transport.fluid.tiles;

import ic2.api.blocks.PainterHelper;
import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.FilteredCapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.block.transport.fluid.FluidPipeContainer;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.block.transport.fluid.helper.FluidCache;
import ic2.core.block.transport.fluid.helper.FluidCacheEntry;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PipeTileEntity
   extends BaseTileEntity
   implements IFluidPipe,
   ITileGui,
   IClickable,
   ICamouflagable,
   ISpecialWrenchable,
   ITickListener,
   ICopyableSettings {
   public ICache<IFluidHandler> fluidCache = new FilteredCapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.FLUID_HANDLER, this::isValidPipe);
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int connectivity = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int emitters = 0;
   @NetworkInfo
   public FluidCache<PipeTileEntity> cache = new FluidCache<>("cache", this, this.fluidCache, this::canConnect);
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int anchors = 0;
   @NetworkInfo
   public byte foamed = 0;
   @NetworkInfo
   public CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);

   public PipeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCaches(new ICache[]{this.fluidCache});
      this.addNetworkFields(new String[]{"connectivity", "emitters", "anchors", "foamed", "storage", "cache"});
   }

   private boolean isValidPipe(BlockEntity tile) {
      if (tile instanceof IFluidPipe pipe && !pipe.isFluidAcceptor()) {
         return false;
      }

      return true;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PIPE;
   }

   protected boolean hasFluidOutputs() {
      return true;
   }

   protected boolean overrideBlockUpdate() {
      return false;
   }

   @Override
   public boolean needsUpdateTick() {
      return super.needsUpdateTick() && this.emitters > 0;
   }

   @Override
   public void onTick() {
      if (this.emitters <= 0) {
         if (!this.needsUpdateTick()) {
            this.removeFromTick();
         }
      } else if (this.cache.isAdded() && !this.invClock(5)) {
         for (Direction dir : DirectionList.ofNumber(this.emitters)) {
            IFluidHandler handler = this.fluidCache.getHandler(dir);
            if (handler != null) {
               FluidCacheEntry entry = this.cache.getDir(dir);
               if (entry.outputFilter.isEmpty()) {
                  int drained = FluidNet.INSTANCE.sendFluid(this, dir, handler.drain(this.getTransferRate(), FluidAction.SIMULATE), this.getPressure());
                  if (drained > 0) {
                     handler.drain(drained, FluidAction.EXECUTE);
                  }
               } else {
                  int transfer = this.getTransferRate();
                  int i = 0;

                  for (int m = handler.getTanks(); i < m; i++) {
                     FluidStack stack = handler.getFluidInTank(i);
                     if (!stack.isEmpty() && entry.outputFilter.contains(stack.getFluid())) {
                        FluidStack copy = new FluidStack(stack, Math.min(transfer, stack.getAmount()));
                        copy.setAmount(FluidNet.INSTANCE.sendFluid(this, dir, handler.drain(copy, FluidAction.SIMULATE), this.getPressure()));
                        if (copy.getAmount() > 0) {
                           transfer -= copy.getAmount();
                           handler.drain(copy, FluidAction.EXECUTE);
                           if (transfer <= 0) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public int getTransferRate() {
      return 100;
   }

   public int getPressure() {
      return 25;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.remove("facing");
      NBTUtils.putByte(compound, "connections", this.connectivity, 0);
      NBTUtils.putByte(compound, "emitters", this.emitters, 0);
      NBTUtils.putByte(compound, "anchors", this.anchors, 0);
      NBTUtils.putByte(compound, "foamed", this.foamed, 0);
      NBTUtils.putIf(compound, "camouflage", this.storage.save(new CompoundTag()), this.foamed > 0);
      this.cache.save(compound, this.hasFluidOutputs());
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.connectivity = compound.getInt("connectivity");
      this.emitters = compound.getInt("emitters");
      this.anchors = compound.getInt("anchors");
      this.foamed = compound.getByte("foamed");
      this.storage.load(compound.getCompound("camouflage"));
      this.cache.load(compound);
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      NBTUtils.putByte(compound, "emitters", this.emitters, 0);
      this.cache.save(compound, this.hasFluidOutputs());
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.emitters = compound.getInt("emitters");
      this.cache.load(compound);
      this.updateGuiField("emitters");
      this.cache.update();
      if (this.emitters > 0) {
         this.addToTick();
      }
   }

   @Override
   public AABB hasSpecialAction(Direction side, Vec3 hit, Player player) {
      float radius = this.getCableWidth() / 16.0F;
      Direction dir = ITubeBlock.isClickingAt(hit, side, radius);
      return dir != null && this.hasAnchor(dir) ? ITubeBlock.getHitBox(hit, side, radius) : null;
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      Direction dir = ITubeBlock.isClickingAt(hit, side, this.getCableWidth() / 16.0F);
      if (dir != null && this.hasAnchor(dir)) {
         this.removeAnchor(dir);
         StackUtil.addOrPop(player, this.getBlockPos(), new ItemStack(IC2Blocks.MINING_PIPE_SHAFT));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public CamouflageStorage getStorage() {
      return this.storage;
   }

   @Override
   public boolean isSideEnabled(Direction dir) {
      return this.foamed == 2;
   }

   @Override
   public boolean removeCamouflage() {
      return this.changeFoam((byte)0, false);
   }

   @Override
   public ItemLike getCamouflageDrop() {
      DyeColor color = this.storage.isSingleColor();
      return (ItemLike)(color == null ? Items.AIR : ColorMaps.CFOAM_BLOCKS.getBlock(color));
   }

   @Override
   public boolean applyTexture(Direction dir, RetextureEvent.TextureContainer container) {
      if (this.foamed == 2 && this.storage.canApply(dir, container)) {
         this.storage.setTexture(DirectionList.ofFacing(dir), container);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   public boolean setColor(Direction dir, DyeColor color) {
      if (this.foamed == 2 && this.isSimulating() && this.storage.canApply(dir == null ? DirectionList.ALL : DirectionList.ofFacing(dir), color)) {
         this.storage.setColor(dir == null ? DirectionList.ALL : DirectionList.ofFacings(dir), color);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.isEmpty()) {
         return false;
      } else if (stack.is(ItemTags.SAND) && this.foamed == 1) {
         this.changeFoam((byte)2, false);
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return true;
      } else if (stack.is(IC2Blocks.CFOAM_WET.asItem()) && this.foamed == 0) {
         this.changeFoam((byte)1, false);
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return true;
      } else {
         if (stack.is(IC2Blocks.MINING_PIPE_SHAFT.asItem())) {
            Direction click = ITubeBlock.isClickingAt(
               hit.getLocation().subtract((double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ()),
               side,
               this.getCableWidth() / 16.0F
            );
            if (click != null) {
               this.addAnchor(click);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         } else if (this.foamed == 1) {
            BlockState dustState = Block.byItem(stack.getItem()).defaultBlockState();
            if (PainterHelper.CONCRETE_DUST.contains(dustState.getBlock())) {
               this.storage.setColor(DirectionList.ALL, PainterHelper.INSTANCE.getColor(dustState));
               this.changeFoam((byte)2, false);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return !this.fluidCache.isEmpty() && this.hasFluidOutputs();
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidPipeContainer(this, player, windowID);
   }

   @Override
   public boolean canPushFluid(Direction dir) {
      return (this.getValidDirections() & 1 << dir.get3DDataValue()) != 0;
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return (this.getValidDirections() & 1 << dir.get3DDataValue()) != 0;
   }

   @Override
   public List<IFluidPipe.FluidOutput> getOutputs() {
      return this.cache.getOutputs();
   }

   @Override
   public Iterable<Direction> getEmitterSources() {
      return DirectionList.ofNumber(this.emitters);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("connectivity")
         || fields.contains("emitters")
         || fields.contains("cache")
         || fields.contains("anchors")
         || fields.contains("foamed")
         || fields.contains("storage")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key >= 0 && key <= 4) {
         FluidPipeContainer container = this.getContainer(entity, FluidPipeContainer.class);
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
                     FluidCacheEntry entry = this.cache.getDir(dir);
                     if (entry.filter.isEmpty()) {
                        entry.setWhitelist(true);
                     }
                  } else {
                     this.addEmitter(dir);
                     FluidCacheEntry entry = this.cache.getDir(dir);
                     if (entry.filter.isEmpty()) {
                        entry.setWhitelist(false);
                     }
                  }
                  break;
               case 4:
                  container.data.setInputs(container, value == 1);
            }
         }
      }
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.getValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false))) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
      } else if (this.getValue(IC2Properties.LAVA_LOGGED, Boolean.valueOf(false))) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.LAVA, Fluids.LAVA.getTickDelay(this.getLevel()));
      }

      this.updateConnectivity();
   }

   protected void updateConnectivity() {
      int newValue = this.cache.updateConnectivity();
      if (newValue != this.connectivity) {
         this.connectivity = newValue;
         this.updateTileField("connectivity");
      }
   }

   public boolean canConnect(Direction dir, IFluidPipe pipe) {
      return !this.hasAnchor(dir) && !pipe.hasAnchor(dir.getOpposite());
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
      if (this.hasEmitter(side)) {
         this.emitters = this.emitters & ~(1 << side.get3DDataValue());
         this.updateTileField("emitters");
         this.cache.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean hasAnchor(Direction side) {
      return (this.anchors & 1 << side.get3DDataValue()) != 0;
   }

   @Override
   public boolean addAnchor(Direction side) {
      if (this.hasAnchor(side)) {
         return false;
      } else {
         this.anchors = this.anchors | 1 << side.get3DDataValue();
         this.updateTileField("anchors");
         this.cache.update();
         return true;
      }
   }

   @Override
   public boolean removeAnchor(Direction side) {
      if (this.hasAnchor(side)) {
         this.anchors = this.anchors & ~(1 << side.get3DDataValue());
         this.updateTileField("anchors");
         this.cache.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      this.cache.add();
      if (this.isSimulating()) {
         this.tickFoamed();
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.cache.remove();
      super.onUnloaded(chunk);
   }

   public int getConnectivity() {
      return this.connectivity | this.anchors | this.cache.getTanks();
   }

   public int getValidDirections() {
      return this.connectivity | this.cache.getTanks();
   }

   public int getEmitters() {
      return this.emitters;
   }

   public int getAnchors() {
      return this.anchors;
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.foamed == 2
         ? ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
            )
            .build()
         : ModelData.builder()
            .with(CableProperty.INSTANCE, this.getValidDirections() << 6 | this.anchors)
            .with(CableProperty.PIPE_EXTRACTION, this.getEmitters())
            .build();
   }

   public float getCableWidth() {
      BlockState state = this.getBlockState();
      return state.getBlock() instanceof ITubeBlock tube ? tube.getRadius(state) : -1.0F;
   }

   public boolean changeFoam(byte level, boolean duringLoad) {
      if (this.foamed == level) {
         return false;
      } else {
         if (this.isSimulating()) {
            if ((this.foamed = level) == 1) {
               this.storage.setColor(DirectionList.ALL, DyeColor.LIGHT_GRAY);
               if (!duringLoad) {
                  this.updateTileField("storage");
               }

               this.tickFoamed();
            }

            if (!duringLoad) {
               this.updateTileField("foamed");
               this.onStateChanged();
            }
         }

         return true;
      }
   }

   private void tickFoamed() {
      if (this.foamed == 1) {
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), world -> {
            if (!this.isRemoved() && this.foamed == 1) {
               if (!world.dimension().equals(Level.NETHER)) {
                  if (world.getMaxLocalRawBrightness(this.worldPosition) * 6 > world.random.nextInt(1000)) {
                     this.changeFoam((byte)2, false);
                     return 0;
                  } else {
                     return 500;
                  }
               } else if (world.random.nextInt(5) == 0) {
                  this.changeFoam((byte)2, false);
                  return 0;
               } else {
                  return 20;
               }
            } else {
               return 0;
            }
         });
      }
   }
}
