package ic2.core.item.inv.inventory;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.WikiBoxContainer;
import ic2.core.item.tool.WikiItem;
import ic2.core.wiki.base.book.BuildWiki;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WikiInventory extends PortableInventory {
   public WikiInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new WikiBoxContainer(this, player, this.getID(), windowID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Screen createGui(Player player, InteractionHand hand, Direction side, IC2Container container) {
      return new WikiInventory.WikiGui((ContainerComponent<?>)container);
   }

   @Override
   public int getSlotCount() {
      return 0;
   }

   public BuildWiki getWiki() {
      return ((WikiItem)this.stack.getItem()).getWiki();
   }

   @OnlyIn(Dist.CLIENT)
   public static class WikiGui extends ComponentContainerScreen {
      int last = -1;

      public WikiGui(ContainerComponent<?> container) {
         super(container);
      }

      @Override
      protected void init() {
         Minecraft mc = this.getMinecraft();
         if (this.last == -1) {
            OptionInstance<Integer> scale = mc.options.guiScale();
            int defaultScale = IC2.CONFIG.wikiGuiScale.get();
            if (defaultScale == -1) {
               int value = (Integer)scale.get();
               if (value != 0) {
                  scale.set(0);
                  this.last = value;
                  mc.resizeDisplay();
               }
            } else if (defaultScale != (Integer)scale.get()) {
               this.last = (Integer)scale.get();
               scale.set(defaultScale);
               mc.resizeDisplay();
            }
         }

         super.init();
      }

      public boolean isPauseScreen() {
         return IC2.CONFIG.pauseWiki.get();
      }

      public void onClose() {
         if (this.last != -1) {
            Minecraft mc = this.getMinecraft();
            mc.options.guiScale().set(this.last);
            mc.resizeDisplay();
            this.last = -1;
         }

         super.onClose();
      }

      @Override
      public void removed() {
         if (this.last != -1) {
            Minecraft mc = this.getMinecraft();
            mc.options.guiScale().set(this.last);
            int i = mc.getWindow().calculateScale(this.last, mc.isEnforceUnicode());
            mc.getWindow().setGuiScale((double)i);
            this.last = -1;
         }

         super.removed();
      }
   }
}
