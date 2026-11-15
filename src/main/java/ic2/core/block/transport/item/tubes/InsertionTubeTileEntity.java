package ic2.core.block.transport.item.tubes;

import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.inventory.transporter.transporters.special.OnlyExistingTransporter;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

public class InsertionTubeTileEntity extends TubeTileEntity {
   public boolean onlyExistingOne = false;

   public InsertionTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.INSERTION_TUBE;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.onlyExistingOne = compound.getBoolean("existing");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "existing", this.onlyExistingOne, false);
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.ONLY_EXISTING) {
         this.onlyExistingOne = !this.onlyExistingOne;
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.sensitive ? "gui.ic2.tube.insertion.only_one.enable" : "gui.ic2.tube.insertion.only_one.disable"), false
            );
         }

         return true;
      } else {
         return super.doTubeAction(side, hit, player, action);
      }
   }

   @Override
   public boolean insertIntoInventory(IItemHandler handler, Direction flowDirection, TransportedItem item) {
      if (this.onlyExistingOne) {
         ItemStack stack = item.getStack();
         stack.shrink(new OnlyExistingTransporter(handler).addItem(stack, flowDirection.getOpposite(), false));
         return item.isInvalid();
      } else {
         return super.insertIntoInventory(handler, flowDirection, item);
      }
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      if (item.getRequestId() != null) {
         return this.tubes.getPresentSides().remove(item.getAttemptedDirections()).remove(DirectionList.ofNumber(this.anchors));
      } else {
         DirectionList existingSides = this.inventories.getPresentSides().remove(item.getAttemptedDirections()).remove(DirectionList.ofNumber(this.anchors));
         return existingSides.isEmpty()
            ? this.tubes.getPresentSides().remove(item.getAttemptedDirections()).remove(DirectionList.ofNumber(this.anchors))
            : existingSides;
      }
   }
}
