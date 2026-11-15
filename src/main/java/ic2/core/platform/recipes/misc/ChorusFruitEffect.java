package ic2.core.platform.recipes.misc;

import ic2.api.recipes.misc.ICanEffect;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChorusFruitEffect implements ICanEffect {
   String key;
   String value;
   final ResourceLocation id;
   Component component = null;

   public ChorusFruitEffect(ResourceLocation id) {
      this.id = id;
   }

   @Override
   public void onFoodEaten(ItemStack stack, Player player) {
      Level world = player.getCommandSenderWorld();
      if (!world.isClientSide) {
         double currentPosX = player.getX();
         double currentPosY = player.getY();
         double currentPosZ = player.getZ();

         for (int i = 0; i < 16; i++) {
            double newPosX = currentPosX + (player.getRandom().nextDouble() - 0.5) * 16.0;
            double newPosY = Mth.clamp(currentPosY + (double)(player.getRandom().nextInt(16) - 8), 0.0, (double)(world.getMaxBuildHeight() - 1));
            double newPosZ = currentPosZ + (player.getRandom().nextDouble() - 0.5) * 16.0;
            if (player.isVehicle()) {
               player.ejectPassengers();
            }

            if (player.randomTeleport(newPosX, newPosY, newPosZ, true)) {
               world.playSound(null, currentPosX, currentPosY, currentPosZ, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
               player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
               break;
            }
         }
      }
   }

   public ICanEffect setTexture(String key, String value) {
      this.key = key;
      this.value = value;
      return this;
   }

   public ICanEffect setTooltip(Component component) {
      this.component = component;
      return this;
   }

   @Override
   public Component getTooltip() {
      return this.component;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getOverrideIcon() {
      return IC2Textures.getMappedEntriesItemIC2(this.key).get(this.value);
   }

   @Override
   public ResourceLocation getID() {
      return this.id;
   }
}
