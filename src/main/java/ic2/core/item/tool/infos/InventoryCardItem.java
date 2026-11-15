package ic2.core.item.tool.infos;

import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.impl.StringDisplayInfo;
import ic2.core.inventory.inv.HandlerInventory;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.tool.infos.components.InventoryDisplayInfo;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class InventoryCardItem extends BaseTileInfoProvider {
   public static final int FLAG_SHOW_COUNT = 2;
   public static final int FLAG_SHOW_COUNT_SIDE = 4;
   public static final int FLAG_SHOW_ITEM_NAME = 8;
   public static final int FLAG_MERGE_INV = 16;

   public InventoryCardItem() {
      super("inv_card", new PropertiesBuilder().maxStackSize(1), "tools/monitor", "inv_card");
   }

   @Override
   public void provideInfo(ItemStack stack, Consumer<IDisplayInfo> infos) {
      BlockEntity tile = this.getTileEntity(stack);
      if (tile != null) {
         IItemHandler handler = (IItemHandler)tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(EmptyHandler.INSTANCE);
         if (handler.getSlots() > 0) {
            BooleanSupplier alive = this.createAliveTester(tile);
            if ((this.getFlags(stack) & 1) != 0) {
               infos.accept(new StringDisplayInfo(new BaseTileInfoProvider.NameProvider(this.getTileName(stack)), alive));
            }

            infos.accept(new InventoryDisplayInfo(() -> new HandlerInventory(handler), () -> this.create(this.getFlags(stack)), alive));
            return;
         }
      }

      infos.accept(this.createTileError(stack, null));
   }

   private InventoryDisplayInfo.InvSettings create(int flags) {
      return new InventoryDisplayInfo.InvSettings((flags & 2) != 0, (flags & 4) != 0, (flags & 8) != 0, (flags & 16) != 0);
   }

   @Override
   protected boolean isValidTile(BlockEntity tile) {
      return tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
   }

   @Override
   protected List<BaseTileInfoProvider.SettingEntry> createSettings() {
      List<BaseTileInfoProvider.SettingEntry> entries = CollectionUtils.<BaseTileInfoProvider.SettingEntry>createList();
      entries.add(
         new BaseTileInfoProvider.SettingEntry(new ItemStack(Items.WRITABLE_BOOK), "gui.ic2.cards.show_name", 0, BaseTileInfoProvider.SettingType.SIMPLE, 7, 50)
      );
      entries.add(new BaseTileInfoProvider.SettingEntry(null, "gui.ic2.cards.text_color", 0, BaseTileInfoProvider.SettingType.COLOR, 24, 50));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("C"), "gui.ic2.inv_card.show_count", 1, BaseTileInfoProvider.SettingType.TEXT, 7, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("S"), "gui.ic2.inv_card.show_count_side", 2, BaseTileInfoProvider.SettingType.TEXT, 24, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("N"), "gui.ic2.inv_card.show_name", 3, BaseTileInfoProvider.SettingType.TEXT, 41, 67));
      entries.add(new BaseTileInfoProvider.SettingEntry(this.string("M"), "gui.ic2.inv_card.mergeInv", 4, BaseTileInfoProvider.SettingType.TEXT, 58, 67));
      return entries;
   }

   @Override
   protected int[] createDefaultColors() {
      return new int[]{0};
   }

   @Override
   protected int createDefaultFlags() {
      return 27;
   }
}
