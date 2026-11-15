package ic2.core.item.inv.container;

import ic2.core.block.machines.components.mv.CropAnalyzerComponent;
import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.item.inv.inventory.CropAnalyzerInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropAnalyzerContainer extends ItemContainer<CropAnalyzerInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_crop_analyzer.png");

   public CropAnalyzerContainer(CropAnalyzerInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);
      this.disablePreviewer();
      this.addSlot(new FilterSlot(key, 0, 8, 7, SpecialFilters.CROP_FILTER));
      this.addSlot(FilterSlot.createOutputSlot(key, 1, 41, 7));
      this.addSlot(FilterSlot.createDischargeSlot(key, 1, 2, 152, 7));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 58);
      this.addComponent(new CropAnalyzerComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.clearFlag(1);
      screen.setContainerOffset(16, 4);
      screen.modifySize(0, 59);
   }
}
