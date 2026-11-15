package ic2.core.block.transport.item.tubes;

import ic2.api.tiles.tubes.ITube;
import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.misc.TreeTapAndBucketBlock;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StickyTubeTileEntity extends TubeTileEntity {
   public int priorityDir = 0;
   public boolean specialConnect = false;
   boolean changed = false;
   int nextTime = RandomSource.create().nextInt(1024);

   public StickyTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.STICKY_TUBE;
   }

   @Override
   public ITube.TubeType getTubeType() {
      return this.specialConnect ? ITube.TubeType.SIMPLE : ITube.TubeType.EXTRACTION;
   }

   @Override
   public int getPrioritySide() {
      return this.priorityDir;
   }

   @Override
   public boolean canConnect(ITube other, Direction dir) {
      return super.canConnect(other, dir) && other.getTubeType() == ITube.TubeType.SIMPLE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "prio", this.priorityDir, 0);
      NBTUtils.putBoolean(compound, "special_connected", this.specialConnect, false);
   }

   @Override
   public void load(CompoundTag compound) {
      this.priorityDir = NBTUtils.getInt(compound, "prio", 0);
      this.specialConnect = compound.getBoolean("special_connected");
      super.load(compound);
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.OUTPUT_PRIORITY) {
         Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
         if (clicked != null && clicked != this.getFacing()) {
            if (clicked.ordinal() != this.priorityDir - 1) {
               this.priorityDir = clicked.ordinal() + 1;
            } else {
               this.priorityDir = 0;
            }

            this.onStateChanged();
            return true;
         }
      } else if (action == TubeAction.SPECIAL_EXTRACTION_CONNECTIVITY) {
         this.specialConnect = !this.specialConnect;
         this.tubes.markDirty();
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.specialConnect ? "gui.ic2.tube.extraction.special.enable" : "gui.ic2.tube.extraction.special.disable"), false
            );
         }

         this.self.setActiveDirections(DirectionList.EMPTY);
         this.self.setActiveDirections(DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
         this.tubes.invalidateCache();
         this.onStateChanged();
         this.notifyChanges(true, DirectionList.ALL);
         return true;
      }

      return false;
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      this.changed = true;
   }

   @Override
   protected void onCachesUpdated() {
      DirectionList list = DirectionList.EMPTY.add(this.inventories.getPresentSides()).add(this.tubes.getPresentSides());
      if (list.notContains(Direction.UP) && this.getLevel().getBlockState(this.getBlockPos().above()).is(IC2Blocks.TREETAP_AND_BUCKET)) {
         list = list.add(Direction.UP);
      }

      this.connectivity = list.remove(DirectionList.ofNumber(this.anchors)).getCode();
      this.updateTileField("connectivity");
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering()) {
         this.tickLog();
         this.onExtraction();
      }
   }

   protected void tickLog() {
      if (this.nextTime > 0) {
         this.nextTime--;
      } else {
         this.nextTime = this.getLevel().random.nextInt(1024);
         if (this.getLevel() instanceof ServerLevel server) {
            BlockPos var4 = this.getBlockPos().relative(Direction.UP);
            BlockState state = this.getLevel().getBlockState(var4);
            if (state.is(IC2Blocks.TREETAP_AND_BUCKET)) {
               var4 = var4.relative((Direction)state.getValue(TreeTapAndBucketBlock.FACING));
               state = this.getLevel().getBlockState(var4);
               if (state.is(IC2Blocks.RUBBERWOOD_LOG)) {
                  state.randomTick(server, var4, server.random);
               }
            }
         }
      }
   }

   protected void onExtraction() {
      if (!this.invClock(256) || this.changed) {
         this.changed = false;
         BlockState state = this.getLevel().getBlockState(this.getBlockPos().relative(Direction.UP));
         if (state.is(IC2Blocks.TREETAP_AND_BUCKET)) {
            int resin = (Integer)state.getValue(TreeTapAndBucketBlock.FILL_STAGE);
            if (resin > 0) {
               this.addItem(new ItemStack(IC2Items.STICKY_RESIN, resin), Direction.UP);
               this.getLevel().setBlockAndUpdate(this.getBlockPos().relative(Direction.UP), (BlockState)state.setValue(TreeTapAndBucketBlock.FILL_STAGE, 0));
               this.changed = false;
            }
         }
      }
   }
}
