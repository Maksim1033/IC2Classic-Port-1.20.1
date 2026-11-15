package ic2.core.block.rendering.props;

import net.minecraftforge.client.model.data.ModelProperty;

public final class CableProperty extends ModelProperty<Integer> {
   public static final CableProperty INSTANCE = new CableProperty();
   public static final CableProperty TUBE_EXTRA_SIDE = new CableProperty();
   public static final CableProperty PIPE_EXTRACTION = new CableProperty();

   private CableProperty() {
   }
}
