package ic2.core.inventory.gui.feature;

import ic2.core.inventory.gui.IC2Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICloseableComponent {
   @OnlyIn(Dist.CLIENT)
   void closeComponent(IC2Screen var1);
}
