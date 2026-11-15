package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.tiles.teleporter.ITeleporterTarget;
import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.TeleportUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortableTeleporter extends IC2ElectricItem implements ISimpleItemModel, IElectricEnchantable {
   public PortableTeleporter() {
      super("portable_teleporter");
      this.capacity = 50000000;
      this.tier = 4;
      this.transferLimit = 25000;
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return 1000;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(
         this.buildKeyDescription(KeyHelper.BLOCK_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.portable_teleporter.target_set", new Object[0])
      );
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.portable_teleporter.target", new Object[0]));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("portable_teleporter");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.PLATFORM.isSimulating() && !playerIn.isShiftKeyDown()) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         TeleporterTarget target = getTarget(stack);
         if (target != null && target.getTile() instanceof ITeleporterTarget tileTarget && tileTarget.canReceive(ITeleporterTarget.TeleportType.ENTITY)) {
            this.teleportEntity(playerIn, target, tileTarget.getFacing(), stack);
            return InteractionResultHolder.success(stack);
         }
      }

      return super.use(worldIn, playerIn, handIn);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (IC2.PLATFORM.isSimulating() && context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
         BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
         if (tile instanceof ITeleporterTarget && ((ITeleporterTarget)tile).canReceive(ITeleporterTarget.TeleportType.ENTITY)) {
            setTarget(stack, new TeleporterTarget(tile), this.getName(tile));
            context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.portable_teleporter.set_target"), true);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   protected String getName(BlockEntity tile) {
      return tile instanceof Nameable && ((Nameable)tile).hasCustomName() ? ((Nameable)tile).getCustomName().getString() : null;
   }

   public void teleportEntity(Player player, TeleporterTarget target, Direction dir, ItemStack stack) {
      int weight = TeleportUtil.getWeightOfEntity(player, true);
      if (weight != 0) {
         ServerLevel server = target.getWorld();
         BlockPos pos = target.getTargetPosition();
         if (ElectricItem.MANAGER
            .use(stack, (int)((double)weight * TeleportUtil.getDistanceCost(player.level(), player.blockPosition(), server, pos) * 5.0), player)) {
            TeleportUtil.teleportEntity(player, server, pos, dir);
            IC2.AUDIO.playSound(player, new ResourceLocation("ic2", "sounds/machines/teleport.ogg"), AudioManager.SoundType.ITEM);
         }
      }
   }

   public static TeleporterTarget getTarget(ItemStack stack) {
      return TeleporterTarget.read(StackUtil.getNbtData(stack).getCompound("target"));
   }

   public static void setTarget(ItemStack stack, TeleporterTarget target, String name) {
      if (target == null) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         nbt.remove("target");
         nbt.remove("targetName");
      } else {
         CompoundTag nbt = stack.getOrCreateTag();
         nbt.put("target", target.write(new CompoundTag()));
         if (name != null) {
            nbt.putString("targetName", name);
         } else {
            nbt.remove("targetName");
         }
      }
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.UNBREAKING
         ? InteractionResult.SUCCESS
         : (enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }
}
