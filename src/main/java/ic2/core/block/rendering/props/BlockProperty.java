package ic2.core.block.rendering.props;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.ModelProperty;

public class BlockProperty extends ModelProperty<Block> {
   public static final BlockProperty INSTANCE = new BlockProperty();

   private BlockProperty() {
   }
}
