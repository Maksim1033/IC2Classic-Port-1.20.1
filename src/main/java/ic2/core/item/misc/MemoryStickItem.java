package ic2.core.item.misc;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.BaseNuclearReactorTileEntity;
import ic2.core.block.machines.logic.crafter.CraftRecipe;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

public class MemoryStickItem extends IC2Item implements ISimpleItemModel {
   public MemoryStickItem() {
      super("memory_stick", new PropertiesBuilder().maxStackSize(1));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.memory_stick.clear", new Object[0]));
      CompoundTag tag = StackUtil.getNbtData(stack);
      if (tag.contains("setupId")) {
         helper.addSimpleToolTip("tooltip.item.ic2.memory_stick.setup", tag.getString("setupName"));
      } else if (tag.contains("crops")) {
         helper.addSimpleToolTip("tooltip.item.ic2.memory_stick.crops", tag.getList("crops", 10).size());
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (IC2.KEYBOARD.isAltKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         stack.setTag(null);
         return InteractionResultHolder.success(stack);
      } else {
         return super.use(world, player, hand);
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (context.isSecondaryUseActive()) {
         CompoundTag tag = StackUtil.getNbtData(stack);
         if (!tag.contains("setupId")) {
            return InteractionResult.FAIL;
         } else if (this.getReactor(context.getLevel(), context.getClickedPos()) instanceof BaseNuclearReactorTileEntity nuclear) {
            if (IC2.PLATFORM.isRendering()) {
               return InteractionResult.SUCCESS;
            } else if (nuclear.applyFilter(tag.getString("setupId"))) {
               if (context.getPlayer() != null) {
                  context.getPlayer().displayClientMessage(this.translate("info.item.ic2.filter.applied", new Object[]{tag.getString("setupName")}), false);
               }

               return InteractionResult.SUCCESS;
            } else {
               if (context.getPlayer() != null) {
                  context.getPlayer().displayClientMessage(this.translate("info.item.ic2.filter.failed", new Object[]{tag.getString("setupName")}), false);
               }

               return InteractionResult.FAIL;
            }
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }

   public IReactor getReactor(Level level, BlockPos pos) {
      BlockEntity block = level.getBlockEntity(pos);
      if (block instanceof IReactorChamber chamber) {
         return chamber.getReactor();
      } else {
         return block instanceof IReactor ? (IReactor)block : null;
      }
   }

   public static void saveRecipe(ItemStack stack, int index, CraftRecipe recipe) {
      stack.getOrCreateTag().put("recipe_" + index, recipe.writeRecipe(new CompoundTag()));
   }

   public static void deleteRecipe(ItemStack stack, int index) {
      StackUtil.getNbtData(stack).remove("recipe_" + index);
   }

   public static CraftRecipe loadRecipe(ItemStack stack, int index) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      if (nbt.contains("recipe_" + index)) {
         CraftRecipe recipe = new CraftRecipe();
         recipe.readRecipe(nbt.getCompound("recipe_" + index));
         return recipe;
      } else {
         return null;
      }
   }

   public static void setSetup(ItemStack stack, String text, String name) {
      CompoundTag data = stack.getOrCreateTag();
      data.putString("plan", text);
      data.putString("plan_name", name);
   }

   public static boolean hasSetup(ItemStack stack) {
      return StackUtil.getNbtData(stack).contains("plan");
   }

   public static String getSetup(ItemStack stack) {
      return StackUtil.getNbtData(stack).getString("plan");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("memory_stick");
   }
}
