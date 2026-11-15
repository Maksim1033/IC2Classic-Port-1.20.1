package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.crops.ICrop;
import ic2.core.block.crops.CropRegistry;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.item.misc.CropSeedItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropComponent extends BaseWikiComponent {
   ICrop crop;
   ItemStack baseSeed;
   ItemStack cropItem;
   ItemStack display;

   public CropComponent(ICrop crop) {
      super(36);
      this.crop = crop;
      this.baseSeed = CropRegistry.INSTANCE.getSeedForCrop(crop);
      this.cropItem = CropSeedItem.createStack(crop, 1, 1, 1, 4);
      this.display = CropRegistry.INSTANCE.getDisplayItem(crop.id());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addSearchString(Consumer<Component> listener) {
      listener.accept(this.crop.getName());
      listener.accept(this.crop.discoveredBy());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      screen.bindDefaultTexture();
      screen.drawTextureRegion(stack, (float)x, (float)y, 0.0F, 248.0F, 116.0F, 4.0F);
      screen.drawTextureRegion(stack, (float)x, (float)(y + 30), 0.0F, 252.0F, 116.0F, 4.0F);
      screen.bindTexture(IWikiComponent.TEXTURE);
      screen.drawTextureRegion(stack, (float)(x + 10), (float)(y + 7), 158.0F, 0.0F, 20.0F, 20.0F);
      screen.drawTextureRegion(stack, (float)(x + 43), (float)(y + 8), 124.0F, 0.0F, 18.0F, 18.0F, 18.0F, 18.0F);
      screen.drawTextureRegion(stack, (float)(x + 67), (float)(y + 11), 124.0F, 18.0F, 16.0F, 12.0F, 16.0F, 12.0F);
      screen.drawTextureRegion(stack, (float)(x + 88), (float)(y + 5), 124.0F, 0.0F, 24.0F, 24.0F, 18.0F, 18.0F);
      screen.drawItemStack(stack, (float)(x + 11), (float)(y + 8), this.baseSeed, 18.0F, 18.0F, true, null);
      screen.drawItemStack(stack, (float)(x + 43), (float)(y + 8), this.cropItem, 18.0F, 18.0F);
      screen.drawItemStack(stack, (float)(x + 91), (float)(y + 8), this.display, 18.0F, 18.0F);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      Box2i box = new Box2i(11, 7, 18, 18);
      if (box.isInBox(mouseX - x, mouseY - y)) {
         this.baseSeed.getTooltipLines(screen.getPlayer(), Default.NORMAL).forEach(tooltips);
         tooltips.accept(
            this.translate(this.baseSeed.getItem() == IC2Items.CROP_STICKS ? "tooltip.ic2.crops.breed_only.name" : "tooltip.ic2.crops.plant_only.name")
         );
      }

      box = new Box2i(43, 8, 18, 18);
      if (box.isInBox(mouseX - x, mouseY - y)) {
         this.cropItem.getTooltipLines(screen.getPlayer(), Default.NORMAL).forEach(tooltips);
      }

      box = new Box2i(91, 8, 18, 18);
      if (box.isInBox(mouseX - x, mouseY - y)) {
         this.display.getTooltipLines(screen.getPlayer(), Default.NORMAL).forEach(tooltips);
      }
   }
}
