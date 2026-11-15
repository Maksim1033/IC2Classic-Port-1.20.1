package ic2.core.item.upgrades.base;

import ic2.api.items.ITagItem;
import ic2.api.items.IUpgradeItem;
import ic2.api.network.item.INetworkItemEvent;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.filter.ArrayOrFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.InvertedFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.TransferFilter;
import ic2.core.item.inv.container.ImportExportContainer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;

public class BaseTransportUpgrade extends BaseDirectionalUpgrade implements INetworkItemEvent {
   public static final String TRANSFER_SIZE = "transfer_size";
   public static final String FILTERS = "filters";
   public static final String FLAGS = "filter_flags";
   public static final String DURABILITY = "filter_durability";
   public static final String INVERTED = "filter_inverted";

   public BaseTransportUpgrade(String itemName) {
      super(itemName);
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.TRANSPORT_MOD;
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer() && player.containerMenu instanceof ImportExportContainer) {
         ((ImportExportContainer)player.containerMenu).getHolder().onDataReceived(key, value);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (this instanceof IHasHeldGui && IC2.KEYBOARD.isSideInventoryKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (IC2.PLATFORM.isSimulating()) {
            IC2.PLATFORM.launchGui(playerIn, handIn, null, ((IHasHeldGui)this).getInventory(playerIn, handIn, stack));
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public int getTransferSize(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return nbt.contains("transfer_size") ? nbt.getInt("transfer_size") : 64;
   }

   public IFilter getFilter(ItemStack stack) {
      List<IFilter> filters = CollectionUtils.<IFilter>createList();
      CompoundTag nbt = StackUtil.getNbtData(stack);
      int flags = nbt.getInt("filter_flags") | 4;
      int durability = nbt.getInt("filter_durability");

      for (ItemStack item : NBTUtils.getItems(stack, "filters")) {
         filters.add(new TransferFilter(item, flags | (item.getItem() instanceof ITagItem ? 32 : 0), durability));
      }

      if (!filters.isEmpty()) {
         return (IFilter)(!NBTUtils.getBoolean(nbt, "filter_inverted", true) ? new InvertedFilter(new ArrayOrFilter(filters)) : new ArrayOrFilter(filters));
      } else {
         return SpecialFilters.ANY_ITEM;
      }
   }
}
