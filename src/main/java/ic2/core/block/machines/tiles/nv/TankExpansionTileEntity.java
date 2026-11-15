package ic2.core.block.machines.tiles.nv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.tiles.impls.BaseExpansionTileEntity;
import ic2.core.block.machines.containers.nv.FluidExpansionContainer;
import ic2.core.block.machines.logic.crafter.CraftRecipe;
import ic2.core.block.machines.logic.crafter.CraftingList;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.fluid.ArrayFluidHandler;
import ic2.core.fluid.IC2Tank;
import ic2.core.fluid.WrappedFluid;
import ic2.core.fluid.WrapperFluidHandler;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.transporters.BaseTransporter;
import ic2.core.platform.recipes.helpers.ItemStackCache;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TankExpansionTileEntity extends BaseExpansionTileEntity implements ITileGui, IClickable {
   public static final String[] FIELDS = new String[]{"firstTank", "secondTank", "thirdTank", "fourthTank"};
   @NetworkInfo
   public IC2Tank firstTank = new IC2Tank(128000, T -> this.canFill(0, T)).addListener(T -> this.updateTank(0));
   @NetworkInfo
   public IC2Tank secondTank = new IC2Tank(128000, T -> this.canFill(1, T)).addListener(T -> this.updateTank(1));
   @NetworkInfo
   public IC2Tank thirdTank = new IC2Tank(128000, T -> this.canFill(2, T)).addListener(T -> this.updateTank(2));
   @NetworkInfo
   public IC2Tank fourthTank = new IC2Tank(128000, T -> this.canFill(3, T)).addListener(T -> this.updateTank(3));
   IC2Tank[] handlers = new IC2Tank[]{this.firstTank, this.secondTank, this.thirdTank, this.fourthTank};
   CraftRecipe recipe = null;
   int recipeSlot = -1;

   public TankExpansionTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, new ArrayFluidHandler(this.handlers));
      this.addNetworkFields(FIELDS);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FLUID_EXPANSION;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidExpansionContainer(this, player, windowID);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      IFluidHandler handler = new ArrayFluidHandler(this.handlers);
      return FluidHelper.drainContainers(player.getItemInHand(hand), player, handler)
         ? true
         : FluidHelper.fillContainers(player.getItemInHand(hand), player, handler);
   }

   public IC2Tank getTank(int index) {
      return this.handlers[index];
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("tank_1", this.firstTank.writeToNBT(new CompoundTag()));
      compound.put("tank_2", this.secondTank.writeToNBT(new CompoundTag()));
      compound.put("tank_3", this.thirdTank.writeToNBT(new CompoundTag()));
      compound.put("tank_4", this.fourthTank.writeToNBT(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.firstTank.readFromNBT(compound.getCompound("tank_1"));
      this.secondTank.readFromNBT(compound.getCompound("tank_2"));
      this.thirdTank.readFromNBT(compound.getCompound("tank_3"));
      this.fourthTank.readFromNBT(compound.getCompound("tank_4"));
   }

   protected void consume(int extra) {
      if (this.recipe != null && this.recipeSlot != -1) {
         this.recipe.consumeItems(this.recipeSlot, extra);
      }
   }

   public void updateTank(int index) {
      this.updateTileField(FIELDS[index]);
   }

   @Override
   public boolean needsRecipeInfo() {
      return true;
   }

   @Override
   public void setCurrentCraftingRecipe(CraftRecipe recipe, int recipeSlot) {
      this.recipe = recipe;
      this.recipeSlot = recipeSlot;
   }

   @Override
   public void clearCurrentRecipe() {
      this.recipe = null;
      this.recipeSlot = -1;
   }

   protected boolean canFill(int index, FluidStack stack) {
      for (int i = 0; i < 4; i++) {
         if (i != index && this.handlers[i].getFluidInTank(0).isFluidEqual(stack)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void createMainTransporter(boolean simulate, List<IItemTransporter> insertion, List<IItemTransporter> extraction) {
      extraction.add(new TankExpansionTileEntity.TankTransporter(this, simulate));
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
         this.snapshot = new Snapshot(4);
         this.snapshot.set(0, new WrappedFluid(this.firstTank.getFluid().copy()));
         this.snapshot.set(1, new WrappedFluid(this.secondTank.getFluid().copy()));
         this.snapshot.set(2, new WrappedFluid(this.thirdTank.getFluid().copy()));
         this.snapshot.set(3, new WrappedFluid(this.fourthTank.getFluid().copy()));
      }
   }

   @Override
   public void releaseSnapshot() {
      if (this.snapshot == null) {
         throw new IllegalStateException("Snapshot is missing");
      } else {
         this.firstTank.setFluid(this.snapshot.get(0, WrappedFluid.class).getFluid());
         this.secondTank.setFluid(this.snapshot.get(1, WrappedFluid.class).getFluid());
         this.thirdTank.setFluid(this.snapshot.get(2, WrappedFluid.class).getFluid());
         this.fourthTank.setFluid(this.snapshot.get(3, WrappedFluid.class).getFluid());
         this.snapshot = null;
      }
   }

   @Override
   public void triggerRestock(IItemTransporter transporter, IFluidHandler handler, Player player) {
      for (int i = 0; i < 4; i++) {
         FluidStack saved = this.snapshot.get(i, WrappedFluid.class).getFluid();
         FluidStack stored = this.handlers[i].getFluid();
         if (!saved.isEmpty() || !stored.isEmpty()) {
            if (saved.isEmpty() && !stored.isEmpty()) {
               this.handlers[i].setFluid(FluidStack.EMPTY);
            } else if (!saved.isEmpty() && stored.isEmpty()) {
               FluidStack toFill = new FluidStack(saved.getFluid(), 128000);
               toFill.setAmount(this.handlers[i].fill(handler.drain(toFill, FluidAction.SIMULATE), FluidAction.EXECUTE));
               if (toFill.getAmount() > 0) {
                  handler.drain(toFill, FluidAction.EXECUTE);
               }
            } else {
               if (!stored.isFluidEqual(saved)) {
                  stored.setAmount(0);
                  this.handlers[i].setFluid(FluidStack.EMPTY);
               }

               FluidStack toFill = new FluidStack(saved.getFluid(), 128000 - stored.getAmount());
               if (toFill.getAmount() > 0) {
                  toFill.setAmount(this.handlers[i].fill(handler.drain(toFill, FluidAction.SIMULATE), FluidAction.EXECUTE));
                  if (toFill.getAmount() > 0) {
                     handler.drain(toFill, FluidAction.EXECUTE);
                  }
               }
            }
         }
      }
   }

   public static class TankTransporter extends BaseTransporter {
      TankExpansionTileEntity tile;
      IFluidHandler[] handlers;

      public TankTransporter(TankExpansionTileEntity tile, boolean doSimulate) {
         this.tile = tile;
         this.handlers = new IFluidHandler[tile.handlers.length];

         for (int i = 0; i < this.handlers.length; i++) {
            this.handlers[i] = (IFluidHandler)(doSimulate ? new WrapperFluidHandler(tile.handlers[i]) : tile.handlers[i]);
         }
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
            for (IFluidHandler handler : this.handlers) {
               FluidStack stack = handler.getFluidInTank(0);
               if (!stack.isEmpty()) {
                  ObjectArrayList<ItemStack> items = ItemStackCache.INSTANCE.getFluidContainers(stack.getFluid());
                  int j = 0;

                  for (int m = items.size(); j < m; j++) {
                     ItemStack item = (ItemStack)items.get(j);
                     if (filter.matches(item) && item.getMaxStackSize() >= amount) {
                        FluidStack stored = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
                        int drainedAmount = handler.drain(
                                 new FluidStack(stored, Math.min(stored.getAmount() * amount, stack.getAmount() / stored.getAmount() * stack.getAmount())),
                                 simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE
                              )
                              .getAmount()
                           / stored.getAmount();
                        if (!simulate) {
                           this.tile.consume(drainedAmount);
                        }

                        return StackUtil.copyWithSize(item, drainedAmount);
                     }
                  }
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
