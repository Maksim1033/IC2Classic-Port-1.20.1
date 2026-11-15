package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import java.util.Objects;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2CarpetBlock extends IC2Block implements IBlockModel, IBlockModifiers, PainterHelper.IPaintable {
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   protected DyeableMap colorMap;
   private DyeColor color;
   private String textureName;
   private String textureFolder;
   private Block material;

   public IC2CarpetBlock(String name, Block material, String textureFolder, String textureName) {
      super(name, Properties.copy(material));
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
   }

   public IC2CarpetBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
      this.setHarvestTool(Tool.HOE.withLevel(1));
   }

   public static IC2CarpetBlock createCFoamCarpet(DyeableBlock material, DyeableMap colorMap) {
      return new IC2CarpetBlock(material, "cfoam_wool_carpet_", "cfoam/wool", colorMap);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      if (this.colorMap == null) {
         return false;
      } else {
         Block block = this.colorMap.getBlock(color);
         return block != null && world.setBlockAndUpdate(pos, PainterHelper.copyProperties(state, block.defaultBlockState()));
      }
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return this.color;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
      return !state.canSurvive(world, currentPos) ? Blocks.AIR.defaultBlockState() : state;
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return !world.isEmptyBlock(pos.below());
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return new AABB(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName);
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      return false;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      return 0;
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return side.getAxis().isHorizontal();
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      return new float[]{0.0F, 0.0F, 16.0F, 1.0F};
   }
}
