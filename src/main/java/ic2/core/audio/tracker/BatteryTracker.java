package ic2.core.audio.tracker;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioSource;
import ic2.core.item.base.features.ISoundBattery;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BatteryTracker implements IAudioTracker {
   Player player;
   InteractionHand activeHand;
   IAudioSource source;

   public BatteryTracker(Player player) {
      this.player = player;
   }

   @Override
   public void onTick() {
      boolean usesItem = this.player.isUsingItem();
      if (usesItem == (this.activeHand == null)) {
         if (!usesItem) {
            this.activeHand = null;
            if (this.source != null) {
               this.source.remove();
               this.source = null;
            }
         } else {
            this.player.getUsedItemHand();
            this.activeHand = this.player.getUsedItemHand();
            if (this.source != null) {
               this.source.remove();
               this.source = null;
            }

            ItemStack stack = this.player.getItemInHand(this.activeHand);
            if (stack.getItem() instanceof ISoundBattery battery) {
               if (!battery.wantsToPlay(stack)) {
                  return;
               }

               this.source = IC2.AUDIO.createSource(this.player, battery.getSound(stack), AudioManager.SoundType.ITEM, 4.0F, true, true);
               if (this.source != null) {
                  this.source.play();
               }
            }
         }
      }
   }

   @Override
   public boolean isValid(Level world) {
      return this.player.isAlive() && this.player.getCommandSenderWorld() == world;
   }
}
