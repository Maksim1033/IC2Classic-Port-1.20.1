package ic2.core.block.personal.tile;

import ic2.api.items.ItemRegistries;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.cache.PersonalCapabilityCache;
import ic2.core.block.base.tiles.impls.BaseTradeOMatTileEntity;
import ic2.core.block.personal.container.FluidOMatContainer;
import ic2.core.block.personal.provider.ITradeProvider;
import ic2.core.block.personal.trade.FluidTrade;
import ic2.core.block.personal.trade.FluidTradeHelper;
import ic2.core.fluid.IC2Tank;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class FluidOMatTileEntity extends BaseTradeOMatTileEntity<FluidTrade, ITradeProvider> {
   protected ICache<IFluidHandler> tradeSources = new PersonalCapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.FLUID_HANDLER);
   protected ICache<IItemHandler> items = new PersonalCapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.ITEM_HANDLER);
   @NetworkInfo
   public IC2Tank tank = new IC2Tank(128000);
   @NetworkInfo
   public FluidTradeHelper helper = new FluidTradeHelper(this);
   protected IHasInventory input = new RangedInventory(this, 0, 1, 2, 3);
   protected IHasInventory output = new RangedInventory(this, 4, 5, 6, 7);

   public FluidOMatTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 10, new FluidTrade());
      this.addGuiFields(new String[]{"tank"});
      this.addCaches(new ICache[]{this.tradeSources, this.items});
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
      this.tank.addListener(T -> this.updateGuiField("tank"));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.put(compound, "tank", this.tank.writeToNBT(new CompoundTag()));
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidOMatContainer(
         this, player, windowID, !IC2.KEYBOARD.isSideInventoryKeyDown(player) && this.canDoAction(player.getUUID(), Action.TRADE_SETTINGS, false)
      );
   }

   @Override
   protected ITradeProvider createForPlayer(UUID player) {
      return null;
   }

   @Override
   protected ITradeProvider getSelf() {
      return null;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FLUID_O_MAT;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (this.canDoAction(entity.getUUID(), Action.TRADE_SETTINGS, false) && key == 8) {
         this.helper.setAmount(value);
      }

      super.onClientDataReceived(entity, key, value);
   }

   public void onHelperUpdated() {
      this.trade.offered.setFluid(this.helper.getResult());
      this.updateGuiFields(new String[]{"helper", "trade"});
   }

   @Override
   protected boolean attemptTrade(ITradeProvider provider) {
      if ((!this.tradeSources.isEmpty() || this.trade.infinite) && this.trade.canTrade(this.getLevel().getGameTime(), null)) {
         if (this.trade.infinite) {
            if (this.trade(EmptyFluidHandler.INSTANCE)) {
               this.trade.onTrade(this.getLevel().getGameTime(), null);
               this.onTraded();
               return true;
            }
         } else {
            for (Direction dir : this.tradeSources) {
               if (this.trade(this.tradeSources.getHandler(dir))) {
                  this.trade.onTrade(this.getLevel().getGameTime(), null);
                  this.onTraded();
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   protected int recalculateStock() {
      int amount = 0;

      for (Direction dir : this.tradeSources) {
         IFluidHandler handler = this.tradeSources.getHandler(dir);
         if (handler != null) {
            amount += this.trade.getCount(handler);
         }
      }

      return amount;
   }

   @Override
   protected IHasInventory getInput() {
      return this.input;
   }

   @Override
   public void onTick() {
      super.onTick();
      if (this.clock(10)) {
         FluidHelper.fillContainers(this, 8, 9, this.tank);
      }
   }

   protected boolean trade(IFluidHandler handler) {
      if (handler != null && this.trade.hasStock(handler, this.tank, FluidAction.SIMULATE)) {
         SimpleInventory inputCopy = SimpleInventory.createCopy(this.input);
         NonNullList<ItemStack> stockResult = NonNullList.create();
         NonNullList<ItemStack> result = NonNullList.create();
         if (this.trade.moneyNeeded > 0) {
            int moneyFound = this.trade.getFoundMoney(inputCopy);
            if (this.trade.moneyNeeded > moneyFound) {
               return false;
            }

            int coinsLeft = this.trade.consumeCoins(inputCopy, this.trade.moneyNeeded);
            ItemRegistries.generateCoins(this.trade.moneyNeeded, stockResult, false);
            if (coinsLeft < 0) {
               ItemRegistries.generateCoins(-coinsLeft, result, false);
            }
         } else if (!this.trade.hasItems(TransporterManager.getTransporter(inputCopy), this.inputFilter, stockResult)) {
            return false;
         }

         SimpleInventory outputCopy = SimpleInventory.createCopy(this.output);
         if (!insertItems(TransporterManager.getTransporter(outputCopy), result, null, false)) {
            return false;
         } else if (!this.trade.infinite && !this.insertItems(stockResult)) {
            return false;
         } else {
            this.trade.hasStock(handler, this.tank, FluidAction.EXECUTE);
            mergeCoins(outputCopy);
            outputCopy.overrideTo(this.output);
            inputCopy.overrideTo(this.input);
            return true;
         }
      } else {
         return false;
      }
   }

   protected boolean insertItems(NonNullList<ItemStack> toInsert) {
      for (Direction dir : this.items) {
         IItemHandler handler = this.items.getHandler(dir);
         if (handler != null) {
            SimpleInventory inv = SimpleInventory.createCopy(handler);
            if (insertItems(TransporterManager.getTransporter(inv), toInsert, null, false)) {
               mergeCoins(inv);
               inv.applyDifferences(handler);
               return true;
            }
         }
      }

      return toInsert.isEmpty();
   }
}
