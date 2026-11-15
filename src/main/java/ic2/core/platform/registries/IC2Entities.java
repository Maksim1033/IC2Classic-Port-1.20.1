package ic2.core.platform.registries;

import ic2.core.entity.explosion.DynamiteEntity;
import ic2.core.entity.explosion.ITNTEntity;
import ic2.core.entity.explosion.NukeEntity;
import ic2.core.entity.minecarts.BatboxMinecart;
import ic2.core.entity.minecarts.ESUMinecart;
import ic2.core.entity.minecarts.ISUMinecart;
import ic2.core.entity.minecarts.MFEMinecart;
import ic2.core.entity.minecarts.MFSUMinecart;
import ic2.core.entity.minecarts.PESUMinecart;
import ic2.core.entity.misc.CarbonBoat;
import ic2.core.entity.misc.MiningLaserEntity;
import ic2.core.entity.misc.RubberBoat;
import ic2.core.entity.misc.RubberwoodBoat;
import ic2.core.entity.misc.TrackingMiningLaserEntity;
import ic2.core.entity.rockets.MiningRocketEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;

public class IC2Entities {
   public static EntityType<ITNTEntity> ITNT;
   public static EntityType<NukeEntity> NUKE;
   public static EntityType<RubberwoodBoat> RUBBERWOOD_BOAT;
   public static EntityType<RubberBoat> RUBBER_BOAT;
   public static EntityType<CarbonBoat> CARBON_BOAT;
   public static EntityType<DynamiteEntity> DYNAMITE;
   public static EntityType<MiningLaserEntity> MINING_LASER;
   public static EntityType<TrackingMiningLaserEntity> TRACKING_MINING_LASER;
   public static EntityType<MiningRocketEntity> ROCKET_MINER;
   public static EntityType<AbstractMinecart> BATBOX;
   public static EntityType<AbstractMinecart> MFE;
   public static EntityType<AbstractMinecart> MFSU;
   public static EntityType<AbstractMinecart> ESU;
   public static EntityType<AbstractMinecart> ISU;
   public static EntityType<AbstractMinecart> PESU;

   public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
      ResourceLocation id = GameData.checkPrefix(name, false);
      ForgeRegistries.ENTITY_TYPES.register(id, type);
      return type;
   }

   public static void init() {
      Builder<ITNTEntity> itnt = Builder.of(ITNTEntity::new, MobCategory.MISC);
      ITNT = register("itnt", itnt.setTrackingRange(160).setUpdateInterval(5).sized(0.98F, 0.98F).build("tnt"));
      Builder<NukeEntity> nuke = Builder.of(NukeEntity::new, MobCategory.MISC);
      NUKE = register("nuke", nuke.setTrackingRange(160).setUpdateInterval(5).sized(0.98F, 0.98F).build("tnt"));
      Builder<RubberwoodBoat> rubberwood_boat = Builder.of(RubberwoodBoat::new, MobCategory.MISC);
      RUBBERWOOD_BOAT = register("rubberwood_boat", rubberwood_boat.setTrackingRange(80).setUpdateInterval(1).sized(1.375F, 0.5625F).build("boat"));
      Builder<RubberBoat> rubber_boat = Builder.of(RubberBoat::new, MobCategory.MISC);
      RUBBER_BOAT = register("rubber_boat", rubber_boat.setTrackingRange(80).setUpdateInterval(1).sized(1.375F, 0.5625F).build("boat"));
      Builder<CarbonBoat> carbon_boat = Builder.of(CarbonBoat::new, MobCategory.MISC);
      CARBON_BOAT = register("carbon_boat", carbon_boat.setTrackingRange(80).setUpdateInterval(1).sized(1.375F, 0.5625F).build("boat"));
      Builder<DynamiteEntity> dynamite = Builder.of(DynamiteEntity::new, MobCategory.MISC);
      DYNAMITE = register("dynamite", dynamite.setTrackingRange(80).setUpdateInterval(1).sized(0.5F, 0.5F).build("tnt"));
      Builder<MiningLaserEntity> laser = Builder.of(MiningLaserEntity::new, MobCategory.MISC);
      MINING_LASER = register("mininglaser", laser.setTrackingRange(160).setUpdateInterval(1).noSummon().fireImmune().sized(0.85F, 0.85F).build("tnt"));
      Builder<TrackingMiningLaserEntity> tracking = Builder.of(TrackingMiningLaserEntity::new, MobCategory.MISC);
      TRACKING_MINING_LASER = register(
         "tracking_mininglaser", tracking.setTrackingRange(160).setUpdateInterval(1).noSummon().fireImmune().sized(0.85F, 0.85F).build("tnt")
      );
      Builder<MiningRocketEntity> rocket = Builder.of(MiningRocketEntity::new, MobCategory.MISC);
      ROCKET_MINER = register("rocket_miner", rocket.setTrackingRange(160).setUpdateInterval(5).noSummon().fireImmune().sized(0.85F, 0.85F).build("tnt"));
      Builder<AbstractMinecart> batbox = Builder.of(BatboxMinecart::new, MobCategory.MISC);
      BATBOX = register("batbox", batbox.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
      Builder<AbstractMinecart> mfe = Builder.of(MFEMinecart::new, MobCategory.MISC);
      MFE = register("mfe", mfe.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
      Builder<AbstractMinecart> mfsu = Builder.of(MFSUMinecart::new, MobCategory.MISC);
      MFSU = register("mfsu", mfsu.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
      Builder<AbstractMinecart> esu = Builder.of(ESUMinecart::new, MobCategory.MISC);
      ESU = register("esu", esu.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
      Builder<AbstractMinecart> isu = Builder.of(ISUMinecart::new, MobCategory.MISC);
      ISU = register("isu", isu.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
      Builder<AbstractMinecart> pesu = Builder.of(PESUMinecart::new, MobCategory.MISC);
      PESU = register("pesu", pesu.sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart"));
   }
}
