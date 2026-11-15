package ic2.core.networking.packets.server;

import ic2.core.networking.packets.IC2Packet;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExplosionPacket extends IC2Packet {
   List<ExplosionPacket.ParticleSpawn> spawns = CollectionUtils.<ExplosionPacket.ParticleSpawn>createList();
   Int2ObjectMap<Vec3> motions = new Int2ObjectLinkedOpenHashMap();
   Vec3 origin;

   public ExplosionPacket() {
   }

   public ExplosionPacket(List<ExplosionPacket.ParticleSpawn> spawns, Int2ObjectMap<Vec3> motions, Vec3 origin) {
      this.spawns = spawns;
      this.origin = origin;
      this.motions = motions;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeDouble(this.origin.x);
      buffer.writeDouble(this.origin.y);
      buffer.writeDouble(this.origin.z);
      int max = Math.min(this.spawns.size(), 32767);
      buffer.writeShort(max);

      for (int i = 0; i < max; i++) {
         this.spawns.get(i).send(buffer);
      }

      max = Math.min(this.motions.size(), 32767);
      buffer.writeShort(max);
      int i = 0;

      for (ObjectIterator var4 = this.motions.int2ObjectEntrySet().iterator(); var4.hasNext(); i++) {
         Entry<Vec3> entry = (Entry<Vec3>)var4.next();
         if (i >= max) {
            break;
         }

         buffer.writeVarInt(entry.getIntKey());
         Vec3 vec = (Vec3)entry.getValue();
         buffer.writeDouble(vec.x);
         buffer.writeDouble(vec.y);
         buffer.writeDouble(vec.z);
      }
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      double x = buffer.readDouble();
      double y = buffer.readDouble();
      double z = buffer.readDouble();
      this.origin = new Vec3(x, y, z);
      int count = buffer.readShort();

      for (int i = 0; i < count; i++) {
         this.spawns.add(new ExplosionPacket.ParticleSpawn(buffer));
      }

      int var17 = buffer.readShort();

      for (int i = 0; i < var17; i++) {
         int id = buffer.readVarInt();
         double mX = buffer.readDouble();
         double mY = buffer.readDouble();
         double mZ = buffer.readDouble();
         this.motions.put(id, new Vec3(mX, mY, mZ));
      }
   }

   @Override
   public void handlePacket(Player source) {
      Level world = source.level();
      int i = 0;

      for (int m = this.spawns.size(); i < m; i++) {
         ExplosionPacket.ParticleSpawn spawn = this.spawns.get(i);
         BlockPos pos = spawn.getPos();
         float power = spawn.power;
         double x = (double)((float)pos.getX() + world.random.nextFloat());
         double y = (double)((float)pos.getY() + world.random.nextFloat());
         double z = (double)((float)pos.getZ() + world.random.nextFloat());
         double offsetX = x - this.origin.x;
         double offsetY = y - this.origin.y;
         double offsetZ = z - this.origin.z;
         double distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
         offsetX /= distance;
         offsetY /= distance;
         offsetZ /= distance;
         double randomOffset = 0.5 / (distance / (double)power + 0.1);
         randomOffset *= (double)(world.random.nextFloat() * world.random.nextFloat() + 0.3F);
         offsetX *= randomOffset;
         offsetY *= randomOffset;
         offsetZ *= randomOffset;
         world.addParticle(ParticleTypes.POOF, (x + this.origin.x) / 2.0, (y + this.origin.y) / 2.0, (z + this.origin.z) / 2.0, offsetX, offsetY, offsetZ);
         world.addParticle(ParticleTypes.SMOKE, x, y, z, offsetX, offsetY, offsetZ);
      }

      world.addParticle(ParticleTypes.EXPLOSION, this.origin.x, this.origin.y, this.origin.z, 1.0, 0.0, 0.0);
      ObjectIterator var24 = this.motions.int2ObjectEntrySet().iterator();

      while (var24.hasNext()) {
         Entry<Vec3> entry = (Entry<Vec3>)var24.next();
         Entity entity = world.getEntity(entry.getIntKey());
         if (entity != null) {
            entity.setDeltaMovement(((Vec3)entry.getValue()).multiply(0.01, 0.01, 0.01));
         }
      }
   }

   public static class ParticleSpawn {
      long position;
      float power;

      public ParticleSpawn(long position, float power) {
         this.position = position;
         this.power = power;
      }

      public ParticleSpawn(FriendlyByteBuf buffer) {
         this.position = buffer.readLong();
         this.power = buffer.readFloat();
      }

      public BlockPos getPos() {
         return BlockPos.of(this.position);
      }

      public void send(FriendlyByteBuf buffer) {
         buffer.writeLong(this.position);
         buffer.writeFloat(this.power);
      }
   }
}
