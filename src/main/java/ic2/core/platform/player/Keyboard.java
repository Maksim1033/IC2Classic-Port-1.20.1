package ic2.core.platform.player;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class Keyboard {
   public boolean isAltKeyDown(Player player) {
      return this.getHandler(player).altKeyDown;
   }

   public boolean isBoostKeyDown(Player player) {
      return this.getHandler(player).boostKeyDown;
   }

   public boolean isModeSwitchKeyDown(Player player) {
      return this.getHandler(player).modeSwitchKeyDown;
   }

   public boolean isSideInventoryKeyDown(Player player) {
      return this.getHandler(player).sideInventoryKeyDown;
   }

   public boolean isHudModeKeyDown(Player player) {
      return this.getHandler(player).hudModeKeyDown;
   }

   public boolean isSwitchKeyDown(Player player) {
      return this.getHandler(player).toggleKeyDown;
   }

   public boolean isSneakKeyDown(Player player) {
      return player.isShiftKeyDown();
   }

   public boolean isForwardKeyDown(Player player) {
      return this.getHandler(player).forwardKeyDown;
   }

   public boolean isJumpKeyDown(Player player) {
      return this.getHandler(player).jumpKeyDown;
   }

   public PlayerHandler getHandler(Player player) {
      return PlayerHandler.getHandler(player);
   }

   public MutableComponent getKeyName(KeyHelper helper) {
      switch (helper) {
         case BLOCK_CLICK:
            return Component.translatable("tooltip.ic2.block_click");
         case BLOCK_LEFT_CLICK:
            return Component.translatable("tooltip.ic2.block_left_click");
         default:
            return Component.literal("I AM ERROR");
      }
   }

   public int getKey(KeyHelper helper) {
      return -1;
   }

   public void sendKeyUpdate() {
   }

   public void processKeyUpdate(Player player, int keyState) {
      this.getHandler(player).onKeyChanged(keyState);
   }

   public void init() {
   }
}
