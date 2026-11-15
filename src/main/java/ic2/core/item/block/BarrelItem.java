package ic2.core.item.block;

import ic2.core.item.base.IC2TexturedBlockItem;
import ic2.core.item.food_and_drink.drinks.Beer;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class BarrelItem extends IC2TexturedBlockItem implements IItemModel, IAdvancedTooltip, ILangHelper {
   public BarrelItem(Block block) {
      super(block, "food_and_drink", "barrel");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag flag, ToolTipHelper helper) {
      if (PlayerHandler.getClientHandler().hasThermometer()) {
         CompoundTag tag = StackUtil.getNbtData(stack);
         int type = tag.getInt("type");
         int data = tag.getInt("data");
         switch (type) {
            case 1:
               helper.addSimpleToolTip(this.translate("tooltip.block.ic2.barrel.type.beer").withStyle(ChatFormatting.GOLD));
               helper.addSimpleToolTip(
                  this.translate("tooltip.block.ic2.barrel.water.stored", new Object[]{Formatters.EU_FORMAT.format((long)tag.getInt("fluid"))})
                     .withStyle(ChatFormatting.BLUE)
               );
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.beer.quality." + Beer.getTime(data) + ".name").withStyle(ChatFormatting.AQUA));
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.beer.alc." + Beer.getHops(data) + ".name").withStyle(ChatFormatting.GREEN));
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.beer.solid." + Beer.getSolid(data) + ".name").withStyle(ChatFormatting.YELLOW));
               helper.addDataTooltip(this.translate("ic2.probe.barrel.beer.hops.name", new Object[]{tag.getInt("hops")}).withStyle(ChatFormatting.GREEN));
               helper.addDataTooltip(this.translate("ic2.probe.barrel.beer.wheat.name", new Object[]{tag.getInt("wheat")}).withStyle(ChatFormatting.YELLOW));
               break;
            case 2:
               helper.addSimpleToolTip(this.translate("tooltip.block.ic2.barrel.type.rum").withStyle(ChatFormatting.GOLD));
               helper.addSimpleToolTip(
                  this.translate("ic2.probe.barrel.beer.sugar_cane.name", new Object[]{tag.getInt("fluid") / 1000}).withStyle(ChatFormatting.GREEN)
               );
               helper.addSimpleToolTip(this.translate("tooltip.block.ic2.barrel.rum.progress", new Object[]{data + "%"}).withStyle(ChatFormatting.AQUA));
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            default:
               break;
            case 5:
               helper.addSimpleToolTip(this.translate("tooltip.block.ic2.barrel.type.whisky").withStyle(ChatFormatting.GOLD));
               helper.addSimpleToolTip(
                  this.translate("tooltip.block.ic2.barrel.water.stored", new Object[]{Formatters.EU_FORMAT.format((long)tag.getInt("fluid"))})
                     .withStyle(ChatFormatting.BLUE)
               );
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.whisky.grist.name", new Object[]{tag.getInt("hops")}).withStyle(ChatFormatting.GREEN));
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.whisky.years.name", new Object[]{data}).withStyle(ChatFormatting.DARK_AQUA));
               break;
            case 10:
               MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(tag.getString("potion")));
               helper.addSimpleToolTip(this.translate("tooltip.block.ic2.barrel.type.potion").withStyle(ChatFormatting.GOLD));
               helper.addSimpleToolTip(
                  this.translate("tooltip.block.ic2.barrel.water.stored", new Object[]{Formatters.EU_FORMAT.format((long)tag.getInt("fluid"))})
                     .withStyle(ChatFormatting.BLUE)
               );
               helper.addSimpleToolTip(
                  this.translate(
                        "ic2.probe.barrel.status.output.name",
                        new Object[]{effect == null ? this.translate("tooltip.block.ic2.barrel.unknown") : effect.getDisplayName()}
                     )
                     .withStyle(ChatFormatting.GREEN)
               );
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.potion.quality." + tag.getInt("quality") + ".name").withStyle(ChatFormatting.AQUA));
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.beer.glowstone.name", new Object[]{tag.getInt("red")}).withStyle(ChatFormatting.YELLOW));
               helper.addSimpleToolTip(this.translate("ic2.probe.barrel.beer.redstone.name", new Object[]{tag.getInt("glow")}).withStyle(ChatFormatting.RED));
         }
      }
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      switch (StackUtil.getNbtData(stack).getInt("type")) {
         case 1:
            return 1;
         case 2:
            return 2;
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return 0;
         case 5:
            return 3;
         case 10:
            return 4;
      }
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> items = CollectionUtils.<ItemStack>createList();
      items.add(this.create(1));
      items.add(this.create(2));
      items.add(this.create(5));
      items.add(this.create(10));
      return items;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      switch (StackUtil.getNbtData(input).getInt("type")) {
         case 1:
            return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "food_and_drink/barrel").get("beer");
         case 2:
            return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "food_and_drink/barrel").get("rum");
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return this.getTexture();
         case 5:
            return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "food_and_drink/barrel").get("whisky");
         case 10:
            return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "food_and_drink/barrel").get("potion");
      }
   }

   private ItemStack create(int type) {
      ItemStack stack = new ItemStack(IC2Blocks.BARREL);
      stack.addTagElement("type", IntTag.valueOf(type));
      return stack;
   }
}
