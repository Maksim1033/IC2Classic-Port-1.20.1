package ic2.core.block.cables.luminator;

import ic2.api.network.buffer.NetworkInfo;
import ic2.core.IC2;
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
import net.minecraft.world.phys.BlockHitResult;

public class AdjustableLuminatorTileEntity extends LuminatorTileEntity {
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   int lightLevel = 5;

   public AdjustableLuminatorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"lightLevel"});
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "light", this.lightLevel, 5);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.lightLevel = NBTUtils.getInt(compound, "light", 5);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      if (IC2.KEYBOARD.isModeSwitchKeyDown(player) && player.getItemInHand(hand).isEmpty()) {
         if (IC2.PLATFORM.isSimulating()) {
            this.lightLevel = Mth.clamp(this.lightLevel + (player.isShiftKeyDown() ? -1 : 1), 0, 15);
            this.onStateChanged();
            player.displayClientMessage(this.translate("info.block.ic2.light_level", new Object[]{this.lightLevel}), false);
         }

         return true;
      } else {
         return super.onRightClick(player, hand, side, hit);
      }
   }

   @Override
   public int getLightLevel() {
      return this.isActive() ? this.lightLevel : 0;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.LUMINATOR_ADJUSTABLE;
   }
}
