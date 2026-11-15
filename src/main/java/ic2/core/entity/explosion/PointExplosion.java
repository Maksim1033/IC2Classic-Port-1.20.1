package ic2.core.entity.explosion;

import ic2.core.IC2;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class PointExplosion {
   Level world;
   BlockPos position;
   Entity exploder;
   RandomSource rand;
   float power;
   float dropRate;
   float damage;
   LongSet toDestroy = new LongLinkedOpenHashSet();
   Explosion explosion;
   DamageSource source;

   public PointExplosion(Level world, Entity entity, BlockPos pos, float power, float drop, float entityDamage) {
      this.rand = world.random;
      this.world = world;
      this.exploder = entity;
      this.power = power;
      this.dropRate = drop;
      this.damage = entityDamage;
      this.position = pos.offset(pos.getX() < 0 ? -1 : 0, 0, pos.getZ() < 0 ? -1 : 0);
      this.explosion = new Explosion(
         world, entity, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), power, false, BlockInteraction.DESTROY
      );
      this.source = this.world.damageSources().explosion(this.explosion);
   }

   public void doExplosionA(Box box) {
      for (BlockPos pos : box.offset(this.position)) {
         if (!this.world.isEmptyBlock(pos) && this.power >= this.world.getBlockState(pos).getExplosionResistance(this.world, pos, this.explosion) / 50.0F) {
            this.toDestroy.add(pos.asLong());
         }
      }

      this.power *= 2.0F;
      Vec3 Vector3d = new Vec3((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ());

      for (Entity entity : this.world.getEntities(this.exploder, new AABB(this.position).inflate((double)(this.power + 1.0F)))) {
         if (!(entity instanceof ItemEntity)
            && Math.sqrt(entity.distanceToSqr((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ())) / (double)this.power
               <= 1.0) {
            double x = entity.getX() - (double)this.position.getX();
            double y = entity.getY() - (double)this.position.getY();
            double z = entity.getZ() - (double)this.position.getZ();
            double d5 = Math.sqrt(x * x + y * y + z * z);
            x /= d5;
            y /= d5;
            z /= d5;
            double d7 = (1.0 - z) * (double)this.getDensity(Vector3d, entity.getBoundingBox());
            entity.hurt(this.source, (float)((int)(((d7 * d7 + d7) / 16.0 * (double)this.power + 1.0) * (double)this.damage)));
            entity.setDeltaMovement(entity.getDeltaMovement().add(x * d7, y * d7, z * d7));
         }
      }
   }

   public float getDensity(Vec3 vec, AABB box) {
      double d0 = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
      double d1 = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
      double d2 = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
      double d3 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
      double d4 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;
      if (d0 >= 0.0 && d1 >= 0.0 && d2 >= 0.0) {
         int j2 = 0;
         int k2 = 0;

         for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
            for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
               for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
                  double d5 = box.minX + (box.maxX - box.minX) * (double)f;
                  double d6 = box.minY + (box.maxY - box.minY) * (double)f1;
                  double d7 = box.minZ + (box.maxZ - box.minZ) * (double)f2;
                  if (this.world.clip(new ClipContext(new Vec3(d5 + d3, d6, d7 + d4), vec, Block.COLLIDER, Fluid.NONE, this.exploder)).getType() == Type.MISS) {
                     j2++;
                  }

                  k2++;
               }
            }
         }

         return (float)j2 / (float)k2;
      } else {
         return 0.0F;
      }
   }

   public void doExplosionB(boolean spawnParticles) {
      this.world
         .playSound(
            null,
            this.position,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.AMBIENT,
            4.0F,
            (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F
         );
      boolean isServer = IC2.PLATFORM.isSimulating();
      MutableBlockPos pos = new MutableBlockPos();
      LongIterator var4 = this.toDestroy.iterator();

      while (var4.hasNext()) {
         long entry = (Long)var4.next();
         pos.set(entry);
         if (spawnParticles) {
            double baseX = (double)((float)pos.getX() + this.rand.nextFloat());
            double baseY = (double)((float)pos.getY() + this.rand.nextFloat());
            double baseZ = (double)((float)pos.getZ() + this.rand.nextFloat());
            double x = baseX - (double)this.position.getX();
            double y = baseY - (double)this.position.getY();
            double z = baseZ - (double)this.position.getZ();
            double d7 = Math.sqrt(x * x + y * y + z * z);
            x /= d7;
            y /= d7;
            z /= d7;
            double d8 = 0.5 / (d7 / (double)this.power + 0.1) * (double)this.rand.nextFloat() * (double)this.rand.nextFloat() + 0.30000001192092896;
            x *= d8;
            y *= d8;
            z *= d8;
            this.world
               .addParticle(
                  ParticleTypes.EXPLOSION,
                  (baseX + (double)this.position.getX() * 1.0) / 2.0,
                  (baseY + (double)this.position.getY() * 1.0) / 2.0,
                  (baseZ + (double)this.position.getZ() * 1.0) / 2.0,
                  x,
                  y,
                  z
               );
            this.world.addParticle(ParticleTypes.SMOKE, baseX, baseY, baseZ, x, y, z);
         }

         if (isServer && !this.world.isEmptyBlock(pos)) {
            BlockState state = this.world.getBlockState(pos);
            if (state.canDropFromExplosion(this.world, pos, this.explosion) && this.world.getRandom().nextFloat() < this.dropRate) {
               net.minecraft.world.level.block.Block.dropResources(state, this.world, pos, this.world.getBlockEntity(pos));
            }

            state.onBlockExploded(this.world, pos, this.explosion);
         }
      }
   }
}
