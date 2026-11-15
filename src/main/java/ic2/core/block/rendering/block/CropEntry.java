package ic2.core.block.rendering.block;

import ic2.api.crops.ICrop;
import java.util.Objects;

public class CropEntry {
   ICrop crop;
   int stage;

   public CropEntry(ICrop crop, int stage) {
      this.crop = crop;
      this.stage = stage;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof CropEntry entry) ? false : entry.crop == this.crop && entry.stage == this.stage;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.crop, this.stage);
   }
}
