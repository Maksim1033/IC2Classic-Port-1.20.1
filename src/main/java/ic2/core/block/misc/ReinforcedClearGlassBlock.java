package ic2.core.block.misc;

import ic2.core.block.base.blocks.BaseCTBlock;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReinforcedClearGlassBlock extends BaseCTBlock implements IRenderType {
   public ReinforcedClearGlassBlock(String name, String texturePath) {
      super(
         name,
         Block.Properties.of()
            .isValidSpawn((B, W, P, E) -> false)
            .isViewBlocking((B, W, P) -> false)
            .isRedstoneConductor((K, V, Y) -> false)
            .noOcclusion()
            .sound(SoundType.GLASS)
            .strength(15.0F, 150.0F)
            .requiresCorrectToolForDrops(),
         texturePath,
         null
      );
      this.setHarvestTool(Tool.PICKAXE.withLevel(2));
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
      return false;
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @OnlyIn(Dist.CLIENT)
   public float getShadeBrightness(BlockState p_220080_1_, BlockGetter p_220080_2_, BlockPos p_220080_3_) {
      return 1.0F;
   }

   public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean skipRendering(BlockState p_200122_1_, BlockState p_200122_2_, Direction p_200122_3_) {
      return p_200122_2_.is(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) <= 0 ? ItemStack.EMPTY : super.createDrop(state, tool, rand, tile);
   }
}
