package ic2.api.core;

import ic2.api.network.INetworkManager;
import ic2.api.ticks.ITickScheduler;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;

public interface APIHelper {
   List<ItemStack> getFluidContainers(Fluid var1);

   FluidStack createSteam(int var1);

   ITickScheduler getTickHelper();

   INetworkManager getNetworkManager();

   INetworkManager getNetworkManager(Dist var1);
}
