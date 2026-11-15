package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.inv.inventory.WikiInventory;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.tooltips.ToolTipHelper;
import ic2.core.wiki.base.book.BuildWiki;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WikiItem extends IC2Item implements ISimpleItemModel, IHasHeldGui {
   Supplier<BuildWiki> wiki;

   public WikiItem(Supplier<BuildWiki> wiki) {
      this("wiki_item", null, wiki);
   }

   public WikiItem(String itemName, @Nullable PropertiesBuilder properties, Supplier<BuildWiki> wiki) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).maxStackSize(1).group(IC2.IC2_MAIN_GROUP));
      this.wiki = wiki;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.SNEAK_KEY, KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.wiki.block_click", new Object[0]));
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.PLATFORM.isSimulating()) {
         IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, playerIn.getItemInHand(handIn)));
      }

      return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (context.isSecondaryUseActive() && IC2.PLATFORM.isRendering()) {
         ResourceLocation location = getPreviewLocation(ForgeRegistryWrapper.getItemKey(this.getProvider(context)));
         if (!this.getWiki().tryOpenPage(location, 0)) {
            context.getPlayer().displayClientMessage(this.string("Couldn't find page for block"), false);
            return InteractionResult.FAIL;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }

   private Item getProvider(UseOnContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      return state.getCloneItemStack(
            new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()),
            context.getLevel(),
            context.getClickedPos(),
            context.getPlayer()
         )
         .getItem();
   }

   public static ResourceLocation getPreviewLocation(ResourceLocation location) {
      return new ResourceLocation(location.getNamespace(), "preview." + location.getPath());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("misc").get("wiki_book");
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new WikiInventory(player, this, stack, null).load(stack);
   }

   public BuildWiki getWiki() {
      return this.wiki.get();
   }
}
