package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.wiki.base.managers.IWikiProvider;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IWikiComponent {
   ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/wiki/book_components.png");
   int TEXT_COLOR = -12829907;

   int getHeight();

   @OnlyIn(Dist.CLIENT)
   void tick(IC2Screen var1);

   @OnlyIn(Dist.CLIENT)
   void renderBackground(IC2Screen var1, int var2, int var3, PoseStack var4, int var5, int var6, float var7);

   @OnlyIn(Dist.CLIENT)
   void renderForeground(IC2Screen var1, int var2, int var3, PoseStack var4, int var5, int var6, float var7);

   @OnlyIn(Dist.CLIENT)
   boolean onMouseClick(IC2Screen var1, int var2, int var3, int var4, int var5, IWikiProvider var6);

   @OnlyIn(Dist.CLIENT)
   boolean onMouseRelease(IC2Screen var1, int var2, int var3, int var4, int var5);

   @OnlyIn(Dist.CLIENT)
   boolean onMouseScroll(IC2Screen var1, int var2, int var3, int var4, int var5, int var6, IWikiProvider var7);

   @OnlyIn(Dist.CLIENT)
   void addToolTips(IC2Screen var1, int var2, int var3, PoseStack var4, int var5, int var6, Consumer<Component> var7);

   @OnlyIn(Dist.CLIENT)
   default ItemStack getHoveredStack(IC2Screen screen, int x, int y, int mouseX, int mouseY) {
      return ItemStack.EMPTY;
   }

   default boolean isOver(int mouseX, int mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
   }

   @OnlyIn(Dist.CLIENT)
   void addSearchString(Consumer<Component> var1);
}
