package ic2.core.block.machines.logic.scanner;

import ic2.core.block.machines.tiles.hv.OreScannerTileEntity;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ScannerInventory extends SimpleInventory {
   OreScannerTileEntity tile;
   Object2ObjectMap<Block, LongList> positionMap = CollectionUtils.<Block, LongList>createLinkedMap();
   Block[] blocks = new Block[0];
   ItemStack[] items = new ItemStack[0];

   public ScannerInventory(int size, OreScannerTileEntity tile) {
      super(size);
      this.tile = tile;
   }

   @Override
   public void load(CompoundTag nbt) {
      for (CompoundTag entry : NBTListWrapper.wrap(nbt.getList("results", 10), CompoundTag.class)) {
         Block block = ForgeRegistryWrapper.getBlockValue(new ResourceLocation(entry.getString("name")));
         if (block != Blocks.AIR) {
            this.positionMap.put(block, LongArrayList.wrap(entry.getLongArray("positions")));
         }
      }

      this.populate();
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.positionMap).iterator();

      while (var3.hasNext()) {
         Entry<Block, LongList> entry = (Entry<Block, LongList>)var3.next();
         CompoundTag data = new CompoundTag();
         data.putString("name", ForgeRegistryWrapper.getBlockKey((Block)entry.getKey()).toString());
         data.putLongArray("positions", ((LongList)entry.getValue()).toLongArray());
         list.add(data);
      }

      nbt.put("results", list);
      return nbt;
   }

   public void clearData() {
      this.tile.offset = 0;
      this.items = new ItemStack[0];
      this.blocks = new Block[0];
      this.positionMap.clear();
      this.updateSelection();
   }

   public void populate() {
      this.tile.offset = 0;
      List<Block> blockResults = new ObjectArrayList(this.positionMap.keySet());
      blockResults.sort(Comparator.<Block>comparingInt(T -> ((LongList)this.positionMap.get(T)).size()).reversed());
      List<ItemStack> results = CollectionUtils.<ItemStack>createList();

      for (int i = 0; i < blockResults.size(); i++) {
         Block entry = blockResults.get(i);
         ItemStack stack = new ItemStack(entry.asItem());
         if (stack.isEmpty()) {
            blockResults.remove(i--);
         } else {
            stack.getOrCreateTag().putInt("ore_scanner_count", ((LongList)this.positionMap.get(entry)).size());
            StackUtil.addTooltip(stack, "" + ChatFormatting.RESET + ChatFormatting.AQUA + "Click to toggle Highlight");
            results.add(stack);
         }
      }

      this.items = results.toArray(ItemStack[]::new);
      this.blocks = blockResults.toArray(Block[]::new);
      this.updateSelection();
   }

   public void updateSelection() {
      int i = 0;

      for (int m = this.getSlotCount(); i < m; i++) {
         int index = i + this.tile.offset;
         if (index >= this.items.length) {
            this.setStackInSlot(i, ItemStack.EMPTY);
         } else {
            this.setStackInSlot(i, this.items[index].copy());
         }
      }
   }

   public LongList getBlocks(int index) {
      index += this.tile.offset;
      return (LongList)(index >= 0 && index < this.blocks.length ? (LongList)this.positionMap.get(this.blocks[index]) : LongLists.EMPTY_LIST);
   }

   public Block getBlock(int index) {
      index += this.tile.offset;
      return index >= 0 && index < this.blocks.length ? this.blocks[index] : Blocks.AIR;
   }

   public int getColor(int index) {
      index += this.tile.offset;
      return index >= 0 && index < this.blocks.length ? this.blocks[index].defaultMapColor().col | -1778384896 : 0;
   }

   public Object2ObjectMap<Block, LongList> getPositionMap() {
      return this.positionMap;
   }

   public int resultSize() {
      return this.items.length;
   }

   public NonNullList<ItemStack> getFoundItems() {
      NonNullList<ItemStack> list = NonNullList.create();
      ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.positionMap).iterator();

      while (var2.hasNext()) {
         Entry<Block, LongList> entry = (Entry<Block, LongList>)var2.next();
         ItemStack stack = new ItemStack(((Block)entry.getKey()).asItem());
         if (!stack.isEmpty()) {
            stack.setCount(((LongList)entry.getValue()).size());
            list.add(stack);
         }
      }

      return list;
   }
}
