package ic2.core.item.misc.tfbp;

import ic2.api.items.ITerraformerBP;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.ITerraformer;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2Item;
import ic2.core.item.inv.container.TFBPContainer;
import ic2.core.item.inv.inventory.TFBPInventory;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TerraformerBlueprintItem extends IC2Item implements ISimpleItemModel, ITerraformerBP, IHasHeldSlotInventory, INetworkItemEvent {
   ITerraformerBP bluePrint;
   String texture;
   String path;

   public TerraformerBlueprintItem(String itemName, ITerraformerBP bluePrint, String path, String texture) {
      super(itemName);
      this.path = path;
      this.texture = texture;
      this.bluePrint = bluePrint;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      helper.addSimpleToolTip("item.ic2.tfbp.radius", this.getRadius(stack));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.path).get(this.texture);
   }

   @Override
   public boolean canInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
      return this.bluePrint.canInsert(stack, player, world, pos);
   }

   @Override
   public boolean isRandomized(ItemStack stack) {
      return this.bluePrint.isRandomized(stack);
   }

   @Override
   public int getEnergyUsage(ItemStack stack) {
      return this.bluePrint.getEnergyUsage(stack);
   }

   @Override
   public int getRadius(ItemStack stack) {
      int radius = StackUtil.getNbtData(stack).getInt("radius");
      return radius > 0 ? radius : this.bluePrint.getRadius(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new TFBPInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new TFBPInventory(player, this, stack, slot).load(stack);
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer()) {
         TFBPContainer container = this.getContainer(player, TFBPContainer.class);
         if (container == null) {
            return;
         }

         container.getHolder().onDataReceived(key, value);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (IC2.KEYBOARD.isSideInventoryKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         IC2.PLATFORM.launchGui(player, hand, Direction.NORTH, this.getInventory(player, hand, stack));
         return InteractionResultHolder.success(stack);
      } else {
         return super.use(world, player, hand);
      }
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      return this.bluePrint.terraform(stack, world, position, terraformer);
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
      this.bluePrint.onInsert(stack, player, world, pos);
   }

   public static void setRadius(ItemStack stack, int radius) {
      if (radius <= 0) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         nbt.remove("radius");
         if (nbt.isEmpty()) {
            stack.setTag(null);
         }
      } else {
         stack.getOrCreateTag().putInt("radius", radius);
      }
   }
}
