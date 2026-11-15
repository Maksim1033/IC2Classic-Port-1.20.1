package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.FilterTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.collection.SynchronizedList;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class FilterTubeTileEntity extends TubeTileEntity implements ICamouflageGui, ICopyableSettings {
   public SimpleInventory inventory = new SimpleInventory(1);
   public Object2IntMap<Item> filters = new Object2IntOpenHashMap();
   @NetworkInfo
   public SynchronizedList<FilterTubeTileEntity.FilterEntry> stacks = new SynchronizedList<>(
      FilterTubeTileEntity.FilterEntry::write, FilterTubeTileEntity.FilterEntry::new
   );
   @NetworkInfo
   public boolean invPriority = false;
   @NetworkInfo
   public int emptyDirections = 63;

   public FilterTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"stacks", "invPriority", "emptyDirections"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FILTER_TUBE;
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FilterTubeContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      ListTag list = new ListTag();

      for (int i = 0; i < this.stacks.size(); i++) {
         list.add(((FilterTubeTileEntity.FilterEntry)this.stacks.get(i)).write());
      }

      NBTUtils.put(compound, "filters", list);
      NBTUtils.putBoolean(compound, "invPrio", this.invPriority, false);
      compound.putInt("emptyDirs", this.emptyDirections);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.invPriority = compound.getBoolean("invPrio");

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         FilterTubeTileEntity.FilterEntry entry = FilterTubeTileEntity.FilterEntry.read(data);
         if (entry != null) {
            this.stacks.add(entry);
            this.filters.put(entry.getStack().getItem(), entry.getSides().getCode());
         }
      }

      this.emptyDirections = compound.contains("emptyDirs") ? compound.getInt("emptyDirs") : this.generateEmptyDirs();
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      ListTag list = new ListTag();

      for (int i = 0; i < this.stacks.size(); i++) {
         list.add(((FilterTubeTileEntity.FilterEntry)this.stacks.get(i)).write());
      }

      NBTUtils.put(compound, "filters", list);
      NBTUtils.putBoolean(compound, "invPrio", this.invPriority, false);
      compound.putInt("emptyDirs", this.emptyDirections);
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.stacks.clear();
      this.filters.clear();
      this.invPriority = compound.getBoolean("invPrio");

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         FilterTubeTileEntity.FilterEntry entry = FilterTubeTileEntity.FilterEntry.read(data);
         if (entry != null) {
            this.stacks.add(entry);
            this.filters.put(entry.getStack().getItem(), entry.getSides().getCode());
         }
      }

      this.emptyDirections = compound.contains("emptyDirs") ? compound.getInt("emptyDirs") : this.generateEmptyDirs();
      this.updateGuiFields(new String[]{"stacks", "filters", "emptyDirections", "invPriority"});
   }

   private int generateEmptyDirs() {
      DirectionList list = DirectionList.ALL;
      ObjectListIterator var2 = this.stacks.iterator();

      while (var2.hasNext()) {
         FilterTubeTileEntity.FilterEntry entry = (FilterTubeTileEntity.FilterEntry)var2.next();
         list = list.remove(entry.getSides());
         if (list.isEmpty()) {
            return 0;
         }
      }

      return list.getCode();
   }

   public DirectionList getEmptyDirs() {
      return DirectionList.ofNumber(this.emptyDirections);
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      DirectionList list = super.getValidDirections(item);
      if (this.isSimulating() && !list.isEmpty()) {
         DirectionList filter = list.keep(DirectionList.ofNumber(this.filters.getInt(item.getStack().getItem())));
         list = filter.isEmpty() ? list.keep(this.getEmptyDirs()) : filter;
         if (this.invPriority && item.getRequestId() == null && !list.keep(this.inventories.getPresentSides()).isEmpty()) {
            list = list.keep(this.inventories.getPresentSides());
         }
      }

      return list;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 1) {
         if (value >= 0 && value < this.stacks.size()) {
            FilterTubeTileEntity.FilterEntry entry = (FilterTubeTileEntity.FilterEntry)this.stacks.remove(value);
            this.filters.removeInt(entry.getStack().getItem());
            this.updateGuiField("stacks");
         }
      } else if (key == 2) {
         this.invPriority = !this.invPriority;
         this.updateGuiField("invPriority");
      } else if (key == 3) {
         ItemStack stack = this.inventory.getStackInSlot(0);
         if (stack.isEmpty() || this.stacks.size() >= 127) {
            return;
         }

         FilterTubeTileEntity.FilterEntry entry = new FilterTubeTileEntity.FilterEntry(stack, DirectionList.ALL);
         if (!this.stacks.contains(entry)) {
            this.stacks.add(entry);
            this.inventory.setStackInSlot(0, ItemStack.EMPTY);
            this.filters.put(entry.getStack().getItem(), DirectionList.ALL.getCode());
            this.updateGuiField("stacks");
         }
      } else if (key >= 10 && key < 16) {
         if (value >= 0 && value < this.stacks.size()) {
            Direction dir = Direction.from3DDataValue(key - 10);
            FilterTubeTileEntity.FilterEntry entry = (FilterTubeTileEntity.FilterEntry)this.stacks.get(value);
            entry.flip(dir);
            this.filters.put(entry.getStack().getItem(), entry.getSides().getCode());
            this.updateGuiField("stacks");
         } else if (value == -1) {
            this.emptyDirections = this.emptyDirections ^ 1 << Direction.from3DDataValue(key - 10).get3DDataValue();
            this.updateGuiField("emptyDirections");
         }
      }
   }

   public static class FilterEntry {
      ItemStack stack;
      DirectionList sides;

      public FilterEntry(ItemStack stack, DirectionList sides) {
         this.stack = stack;
         this.sides = sides;
      }

      public FilterEntry(IInputBuffer buffer) {
         this.stack = new ItemStack(buffer.readForgeRegistryEntry(ForgeRegistries.ITEMS));
         this.sides = DirectionList.ofNumber(buffer.readByte());
      }

      public static FilterTubeTileEntity.FilterEntry read(CompoundTag data) {
         ItemStack stack = new ItemStack(ForgeRegistryWrapper.getItemValue(new ResourceLocation(data.getString("item"))));
         return stack.isEmpty() ? null : new FilterTubeTileEntity.FilterEntry(stack, DirectionList.ofNumber(data.getInt("sides")));
      }

      public void write(IOutputBuffer buffer) {
         buffer.writeForgeEntry(this.stack.getItem(), ForgeRegistries.ITEMS);
         buffer.writeByte((byte)this.sides.getCode());
      }

      public CompoundTag write() {
         CompoundTag nbt = new CompoundTag();
         nbt.putString("item", ForgeRegistryWrapper.getItemKey(this.stack.getItem()).toString());
         nbt.putByte("sides", (byte)this.sides.getCode());
         return nbt;
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof FilterTubeTileEntity.FilterEntry ? this.stack.getItem() == ((FilterTubeTileEntity.FilterEntry)obj).getStack().getItem() : false;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public DirectionList getSides() {
         return this.sides;
      }

      public boolean flip(Direction dir) {
         boolean result = this.sides.notContains(dir);
         this.sides = this.sides.flip(dir);
         return result;
      }
   }
}
