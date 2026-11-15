package ic2.probeplugin.info.transport;

import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.tubes.ColorFilterTubeTileEntity;
import ic2.core.block.transport.item.tubes.DirectionalTubeTileEntity;
import ic2.core.block.transport.item.tubes.ExtractionTubeTileEntity;
import ic2.core.block.transport.item.tubes.FilterTubeTileEntity;
import ic2.core.block.transport.item.tubes.FilteredExtractionTubeTileEntity;
import ic2.core.block.transport.item.tubes.InsertionTubeTileEntity;
import ic2.core.block.transport.item.tubes.LimiterTubeTileEntity;
import ic2.core.block.transport.item.tubes.PickupTubeTileEntity;
import ic2.core.block.transport.item.tubes.RequestTubeTileEntity;
import ic2.core.block.transport.item.tubes.RoundRobinTubeTileEntity;
import ic2.core.block.transport.item.tubes.StackingTubeTileEntity;
import ic2.core.block.transport.item.tubes.TeleportTubeTileEntity;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public class TubeProvider implements ITileInfoComponent<TubeTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, TubeTileEntity tile) {
      Panel panel = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      Panel inner = new Panel(IC2Styles.SLOTS_STYLE.copy().borderColor((Color)null), Panel.Type.VERTICAL);
      Map<Component, List<ItemStack>> slots = CollectionUtils.<Component, List<ItemStack>>createLinkedMap();
      IExpandedProbeInfo data = panel.vertical(IC2Styles.INNER_STYLE);
      if (!tile.synchronize) {
         data.text(this.translate("ic2.probe.tube.invisible").withStyle(ChatFormatting.RED));
      }

      int priority = tile.getPrioritySide();
      if (priority > 0) {
         data.text(
            this.translate("ic2.probe.tube.direction.prio", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(this.translate("misc.ic2.side." + Direction.from3DDataValue(priority - 1), new ChatFormatting[]{IC2Styles.getColor(priority - 1)}))
         );
      }

      if (tile instanceof DirectionalTubeTileEntity tube) {
         data.text(
            this.translate("ic2.probe.tube.direction", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate("misc.ic2.side." + tube.getFacing().getName(), new ChatFormatting[]{IC2Styles.getColor(tube.getFacing().get3DDataValue())})
               )
         );
         data.text(
            this.translate("ic2.probe.tube.blocked", new ChatFormatting[]{ChatFormatting.DARK_AQUA})
               .append(
                  this.translate(
                     tube.isBlocked() ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{tube.isBlocked() ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      if (tile instanceof ExtractionTubeTileEntity extraction) {
         data.text(
            this.translate("ic2.probe.tube.redstone.control", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     tile.sensitive ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{tile.sensitive ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
         if (tile.sensitive) {
            data.text(
               this.translate("ic2.probe.tube.redstone.comparator", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})
                  .append(
                     this.translate(
                        extraction.comparator ? "misc.ic2.yes" : "misc.ic2.no",
                        new ChatFormatting[]{extraction.comparator ? ChatFormatting.GREEN : ChatFormatting.RED}
                     )
                  )
            );
            data.text(
               this.translate("ic2.probe.tube.redstone.pulse", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})
                  .append(
                     this.translate(
                        extraction.pulse ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{extraction.pulse ? ChatFormatting.GREEN : ChatFormatting.RED}
                     )
                  )
            );
         }
      }

      if (tile instanceof FilteredExtractionTubeTileEntity extractionx) {
         data.text(
            this.translate("ic2.probe.tube.redstone.control", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     tile.sensitive ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{tile.sensitive ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
         if (tile.sensitive) {
            data.text(
               this.translate("ic2.probe.tube.redstone.comparator", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})
                  .append(
                     this.translate(
                        extractionx.comparator ? "misc.ic2.yes" : "misc.ic2.no",
                        new ChatFormatting[]{extractionx.comparator ? ChatFormatting.GREEN : ChatFormatting.RED}
                     )
                  )
            );
            data.text(
               this.translate("ic2.probe.tube.redstone.pulse", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})
                  .append(
                     this.translate(
                        extractionx.pulse ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{extractionx.pulse ? ChatFormatting.GREEN : ChatFormatting.RED}
                     )
                  )
            );
         }

         data.text(
            this.translate("ic2.probe.tube.redstone.comparator", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})
               .append(
                  this.translate(
                     extractionx.comparator ? "misc.ic2.yes" : "misc.ic2.no",
                     new ChatFormatting[]{extractionx.comparator ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
         data.padding(0, 5)
            .text(
               this.translate("ic2.probe.tube.filter.whitelist", new ChatFormatting[]{ChatFormatting.GOLD})
                  .append(
                     this.translate(
                        extractionx.whitelist ? "misc.ic2.yes" : "misc.ic2.no",
                        new ChatFormatting[]{extractionx.whitelist ? ChatFormatting.GREEN : ChatFormatting.RED}
                     )
                  )
            );
         data.padding(0, -5);
         Long2ObjectMap<List<FilteredExtractionTubeTileEntity.FilterEntry>> mappedFilters = new Long2ObjectAVLTreeMap();
         ObjectListIterator m = extractionx.filters.iterator();

         while (m.hasNext()) {
            FilteredExtractionTubeTileEntity.FilterEntry entry = (FilteredExtractionTubeTileEntity.FilterEntry)m.next();
            ((List)mappedFilters.computeIfAbsent(
                  MathUtils.putIntoLong((long)(entry.getFlags() & -5), (long)entry.getKeepItems()), T -> CollectionUtils.createList()
               ))
               .add(entry);
         }

         ObjectIterator var47 = mappedFilters.long2ObjectEntrySet().iterator();

         while (var47.hasNext()) {
            Entry<List<FilteredExtractionTubeTileEntity.FilterEntry>> entry = (Entry<List<FilteredExtractionTubeTileEntity.FilterEntry>>)var47.next();
            long infoData = entry.getLongKey();
            int flags = MathUtils.getIntKey(infoData);
            int keep = MathUtils.getIntValue(infoData);
            MutableComponent text = this.string().withStyle(ChatFormatting.GOLD);
            text.append(this.translate(flags == 0 ? "ic2.probe.tube.filter.basic" : "ic2.probe.tube.filter.adv"));
            if ((flags & 16) != 0) {
               text.append(this.translate("ic2.probe.tube.filter.nbt"));
            }

            if ((flags & 128) != 0) {
               text.append(this.translate("ic2.probe.tube.filter.fluid"));
            }

            if ((flags & 256) != 0) {
               text.append(this.translate("ic2.probe.tube.filter.durability"));
            }

            if (keep > 0) {
               text.append(this.translate("ic2.probe.tube.filter.keep", new Object[]{keep}));
            }

            List<ItemStack> result = CollectionUtils.<ItemStack>createList();

            for (FilteredExtractionTubeTileEntity.FilterEntry filterEntry : entry.getValue()) {
               result.add(filterEntry.getStack());
            }

            slots.put(text, result);
         }
      }

      if (tile instanceof StackingTubeTileEntity stacking) {
         data.text(
            this.translate("ic2.probe.tube.stacking.color", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     stacking.ignoreColors ? "misc.ic2.yes" : "misc.ic2.no",
                     new ChatFormatting[]{stacking.ignoreColors ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
         data.text(this.translate("ic2.probe.tube.stacking.limit", ChatFormatting.GOLD, new Object[]{"" + ChatFormatting.AQUA + stacking.stacking}));
      }

      if (tile instanceof PickupTubeTileEntity pickup) {
         data.text(
            this.translate("ic2.probe.tube.pickup.large", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     pickup.largeRadius ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{pickup.largeRadius ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      if (tile instanceof FilterTubeTileEntity filter) {
         Map<Direction, List<ItemStack>> filters = new EnumMap<>(Direction.class);
         ObjectListIterator var48 = filter.stacks.iterator();

         while (var48.hasNext()) {
            FilterTubeTileEntity.FilterEntry entryx = (FilterTubeTileEntity.FilterEntry)var48.next();

            for (Direction side : entryx.getSides()) {
               filters.computeIfAbsent(side, T -> CollectionUtils.createList()).add(entryx.getStack());
            }
         }

         for (java.util.Map.Entry<Direction, List<ItemStack>> entryx : filters.entrySet()) {
            slots.put(DirectionList.getName(entryx.getKey()).withStyle(IC2Styles.getColor(entryx.getKey().get3DDataValue())).append(": "), entryx.getValue());
         }

         data.text(
            this.translate("ic2.probe.tube.inv_priority", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     filter.invPriority ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{filter.invPriority ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      if (tile instanceof ColorFilterTubeTileEntity filter) {
         Map<Direction, List<ItemStack>> filters = new EnumMap<>(Direction.class);

         for (int i = 0; i < 16; i++) {
            ItemStack stack = new ItemStack(DyeItem.byColor(DyeColor.byId(i)));

            for (Direction side : DirectionList.ofNumber(filter.colorDirections[i])) {
               filters.computeIfAbsent(side, T -> CollectionUtils.createList()).add(stack);
            }
         }

         for (java.util.Map.Entry<Direction, List<ItemStack>> entryx : filters.entrySet()) {
            slots.put(DirectionList.getName(entryx.getKey()).withStyle(IC2Styles.getColor(entryx.getKey().get3DDataValue())).append(": "), entryx.getValue());
         }

         data.text(
            this.translate("ic2.probe.tube.inv_priority", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     filter.invPriority ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{filter.invPriority ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      if (tile instanceof RoundRobinTubeTileEntity robin) {
         data.text(this.translate("ic2.probe.tube.robin.rules", new ChatFormatting[]{ChatFormatting.GOLD}));
         data.text(
            this.translate(
               "ic2.probe.tube.robin.side",
               ChatFormatting.GOLD,
               new Object[]{DirectionList.getName(Direction.from3DDataValue(robin.currentIndex)).withStyle(IC2Styles.getColor(robin.currentIndex))}
            )
         );
         data.text(this.translate("ic2.probe.tube.robin.count", ChatFormatting.GOLD, new Object[]{"" + ChatFormatting.AQUA + Math.max(0, robin.currentItem)}));

         for (Direction side : DirectionList.ALL) {
            data.text(DirectionList.getName(side).append(": " + robin.cap[side.ordinal()]).withStyle(IC2Styles.getColor(side.get3DDataValue())));
         }
      }

      if (tile instanceof TeleportTubeTileEntity teleport) {
         int filter = teleport.state;
         boolean isPrivate = (filter & 4) != 0;
         boolean canReceive = (filter & 2) != 0;
         boolean canSend = (filter & 1) != 0;
         data.text(this.translate("ic2.probe.tube.teleport.network", ChatFormatting.GOLD, new Object[]{ChatFormatting.AQUA + teleport.frequency}));
         data.text(
            this.translate("ic2.probe.tube.teleport.private", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(this.translate(isPrivate ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{isPrivate ? ChatFormatting.GREEN : ChatFormatting.RED}))
         );
         data.text(
            this.translate("ic2.probe.tube.teleport.send", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(this.translate(canSend ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{canSend ? ChatFormatting.GREEN : ChatFormatting.RED}))
         );
         data.text(
            this.translate("ic2.probe.tube.teleport.receive", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(canReceive ? "misc.ic2.yes" : "misc.ic2.no", new ChatFormatting[]{canReceive ? ChatFormatting.GREEN : ChatFormatting.RED})
               )
         );
      }

      if (!tile.items.isEmpty()) {
         Object2IntLinkedOpenCustomHashMap<ItemStack> mapped = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.INSTANCE);
         int i = 0;

         for (int m = tile.items.size(); i < m; i++) {
            TransportedItem item = (TransportedItem)tile.items.get(i);
            mapped.addTo(StackUtil.copyWithSize(item.getServerStack(), 1), item.getServerStack().getCount());
         }

         NonNullList<ItemStack> list = NonNullList.create();
         mapped.forEach((K, V) -> list.add(StackUtil.copyWithSize(K, V)));
         slots.put(this.translate("ic2.probe.tube.transported"), list);
      }

      if (tile instanceof StackingTubeTileEntity stackingTile && !stackingTile.cached.isEmpty()) {
         NonNullList<ItemStack> cached = NonNullList.create();
         ObjectListIterator var55 = stackingTile.cached.iterator();

         while (var55.hasNext()) {
            StackingTubeTileEntity.StackingStack item = (StackingTubeTileEntity.StackingStack)var55.next();
            cached.add(StackUtil.copyWithSize(item.getStack(), item.getAmount()));
         }

         slots.put(this.translate("ic2.probe.tube.cached"), cached);
      }

      if (tile instanceof LimiterTubeTileEntity limiter) {
         List<ItemStack> list = CollectionUtils.<ItemStack>createList();
         ObjectListIterator var56 = limiter.usedColors.iterator();

         while (var56.hasNext()) {
            DyeColor color = (DyeColor)var56.next();
            list.add(new ItemStack(DyeItem.byColor(color)));
         }

         slots.put(this.translate("ic2.probe.tube.limiter.unblocked"), list);
      }

      if (tile instanceof InsertionTubeTileEntity insertion) {
         data.text(
            this.translate("ic2.probe.tube.inserter.existing", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     insertion.onlyExistingOne ? "misc.ic2.yes" : "misc.ic2.no",
                     new ChatFormatting[]{insertion.onlyExistingOne ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      if (tile instanceof RequestTubeTileEntity request) {
         NonNullList<ItemStack> list = NonNullList.create();
         ObjectListIterator var57 = request.filters.iterator();

         while (var57.hasNext()) {
            RequestTubeTileEntity.RequestEntry entryx = (RequestTubeTileEntity.RequestEntry)var57.next();
            list.add(StackUtil.copyWithSize(entryx.getStack(), entryx.getAmount()));
         }

         if (!list.isEmpty()) {
            slots.put(this.translate("ic2.probe.tube.requests"), list);
         }

         list = NonNullList.create();
         request.getMissing(list);
         if (!list.isEmpty()) {
            slots.put(this.translate("ic2.probe.tube.missing"), list);
         }

         list = NonNullList.create();
         ObjectIterator var58 = Object2IntMaps.fastIterable(request.requested).iterator();

         while (var58.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack> entryx = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>)var58.next();
            list.add(StackUtil.copyWithSize((ItemStack)entryx.getKey(), entryx.getIntValue()));
         }

         if (!list.isEmpty()) {
            slots.put(this.translate("ic2.probe.tube.requesting"), list);
         }

         if (!request.toInsert.isEmpty()) {
            list = NonNullList.create();

            for (ItemStack stack : request.toInsert) {
               list.add(stack.copy());
            }

            slots.put(this.translate("ic2.probe.tube.stuck"), list);
         }

         data.text(
            this.translate("ic2.probe.tube.redstone.control", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(
                  this.translate(
                     request.redstoneRequest ? "misc.ic2.yes" : "misc.ic2.no",
                     new ChatFormatting[]{request.redstoneRequest ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               )
         );
      }

      ProbePluginHelper.generateSlotInfo(slots, inner);
      panel.element(inner);
      this.addSecurely(info, 1, panel);
   }
}
