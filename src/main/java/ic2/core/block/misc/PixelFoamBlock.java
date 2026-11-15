package ic2.core.block.misc;

import ic2.api.blocks.PainterHelper;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.providers.SimplePosition;
import ic2.core.block.base.IC2Block;
import ic2.core.block.base.misc.color.IBlockColorListener;
import ic2.core.block.rendering.block.PixelFoamModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.geometry.Box;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PixelFoamBlock extends IC2Block implements IBlockModel {
   private String name;
   private PixelFoamBlock.PixelFoamType type;

   public PixelFoamBlock(String name, PixelFoamBlock.PixelFoamType type) {
      super("pixelfoam_" + name, Block.Properties.of().strength(3.0F, 30.0F).sound(SoundType.STONE));
      this.setHarvestTool(Tool.PICKAXE);
      this.name = name;
      this.type = type;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("cfoam/pixelfoam").get(this.name);
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (hand == InteractionHand.OFF_HAND) {
         return InteractionResult.PASS;
      } else {
         Direction facing = hit.getDirection();
         ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
         switch (this.type) {
            case HALF_LIFE:
               player.setDeltaMovement((double)facing.getStepX() * 2.5, (double)facing.getStepY(), (double)facing.getStepZ() * 2.5);
               return InteractionResult.SUCCESS;
            case ROSE:
               if (StackUtil.isStackEqual(heldItem, new ItemStack(Blocks.POPPY)) | StackUtil.isStackEqual(heldItem, new ItemStack(Blocks.ROSE_BUSH))) {
                  if (player.getHealth() == player.getMaxHealth()) {
                     return InteractionResult.PASS;
                  }

                  player.heal(player.getMaxHealth() - player.getHealth());
                  heldItem.shrink(1);
                  return InteractionResult.SUCCESS;
               }
               break;
            case MUFFIN_BUTTON:
               if (heldItem.is(ItemTags.WOODEN_BUTTONS)) {
                  return player.getInventory().add(new ItemStack(Items.COOKIE)) ? InteractionResult.SUCCESS : InteractionResult.PASS;
               }
               break;
            case PORTAL:
               BlockPos target = pos.relative(facing.getOpposite());
               if (world.isEmptyBlock(target.below())) {
                  player.teleportTo((double)target.getX() + 0.5, (double)(target.getY() - 1), (double)target.getZ() + 0.5);
                  return InteractionResult.SUCCESS;
               }

               if (world.isEmptyBlock(target.above())) {
                  player.teleportTo((double)target.getX() + 0.5, (double)target.getY(), (double)target.getZ() + 0.5);
                  return InteractionResult.SUCCESS;
               }
               break;
            default:
               return InteractionResult.PASS;
         }

         return InteractionResult.PASS;
      }
   }

   public static enum PixelFoamType {
      NOOP,
      HALF_LIFE,
      ROSE,
      MUFFIN_BUTTON,
      PORTAL,
      SPACE_BEAN;
   }

   public static class SustainablePixelFoamBlock
      extends PixelFoamBlock
      implements IRenderType,
      ICustomBlockModel,
      IBlockColorListener,
      PainterHelper.IPaintable {
      public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 3);
      public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

      public SustainablePixelFoamBlock(String name) {
         super(name, PixelFoamBlock.PixelFoamType.SPACE_BEAN);
      }

      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         builder.add(new Property[]{TYPE}).add(new Property[]{COLOR});
      }

      @Override
      public List<BlockState> getModelStates() {
         return Collections.emptyList();
      }

      @Override
      public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
         return world.setBlockAndUpdate(pos, (BlockState)state.setValue(COLOR, color));
      }

      @Override
      public DyeColor getColor(BlockState state) {
         return (DyeColor)state.getValue(COLOR);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public RenderType getType() {
         return RenderType.cutout();
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("cfoam/normal").get("gray");
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public int getItemColor(ItemStack stack, int tintIndex) {
         return DyeColor.RED.getTextColor();
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
         return ((DyeColor)state.getValue(COLOR)).getTextColor();
      }

      public BlockState getStateForPlacement(BlockPlaceContext context) {
         RandomSource rand = context.getLevel().getRandom();
         return (BlockState)((BlockState)this.defaultBlockState().setValue(COLOR, DyeColor.byId(rand.nextInt(16))))
            .setValue(TYPE, rand.nextInt(64) == 0 ? 2 : 0);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public BaseModel getForCustomState(BlockState state) {
         return new PixelFoamModel(state, this);
      }

      public boolean isRandomlyTicking(BlockState state) {
         return (Integer)state.getValue(TYPE) == 2;
      }

      public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
         if (random.nextInt(16) == 0) {
            List<BlockPos> positions = AABBUtil.getValidBlocks(
                  world,
                  pos,
                  Box.withRange(pos, 0).expand(DirectionList.HORIZONTAL, 5),
                  PixelFoamBlock.SustainablePixelFoamBlock.Filter.INSTANCE,
                  34,
                  DirectionList.ALL,
                  2147483647
               )
               .getPositions();
            if (!positions.isEmpty()) {
               BlockPos target = positions.get(random.nextInt(positions.size()));
               BlockState deadState = world.getBlockState(target);
               world.setBlockAndUpdate(target, (BlockState)deadState.setValue(TYPE, 1));
               IC2.AUDIO.playSound(new SimplePosition(world, pos), SoundEvents.GENERIC_DEATH.getLocation());
            }
         }
      }

      private static final class Filter implements AABBUtil.IBlockFilter {
         public static final AABBUtil.IBlockFilter INSTANCE = new PixelFoamBlock.SustainablePixelFoamBlock.Filter();

         @Override
         public boolean isValid(BlockState state) {
            return state.getBlock() == IC2Blocks.PIXELBLOCK_SPACE_BEAN && (Integer)state.getValue(PixelFoamBlock.SustainablePixelFoamBlock.TYPE) == 0;
         }
      }
   }
}
