package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.base.ElectricToolItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.base.features.IMultiTargetTool;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.Iterator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class AdvancedHoe extends ElectricToolItem implements ISimpleItemModel, IMultiTargetTool {
   public AdvancedHoe() {
      this("advanced_hoe");
   }

   public AdvancedHoe(String itemName) {
      super(itemName, Tiers.NETHERITE.getSpeed(), Tiers.NETHERITE.getAttackDamageBonus(), -3.0F, new PropertiesBuilder());
      this.registerTools(new Tool[]{Tool.HOE.withLevel(Tiers.NETHERITE.getLevel())});
      this.capacity = 100000;
      this.tier = 3;
      this.transferLimit = 100;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      CompoundTag tag = StackUtil.getNbtData(stack);
      helper.addSimpleToolTip(this.translate("tooltip.item.ic2.adv_hoe.radius", new Object[]{tag.getInt("radius")}));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.adv_hoe.radius.increase", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.adv_hoe.radius.decrease", new Object[0]));
   }

   @Override
   public int getEnergyCost(ItemStack stack) {
      return 100;
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         CompoundTag nbt = stack.getOrCreateTag();
         int newValue = Mth.clamp(nbt.getInt("radius") + (playerIn.isShiftKeyDown() ? -1 : 1), 0, 5);
         nbt.putInt("radius", newValue);
         if (IC2.PLATFORM.isSimulating()) {
            playerIn.displayClientMessage(this.translate("tooltip.item.ic2.adv_hoe.radius", new Object[]{newValue}), false);
         }

         nbt.remove("lastPos");
         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      ItemStack stack = context.getItemInHand();
      Level world = context.getLevel();
      int used = 0;

      for (BlockPos pos : IterableWrapper.wrap(this.getHitPositions(stack, player, context.getClickedPos(), context.getClickedFace()))) {
         BlockState state = world.getBlockState(pos).getToolModifiedState(context, ToolActions.HOE_TILL, false);
         if (state != null) {
            world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            used++;
            if (!world.isClientSide) {
               world.setBlock(pos, state, 11);
            }
         }
      }

      if (used > 0) {
         stack.getOrCreateTag().remove("lastPos");
         ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack) * used, player);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/hoe").get("advanced");
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment != Enchantments.BLOCK_FORTUNE && enchantment != Enchantments.BLOCK_EFFICIENCY
         ? super.getEnchantmentCompatibility(stack, enchantment)
         : InteractionResult.SUCCESS;
   }

   @Override
   public boolean isMultiMining(ItemStack stack) {
      return true;
   }

   @Override
   public boolean canMultiMine(ItemStack stack) {
      return true;
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction);
   }

   @Override
   public Iterator<BlockPos> getHitPositions(ItemStack stack, Player player, BlockPos pos, Direction dir) {
      final CompoundTag nbt = stack.getOrCreateTag();
      if (pos.asLong() != nbt.getLong("lastPos")) {
         boolean remove = true;
         int limit = ElectricItem.MANAGER.getCharge(stack) / this.getEnergyCost(stack);
         if (limit > 0) {
            UseOnContext context = new UseOnContext(
               player, player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, BlockHitResult.miss(Vec3.ZERO, dir, pos)
            );
            BlockState state = player.level().getBlockState(pos).getToolModifiedState(context, ToolActions.HOE_TILL, true);
            if (state != null && player.level().isEmptyBlock(pos.above())) {
               LongArrayList list = new LongArrayList();
               list.add(pos.asLong());
               int radius = StackUtil.getNbtData(stack).getInt("radius");
               if (radius > 0) {
                  for (BlockPos subPos : Box.fromPos(pos, true).expandSide(Axis.Y, radius)) {
                     if (!subPos.equals(pos)) {
                        state = player.level().getBlockState(subPos).getToolModifiedState(context, ToolActions.HOE_TILL, true);
                        if (state != null && player.level().isEmptyBlock(subPos.above())) {
                           list.add(subPos.asLong());
                           if (list.size() >= limit) {
                              break;
                           }
                        }
                     }
                  }
               }

               nbt.putLongArray("positions", list.toLongArray());
               remove = false;
            }
         }

         if (remove) {
            nbt.remove("positions");
         }

         nbt.putLong("lastPos", pos.asLong());
      }

      return new Iterator<BlockPos>() {
         long[] positions = nbt.getLongArray("positions");
         int index = 0;
         MutableBlockPos position = new MutableBlockPos();

         @Override
         public boolean hasNext() {
            return this.index < this.positions.length;
         }

         public BlockPos next() {
            return this.position.set(this.positions[this.index++]);
         }
      };
   }
}
