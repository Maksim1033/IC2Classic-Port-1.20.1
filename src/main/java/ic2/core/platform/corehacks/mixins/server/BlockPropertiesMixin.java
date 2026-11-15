package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Properties.class})
public interface BlockPropertiesMixin {
   @Accessor("explosionResistance")
   void setResistance(float var1);
}
