package ic2.api.reactor.planner;

import net.minecraft.nbt.CompoundTag;

public class Tracker {
   private int total = 0;
   private int count = 0;
   private int change = 0;

   public CompoundTag save(CompoundTag tag) {
      tag.putInt("total", this.total);
      tag.putInt("count", this.count);
      tag.putInt("change", this.change);
      return tag;
   }

   public void load(CompoundTag tag) {
      this.total = tag.getInt("total");
      this.count = tag.getInt("count");
      this.change = tag.getInt("change");
   }

   public void addChange(int value) {
      this.change += value;
   }

   public void commit() {
      this.total = this.total + this.change;
      this.change = 0;
      this.count++;
   }

   public void reset() {
      this.total = 0;
      this.count = 0;
      this.change = 0;
   }

   public float getAverage() {
      return this.count == 0 ? 0.0F : (float)this.total / (float)this.count;
   }

   public int getCount() {
      return this.count;
   }

   public int getTotal() {
      return this.total;
   }
}
