package ic2.core.platform.wind;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IWindCalculation {
   void update(Level var1);

   double getWindSpeed(AABB var1, float var2, float var3);

   List<IWindStream> getStreams();
}
