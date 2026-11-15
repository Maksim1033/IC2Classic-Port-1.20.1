package ic2.core.platform.registries;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;

public class IC2Paintings {
   public static final List<PaintingVariant> PAINTINGS = ObjectLists.synchronize(CollectionUtils.createList());

   public static PaintingVariant create(String textureName, int width, int height) {
      PaintingVariant type = new PaintingVariant(width, height);
      ResourceLocation location = GameData.checkPrefix(textureName, false);
      ForgeRegistries.PAINTING_VARIANTS.register(location, type);
      PAINTINGS.add(type);
      return type;
   }

   public static void init() {
      create("hazard_battery_fumes", 16, 16);
      create("hazard_bio", 16, 16);
      create("hazard_electrical", 16, 16);
      create("hazard_electromagnetic_field", 16, 16);
      create("hazard_general", 16, 16);
      create("hazard_high_temperature", 16, 16);
      create("hazard_low_temperature", 16, 16);
      create("hazard_noxious_or_irritating_materials", 16, 16);
      create("hazard_open_flame", 16, 16);
      create("hazard_radioactive", 16, 16);
      create("hazard_slippery_floor", 16, 16);
      create("hazard_strong_magnetic_field", 16, 16);
      create("hazard_suspended_load", 16, 16);
      create("hazard_toxic", 16, 16);
      create("hazard_trip", 16, 16);
   }
}
