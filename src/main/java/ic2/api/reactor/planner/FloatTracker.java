package ic2.api.reactor.planner;

public class FloatTracker {
   private float total = 0.0F;
   private int count = 0;
   private float change = 0.0F;

   public void addChange(float value) {
      this.change += value;
   }

   public void commit() {
      this.total = this.total + this.change;
      this.change = 0.0F;
      this.count++;
   }

   public void reset() {
      this.total = 0.0F;
      this.count = 0;
      this.change = 0.0F;
   }

   public float getAverage() {
      return this.count == 0 ? 0.0F : this.total / (float)this.count;
   }
}
