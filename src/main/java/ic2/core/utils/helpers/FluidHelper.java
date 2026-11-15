package ic2.core.utils.helpers;

import ic2.core.fluid.WrapperFluidHandler;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.misc.FluidDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidHelper {
   public static FluidStack transform(FluidStack stack, TagKey<Fluid> tag, Fluid targetFluid) {
      if (stack.getFluid() == targetFluid) {
         return stack;
      } else {
         return stack.getFluid().is(tag) ? new FluidStack(targetFluid, stack.getAmount()) : FluidStack.EMPTY;
      }
   }

   public static int getFluidColor(FluidStack stack) {
      return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
   }

   @OnlyIn(Dist.CLIENT)
   public static TextureAtlasSprite getTexture(FluidStack stack, boolean flowing) {
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureLocation(stack, flowing));
   }

   public static ResourceLocation getTextureLocation(FluidStack stack, boolean flowing) {
      IClientFluidTypeExtensions attribute = IClientFluidTypeExtensions.of(stack.getFluid());
      return flowing ? attribute.getFlowingTexture(stack) : attribute.getStillTexture(stack);
   }

   public static Fluid getDisplayFluid(ItemStack stack) {
      return stack.getItem() instanceof FluidDisplay ? FluidDisplay.getFluid(stack) : FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getFluid();
   }

   public static boolean drainContainers(ItemStack source, Player player, IFluidHandler handler) {
      if (source.isEmpty()) {
         return false;
      } else {
         IItemTransporter transporter = TransporterManager.getTransporter(player.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElse(null));
         if (transporter == null) {
            return false;
         } else {
            FluidActionResult emptyingResult = FluidUtil.tryEmptyContainer(
               source, new WrapperFluidHandler.FillHandlerWrapper(handler), handler.getTankCapacity(0), null, true
            );
            if (!emptyingResult.isSuccess()) {
               return false;
            } else if (transporter.addItem(emptyingResult.getResult(), Direction.DOWN, true) <= 0) {
               return false;
            } else {
               emptyingResult = FluidUtil.tryEmptyContainer(source, handler, handler.getTankCapacity(0), null, true);
               source.shrink(1);
               transporter.addItem(emptyingResult.getResult(), Direction.DOWN, false);
               return true;
            }
         }
      }
   }

   public static boolean drainContainers(IHasInventory inv, int inputSlot, int outputSlot, IFluidHandler handler) {
      ItemStack input = inv.getStackInSlot(inputSlot);
      if (!input.isEmpty() && StackUtil.getStackSizeLeft(inv.getStackInSlot(outputSlot)) > 0) {
         FluidActionResult emptyingResult = FluidUtil.tryEmptyContainer(
            input, new WrapperFluidHandler.FillHandlerWrapper(handler), handler.getTankCapacity(0), null, true
         );
         if (!emptyingResult.isSuccess()) {
            return false;
         } else {
            ItemStack result = emptyingResult.getResult();
            if (!fillIntoInventory(inv, outputSlot, result, false)) {
               return false;
            } else {
               emptyingResult = FluidUtil.tryEmptyContainer(input, handler, handler.getTankCapacity(0), null, true);
               input.shrink(1);
               fillIntoInventory(inv, outputSlot, emptyingResult.getResult(), true);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean fillContainers(IHasInventory inv, int inputSlot, int outputSlot, IFluidHandler handler) {
      ItemStack input = inv.getStackInSlot(inputSlot);
      if (!input.isEmpty() && StackUtil.getStackSizeLeft(inv.getStackInSlot(outputSlot)) > 0) {
         FluidActionResult fillingResult = FluidUtil.tryFillContainer(input, new WrapperFluidHandler(handler), handler.getTankCapacity(0), null, true);
         if (!fillingResult.isSuccess()) {
            return false;
         } else {
            ItemStack result = fillingResult.getResult();
            if (!fillIntoInventory(inv, outputSlot, result, false)) {
               return false;
            } else {
               fillingResult = FluidUtil.tryFillContainer(input, handler, handler.getTankCapacity(0), null, true);
               input.shrink(1);
               fillIntoInventory(inv, outputSlot, fillingResult.getResult(), true);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean fillContainer(IHasInventory inputInv, int inputSlot, IHasInventory outputInv, IFluidHandler handler, boolean simulate) {
      ItemStack input = inputInv.getStackInSlot(inputSlot);
      if (input.isEmpty()) {
         return false;
      } else {
         FluidActionResult fillingResult = FluidUtil.tryFillContainer(input, new WrapperFluidHandler(handler), handler.getTankCapacity(0), null, true);
         if (!fillingResult.isSuccess()) {
            return false;
         } else {
            ItemStack result = fillingResult.getResult();
            if (!fillIntoInventory(outputInv, result, false)) {
               return false;
            } else if (simulate) {
               return true;
            } else {
               fillingResult = FluidUtil.tryFillContainer(input, handler, handler.getTankCapacity(0), null, true);
               input.shrink(1);
               fillIntoInventory(outputInv, fillingResult.getResult(), true);
               return true;
            }
         }
      }
   }

   public static boolean fillContainers(ItemStack source, Player player, IFluidHandler handler) {
      if (source.isEmpty()) {
         return false;
      } else {
         IItemTransporter transporter = TransporterManager.getTransporter(player.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElse(null));
         if (transporter == null) {
            return false;
         } else {
            FluidActionResult fillingResult = FluidUtil.tryFillContainer(source, new WrapperFluidHandler(handler), handler.getTankCapacity(0), null, true);
            if (!fillingResult.isSuccess()) {
               return false;
            } else if (transporter.addItem(fillingResult.getResult(), Direction.DOWN, true) <= 0) {
               return false;
            } else {
               fillingResult = FluidUtil.tryFillContainer(source, handler, handler.getTankCapacity(0), null, true);
               source.shrink(1);
               transporter.addItem(fillingResult.getResult(), Direction.DOWN, false);
               return true;
            }
         }
      }
   }

   static boolean fillIntoInventory(IHasInventory inv, ItemStack stack, boolean doInsert) {
      return stack.isEmpty() ? false : TransporterManager.getTransporter(inv).addItem(stack, null, !doInsert) >= stack.getCount();
   }

   static boolean fillIntoInventory(IHasInventory inv, int slot, ItemStack stack, boolean doInsert) {
      if (stack.isEmpty()) {
         return false;
      } else {
         ItemStack invStack = inv.getStackInSlot(slot);
         if (invStack.isEmpty()) {
            if (doInsert) {
               inv.setStackInSlot(slot, stack);
            }

            return true;
         } else if (StackUtil.isStackEqual(invStack, stack)
            && invStack.getCount() + stack.getCount() <= Math.min(inv.getMaxStackSize(slot), stack.getMaxStackSize())) {
            if (doInsert) {
               invStack.grow(stack.getCount());
               stack.setCount(0);
               inv.setStackInSlot(slot, invStack);
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
