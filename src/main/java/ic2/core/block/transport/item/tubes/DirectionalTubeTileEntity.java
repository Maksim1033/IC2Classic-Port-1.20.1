package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DirectionalTubeTileEntity extends TubeTileEntity implements IRedstoneListener {
   boolean lastRedstone = false;
   int invalids = 0;
   @NetworkInfo
   boolean blockFully;

   public DirectionalTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"facing", "blockFully"});
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "last", this.lastRedstone, false);
      NBTUtils.putBoolean(compound, "full", this.blockFully, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.lastRedstone = compound.getBoolean("last");
      this.blockFully = compound.getBoolean("full");
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      if (!this.blockFully) {
         ItemStack stack = player.getItemInHand(hand);
         if (stack.getItem() == Items.GRAVEL) {
            this.blockFully = true;
            stack.shrink(1);
            this.updateTileField("blockFully");
            if (this.isSimulating()) {
               player.displayClientMessage(this.translate("gui.ic2.tube.directional_tube.enable"), false);
            }

            return true;
         }
      }

      return super.onRightClick(player, hand, side, hit);
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.BLOCK_OUTPUT) {
         this.blockFully = !this.blockFully;
         this.updateTileField("blockFully");
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.blockFully ? "gui.ic2.tube.directional_tube.enable" : "gui.ic2.tube.directional_tube.disable"), false
            );
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.DIRECTIONAL_TUBE;
   }

   @Override
   protected void onCachesUpdated() {
      super.onCachesUpdated();
      this.invalids = 0;

      for (Direction dir : this.tubes) {
         if (this.tubes.getHandler(dir).getTubeType() == ITube.TubeType.EXTRACTION) {
            this.invalids = this.invalids | 1 << dir.get3DDataValue();
         }
      }
   }

   @Override
   public boolean canAddItem(TransportedItem item, Direction side) {
      return !this.blockFully || side != this.getFacing();
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      return DirectionList.ofFacing(this.getFacing()).remove(DirectionList.ofNumber(this.connectivity).invert());
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering()) {
         this.handleRedstone();
         if (this.lastRedstone != this.isRedstonePowered()) {
            this.lastRedstone = this.isRedstonePowered();
            this.setFacing(DirectionList.ofNumber(this.connectivity).remove(DirectionList.ofNumber(this.invalids)).getNextFacing(this.getFacing()));
         }
      }
   }

   public boolean isBlocked() {
      return this.blockFully;
   }

   @Override
   public boolean isRotatable() {
      return true;
   }

   @Override
   public boolean canSetFacingInternal(Direction dir) {
      return dir != this.getFacing() && (this.connectivity & 1 << dir.get3DDataValue()) != 0;
   }

   @Override
   public boolean allowWeakSignal(Direction dir) {
      return true;
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   protected boolean needsInitialRedstoneCheck() {
      return true;
   }
}
