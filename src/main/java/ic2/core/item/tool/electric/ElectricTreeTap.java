package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.tool.TreeTapTool;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ElectricTreeTap extends TreeTapTool implements IDamagelessElectricItem, IElectricEnchantable {
   protected int energyCost = 50;

   public ElectricTreeTap() {
      super("electric_tree_tap", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).setNoRepair().maxStackSize(1));
   }

   protected ElectricTreeTap(String itemName) {
      super(itemName, new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).setNoRepair().maxStackSize(1));
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      ItemStack stack = player.getItemInHand(context.getHand());
      if (world.getBlockState(pos).getBlock() instanceof RubberwoodLogBlock) {
         if (!ElectricItem.MANAGER.canUse(stack, this.energyCost)) {
            return InteractionResult.PASS;
         }

         boolean server = IC2.PLATFORM.isSimulating();
         List<ItemStack> list = new ArrayList<>();
         boolean extractedRubber = TreeTapTool.extractRubber(context, this.canInventoryImport(stack) ? list : null);
         if (extractedRubber && server) {
            IC2.AUDIO.playSound(context.getPlayer(), IC2Sounds.TREE_TAP, AudioManager.SoundType.ITEM);
            ElectricItem.MANAGER.use(stack, this.energyCost, player);

            for (ItemStack item : list) {
               StackUtil.addOrDrop(player, item);
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return 10000;
   }

   @Override
   public int getTier(ItemStack stack) {
      return 1;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return 100;
   }

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      if (this.canInventoryImport(stack)) {
         helper.addSimpleToolTip("tooltip.item.ic2.electric_tree_tap.inv_import");
      }
   }

   public int getBarWidth(ItemStack stack) {
      return IC2ElectricItem.getElectricWidth(stack);
   }

   public int getBarColor(ItemStack stack) {
      return IC2ElectricItem.getRGBDurability(stack);
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/treetap").get("base");
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
         ItemStack inventoryImport = new ItemStack(this);
         inventoryImport.getOrCreateTag().putBoolean("inv_import", true);
         IC2ElectricItem.addEmptyAndFullToGroup(inventoryImport, items);
      }
   }

   public boolean canInventoryImport(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return nbt.getBoolean("inv_import");
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, this.energyCost * amount), 2147483647, true, false, false);
      return 0;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING
         ? InteractionResult.FAIL
         : (enchantment == Enchantments.UNBREAKING ? InteractionResult.SUCCESS : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }
}
