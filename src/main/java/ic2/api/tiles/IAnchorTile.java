package ic2.api.tiles;

import net.minecraft.core.Direction;

public interface IAnchorTile {
   boolean hasAnchor(Direction var1);

   boolean addAnchor(Direction var1);

   boolean removeAnchor(Direction var1);
}
