package ic2.core.block.misc.base;

import com.mojang.datafixers.util.Pair;
import ic2.core.block.base.IAutoCreator;
import ic2.core.item.block.CakeBlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2CakeBlock extends CakeBlock implements IBlockModel, IBlockModifiers.IAutoUV, IAutoCreator {
   ResourceLocation id;
   String textureFolder;
   String itemTextureFolder;
   String itemTexture;
   Function<Block, BlockState> candleCake;
   FoodProperties props;

   public IC2CakeBlock(String name, String textureFolder, String itemTextureFolder, String itemTexture, FoodProperties props) {
      this(name, textureFolder, itemTextureFolder, itemTexture, props, null);
   }

   public IC2CakeBlock(String name, String textureFolder, String itemTextureFolder, String itemTexture, FoodProperties props, Supplier<Block> provider) {
      super(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.WOOL));
      this.id = GameData.checkPrefix(name, false);
      this.textureFolder = textureFolder;
      this.itemTextureFolder = itemTextureFolder;
      this.itemTexture = itemTexture;
      this.props = props;
      this.candleCake = provider == null ? null : cakeProvider(provider);
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      AABB box = SHAPE_BY_BITE[state.getValue(BITES)].bounds();
      return new AABB(box.minX * 16.0, box.minY * 16.0, box.minZ * 16.0, box.maxX * 16.0, box.maxY * 16.0, box.maxZ * 16.0);
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BITES, context.getItemInHand().getDamageValue());
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         ItemStack stack = new ItemStack(this);
         stack.setDamageValue((Integer)state.getValue(BITES));
         drops.add(stack);
         return drops;
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(ItemTags.CANDLES) && (Integer)state.getValue(BITES) == 0 && this.candleCake != null) {
         Block block = Block.byItem(stack.getItem());
         if (block instanceof CandleBlock) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlockAndUpdate(pos, this.candleCake.apply(block));
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            return InteractionResult.SUCCESS;
         }
      }

      if (level.isClientSide) {
         if (this.eatCake(level, pos, state, player).consumesAction()) {
            return InteractionResult.SUCCESS;
         }

         if (stack.isEmpty()) {
            return InteractionResult.CONSUME;
         }
      }

      return this.eatCake(level, pos, state, player);
   }

   private InteractionResult eatCake(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
      if (!player.canEat(this.props.canAlwaysEat())) {
         return InteractionResult.PASS;
      } else {
         player.awardStat(Stats.EAT_CAKE_SLICE);
         player.getFoodData().eat(this.props.getNutrition(), this.props.getSaturationModifier());

         for (Pair<MobEffectInstance, Float> pair : this.props.getEffects()) {
            if (!level.isClientSide() && pair.getFirst() != null && player.getRandom().nextFloat() < (Float)pair.getSecond()) {
               player.addEffect(new MobEffectInstance((MobEffectInstance)pair.getFirst()));
            }
         }

         int i = (Integer)state.getValue(BITES);
         level.gameEvent(player, GameEvent.EAT, pos);
         if (i < 6) {
            level.setBlock(pos, (BlockState)state.setValue(BITES, i + 1), 3);
         } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
         }

         return InteractionResult.SUCCESS;
      }
   }

   public static InteractionResult eat(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
      return state.getBlock() instanceof IC2CakeBlock cake ? cake.eatCake(level, pos, state, player) : CakeBlock.eat(level, pos, state, player);
   }

   @Override
   public BlockItem createItem() {
      return new CakeBlockItem(this, this.itemTextureFolder, this.itemTexture, this.props);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureFolder)
         .get(side == Direction.UP ? "up" : (side == Direction.DOWN ? "down" : (state.getValue(BITES) > 0 && side == Direction.WEST ? "inner" : "outer")));
   }

   public static Function<Block, BlockState> cakeProvider(Supplier<Block> provider) {
      return T -> (BlockState)provider.get().defaultBlockState().setValue(IC2CandleCake.COLOR, IC2CandleCake.getIdFromBlock(T));
   }
}
