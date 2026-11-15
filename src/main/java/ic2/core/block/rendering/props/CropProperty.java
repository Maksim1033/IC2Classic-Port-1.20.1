package ic2.core.block.rendering.props;

import ic2.core.block.rendering.block.CropEntry;
import net.minecraftforge.client.model.data.ModelProperty;

public class CropProperty extends ModelProperty<CropEntry> {
   public static final CropProperty CROP = new CropProperty();

   private CropProperty() {
   }
}
