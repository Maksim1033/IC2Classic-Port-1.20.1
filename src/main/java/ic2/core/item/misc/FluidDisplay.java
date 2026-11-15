package ic2.core.item.misc;

import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.renders.models.FluidModel;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidDisplay extends IC2Item implements ICustomItemModel, IItemColorListener {
   public FluidDisplay() {
      super("fluid_display", new PropertiesBuilder().setNoRepair().maxStackSize(1));
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      Fluid fluid = getFluid(stack);
      return fluid == Fluids.EMPTY ? -1 : IClientFluidTypeExtensions.of(fluid).getTintColor(new FluidStack(fluid, 1000));
   }

   @Override
   public List<ItemStack> getCustomTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getModel(ItemStack stack) {
      return new FluidModel();
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         for (Fluid fluid : ForgeRegistries.FLUIDS) {
            if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource()) {
               items.add(createDisplay(fluid));
            }
         }
      }
   }

   public Component getName(ItemStack stack) {
      Fluid fluid = getFluid(stack);
      return fluid == Fluids.EMPTY ? super.getName(stack) : fluid.getFluidType().getDescription(new FluidStack(fluid, 1000));
   }

   public static ItemStack createDisplay(Fluid fluid) {
      ItemStack stack = new ItemStack(IC2Items.FLUID_DISPLAY);
      setFluid(stack, fluid);
      return stack;
   }

   public static Fluid getFluid(ItemStack stack) {
      return ForgeRegistryWrapper.getFluidValue(new ResourceLocation(StackUtil.getNbtData(stack).getString("fluid")));
   }

   public static void setFluid(ItemStack stack, Fluid fluid) {
      if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource()) {
         stack.getOrCreateTag().putString("fluid", ForgeRegistryWrapper.getFluidKey(fluid).toString());
      }
   }
}
