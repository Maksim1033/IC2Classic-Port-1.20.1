package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ChunkHolder.class})
public interface ChunkHolderMixin {
   @Accessor(
      value = "currentlyLoading",
      remap = false
   )
   LevelChunk getLoadingChunk();
}
