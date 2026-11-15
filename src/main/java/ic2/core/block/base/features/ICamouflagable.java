package ic2.core.block.base.features;

import ic2.api.events.RetextureEvent;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ItemLike;

public interface ICamouflagable {
   CamouflageStorage getStorage();

   boolean applyTexture(Direction var1, RetextureEvent.TextureContainer var2);

   boolean isSideEnabled(Direction var1);

   boolean removeCamouflage();

   ItemLike getCamouflageDrop();
}
