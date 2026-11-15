package ic2.core.item.tool;

import ic2.api.tiles.teleporter.ITeleporterTarget;
import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.core.IC2;
import ic2.core.block.storage.tiles.tank.TeleportValveEntity;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrequencyTransmitterItem extends IC2Item implements ISimpleItemModel {
   public FrequencyTransmitterItem() {
      super("frequency_transmitter", new PropertiesBuilder().maxStackSize(1));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      TeleporterTarget target = TeleporterTarget.read(StackUtil.getNbtData(stack).getCompound("target"));
      if (target != null) {
         tooltip.add(this.translate("tooltip.item.ic2.frequency_transmitter.pos", new Object[]{this.pos(target.getDimension(), target.getTargetPosition())}));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.frequency_transmitter.clear_key", new Object[0]));
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.PLATFORM.isSimulating() && IC2.KEYBOARD.isAltKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         CompoundTag nbt = StackUtil.getNbtData(stack);
         if (nbt.contains("target")) {
            nbt.remove("target");
            playerIn.displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.clear"), false);
            if (nbt.isEmpty()) {
               stack.setTag(null);
            }
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (IC2.PLATFORM.isSimulating()) {
         BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
         if (tile instanceof ITeleporterTarget receiver) {
            CompoundTag nbt = stack.getOrCreateTag();
            TeleporterTarget target = TeleporterTarget.read(nbt.getCompound("target"));
            BlockEntity targetTile = target == null ? null : target.getTile();
            if (target != null && !(targetTile instanceof ITeleporterTarget)) {
               target = null;
            }

            if (target == null) {
               target = new TeleporterTarget(tile);
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.set"), false);
            } else if (target.isSame(tile)) {
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.same"), false);
            } else {
               receiver.setTarget(target);
               ITeleporterTarget targetInter = (ITeleporterTarget)targetTile;
               TeleporterTarget other = new TeleporterTarget(tile);
               if (!targetInter.hasTarget(other)) {
                  targetInter.setTarget(other);
               }

               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.applied"), false);
            }

            nbt.put("target", target.write(new CompoundTag()));
            return InteractionResult.SUCCESS;
         }

         if (tile instanceof TeleportValveEntity valve) {
            CompoundTag nbtx = stack.getOrCreateTag();
            TeleporterTarget targetx = TeleporterTarget.read(nbtx.getCompound("target"));
            BlockEntity targetTilex = targetx == null ? null : targetx.getTile();
            if (targetx != null && !(targetTilex instanceof TeleportValveEntity)) {
               targetx = null;
            }

            if (targetx == null) {
               targetx = new TeleporterTarget(tile);
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.valve"), false);
            } else if (targetx.isSame(tile)) {
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.same"), false);
            } else {
               valve.setTarget(targetx);
               TeleportValveEntity targetInter = (TeleportValveEntity)targetTilex;
               TeleporterTarget other = new TeleporterTarget(tile);
               if (!targetInter.hasTarget(other)) {
                  targetInter.setTarget(other);
               }

               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.frequency_transmitter.applied"), false);
            }

            nbtx.put("target", targetx.write(new CompoundTag()));
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("frequency_transmitter");
   }
}
