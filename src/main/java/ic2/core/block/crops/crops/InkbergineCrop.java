package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InkbergineCrop extends BaseCrop {
   public InkbergineCrop() {
      super("inkbergine", new CropProperties(6, 5, 0, 3, 1, 0), "Green", "Ink", "Leaves", "Black");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/inkbergine").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NANO_HEART;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.INK_SAC);
   }

   @Override
   public int getGrowthSteps() {
      return 7;
   }

   @Override
   public void onEntityCollision(ICropTile cropTile, Entity entity) {
      if (cropTile.getGrowthStage() == this.getGrowthSteps()) {
         int cooldown = cropTile.getCustomData().getInt("cooldown");
         Level world = cropTile.getWorldObj();
         if (cooldown == 0) {
            IC2.AUDIO.playSound(entity, SoundEvents.SQUID_SQUIRT.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);

            for (int i = 0; i < 10; i++) {
               ((ServerLevel)world)
                  .sendParticles(
                     ParticleTypes.SQUID_INK,
                     (double)cropTile.getPosition().getX() + 0.5,
                     (double)cropTile.getPosition().getY() + 0.5,
                     (double)cropTile.getPosition().getZ() + 0.5,
                     0,
                     (double)world.random.nextInt(20) / 10.0,
                     (double)world.random.nextInt(20) / 10.0,
                     (double)world.random.nextInt(20) / 10.0,
                     0.10000000149011612
                  );
            }

            cropTile.getCustomData().putInt("cooldown", world.random.nextInt(100));
         } else {
            cropTile.getCustomData().putInt("cooldown", Math.max(cooldown - 1, 0));
         }
      }
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.INK_SAC)};
   }

   @Override
   public ICrop.CropType getCropType() {
      return ICrop.CropType.WATER;
   }
}
