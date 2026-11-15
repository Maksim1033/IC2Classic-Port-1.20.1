package ic2.core.item.tool;

import ic2.api.blocks.PainterHelper;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.tooltips.ToolTipHelper;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PainterTool extends IC2Item implements ISimpleItemModel, IToolModel {
   private DyeColor color;

   public PainterTool(DyeColor color) {
      super("painter_" + color.getName(), new PropertiesBuilder().setNoRepair().maxStackSize(1).maxDamage(32).group(IC2.IC2_MAIN_GROUP));
      this.color = color;
      ColorMaps.PAINTER.putIfAbsent(color, this);
      CauldronInteraction.WATER.put(this, new PainterTool.DrainCauldron());
   }

   public PainterTool() {
      super("painter", new PropertiesBuilder().setNoRepair().maxStackSize(1).maxDamage(32).group(IC2.IC2_MAIN_GROUP));
      this.color = null;
   }

   public DyeColor getColor() {
      return this.color;
   }

   @Nonnull
   public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity target, @Nonnull InteractionHand hand) {
      if (this.color == null || stack.getItem() != this) {
         return InteractionResult.PASS;
      } else if (target instanceof Sheep sheep) {
         if (sheep.getColor() == this.color) {
            return InteractionResult.PASS;
         } else {
            sheep.setColor(this.color);
            damagePainter(stack, player, hand);
            return InteractionResult.SUCCESS;
         }
      } else {
         return super.interactLivingEntity(stack, player, target, hand);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (IC2.PLATFORM.isSimulating()) {
         PlayerHandler handler = PlayerHandler.getHandler(player);
         CompoundTag nbt = stack.getOrCreateTag();
         if (player.isShiftKeyDown()) {
            byte range = (byte)(nbt.getByte("range") + 1);
            if (range > 2) {
               range = 0;
            }

            nbt.putByte("range", range);
            player.displayClientMessage(this.translate("info.item.ic2.painter.range", new Object[]{range * 2 + 1}), false);
            return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
         }

         if (handler.modeSwitchKeyDown) {
            boolean newValue = !nbt.getBoolean("autoRefill");
            nbt.putBoolean("autoRefill", newValue);
            player.displayClientMessage(
               newValue ? this.translate("info.item.ic2.painter.auto_refill_on") : this.translate("info.item.ic2.painter.auto_refill_off"), false
            );
            return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
         }
      }

      return new InteractionResultHolder(InteractionResult.PASS, stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      BlockPos pos = context.getClickedPos();
      Level world = context.getLevel();
      Direction facing = context.getClickedFace();
      Vec3 position = this.getLocation(context);
      Box box = Box.withRange(pos, 0).expandSide(facing.getAxis(), StackUtil.getNbtData(stack).getByte("range"));
      boolean didSomething = false;

      for (BlockPos block : box) {
         if (!world.isEmptyBlock(block)
            && (
               PlayerHandler.getHandler(player).altKeyDown
                  ? this.colorBlock(world.getBlockState(block), world, block, position, null, this.color)
                     || this.colorBlock(world.getBlockState(block), world, block, position, facing, this.color)
                  : this.colorBlock(world.getBlockState(block), world, block, position, facing, this.color)
            )) {
            didSomething = true;
            if (IC2.PLATFORM.isSimulating() && !damagePainter(stack, player, hand)) {
               break;
            }
         }
      }

      if (didSomething) {
         if (IC2.PLATFORM.isSimulating()) {
            IC2.AUDIO.playSound(player, new ResourceLocation(IC2.MOD_ID, "sounds/tools/painter.ogg"), AudioManager.SoundType.ITEM, 4.0F, 1.0F);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   private Vec3 getLocation(UseOnContext context) {
      return context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
   }

   private boolean colorBlock(BlockState state, Level world, BlockPos pos, Vec3 exactPos, Direction side, DyeColor color) {
      if (color == null) {
         return false;
      } else {
         PainterHelper.IPaintable paintable = PainterHelper.INSTANCE.getPaintable(state);
         return paintable != null && paintable.recolor(state, world, pos, exactPos, side, color);
      }
   }

   private static boolean damagePainter(ItemStack stack, Player player, InteractionHand hand) {
      if (stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
         IItemTransporter transporter = TransporterManager.getTransporter(player);
         if (transporter != null && !transporter.removeItem(SpecialFilters.createDyeFilter(((PainterTool)stack.getItem()).color), null, 1, false).isEmpty()) {
            stack.setDamageValue(0);
            return true;
         } else {
            player.setItemInHand(hand, new ItemStack(IC2Items.PAINTER));
            return false;
         }
      } else {
         stack.hurtAndBreak(1, player, get(hand));
         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return this.color == null
         ? IC2Textures.getMappedEntriesItemIC2("tools/painter").get("empty")
         : IC2Textures.getMappedEntriesItemIC2("tools/painter").get(this.color.getName());
   }

   public boolean isDamaged(ItemStack stack) {
      return this.getDamage(stack) > 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      helper.addSimpleToolTip(this.translate("tooltip.item.ic2.painter.range", new Object[]{nbt.getByte("range") * 2 + 1}));
      helper.addSimpleToolTip(this.translate("tooltip.item.ic2.painter.refill", new Object[]{nbt.getBoolean("autoRefill") ? "On" : "Off"}));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.painter.change_refill", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.SNEAK_KEY, KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.painter.change_range", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.painter.paint_full_block", new Object[0]));
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   private static class DrainCauldron implements CauldronInteraction {
      public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
         if (stack.getItem() instanceof PainterTool painter && painter.getColor() != null) {
            if (!level.isClientSide()) {
               player.setItemInHand(hand, new ItemStack(IC2Items.PAINTER));
               LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
         }

         return InteractionResult.PASS;
      }
   }
}
