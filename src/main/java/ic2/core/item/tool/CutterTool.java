package ic2.core.item.tool;

import ic2.api.energy.tile.IEnergyConductorModifiable;
import ic2.api.items.ICutterItem;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CutterTool extends IC2Item implements ICutterItem, ISimpleItemModel {
   public static ResourceLocation AUDIO_FILE = new ResourceLocation("ic2", "sounds/tools/cutter.ogg");

   public CutterTool() {
      super("cutter", new PropertiesBuilder().maxDamage(128).group(IC2.IC2_MAIN_GROUP).setNoRepair());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_LEFT_CLICK, "tooltip.item.ic2.cutter.cut", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.cutter.add", new Object[0]));
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
      ItemStack copy = itemStack.copy();
      return copy.hurt(1, IC2Item.RANDOM, null) ? ItemStack.EMPTY : copy;
   }

   @Override
   public void cutInsulation(Player player, ItemStack stack, Level world, BlockPos pos) {
      if (!IC2.PLATFORM.isRendering()) {
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof IEnergyConductorModifiable && ((IEnergyConductorModifiable)tile).tryRemoveInsulation()) {
            stack.hurtAndBreak(1, player, get(InteractionHand.MAIN_HAND));
            IC2.AUDIO.playSound(player, AUDIO_FILE, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
            Block.popResource(world, pos, new ItemStack(IC2Items.RUBBER));
         }
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof IEnergyConductorModifiable cable && cable.tryAddInsulation()) {
         IItemTransporter trans = TransporterManager.getTransporter(context.getPlayer());
         if (trans != null && !trans.removeItem(new SimpleFilter(IC2Items.RUBBER), Direction.DOWN, 1, false).isEmpty()) {
            if (IC2.PLATFORM.isSimulating()) {
               context.getItemInHand().hurtAndBreak(1, context.getPlayer(), get(context.getHand()));
            }

            return InteractionResult.SUCCESS;
         }

         cable.tryRemoveInsulation();
      }

      return InteractionResult.PASS;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("insulation_cutter");
   }
}
