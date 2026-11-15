package ic2.core.block.misc.base;

import ic2.core.IC2;
import ic2.core.block.base.IC2Block;
import ic2.core.block.misc.explosive.NukeBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.base.IC2Item;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class IC2ExplosiveBlock extends IC2Block implements IBlockModel {
   private String spriteBaseName;
   private String spritePath;
   private boolean isTriggeredByExplosion;

   public IC2ExplosiveBlock(String blockName, String spritePath, String spriteBaseName, boolean isTriggeredByExplosion) {
      super(blockName, Block.Properties.of().sound(SoundType.GRASS));
      this.spriteBaseName = spriteBaseName;
      this.spritePath = spritePath;
      this.isTriggeredByExplosion = isTriggeredByExplosion;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(handIn);
      if (stack.getItem() == Items.FLINT_AND_STEEL) {
         if (IC2.PLATFORM.isRendering()) {
            return InteractionResult.SUCCESS;
         } else {
            worldIn.addFreshEntity(this.getExplosiveEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, state, false));
            worldIn.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            worldIn.removeBlock(pos, false);
            stack.hurtAndBreak(1, player, IC2Item.get(handIn));
            if (this instanceof NukeBlock) {
               player.awardStat(IC2Stats.NUKES_IGNITED);
            }

            return InteractionResult.SUCCESS;
         }
      } else if (stack.getItem() == Items.FIRE_CHARGE) {
         if (IC2.PLATFORM.isRendering()) {
            return InteractionResult.SUCCESS;
         } else {
            worldIn.addFreshEntity(this.getExplosiveEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, state, false));
            worldIn.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            worldIn.removeBlock(pos, false);
            stack.shrink(1);
            if (this instanceof NukeBlock) {
               player.awardStat(IC2Stats.NUKES_IGNITED);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (worldIn.hasNeighborSignal(pos) && IC2.PLATFORM.isSimulating()) {
         worldIn.addFreshEntity(this.getExplosiveEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, state, false));
         worldIn.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         worldIn.removeBlock(pos, false);
      }
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (worldIn.hasNeighborSignal(pos) && IC2.PLATFORM.isSimulating()) {
         worldIn.addFreshEntity(this.getExplosiveEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, state, false));
         worldIn.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         worldIn.removeBlock(pos, false);
      }
   }

   public void wasExploded(Level worldIn, BlockPos pos, Explosion explosionIn) {
      if (IC2.PLATFORM.isSimulating() && this.isTriggeredByExplosion) {
         worldIn.addFreshEntity(
            this.getExplosiveEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, worldIn.getBlockState(pos), true)
         );
         worldIn.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         worldIn.removeBlock(pos, false);
      }
   }

   public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
      this.wasExploded(level, pos, explosion);
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
   }

   public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
      return !this.isTriggeredByExplosion;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc/explosive/" + this.spritePath)
         .get(this.spriteBaseName + (side.getAxis().isVertical() ? (side.getAxisDirection() == AxisDirection.POSITIVE ? "top" : "bottom") : "sides"));
   }

   public abstract Entity getExplosiveEntity(Level var1, double var2, double var4, double var6, BlockState var8, boolean var9);
}
