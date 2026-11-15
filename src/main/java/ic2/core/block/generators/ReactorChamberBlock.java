package ic2.core.block.generators;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.ISteamReactorChamber;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.tiles.impls.BaseReactorChamberTileEntity;
import ic2.core.inventory.base.IHasGui;
import ic2.core.item.block.ReactorChamberItemBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorChamberBlock extends IC2ContainerBlock implements IBlockModel {
   BlockEntityType<? extends BlockEntity> tile;
   boolean steam;

   public ReactorChamberBlock(String blockName, boolean steam, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, Properties.of().sound(SoundType.METAL).strength(5.0F).requiresCorrectToolForDrops());
      this.steam = steam;
      this.tile = tile;
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   public BlockItem createItem() {
      return new ReactorChamberItemBlock(this, this::getReactor);
   }

   private Block getReactor() {
      return this.steam ? IC2Blocks.STEAM_REACTOR : IC2Blocks.NUCLEAR_REACTOR;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.tile.create(pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.steam ? "electric/generator/steam_reactor_chamber" : "electric/generator/reactor_chamber")
         .get(side.getName());
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.isReactorNearby(context.getLevel(), context.getClickedPos()) ? super.getStateForPlacement(context) : null;
   }

   @Override
   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      InteractionResult result = super.use(state, worldIn, pos, player, handIn, hit);
      if (result != InteractionResult.PASS) {
         return result;
      } else if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else {
         IReactor reactor = this.getReactor(worldIn, pos);
         if (reactor == null) {
            return InteractionResult.PASS;
         } else {
            return !(reactor instanceof IHasGui)
                  || !IC2.PLATFORM.isRendering() && !IC2.PLATFORM.launchGui(player, handIn, hit.getDirection(), (IHasGui)reactor)
               ? InteractionResult.PASS
               : InteractionResult.SUCCESS;
         }
      }
   }

   @Override
   public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
      IReactor reactor = this.getReactor(worldIn, pos);
      if (reactor != null) {
         int puffs = reactor.getHeat() / 1000;
         if (puffs > 0) {
            puffs = worldIn.random.nextInt(puffs);

            for (int n = 0; n < puffs; n++) {
               worldIn.addParticle(
                  ParticleTypes.SMOKE,
                  (double)((float)pos.getX() + rand.nextFloat()),
                  (double)((float)pos.getY() + 0.95F),
                  (double)((float)pos.getZ() + rand.nextFloat()),
                  0.0,
                  0.0,
                  0.0
               );
            }

            puffs -= rand.nextInt(4) + 3;

            for (int n = 0; n < puffs; n++) {
               worldIn.addParticle(
                  ParticleTypes.FLAME,
                  (double)((float)pos.getX() + rand.nextFloat()),
                  (double)((float)pos.getY() + 1.0F),
                  (double)((float)pos.getZ() + rand.nextFloat()),
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   @Override
   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!IC2.PLATFORM.isRendering()) {
         if (this.isReactorNearby(worldIn, pos)) {
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
         } else {
            Block.popResource(worldIn, pos, new ItemStack(this));
            worldIn.removeBlock(pos, isMoving);
         }
      }
   }

   public boolean isReactorNearby(Level world, BlockPos pos) {
      int count = 0;

      for (Direction facing : DirectionList.ALL) {
         if (this.isReactorAt(world, pos.relative(facing))) {
            count++;
         }
      }

      return count == 1;
   }

   public boolean isReactorAt(Level world, BlockPos pos) {
      BlockEntity tile = world.getBlockEntity(pos);
      return this.steam ? tile instanceof ISteamReactor : tile instanceof IReactor && !(tile instanceof ISteamReactor);
   }

   public IReactor getReactor(Level world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof BaseReactorChamberTileEntity base) {
         if (this.steam) {
            return base instanceof ISteamReactorChamber ? base.getReactor() : null;
         } else {
            return base instanceof ISteamReactorChamber ? null : base.getReactor();
         }
      } else {
         return null;
      }
   }
}
