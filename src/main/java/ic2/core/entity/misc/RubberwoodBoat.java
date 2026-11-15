package ic2.core.entity.misc;

import ic2.core.platform.registries.IC2Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class RubberwoodBoat extends IC2Boat {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/boat/rubberwood_boat.png");

   public RubberwoodBoat(EntityType<? extends Boat> type, Level world) {
      super(type, world);
   }

   public RubberwoodBoat(EntityType<? extends Boat> type, Level worldIn, double x, double y, double z) {
      super(type, worldIn, x, y, z);
   }

   @Override
   public Item getDropItem() {
      return IC2Items.RUBBERWOOD_BOAT;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
