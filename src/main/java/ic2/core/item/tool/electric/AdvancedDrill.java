package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.base.features.IMultiTargetTool;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box;
import java.util.Iterator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;

public class AdvancedDrill extends DrillTool implements IMultiTargetTool {
   public AdvancedDrill() {
      super("advanced_drill", 4, 80, 30.0F);
      this.tier = 3;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/drill").get("advanced");
   }

   @Override
   public float getDestroySpeed(ItemStack stack, BlockState state) {
      if (!ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack))) {
         return 1.0F;
      } else if (this.isCorrectToolForDrops(stack, state)) {
         return this.isMultiMining(stack) ? 270.0F : 30.0F;
      } else {
         return 1.0F;
      }
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      if (this.isMultiMining(stack) && this.canMultiMine(stack)) {
         Level world = player.level();
         BlockHitResult ray = getPlayerPOVHitResult(world, player, Fluid.NONE);
         int removed = 0;

         for (BlockPos offset : IterableWrapper.wrap(this.getHitPositions(stack, player, pos, ray.getDirection()))) {
            BlockState state = world.getBlockState(offset);
            if (!state.isAir() && state.getDestroySpeed(world, pos) >= 0.0F) {
               BreakEvent event = new BreakEvent(world, offset, state, player);
               MinecraftForge.EVENT_BUS.post(event);
               if (!event.isCanceled()) {
                  BlockEntity tile = world.getBlockEntity(offset);
                  if (state.onDestroyedByPlayer(world, offset, player, true, state.getFluidState())) {
                     Block block = state.getBlock();
                     block.destroy(world, offset, state);
                     block.playerDestroy(world, player, offset, state, tile, stack);
                     if (event.getExpToDrop() != -1 && world instanceof ServerLevel server) {
                        block.popExperience(server, pos, event.getExpToDrop());
                     }

                     removed++;
                  }
               }
            }
         }

         if (removed > 0) {
            player.awardStat(IC2Stats.BLOCKS_DRILLED);
            ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack) * removed, player);
            return true;
         }
      }

      return false;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         CompoundTag data = stack.getOrCreateTag();
         boolean multi = !data.getBoolean("multi");
         data.putBoolean("multi", multi);
         if (IC2.PLATFORM.isSimulating()) {
            playerIn.displayClientMessage(this.translate(multi ? "tooltip.item.ic2.multi_mine.enable" : "tooltip.item.ic2.multi_mine.disable"), false);
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   @Override
   public boolean canMultiMine(ItemStack stack) {
      return ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack) * 9);
   }

   @Override
   public boolean isMultiMining(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("multi");
   }

   @Override
   public Iterator<BlockPos> getHitPositions(ItemStack stack, Player player, BlockPos pos, Direction dir) {
      Box box = Box.fromPos(pos, true);
      if (!player.isSecondaryUseActive()) {
         box.expandSide(dir.getAxis(), 1);
      }

      return box.iterator();
   }

   @Override
   public int getMiningBoost(ItemStack stack, BlockState state) {
      int boost = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
      return 10 + (boost == 0 ? 0 : boost * boost + 1);
   }

   @Override
   public int getExtraEnergyCost(ItemStack stack) {
      int eff = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
      int unb = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, stack);
      return 25 + (eff == 0 ? 0 : eff * eff + 1) - unb * (unb + unb);
   }
}
