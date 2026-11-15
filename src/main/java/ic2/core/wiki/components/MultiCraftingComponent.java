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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MultiCraftingComponent extends BaseWikiComponent {
   public static final int TEXT_COLOR = -12829907;
   public static final int BASE_COLOR = -1;
   public static final Box2i LEFT_BOX = new Box2i(2, 85, 10, 8);
   public static final Box2i LEFT_SUB_BOX = new Box2i(2, 71, 10, 8);
   public static final Box2i RIGHT_BOX = new Box2i(104, 85, 10, 8);
   public static final Box2i RIGHT_SUB_BOX = new Box2i(104, 71, 10, 8);
   public static final Box2i CRAFT_BOX = new Box2i(16, 85, 8, 8);
   public static final Box2i USE_BOX = new Box2i(91, 85, 8, 8);
   public static final Box2i ITEM_BOX = new Box2i(15, 67, 15, 15);
   Map<ItemStack, IRecipeReference> references;
   List<ItemStack> items;
   int itemIndex;
   int itemOffset;
   int offset;
   boolean isOutput = true;

   public MultiCraftingComponent(Map<ItemStack, IRecipeReference> references) {
      super(94);
      this.references = references;
      this.items = new ObjectArrayList(references.keySet());
      references.get(this.items.get(0)).setListener(() -> this.isOutput = this.hasOutputs());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addSearchString(Consumer<Component> listener) {
      for (ItemStack stack : this.items) {
         listener.accept(stack.getDisplayName());
      }
   }

   public IRecipeReference getReference() {
      return this.itemIndex >= 0 && this.itemIndex < this.items.size() ? this.references.get(this.items.get(this.itemIndex)) : null;
   }

   public List<IRecipeRenderer> getRecipes() {
      IRecipeReference ref = this.getReference();
      if (ref == null) {
         return ObjectLists.emptyList();
      } else {
         return this.isOutput ? ref.getOutputs() : ref.getInputs();
      }
   }

   public boolean hasInputs() {
      IRecipeReference ref = this.getReference();
      return ref != null && !ref.getInputs().isEmpty();
   }

   public boolean hasOutputs() {
      IRecipeReference ref = this.getReference();
      return ref != null && !ref.getOutputs().isEmpty();
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
            int renderIndex = -1;

            for (int i = 0; i < 5; i++) {
               screen.drawTextureRegion(stack, (float)(x + 15 + i * 17), (float)(y + 66), 124.0F, 0.0F, 18.0F, 18.0F, 18.0F, 18.0F);
               if (i + this.itemOffset == this.itemIndex) {
                  renderIndex = i;
               }
            }

            if (renderIndex != -1) {
               screen.drawTextureRegion(stack, (float)(x + 15 + renderIndex * 17), (float)(y + 66), 141.0F, 0.0F, 18.0F, 18.0F, 18.0F, 18.0F);
            }

            screen.bindDefaultTexture();
            float effect = this.itemOffset == 0 ? 0.5F : (LEFT_SUB_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(
               stack, (float)(x + LEFT_SUB_BOX.getX()), (float)(y + LEFT_SUB_BOX.getY()), 10.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect)
            );
            effect = this.itemOffset + 5 >= this.items.size() ? 0.5F : (RIGHT_SUB_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(
               stack, (float)(x + RIGHT_SUB_BOX.getX()), (float)(y + RIGHT_SUB_BOX.getY()), 0.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect)
            );
            effect = this.offset == 0 ? 0.5F : (LEFT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(
               stack, (float)(x + LEFT_BOX.getX()), (float)(y + LEFT_BOX.getY()), 10.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect)
            );
            effect = this.offset + 1 >= recipes.size() ? 0.5F : (RIGHT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F);
            screen.drawTextureRegion(
               stack, (float)(x + RIGHT_BOX.getX()), (float)(y + RIGHT_BOX.getY()), 0.0F, 209.0F, 10.0F, 8.0F, ColorUtils.darker(-1, effect)
            );
            effect = this.isOutput && this.hasInputs() ? (USE_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F) : 0.5F;
            screen.drawTextureRegion(
               stack, (float)(x + USE_BOX.getX()), (float)(y + USE_BOX.getY()), 117.0F, 235.0F, 8.0F, 8.0F, 16.0F, 16.0F, ColorUtils.darker(-1, effect)
            );
            effect = !this.isOutput && this.hasOutputs() ? (CRAFT_BOX.isInBox(mouseX - x, mouseY - y) ? 0.75F : 1.0F) : 0.5F;
            screen.drawTextureRegion(
               stack, (float)(x + CRAFT_BOX.getX()), (float)(y + CRAFT_BOX.getY()), 133.0F, 235.0F, 8.0F, 8.0F, 16.0F, 16.0F, ColorUtils.darker(-1, effect)
            );
            render.renderItems(screen, stack, x, y);

            for (int ix = 0; ix < 5; ix++) {
               if (ix + this.itemOffset < this.items.size()) {
                  screen.drawItemStack(stack, (float)(x + 15 + ix * 17), (float)(y + 66), this.items.get(ix + this.itemOffset), 18.0F, 18.0F);
               }
            }
         }
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

      mouseX -= x;
      mouseY -= y;

      for (int i = 0; i < 5; i++) {
         if (i + this.itemOffset < this.items.size() && mouseX >= 15 + i * 17 && mouseX < 33 + i * 17 && mouseY >= 66 && mouseY < 84) {
            this.items.get(i + this.itemOffset).getTooltipLines(screen.getPlayer(), Default.NORMAL).forEach(tooltips);
            break;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getHoveredStack(IC2Screen screen, int x, int y, int mouseX, int mouseY) {
      List<IRecipeRenderer> recipes = this.getRecipes();
      if (!recipes.isEmpty()) {
         ItemStack stack = recipes.get(this.offset).getHoverStack(screen, mouseX, mouseY, x, y);
         if (!stack.isEmpty()) {
            return stack;
         }
      }

      mouseX -= x;
      mouseY -= y;

      for (int i = 0; i < 5; i++) {
         if (i + this.itemOffset < this.items.size() && mouseX >= 15 + i * 17 && mouseX < 33 + i * 17 && mouseY >= 66 && mouseY < 84) {
            return this.items.get(i + this.itemOffset).copy();
         }
      }

      return ItemStack.EMPTY;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderForeground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      List<IRecipeRenderer> recipes = this.getRecipes();
      if (!recipes.isEmpty()) {
         screen.drawCenterString(stack, this.string(this.offset + 1 + " / " + recipes.size()), x + 58, y + 86, -12829907);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      x += 9;
      y += 9;
      mouseX -= x;
      mouseY -= y;
      if (LEFT_BOX.isInBox(mouseX, mouseY)) {
         if (this.offset > 0) {
            this.offset--;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else if (LEFT_SUB_BOX.isInBox(mouseX, mouseY)) {
         if (this.itemOffset > 0) {
            this.itemOffset--;
         }

         return true;
      } else if (RIGHT_BOX.isInBox(mouseX, mouseY)) {
         if (this.offset + 1 < this.getRecipes().size()) {
            this.offset++;
            IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
         }

         return true;
      } else if (RIGHT_SUB_BOX.isInBox(mouseX, mouseY)) {
         if (this.itemOffset < this.items.size() - 5) {
            this.itemOffset++;
         }

         return true;
      } else {
         for (int i = 0; i < 5; i++) {
            int index = this.itemOffset + i;
            if (index < this.items.size() && ITEM_BOX.isInBox(mouseX - i * 17, mouseY) && this.itemIndex != index) {
               this.itemIndex = index;
               this.offset = 0;
               this.isOutput = this.hasOutputs();
               IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 2.0F);
               return true;
            }
         }

         if (USE_BOX.isInBox(mouseX, mouseY)) {
            if (this.hasInputs()) {
               this.isOutput = false;
               this.offset = 0;
            }

            return true;
         } else if (CRAFT_BOX.isInBox(mouseX, mouseY)) {
            if (this.hasOutputs()) {
               this.isOutput = true;
               this.offset = 0;
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
