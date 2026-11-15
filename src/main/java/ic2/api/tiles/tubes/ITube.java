package ic2.api.tiles.tubes;

import ic2.api.util.ILocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface ITube extends ILocation {
   default void addItem(ItemStack item, Direction side) {
      this.addItem(item, side, null);
   }

   void addItem(ItemStack var1, Direction var2, DyeColor var3);

   void addItem(TransportedItem var1, Direction var2);

   boolean canAddItem(TransportedItem var1, Direction var2);

   boolean canConnect(ITube var1, Direction var2);

   ITube.TubeType getTubeType();

   public static enum TubeType {
      SIMPLE,
      EXTRACTION;
   }
}
