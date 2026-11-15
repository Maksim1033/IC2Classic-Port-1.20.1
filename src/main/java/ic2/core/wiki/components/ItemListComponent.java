package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.managers.IWikiProvider;
import ic2.core.wiki.recipes.providers.IItemReference;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemListComponent extends BaseWikiComponent {
   public static final Box2i UP_ARROW = new Box2i(20, 20, 8, 10);
   public static final Box2i DOWN_ARROW = new Box2i(88, 20, 8, 10);
   int rows;
   IItemReference items;
   int offset;

   public ItemListComponent(int rows, IItemReference items) {
      super(rows * 17 + 15);
      this.rows = rows;
      this.items = items;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      for (int column = 0; column < this.rows; column++) {
         for (int row = 0; row < 6; row++) {
            screen.drawTextureRegion(stack, (float)(x + 7 + row * 17), (float)(y + column * 17), 124.0F, 0.0F, 18.0F, 18.0F);
         }
      }

      screen.bindDefaultTexture();
      mouseX += screen.getGuiLeft();
      mouseY += screen.getGuiTop();
      List<ItemStack> stacks = this.items.getItemList();
      int limit = this.items.getItemList().size() / 6 - Math.max(1, this.rows - 1);
      screen.drawTextureRegion(
         stack,
         (float)(x + 20),
         (float)(y + 20 + (this.rows - 1) * 17),
         21.0F,
         208.0F,
         8.0F,
         10.0F,
         ColorUtils.darker(-1, this.offset > 0 && !UP_ARROW.isInBox(mouseX - x, mouseY - y - (this.rows - 1) * 17) ? 1.0F : 0.75F)
      );
      screen.drawTextureRegion(
         stack,
         (float)(x + 88),
         (float)(y + 20 + (this.rows - 1) * 17),
         31.0F,
         208.0F,
         8.0F,
         10.0F,
         ColorUtils.darker(-1, this.offset < limit && !DOWN_ARROW.isInBox(mouseX - x, mouseY - y - (this.rows - 1) * 17) ? 1.0F : 0.75F)
      );
      ItemRenderer render = screen.getRenderItem();
      if (!stacks.isEmpty()) {
         for (int column = 0; column < this.rows; column++) {
            for (int row = 0; row < 6; row++) {
               int index = row + (this.offset + column) * 6;
               if (index < stacks.size()) {
                  RenderUtils.renderGuiItem(render, stack, stacks.get(index), (float)(x + 8 + row * 17), (float)(y + 1 + column * 17));
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderForeground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      List<ItemStack> stacks = this.items.getItemList();
      screen.drawCenterString(
         stack, this.string(this.offset + " / " + (stacks.size() / 6 - Math.max(1, this.rows - 1))), x + 60, y + 21 + (this.rows - 1) * 17, -12829907
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      x += 8;
      mouseX -= x;
      mouseY -= y;
      if (mouseX >= 0 && mouseY >= 0) {
         int xIndex = mouseX / 17;
         int yIndex = mouseY / 17;
         if (xIndex < 6 && yIndex < this.rows) {
            int index = (yIndex + this.offset) * 6 + xIndex;
            List<ItemStack> list = this.items.getItemList();
            if (index < list.size()) {
               list.get(index).getTooltipLines(screen.getPlayer(), Default.NORMAL).forEach(tooltips);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(IC2Screen screen, int x, int y, int mouseX, int mouseY, int scroll, IWikiProvider provider) {
      x += 9;
      y += 9;
      mouseX -= x;
      mouseY -= y;
      if (mouseX >= 7 && mouseX < 109 && mouseY >= 0 && mouseY < this.rows * 17 && scroll != 0) {
         int limit = this.items.getItemList().size() / 6 - Math.max(1, this.rows - 1);
         int newAmount = Mth.clamp(this.offset - scroll, 0, limit);
         if (this.offset != newAmount) {
            this.offset = newAmount;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      x += 9;
      y += 9;
      if (UP_ARROW.isInBox(mouseX - x, mouseY - y - (this.rows - 1) * 17)) {
         if (this.offset > 0) {
            this.offset--;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else if (DOWN_ARROW.isInBox(mouseX - x, mouseY - y - (this.rows - 1) * 17)) {
         int limit = this.items.getItemList().size() / 6 - Math.max(1, this.rows - 1);
         if (this.offset < limit) {
            this.offset++;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else {
         return false;
      }
   }
}
