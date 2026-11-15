package ic2.core.item.food_and_drink;

import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TerraWartItem extends IC2Item implements ISimpleItemModel {
   public static final FoodProperties TERRAWART_FOOD = new Builder().fast().saturationMod(1.0F).alwaysEat().build();

   public TerraWartItem() {
      super("terrawart", new PropertiesBuilder().group(IC2.FOOD_AND_DRINK_GROUP).food(TERRAWART_FOOD).rarity(Rarity.RARE));
   }

   public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
      ObjectListIterator var4 = new ObjectArrayList(entityLiving.getActiveEffects()).iterator();

      while (var4.hasNext()) {
         MobEffectInstance instance = (MobEffectInstance)var4.next();
         if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
            entityLiving.removeEffect(instance.getEffect());
         }
      }

      return super.finishUsingItem(stack, worldIn, entityLiving);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("crops").get("terrawart");
   }
}
