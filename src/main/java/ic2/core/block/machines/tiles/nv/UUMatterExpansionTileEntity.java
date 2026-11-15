package ic2.core.block.machines.tiles.nv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.recipes.registries.IUUMatterRegistry;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.special.UUMatterComparator;
import ic2.core.block.base.tiles.impls.BaseExpansionTileEntity;
import ic2.core.block.machines.containers.nv.UUMatterExpansionContainer;
import ic2.core.block.machines.logic.crafter.CraftingList;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.block.machines.logic.crafter.WrappedInteger;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.platform.recipes.misc.UUMatterRegistry;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class UUMatterExpansionTileEntity extends BaseExpansionTileEntity implements IntConsumer, ITileGui, ITickListener, INetworkClientEventListener {
   static final IntConsumer EMPTY = T -> {
   };
   @NetworkInfo
   public int maxUUMatter = 1024000;
   @NetworkInfo
   public int uuMatter = 0;
   public SimpleInventory uuInsertion = new SimpleInventory(1);
   public SimpleInventory filter = new SimpleInventory(9);

   public UUMatterExpansionTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"maxUUMatter", "uuMatter"});
      this.addComparator(new UUMatterComparator("uu_storage", ComparatorNames.STORED_UU_MATTER, this));
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.UU_EXPANSION;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("filter", this.filter.save(new CompoundTag()));
      compound.put("uu_inv", this.uuInsertion.save(new CompoundTag()));
      compound.putInt("max", this.maxUUMatter);
      compound.putInt("current", this.uuMatter);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.filter.load(compound.getCompound("filter"));
      this.uuInsertion.load(compound.getCompound("uu_inv"));
      this.maxUUMatter = compound.getInt("max");
      this.uuMatter = compound.getInt("current");
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new UUMatterExpansionContainer(this, player, windowID);
   }

   @Override
   public void onTick() {
      ItemStack stack = this.uuInsertion.getStackInSlot(0);
      if (stack.getItem() == IC2Items.UUMATTER) {
         int wanted = (this.maxUUMatter - this.uuMatter) / 1000;
         if (wanted <= 0) {
            return;
         }

         this.uuMatter = this.uuMatter + stack.split(wanted).getCount() * 1000;
         this.updateGuiField("uuMatter");
      }

      this.handleComparators();
   }

   @Override
   public void accept(int value) {
      this.uuMatter -= value;
      this.updateGuiField("uuMatter");
   }

   @Override
   public void createMainTransporter(boolean simulate, List<IItemTransporter> insertion, List<IItemTransporter> extraction) {
      List<IUUMatterRegistry.UUMatterEntry> entries = CollectionUtils.<IUUMatterRegistry.UUMatterEntry>createList();
      UUMatterRegistry registry = IC2.RECIPES.get(this.isSimulating()).UU;
      Map<ItemStack, IUUMatterRegistry.UUMatterEntry> mapped = registry.getMappedEntries();
      int i = 0;

      for (int m = this.filter.getSlotCount(); i < m; i++) {
         ItemStack entry = this.filter.getStackInSlot(i);
         if (!entry.isEmpty()) {
            IUUMatterRegistry.UUMatterEntry uu = mapped.get(entry);
            if (uu != null) {
               entries.add(uu);
            }
         }
      }

      if (entries.isEmpty()) {
         entries.addAll(registry.getEntries());
      }

      if (!entries.isEmpty()) {
         extraction.add(new UUMatterExpansionTileEntity.UUTransporter(entries, this.uuMatter, (IntConsumer)(simulate ? EMPTY : this)));
      }
   }

   @Override
   public void createTrashTransporter(boolean simulate, List<IItemTransporter> insertion, List<IItemTransporter> extraction) {
   }

   @Override
   public CraftingList getExpansionRecipeList() {
      return null;
   }

   @Override
   public void createSnapshot() {
      this.snapshot = new Snapshot(1);
      this.snapshot.set(0, new WrappedInteger(this.uuMatter));
   }

   @Override
   public void releaseSnapshot() {
      this.uuMatter = this.snapshot.get(0, WrappedInteger.class).getValue();
      this.snapshot = null;
      this.updateGuiField("uuMatter");
   }

   @Override
   public void triggerRestock(IItemTransporter transporter, IFluidHandler handler, Player player) {
      int wanted = (this.maxUUMatter - this.uuMatter) / 1000;
      if (wanted > 0) {
         ItemStack stack = this.uuInsertion.getStackInSlot(0);
         if (stack.getItem() == IC2Items.UUMATTER) {
            this.uuMatter = this.uuMatter + stack.split(wanted).getCount() * 1000;
            this.updateGuiField("uuMatter");
         }

         wanted = (this.maxUUMatter - this.uuMatter) / 1000;
         if (wanted > 0) {
            this.uuMatter = this.uuMatter + transporter.removeItem(new SimpleFilter(IC2Items.UUMATTER), null, wanted, false).getCount() * 1000;
            this.updateGuiField("uuMatter");
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 1) {
         if (value < 0) {
            int amount = this.uuMatter / 1000;
            int removed = 0;

            while (amount > removed) {
               int added = amount - removed;
               ItemStack stack = new ItemStack(IC2Items.UUMATTER, added);
               entity.getInventory().add(stack);
               removed += added - stack.getCount();
               if (stack.getCount() > 0) {
                  break;
               }
            }

            if (removed > 0) {
               this.uuMatter -= removed * 1000;
               this.updateGuiField("uuMatter");
            }
         } else if (value > 0 && value <= 64) {
            value = Math.min(value, this.uuMatter / 1000);
            if (value <= 0) {
               return;
            }

            ItemStack stack = new ItemStack(IC2Items.UUMATTER, value);
            entity.getInventory().add(stack);
            this.uuMatter = this.uuMatter - (value - stack.getCount()) * 1000;
            this.updateGuiField("uuMatter");
         }
      }
   }

   public static class UUTransporter implements IItemTransporter {
      List<IUUMatterRegistry.UUMatterEntry> entries;
      int uuLeft;
      IntConsumer actual;

      public UUTransporter(List<IUUMatterRegistry.UUMatterEntry> entries, int uuLeft, IntConsumer actual) {
         this.entries = entries;
         this.uuLeft = uuLeft;
         this.actual = actual;
      }

      @Override
      public int addItem(ItemStack stack, Direction dir, boolean simulate) {
         return 0;
      }

      @Override
      public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
         if (amount <= 0) {
            return ItemStack.EMPTY;
         } else {
            int i = 0;

            for (int m = this.entries.size(); i < m; i++) {
               IUUMatterRegistry.UUMatterEntry entry = this.entries.get(i);
               ItemStack stack = entry.getStack();
               if (filter.matches(stack)) {
                  int perItem = entry.getUUNeeded();
                  amount = Math.min(amount, this.uuLeft / perItem);
                  if (amount > 0) {
                     int value = amount * perItem;
                     this.actual.accept(value);
                     this.uuLeft -= value;
                  }

                  return StackUtil.copyWithSize(stack, amount);
               }
            }

            return ItemStack.EMPTY;
         }
      }

      @Override
      public int getInventorySize(Direction dir) {
         return 0;
      }

      @Override
      public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
         return Object2IntMaps.emptyMap();
      }

      @Override
      public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
         return new IItemTransporter.InvResult(compareNBT);
      }
   }
}
