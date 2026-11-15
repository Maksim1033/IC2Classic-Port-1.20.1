package ic2.api.network.buffer;

public interface IBitLevelOverride {
   NetworkInfo.BitLevel getOverride(int var1, String var2);

   boolean hasOverride(int var1, String var2);
}
