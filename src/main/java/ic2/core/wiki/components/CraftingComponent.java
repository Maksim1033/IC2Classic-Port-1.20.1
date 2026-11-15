package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.managers.IWikiProvider;
import ic2.core.wiki.recipes.providers.IRecipeReference;
import ic2.core.wiki.recipes.renderers.IRecipeRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CraftingComponent extends BaseWikiComponent {
   public static final int TEXT_COLOR = -12829907;
   public static final int BASE_COLOR = -1;
   public static Box2i LEFT_BOX = new Box2i(2, 64, 10, 8);
   public static Box2i RIGHT_BOX = new Box2i(104, 64, 10, 8);
   public static Box2i CRAFT_BOX = new Box2i(18, 64, 8, 8);
   public static Box2i USE_BOX = new Box2i(92, 64, 8, 8);
   IRecipeReference reference;
   int offset;
   boolean isOutput;

   public CraftingComponent(IRecipeReference reference) {
      super(75);
      this.reference = reference;
      this.reference.setListener(() -> this.isOutput = this.hasOutputs());
   }

   public List<IRecipeRenderer> getRecipes() {
      return this.isOutput ? this.reference.getOutputs() : this.reference.getInputs();
   }

   public boolean hasInputs() {
      return !this.reference.getInputs().isEmpty();
   }

   public boolean hasOutputs() {
      return !this.reference.getOutputs().isEmpty();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addSearchString(Consumer<Component> listener) {
      this.reference.addSearchInfo(listener);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      List<IRecipeRenderer> recipes = this.getRecipes();
      if (!recipes.isEmpty()) {
         this.offset = Math.min(this.offset, recipes.size() - 1);
         IRecipeRenderer render = recipes.get(this.offset);
         if (render != null) {
            mouseX += screen.getGuiLeft();
            mouseY += screen.getGuiTop();
            render.renderBackground(screen, stack, x, y);
            screen.bindDefaultTexture();
            float effect = this.offset == 0 ? 0.5F : (LEFT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(stack, (float)(x + 2), (float)(y + 66), 10.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect));
            effect = this.offset + 1 >= recipes.size() ? 0.5F : (RIGHT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(stack, (float)(x + 104), (float)(y + 66), 0.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect));
            effect = this.isOutput && this.hasInputs() ? (USE_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F) : 0.5F;
            screen.drawTextureRegion(stack, (float)(x + 91), (float)(y + 66), 117.0F, 235.0F, 8.0F, 8.0F, 16.0F, 16.0F, ColorUtils.darker(-1, effect));
            effect = !this.isOutput && this.hasOutputs() ? (CRAFT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F) : 0.5F;
            screen.drawTextureRegion(stack, (float)(x + 16), (float)(y + 66), 133.0F, 235.0F, 8.0F, 8.0F, 16.0F, 16.0F, ColorUtils.darker(-1, effect));
            render.renderItems(screen, stack, x, y);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderForeground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      List<IRecipeRenderer> recipes = this.getRecipes();
      if (!recipes.isEmpty()) {
         screen.drawCenterString(stack, this.string(this.offset + 1 + " / " + recipes.size()), x + 58, y + 66, -12829907);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      x += 9;
      y += 9;
      if (LEFT_BOX.isInBox(mouseX - x, mouseY - y)) {
         if (this.offset > 0) {
            this.offset--;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else if (RIGHT_BOX.isInBox(mouseX - x, mouseY - y)) {
         if (this.offset + 1 < this.getRecipes().size()) {
            this.offset++;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else if (USE_BOX.isInBox(mouseX - x, mouseY - y)) {
         if (this.hasInputs()) {
            this.isOutput = false;
            this.offset = 0;
         }

         return true;
      } else if (CRAFT_BOX.isInBox(mouseX - x, mouseY - y)) {
         if (this.hasOutputs()) {
            this.isOutput = true;
            this.offset = 0;
         }

         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (CRAFT_BOX.isInBox(mouseX - x, mouseY - y)) {
         tooltips.accept(this.translate("gui.ic2.wiki.show.craft"));
      }

      if (USE_BOX.isInBox(mouseX - x, mouseY - y)) {
         tooltips.accept(this.translate("gui.ic2.wiki.show.usage"));
      }

      List<IRecipeRenderer> recipes = this.getRecipes();
      if (!recipes.isEmpty()) {
         recipes.get(this.offset).getHoverInfo(screen, mouseX, mouseY, x, y).forEach(tooltips);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getHoveredStack(IC2Screen screen, int x, int y, int mouseX, int mouseY) {
      List<IRecipeRenderer> recipes = this.getRecipes();
      return recipes.isEmpty() ? ItemStack.EMPTY : recipes.get(this.offset).getHoverStack(screen, mouseX, mouseY, x, y);
   }
}
