package ic2.core.platform.registries;

import ic2.core.fluid.IC2Fluid;
import ic2.core.utils.math.ColorUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidType.Properties;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class IC2Fluids {
   public static final FluidType WOOD_GAS_TYPE = IC2Fluid.createType(
      Properties.create().density(-1000).viscosity(500).descriptionId("fluid.ic2.woodgas"), new ResourceLocation("ic2:block/fluids/woodgas_still")
   );
   public static final FluidType STEAM_TYPE = IC2Fluid.createType(
      Properties.create().density(-1000).viscosity(500).descriptionId("fluid.ic2.steam"), new ResourceLocation("ic2:block/fluids/steam_still")
   );
   public static final FluidType BIO_FUEL_TYPE = IC2Fluid.createType(
      Properties.create().density(1000).viscosity(790).descriptionId("fluid.ic2.bio_fuel"),
      new ResourceLocation("ic2:block/fluids/base_fluid_still"),
      ColorUtils.rgb(255, 111, 0)
   );
   public static final FluidType ALCOHOL_TYPE = IC2Fluid.createType(
      Properties.create().density(1000).viscosity(500).descriptionId("fluid.ic2.alcohol"),
      new ResourceLocation("ic2:block/fluids/base_fluid_still"),
      ColorUtils.rgb(157, 170, 106)
   );
   public static final FluidType WHISKY_TYPE = IC2Fluid.createType(
      Properties.create().density(1000).viscosity(790).descriptionId("fluid.ic2.unrefined_whisky"),
      new ResourceLocation("ic2:block/fluids/base_fluid_still"),
      ColorUtils.rgb(191, 159, 24)
   );
   public static final FluidType BLAZING_LAVA_TYPE = IC2Fluid.createType(
      Properties.create().density(4500).viscosity(8000).temperature(2500).descriptionId("fluid.ic2.blazing_lava"),
      new ResourceLocation("ic2:block/fluids/blazing_lava_still"),
      -1
   );
   public static Fluid WOOD_GAS = new IC2Fluid(WOOD_GAS_TYPE);
   public static Fluid STEAM = new IC2Fluid(STEAM_TYPE);
   public static Fluid BIO_FUEL = new IC2Fluid(BIO_FUEL_TYPE);
   public static Fluid ALCOHOL = new IC2Fluid(ALCOHOL_TYPE);
   public static Fluid WHISKY = new IC2Fluid(WHISKY_TYPE);
   public static Fluid BLAZING_LAVA = new IC2Fluid(BLAZING_LAVA_TYPE);

   public static void registerFluidType(String name, FluidType type) {
      ((IForgeRegistry)ForgeRegistries.FLUID_TYPES.get()).register(name, type);
   }

   public static void registerFluid(String name, Fluid fluid) {
      ForgeRegistries.FLUIDS.register(name, fluid);
   }

   public static Fluid registerForgeFluid(String name, Fluid fluid) {
      ResourceLocation location = new ResourceLocation("forge", name);
      if (ForgeRegistries.FLUIDS.containsKey(location)) {
         return (Fluid)ForgeRegistries.FLUIDS.getValue(location);
      } else {
         ForgeRegistries.FLUIDS.register(name, fluid);
         return fluid;
      }
   }

   public static void loadFluids() {
      registerFluidType("woodgas", WOOD_GAS_TYPE);
      registerFluidType("steam", STEAM_TYPE);
      registerFluidType("bio_fuel", BIO_FUEL_TYPE);
      registerFluidType("alcohol", ALCOHOL_TYPE);
      registerFluidType("whisky", WHISKY_TYPE);
      registerFluidType("blazing_lava", BLAZING_LAVA_TYPE);
      registerFluid("woodgas", WOOD_GAS);
      STEAM = registerForgeFluid("steam", STEAM);
      registerFluid("bio_fuel", BIO_FUEL);
      registerFluid("alcohol", ALCOHOL);
      registerFluid("whisky", WHISKY);
      registerFluid("blazing_lava", BLAZING_LAVA);
   }
}
