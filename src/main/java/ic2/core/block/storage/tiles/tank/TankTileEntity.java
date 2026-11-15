package ic2.core.block.storage.tiles.tank;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.blocks.ValveBlock;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.features.multiblock.IMultiBlockClickable;
import ic2.core.block.base.features.multiblock.IMultiBlockFluidExporter;
import ic2.core.block.base.misc.BucketFiller;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.block.storage.container.TankContainer;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.fluid.IC2Tank;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.StructureBuilder;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TankTileEntity extends BaseMultiBlockTileEntity implements ITileGui, IMultiBlockClickable, IMultiBlockFluidExporter, ICamouflagable {
   static final Vec2i MAX_BOUNDS = new Vec2i(50, 200);
   public static final Predicate<BlockState> VALID_BLOCKS = T -> T.is(IC2Tags.TANK_STRUCTURE)
         || T.getBlock() == IC2Blocks.TANK
         || T.getBlock() instanceof ValveBlock;
   public static final Predicate<BlockState> STRUCTURE_BLOCKS = T -> T.is(IC2Tags.TANK_STRUCTURE) && !T.is(Blocks.GLASS);
   @NetworkInfo
   public IC2Tank tank;
   @NetworkInfo
   public CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);
   @NetworkInfo
   public boolean textureCopied = false;
   protected BucketFiller filler;
   protected BucketFiller drainer;

   public TankTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 4);
      this.filler = new BucketFiller(this, this.tank, 0, 1);
      this.drainer = new BucketFiller(this, this.tank, 2, 3);
      this.addNetworkFields(new String[]{"tank", "children", "storage", "textureCopied"});
      this.tank.addListener(T -> {
         this.updateTileField("tank");
         this.updateGuiField("tank");
      });
      this.tank.addListener(T -> this.handleNonTickComparators());
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
      this.addComparator(new TankComparator("tank", ComparatorNames.TANK, this.tank));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      this.tank = new IC2Tank(0);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerInputFilter(new FluidFilter.FluidTankFillFilter(this.tank), 2);
      handler.registerInputFilter(new FluidFilter.FluidTankFilter(this.tank), 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 0, 2);
      handler.registerSlotAccess(AccessRule.EXPORT, 1, 3);
      handler.registerSlotsForSide(DirectionList.ALL, 0, 1, 2, 3);
      handler.registerNamedSlot(SlotType.INPUT, 0, 2);
      handler.registerNamedSlot(SlotType.OUTPUT, 1, 3);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TANK;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.put(compound, "tank", this.tank.writeToNBT(new CompoundTag()));
      NBTUtils.putBoolean(compound, "tex", this.textureCopied, false);
      if (this.textureCopied) {
         NBTUtils.put(compound, "storage", this.storage.save(new CompoundTag()));
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
      this.textureCopied = compound.getBoolean("tex");
      if (this.textureCopied) {
         this.storage.load(compound.getCompound("storage"));
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("storage")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public CamouflageStorage getStorage() {
      return this.storage;
   }

   @Override
   public boolean applyTexture(Direction dir, RetextureEvent.TextureContainer container) {
      if (this.storage.canApply(DirectionList.ALL, container)) {
         this.storage.setTexture(DirectionList.ALL, container);
         this.textureCopied = true;
         this.updateTileFields(new String[]{"storage", "textureCopied"});
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSideEnabled(Direction dir) {
      return this.textureCopied;
   }

   @Override
   public boolean removeCamouflage() {
      return false;
   }

   @Override
   public ItemLike getCamouflageDrop() {
      return Items.AIR;
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.textureCopied
         ? ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
            )
            .build()
         : ModelData.EMPTY;
   }

   public AABB getBox(float progress) {
      AABB box = this.boundingBox.toInclusiveAxis().deflate(1.0);
      return box.contract(0.0, Math.max(0.0010000000474974513, (box.maxY - box.minY) * (double)(1.0F - progress)), 0.0)
         .move((double)(-this.worldPosition.getX()), (double)(-this.worldPosition.getY()), (double)(-this.worldPosition.getZ()));
   }

   public BlockPos getCenter() {
      return new BlockPos(
         this.boundingBox.getMinX() + this.boundingBox.getWidth() / 2,
         this.boundingBox.getMinY() + 1,
         this.boundingBox.getMinZ() + this.boundingBox.getDepth() / 2
      );
   }

   @Override
   public boolean doesNeedTickAfterValidation() {
      return !((ItemStack)this.inventory.get(0)).isEmpty() || !((ItemStack)this.inventory.get(2)).isEmpty();
   }

   @Override
   public void onStructureValidated(boolean revalidation) {
      super.onStructureValidated(revalidation);
      this.tank.setCapacity(this.boundingBox.copy().expand(DirectionList.ALL, -1).size() * 16000);
      this.updateTileField("tank");
   }

   @Override
   protected void onInitialValidation() {
      this.updateTileField("children");
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new TankContainer(this, player, windowID);
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.isValid;
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && this.isValid;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && (slot == 0 || slot == 2) && !stack.isEmpty()) {
         this.addToTick();
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 0) {
         this.tank.drain(2147483647, FluidAction.EXECUTE);
      } else {
         super.onClientDataReceived(entity, key, value);
      }
   }

   @Override
   public void onStructureTick() {
      this.filler.fillTank();
      this.drainer.fillItem();
      if (!this.doesNeedTickAfterValidation()) {
         this.removeFromTick();
      }

      this.handleComparators();
   }

   @Override
   public void exportFluids(ICache<IFluidHandler> cache, IFluidPipe pump, Direction pumpSide) {
      if (!this.tank.isEmpty() && !cache.isEmpty()) {
         for (Direction dir : cache) {
            IFluidHandler handler = cache.getHandler(dir);
            if (handler != null
               && this.tank
                     .drain(
                        handler.fill(new FluidStack(this.tank.getFluid(), Math.min(2000, this.tank.getFluidAmount())), FluidAction.EXECUTE),
                        FluidAction.EXECUTE
                     )
                     .getAmount()
                  > 0
               && this.tank.isEmpty()) {
               return;
            }
         }
      }

      if (!this.tank.isEmpty() && pump != null) {
         this.tank
            .drain(
               FluidNet.INSTANCE.sendFluid(pump, pumpSide, new FluidStack(this.tank.getFluid(), Math.min(2000, this.tank.getFluidAmount())), 100),
               FluidAction.EXECUTE
            );
      }
   }

   @Override
   public boolean isStateStillValid(BlockPos actualPos, BlockPos structurePos, BlockState newState) {
      return this.boundingBox.isEdge(structurePos) ? STRUCTURE_BLOCKS.test(newState) : VALID_BLOCKS.test(newState);
   }

   @Override
   public boolean testStructure(StructureBuilder builder) {
      if (!builder.forward().isAir()) {
         return false;
      } else {
         Box box = new Box(0, 0, 0, 0, 0, 0).offset(this.getFacing().getOpposite());
         if (!this.rayTest(builder.getFacing(), builder.resetPosition(), box)
            && !this.rayTest(builder.getFacing().getClockWise(), builder.resetPosition(), box)
            && !this.rayTest(builder.getFacing().getCounterClockWise(), builder.resetPosition(), box)
            && !this.rayTest(Direction.DOWN, builder.resetPosition(), box)
            && !this.rayTest(Direction.UP, builder.resetPosition(), box)) {
            for (BlockPos pos : box) {
               if (!builder.setOffset(pos).isAir()) {
                  return false;
               }
            }

            box.expand(DirectionList.ALL, 1);

            for (BlockPos posx : box.getHollowIterator()) {
               BlockState state = builder.setOffset(posx).getState();
               if ((box.isEdge(posx) ? STRUCTURE_BLOCKS : VALID_BLOCKS).test(state)
                  && Block.isShapeFullBlock(state.getShape(this.getLevel(), posx.offset(this.getBlockPos())))) {
                  if (!state.is(this.getBlockState().getBlock()) || posx.getX() == 0 && posx.getY() == 0 && posx.getZ() == 0) {
                     if (state.getBlock() instanceof ValveBlock) {
                        this.children.add(builder.getTile(BaseLinkingTileEntity.class));
                     }
                     continue;
                  }

                  return false;
               }

               return false;
            }

            builder.resetPosition().removeMark();
            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean rayTest(Direction rotation, StructureBuilder builder, Box box) {
      builder.forward();
      int max = rotation.getAxis().isHorizontal() ? MAX_BOUNDS.getX() : MAX_BOUNDS.getY();
      int steps = 0;

      while (steps <= max && builder.offset(rotation).isAir()) {
         steps++;
      }

      if (steps < max && builder.isState(VALID_BLOCKS)) {
         box.expand(rotation, steps);
         return false;
      } else {
         return true;
      }
   }
}
