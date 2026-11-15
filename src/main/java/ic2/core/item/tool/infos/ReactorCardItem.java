package ic2.core.item.tool.infos;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.reactor.IChamberReactor;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.ISteamReactor;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.ProgressDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.api.tiles.readers.IActivityProvider;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.inv.inventory.ReactorCardInventory;
import ic2.core.item.reactor.ReactorUraniumRod;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ReactorCardItem extends BaseTileInfoProvider implements IHasHeldSlotInventory, INetworkItemEvent, INetworkItemBufferEvent<StringDataBuffer> {
   public static final int FLAG_SHOW_HEAT = 2;
   public static final int FLAG_SHOW_HEAT_PROGRESS = 4;
   public static final int FLAG_SHOW_STATE = 8;
   public static final int FLAG_SHOW_PRODUCTION = 16;
   public static final int FLAG_SHOW_RUNTIME = 32;
   public static final int FLAG_SHOW_RUNTIME_PROGRESS = 64;
   public static final int FLAG_SHOW_TIME_LEFT = 128;
   public static final int FLAG_SHOW_PART_HEALTH = 256;
   public static final int FLAG_SHOW_FULL_TEXT = 512;
   public static final int FLAG_SHOW_RUNTIME_PERCENT = 1024;

   public ReactorCardItem() {
      super("reactor_card", "tools/monitor", "reactor_card");
   }

   @Override
   protected int createDefaultFlags() {
      return 1023;
   }

   @Override
   protected int[] createDefaultColors() {
      int[] colors = new int[13];
      colors[2] = DyeColor.LIME.getId();
      colors[9] = DyeColor.YELLOW.getId();
      colors[10] = DyeColor.RED.getId();
      colors[11] = DyeColor.LIME.getId();
      colors[12] = DyeColor.RED.getId();
      return colors;
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      BlockEntity tile = this.getTileEntity(stack);
      IReactor reactor = tile instanceof IReactor ? (IReactor)tile : (tile instanceof IReactorChamber ? ((IReactorChamber)tile).getReactor() : null);
      if (reactor != null) {
         int flags = this.getFlags(stack);
         BooleanSupplier alive = this.createAliveTester(reactor instanceof BlockEntity ? (BlockEntity)reactor : tile);
         if ((flags & 1) != 0) {
            infos.accept(new StringDisplayInfo(new BaseTileInfoProvider.NameProvider(this.getTileName(stack)), alive));
         }

         if ((flags & 2) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 1, createHeatText(stack, reactor, flags)), alive));
         }

         if ((flags & 4) != 0) {
            infos.accept(new ProgressDisplayInfo(() -> createHeatProgress(stack, reactor), alive));
         }

         if ((flags & 8) != 0 && reactor instanceof IActivityProvider) {
            infos.accept(
               new StringDisplayInfo(
                  new ActivityCardItem.InfoProvider(
                     (IActivityProvider)reactor, getColor(stack, 11, ColorUtils.GREEN), getColor(stack, 12, ColorUtils.RED), getColor(stack, 3, -1)
                  ),
                  alive
               )
            );
         }

         if ((flags & 16) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 4, getProduction(reactor)), alive));
         }

         if ((flags & 32) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 5, getRuntime(reactor, flags)), alive));
         }

         if ((flags & 64) != 0) {
            infos.accept(new ProgressDisplayInfo(() -> getRuntime(reactor, stack), alive));
         }

         if ((flags & 128) != 0) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 7, getUraniumLeft(reactor)), alive));
         }

         if ((flags & 256) != 0 && reactor instanceof IChamberReactor) {
            infos.accept(new StringDisplayInfo(() -> this.withColor(stack, 8, getComponentHeat((IChamberReactor)reactor)), alive));
         }
      } else {
         infos.accept(this.createTileError(stack, tile));
      }
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new ReactorCardInventory(player, this, stack, null, this.createDefaultColors().length, this.createSettings()).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new ReactorCardInventory(player, this, stack, slot, this.createDefaultColors().length, this.createSettings()).load(stack);
   }

   @Override
   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      List<BaseTileInfoProvider.SettingEntry> entries = CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.WRITABLE_BOOK), "gui.ic2.cards.show_name", 0, BaseTileInfoProvider.SettingType.SIMPLE, 7, 50)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.BOOK), "gui.ic2.reactor_card.show_heat", 1, BaseTileInfoProvider.SettingType.SIMPLE, 7, 67)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.POWERED_RAIL), "gui.ic2.reactor_card.show_heat_progress", 2, BaseTileInfoProvider.SettingType.SIMPLE, 7, 84
         )
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.REDSTONE_TORCH), "gui.ic2.cards.show_state", 3, BaseTileInfoProvider.SettingType.SIMPLE, 7, 101
         )
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(IC2Items.RE_BATTERY), "gui.ic2.reactor_card.production", 4, BaseTileInfoProvider.SettingType.SIMPLE, 80, 50
         )
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.CLOCK), "gui.ic2.reactor_card.runtime", 5, BaseTileInfoProvider.SettingType.SIMPLE, 80, 67)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.ACTIVATOR_RAIL), "gui.ic2.reactor_card.runtime_progress", 6, BaseTileInfoProvider.SettingType.SIMPLE, 80, 84
         )
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.CLOCK), "gui.ic2.reactor_card.left", 7, BaseTileInfoProvider.SettingType.SIMPLE, 80, 101)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(IC2Items.RUBBER_BOAT_BROKEN), "gui.ic2.reactor_card.durability", 8, BaseTileInfoProvider.SettingType.SIMPLE, 80, 118
         )
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("F"), "gui.ic2.cards.full_text", 9, BaseTileInfoProvider.SettingType.TEXT, 41, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("%"), "gui.ic2.cards.percent", 10, BaseTileInfoProvider.SettingType.TEXT, 114, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 1, BaseTileInfoProvider.SettingType.COLOR, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.reactor_card.heat_low_color", 2, BaseTileInfoProvider.SettingType.COLOR, 24, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.reactor_card.heat_medium_color", 9, BaseTileInfoProvider.SettingType.COLOR, 41, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.reactor_card.heat_high_color", 10, BaseTileInfoProvider.SettingType.COLOR, 58, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 3, BaseTileInfoProvider.SettingType.COLOR, 24, 101));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.active", 11, BaseTileInfoProvider.SettingType.COLOR, 41, 101));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.inactive", 12, BaseTileInfoProvider.SettingType.COLOR, 58, 101));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 4, BaseTileInfoProvider.SettingType.COLOR, 97, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 5, BaseTileInfoProvider.SettingType.COLOR, 97, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 6, BaseTileInfoProvider.SettingType.COLOR, 97, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 7, BaseTileInfoProvider.SettingType.COLOR, 97, 101));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 8, BaseTileInfoProvider.SettingType.COLOR, 97, 118));
      return entries;
   }

   public static int getMaxHeat(ItemStack stack, IReactor reactor) {
      return NBTUtils.getInt(StackUtil.getNbtData(stack), "max_heat", reactor.getMaxHeat());
   }

   @Override
   protected boolean isValidTile(BlockEntity tile) {
      return tile instanceof IReactor || tile instanceof IReactorChamber;
   }

   private static MutableComponent getRuntime(IReactor reactor, int flags) {
      boolean percent = (flags & 1024) != 0;
      ItemStack stack = reactor instanceof IChamberReactor ? getUraniumRod((IChamberReactor)reactor, percent) : ItemStack.EMPTY;
      return stack.isEmpty()
         ? Component.translatable("tooltip.item.ic2.reactor_card.runtime.no_uranium")
         : Component.translatable(
            "tooltip.item.ic2.reactor_card.runtime.time",
            new Object[]{
               percent
                  ? Mth.floor((double)stack.getDamageValue() / (double)stack.getMaxDamage() * 100.0) + "%"
                  : DurationFormatUtils.formatDuration((long)stack.getDamageValue() * 1000L, "HH:mm:ss")
            }
         );
   }

   public static ProgressDisplayInfo.Progress getRuntime(IReactor reactor, ItemStack stack) {
      int color = getColor(stack, 6, -1);
      ItemStack rod = reactor instanceof IChamberReactor ? getUraniumRod((IChamberReactor)reactor, true) : ItemStack.EMPTY;
      return new ProgressDisplayInfo.Progress((double)rod.getDamageValue() / (double)rod.getMaxDamage(), color, ColorUtils.darker(color));
   }

   private static MutableComponent getUraniumLeft(IReactor reactor) {
      ItemStack stack = reactor instanceof IChamberReactor ? getUraniumRod((IChamberReactor)reactor, true) : ItemStack.EMPTY;
      return stack.isEmpty()
         ? Component.translatable("tooltip.item.ic2.reactor_card.time_left.no_uranium")
         : Component.translatable(
            "tooltip.item.ic2.reactor_card.time_left.time",
            new Object[]{DurationFormatUtils.formatDuration((long)(stack.getMaxDamage() - stack.getDamageValue()) * 1000L, "HH:mm:ss")}
         );
   }

   private static MutableComponent getProduction(IReactor reactor) {
      if (reactor instanceof ISteamReactor) {
         return Component.translatable("tooltip.item.ic2.reactor_card.production.fluid", new Object[]{(int)(reactor.getEnergyOutput() * 3.200000047683716)});
      } else {
         return reactor instanceof IEnergySource
            ? Component.translatable("tooltip.item.ic2.reactor_card.production.energy", new Object[]{((IEnergySource)reactor).getProvidedEnergy()})
            : Component.translatable("tooltip.item.ic2.reactor_card.production.none");
      }
   }

   private static MutableComponent createHeatText(ItemStack stack, IReactor reactor, int flags) {
      int heat = reactor.getHeat();
      return (flags & 512) != 0
         ? Component.translatable("tooltip.item.ic2.reactor_card.heat.full", new Object[]{heat, getMaxHeat(stack, reactor)})
         : Component.translatable("tooltip.item.ic2.reactor_card.heat.simple", new Object[]{heat});
   }

   private static ProgressDisplayInfo.Progress createHeatProgress(ItemStack stack, IReactor reactor) {
      double progress = (double)reactor.getHeat() / (double)getMaxHeat(stack, reactor);
      int baseColor = progress < 0.33
         ? getColor(stack, 2, ColorUtils.GREEN)
         : (progress < 0.66 ? getColor(stack, 9, ColorUtils.YELLOW) : getColor(stack, 10, ColorUtils.RED));
      return new ProgressDisplayInfo.Progress(progress, baseColor, ColorUtils.darker(baseColor));
   }

   private static MutableComponent getComponentHeat(IChamberReactor reactor) {
      double value = 0.0;
      int total = 0;
      int x = 0;

      for (int m = reactor.getWidth(); x < m; x++) {
         int y = 0;

         for (int n = reactor.getHeight(); y < n; y++) {
            ItemStack stack = reactor.getStackInReactor(x, y);
            Item max = stack.getItem();
            if (max instanceof IReactorComponent) {
               IReactorComponent comp = (IReactorComponent)max;
               double maxx = (double)comp.getMaxStoredHeat(stack, reactor, x, y);
               if (maxx > 0.0) {
                  value += (double)comp.getStoredHeat(stack, reactor, x, y) / maxx;
                  total++;
               }
            }
         }
      }

      return Component.translatable(
         "tooltip.item.ic2.reactor_card.part_health", new Object[]{Mth.floor(total > 0 ? 100.0 - value / (double)total * 100.0 : 100.0)}
      );
   }

   private static ItemStack getUraniumRod(IChamberReactor reactor, boolean findLargest) {
      int durabilityUsed = findLargest ? 2147483647 : 0;
      int maxDurability = 0;
      ItemStack result = ItemStack.EMPTY;
      int x = 0;

      for (int m = reactor.getWidth(); x < m; x++) {
         int y = 0;

         for (int n = reactor.getHeight(); y < n; y++) {
            ItemStack stack = reactor.getStackInReactor(x, y);
            if (!stack.isEmpty()
               && (
                  stack.getItem() instanceof ReactorUraniumRod
                     || stack.getItem() instanceof IReactorPlannerComponent
                        && ((IReactorPlannerComponent)stack.getItem()).getType(stack) == IReactorPlannerComponent.ComponentType.FUEL_ROD
               )) {
               int damage = stack.getDamageValue();
               if (findLargest) {
                  int maxDamage = stack.getMaxDamage();
                  if (maxDamage >= maxDurability && damage < durabilityUsed) {
                     result = stack;
                     durabilityUsed = damage;
                  }
               } else if (damage > durabilityUsed) {
                  result = stack;
                  durabilityUsed = damage;
               }
            }
         }
      }

      return result;
   }
}
