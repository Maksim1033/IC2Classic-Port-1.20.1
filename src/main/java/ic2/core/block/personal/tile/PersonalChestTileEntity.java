package ic2.core.block.personal.tile;

import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.BasePersonalTileEntity;
import ic2.core.block.personal.container.PersonalChestContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.IHasItemHandler;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class PersonalChestTileEntity extends BasePersonalTileEntity {
   public PersonalChestTileEntity.AnimationInfo animation = new PersonalChestTileEntity.AnimationInfo();
   public SimpleInventory inventory = new SimpleInventory(54);

   public PersonalChestTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addPersonalCapability(ForgeCapabilities.ITEM_HANDLER, new IHasItemHandler(this.inventory));
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      this.animation.setOpen(true);
      return new PersonalChestContainer(this, player, windowID);
   }

   @Override
   public void onGuiClosed(Player player) {
      this.animation.setOpen(false);
      super.onGuiClosed(player);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PERSONAL_CHEST;
   }

   @Override
   public void readItemData(CompoundTag nbt, boolean place) {
      super.readItemData(nbt, place);
      this.inventory.load(nbt);
   }

   @Override
   public CompoundTag writeItemData(CompoundTag nbt) {
      super.writeItemData(nbt);
      this.inventory.save(nbt);
      return nbt;
   }

   @Override
   public void voidStorage() {
      this.inventory.clear();
   }

   public class AnimationInfo {
      public int numUsingPlayers;
      public float lidAngle;
      public float prevLidAngle;
      private long lastTime = 0L;

      public void tick() {
         if (PersonalChestTileEntity.this.getLevel() != null && PersonalChestTileEntity.this.getLevel().getGameTime() >= this.lastTime) {
            this.lastTime = PersonalChestTileEntity.this.getLevel().getGameTime() + 1L;
            this.prevLidAngle = this.lidAngle;
            if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F) {
               IC2.AUDIO.playSound(PersonalChestTileEntity.this, SoundEvents.CHEST_OPEN.getLocation());
            }

            float expected = (float)Math.min(1, this.numUsingPlayers) * 1.5F;
            if (expected != this.lidAngle) {
               float last = this.lidAngle;
               this.lidAngle = Mth.clamp(this.lidAngle + (this.numUsingPlayers > 0 ? 0.05F : -0.05F), 0.0F, 1.0F);
               if (this.lidAngle < 0.5F && last >= 0.5F) {
                  IC2.AUDIO.playSound(PersonalChestTileEntity.this, SoundEvents.CHEST_CLOSE.getLocation());
               }
            }
         }
      }

      public void setOpen(boolean value) {
         if (value) {
            this.numUsingPlayers++;
         } else {
            this.numUsingPlayers = Math.max(0, this.numUsingPlayers - 1);
         }
      }
   }
}
