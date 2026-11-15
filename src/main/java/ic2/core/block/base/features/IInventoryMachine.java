package ic2.core.block.base.features;

import ic2.api.tiles.IMachine;
import ic2.core.inventory.base.IHasInventory;

public interface IInventoryMachine extends IMachine {
   IHasInventory getInputInventory();

   IHasInventory getOutputInventory();
}
