package ic2.core.item.tool.infos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import ic2.api.items.IDisplayProvider;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.inv.container.CardContainer;
import ic2.core.item.inv.inventory.CardInventory;
import ic2.core.item.tool.infos.components.TimeDisplayInfo;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.SanityHelper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import org.apache.commons.lang3.time.DurationFormatUtils;

public class TimeCardItem
   extends IC2SimpleItem
   implements IDisplayProvider,
   IItemModel,
   INetworkItemEvent,
   INetworkItemBufferEvent<StringDataBuffer>,
   IHasHeldSlotInventory {
   public static final int FLAG_SHOW_NAME = 1;
   public static final int FLAG_DAY_TIME = 2;
   public static final int FLAG_SAVE_TIME = 4;
   public static final int FLAG_SERVER_TIME = 8;
   public static final int FLAG_REAL_TIME = 16;
   public static final int FLAG_ONLY_TIME_DAY = 32;
   public static final int FLAG_ONLY_TIME_SAVE = 64;
   public static final int FLAG_ONLY_TIME_SERVER = 128;
   public static final int FLAG_ONLY_TIME_REAL = 256;

   public TimeCardItem() {
      super("time_card", new PropertiesBuilder().maxStackSize(1), "tools/monitor", "active_time_card");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.card.init", new Object[0]));
   }

   protected int createDefaultFlags() {
      return 31;
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return StackUtil.getNbtData(stack).contains("world") ? 1 : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItemIC2(this.textureFolder).get("inactive_time_card");
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         this.storeDimension(player.getItemInHand(hand), world);
         return InteractionResultHolder.success(player.getItemInHand(hand));
      } else if (IC2.KEYBOARD.isSideInventoryKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         IC2.PLATFORM.launchGui(player, hand, Direction.NORTH, this.getInventory(player, hand, stack));
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(player.getItemInHand(hand));
      }
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      Level world = this.getWorld(stack);
      if (world != null) {
         int flags = this.getFlags(stack);
         if ((flags & 1) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.getTileName(stack), () -> true));
         }

         if ((flags & 2) != 0) {
            infos.accept(new StringDisplayInfo(new TimeCardItem.GameTimeProvider(world, getColor(stack, 1, -1), (flags & 32) != 0, true), () -> true));
         }

         if ((flags & 4) != 0) {
            infos.accept(new StringDisplayInfo(new TimeCardItem.GameTimeProvider(world, getColor(stack, 2, -1), (flags & 64) != 0, false), () -> true));
         }

         if ((flags & 8) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 3, this.getServerTime((flags & 128) != 0)), () -> true));
         }

         if ((flags & 16) != 0) {
            infos.accept(new TimeDisplayInfo(getColor(stack, 4, -1), (flags & 256) != 0));
         }
      } else {
         infos.accept(new StringDisplayInfo(() -> this.translate("tooltip.item.ic2.time_card.no_world"), () -> true));
      }
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
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.CLOCK), "gui.ic2.lag_card.lag_progress", 3, BaseTileInfoProvider.SettingType.SIMPLE, 80, 50)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.CLOCK), "gui.ic2.lag_card.lag_progress", 4, BaseTileInfoProvider.SettingType.SIMPLE, 80, 67)
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 1, BaseTileInfoProvider.SettingType.COLOR, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 2, BaseTileInfoProvider.SettingType.COLOR, 24, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 3, BaseTileInfoProvider.SettingType.COLOR, 97, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 4, BaseTileInfoProvider.SettingType.COLOR, 97, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("P"), "gui.ic2.time_card.disable_prefix", 5, BaseTileInfoProvider.SettingType.TEXT, 41, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("P"), "gui.ic2.time_card.disable_prefix", 6, BaseTileInfoProvider.SettingType.TEXT, 41, 84));
      entries.add(
         new BaseTileInfoProvider.SettingEntry(this.string("P"), "gui.ic2.time_card.disable_prefix", 7, BaseTileInfoProvider.SettingType.TEXT, 114, 50)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(this.string("P"), "gui.ic2.time_card.disable_prefix", 8, BaseTileInfoProvider.SettingType.TEXT, 114, 67)
      );
      return entries;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new CardInventory(player, this, stack, null, 5, this.createSettings()).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new CardInventory(player, this, stack, slot, 5, this.createSettings()).load(stack);
   }

   public Level getWorld(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return data.contains("world") ? IC2.PLATFORM.getWorld(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(data.getString("world")))) : null;
   }

   public static int getColor(ItemStack stack, int colorIndex, int defaultValue) {
      int[] colors = StackUtil.getNbtData(stack).getIntArray("colors");
      return colors.length <= colorIndex ? defaultValue : DyeColor.byId(colors[colorIndex]).getTextColor();
   }

   private MutableComponent getServerTime(boolean cut) {
      return cut
         ? this.string(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
         : this.translate("tooltip.item.ic2.time_card.server", new Object[]{LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))});
   }

   public Component withColor(ItemStack stack, int index, MutableComponent comp) {
      return comp.withStyle(ColorUtils.toColor(getColor(stack, index, -1)));
   }

   public Component getTileName(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return this.withColor(
         stack,
         0,
         data.contains("name")
            ? Serializer.fromJson(data.getString("name"))
            : (data.contains("base_name") ? Serializer.fromJson(data.getString("base_name")) : this.string("Error"))
      );
   }

   public int getFlags(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("flags");
   }

   public void storeDimension(ItemStack stack, Level world) {
      CompoundTag data = stack.getOrCreateTag();
      data.putString("world", world.dimension().location().toString());
      if (!data.contains("flags")) {
         data.putInt("flags", this.createDefaultFlags());
      }

      if (!data.contains("colors")) {
         data.putIntArray("colors", new int[5]);
      }

      data.putString("base_name", Serializer.toJson(this.string(SanityHelper.toPascalCase(world.dimension().location().getPath()))));
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

   public static class GameTimeProvider implements Supplier<Component> {
      Level world;
      int color;
      boolean cut;
      boolean dayTime;

      public GameTimeProvider(Level world, int color, boolean cut, boolean dayTime) {
         this.world = world;
         this.color = color;
         this.cut = cut;
         this.dayTime = dayTime;
      }

      public Component get() {
         if (this.dayTime) {
            return this.cut
               ? Component.literal(this.format()).withStyle(ColorUtils.toColor(this.color))
               : Component.translatable("tooltip.item.ic2.time_card.day", new Object[]{this.format()}).withStyle(ColorUtils.toColor(this.color));
         } else {
            return this.cut
               ? Component.literal(this.format()).withStyle(ColorUtils.toColor(this.color))
               : Component.translatable("tooltip.item.ic2.time_card.game", new Object[]{this.format()}).withStyle(ColorUtils.toColor(this.color));
         }
      }

      public String format() {
         return DurationFormatUtils.formatDuration(this.getTime(), this.dayTime ? "HH:mm" : "HH:mm:ss");
      }

      public long getTime() {
         return this.dayTime
            ? Duration.ofSeconds((long)((double)((this.world.getDayTime() + 6000L) % 24000L) / 24000.0 * 86400.0)).toMillis()
            : this.world.getGameTime() * 50L;
      }
   }
}
