package ic2.core.platform.rendering.misc;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;

public class ModelTransform implements ModelState {
   ModelState transform;
   boolean uvLock;

   public ModelTransform(ModelState transform, boolean uvLock) {
      this.transform = transform;
      this.uvLock = uvLock;
   }

   public Transformation getRotation() {
      return this.transform.getRotation();
   }

   public boolean isUvLocked() {
      return this.uvLock;
   }
}
