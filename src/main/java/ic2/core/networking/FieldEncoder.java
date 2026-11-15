package ic2.core.networking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import com.mojang.authlib.GameProfile;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

public class FieldEncoder {
   public static void encode(IOutputBuffer buffer, Object obj) {
      encode(buffer, obj, NetworkInfo.BitLevel.BIT_0);
   }

   public static void encode(IOutputBuffer buffer, Object obj, NetworkInfo.BitLevel level) {
      if (obj == null) {
         buffer.writeByte((byte)-1);
      } else if (obj instanceof Number) {
         if (obj instanceof Integer) {
            buffer.writeByte((byte)0);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)((Integer)obj).intValue(), level);
            } else {
               buffer.writeInt((Integer)obj);
            }
         } else if (obj instanceof Short) {
            buffer.writeByte((byte)1);
            if (level.isValid(NetworkInfo.BitLevel.BIT_16)) {
               buffer.writeData((long)((Short)obj).shortValue(), level);
            } else {
               buffer.writeShort((Short)obj);
            }
         } else if (obj instanceof Long) {
            buffer.writeByte((byte)2);
            if (level.isValid(NetworkInfo.BitLevel.BIT_64)) {
               buffer.writeData((Long)obj, level);
            } else {
               buffer.writeLong((Long)obj);
            }
         } else if (obj instanceof Float) {
            buffer.writeByte((byte)3);
            buffer.writeFloat((Float)obj);
         } else if (obj instanceof Double) {
            buffer.writeByte((byte)4);
            buffer.writeDouble((Double)obj);
         } else if (obj instanceof Byte) {
            buffer.writeByte((byte)5);
            buffer.writeByte((Byte)obj);
         }
      } else if (obj instanceof Boolean) {
         buffer.writeByte((byte)6);
         buffer.writeBoolean((Boolean)obj);
      } else if (obj instanceof String) {
         buffer.writeByte((byte)7);
         buffer.writeString((String)obj);
      } else if (obj.getClass().isArray()) {
         if (obj instanceof int[] array) {
            buffer.writeByte((byte)8);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (int j : array) {
               buffer.writeInt(j);
            }
         } else if (obj instanceof short[] array) {
            buffer.writeByte((byte)9);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (short value : array) {
               buffer.writeShort(value);
            }
         } else if (obj instanceof long[] array) {
            buffer.writeByte((byte)10);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (long l : array) {
               buffer.writeLong(l);
            }
         } else if (obj instanceof float[] array) {
            buffer.writeByte((byte)11);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (float v : array) {
               buffer.writeFloat(v);
            }
         } else if (obj instanceof double[] array) {
            buffer.writeByte((byte)12);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (double v : array) {
               buffer.writeDouble(v);
            }
         } else if (obj instanceof byte[]) {
            buffer.writeByte((byte)13);
            buffer.writeBytes((byte[])obj);
         } else if (obj instanceof boolean[] array) {
            buffer.writeByte((byte)14);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (boolean b : array) {
               buffer.writeBoolean(b);
            }
         } else if (obj instanceof String[] array) {
            buffer.writeByte((byte)15);
            if (level.isValid(NetworkInfo.BitLevel.BIT_32)) {
               buffer.writeData((long)array.length, level);
            } else {
               buffer.writeInt(array.length);
            }

            for (String s : array) {
               buffer.writeString(s);
            }
         }
      } else if (obj instanceof INetworkDataBuffer) {
         buffer.writeByte((byte)16);

         try {
            ByteBuf buf = Unpooled.buffer();
            ((INetworkDataBuffer)obj).write(new OutputBuffer(new FriendlyByteBuf(buf)));
            byte[] data = new byte[buf.writerIndex()];
            buf.readBytes(data);
            buffer.writeBytes(data);
         } catch (Exception var21) {
            var21.printStackTrace();
         }
      } else if (obj instanceof ItemStack) {
         buffer.writeByte((byte)17);
         buffer.writeItemStack((ItemStack)obj);
      } else if (obj instanceof Item) {
         buffer.writeByte((byte)18);
         buffer.writeForgeEntry((Item)obj, ForgeRegistries.ITEMS);
      } else if (obj instanceof Block) {
         buffer.writeByte((byte)19);
         buffer.writeForgeEntry((Block)obj, ForgeRegistries.BLOCKS);
      } else if (obj instanceof Enchantment) {
         buffer.writeByte((byte)20);
         buffer.writeForgeEntry((Enchantment)obj, ForgeRegistries.ENCHANTMENTS);
      } else if (obj instanceof MobEffect) {
         buffer.writeByte((byte)21);
         buffer.writeForgeEntry((MobEffect)obj, ForgeRegistries.MOB_EFFECTS);
      } else if (obj instanceof ICrop) {
         buffer.writeByte((byte)22);
         buffer.writeString(((ICrop)obj).id().toString());
      } else if (obj instanceof CompoundTag) {
         buffer.writeByte((byte)23);
         buffer.writeNBTData((CompoundTag)obj);
      } else if (obj instanceof BlockPos pos) {
         buffer.writeByte((byte)24);
         buffer.writeLong(pos.asLong());
      } else if (obj instanceof Vec3 pos) {
         buffer.writeByte((byte)25);
         buffer.writeDouble(pos.x);
         buffer.writeDouble(pos.y);
         buffer.writeDouble(pos.z);
      } else if (obj instanceof Vec3i pos) {
         buffer.writeByte((byte)26);
         buffer.writeInt(pos.getX());
         buffer.writeInt(pos.getY());
         buffer.writeInt(pos.getZ());
      } else if (obj instanceof BlockEntity tile) {
         buffer.writeByte((byte)27);
         buffer.writeRegistryKey(tile.getLevel().dimension());
         buffer.writeLong(tile.getBlockPos().asLong());
      } else if (obj instanceof Level) {
         buffer.writeByte((byte)28);
         buffer.writeRegistryKey(((Level)obj).dimension());
      } else if (obj instanceof FluidStack) {
         buffer.writeByte((byte)29);
         buffer.writeFluidStack((FluidStack)obj);
      } else if (obj instanceof FluidTank tank) {
         buffer.writeByte((byte)30);
         boolean empty = tank.getFluid().isEmpty();
         buffer.writeBoolean(empty);
         buffer.writeInt(tank.getCapacity());
         if (!empty) {
            buffer.writeFluidStack(tank.getFluid());
         }
      } else if (obj instanceof UUID) {
         buffer.writeByte((byte)31);
         buffer.writeUUID((UUID)obj);
      } else if (obj instanceof GameProfile prof) {
         buffer.writeByte((byte)32);
         buffer.writeString(prof.getName());
         buffer.writeUUID(prof.getId());
      } else if (obj instanceof Enum) {
         buffer.writeByte((byte)33);
         buffer.writeString(((Enum)obj).name());
      } else if (obj instanceof ResourceLocation) {
         buffer.writeByte((byte)34);
         buffer.writeString(((ResourceLocation)obj).toString());
      } else if (obj instanceof DirectionList) {
         buffer.writeByte((byte)35);
         buffer.writeByte((byte)((DirectionList)obj).getCode());
      } else {
         IC2.LOGGER.info("Unknown Object: {}", obj);
      }
   }

   public static Object decode(IInputBuffer buffer) {
      return decode(buffer, NetworkInfo.BitLevel.BIT_0);
   }

   public static Object decode(IInputBuffer buffer, NetworkInfo.BitLevel level) {
      int index = buffer.readByte();
      switch (index) {
         case -1:
            return null;
         case 0:
            return level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt();
         case 1:
            return level.isValid(NetworkInfo.BitLevel.BIT_16) ? (short)((int)buffer.readData(level)) : buffer.readShort();
         case 2:
            return level.isValid(NetworkInfo.BitLevel.BIT_64) ? buffer.readData(level) : buffer.readLong();
         case 3:
            return buffer.readFloat();
         case 4:
            return buffer.readDouble();
         case 5:
            return buffer.readByte();
         case 6:
            return buffer.readBoolean();
         case 7:
            return buffer.readString();
         case 8: {
            int[] array = new int[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readInt();
            }

            return array;
         }
         case 9: {
            short[] array = new short[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readShort();
            }

            return array;
         }
         case 10: {
            long[] array = new long[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readLong();
            }

            return array;
         }
         case 11: {
            float[] array = new float[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readFloat();
            }

            return array;
         }
         case 12: {
            double[] array = new double[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readDouble();
            }

            return array;
         }
         case 13:
            return buffer.readBytes();
         case 14: {
            boolean[] array = new boolean[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readBoolean();
            }

            return array;
         }
         case 15: {
            String[] array = new String[level.isValid(NetworkInfo.BitLevel.BIT_32) ? (int)buffer.readData(level) : buffer.readInt()];

            for (int i = 0; i < array.length; i++) {
               array[i] = buffer.readString();
            }

            return array;
         }
         case 16:
            return new InputBuffer(new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer.readBytes())));
         case 17:
            return buffer.readItemStack();
         case 18:
            return buffer.readForgeRegistryEntry(ForgeRegistries.ITEMS);
         case 19:
            return buffer.readForgeRegistryEntry(ForgeRegistries.BLOCKS);
         case 20:
            return buffer.readForgeRegistryEntry(ForgeRegistries.ENCHANTMENTS);
         case 21:
            return buffer.readForgeRegistryEntry(ForgeRegistries.MOB_EFFECTS);
         case 22:
            return ICropRegistry.INSTANCE.getCrop(new ResourceLocation(buffer.readString()));
         case 23:
            return buffer.readNBTData();
         case 24:
            return BlockPos.of(buffer.readLong());
         case 25: {
            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();
            return new Vec3i((int)x, (int)y, (int)z);
         }
         case 26: {
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            return new Vec3i(x, y, z);
         }
         case 27:
            Level world = IC2.PLATFORM.getWorld(buffer.readRegistryKey(Registries.DIMENSION));
            if (world == null) {
               return null;
            }

            return world.getBlockEntity(BlockPos.of(buffer.readLong()));
         case 28:
            return IC2.PLATFORM.getWorld(buffer.readRegistryKey(Registries.DIMENSION));
         case 29:
            return buffer.readFluidStack();
         case 30:
            boolean empty = buffer.readBoolean();
            if (empty) {
               return new FluidTank(buffer.readInt());
            }

            FluidTank tank = new FluidTank(buffer.readInt());
            tank.setFluid(buffer.readFluidStack());
            return tank;
         case 31:
            return buffer.readUUID();
         case 32:
            String id = buffer.readString();
            return new GameProfile(buffer.readUUID(), id);
         case 33:
            return buffer.readString();
         case 34:
            return new ResourceLocation(buffer.readString());
         case 35:
            return DirectionList.ofNumber(buffer.readByte());
         default:
            return null;
      }
   }
}
