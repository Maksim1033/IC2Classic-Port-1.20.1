package ic2.core.platform.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class IC2Stats {
   public static final ResourceLocation DROWNED_WITH_Q_HELMET = createStat("drown_with_q_helmet", StatFormatter.DEFAULT);
   public static final ResourceLocation CABLE_SHOCK_DAMAGE = createStat("cable_shock_damage", StatFormatter.DEFAULT);
   public static final ResourceLocation NUKES_SURVIVED = createStat("nukes_survived", StatFormatter.DEFAULT);
   public static final ResourceLocation NUKES_IGNITED = createStat("nukes_ignited", StatFormatter.DEFAULT);
   public static final ResourceLocation WITHERS_NUKED = createStat("withers_nuked", StatFormatter.DEFAULT);
   public static final ResourceLocation CHAINSAW_KILLS = createStat("chainsaws_kills", StatFormatter.DEFAULT);
   public static final ResourceLocation CROPS_BREED = createStat("crops_breed", StatFormatter.DEFAULT);
   public static final ResourceLocation JETPACK_FLY_TIME = createStat("jetpack_fly_time", StatFormatter.TIME);
   public static final ResourceLocation ROCKET_MODE_USED = createStat("rocket_mode_used", StatFormatter.DEFAULT);
   public static final ResourceLocation BLOCKS_DRILLED = createStat("blocks_drilled", StatFormatter.DEFAULT);
   public static final ResourceLocation BLOCKS_SAWED = createStat("blocks_sawed", StatFormatter.DEFAULT);
   public static final ResourceLocation BLOCKS_LASERED = createStat("blocks_lasered", StatFormatter.DEFAULT);
   public static final ResourceLocation DISTANCE_TELEPORTED = createStat("distance_teleported", StatFormatter.DISTANCE);
   public static final ResourceLocation FOAM_SPRAYED = createStat("foam_sprayed", StatFormatter.DEFAULT);
   public static final ResourceLocation FOOD_CANS_EATEN = createStat("food_cans_eaten", StatFormatter.DEFAULT);
   public static final ResourceLocation AIR_CELLS_USED = createStat("air_cells_used", StatFormatter.DEFAULT);
   public static final ResourceLocation OVERGROWTH_USED = createStat("overgrowth_used", StatFormatter.DEFAULT);
   public static final ResourceLocation REVIVED_USED = createStat("revived_used", StatFormatter.DEFAULT);
   public static final ResourceLocation ELECTRIC_ENCHANTED = createStat("electric_enchanted", StatFormatter.DEFAULT);
   public static final ResourceLocation TEXTURES_STOLEN = createStat("textures_stolen", StatFormatter.DEFAULT);
   public static final ResourceLocation LUCKY_PERSON = createStat("lucky_person", StatFormatter.DEFAULT);
   public static final ResourceLocation DRAGONS_SHOT = createStat("dragons_lasered", StatFormatter.DEFAULT);
   public static final ResourceLocation PLAYERS_SHOT = createStat("laser_kills", StatFormatter.DEFAULT);
   public static final ResourceLocation CROPS_DISCOVERED = createStat("crops_discovered", StatFormatter.DEFAULT);
   public static final ResourceLocation HIGHEST_CROP_TIER = createStat("highest_crop_tier", StatFormatter.DEFAULT);
   public static final ResourceLocation HIGHEST_CROP_GROWTH = createStat("highest_crop_growth", StatFormatter.DEFAULT);
   public static final ResourceLocation HIGHEST_CROP_GAIN = createStat("highest_crop_gain", StatFormatter.DEFAULT);
   public static final ResourceLocation HIGHEST_CROP_RESISTANCE = createStat("highest_crop_resistance", StatFormatter.DEFAULT);

   public static void init() {
   }

   public static ResourceLocation createStat(String name, StatFormatter format) {
      ResourceLocation location = new ResourceLocation("ic2", name);
      Registry.register(BuiltInRegistries.CUSTOM_STAT, location, location);
      Stats.CUSTOM.get(location, format);
      return location;
   }
}
