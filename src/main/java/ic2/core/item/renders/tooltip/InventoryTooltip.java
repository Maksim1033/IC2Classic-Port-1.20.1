package ic2.core.item.renders.tooltip;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class InventoryTooltip implements TooltipComponent {
   List<ItemStack> items;

   public InventoryTooltip(List<ItemStack> items) {
      this.items = items;
   }

   public List<ItemStack> getItems() {
      return this.items;
   }

   public static InventoryTooltip create(ItemStack stack) {
      return create(NBTUtils.getItems(stack));
   }

   public static InventoryTooltip create(List<ItemStack> list) {
      Object2IntLinkedOpenCustomHashMap<ItemStack> mapped = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.SIMPLE_INSTANCE);

      for (ItemStack stack : list) {
         mapped.addTo(stack, stack.getCount());
      }

      List<ItemStack> result = CollectionUtils.<ItemStack>createList();
      ObjectIterator var7 = Object2IntMaps.fastIterable(mapped).iterator();

      while (var7.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)var7.next();
         ItemStack stack = (ItemStack)entry.getKey();
         stack.setCount(entry.getIntValue());
         result.add(stack);
      }

      return new InventoryTooltip(result);
   }
}
