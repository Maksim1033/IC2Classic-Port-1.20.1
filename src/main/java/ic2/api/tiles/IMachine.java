package ic2.api.tiles;

import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.ILocation;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;

public interface IMachine extends ILocation, IInputMachine {
   EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes();

   void onUpgradesChanged();

   int getAvailableEnergy();

   boolean useEnergy(int var1, boolean var2);

   boolean isMachineWorking();

   void setRedstoneSensitive(boolean var1);

   boolean isRedstoneSensitive();

   IItemHandler getConnectedInventory(Direction var1);

   ITube getConnectedTube(Direction var1);
}
