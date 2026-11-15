package ic2.core.platform.corehacks.mixins.client.rendering;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({BlockEntity.class})
public interface TileEntityMixin {
   @Accessor("blockState")
   void setBlockState(BlockState var1);
}
