package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import ic2.api.core.IC2Classic;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidInput implements IInput {
   Fluid fluid;
   int fluidAmount;
   int size;

   public FluidInput(JsonObject obj) {
      this.fluid = ForgeRegistryWrapper.getFluidValue(ResourceLocation.tryParse(obj.get("fluid").getAsString()));
      this.fluidAmount = obj.get("amount").getAsInt();
      this.size = obj.get("size").getAsInt();
   }

   public FluidInput(FriendlyByteBuf buffer) {
      this.fluid = (Fluid)buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
      this.fluidAmount = buffer.readInt();
      this.size = buffer.readByte();
   }

   public FluidInput(FluidStack stack) {
      this(stack.getFluid(), stack.getAmount(), 1);
   }

   public FluidInput(Fluid fluid) {
      this(fluid, 1);
   }

   public FluidInput(Fluid fluid, int size) {
      this(fluid, 1000, size);
   }

   public FluidInput(Fluid fluid, int fluidAmount, int size) {
      this.fluid = fluid;
      this.fluidAmount = fluidAmount;
      this.size = size;
   }

   @Override
   public List<ItemStack> getComponents() {
      ObjectList<ItemStack> stacks = new ObjectArrayList();

      for (ItemStack stack : IC2Classic.getHelper().getFluidContainers(this.fluid)) {
         stacks.add(stack.copy());
      }

      return ObjectLists.unmodifiable(stacks);
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
         return !fluidStack.isEmpty() && fluidStack.getFluid() == this.fluid && fluidStack.getAmount() >= this.fluidAmount;
      }
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, this.fluid);
      buffer.writeInt(this.fluidAmount);
      buffer.writeByte(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("fluid", ForgeRegistryWrapper.getFluidKey(this.fluid).toString());
      obj.addProperty("amount", this.fluidAmount);
      obj.addProperty("size", this.size);
      return obj;
   }
}
