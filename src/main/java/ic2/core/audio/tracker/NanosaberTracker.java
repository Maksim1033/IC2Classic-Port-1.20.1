package ic2.core.audio.tracker;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioSource;
import ic2.core.item.tool.electric.NanoSaber;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NanosaberTracker implements IAudioTracker {
   Player player;
   boolean hasActiveNanosaber = false;
   IAudioSource source;

   public NanosaberTracker(Player player) {
      this.player = player;
   }

   @Override
   public void onTick() {
      ItemStack mainHand = this.player.getMainHandItem();
      ItemStack offhand = this.player.getOffhandItem();
      if (!this.hasActiveNanosaber) {
         if (this.isActiveNanoSaber(mainHand) || this.isActiveNanoSaber(offhand)) {
            this.hasActiveNanosaber = true;
            this.source = IC2.AUDIO
               .createSource(
                  this.player, NanoSaber.saberIdle, AudioManager.SoundType.ITEM, IC2.AUDIO.getVolumeForType(AudioManager.SoundType.ITEM), true, false
               );
            if (this.source != null) {
               this.source.play();
            }
         }
      } else if (!this.isActiveNanoSaber(mainHand) && !this.isActiveNanoSaber(offhand)) {
         this.hasActiveNanosaber = false;
         if (this.source != null) {
            this.source.remove();
            this.source = null;
         }
      }
   }

   @Override
   public boolean isValid(Level world) {
      return this.player.getCommandSenderWorld() == world && this.player.isAlive();
   }

   private boolean isActiveNanoSaber(ItemStack stack) {
      return stack.getItem() instanceof NanoSaber && StackUtil.getNbtData(stack).getBoolean("active");
   }
}
