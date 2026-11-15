package ic2.api.energy.tile;

public interface IEnergySource extends IEnergyEmitter {
   int getSourceTier();

   int getMaxEnergyOutput();

   int getProvidedEnergy();

   void consumeEnergy(int var1);

   default void onPacketFailed() {
   }

   default IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.UNKNOWN;
   }

   public static enum SourceType {
      ALWAYS_CONSUMING,
      PASSIVE_PRODUCING,
      INDIRECT_PASSIVE,
      INTELLIGENT_CONSUMING,
      DUMB_CONSUMING,
      UNKNOWN;

      public IEnergySource.SourceType merge(IEnergySource.SourceType other) {
         return this.ordinal() < other.ordinal() ? this : other;
      }

      public static IEnergySource.SourceType required(IEnergySource.SourceType required, IEnergySource.SourceType other) {
         return other.ordinal() < required.ordinal() ? required : other;
      }
   }
}
