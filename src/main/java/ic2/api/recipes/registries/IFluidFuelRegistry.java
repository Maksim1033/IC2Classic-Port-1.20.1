package ic2.api.recipes.registries;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public interface IFluidFuelRegistry extends IListenableRegistry<IFluidFuelRegistry> {
   void addFuel(Fluid var1, int var2, int var3);

   void removeFuel(Fluid var1);

   List<IFluidFuelRegistry.FuelEntry> getFuels();

   IFluidFuelRegistry.FuelEntry getFuel(Fluid var1);

   public static class FuelEntry {
      final Fluid fluid;
      final int ticksPerBucket;
      final int euPerTick;

      public FuelEntry(FriendlyByteBuf buffer) {
         this.fluid = (Fluid)buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
         this.ticksPerBucket = buffer.readVarInt();
         this.euPerTick = buffer.readVarInt();
      }

      public FuelEntry(Fluid fluid, int ticksPerBucket, int euPerTick) {
         this.fluid = fluid;
         this.ticksPerBucket = ticksPerBucket;
         this.euPerTick = euPerTick;
      }

      public Fluid getFluid() {
         return this.fluid;
      }

      public int getTicksPerBucket() {
         return this.ticksPerBucket;
      }

      public int getEuPerTick() {
         return this.euPerTick;
      }

      public void write(FriendlyByteBuf buffer) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, this.fluid);
         buffer.writeVarInt(this.ticksPerBucket);
         buffer.writeVarInt(this.euPerTick);
      }
   }
}
