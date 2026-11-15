package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IMiningDrill;
import ic2.api.items.electric.IScanner;
import ic2.core.IC2;
import ic2.core.block.rendering.world.impl.ServerBlockHighlighter;
import ic2.core.item.base.ElectricToolItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrillTool extends ElectricToolItem implements ISimpleItemModel, IMiningDrill {
   int cost;

   public DrillTool() {
      this("drill", 2, 50, 8.0F);
   }

   public DrillTool(String itemName, int level, int cost, float speed) {
      super(itemName, speed, 0.0F, -3.0F, new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP));
      this.registerTools(new Tool[]{Tool.PICKAXE.withLevel(level), Tool.SHOVEL.withLevel(level)});
      this.tier = 1;
      this.transferLimit = 100;
      this.capacity = 10000;
      this.cost = cost;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.diamond_drill.probe", new Object[0]));
   }

   @Override
   public int getEnergyCost(ItemStack stack) {
      return this.cost;
   }

   @Override
   public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
      if (entityLiving instanceof Player) {
         ((Player)entityLiving).awardStat(IC2Stats.BLOCKS_DRILLED);
      }

      return super.mineBlock(stack, worldIn, state, pos, entityLiving);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (handIn == InteractionHand.MAIN_HAND && IC2.KEYBOARD.isAltKeyDown(playerIn)) {
         ItemStack stack = playerIn.getOffhandItem();
         if (stack.getItem() instanceof IScanner scanner && scanner.hasScanEffect(stack)) {
            return InteractionResultHolder.success(playerIn.getMainHandItem());
         }
      }

      return super.use(worldIn, playerIn, handIn);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null && IC2.KEYBOARD.isAltKeyDown(player) && context.getHand() == InteractionHand.MAIN_HAND) {
         ItemStack offhand = player.getOffhandItem();
         if (offhand.getItem() instanceof IScanner scanner && scanner.hasScanEffect(offhand)) {
            int radius = scanner.getScanRadius(offhand, true);
            if (radius > 0 && !player.level().isClientSide()) {
               Level world = context.getLevel();

               for (BlockPos pos : Box.fromPos(context.getClickedPos(), true)
                  .expandSide(context.getClickedFace().getAxis(), 1)
                  .expand(context.getClickedFace().getOpposite(), radius)) {
                  BlockState state = world.getBlockState(pos);
                  if (scanner.isOreValuable(offhand, state, world, pos)) {
                     ServerBlockHighlighter.INSTANCE.addServerHighlight(player, pos.immutable(), 200, state.getMapColor(world, pos).col | 1342177280);
                  }
               }
            }

            ElectricItem.MANAGER.chargeFromArmor(offhand, context.getPlayer());
            return InteractionResult.SUCCESS;
         }
      }

      return super.onItemUseFirst(stack, context);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/drill").get("base");
   }

   @Override
   public boolean canMineBlock(ItemStack drill, BlockState state, LevelReader access, BlockPos pos) {
      return this.isCorrectToolForDrops(drill, state);
   }

   @Override
   public boolean canDrillBeUsed(ItemStack stack) {
      return ElectricItem.MANAGER.canUse(stack, this.cost);
   }

   @Override
   public void onDrillUsed(ItemStack stack) {
      ElectricItem.MANAGER.use(stack, this.cost, null);
   }

   @Override
   public int getMiningBoost(ItemStack stack, BlockState state) {
      int boost = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
      return boost == 0 ? 0 : boost * boost + 1;
   }

   @Override
   public int getExtraEnergyCost(ItemStack stack) {
      int eff = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
      int unb = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, stack);
      return (eff == 0 ? 0 : eff * eff + 1) - unb * (unb + unb);
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment != Enchantments.BLOCK_FORTUNE && enchantment != Enchantments.BLOCK_EFFICIENCY
         ? super.getEnchantmentCompatibility(stack, enchantment)
         : InteractionResult.SUCCESS;
   }

   public static class DiamondDrillTool extends DrillTool {
      public static final String DIRT_MODE_TAG = "dirt_mode";

      public DiamondDrillTool() {
         super("diamond_drill", 3, 80, 16.0F);
      }

      public DiamondDrillTool(String itemName, int level, int cost, float speed) {
         super(itemName, level, cost, speed);
         this.cost = cost;
      }

      @Override
      public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         helper.addSimpleToolTip(
            this.translate(nbt.getBoolean("dirt_mode") ? "tooltip.item.ic2.diamond_drill.dirtModeOn" : "tooltip.item.ic2.diamond_drill.dirtModeOff")
         );
         helper.addKeybindingTooltip(
            this.buildKeyDescription(KeyHelper.MODE_KEY, new Object[]{this.translate("tooltip.item.ic2.diamond_drill.toggleDirtMode")})
         );
         super.addToolTip(stack, player, type, helper);
      }

      @Override
      public float getDestroySpeed(ItemStack stack, BlockState state) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         return super.getDestroySpeed(stack, state) / (state.is(BlockTags.DIRT) && nbt.getBoolean("dirt_mode") ? 8.0F : 1.0F);
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
         if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            CompoundTag nbt = stack.getOrCreateTag();
            boolean result = !nbt.getBoolean("dirt_mode");
            nbt.putBoolean("dirt_mode", result);
            if (IC2.PLATFORM.isSimulating()) {
               playerIn.displayClientMessage(
                  this.translate(result ? "tooltip.item.ic2.diamond_drill.dirtModeOn" : "tooltip.item.ic2.diamond_drill.dirtModeOff"), false
               );
            }

            return InteractionResultHolder.success(stack);
         } else {
            return super.use(worldIn, playerIn, handIn);
         }
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture() {
         return IC2Textures.getMappedEntriesItemIC2("tools/drill").get("diamond");
      }

      @Override
      public int getMiningBoost(ItemStack stack, BlockState state) {
         int boost = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
         return 3 + (boost == 0 ? 0 : boost * boost + 1);
      }

      @Override
      public int getExtraEnergyCost(ItemStack stack) {
         int eff = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
         int unb = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, stack);
         return 14 + (eff == 0 ? 0 : eff * eff + 1) - unb * (unb + unb);
      }
   }

   public static class RockCutterDrillTool extends DrillTool.DiamondDrillTool {
      public RockCutterDrillTool() {
         super("rock_cutter_drill", 3, 20, 60.0F);
      }

      @Override
      public float getDestroySpeed(ItemStack stack, BlockState state) {
         return state.is(IC2Tags.ROCK_CUTTER_BLOCKS) ? super.getDestroySpeed(stack, state) : 1.0F;
      }

      @Override
      public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
         helper.addSimpleToolTip(this.translate("tooltip.item.ic2.rock_cutter"));
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture() {
         return IC2Textures.getMappedEntriesItemIC2("tools/drill").get("jackhammer");
      }

      @Override
      public int getMiningBoost(ItemStack stack, BlockState state) {
         int boost = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
         return (state.is(IC2Tags.ROCK_CUTTER_BLOCKS) ? 100 : 3) + (boost == 0 ? 0 : boost * boost + 1);
      }

      @Override
      public int getExtraEnergyCost(ItemStack stack) {
         int eff = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
         int unb = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, stack);
         return 20 + (eff == 0 ? 0 : eff * eff + 1) - unb * (unb + unb);
      }
   }
}
