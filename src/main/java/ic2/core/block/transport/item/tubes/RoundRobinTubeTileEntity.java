package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.RoundRobinTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RoundRobinTubeTileEntity extends TubeTileEntity implements ICamouflageGui, ICopyableSettings {
   @NetworkInfo
   public int[] cap = new int[]{1, 1, 1, 1, 1, 1};
   public int currentItem = -1;
   public int currentIndex = 0;

   public RoundRobinTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"cap"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ROUND_ROBIN_TUBE;
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RoundRobinTubeContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putIntArray(compound, "caps", this.cap, new int[]{1, 1, 1, 1, 1, 1});
      NBTUtils.putInt(compound, "currentItem", this.currentItem, -1);
      NBTUtils.putInt(compound, "currentIndex", this.currentIndex, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.cap = NBTUtils.getIntArray(compound, "caps", new int[]{1, 1, 1, 1, 1, 1});
      this.currentItem = NBTUtils.getInt(compound, "currentItem", -1);
      this.currentIndex = NBTUtils.getInt(compound, "currentIndex", 0);
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      NBTUtils.putIntArray(compound, "caps", this.cap, new int[]{1, 1, 1, 1, 1, 1});
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.cap = NBTUtils.getIntArray(compound, "caps", new int[]{1, 1, 1, 1, 1, 1});
      this.currentIndex = 0;
      this.currentItem = -1;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if ((key == 0 || key == 2) && value >= 0 && value < 6) {
         this.cap[value] = Mth.clamp(this.cap[value] + (key == 2 ? 10 : 1), 0, 1000);
         this.updateGuiField("cap");
      } else if ((key == 1 || key == 3) && value >= 0 && value < 6) {
         this.cap[value] = Mth.clamp(this.cap[value] - (key == 3 ? 10 : 1), 0, 1000);
         this.updateGuiField("cap");
      }
   }

   @Override
   public boolean onCenterReached(TransportedItem item) {
      if (this.currentItem < 0 || this.currentItem >= this.cap[this.currentIndex]) {
         this.findNextSide(item);
      }

      this.currentItem++;
      item.setExportDirection(Direction.from3DDataValue(this.currentIndex));
      this.sync.markDirty(item);
      return true;
   }

   public void findNextSide(TransportedItem item) {
      for (int i = 0; i < 6; i++) {
         this.currentIndex = ++this.currentIndex % 6;
         if (this.cap[this.currentIndex] > 0) {
            Direction dir = Direction.from3DDataValue(this.currentIndex);
            ITube tube = this.tubes.getHandler(dir);
            if (tube != null && tube.canAddItem(item, dir.getOpposite()) || this.inventories.getHandler(dir) != null) {
               break;
            }
         }
      }

      this.currentItem = 0;
   }
}
