package ic2.core.block.misc.explosive;

import ic2.core.IC2;
import ic2.core.entity.explosion.DynamiteEntity;
import ic2.core.item.base.IC2Item;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StickyDynamite extends IC2Item implements ISimpleItemModel {
   public StickyDynamite() {
      super("sticky_dynamite");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("explosive").get("sticky_dynamite");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating()) {
         stack.shrink(1);
         DynamiteEntity dynamite = new DynamiteEntity(IC2Entities.DYNAMITE, worldIn, playerIn);
         dynamite.setItem(new ItemStack(this));
         dynamite.setSticky(true);
         worldIn.addFreshEntity(dynamite);
      }

      return InteractionResultHolder.success(stack);
   }
}
