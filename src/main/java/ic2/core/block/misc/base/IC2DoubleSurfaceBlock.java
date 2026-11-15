package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.core.block.base.IC2Block;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.base.DoubleSurfaceModel;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2DoubleSurfaceBlock extends IC2Block implements ICustomBlockModel, IDualLogged {
   public static final EnumProperty<DyeColor> MAIN_COLOR = IC2Properties.MAIN_COLOR;
   public static final EnumProperty<DyeColor> SECOND_COLOR = IC2Properties.SECOND_COLOR;
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   private static final VoxelShape SHAPE = Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0));
   protected DyeableMap colorMap;
   protected String path;

   public IC2DoubleSurfaceBlock(String blockName, String path, Properties properties, DyeableMap colorMap) {
      super(blockName, properties);
      this.colorMap = colorMap;
      this.path = path;
      ObjectIterator var5 = colorMap.getBlocks().iterator();

      while (var5.hasNext()) {
         Block block = (Block)var5.next();
         if (block instanceof IC2SurfaceBlock) {
            ((IC2SurfaceBlock)block).setDouble(this);
         }
      }

      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LAVA, false));
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{MAIN_COLOR}).add(new Property[]{SECOND_COLOR}).add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      Map<String, TextureAtlasSprite> sprite = IC2Textures.getMappedEntriesBlockIC2(this.path);
      return new DoubleSurfaceModel(
         sprite.get(((DyeColor)state.getValue(MAIN_COLOR)).getName()), sprite.get(((DyeColor)state.getValue(SECOND_COLOR)).getName())
      );
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return ItemStack.EMPTY;
   }

   @Override
   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
      list.add(new ItemStack(this.colorMap.getBlock((DyeColor)state.getValue(MAIN_COLOR))));
      list.add(new ItemStack(this.colorMap.getBlock((DyeColor)state.getValue(SECOND_COLOR))));
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      if (target.getType() != Type.BLOCK) {
         return ItemStack.EMPTY;
      } else {
         BlockHitResult block = (BlockHitResult)target;
         return new ItemStack(this.colorMap.getBlock((DyeColor)state.getValue(block.getLocation().y() - (double)pos.getY() > 0.5 ? MAIN_COLOR : SECOND_COLOR)));
      }
   }

   public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
      return SHAPE;
   }

   public void m_49811_(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
   }

   @Override
   public BlockItem createItem() {
      return null;
   }
}
