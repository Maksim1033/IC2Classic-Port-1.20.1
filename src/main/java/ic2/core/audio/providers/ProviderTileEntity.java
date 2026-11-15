package ic2.core.audio.providers;

import ic2.core.audio.IAudioPosition;
import ic2.core.audio.ISoundProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ProviderTileEntity implements ISoundProvider {
   BlockEntity tile;

   public ProviderTileEntity(BlockEntity tile) {
      this.tile = tile;
   }

   @Override
   public IAudioPosition getPosition() {
      return new SimplePosition(this.tile.getLevel(), this.tile.getBlockPos());
   }

   @Override
   public boolean isValid(Level world) {
      return !this.tile.isRemoved() && IAudioPosition.isInSameWorld(world, this.tile.getLevel());
   }

   @Override
   public int hashCode() {
      return this.tile.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof ProviderTileEntity ? ((ProviderTileEntity)obj).tile.equals(this.tile) : false;
   }
}
