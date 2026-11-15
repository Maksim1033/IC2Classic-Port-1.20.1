package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.base.ElectricToolItem;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.ToolActions;

public class MowingTool extends ElectricToolItem implements IItemModel, IToolModel {
   public MowingTool() {
      super("mowing_tool", -1.0F, Tiers.IRON.getAttackDamageBonus(), -3.0F, new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP));
      this.capacity = 10000;
      this.tier = 1;
      this.transferLimit = 100;
      this.provider = false;
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (IC2.PLATFORM.isSimulating()) {
         CompoundTag nbt = stack.getOrCreateTag();
         nbt.putBoolean("active", !nbt.getBoolean("active"));
         player.displayClientMessage(this.translate("info.item.ic2.mowing_tool.state", new Object[]{isActive(stack) ? "enabled" : "disabled"}), false);
      }

      return InteractionResultHolder.success(stack);
   }

   public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
      if (isActive(stack) && entity instanceof Player player && !world.isClientSide) {
         if (!isSelected) {
            ItemStack offHand = player.getOffhandItem();
            if (offHand.getItem() != this) {
               return;
            }
         }

         if (!ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack))) {
            return;
         }

         if (world.getGameTime() % 5L == 0L) {
            HitResult result = getPlayerPOVHitResult(world, player, Fluid.NONE);
            if (result.getType() == Type.MISS) {
               result = IC2Item.rayTraceEntities(world, player, false, 5.0);
               if (result == null || result.getType() == Type.MISS) {
                  return;
               }
            }

            if (result.getType() == Type.ENTITY && result instanceof EntityHitResult entityRayTraceResult) {
               entityRayTraceResult.getEntity().hurt(world.damageSources().generic(), 1.0F);
            } else if (result.getType() == Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
               BlockPos pos = blockHitResult.getBlockPos();
               List<ItemStack> list = CollectionUtils.<ItemStack>createList();
               UseOnContext context = new UseOnContext(player, isSelected ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockHitResult);

               for (int x = -1; x <= 1; x++) {
                  for (int y = -1; y <= 1; y++) {
                     for (int z = -1; z <= 1; z++) {
                        BlockPos target = pos.offset(x, y, z);
                        BlockState state = world.getBlockState(target);
                        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, isSelected);
                        if (newState != null) {
                           world.setBlockAndUpdate(target.immutable(), newState);
                        } else {
                           list.addAll(this.getDrops(state, (ServerLevel)world, target, null, player, stack));
                        }
                     }
                  }
               }

               this.addItemsToPlayer(list, player);
            }
         }

         return;
      }
   }

   public List<ItemStack> getDrops(BlockState state, ServerLevel world, BlockPos pos, BlockEntity tileEntity, LivingEntity entity, ItemStack stack) {
      List<ItemStack> list = new LinkedList<>();
      Block block = state.getBlock();
      if (!(block instanceof CropBlock)) {
         if (block instanceof DoublePlantBlock) {
            DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(DoublePlantBlock.HALF);
            if (half == DoubleBlockHalf.UPPER) {
               pos = pos.below();
               state = world.getBlockState(pos);
               block = state.getBlock();
               if (!(block instanceof DoublePlantBlock)) {
                  return list;
               }
            }

            List<ItemStack> drops = Block.getDrops(state, world, pos, tileEntity, entity, stack);
            if (!drops.isEmpty()) {
               list.addAll(drops);
            }

            world.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            world.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            ElectricItem.MANAGER.use(stack, 5, entity);
         } else if (block instanceof BushBlock) {
            List<ItemStack> drops = Block.getDrops(state, world, pos, tileEntity, entity, stack);
            if (!drops.isEmpty()) {
               list.addAll(drops);
            }

            world.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            ElectricItem.MANAGER.use(stack, 5, entity);
         }
      }

      if (state.is(BlockTags.LEAVES)) {
         List<ItemStack> drops = Block.getDrops(state, world, pos, tileEntity, entity, stack);
         if (!drops.isEmpty()) {
            list.addAll(drops);
         }

         world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
         world.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
         ElectricItem.MANAGER.use(stack, 5, entity);
      }

      return list;
   }

   public void addItemsToPlayer(List<ItemStack> list, Player player) {
      if (list != null && !list.isEmpty()) {
         for (ItemStack stack : list) {
            StackUtil.addOrDrop(player, stack);
         }
      }
   }

   @Override
   public int getEnergyCost(ItemStack stack) {
      return 1;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> types = CollectionUtils.<ItemStack>createList();
      ItemStack inactiveState = new ItemStack(this);
      inactiveState.getOrCreateTag().putBoolean("active", false);
      types.add(inactiveState);
      ItemStack activeState = new ItemStack(this);
      activeState.getOrCreateTag().putBoolean("active", true);
      types.add(activeState);
      return types;
   }

   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItemIC2("tools/mowing").get("base_" + (isActive(input) ? "on" : "off"));
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return isActive(stack) ? 1 : 0;
   }

   public static boolean isActive(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("active");
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.BLOCK_EFFICIENCY ? InteractionResult.SUCCESS : super.getEnchantmentCompatibility(stack, enchantment);
   }
}
