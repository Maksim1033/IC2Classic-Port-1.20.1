package ic2.core.block.storage.tiles.storage;

import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MFSUTileEntity extends BaseEnergyStorageTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_mfsu.png");

   public MFSUTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 512, 10000000);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.MFSU;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.7;
   }

   @Override
   public int getGuiOffset() {
      return -15;
   }
}
