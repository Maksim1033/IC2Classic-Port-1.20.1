package ic2.api.network.buffer;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;

public interface IOutputBuffer {
   void writeBoolean(boolean var1);

   void writeByte(byte var1);

   void writeShort(short var1);

   void writeMedium(int var1);

   void writeInt(int var1);

   void writeVarInt(int var1);

   void writeFloat(float var1);

   void writeDouble(double var1);

   void writeLong(long var1);

   void writeData(long var1, NetworkInfo.BitLevel var3);

   void writeChar(char var1);

   void writeEnum(Enum<?> var1);

   void writeString(String var1);

   void writeBytes(byte[] var1);

   void writeNBTData(CompoundTag var1);

   <T> void writeForgeEntry(T var1, IForgeRegistry<T> var2);

   void writeItemStack(ItemStack var1);

   void writeFluidStack(FluidStack var1);

   void writeUUID(UUID var1);

   void writeRegistryKey(ResourceKey<?> var1);

   static void writeItemStack(ItemStack stack, IOutputBuffer buffer) {
      buffer.writeItemStack(stack);
   }

   static void writeEnum(Enum<?> value, IOutputBuffer buffer) {
      buffer.writeEnum(value);
   }
}
