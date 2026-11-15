package ic2.core.item.upgrades.base;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.IMachine;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.inv.container.FluidImportExportContainer;
import ic2.core.item.inv.inventory.FluidImportExportInventory;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

public class BaseFluidTransportUpgrade extends BaseDirectionalUpgrade implements INetworkItemEvent, IHasHeldSlotInventory {
   public static final String FILTERS = "filters";
   public static final String TRANSFER = "transfer";
   public static final String INVERTED = "inverted";

   public BaseFluidTransportUpgrade(String itemName) {
      super(itemName);
      this.functions.remove(IUpgradeItem.Functions.RECIPE);
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.TRANSPORT_MOD;
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer() && player.containerMenu instanceof FluidImportExportContainer) {
         ((FluidImportExportContainer)player.containerMenu).getHolder().onDataReceived(key, value);
      }
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new FluidImportExportInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new FluidImportExportInventory(player, this, stack, slot).load(stack);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.KEYBOARD.isSideInventoryKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (IC2.PLATFORM.isSimulating()) {
            IC2.PLATFORM.launchGui(playerIn, handIn, null, this.getInventory(playerIn, handIn, stack));
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public Set<Fluid> getFluids(ItemStack stack) {
      Set<Fluid> fluids = CollectionUtils.<Fluid>createLinkedSet();

      for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("filters", 10), CompoundTag.class)) {
         Fluid fluid = (Fluid)ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("id")));
         if (fluid != Fluids.EMPTY) {
            fluids.add(fluid);
         }
      }

      return fluids;
   }

   public int transferRate(ItemStack stack) {
      return NBTUtils.getInt(StackUtil.getNbtData(stack), "transfer", 1000);
   }

   public boolean isInverted(ItemStack stack) {
      return !NBTUtils.getBoolean(StackUtil.getNbtData(stack), "inverted", true);
   }

   public void transferFluid(IMachine machine, IFluidHandler source, IFluidHandler target, int transferRate, Set<Fluid> fluids, boolean inverted) {
      if (transferRate > 0) {
         int used = 0;
         int i = 0;

         for (int m = source.getTanks(); i < m; i++) {
            FluidStack fluid = source.getFluidInTank(i);
            if (fluids.isEmpty() || fluids.contains(fluid.getFluid()) != inverted) {
               int drained = target.fill(source.drain(new FluidStack(fluid, transferRate), FluidAction.SIMULATE), FluidAction.EXECUTE);
               if (drained > 0) {
                  source.drain(new FluidStack(fluid, drained), FluidAction.EXECUTE);
                  transferRate -= drained;
                  used += drained;
                  if (transferRate <= 0) {
                     break;
                  }
               }
            }
         }

         machine.useEnergy(Mth.ceil((float)used / 100.0F), true);
      }
   }
}
