package ic2.core.item.tool.electric;

import ic2.api.blocks.BlockRegistries;
import ic2.api.items.ITagBlock;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.items.electric.IScanner;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.rendering.world.impl.SonarOverlay;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.features.ISonarProvider;
import ic2.core.item.inv.inventory.ScannerInventory;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScannerTool extends IC2ElectricItem implements IScanner, ISimpleItemModel, IElectricEnchantable {
   public static final ResourceLocation AUDIO_FILE = new ResourceLocation("ic2", "sounds/tools/scanner.ogg");
   int radius;
   int cost;
   boolean ore;
   String texture;
   protected boolean hasScan = true;

   public ScannerTool(String itemName, int cost, int radius, boolean ore, String texture) {
      super(itemName);
      this.cost = cost;
      this.radius = radius;
      this.ore = ore;
      this.texture = texture;
      this.capacity = 10000;
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return this.cost;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.scanner.start", new Object[0]));
      if (this instanceof IHasHeldGui) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      }

      helper.addSimpleToolTip("tooltip.item.ic2.scanner.range", this.getScanRadius(stack, false));
      List<Block> targets = this.getTargets(stack);
      if (!targets.isEmpty()) {
         helper.addDataTooltip("tooltip.item.ic2.scanner.targets");

         for (Block block : targets) {
            helper.addDataTooltip(block.getName().withStyle(ChatFormatting.YELLOW).append(" (" + this.getOreValue(stack, block.defaultBlockState()) + ")"));
         }
      }
   }

   protected List<Block> getTargets(ItemStack stack) {
      return BlockRegistries.getAllValueBlocks();
   }

   @Override
   public int getScanRadius(ItemStack stack, boolean useEnergy) {
      return useEnergy && !ElectricItem.MANAGER.use(stack, this.cost, null) ? 0 : this.radius;
   }

   @Override
   public boolean hasScanEffect(ItemStack stack) {
      return this.hasScan;
   }

   @Override
   public int getOreValue(ItemStack stack, BlockState state) {
      int value = BlockRegistries.getOreValue(state);
      return this.ore ? Math.max(0, value) : (value > 0 ? 1 : 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "tools/scanner").get(this.texture);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (this.hasScan) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (IC2.KEYBOARD.isAltKeyDown(playerIn)) {
            return InteractionResultHolder.pass(stack);
         } else if (!ElectricItem.MANAGER.use(stack, this.cost, playerIn)) {
            return InteractionResultHolder.fail(stack);
         } else if (IC2.PLATFORM.isRendering()) {
            SonarOverlay.INSTANCE.addSonar(ISonarProvider.simple(3290423, playerIn.blockPosition(), this.radius), () -> stack);
            return InteractionResultHolder.success(stack);
         } else {
            IC2.AUDIO.playSound(playerIn, AUDIO_FILE, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
            int[] values = this.getValueOfArea(stack, worldIn, playerIn.blockPosition());
            if (this.ore) {
               playerIn.displayClientMessage(this.translate("tooltip.item.ic2.scanner.ov_scan_result", new Object[]{values[2], values[0], values[1]}), false);
            } else {
               playerIn.displayClientMessage(this.translate("tooltip.item.ic2.scanner.od_scan_result", new Object[]{values[2], values[0]}), false);
            }

            return InteractionResultHolder.success(stack);
         }
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public int[] getValueOfArea(ItemStack stack, Level world, BlockPos pos) {
      int totalValue = 0;
      int found = 0;
      int scanned = 0;

      for (BlockPos entry : Box.withRange(pos, this.getScanRadius(stack, false)).setValue(DirectionList.DOWN, world.getMinBuildHeight())) {
         scanned++;
         int value = this.getOreValue(stack, world.getBlockState(entry), world, entry);
         if (value > 0) {
            totalValue += value;
            found++;
         }
      }

      return new int[]{totalValue, found, scanned};
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

   public static class ODScanner extends ScannerTool {
      public ODScanner() {
         super("od_scanner", 50, 2, false, "od");
         this.tier = 1;
         this.transferLimit = 50;
      }
   }

   public static class ODScannerBigRanged extends ScannerTool {
      public ODScannerBigRanged() {
         super("od_scanner_big_ranged", 300, 8, false, "big_ranged_od");
         this.tier = 2;
         this.transferLimit = 100;
      }
   }

   public static class ODScannerRanged extends ScannerTool {
      public ODScannerRanged() {
         super("od_scanner_ranged", 150, 7, false, "ranged_od");
         this.tier = 1;
         this.transferLimit = 50;
      }
   }

   public static class OVScanner extends ScannerTool {
      public OVScanner() {
         super("ov_scanner", 250, 4, true, "ov");
         this.tier = 2;
         this.transferLimit = 100;
      }
   }

   public static class OVScannerBigRanged extends ScannerTool {
      public OVScannerBigRanged() {
         super("ov_scanner_big_ranged", 1500, 10, true, "big_ranged_ov");
         this.tier = 3;
         this.transferLimit = 150;
      }
   }

   public static class OVScannerRanged extends ScannerTool {
      public OVScannerRanged() {
         super("ov_scanner_ranged", 750, 8, true, "ranged_ov");
         this.tier = 2;
         this.transferLimit = 100;
      }
   }

   public static class ScannerFiltered extends ScannerTool implements IHasHeldSlotInventory {
      public ScannerFiltered() {
         super("filtered_scanner", 200, 7, false, "filtered");
         this.tier = 2;
         this.transferLimit = 100;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
         super.addToolTip(stack, player, type, helper);
      }

      @Override
      protected List<Block> getTargets(ItemStack stack) {
         return new ObjectArrayList(this.getBlocks(stack));
      }

      public Set<Block> getBlocks(ItemStack stack) {
         Set<Block> blocks = CollectionUtils.<Block>createLinkedSet();

         for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("blocks", 10), CompoundTag.class)) {
            if (nbt.getBoolean("isTag")) {
               ItemStack item = ItemStack.of(nbt);
               if (item.getItem() instanceof ITagBlock) {
                  blocks.addAll(((ITagBlock)item.getItem()).getBlocks(item));
               }
            } else {
               Block block = ForgeRegistryWrapper.getBlockValue(new ResourceLocation(nbt.getString("block")));
               if (block != Blocks.AIR) {
                  blocks.add(block);
               }
            }
         }

         return blocks;
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
         if (IC2.KEYBOARD.isSideInventoryKeyDown(playerIn)) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, stack));
            return InteractionResultHolder.success(stack);
         } else {
            return super.use(worldIn, playerIn, handIn);
         }
      }

      @Override
      public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
         return new ScannerInventory(player, this, stack, null).load(stack);
      }

      @Override
      public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
         return new ScannerInventory(player, this, stack, slot).load(stack);
      }

      @Override
      public int getOreValue(ItemStack stack, BlockState state) {
         Set<Block> set = this.getBlocks(stack);
         if (set.contains(state.getBlock())) {
            return 1;
         } else {
            if (StackUtil.getNbtData(stack).getBoolean("tags")) {
               for (TagKey<Block> location : IterableWrapper.wrap(state.getTags().iterator())) {
                  for (Block block : set) {
                     if (block.builtInRegistryHolder().containsTag(location)) {
                        return 1;
                     }
                  }
               }
            }

            return 0;
         }
      }
   }

   public static class ScannerQuarry extends ScannerTool {
      public ScannerQuarry() {
         super("quarry_scanner", 25, 12, false, "quarry");
         this.tier = 2;
         this.transferLimit = 100;
         this.hasScan = false;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
         super.addToolTip(stack, player, type, helper);
         if (this.isUpgraded(stack)) {
            helper.addKeybindingTooltip(
               this.buildKeyDescription(
                  KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.scanner.range.increase", new Object[]{ChatFormatting.UNDERLINE, ChatFormatting.GRAY}
               )
            );
            helper.addKeybindingTooltip(
               this.buildKeyDescription(
                  KeyHelper.RIGHT_CLICK,
                  KeyHelper.SNEAK_KEY,
                  new Object[]{this.translate("tooltip.item.ic2.scanner.range.decrease", new ChatFormatting[]{ChatFormatting.UNDERLINE, ChatFormatting.GRAY})}
               )
            );
            helper.addKeybindingTooltip(
               this.buildKeyDescription(
                  KeyHelper.ALT_KEY,
                  new Object[]{this.translate("tooltip.item.ic2.scanner.range.multiply", new ChatFormatting[]{ChatFormatting.UNDERLINE, ChatFormatting.GRAY})}
               )
            );
         }
      }

      @Override
      public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
         if (this.m_220152_(group)) {
            ItemStack stack = new ItemStack(this);
            stack.addTagElement("upgraded", ByteTag.valueOf((byte)1));
            this.setRadius(stack, this.radius);
            addEmptyAndFullToGroup(this, items);
            addEmptyAndFullToGroup(stack, items);
         }
      }

      @Override
      public int getScanRadius(ItemStack stack, boolean useEnergy) {
         return useEnergy && !ElectricItem.MANAGER.use(stack, this.cost, null) ? 0 : (this.isUpgraded(stack) ? this.getRadius(stack) : this.radius);
      }

      @Override
      protected List<Block> getTargets(ItemStack stack) {
         return Collections.emptyList();
      }

      @Override
      public int getOreValue(ItemStack stack, BlockState state, LevelReader world, BlockPos pos) {
         return state.isAir() ? 0 : 1;
      }

      @Override
      public int getOreValue(ItemStack stack, BlockState state) {
         return state.isAir() ? 0 : 1;
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (this.isUpgraded(stack)) {
            int radius = this.getRadius(stack) + (IC2.KEYBOARD.isSneakKeyDown(playerIn) ? -1 : 1) * (IC2.KEYBOARD.isAltKeyDown(playerIn) ? 10 : 1);
            this.setRadius(stack, Mth.clamp(radius, 1, 64));
            if (IC2.PLATFORM.isSimulating()) {
               playerIn.displayClientMessage(this.translate("tooltip.item.ic2.scanner.set_range", new Object[]{radius}), false);
            }

            return InteractionResultHolder.success(stack);
         } else {
            return InteractionResultHolder.pass(stack);
         }
      }

      public int getRadius(ItemStack stack) {
         return StackUtil.getNbtData(stack).getInt("radius");
      }

      public void setRadius(ItemStack stack, int radius) {
         stack.addTagElement("radius", IntTag.valueOf(radius));
      }

      public boolean isUpgraded(ItemStack stack) {
         return StackUtil.getNbtData(stack).getBoolean("upgraded");
      }
   }
}
