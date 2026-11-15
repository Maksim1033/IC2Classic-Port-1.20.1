package ic2.core.block.rendering.props;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;

public class ParticleOverride extends ModelProperty<BlockState> {
   public static final ParticleOverride INSTANCE = new ParticleOverride();

   private ParticleOverride() {
   }
}
