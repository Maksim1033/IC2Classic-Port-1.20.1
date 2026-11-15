package ic2.core.item.tool;

import ic2.api.blocks.IWrenchable;
import ic2.api.blocks.WrenchHelper;
import ic2.api.items.readers.IWrenchTool;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WrenchTool extends IC2Item implements IToolModel, ISimpleItemModel, IWrenchTool {
   public WrenchTool() {
      this("wrench");
   }

   public WrenchTool(String itemName) {
      this(itemName, null);
   }

   protected WrenchTool(String itemName, @Nullable PropertiesBuilder properties) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).maxDamage(160).group(IC2.IC2_MAIN_GROUP));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/wrench").get("manual");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (!this.canTakeDamage(stack, 1)) {
         return InteractionResult.PASS;
      } else {
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         Player player = context.getPlayer();
         BlockState state = world.getBlockState(pos);
         IWrenchable wrench = IWrenchable.WrenchRegistry.INSTANCE.getWrenchable(state);
         if (wrench == null) {
            return InteractionResult.PASS;
         } else {
            Direction old = wrench.getFacing(state, world, pos);
            if (old != null) {
               Direction newFacing = WrenchHelper.getFacingFromIndex(context.getClickedFace(), WrenchHelper.getDirectionIndex(context), player);
               if (newFacing != old && wrench.setFacing(state, world, pos, context.getPlayer(), newFacing)) {
                  this.damageWrench(stack, 1, player, context.getHand());
                  this.playWrenchSound(stack, player);
                  return InteractionResult.SUCCESS;
               }
            }

            if (this.canTakeDamage(stack, 5)
               && wrench.doSpecialAction(state, world, pos, context.getClickedFace(), player, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)))) {
               this.damageWrench(stack, 5, context.getPlayer(), context.getHand());
               this.playWrenchSound(stack, player);
               return InteractionResult.SUCCESS;
            } else if (!this.canTakeDamage(stack, this.canOverrideLoss(stack) ? 100 : 10) || !wrench.canRemoveBlock(state, world, pos, player)) {
               return InteractionResult.PASS;
            } else if (world.isClientSide) {
               return InteractionResult.SUCCESS;
            } else {
               boolean originalDrop = world.random.nextDouble()
                  <= wrench.getDropRate(state, world, pos, player) * this.applyFortune(stack, this.getChanceModifier(stack));
               this.damageWrench(stack, 10, player, context.getHand());
               this.playWrenchSound(stack, player);
               if (!originalDrop && this.canOverrideLoss(stack)) {
                  originalDrop = true;
                  this.onLossPrevented(player, stack);
                  if (this.hasBigCost(stack)) {
                     this.damageItem(stack, 200, player, get(context.getHand()));
                  }
               }

               for (ItemStack drop : originalDrop
                  ? wrench.getDrops(state, world, pos, player)
                  : Block.getDrops(state, (ServerLevel)world, pos, world.getBlockEntity(pos), player, stack)) {
                  Block.popResource(world, pos, drop);
               }

               world.removeBlock(pos, false);
               return InteractionResult.SUCCESS;
            }
         }
      }
   }

   public void playWrenchSound(ItemStack stack, Player player) {
      IC2.AUDIO.playSound(player, IC2Sounds.WRENCH, AudioManager.SoundType.ITEM);
   }

   public boolean canOverrideLoss(ItemStack stack) {
      return false;
   }

   public boolean canTakeDamage(ItemStack stack, int amount) {
      return true;
   }

   public void damageWrench(ItemStack stack, int amount, Player player, InteractionHand hand) {
      stack.hurtAndBreak(amount, player, get(hand));
   }

   public void onLossPrevented(Player player, ItemStack stack) {
   }

   public boolean hasBigCost(ItemStack stack) {
      return true;
   }

   public double getChanceModifier(ItemStack stack) {
      return 1.0;
   }

   public double applyFortune(ItemStack stack, double base) {
      int fortune = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);

      for (int i = 0; i < fortune; i++) {
         base += base * 0.03;
      }

      return base;
   }

   @Override
   public double getActualLoss(ItemStack stack, double originalLoss) {
      originalLoss *= this.applyFortune(stack, this.getChanceModifier(stack));
      return originalLoss < 1.0 && this.canOverrideLoss(stack) && this.canTakeDamage(stack, 100) ? 1.0 : originalLoss;
   }
}
