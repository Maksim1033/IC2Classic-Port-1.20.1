package ic2.core.block.storage.container;

import ic2.core.block.storage.components.AdjustableTransformerComponent;
import ic2.core.block.storage.tiles.transformer.AdjustableTransformerTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AdjustableTransformerContainer extends ContainerComponent<AdjustableTransformerTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_adjustable_transformer.png");

   public AdjustableTransformerContainer(AdjustableTransformerTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addPlayerInventory(player.getInventory());
      this.addComponent(new AdjustableTransformerComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public int getInventorySize() {
      return 0;
   }
}
