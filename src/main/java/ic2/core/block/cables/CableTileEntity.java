package ic2.core.block.cables;

import ic2.api.blocks.PainterHelper;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyConductorAnchored;
import ic2.api.energy.tile.IEnergyConductorColored;
import ic2.api.energy.tile.IEnergyConductorModifiable;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public abstract class CableTileEntity
   extends BaseTileEntity
   implements IEnergyConductorColored,
   IEnergyConductorAnchored,
   IEnergyConductorModifiable,
   IDropProvider,
   IClickable,
   ICamouflagable,
   ISpecialWrenchable {
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int connectivity = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int anchors = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int cableColor = 0;
   @NetworkInfo
   public byte foamed = 0;
   @NetworkInfo
   public byte insulation = 0;
   @NetworkInfo
   public CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);
   protected boolean addedToEnergyNet;
   final int maxInsulation;

   public CableTileEntity(BlockPos pos, BlockState state, int max) {
      super(pos, state);
      this.maxInsulation = max;
      this.addNetworkFields(new String[]{"connectivity", "anchors", "cableColor", "insulation", "foamed", "storage"});
   }

   public boolean hasUninsulatedColor() {
      return false;
   }

   public boolean canDye(int insulation) {
      return insulation > 0;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.connectivity = compound.getInt("connections");
      this.anchors = compound.getInt("anchors");
      this.cableColor = compound.getInt("color");
      this.foamed = compound.getByte("foamed");
      this.insulation = NBTUtils.getByte(compound, "insulation", (byte)this.maxInsulation);
      this.storage.load(compound.getCompound("camouflage"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.remove("facing");
      NBTUtils.putByte(compound, "connections", this.connectivity, 0);
      NBTUtils.putByte(compound, "anchors", this.anchors, 0);
      NBTUtils.putByte(compound, "color", this.cableColor, 0);
      NBTUtils.putByte(compound, "foamed", this.foamed, 0);
      NBTUtils.putByte(compound, "insulation", this.insulation, this.maxInsulation);
      NBTUtils.putIf(compound, "camouflage", this.storage.save(new CompoundTag()), this.foamed > 0);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("connectivity") || fields.contains("anchors") || fields.contains("foamed") || fields.contains("storage")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
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
      } else if (IC2Blocks.CFOAM_WET.asItem() == stack.getItem() && this.foamed == 0) {
         this.changeFoam((byte)1, false);
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return true;
      } else {
         if (stack.getItem() == IC2Blocks.MINING_PIPE_SHAFT.asItem()) {
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

   public float getCableWidth() {
      BlockState state = this.getBlockState();
      return state.getBlock() instanceof ITubeBlock ? ((ITubeBlock)state.getBlock()).getRadius(state) : -1.0F;
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

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.addedToEnergyNet && this.isSimulating()) {
         this.addedToEnergyNet = true;
         EnergyNet.INSTANCE.addTile(this);
      }

      if (this.isSimulating()) {
         this.tickFoamed();
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.addedToEnergyNet && this.isSimulating()) {
         this.addedToEnergyNet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Nonnull
   @Override
   public Component getName() {
      return this.getCustomName();
   }

   public int getConnectivity() {
      return this.connectivity | this.anchors;
   }

   public int getAnchors() {
      return this.anchors;
   }

   public int getConnections() {
      return this.connectivity;
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.foamed == 2
         ? ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)R -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), R)
            )
            .build()
         : ModelData.builder().with(CableProperty.INSTANCE, this.connectivity << 6 | this.anchors).build();
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return (this.connectivity & 1 << side.get3DDataValue()) != 0;
   }

   @Override
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return (this.connectivity & 1 << side.get3DDataValue()) != 0;
   }

   @Override
   public void removeInsulation() {
      this.setInsulation(0);
   }

   @Override
   public void removeConductor() {
      this.getLevel().removeBlock(this.getBlockPos(), false);
   }

   @Override
   public DyeColor getColor() {
      return this.cableColor == 0 ? null : DyeColor.byId(this.cableColor - 1);
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
         EnergyNet.INSTANCE.updateTile(this);
         return true;
      }
   }

   @Override
   public boolean removeAnchor(Direction side) {
      if (this.hasAnchor(side)) {
         this.anchors = this.anchors & ~(1 << side.get3DDataValue());
         this.updateTileField("anchors");
         EnergyNet.INSTANCE.updateTile(this);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean tryAddInsulation() {
      if (this.insulation < this.maxInsulation) {
         this.insulation++;
         this.updateTileField("insulation");
         this.onStateChanged();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean tryRemoveInsulation() {
      if (this.insulation > 0) {
         this.insulation--;
         if (this.insulation == 0) {
            this.cableColor = 0;
            this.updateTileField("cableColor");
            EnergyNet.INSTANCE.updateTile(this);
         }

         this.updateTileField("insulation");
         this.onStateChanged();
         return true;
      } else {
         return false;
      }
   }

   public boolean setColor(Direction dir, DyeColor color) {
      if (this.foamed != 0 || this.cableColor != (color == null ? 0 : color.getId() + 1) && this.canDye(this.insulation)) {
         if (this.foamed == 1) {
            return false;
         } else if (this.foamed == 2 && !this.storage.canApply(dir == null ? DirectionList.ALL : DirectionList.ofFacing(dir), color)) {
            return false;
         } else {
            if (this.isSimulating()) {
               if (this.foamed == 0) {
                  this.cableColor = color == null ? 0 : color.getId() + 1;
                  EnergyNet.INSTANCE.updateTile(this);
                  this.updateTileField("cableColor");
                  this.onStateChanged();
               } else if (this.foamed == 2) {
                  this.storage.setColor(dir == null ? DirectionList.ALL : DirectionList.ofFacings(dir), color);
                  this.updateTileField("storage");
               }
            }

            return true;
         }
      } else {
         return false;
      }
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

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.getBlockState().hasProperty(CableBlock.WATER) && (Boolean)this.getBlockState().getValue(CableBlock.WATER)) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
      } else if (this.getBlockState().hasProperty(IC2Properties.LAVA_LOGGED) && (Boolean)this.getBlockState().getValue(IC2Properties.LAVA_LOGGED)) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.LAVA, Fluids.LAVA.getTickDelay(this.getLevel()));
      }

      if (!this.isRendering()) {
         DirectionList newConnection = DirectionList.EMPTY;

         for (Direction dir : DirectionList.ALL) {
            IEnergyTile tile = EnergyNet.INSTANCE.getSubTile(this.getLevel(), this.getBlockPos().relative(dir));
            if (!(tile instanceof IEnergyAcceptor) && !(tile instanceof IEnergyEmitter)) {
               tile = EnergyNet.INSTANCE.getTile(this.getLevel(), this.getBlockPos().relative(dir));
            }

            if (tile != null && this.canInteractWith(tile, dir)) {
               newConnection = newConnection.add(dir);
            }
         }

         if (newConnection.getCode() != this.connectivity) {
            this.connectivity = newConnection.getCode();
            this.updateTileField("connectivity");
         }
      }
   }

   public void setInsulation(int insulation) {
      insulation = Mth.clamp(insulation, 0, this.maxInsulation);
      if (this.insulation != insulation) {
         this.insulation = (byte)insulation;
         if (insulation == 0 && !this.hasUninsulatedColor()) {
            this.cableColor = 0;
         }

         EnergyNet.INSTANCE.updateTile(this);
         this.updateTileField("cableColor");
         this.updateTileField("insulation");
         this.onStateChanged();
      }
   }

   public boolean canInteractWith(IEnergyTile tile, Direction side) {
      if (this.hasAnchor(side)) {
         return false;
      } else if (tile instanceof IEnergyConductorAnchored && ((IEnergyConductorAnchored)tile).hasAnchor(side.getOpposite())) {
         return false;
      } else if (tile instanceof IEnergyConductorColored) {
         return this.canInteractWithAPICable((IEnergyConductorColored)tile);
      } else if (tile instanceof IEnergyConductor) {
         return true;
      } else if (tile instanceof IEnergyAcceptor && !(tile instanceof IEnergyEmitter)) {
         return ((IEnergyAcceptor)tile).canAcceptEnergy(this, side.getOpposite());
      } else if (tile instanceof IEnergyEmitter && !(tile instanceof IEnergyAcceptor)) {
         return ((IEnergyEmitter)tile).canEmitEnergy(this, side.getOpposite());
      } else {
         return !(tile instanceof IEnergyAcceptor)
            ? this.canSpecialConnect(tile, side)
            : ((IEnergyEmitter)tile).canEmitEnergy(this, side.getOpposite()) || ((IEnergyAcceptor)tile).canAcceptEnergy(this, side.getOpposite());
      }
   }

   public boolean canInteractWithAPICable(IEnergyConductorColored cable) {
      return this.cableColor == 0 || cable.getColor() == null || DyeColor.byId(this.cableColor - 1) == cable.getColor();
   }

   public boolean canSpecialConnect(IEnergyTile other, Direction side) {
      return false;
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      int count = DirectionList.ofNumber(this.anchors).size();
      if (count > 0) {
         drops.add(new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, count));
      }
   }

   @Override
   public boolean isLavaLogged() {
      BlockState state = this.getBlockState();
      return state.hasProperty(IC2Properties.LAVA_LOGGED) && (Boolean)state.getValue(IC2Properties.LAVA_LOGGED);
   }
}
