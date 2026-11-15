package ic2.core.item.tool.infos;

import ic2.api.items.IDisplayProvider;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.ProgressDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.inv.container.CardContainer;
import ic2.core.item.inv.inventory.CardInventory;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

public class LagCardItem
   extends IC2SimpleItem
   implements IDisplayProvider,
   IItemModel,
   IHasHeldSlotInventory,
   INetworkItemEvent,
   INetworkItemBufferEvent<StringDataBuffer> {
   public static final int FLAG_SHOW_NAME = 1;
   public static final int FLAG_SHOW_TEXT = 2;
   public static final int FLAG_SHOW_PROGRESS = 4;

   public LagCardItem() {
      super("lag_card", new PropertiesBuilder().maxStackSize(1), "tools/monitor", "active_lag_card");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.card.init", new Object[0]));
   }

   protected int createDefaultFlags() {
      return 7;
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return StackUtil.getNbtData(stack).getIntArray("colors").length != 0 ? 1 : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItemIC2(this.textureFolder).get("inactive_lag_card");
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      int flags = this.getFlags(stack);
      if ((flags & 1) != 0) {
         infos.accept(new StringDisplayInfo(() -> this.getTileName(stack), () -> true));
      }

      if ((flags & 2) != 0) {
         infos.accept(new StringDisplayInfo(new LagCardItem.LagTextProvider(getColor(stack, 1, -1)), () -> true));
      }

      if ((flags & 4) != 0) {
         infos.accept(new ProgressDisplayInfo(new LagCardItem.LagProvider(getColor(stack, 2, -1)), () -> true));
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (IC2.KEYBOARD.isSideInventoryKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         IC2.PLATFORM.launchGui(player, hand, Direction.NORTH, this.getInventory(player, hand, stack));
         return InteractionResultHolder.success(stack);
      } else {
         return player.isShiftKeyDown() ? InteractionResultHolder.success(this.init(player.getItemInHand(hand))) : super.use(world, player, hand);
      }
   }

   protected ItemStack init(ItemStack stack) {
      CompoundTag data = stack.getOrCreateTag();
      if (!data.contains("flags")) {
         data.putInt("flags", this.createDefaultFlags());
      }

      if (!data.contains("colors")) {
         data.putIntArray("colors", new int[3]);
      }

      data.putString("base_name", Serializer.toJson(this.string("Server Lag")));
      return stack;
   }

   public int getFlags(ItemStack stack) {
      return NBTUtils.getInt(StackUtil.getNbtData(stack), "flags", this.createDefaultFlags());
   }

   public Component getTileName(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return withColor(
         stack,
         0,
         data.contains("name")
            ? Serializer.fromJson(data.getString("name"))
            : (data.contains("base_name") ? Serializer.fromJson(data.getString("base_name")) : this.string("Error"))
      );
   }

   public static int getColor(ItemStack stack, int colorIndex, int defaultValue) {
      int[] colors = StackUtil.getNbtData(stack).getIntArray("colors");
      return colors.length <= colorIndex ? defaultValue : DyeColor.byId(colors[colorIndex]).getTextColor();
   }

   public static Component withColor(ItemStack stack, int index, MutableComponent comp) {
      return comp.withStyle(ColorUtils.toColor(getColor(stack, index, -1)));
   }

   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      List<BaseTileInfoProvider.SettingEntry> entries = CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.WRITABLE_BOOK), "gui.ic2.cards.show_name", 0, BaseTileInfoProvider.SettingType.SIMPLE, 7, 50)
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.BOOK), "gui.ic2.lag_card.lag", 1, BaseTileInfoProvider.SettingType.SIMPLE, 7, 67));
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.POWERED_RAIL), "gui.ic2.lag_card.lag_progress", 2, BaseTileInfoProvider.SettingType.SIMPLE, 7, 84
         )
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 1, BaseTileInfoProvider.SettingType.COLOR, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 2, BaseTileInfoProvider.SettingType.COLOR, 24, 84));
      return entries;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new CardInventory(player, this, stack, null, 3, this.createSettings()).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new CardInventory(player, this, stack, slot, 3, this.createSettings()).load(stack);
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer()) {
         CardContainer container = this.getContainer(player, CardContainer.class);
         if (container == null) {
            return;
         }

         container.getHolder().onDataReceived(key, value);
      }
   }

   public void onDataBufferReceived(ItemStack stack, Player player, String id, StringDataBuffer buffer, Dist targetSide) {
      if (id.equalsIgnoreCase("customName") && !targetSide.isClient()) {
         CardContainer container = this.getContainer(player, CardContainer.class);
         if (container != null) {
            container.getHolder().updateName(buffer.getValue());
         }
      }
   }

   private static long getAverageLag() {
      long total = 0L;
      long[] data = ServerLifecycleHooks.getCurrentServer().tickTimes;
      int i = 0;
      int m = data.length;

      while (i < m) {
         total += data[i++];
      }

      return total / (long)data.length;
   }

   private static class LagProvider implements Supplier<ProgressDisplayInfo.Progress> {
      private static final double MAX = 5.0E7;
      int color;

      public LagProvider(int color) {
         this.color = color;
      }

      public ProgressDisplayInfo.Progress get() {
         return new ProgressDisplayInfo.Progress((double)LagCardItem.getAverageLag() / 5.0E7, this.color, ColorUtils.darker(this.color));
      }
   }

   public static class LagTextProvider implements Supplier<Component> {
      int color;

      public LagTextProvider(int color) {
         this.color = color;
      }

      public Component get() {
         long lag = LagCardItem.getAverageLag() / 1000L;
         long ms = lag / 1000L;
         return Component.literal(ms + "ms / 50ms, (" + lag + "\u00b5s, TPS: " + Math.min(20, Mth.ceil((1.0 - (double)lag / 1000000.0) * 20.0)) + ")")
            .withStyle(ColorUtils.toColor(this.color));
      }
   }
}
