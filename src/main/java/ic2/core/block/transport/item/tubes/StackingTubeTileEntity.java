package ic2.core.block.transport.item.tubes;

import ic2.api.core.IC2Classic;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.network.tile.PacketRange;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.StackingTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.collection.SynchronizedList;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StackingTubeTileEntity extends TubeTileEntity implements IRedstoneListener, ICamouflageGui, INetworkClientEventListener {
   @NetworkInfo
   public SynchronizedList<StackingTubeTileEntity.StackingStack> cached = new SynchronizedList<>(
      StackingTubeTileEntity.StackingStack::write, StackingTubeTileEntity.StackingStack::new
   );
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int stacking = 64;
   @NetworkInfo
   public boolean ignoreColors = false;
   boolean extracted = false;

   public StackingTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"facing"});
      this.addGuiFields(new String[]{"stacking", "cached", "ignoreColors"});
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new StackingTubeContainer(this, player, windowID);
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      super.addDrops(drops);

      for (int i = 0; i < this.cached.size(); i++) {
         ((StackingTubeTileEntity.StackingStack)this.cached.get(i)).addToDrops(drops);
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 1) {
         this.stacking = Mth.clamp(this.stacking + value, 1, 64);
         this.updateGuiField("stacking");
      } else if (key == 2) {
         if (value < 0 || value >= this.cached.size()) {
            return;
         }

         IItemTransporter transporter = TransporterManager.getTransporter(entity);
         StackingTubeTileEntity.StackingStack entry = (StackingTubeTileEntity.StackingStack)this.cached.get(value);

         while (entry.getAmount() > 0) {
            ItemStack toAdd = entry.consume(64);
            int added = transporter.addItem(toAdd, Direction.DOWN, false);
            toAdd.shrink(added);
            if (toAdd.getCount() > 0) {
               entry.add(toAdd.getCount());
               break;
            }
         }

         if (entry.getAmount() <= 0) {
            this.cached.remove(value);
         }

         this.updateGuiField("cached");
      } else if (key == 3) {
         this.ignoreColors = !this.ignoreColors;
         this.updateGuiField("ignoreColors");
      }
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.cached.size(); i < m; i++) {
         list.add(((StackingTubeTileEntity.StackingStack)this.cached.get(i)).write());
      }

      NBTUtils.put(compound, "queue", list);
      NBTUtils.putByte(compound, "max", this.stacking, 64);
      NBTUtils.putBoolean(compound, "fast", this.extracted, false);
      NBTUtils.putBoolean(compound, "ignoreColors", this.ignoreColors, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);

      for (CompoundTag entry : NBTListWrapper.wrap(compound.getList("queue", 10), CompoundTag.class)) {
         StackingTubeTileEntity.StackingStack stack = StackingTubeTileEntity.StackingStack.read(entry);
         if (stack != null) {
            this.cached.add(stack);
         }
      }

      this.stacking = NBTUtils.getInt(compound, "max", 64);
      this.extracted = compound.getBoolean("fast");
      this.ignoreColors = compound.getBoolean("ignoreColors");
   }

   @Override
   protected boolean needsInitialRedstoneCheck() {
      return true;
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering() && !this.invClock(this.extracted ? 1 : 40)) {
         this.handleRedstone();
         boolean powered = this.isRedstonePowered();
         int max = powered ? -1 : this.stacking;
         int i = 0;

         for (int m = this.cached.size(); i < m; i++) {
            StackingTubeTileEntity.StackingStack entry = (StackingTubeTileEntity.StackingStack)this.cached.get(i);
            if (entry.canExtract(max)) {
               TransportedItem item = new TransportedItem(entry.consume(powered ? 64 : this.stacking));
               this.addItem(item, null);
               item.setExportDirection(this.getFacing());
               if (entry.getAmount() <= 0) {
                  this.cached.remove(i);
               }

               this.updateGuiField("cached");
               this.extracted = true;
               return;
            }
         }

         this.extracted = false;
      }
   }

   @Override
   public boolean onCenterReached(TransportedItem item) {
      if (this.isSimulating()
         && item.getRequestId() == null
         && (this.ignoreColors || item.getColor() == null)
         && item.getTransferDirection() != this.getFacing()
         && item.getStack().getMaxStackSize() >= this.stacking) {
         this.updateGuiField("cached");
         ItemStack stack = item.getStack();
         int i = 0;

         for (int m = this.cached.size(); i < m; i++) {
            StackingTubeTileEntity.StackingStack entry = (StackingTubeTileEntity.StackingStack)this.cached.get(i);
            if (entry.matches(stack)) {
               entry.add(stack.getCount());
               item.invalidate();
               this.sendToClient(1, item.getId(), PacketRange.CHUNK_TRACKED);
               return true;
            }
         }

         this.cached.add(new StackingTubeTileEntity.StackingStack(stack));
         item.invalidate();
         this.sendToClient(1, item.getId(), PacketRange.CHUNK_TRACKED);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isRotatable() {
      return true;
   }

   @Override
   public boolean canSetFacingInternal(Direction dir) {
      return dir != this.getFacing()
         && (this.isRendering() ? DirectionList.getNeighborCapability(this, dir, IC2Classic.TUBE_CAPABILITY).isPresent() : this.tubes.getHandler(dir) != null);
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      return DirectionList.ofFacing(this.getFacing()).remove(DirectionList.ofNumber(this.connectivity).invert());
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.inventories.getHandler(this.getFacing()) == null && this.tubes.getHandler(this.getFacing()) == null) {
         DirectionList list = this.inventories.getPresentSides().add(this.tubes.getPresentSides());
         if (!list.isEmpty()) {
            this.setFacing(list.getDefaultFacing());
         }
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.STACKING_TUBE;
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   public boolean allowWeakSignal(Direction dir) {
      return true;
   }

   public static class StackingStack {
      ItemStack stack;
      int amount;

      public StackingStack(ItemStack stack) {
         this.stack = StackUtil.copyWithSize(stack, 1);
         this.amount = stack.getCount();
      }

      public StackingStack(ItemStack stack, int amount) {
         this.stack = StackUtil.copyWithSize(stack, 1);
         this.amount = amount;
      }

      public StackingStack(IInputBuffer buffer) {
         this.stack = buffer.readItemStack();
         this.amount = buffer.readVarInt();
      }

      public static StackingTubeTileEntity.StackingStack read(CompoundTag nbt) {
         ItemStack stack = ItemStack.of(nbt);
         return stack.isEmpty() ? null : new StackingTubeTileEntity.StackingStack(stack, nbt.getInt("items"));
      }

      public CompoundTag write() {
         CompoundTag data = this.stack.save(new CompoundTag());
         data.putInt("items", this.amount);
         return data;
      }

      public void add(int amount) {
         this.amount += amount;
      }

      public void addToDrops(List<ItemStack> list) {
         int oldAmount = this.amount;

         while (this.amount > 0) {
            ItemStack stack = this.consume(64);
            if (!stack.isEmpty()) {
               list.add(stack);
            }
         }

         this.amount = oldAmount;
      }

      public ItemStack consume(int amount) {
         if (this.getAmount() > 0 && amount > 0) {
            ItemStack result = StackUtil.copyWithSize(this.stack, Math.min(this.stack.getMaxStackSize(), Math.min(this.getAmount(), amount)));
            this.amount = this.amount - result.getCount();
            return result;
         } else {
            return ItemStack.EMPTY;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public ItemStack getClientStack() {
         return StackUtil.copyWithSize(this.stack, Math.min(999, this.amount));
      }

      public void write(IOutputBuffer buffer) {
         buffer.writeItemStack(this.stack);
         buffer.writeVarInt(this.amount);
      }

      public boolean canExtract(int value) {
         return value < 0 || this.amount >= value;
      }

      public int getAmount() {
         return this.amount;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public boolean matches(ItemStack item) {
         return StackUtil.isStackEqual(this.stack, item);
      }
   }
}
