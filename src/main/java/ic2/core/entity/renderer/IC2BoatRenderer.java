package ic2.core.entity.renderer;

import com.mojang.datafixers.util.Pair;
import ic2.core.entity.misc.IC2Boat;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public class IC2BoatRenderer extends BoatRenderer {
   public IC2BoatRenderer(Context context, boolean chest) {
      super(context, chest);
   }

   @Deprecated
   public ResourceLocation getTextureLocation(Boat entity) {
      return entity instanceof IC2Boat ? ((IC2Boat)entity).getTexture() : super.getTextureLocation(entity);
   }

   public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
      return boat instanceof IC2Boat ic2Boat
         ? Pair.of(ic2Boat.getTexture(), super.getModelWithLocation(boat).getSecond())
         : super.getModelWithLocation(boat);
   }
}
