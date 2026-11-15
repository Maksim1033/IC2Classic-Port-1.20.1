package ic2.core.item.tool.infos;

import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.ProgressDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyCardItem extends BaseTileInfoProvider implements IHasHeldSlotInventory {
   public static final int FLAG_SHOW_TEXT = 2;
   public static final int FLAG_SHOW_PROGRESS = 4;
   public static final int FLAG_SHOW_FULL_TEXT = 8;
   public static final int FLAG_SHOW_PERCENT = 16;

   public EnergyCardItem() {
      super("eu_card", "tools/monitor", "energy_card");
   }

   @Override
   protected boolean isValidTile(BlockEntity tile) {
      return tile instanceof IEUStorage;
   }

   @Override
   protected int createDefaultFlags() {
      return 15;
   }

   @Override
   protected int[] createDefaultColors() {
      return new int[]{0, 0, DyeColor.RED.getId()};
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      BlockEntity tile = this.getTileEntity(stack);
      if (tile instanceof IEUStorage) {
         int flags = this.getFlags(stack);
         BooleanSupplier alive = this.createAliveTester(tile);
         if ((flags & 1) != 0) {
            infos.accept(new StringDisplayInfo(new BaseTileInfoProvider.NameProvider(this.getTileName(stack)), alive));
         }

         if ((flags & 2) != 0) {
            infos.accept(new StringDisplayInfo(new EnergyCardItem.InfoProvider(flags, getColor(stack, 1, -1), (IEUStorage)tile), alive));
         }

         if ((flags & 4) != 0) {
            infos.accept(new ProgressDisplayInfo(() -> this.createProgress((IEUStorage)tile, getColor(stack, 2, -1)), alive));
         }
      } else {
         infos.accept(this.createTileError(stack, tile));
      }
   }

   @Override
   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      List<BaseTileInfoProvider.SettingEntry> entries = CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.WRITABLE_BOOK), "gui.ic2.cards.show_name", 0, BaseTileInfoProvider.SettingType.SIMPLE, 7, 50)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.BOOK), "gui.ic2.energy_card.energy", 1, BaseTileInfoProvider.SettingType.SIMPLE, 7, 67)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.POWERED_RAIL), "gui.ic2.energy_card.energy_progress", 2, BaseTileInfoProvider.SettingType.SIMPLE, 7, 84
         )
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 1, BaseTileInfoProvider.SettingType.COLOR, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 2, BaseTileInfoProvider.SettingType.COLOR, 24, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("F"), "gui.ic2.cards.full_text", 3, BaseTileInfoProvider.SettingType.TEXT, 41, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("%"), "gui.ic2.cards.percent", 4, BaseTileInfoProvider.SettingType.TEXT, 58, 67));
      return entries;
   }

   private static class InfoProvider implements Supplier<Component> {
      int flags;
      int color;
      IEUStorage storage;

      public InfoProvider(int flags, int color, IEUStorage storage) {
         this.flags = flags;
         this.color = color;
         this.storage = storage;
      }

      public Component get() {
         MutableComponent text = Component.translatable(
               (this.flags & 8) != 0 ? "misc.ic2.eu" : "misc.ic2.eu_data", new Object[]{this.storage.getStoredEU(), this.storage.getMaxEU()}
            )
            .withStyle(ColorUtils.toColor(this.color));
         return (this.flags & 16) == 0
            ? text
            : text.append(" (" + Mth.floor((double)this.storage.getStoredEU() / (double)this.storage.getMaxEU() * 100.0) + "%)");
      }
   }
}
