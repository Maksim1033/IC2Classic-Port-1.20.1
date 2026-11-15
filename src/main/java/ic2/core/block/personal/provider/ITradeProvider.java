package ic2.core.block.personal.provider;

import ic2.core.inventory.base.IHasInventory;
import java.util.UUID;

public interface ITradeProvider {
   IHasInventory getTradeInput();

   IHasInventory getTradeOutput();

   UUID getTradeOwner();
}
