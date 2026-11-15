package ic2.core.block.rendering.props;

import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraftforge.client.model.data.ModelProperty;

public class TextureProperty extends ModelProperty<ITextureProvider> {
   public static final TextureProperty INSTANCE = new TextureProperty();

   private TextureProperty() {
   }
}
