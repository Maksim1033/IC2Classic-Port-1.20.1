package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ColorTubeTileEntity extends TubeTileEntity implements ISpecialWrenchable, ICopyableSettings {
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int color;
   @NetworkInfo
   public boolean requests = true;

   public ColorTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"color", "requests"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.DYEING_TUBE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "color", this.color, 0);
      NBTUtils.putBoolean(compound, "state", this.requests, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.color = compound.getInt("color");
      this.requests = compound.getBoolean("state");
   }

   @Override
   public void saveSettings(CompoundTag tag) {
      NBTUtils.putByte(tag, "color", this.color, 0);
      NBTUtils.putBoolean(tag, "state", this.requests, false);
      this.updateTileFields(new String[]{"color", "requests"});
   }

   @Override
   public void loadSettings(CompoundTag tag) {
      this.color = tag.getInt("color");
      this.requests = tag.getBoolean("state");
   }

   @Override
   public boolean onCenterReached(TransportedItem item) {
      if (!this.requests && item.getRequestId() != null) {
         return false;
      } else {
         item.setColor(this.color == 0 ? null : DyeColor.byId(this.color - 1));
         return false;
      }
   }

   @Override
   public boolean setColor(Direction dir, DyeColor color) {
      if (this.foamed < 2) {
         int newValue = color == null ? 0 : color.getId() + 1;
         if (this.color == newValue) {
            return false;
         } else {
            this.color = newValue;
            this.updateTileField("color");
            return true;
         }
      } else {
         return super.setColor(dir, color);
      }
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      if (super.doSpecialAction(side, hit, player)) {
         return true;
      } else {
         if (player.isShiftKeyDown()) {
            this.requests = !this.requests;
            this.updateTileField("requests");
            if (this.isSimulating()) {
               player.displayClientMessage(this.translate(this.requests ? "gui.ic2.color_tube.request.handle" : "gui.ic2.color_tube.request.not_handle"), false);
            }
         }

         return false;
      }
   }
}
