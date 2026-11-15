package ic2.core.block.misc.base;

import com.google.common.collect.ImmutableList;
import ic2.core.block.rendering.block.base.CandleCakeModel;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2CandleCake extends AbstractCandleBlock implements IBlockModel, ICustomBlockModel, IBlockModifiers.IAutoUV {
   private static final Object2IntMap<Block> BLOCK_TO_ID = (Object2IntMap<Block>)Util.make(new Object2IntOpenHashMap(), T -> {
      T.put(Blocks.CANDLE, 0);
      T.put(Blocks.WHITE_CANDLE, 1);
      T.put(Blocks.ORANGE_CANDLE, 2);
      T.put(Blocks.MAGENTA_CANDLE, 3);
      T.put(Blocks.LIGHT_BLUE_CANDLE, 4);
      T.put(Blocks.YELLOW_CANDLE, 5);
      T.put(Blocks.LIME_CANDLE, 6);
      T.put(Blocks.PINK_CANDLE, 7);
      T.put(Blocks.GRAY_CANDLE, 8);
      T.put(Blocks.LIGHT_GRAY_CANDLE, 9);
      T.put(Blocks.CYAN_CANDLE, 10);
      T.put(Blocks.PURPLE_CANDLE, 11);
      T.put(Blocks.BLUE_CANDLE, 12);
      T.put(Blocks.BROWN_CANDLE, 13);
      T.put(Blocks.GREEN_CANDLE, 14);
      T.put(Blocks.RED_CANDLE, 15);
      T.put(Blocks.BLACK_CANDLE, 16);
   });
   public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
   public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
   protected static final VoxelShape CAKE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
   protected static final VoxelShape CANDLE_SHAPE = Block.box(7.0, 8.0, 7.0, 9.0, 14.0, 9.0);
   protected static final VoxelShape SHAPE = Shapes.or(CAKE_SHAPE, CANDLE_SHAPE);
   private static final Iterable<Vec3> PARTICLE_OFFSETS = ImmutableList.of(new Vec3(0.5, 1.0, 0.5));
   private Block cake;
   ResourceLocation id;
   String textureLocation;

   public IC2CandleCake(String name, String textureLocation, Block cake) {
      super(Properties.copy(cake).lightLevel(IC2CandleCake::getCakeLight));
      this.id = GameData.checkPrefix(name, false);
      this.textureLocation = textureLocation;
      this.cake = cake;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, false)).setValue(COLOR, 0));
   }

   private static int getCakeLight(BlockState state) {
      return state.getValue(BlockStateProperties.LIT) ? 3 : 0;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> p_152905_) {
      p_152905_.add(new Property[]{LIT}).add(new Property[]{COLOR});
   }

   protected Iterable<Vec3> getParticleOffsets(BlockState state) {
      return PARTICLE_OFFSETS;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
         return InteractionResult.PASS;
      } else if (candleHit(result) && player.getItemInHand(hand).isEmpty() && (Boolean)state.getValue(LIT)) {
         extinguish(player, state, level, pos);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         InteractionResult interactionresult = IC2CakeBlock.eat(level, pos, this.cake.defaultBlockState(), player);
         if (interactionresult.consumesAction()) {
            Block.popResource(level, pos, new ItemStack(getBlockById((Integer)state.getValue(COLOR))));
         }

         return interactionresult;
      }
   }

   private static boolean candleHit(BlockHitResult hit) {
      return hit.getLocation().y - (double)hit.getBlockPos().getY() > 0.5;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         drops.add(
            this.createDrop(
               state,
               builder.getOptionalParameter(LootContextParams.TOOL),
               builder.getLevel().random,
               builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
            )
         );
         drops.add(new ItemStack(this.cake));
         return drops;
      }
   }

   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, @Nullable BlockEntity tile) {
      return new ItemStack(getBlockById((Integer)state.getValue(COLOR)));
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      if (target instanceof BlockHitResult result && candleHit(result)) {
         return new ItemStack(getBlockById((Integer)state.getValue(COLOR)));
      }

      return super.getCloneItemStack(state, target, level, pos, player);
   }

   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      return new ItemStack(this.cake);
   }

   public BlockState updateShape(BlockState state, Direction side, BlockState sourceState, LevelAccessor level, BlockPos pos, BlockPos sourcePos) {
      return side == Direction.DOWN && !state.canSurvive(level, pos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(state, side, sourceState, level, pos, sourcePos);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.below()).isSolid();
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return CakeBlock.FULL_CAKE_SIGNAL;
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new CandleCakeModel(state, this, getTextureById((Integer)state.getValue(COLOR)));
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      AABB box = CAKE_SHAPE.bounds();
      return new AABB(box.minX * 16.0, box.minY * 16.0, box.minZ * 16.0, box.maxX * 16.0, box.maxY * 16.0, box.maxZ * 16.0);
   }

   @Override
   public List<BlockState> getModelStates() {
      return ObjectLists.emptyList();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureLocation).get(side == Direction.UP ? "up" : (side == Direction.DOWN ? "down" : "outer"));
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   public static int getIdFromBlock(Block block) {
      return BLOCK_TO_ID.getInt(block);
   }

   public static Block getBlockById(int id) {
      switch (id) {
         case 0:
            return Blocks.CANDLE;
         case 1:
            return Blocks.WHITE_CANDLE;
         case 2:
            return Blocks.ORANGE_CANDLE;
         case 3:
            return Blocks.MAGENTA_CANDLE;
         case 4:
            return Blocks.LIGHT_BLUE_CANDLE;
         case 5:
            return Blocks.YELLOW_CANDLE;
         case 6:
            return Blocks.LIME_CANDLE;
         case 7:
            return Blocks.PINK_CANDLE;
         case 8:
            return Blocks.GRAY_CANDLE;
         case 9:
            return Blocks.LIGHT_GRAY_CANDLE;
         case 10:
            return Blocks.CYAN_CANDLE;
         case 11:
            return Blocks.PURPLE_CANDLE;
         case 12:
            return Blocks.BLUE_CANDLE;
         case 13:
            return Blocks.BROWN_CANDLE;
         case 14:
            return Blocks.GREEN_CANDLE;
         case 15:
            return Blocks.RED_CANDLE;
         case 16:
            return Blocks.BLACK_CANDLE;
         default:
            return Blocks.AIR;
      }
   }

   public static ResourceLocation getTextureById(int id) {
      return new ResourceLocation(id == 0 ? "block/candle" : "block/" + DyeColor.byId(id - 1).getName() + "_candle");
   }
}
