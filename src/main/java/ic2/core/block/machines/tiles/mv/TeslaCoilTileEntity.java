package ic2.core.block.machines.tiles.mv;

import org.joml.Vector3f;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.IC2DamageSource;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.energy.EnergyNetLocal;
import ic2.core.platform.registries.IC2Tiles;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;

public class TeslaCoilTileEntity extends BaseElectricTileEntity implements ITickListener {
   @NetworkInfo
   public TeslaCoilTileEntity.HitBuffer buffer = new TeslaCoilTileEntity.HitBuffer();

   public TeslaCoilTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 128, 50000);
   }

   @Override
   public boolean allowsUI() {
      return false;
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TESLA_COIL;
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      this.handleComparators();
      if (!this.isRedstonePowered()) {
         if (this.clock(32)) {
            int damage = this.energy / this.getCost();
            if (damage <= 0) {
               return;
            }

            this.useEnergy(1);
            if (this.shock(damage)) {
               this.useEnergy(damage * this.getCost());
            }
         }
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      if (fields.contains("buffer")) {
         this.buffer.applyEffects(this.getLevel());
      }

      super.onNetworkFieldChanged(fields, player);
   }

   public boolean shock(int damage) {
      this.buffer.clear();
      this.buffer.setDamage(damage);
      IC2DamageSource source = IC2DamageSource.newShockDamage(FakePlayerFactory.getMinecraft((ServerLevel)this.getLevel()));
      List<LivingEntity> list = this.getLevel()
         .getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos()).inflate(4.0), EnergyNetLocal.SHOCK_PREDICATE);

      for (int l = 0; l < list.size(); l++) {
         LivingEntity victim = list.get(l);
         victim.hurt(source, (float)damage);
         this.buffer.addEntity(victim);
      }

      if (!list.isEmpty()) {
         this.updateTileField("buffer");
         return true;
      } else {
         return false;
      }
   }

   public int getCost() {
      return 400;
   }

   public static class HitBuffer implements INetworkDataBuffer {
      int damage;
      IntList entities = new IntArrayList();

      public void clear() {
         this.damage = -1;
         this.entities.clear();
      }

      public void setDamage(int damage) {
         this.damage = damage;
      }

      public void addEntity(Entity entity) {
         if (entity != null) {
            this.entities.add(entity.getId());
         }
      }

      public void applyEffects(Level world) {
         if (this.damage != -1 && !this.entities.isEmpty()) {
            int i = 0;

            for (int m = this.entities.size(); i < m; i++) {
               Entity entity = world.getEntity(this.entities.getInt(i));
               if (entity instanceof LivingEntity) {
                  for (int x = 0; x < this.damage; x++) {
                     world.addParticle(
                        DustParticleOptions.REDSTONE,
                        entity.getX() - 0.5 + (double)world.random.nextFloat(),
                        entity.getY() + (double)(world.random.nextFloat() * 2.0F),
                        entity.getZ() - 0.5 + (double)world.random.nextFloat(),
                        0.0,
                        0.0,
                        1.0
                     );
                  }
               }
            }
         }
      }

      @Override
      public void write(IOutputBuffer buffer) {
         if (this.damage == -1) {
            buffer.writeBoolean(true);
         } else {
            buffer.writeBoolean(false);
            buffer.writeInt(this.damage);
            buffer.writeInt(this.entities.size());
            int i = 0;

            for (int m = this.entities.size(); i < m; i++) {
               buffer.writeInt(this.entities.getInt(i));
            }
         }
      }

      @Override
      public void read(IInputBuffer buffer) {
         this.clear();
         if (!buffer.readBoolean()) {
            this.damage = buffer.readInt();
            int size = buffer.readInt();

            for (int i = 0; i < size; i++) {
               this.entities.add(buffer.readInt());
            }
         }
      }
   }
}
