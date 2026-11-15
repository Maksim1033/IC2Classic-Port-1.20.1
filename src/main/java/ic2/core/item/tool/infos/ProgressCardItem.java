package ic2.core.item.tool.infos;

import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.ProgressDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.tiles.readers.ReaderProvider;
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

public class ProgressCardItem extends BaseTileInfoProvider implements IHasHeldSlotInventory {
   public static final int FLAG_SHOW_TEXT = 2;
   public static final int FLAG_SHOW_PROGRESS = 4;
   public static final int FLAG_SHOW_PERCENT = 8;

   public ProgressCardItem() {
      super("progress_card", "tools/monitor", "progress_card");
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      BlockEntity tile = this.getTileEntity(stack);
      IProgressMachine progress = ReaderProvider.getProgressProvider(tile);
      if (progress != null) {
         int flags = this.getFlags(stack);
         BooleanSupplier alive = this.createAliveTester(tile);
         if ((flags & 1) != 0) {
            infos.accept(new StringDisplayInfo(new BaseTileInfoProvider.NameProvider(this.getTileName(stack)), alive));
         }

         if ((flags & 2) != 0) {
            infos.accept(new StringDisplayInfo(new ProgressCardItem.InfoProvider(flags, getColor(stack, 1, -1), progress), alive));
         }

         if ((flags & 4) != 0) {
            infos.accept(new ProgressDisplayInfo(() -> this.createProgress(progress, getColor(stack, 2, ColorUtils.BLUE)), alive));
         }
      } else {
         infos.accept(this.createTileError(stack, tile));
      }
   }

   @Override
   protected boolean isValidTile(BlockEntity tile) {
      return ReaderProvider.getProgressProvider(tile) != null;
   }

   @Override
   protected int createDefaultFlags() {
      return 7;
   }

   @Override
   protected int[] createDefaultColors() {
      return new int[]{0, 0, DyeColor.LIGHT_BLUE.getId()};
   }

   @Override
   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      List<BaseTileInfoProvider.SettingEntry> entries = CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.WRITABLE_BOOK), "gui.ic2.cards.show_name", 0, BaseTileInfoProvider.SettingType.SIMPLE, 7, 50)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.BOOK), "gui.ic2.progress_card.progress", 1, BaseTileInfoProvider.SettingType.SIMPLE, 7, 67)
      );
      entries.add(
         new BaseTileInfoProvider.SettingEntry(
            new ItemStack(Items.POWERED_RAIL), "gui.ic2.progress_card.progress_progress", 2, BaseTileInfoProvider.SettingType.SIMPLE, 7, 84
         )
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 1, BaseTileInfoProvider.SettingType.COLOR, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 2, BaseTileInfoProvider.SettingType.COLOR, 24, 84));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("%"), "gui.ic2.cards.percent", 3, BaseTileInfoProvider.SettingType.TEXT, 41, 67));
      return entries;
   }

   private static class InfoProvider implements Supplier<Component> {
      int flags;
      int color;
      IProgressMachine storage;

      public InfoProvider(int flags, int color, IProgressMachine storage) {
         this.flags = flags;
         this.color = color;
         this.storage = storage;
      }

      public Component get() {
         MutableComponent text = Component.translatable(
               "tooltip.item.ic2.energy_card.progress", new Object[]{(int)this.storage.getProgress(), (int)this.storage.getMaxProgress()}
            )
            .withStyle(ColorUtils.toColor(this.color));
         return (this.flags & 8) == 0
            ? text
            : text.append(" " + Mth.floor((double)this.storage.getProgress() / (double)this.storage.getMaxProgress() * 100.0) + "%");
      }
   }
}
