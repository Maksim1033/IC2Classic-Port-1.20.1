package ic2.core.utils.compat;

import ic2.api.tiles.readers.IActivityProvider;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.core.platform.corehacks.mixins.server.info.SpawnerMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class MobSpawnerHelper implements IActivityProvider, IProgressMachine {
   Level level;
   BlockPos pos;
   SpawnerMixin tile;

   public MobSpawnerHelper(BlockEntity tile) {
      this((SpawnerBlockEntity)tile);
   }

   public MobSpawnerHelper(SpawnerBlockEntity tile) {
      this.level = tile.getLevel();
      this.pos = tile.getBlockPos();
      this.tile = (SpawnerMixin)tile.getSpawner();
   }

   @Override
   public float getProgress() {
      return (float)(this.tile.getMaxDelay() - this.tile.getDelay());
   }

   @Override
   public float getMaxProgress() {
      return (float)this.tile.getMaxDelay();
   }

   @Override
   public boolean isActivated() {
      return this.tile.isActive(this.level, this.pos);
   }
}
