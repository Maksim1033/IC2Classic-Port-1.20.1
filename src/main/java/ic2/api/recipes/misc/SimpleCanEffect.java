package ic2.api.recipes.misc;

import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SimpleCanEffect implements ICanEffect {
   final ResourceLocation id;
   final MobEffectInstance[] effects;
   Component component = null;
   ResourceLocation texture;
   boolean isRandom = false;
   float chance;

   public SimpleCanEffect(ResourceLocation id, Collection<MobEffectInstance> effects) {
      this(id, effects.toArray(MobEffectInstance[]::new));
   }

   public SimpleCanEffect(ResourceLocation id, MobEffectInstance... effects) {
      this.id = id;
      this.effects = effects;
   }

   public SimpleCanEffect setRandom(float chance) {
      this.chance = chance;
      this.isRandom = true;
      return this;
   }

   public SimpleCanEffect setTexture(ResourceLocation texture) {
      this.texture = texture;
      return this;
   }

   public SimpleCanEffect setTooltip(Component component) {
      this.component = component;
      return this;
   }

   @Override
   public void onFoodEaten(ItemStack stack, Player player) {
      if (!player.level().isClientSide) {
         if ((!this.isRandom || !(player.level().random.nextFloat() >= this.chance)) && this.effects != null) {
            for (MobEffectInstance effect : this.effects) {
               if (effect != null) {
                  player.addEffect(new MobEffectInstance(effect));
               }
            }
         }
      }
   }

   @Override
   public Component getTooltip() {
      return this.component;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getOverrideIcon() {
      return this.texture == null ? null : (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.texture);
   }

   @Override
   public ResourceLocation getID() {
      return this.id;
   }
}
