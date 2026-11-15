package ic2.core.inventory.handler;

public interface IHasInventoryHandler {
   InventoryHandler getInventoryHandler();

   boolean allowsUI();
}
