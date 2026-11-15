package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.item.base.ElectricToolItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class ChainsawTool extends ElectricToolItem implements ISimpleItemModel {
   public static final ResourceLocation IDLE = new ResourceLocation("ic2", "sounds/tools/chainsaw/chainsaw_idle.ogg");
   public static final ResourceLocation STOP = new ResourceLocation("ic2", "sounds/tools/chainsaw/chainsaw_stop.ogg");
   public static final ResourceLocation USE_1 = new ResourceLocation("ic2", "sounds/tools/chainsaw/chainsaw_use_one.ogg");
   public static final ResourceLocation USE_2 = new ResourceLocation("ic2", "sounds/tools/chainsaw/chainsaw_use_two.ogg");
   int cost;

   public ChainsawTool() {
      this("chainsaw", 2, 50, 8.0F);
   }

   public ChainsawTool(String itemName, int level, int cost, float speed) {
      super(itemName, speed, 1.0F, 0.0F, new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP));
      this.registerTools(new Tool[]{Tool.AXE.withLevel(level)});
      this.tier = 1;
      this.transferLimit = 100;
      this.capacity = 10000;
      this.miningSpeed = 12.0F;
      this.cost = cost;
   }

   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      return Items.IRON_AXE.isCorrectToolForDrops(state) || state.getBlock() == Blocks.COBWEB;
   }

   @Override
   public float getDestroySpeed(ItemStack stack, BlockState state) {
      if (!ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack))) {
         return 1.0F;
      } else {
         return state.is(BlockTags.MINEABLE_WITH_AXE)
               || state.is(BlockTags.LEAVES)
               || state.is(BlockTags.SWORD_EFFICIENT)
               || state.is(BlockTags.WOOL)
            ? this.miningSpeed
            : super.getDestroySpeed(stack, state);
      }
   }

   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (entity.level().isClientSide) {
         return InteractionResult.PASS;
      } else if (!(entity instanceof IForgeShearable target)) {
         return InteractionResult.PASS;
      } else {
         BlockPos pos = entity.blockPosition();
         if (target.isShearable(stack, entity.level(), pos) && ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack) * 2)) {
            RandomSource rand = entity.getRandom();

            for (ItemStack item : target.onSheared(
               player, stack, entity.level(), pos, EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack)
            )) {
               ItemEntity itemEntity = entity.spawnAtLocation(item, 1.0F);
               itemEntity.setDeltaMovement(
                  itemEntity.getDeltaMovement()
                     .add(
                        (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F),
                        (double)(rand.nextFloat() * 0.05F),
                        (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)
                     )
               );
            }

            ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack) * 2, player);
            IC2.AUDIO.playSound(player, player.level().random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (attacker instanceof Player player) {
         if (ElectricItem.MANAGER.use(stack, this.cost, attacker)) {
            target.hurt(target.damageSources().playerAttack(player), 10.0F);
         } else {
            target.hurt(target.damageSources().playerAttack(player), 1.0F);
         }

         IC2.AUDIO.playSound(attacker, target.level().random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
         if (target.getHealth() <= 0.0F) {
            player.awardStat(IC2Stats.CHAINSAW_KILLS);
            if (target instanceof Creeper) {
               IC2Advancements.ID_TRIGGER.onTrigger((Player)attacker, new ResourceLocation("ic2:creeper_kill"));
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
      if (!player.level().isClientSide && !player.isCreative()) {
         BlockState state = player.level().getBlockState(pos);
         Block block = state.getBlock();
         if ((state.is(BlockTags.LEAVES) || block == Blocks.COBWEB) && ElectricItem.MANAGER.canUse(itemstack, this.getEnergyCost(itemstack))) {
            ItemStack drop = new ItemStack(block);
            float f = 0.7F;
            double d = (double)(player.level().random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
            double d1 = (double)(player.level().random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
            double d2 = (double)(player.level().random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
            ItemEntity itemEntity = new ItemEntity(player.level(), (double)pos.getX() + d, (double)pos.getY() + d1, (double)pos.getZ() + d2, drop);
            itemEntity.setDefaultPickUpDelay();
            player.level().addFreshEntity(itemEntity);
            ElectricItem.MANAGER.use(itemstack, this.getEnergyCost(itemstack), player);
            IC2.AUDIO.playSound(player, player.level().random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
            player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            player.awardStat(Stats.BLOCK_MINED.get(block));
            if (block == Blocks.COBWEB) {
               player.awardStat(IC2Stats.BLOCKS_SAWED);
            }

            player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
      IC2.AUDIO.playSound(entity, world.random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
      if (entity instanceof Player) {
         ((Player)entity).awardStat(IC2Stats.BLOCKS_SAWED);
      }

      return super.mineBlock(stack, world, state, pos, entity);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState stripped = world.getBlockState(pos).getToolModifiedState(context, ToolActions.AXE_STRIP, false);
      if (stripped != null) {
         Player player = context.getPlayer();
         IC2.AUDIO.playSound(player, world.random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
         if (!world.isClientSide) {
            world.setBlock(pos, stripped, 11);
            ItemStack stack = context.getItemInHand();
            ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack), player);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return toolAction == ToolActions.AXE_STRIP;
   }

   @Override
   public int getEnergyCost(ItemStack stack) {
      return this.cost;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/chainsaw").get("base");
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment != Enchantments.BLOCK_FORTUNE
            && enchantment != Enchantments.MOB_LOOTING
            && enchantment != Enchantments.BLOCK_EFFICIENCY
            && enchantment != Enchantments.SHARPNESS
            && enchantment != Enchantments.SMITE
            && enchantment != Enchantments.BANE_OF_ARTHROPODS
         ? super.getEnchantmentCompatibility(stack, enchantment)
         : InteractionResult.SUCCESS;
   }
}
