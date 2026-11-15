package ic2.core.item.tool;

import ic2.api.items.IAutoEatable;
import ic2.api.items.readers.ICropReader;
import ic2.api.items.readers.IEUReader;
import ic2.api.items.readers.IThermometer;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.base.features.IScrollableInventory;
import ic2.core.item.inv.inventory.ToolBoxInventory;
import ic2.core.item.renders.tooltip.InventoryTooltip;
import ic2.core.item.wearable.modules.AutoFeedModuleItem;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ToolBoxTool extends IC2Item implements IHasHeldSlotInventory, IItemModel, IScrollableInventory, IEUReader, IThermometer, ICropReader {
   public static final String FLAGS = "flags";
   public static final ToolBoxTool.ToolBoxData TOOL_BOX = new ToolBoxTool.ToolBoxData(
      "base", 8, 4, false, new Vec2i(0, -17), 53, new ResourceLocation("ic2", "textures/gui_sprites/items/gui_toolbox.png"), SpecialFilters.TOOL_BOX
   );
   public static final ToolBoxTool.ToolBoxData CARBON_TOOL_BOX = new ToolBoxTool.ToolBoxData(
      "nano", 15, 5, true, new Vec2i(0, 1), 44, new ResourceLocation("ic2", "textures/gui_sprites/items/gui_carbon_toolbox.png"), SpecialFilters.TOOL_BOX
   );
   public static final ToolBoxTool.ToolBoxData QUANTUM_TOOL_BOX = new ToolBoxTool.ToolBoxData(
      "quantum", 45, 9, true, new Vec2i(0, 37), 8, new ResourceLocation("ic2", "textures/gui_sprites/items/gui_iridium_toolbox.png"), SpecialFilters.TOOL_BOX
   );
   ToolBoxTool.ToolBoxData data;

   public ToolBoxTool(String itemName, ToolBoxTool.ToolBoxData data) {
      super(itemName, new PropertiesBuilder().maxStackSize(1).group(IC2.IC2_MAIN_GROUP));
      this.data = data;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.ic2.open_item_inventory", new Object[0]));
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      return Optional.of(InventoryTooltip.create(stack));
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating()) {
         if (playerIn.isShiftKeyDown() && this.hasFoodCan(stack)) {
            playerIn.startUsingItem(handIn);
            return InteractionResultHolder.success(stack);
         }

         IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, stack));
      }

      return InteractionResultHolder.success(stack);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity) {
      if (livingEntity instanceof Player player) {
         IItemTransporter transporter = TransporterManager.getTransporter(new ToolBoxTool.ToolInventory(this.data, stack));
         ItemStack food = transporter.removeItem(SpecialFilters.AUTO_EATABLE, null, 1, false);
         if (!food.isEmpty()) {
            food = ((IAutoEatable)food.getItem()).onEaten(food, world, player);
            if (!food.isEmpty()) {
               food.shrink(transporter.addItem(food, null, false));
               StackUtil.addOrDrop(player, food);
            }
         }
      }

      return stack;
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carry) {
      if (action == ClickAction.SECONDARY
         && slot.allowModification(player)
         && !otherStack.isEmpty()
         && stack.getCount() <= 1
         && this.data.getFilter().matches(otherStack)) {
         otherStack.shrink(TransporterManager.getTransporter(new ToolBoxTool.ToolInventory(this.data, stack)).addItem(otherStack, null, false));
         return true;
      } else {
         return false;
      }
   }

   public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
      return action == ClickAction.SECONDARY && slot.hasItem();
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.EAT;
   }

   public int getUseDuration(ItemStack stack) {
      return 20;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return ToolBoxInventory.create(player, this, stack, null, this.data).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return ToolBoxInventory.create(player, this, stack, slot, this.data).load(stack);
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return this.isOpen(stack) ? 1 : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> types = CollectionUtils.<ItemStack>createList();
      types.add(new ItemStack(this));
      ItemStack stack = new ItemStack(this);
      stack.addTagElement("open", ByteTag.valueOf((byte)1));
      types.add(stack);
      return types;
   }

   @Override
   public boolean isCropReader(ItemStack stack) {
      return (StackUtil.getNbtData(stack).getInt("flags") & 4) != 0;
   }

   @Override
   public boolean isThermometer(ItemStack stack) {
      return (StackUtil.getNbtData(stack).getInt("flags") & 2) != 0;
   }

   @Override
   public boolean isEUReader(ItemStack stack) {
      return (StackUtil.getNbtData(stack).getInt("flags") & 1) != 0;
   }

   public boolean hasFoodCan(ItemStack stack) {
      return (StackUtil.getNbtData(stack).getInt("flags") & 8) != 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "tools/toolbox")
         .get((this.isOpen(input) ? "filled_" : "empty_") + this.data.getItemTextureSuffix());
   }

   @Override
   public boolean canScrollInventory(ItemStack stack) {
      return this.data.isScroll();
   }

   @Override
   public int getScrollIndex(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("selected");
   }

   @Override
   public void setScrollIndex(ItemStack stack, int index) {
      stack.getOrCreateTag().putInt("selected", index);
   }

   @Override
   public int getSlotCount(ItemStack stack) {
      return this.data.getSlotCount();
   }

   @Override
   public boolean isValidItem(ItemStack stack, ItemStack toInsert) {
      return this.data.getFilter().matches(toInsert);
   }

   @Override
   public IScrollableInventory.IndexedStack[] getItemsToSwapWith(ItemStack stack, boolean scan) {
      int slotCount = this.getSlotCount(stack);
      NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
      NBTUtils.loadItems(StackUtil.getNbtData(stack), items);
      int current = this.getScrollIndex(stack);
      int first = current == 0 ? slotCount - 1 : current;
      int second = (current + 1) % slotCount;
      if (scan && !StackUtil.getNbtData(stack).getList("Items", 10).isEmpty()) {
         while (((ItemStack)items.get(first)).isEmpty()) {
            if (--first < 0) {
               first = slotCount - 1;
            }
         }

         while (((ItemStack)items.get(second)).isEmpty()) {
            second = (second + 1) % slotCount;
         }
      }

      return new IScrollableInventory.IndexedStack[]{
         new IScrollableInventory.IndexedStack(first, (ItemStack)items.get(first)), new IScrollableInventory.IndexedStack(second, (ItemStack)items.get(second))
      };
   }

   @Override
   public boolean swapItems(ItemStack stack, int clearSlot, int fillSlot, ItemStack toInsert) {
      int slotCount = this.getSlotCount(stack);
      if (fillSlot >= 0 && fillSlot < slotCount && clearSlot >= 0 && clearSlot < slotCount) {
         NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
         CompoundTag nbt = StackUtil.getNbtData(stack);
         NBTUtils.loadItems(StackUtil.getNbtData(stack), items);
         items.set(clearSlot, ItemStack.EMPTY);
         if (((ItemStack)items.get(fillSlot)).isEmpty()) {
            items.set(fillSlot, toInsert);
         } else {
            items.set(clearSlot, toInsert);
         }

         NBTUtils.saveItems(nbt, items);
         stack.setTag(nbt.isEmpty() ? null : nbt);
         return true;
      } else {
         return false;
      }
   }

   public boolean isOpen(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("open");
   }

   private static IItemTransporter getTransporter(Player player, ItemStack ignored) {
      Inventory inventory = player.getInventory();
      int i = 0;

      for (int m = Inventory.getSelectionSize(); i < m; i++) {
         ItemStack stack = (ItemStack)inventory.items.get(i);
         if (stack.getItem() instanceof ToolBoxTool tool && !tool.isOpen(stack) && tool.hasFoodCan(stack)) {
            return TransporterManager.getTransporter(new ToolBoxTool.ToolInventory(tool.data, stack));
         }
      }

      return null;
   }

   static {
      AutoFeedModuleItem.addFoodSource(ToolBoxTool::getTransporter);
   }

   public static class ToolBoxData {
      String itemTextureSuffix;
      int slotCount;
      int columns;
      boolean scroll;
      Vec2i inventoryOffset;
      int slotXStart;
      ResourceLocation guiTexture;
      IFilter filter;

      public ToolBoxData(
         String itemTextureSuffix,
         int slotCount,
         int itemsPerRow,
         boolean scroll,
         Vec2i inventoryOffset,
         int slotXStart,
         ResourceLocation guiTexture,
         IFilter filter
      ) {
         this.itemTextureSuffix = itemTextureSuffix;
         this.slotCount = slotCount;
         this.columns = itemsPerRow;
         this.scroll = scroll;
         this.inventoryOffset = inventoryOffset;
         this.slotXStart = slotXStart;
         this.guiTexture = guiTexture;
         this.filter = filter;
      }

      public String getItemTextureSuffix() {
         return this.itemTextureSuffix;
      }

      public int getSlotCount() {
         return this.slotCount;
      }

      public int getColumns() {
         return this.columns;
      }

      public boolean isScroll() {
         return this.scroll;
      }

      public Vec2i getInventoryOffset() {
         return this.inventoryOffset;
      }

      public int getSlotXStart() {
         return this.slotXStart;
      }

      public ResourceLocation getGuiTexture() {
         return this.guiTexture;
      }

      public IFilter getFilter() {
         return this.filter;
      }
   }

   private static class ToolInventory implements IHasInventory {
      ItemStack stack;
      NonNullList<ItemStack> inventory;

      public ToolInventory(ToolBoxTool.ToolBoxData data, ItemStack stack) {
         this.stack = stack;
         this.inventory = NonNullList.withSize(data.getSlotCount(), ItemStack.EMPTY);
         NBTUtils.loadItems(StackUtil.getNbtData(stack), this.inventory);
      }

      public void onChanged() {
         if (!IC2.PLATFORM.isRendering()) {
            NBTUtils.saveItems(this.stack.getOrCreateTag(), this.inventory);
            NBTUtils.putInt(
               this.stack.getOrCreateTag(),
               "flags",
               StackUtil.hasItems(
                  this.inventory, SpecialFilters.EU_READER, SpecialFilters.THERMOMETER, SpecialFilters.CROP_SCANNER, SpecialFilters.AUTO_EATABLE
               ),
               0
            );
         }
      }

      @Override
      public int getSlotCount() {
         return this.inventory.size();
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
         return (ItemStack)this.inventory.get(slot);
      }

      @Override
      public void setStackInSlot(int slot, ItemStack stack) {
         this.inventory.set(slot, stack);
         this.onChanged();
      }

      @Override
      public int getMaxStackSize(int slot) {
         return 64;
      }

      @Override
      public boolean canInsert(int slot, ItemStack stack) {
         return true;
      }

      @Override
      public boolean canExtract(int slot, ItemStack stack) {
         return true;
      }
   }
}
