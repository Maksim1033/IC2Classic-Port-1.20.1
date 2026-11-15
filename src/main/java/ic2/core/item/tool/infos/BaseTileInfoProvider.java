package ic2.core.item.tool.infos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import ic2.api.items.IDisplayProvider;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.ProgressDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.tiles.readers.IFuelStorage;
import ic2.api.tiles.readers.IProgressMachine;
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
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseTileInfoProvider
   extends IC2SimpleItem
   implements IDisplayProvider,
   IItemModel,
   INetworkItemEvent,
   INetworkItemBufferEvent<StringDataBuffer>,
   IHasHeldSlotInventory {
   public static final int FLAG_SHOW_NAME = 1;
   String inactiveTexture;

   public BaseTileInfoProvider(String itemName, String textureFolder, String textureName) {
      this(itemName, new PropertiesBuilder().maxStackSize(1), textureFolder, textureName);
   }

   public BaseTileInfoProvider(String itemName, PropertiesBuilder properties, String textureFolder, String textureName) {
      super(itemName, properties, textureFolder, "active_" + textureName);
      this.inactiveTexture = "inactive_" + textureName;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.card.init", new Object[0]));
   }

   protected abstract boolean isValidTile(BlockEntity var1);

   protected abstract int createDefaultFlags();

   protected int[] createDefaultColors() {
      return new int[0];
   }

   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      return CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
   }

   private boolean isInvalid(BlockEntity tile) {
      return !this.isValidTile(tile);
   }

   protected IDisplayInfo createTileError(ItemStack stack, BlockEntity tile) {
      if (tile == null) {
         return this.isValidCardData(stack)
            ? new StringDisplayInfo(
               () -> this.translate("tooltip.item.ic2.base_card.tile_not_found"), new BaseTileInfoProvider.ErrorTester(stack, Objects::isNull)
            )
            : new StringDisplayInfo(() -> this.translate("tooltip.item.ic2.base_card.invalid_data"), () -> true);
      } else {
         return new StringDisplayInfo(
            () -> this.translate("tooltip.item.ic2.base_card.invalid_tile"), new BaseTileInfoProvider.ErrorTester(stack, this::isInvalid)
         );
      }
   }

   protected BooleanSupplier createAliveTester(BlockEntity tile) {
      return new BaseTileInfoProvider.AliveTester(tile);
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

   public InteractionResult useOn(UseOnContext context) {
      if (context.isSecondaryUseActive()) {
         BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
         if (tile != null && this.isValidTile(tile)) {
            this.storeTileEntity(context.getItemInHand(), tile);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new CardInventory(player, this, stack, null, this.createDefaultColors().length, this.createSettings()).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new CardInventory(player, this, stack, slot, this.createDefaultColors().length, this.createSettings()).load(stack);
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return StackUtil.getNbtData(stack).getLong("pos") != 0L ? 1 : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.textureFolder).get(this.inactiveTexture);
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

   public void storeFlags(ItemStack stack, int flags) {
      stack.getOrCreateTag().putInt("flags", flags);
   }

   public void storeTileEntity(ItemStack stack, BlockEntity tile) {
      CompoundTag data = stack.getOrCreateTag();
      data.putString("world", tile.getLevel().dimension().location().toString());
      data.putLong("pos", tile.getBlockPos().asLong());
      if (!data.contains("flags")) {
         data.putInt("flags", this.createDefaultFlags());
      }

      if (!data.contains("colors")) {
         data.putIntArray("colors", this.createDefaultColors());
      }

      if (tile instanceof Nameable name && name.hasCustomName()) {
         data.putString("base_name", Serializer.toJson(name.getCustomName()));
         return;
      }

      data.putString("base_name", Serializer.toJson(Component.translatable(tile.getBlockState().getBlock().getDescriptionId())));
   }

   public static int getColor(ItemStack stack, int colorIndex, int defaultValue) {
      int[] colors = StackUtil.getNbtData(stack).getIntArray("colors");
      return colors.length <= colorIndex ? defaultValue : DyeColor.byId(colors[colorIndex]).getTextColor() | 0xFF000000;
   }

   public Component withColor(ItemStack stack, int index, MutableComponent comp) {
      return comp.withStyle(ColorUtils.toColor(getColor(stack, index, -1)));
   }

   public int getFlags(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("flags");
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

   public BlockEntity getTileEntity(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return data.contains("world") && data.contains("pos")
         ? IC2.PLATFORM
            .getTileEntity(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(data.getString("world"))), BlockPos.of(data.getLong("pos")), true)
         : null;
   }

   public <T> T getTileEntity(ItemStack stack, Class<T> clz) {
      BlockEntity tile = this.getTileEntity(stack);
      return clz.isInstance(tile) ? clz.cast(tile) : null;
   }

   public ProgressDisplayInfo.Progress createProgress(IEUStorage machine, int color) {
      return this.createProgress(machine, color, ColorUtils.darker(color));
   }

   public ProgressDisplayInfo.Progress createProgress(IEUStorage machine, int mainColor, int subColor) {
      return new ProgressDisplayInfo.Progress((double)machine.getStoredEU() / (double)machine.getMaxEU(), mainColor, subColor);
   }

   public ProgressDisplayInfo.Progress createProgress(IFuelStorage machine, int color) {
      return this.createProgress(machine, color, ColorUtils.darker(color));
   }

   public ProgressDisplayInfo.Progress createProgress(IFuelStorage storage, int mainColor, int subColor) {
      return new ProgressDisplayInfo.Progress((double)storage.getFuel() / (double)storage.getMaxFuel(), mainColor, subColor);
   }

   public ProgressDisplayInfo.Progress createProgress(IProgressMachine machine, int color) {
      return this.createProgress(machine, color, ColorUtils.darker(color));
   }

   public ProgressDisplayInfo.Progress createProgress(IProgressMachine machine, int mainColor, int subColor) {
      return new ProgressDisplayInfo.Progress((double)machine.getProgress() / (double)machine.getMaxProgress(), mainColor, subColor);
   }

   private boolean isValidCardData(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return data.contains("world") && data.contains("pos");
   }

   private static class AliveTester implements BooleanSupplier {
      BlockEntity tile;
      boolean cache = false;
      long lastTime = -1L;

      public AliveTester(BlockEntity tile) {
         this.tile = tile;
      }

      @Override
      public boolean getAsBoolean() {
         long time = this.tile.getLevel().getGameTime();
         if (this.lastTime != time) {
            this.lastTime = time;
            this.cache = !this.tile.isRemoved() && this.tile.getLevel().hasChunk(this.tile.getBlockPos().getX() >> 4, this.tile.getBlockPos().getZ() >> 4);
         }

         return this.cache;
      }
   }

   private static class ErrorTester implements BooleanSupplier {
      BlockPos pos;
      ResourceKey<Level> world;
      Predicate<BlockEntity> tester;

      public ErrorTester(ItemStack stack, Predicate<BlockEntity> tester) {
         CompoundTag data = StackUtil.getNbtData(stack);
         this.pos = BlockPos.of(data.getLong("pos"));
         this.world = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(data.getString("world")));
         this.tester = tester;
      }

      @Override
      public boolean getAsBoolean() {
         return this.tester.test(IC2.PLATFORM.getTileEntity(this.world, this.pos, true));
      }
   }

   public static class NameProvider implements Supplier<Component> {
      Component comp;

      public NameProvider(Component comp) {
         this.comp = comp;
      }

      public Component get() {
         return this.comp;
      }
   }

   public static class SettingEntry {
      public final Object display;
      public final Component tooltip;
      public final int index;
      public final BaseTileInfoProvider.SettingType type;
      public final int x;
      public final int y;

      public SettingEntry(Object display, String tooltip, int index, BaseTileInfoProvider.SettingType type, int x, int y) {
         this.display = display;
         this.tooltip = Component.translatable(tooltip);
         this.index = index;
         this.type = type;
         this.x = x;
         this.y = y;
      }
   }

   public static enum SettingType {
      SIMPLE,
      COLOR,
      TEXT;
   }
}
