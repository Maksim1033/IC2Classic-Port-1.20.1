package ic2.core.item.misc;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
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

public class SUBattery extends IC2Item implements ISimpleItemModel {
   public SUBattery() {
      super("su_battery");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("battery").get("single_use_battery");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating()) {
         int left = 1000;

         for (int i = 0; i < 9 && left > 0; i++) {
            ItemStack otherItem = playerIn.getInventory().getItem(i);
            if (otherItem.getItem() instanceof IElectricItem) {
               left -= ElectricItem.MANAGER.charge(otherItem, left, 1, true, false);
            }
         }

         if (left != 1000) {
            stack.shrink(1);
         }
      }

      return InteractionResultHolder.success(stack);
   }
}
