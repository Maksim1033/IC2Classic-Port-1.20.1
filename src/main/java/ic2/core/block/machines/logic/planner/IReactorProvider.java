package ic2.core.block.machines.logic.planner;

import ic2.core.inventory.base.IHasInventory;

public interface IReactorProvider {
   UserSettings getSettings();

   int getReactorSize();

   boolean isSteamReactor();

   IHasInventory getComponents();

   long getGameTime(long var1);

   static IReactorProvider dummy(int size, boolean isSteam) {
      return new IReactorProvider.DummyProvider(size, isSteam);
   }

   public static class DummyProvider implements IReactorProvider {
      int size;
      boolean isSteam;

      public DummyProvider(int size, boolean isSteam) {
         this.size = size;
         this.isSteam = isSteam;
      }

      @Override
      public UserSettings getSettings() {
         return new UserSettings();
      }

      @Override
      public int getReactorSize() {
         return this.size;
      }

      @Override
      public boolean isSteamReactor() {
         return this.isSteam;
      }

      @Override
      public IHasInventory getComponents() {
         return null;
      }

      @Override
      public long getGameTime(long tick) {
         return tick;
      }
   }
}
