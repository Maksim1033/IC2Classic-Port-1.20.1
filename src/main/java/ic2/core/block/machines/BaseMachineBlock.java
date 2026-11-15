package ic2.core.block.machines;

import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BaseMachineBlock extends BaseActivityBlock<BaseTileEntity> implements IRenderType {
   public static final Properties BASE_MACHINE = Properties.of()
      .sound(SoundType.METAL)
      .strength(5.0F, 25.0F)
      .requiresCorrectToolForDrops();
   protected boolean cutout = false;

   public BaseMachineBlock(String blockName, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type) {
      super(blockName, BASE_MACHINE, provider, type);
      this.setDropProvider(drop);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public BaseMachineBlock(
      String blockName, Properties properties, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile
   ) {
      super(blockName, properties, provider, tile);
      this.setDropProvider(drop);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public BaseMachineBlock setCutout() {
      this.cutout = true;
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return this.cutout ? RenderType.cutout() : RenderType.solid();
   }
}
