package ic2.core.block.misc;

import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CFoamBlock extends IC2Block implements IBlockModel, IRenderType {
   public static final EnumProperty<CFoamBlock.FoamType> FOAM_TYPE = EnumProperty.create("type", CFoamBlock.FoamType.class);

   public CFoamBlock() {
      super("wet_cfoam", Block.Properties.of().noOcclusion().sound(SoundType.WOOL).randomTicks().strength(0.1F, 10.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FOAM_TYPE, CFoamBlock.FoamType.NORMAL));
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FOAM_TYPE});
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      state = worldIn.getBlockState(pos);
      Block id = state.getBlock();
      return id == Blocks.AIR || id == Blocks.FIRE || state.isAir() || state.canBeReplaced();
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(handIn);
      if (stack.isEmpty() || state.getValue(FOAM_TYPE) == CFoamBlock.FoamType.CABLE) {
         return InteractionResult.PASS;
      } else if (stack.is(ItemTags.SAND)) {
         worldIn.setBlockAndUpdate(
            pos, (state.getValue(FOAM_TYPE) == CFoamBlock.FoamType.NORMAL ? IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY : IC2Blocks.REINFORCED_STONE).defaultBlockState()
         );
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else if (stack.is(ItemTags.WOOL)) {
         DyeColor color = PainterHelper.INSTANCE.getColor(Block.byItem(stack.getItem()).defaultBlockState());
         worldIn.setBlockAndUpdate(pos, ColorMaps.CFOAM_WOOL.getBlock(color == null ? DyeColor.LIGHT_GRAY : color).defaultBlockState());
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else {
         if (state.getValue(FOAM_TYPE) == CFoamBlock.FoamType.NORMAL) {
            BlockState dustState = Block.byItem(stack.getItem()).defaultBlockState();
            if (PainterHelper.CONCRETE_DUST.contains(dustState.getBlock())) {
               DyeColor color = PainterHelper.INSTANCE.getColor(dustState);
               worldIn.setBlockAndUpdate(pos, ColorMaps.CFOAM_BLOCKS.getBlock(color == null ? DyeColor.LIGHT_GRAY : color).defaultBlockState());
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      CFoamBlock.FoamType type = (CFoamBlock.FoamType)state.getValue(FOAM_TYPE);
      if (type != CFoamBlock.FoamType.CABLE) {
         if (worldIn.dimension().equals(Level.NETHER)) {
            if (worldIn.random.nextInt(5) == 0) {
               worldIn.setBlockAndUpdate(
                  pos, (type == CFoamBlock.FoamType.NORMAL ? IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY : IC2Blocks.REINFORCED_STONE).defaultBlockState()
               );
            } else {
               worldIn.scheduleTick(pos, this, this.tickRate(worldIn));
            }
         } else {
            if (worldIn.getMaxLocalRawBrightness(pos) * 6 > worldIn.random.nextInt(1000)) {
               worldIn.setBlockAndUpdate(
                  pos, (type == CFoamBlock.FoamType.NORMAL ? IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY : IC2Blocks.REINFORCED_STONE).defaultBlockState()
               );
            } else {
               worldIn.scheduleTick(pos, this, this.tickRate(worldIn));
            }
         }
      }
   }

   public int tickRate(Level worldIn) {
      return worldIn.dimension().equals(Level.NETHER) ? 40 : 500;
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return ItemStack.EMPTY;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("cfoam").get(state.getValue(FOAM_TYPE) == CFoamBlock.FoamType.CABLE ? "wet_containing" : "wet");
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   public static enum FoamType implements StringRepresentable {
      NORMAL("normal"),
      REINFORCED("reinforced"),
      CABLE("cable");

      final String name;

      private FoamType(String name) {
         this.name = name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
