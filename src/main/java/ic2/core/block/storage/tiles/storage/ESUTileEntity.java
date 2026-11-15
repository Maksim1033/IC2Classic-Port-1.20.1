package ic2.core.block.storage.tiles.storage;

import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ESUTileEntity extends BaseEnergyStorageTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_esu.png");

   public ESUTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 4, 2048, 75000000);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ESU;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.65;
   }

   @Override
   public int getGuiOffset() {
      return -15;
   }
}
