package ic2.core.block.transport.item.tubes;

import ic2.api.network.tile.PacketRange;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DroppingTubeTileEntity extends TubeTileEntity {
   public static final double SPEED = 6.0;
   public static final double ACTUAL_SPEED = 0.44999999999999996;

   public DroppingTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"facing"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.DROPPING_TUBE;
   }

   @Override
   protected boolean isRotatable() {
      return true;
   }

   @Override
   public boolean canSetFacingInternal(Direction dir) {
      return dir != this.getFacing() && (this.connectivity & 1 << dir.get3DDataValue()) == 0;
   }

   @Override
   public boolean onCenterReached(TransportedItem item) {
      if (this.isRendering()) {
         return true;
      } else {
         this.onItemLost(item, true);
         this.sendToClient(1, item.getId(), PacketRange.CHUNK_TRACKED);
         Direction dir = this.getFacing();
         Vec3 pos = Vec3.atCenterOf(this.getBlockPos()).add(Vec3.atLowerCornerOf(dir.getNormal()).multiply(0.75, 0.75, 0.75));
         List<ItemStack> result = new ObjectArrayList();
         StackUtil.expand(item.getStack(), item.getStack().getCount(), result);

         for (ItemStack stack : result) {
            ItemEntity entity = new ItemEntity(this.getLevel(), pos.x(), pos.y(), pos.z(), stack);
            entity.setDeltaMovement(Vec3.atLowerCornerOf(dir.getNormal()).multiply(0.44999999999999996, 0.44999999999999996, 0.44999999999999996));
            this.getLevel().addFreshEntity(entity);
         }

         this.itemsToDelete.add(item);
         return true;
      }
   }
}
