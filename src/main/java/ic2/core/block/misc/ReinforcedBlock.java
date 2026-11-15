package ic2.core.block.misc;

import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
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

public class ReinforcedBlock extends IC2Block implements IBlockModel, IRenderType {
   String texture;
   boolean glass;

   public ReinforcedBlock(String blockName, Properties properties, boolean glass) {
      super("reinforced_" + blockName, properties);
      this.setHarvestTool(Tool.PICKAXE.withLevel(2));
      this.glass = glass;
      this.texture = blockName;
   }

   public static ReinforcedBlock createSolid(String name) {
      return new ReinforcedBlock(
         name, Block.Properties.of().sound(SoundType.METAL).strength(80.0F, 150.0F).requiresCorrectToolForDrops(), false
      );
   }

   public static ReinforcedBlock createGlass(String name) {
      return new ReinforcedBlock(
         name,
         Block.Properties.of()
            .isValidSpawn((B, W, P, E) -> false)
            .isViewBlocking((B, W, P) -> false)
            .isRedstoneConductor((K, V, Y) -> false)
            .noOcclusion()
            .sound(SoundType.GLASS)
            .strength(15.0F, 150.0F)
            .requiresCorrectToolForDrops(),
         true
      );
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
      return false;
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return this.glass ? Shapes.empty() : super.getVisualShape(state, world, pos, context);
   }

   @OnlyIn(Dist.CLIENT)
   public float getShadeBrightness(BlockState p_220080_1_, BlockGetter p_220080_2_, BlockPos p_220080_3_) {
      return this.glass ? 1.0F : 0.2F;
   }

   public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
      return !this.glass;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean skipRendering(BlockState p_200122_1_, BlockState p_200122_2_, Direction p_200122_3_) {
      return this.glass && p_200122_2_.is(this);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return this.glass ? RenderType.cutout() : RenderType.solid();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("cfoam/reinforced").get(this.texture);
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return this.glass && tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) <= 0 ? ItemStack.EMPTY : super.createDrop(state, tool, rand, tile);
   }
}
