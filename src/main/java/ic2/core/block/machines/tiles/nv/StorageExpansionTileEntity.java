package ic2.core.block.machines.tiles.nv;

import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.InventoryComparator;
import ic2.core.block.base.tiles.impls.BaseExpansionTileEntity;
import ic2.core.block.machines.containers.nv.StorageExpansionContainer;
import ic2.core.block.machines.logic.crafter.CraftingList;
import ic2.core.block.machines.logic.crafter.IWorkbenchExpansion;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.inventory.inv.ListenerInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.inventory.transporter.transporters.special.NonEmptyTransporter;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class StorageExpansionTileEntity extends BaseExpansionTileEntity implements ITileGui, INotifyInventory, IDropProvider {
   public SimpleInventory inventory = new ListenerInventory(18, this);

   public StorageExpansionTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addComparator(new InventoryComparator("slots", ComparatorNames.FILLED_SLOTS, this.inventory, true));
      this.addComparator(new InventoryComparator("items", ComparatorNames.FILLED_INVENTORY, this.inventory, false));
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
      if (this.isSimulating()) {
         this.handleComparators(true);
      }
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      this.inventory.addToDrops(drops);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.STORAGE_EXPANSION;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new StorageExpansionContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("inv", this.inventory.save(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.inventory.load(compound.getCompound("inv"));
   }

   @Override
   public void createMainTransporter(boolean simulate, List<IItemTransporter> insertion, List<IItemTransporter> extraction) {
      IHasInventory inv = simulate ? SimpleInventory.createCopy(this.inventory) : this.inventory;
      insertion.add(new NonEmptyTransporter(inv));
      extraction.add(TransporterManager.getTransporter(inv));
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
      if (this.snapshot != null) {
         throw new RuntimeException("Snapshot already existed");
      } else {
         this.snapshot = new Snapshot(1);
         this.snapshot.set(0, SimpleInventory.createCopy(this.inventory));
      }
   }

   @Override
   public void releaseSnapshot() {
      if (this.snapshot == null) {
         throw new IllegalStateException("Snapshot is missing");
      } else {
         this.snapshot.get(0, SimpleInventory.class).overrideTo(this.inventory);
         this.snapshot = null;
      }
   }

   @Override
   public void triggerRestock(IItemTransporter transporter, IFluidHandler handler, Player player) {
      NonNullList<ItemStack> toAdd = IWorkbenchExpansion.restockInventory(
         transporter, this.inventory, this.snapshot.get(0, SimpleInventory.class).getDifference(this.inventory, true)
      );
      if (!toAdd.isEmpty()) {
         transporter = TransporterManager.getTransporter(this.inventory);
         int i = 0;

         for (int m = toAdd.size(); i < m; i++) {
            ItemStack stack = (ItemStack)toAdd.get(i);
            stack.shrink(transporter.addItem(stack, null, false));
            StackUtil.addOrDrop(player, stack);
         }
      }
   }
}
