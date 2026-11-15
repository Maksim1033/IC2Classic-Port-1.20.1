package ic2.core.block.machines.logic.crafter;

import ic2.api.util.DirectionList;
import ic2.core.block.machines.tiles.nv.IndustrialWorkbenchTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IWorkbenchExpansion {
   boolean addUnloadListener(IndustrialWorkbenchTileEntity var1);

   default boolean needsRecipeInfo() {
      return false;
   }

   default void setCurrentCraftingRecipe(CraftRecipe recipe, int recipeSlot) {
   }

   default void clearCurrentRecipe() {
   }

   void createMainTransporter(boolean var1, List<IItemTransporter> var2, List<IItemTransporter> var3);

   void createTrashTransporter(boolean var1, List<IItemTransporter> var2, List<IItemTransporter> var3);

   CraftingList getExpansionRecipeList();

   void createSnapshot();

   void destroySnapshot();

   void releaseSnapshot();

   void triggerRestock(IItemTransporter var1, IFluidHandler var2, Player var3);

   Snapshot takeSnapshot();

   void setSnapshot(Snapshot var1);

   static NonNullList<ItemStack> restockInventory(IItemTransporter transporter, IHasInventory inventory, Int2ObjectMap<ItemStack[]> difference) {
      NonNullList<ItemStack> toAdd = NonNullList.create();
      Int2ObjectMap<ItemStack> toRequest = new Int2ObjectLinkedOpenHashMap();
      ObjectIterator var5 = Int2ObjectMaps.fastIterable(difference).iterator();

      while (var5.hasNext()) {
         Entry<ItemStack[]> entry = (Entry<ItemStack[]>)var5.next();
         int slot = entry.getIntKey();
         ItemStack stored = ((ItemStack[])entry.getValue())[0];
         ItemStack expected = ((ItemStack[])entry.getValue())[1];
         if (!expected.isEmpty() && !stored.isEmpty()) {
            if (expected.isDamageableItem() ? ItemStack.isSameItemSameTags(expected, stored) : ItemStack.isSameItem(expected, stored)) {
               if (expected.getMaxStackSize() > stored.getCount()) {
                  toRequest.put(slot, StackUtil.copyWithSize(expected, expected.getMaxStackSize() - stored.getCount()));
               }
            } else {
               toAdd.add(stored);
               inventory.setStackInSlot(slot, ItemStack.EMPTY);
               toRequest.put(slot, expected);
            }
         } else if (!expected.isEmpty() && stored.isEmpty()) {
            toRequest.put(slot, StackUtil.copyWithSize(expected, expected.getMaxStackSize()));
         }
      }

      var5 = Int2ObjectMaps.fastIterable(toRequest).iterator();

      while (var5.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)var5.next();
         ItemStack value = (ItemStack)entry.getValue();
         ItemStack removed = transporter.removeItem(StackFilter.defaultCompare((ItemStack)entry.getValue()), null, value.getCount(), false);
         if (!removed.isEmpty()) {
            value.shrink(removed.getCount());
            ItemStack stored = inventory.getStackInSlot(entry.getIntKey());
            if (!stored.isEmpty()) {
               stored.grow(removed.getCount());
               inventory.setStackInSlot(entry.getIntKey(), stored);
            } else {
               inventory.setStackInSlot(entry.getIntKey(), removed.copy());
            }
         }
      }

      return toAdd;
   }

   static <T extends BlockEntity & IWorkbenchExpansion> void searchForBenches(T tile) {
      AABBUtil.countBlocks(
         tile.getLevel(),
         tile.getBlockPos(),
         IndustrialWorkbenchTileEntity.getExpansionLimit(),
         new IWorkbenchExpansion.ExpansionFilter(tile),
         0,
         DirectionList.ALL
      );
   }

   public static class ExpansionFilter implements AABBUtil.IBlockFilter {
      IWorkbenchExpansion expansion;

      public ExpansionFilter(IWorkbenchExpansion expansion) {
         this.expansion = expansion;
      }

      @Override
      public boolean isValid(LevelReader world, BlockPos pos) {
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof IndustrialWorkbenchTileEntity) {
            ((IndustrialWorkbenchTileEntity)tile).addExpansion(this.expansion);
            return false;
         } else {
            return tile instanceof IWorkbenchExpansion;
         }
      }

      @Override
      public boolean isValid(BlockState state) {
         return false;
      }
   }
}
