package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({BlockBehaviour.class})
public interface BlockMixin {
   @Accessor("properties")
   Properties getProperties();
}
