package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({LevelChunk.class})
public interface ChunkMixin {
   @Accessor("loaded")
   boolean isLoaded();

   @Invoker("removeBlockEntityTicker")
   void removeTileEntity(BlockPos var1);

   @Invoker("updateBlockEntityTicker")
   <T extends BlockEntity> void updateTileEntity(T var1);
}
