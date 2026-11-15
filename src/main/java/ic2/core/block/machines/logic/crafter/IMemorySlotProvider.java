package ic2.core.block.machines.logic.crafter;

public interface IMemorySlotProvider {
   int getEnabledSlots();

   boolean isServerSided();

   CraftingList getRecipes();
}
