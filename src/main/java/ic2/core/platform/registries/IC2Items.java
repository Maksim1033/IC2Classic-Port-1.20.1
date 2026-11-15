package ic2.core.platform.registries;

import ic2.core.IC2;
import ic2.core.block.base.misc.color.IColorListener;
import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.block.misc.explosive.StickyDynamite;
import ic2.core.entity.minecarts.BatboxMinecart;
import ic2.core.entity.minecarts.ESUMinecart;
import ic2.core.entity.minecarts.ISUMinecart;
import ic2.core.entity.minecarts.MFEMinecart;
import ic2.core.entity.minecarts.MFSUMinecart;
import ic2.core.entity.minecarts.PESUMinecart;
import ic2.core.entity.misc.CarbonBoat;
import ic2.core.entity.misc.RubberBoat;
import ic2.core.entity.misc.RubberwoodBoat;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.base.VanillaTexturedItem;
import ic2.core.item.block.CableItem;
import ic2.core.item.block.FluidBlockItem;
import ic2.core.item.food_and_drink.GlassItem;
import ic2.core.item.food_and_drink.IC2Food;
import ic2.core.item.food_and_drink.IC2FoodsAndDrinks;
import ic2.core.item.food_and_drink.MugItem;
import ic2.core.item.food_and_drink.TerraWartItem;
import ic2.core.item.food_and_drink.TinCanItem;
import ic2.core.item.misc.BatteryItem;
import ic2.core.item.misc.CellItem;
import ic2.core.item.misc.CoinItem;
import ic2.core.item.misc.CropSeedItem;
import ic2.core.item.misc.DebugItem;
import ic2.core.item.misc.FluidDisplay;
import ic2.core.item.misc.FuelCanItem;
import ic2.core.item.misc.IC2BoatItem;
import ic2.core.item.misc.IC2MinecartItem;
import ic2.core.item.misc.IC2SignItem;
import ic2.core.item.misc.IconItem;
import ic2.core.item.misc.MemoryStickItem;
import ic2.core.item.misc.SUBattery;
import ic2.core.item.misc.ScrapBoxItem;
import ic2.core.item.misc.StickyResinItem;
import ic2.core.item.misc.TagBlock;
import ic2.core.item.misc.TagItem;
import ic2.core.item.misc.WindmillBladeItem;
import ic2.core.item.misc.tfbp.TerraformerBlueprintItem;
import ic2.core.item.misc.tfbp.bp.BiomeBlueprint;
import ic2.core.item.misc.tfbp.bp.ChillingBluePrint;
import ic2.core.item.misc.tfbp.bp.CultivatedBluePrint;
import ic2.core.item.misc.tfbp.bp.DesertificationBluePrint;
import ic2.core.item.misc.tfbp.bp.FlatificationBluePrint;
import ic2.core.item.misc.tfbp.bp.IrrigationBluePrint;
import ic2.core.item.misc.tfbp.bp.MushroomBluePrint;
import ic2.core.item.reactor.ReactorCondensatorItem;
import ic2.core.item.reactor.ReactorFuelCellItem;
import ic2.core.item.reactor.ReactorHeatBalancerItem;
import ic2.core.item.reactor.ReactorHeatExchangerItem;
import ic2.core.item.reactor.ReactorHeatPackItem;
import ic2.core.item.reactor.ReactorHeatPumpItem;
import ic2.core.item.reactor.ReactorHeatSpreaderItem;
import ic2.core.item.reactor.ReactorHeatStorageItem;
import ic2.core.item.reactor.ReactorHeatVentItem;
import ic2.core.item.reactor.ReactorIridiumReflectorItem;
import ic2.core.item.reactor.ReactorIsotopicUraniumRod;
import ic2.core.item.reactor.ReactorPlatingItem;
import ic2.core.item.reactor.ReactorReflectorItem;
import ic2.core.item.reactor.ReactorUraniumRod;
import ic2.core.item.reactor.base.ExchangerProperty;
import ic2.core.item.reactor.urantypes.BlazeUranium;
import ic2.core.item.reactor.urantypes.CharcoalUranium;
import ic2.core.item.reactor.urantypes.EnderUranium;
import ic2.core.item.reactor.urantypes.NetherStarUranium;
import ic2.core.item.reactor.urantypes.RedstoneUranium;
import ic2.core.item.reactor.urantypes.StandardUranium;
import ic2.core.item.tool.CopyTool;
import ic2.core.item.tool.CutterTool;
import ic2.core.item.tool.DynamiteRemoteToolItem;
import ic2.core.item.tool.EUReaderTool;
import ic2.core.item.tool.FrequencyTransmitterItem;
import ic2.core.item.tool.MinerRemoteItem;
import ic2.core.item.tool.PainterTool;
import ic2.core.item.tool.SprayerTool;
import ic2.core.item.tool.ThermometerTool;
import ic2.core.item.tool.ToolBoxTool;
import ic2.core.item.tool.TreeTapTool;
import ic2.core.item.tool.TubeTool;
import ic2.core.item.tool.WikiItem;
import ic2.core.item.tool.WrenchTool;
import ic2.core.item.tool.electric.AdvancedChainsaw;
import ic2.core.item.tool.electric.AdvancedDrill;
import ic2.core.item.tool.electric.AdvancedHoe;
import ic2.core.item.tool.electric.AdvancedTreeTap;
import ic2.core.item.tool.electric.ChainsawTool;
import ic2.core.item.tool.electric.CropAnalyzer;
import ic2.core.item.tool.electric.DrillTool;
import ic2.core.item.tool.electric.ElectricCFoamSprayerTool;
import ic2.core.item.tool.electric.ElectricHoe;
import ic2.core.item.tool.electric.ElectricTreeTap;
import ic2.core.item.tool.electric.ElectricWrenchTool;
import ic2.core.item.tool.electric.FluidScannerTool;
import ic2.core.item.tool.electric.MiningLaserTool;
import ic2.core.item.tool.electric.MowingTool;
import ic2.core.item.tool.electric.NanoSaber;
import ic2.core.item.tool.electric.ObscuratorTool;
import ic2.core.item.tool.electric.PortableTeleporter;
import ic2.core.item.tool.electric.ScannerTool;
import ic2.core.item.tool.infos.ActivityCardItem;
import ic2.core.item.tool.infos.EnergyCardItem;
import ic2.core.item.tool.infos.InventoryCardItem;
import ic2.core.item.tool.infos.LagCardItem;
import ic2.core.item.tool.infos.PlayerDetectorCardItem;
import ic2.core.item.tool.infos.ProgressCardItem;
import ic2.core.item.tool.infos.ReactorCardItem;
import ic2.core.item.tool.infos.TextCardItem;
import ic2.core.item.tool.infos.TimeCardItem;
import ic2.core.item.tool.material.ToolMaterial;
import ic2.core.item.tool.simple.AxeTool;
import ic2.core.item.tool.simple.HoeTool;
import ic2.core.item.tool.simple.PickaxeTool;
import ic2.core.item.tool.simple.ShovelTool;
import ic2.core.item.tool.simple.SwordTool;
import ic2.core.item.upgrades.PadUpgradeItem;
import ic2.core.item.upgrades.SwapperUpgrade;
import ic2.core.item.upgrades.audio.LoudnessUpgradeItem;
import ic2.core.item.upgrades.audio.MufflerUpgradeItem;
import ic2.core.item.upgrades.audio.MuteUpgradeItem;
import ic2.core.item.upgrades.energy.EfficiencyUpgradeItem;
import ic2.core.item.upgrades.energy.EnergyStorageMultiplierUpgradeItem;
import ic2.core.item.upgrades.energy.EnergyStorageUpgradeItem;
import ic2.core.item.upgrades.energy.TransformerUpgradeItem;
import ic2.core.item.upgrades.inv.ItemLimiterUpgradeItem;
import ic2.core.item.upgrades.io.fluid.FluidExportUpgrade;
import ic2.core.item.upgrades.io.fluid.FluidImportUpgrade;
import ic2.core.item.upgrades.io.fluid.SimpleFluidExportUpgrade;
import ic2.core.item.upgrades.io.fluid.SimpleFluidImportUpgrade;
import ic2.core.item.upgrades.io.item.CraftingUpgradeItem;
import ic2.core.item.upgrades.io.item.ExportUpgradeItem;
import ic2.core.item.upgrades.io.item.ImportUpgradeItem;
import ic2.core.item.upgrades.io.item.SimpleExportUpgradeItem;
import ic2.core.item.upgrades.io.item.SimpleImportUpgradeItem;
import ic2.core.item.upgrades.redstone.RedstoneInverterUpgradeItem;
import ic2.core.item.upgrades.redstone.RedstoneSensitivityUpgradeItem;
import ic2.core.item.upgrades.special.CobblegenUpgradeItem;
import ic2.core.item.upgrades.special.CreativeUpgrade;
import ic2.core.item.upgrades.special.ExpCollectorUpgradeItem;
import ic2.core.item.upgrades.special.UpgradeContainerItem;
import ic2.core.item.upgrades.speed.OverclockerUpgradeItem;
import ic2.core.item.upgrades.speed.QuantumOverclockerUpgradeItem;
import ic2.core.item.upgrades.speed.SawMillUpgrade;
import ic2.core.item.upgrades.speed.SlownessUpgradeItem;
import ic2.core.item.upgrades.swaps.ISwapper;
import ic2.core.item.wearable.armor.BronzeArmor;
import ic2.core.item.wearable.armor.CFPack;
import ic2.core.item.wearable.armor.CompositeArmor;
import ic2.core.item.wearable.armor.HazmatArmor;
import ic2.core.item.wearable.armor.electric.ElectricPackArmor;
import ic2.core.item.wearable.armor.electric.NanoSuit;
import ic2.core.item.wearable.armor.electric.NightVisionGoggles;
import ic2.core.item.wearable.armor.electric.QuantumSuit;
import ic2.core.item.wearable.armor.electric.SolarHelmet;
import ic2.core.item.wearable.armor.electric.StaticBoots;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.item.wearable.jetpacks.CompactElectricJetpack;
import ic2.core.item.wearable.jetpacks.CompactFuelJetpack;
import ic2.core.item.wearable.jetpacks.CompactedNuclearJetpack;
import ic2.core.item.wearable.jetpacks.ElectricJetpack;
import ic2.core.item.wearable.jetpacks.FuelJetpack;
import ic2.core.item.wearable.jetpacks.NuclearJetpack;
import ic2.core.item.wearable.modules.AirRefillModuleItem;
import ic2.core.item.wearable.modules.AutoFeedModuleItem;
import ic2.core.item.wearable.modules.ChargerModuleItem;
import ic2.core.item.wearable.modules.EnergyHudModuleItem;
import ic2.core.item.wearable.modules.EnergyShieldModuleItem;
import ic2.core.item.wearable.modules.ExtinguishingModuleItem;
import ic2.core.item.wearable.modules.FoodStorageModuleItem;
import ic2.core.item.wearable.modules.GoldenModuleItem;
import ic2.core.item.wearable.modules.JumpBoostModuleItem;
import ic2.core.item.wearable.modules.LavaProtectModuleItem;
import ic2.core.item.wearable.modules.LightModuleItem;
import ic2.core.item.wearable.modules.ParachuteModuleItem;
import ic2.core.item.wearable.modules.ProtectiveModuleItem;
import ic2.core.item.wearable.modules.SonarModuleItem;
import ic2.core.item.wearable.modules.SpeedBoostModuleItem;
import ic2.core.item.wearable.modules.StepAssistModuleItem;
import ic2.core.item.wearable.modules.TankStorageModule;
import ic2.core.item.wearable.modules.WaterWalkerModuleItem;
import ic2.core.item.wearable.shields.AluminiumShield;
import ic2.core.item.wearable.shields.BronzeShield;
import ic2.core.item.wearable.shields.CompositeShield;
import ic2.core.item.wearable.shields.NanoShield;
import ic2.core.platform.recipes.misc.FoodCanRegistry;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.helper.ITooltipProvider;
import java.util.List;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public class IC2Items {
   public static final List<Item> COLORABLE = CollectionUtils.<Item>createList();
   public static Item RAW_TIN;
   public static Item RAW_SILVER;
   public static Item RAW_ALUMINIUM;
   public static Item DUST_IRON;
   public static Item DUST_TIN;
   public static Item DUST_COPPER;
   public static Item DUST_SILVER;
   public static Item DUST_GOLD;
   public static Item DUST_BRONZE;
   public static Item DUST_ALUMINIUM;
   public static Item DUST_CHARCOAL;
   public static Item DUST_HYDRATED_CHARCOAL;
   public static Item DUST_COAL;
   public static Item DUST_HYDRATED_COAL;
   public static Item DUST_CLAY;
   public static Item DUST_NETHERRACK;
   public static Item DUST_OBSIDIAN;
   public static Item DUST_RARE_EARTH;
   public static Item DUST_DIAMOND;
   public static Item INGOT_TIN;
   public static Item INGOT_SILVER;
   public static Item INGOT_REFINED_IRON;
   public static Item INGOT_BRONZE;
   public static Item INGOT_POLISHED_GOLD;
   public static Item INGOT_ALUMINIUM;
   public static Item INGOT_ADVANCED_ALLOY;
   public static Item INGOT_URANIUM;
   public static Item INGOT_URANIUM_ENRICHED_BLAZE;
   public static Item INGOT_URANIUM_ENRICHED_CHARCOAL;
   public static Item INGOT_URANIUM_ENRICHED_ENDERPEARL;
   public static Item INGOT_URANIUM_ENRICHED_NETHERSTAR;
   public static Item INGOT_URANIUM_ENRICHED_REDSTONE;
   public static Item CELL_EMPTY;
   public static Item CELL_AIR;
   public static Item CELL_WATER;
   public static Item CELL_LAVA;
   public static Item CELL_BLAZING_LAVA;
   public static Item CELL_HYDRATED_COAL;
   public static Item CELL_COAL_FUEL;
   public static Item CELL_BIO;
   public static Item CELL_BIO_FUEL;
   public static Item CELL_ELECTROLYZED_WATER;
   public static Item CELL_FUEL;
   public static Item CELL_COOLANT;
   public static Item CELL_PLASMA;
   public static Item CELL_WOODGAS;
   public static Item CELL_FLUID_BIO_FUEL;
   public static Item CELL_ALCOHOL;
   public static Item EMPTY_SPLASH_POTION;
   public static Item EMPTY_LINGERING_POTION;
   public static Item CROP_STICKS;
   public static Item CROP_SEED;
   public static Item COFFEE_BEANS;
   public static Item COFFEE_POWDER;
   public static Item FERTILIZER;
   public static Item GRIN_POWDER;
   public static Item HEMP;
   public static Item HEMP_SEEDS;
   public static Item HOPS;
   public static Item WHISKY_GRIST;
   public static Item OVERGROWTH_FERTILIZER;
   public static Item RESTORING_FERTILIZER;
   public static Item TEA_LEAF;
   public static Item TERRA_WART;
   public static Item WEEDEX;
   public static Item FLOUR;
   public static Item HYDRATION_CELL;
   public static Item CHOCOLATE;
   public static Item POTATO_SLICES;
   public static Item FRIES;
   public static Item CHIPS_ROLL;
   public static Item CHEESE;
   public static Item BATTER;
   public static Item MUG;
   public static Item MUG_COFFEE;
   public static Item MUG_DARK_COFFEE;
   public static Item MUG_COLD_DARK_COFFEE;
   public static Item MUG_TEA;
   public static Item MUG_COLD_TEA;
   public static Item MUG_BLACK_TEA;
   public static Item MUG_COLD_BLACK_TEA;
   public static Item MUG_ICED_TEA;
   public static Item MUG_FRUIT_TEA;
   public static Item MUG_COLD_FRUIT_TEA;
   public static Item MUG_GREEN_TEA;
   public static Item MUG_COLD_GREEN_TEA;
   public static Item MUG_MILK;
   public static Item MUG_COCOA;
   public static Item MUG_HOT_COCOA;
   public static Item MUG_RUM;
   public static Item MUG_BEER;
   public static Item MUG_WHISKY;
   public static Item GLASS;
   public static Item GLASS_COFFEE;
   public static Item GLASS_DARK_COFFEE;
   public static Item GLASS_COLD_DARK_COFFEE;
   public static Item GLASS_TEA;
   public static Item GLASS_COLD_TEA;
   public static Item GLASS_BLACK_TEA;
   public static Item GLASS_COLD_BLACK_TEA;
   public static Item GLASS_ICED_TEA;
   public static Item GLASS_FRUIT_TEA;
   public static Item GLASS_COLD_FRUIT_TEA;
   public static Item GLASS_GREEN_TEA;
   public static Item GLASS_COLD_GREEN_TEA;
   public static Item GLASS_MILK;
   public static Item GLASS_COCOA;
   public static Item GLASS_HOT_COCOA;
   public static Item GLASS_RUM;
   public static Item GLASS_BEER;
   public static Item GLASS_WHISKY;
   public static Item TIN_CAN;
   public static Item TIN_CAN_FILLED;
   public static Item ORE_IRIDIUM;
   public static Item ORE_URANIUM_DROP;
   public static Item SCRAP;
   public static Item SCRAP_METAL;
   public static Item UUMATTER;
   public static Item COAL_BALL;
   public static Item COAL_BALL_COMPRESSED;
   public static Item PLANT_BALL;
   public static Item COAL_CHUNK;
   public static Item PLANT_BALL_COMPRESSED;
   public static Item INDUSTRIAL_DIAMOND;
   public static Item CARBON_FIBER;
   public static Item CARBON_MESH;
   public static Item CARBON_PLATE;
   public static Item PLATE_ADVANCED_ALLOY;
   public static Item PLATE_IRIDIUM;
   public static Item PLATE_DENSE_COPPER;
   public static Item PULSATING_QUARTZ;
   public static Item UPGRADE_BASE;
   public static Item OVERCLOCKER_UPGRADE;
   public static Item QUANTUM_OVERCLOCKER_UPGRADE;
   public static Item TRANSFORMER_UPGRADE;
   public static Item ENERGY_STORAGE_UPGRADE;
   public static Item EFFICIENCY_UPGRADE;
   public static Item ENERGY_STORAGE_MULTIPLIER_UPGRADE;
   public static Item REDSTONE_INVERTER_UPGRADE;
   public static Item REDSTONE_SENSITIVITY_UPGRADE;
   public static Item EXPORT_UPGRADE_SIMPLE;
   public static Item IMPORT_UPGRADE_SIMPLE;
   public static Item IMPORT_UPGRADE;
   public static Item EXPORT_UPGRADE;
   public static Item FLUID_EXPORT_UPGRADE_SIMPLE;
   public static Item FLUID_IMPORT_UPGRADE_SIMPLE;
   public static Item FLUID_IMPORT_UPGRADE;
   public static Item FLUID_EXPORT_UPGRADE;
   public static Item LOUDNESS_UPGRADE;
   public static Item MUFFLER_UPGRADE;
   public static Item MUTE_UPGRADE;
   public static Item SLOWNESS_UPGRADE;
   public static Item EXP_COLLECTOR_UPGRADE;
   public static Item CREATIVE_UPGRADE;
   public static Item COBBLEGEN_UPGRADE;
   public static Item CRAFTING_UPGRADE;
   public static Item SAWBLADE_EFFICIENT_UPGRADE;
   public static Item SAWBLADE_DURABLE_UPGRADE;
   public static Item UPGRADE_CONTAINER;
   public static Item DAMAGE_PAD_UPGRADE;
   public static Item DRAIN_PAD_UPGRADE;
   public static Item PROXIMITY_PAD_UPGRADE;
   public static Item WIDE_BAND_PAD_UPGRADE;
   public static Item ARMOR_PRIORITY_PAD_UPGRADE;
   public static Item BASIC_FIELD_PAD_UPGRADE;
   public static Item FIELD_PAD_UPGRADE;
   public static Item ADVANCED_FIELD_PAD_UPGRADE;
   public static Item ITEM_LIMITER;
   public static Item MFE_UPGRADE_KIT;
   public static Item MFSU_UPGRADE_KIT;
   public static Item ESU_UPGRADE_KIT;
   public static Item ISU_UPGRADE_KIT;
   public static Item PESU_UPGRADE_KIT;
   public static Item CRYSTAL_UPGRADE_KIT;
   public static Item LAPOTRONIC_UPGRADE_KIT;
   public static Item FISSION_UPGRADE_KIT;
   public static Item MV_BATTERY_UPGRADE;
   public static Item HV_BATTERY_UPGRADE;
   public static Item EV_BATTERY_UPGRADE;
   public static Item IV_BATTERY_UPGRADE;
   public static Item MV_CHARGE_UPGRADE;
   public static Item HV_CHARGE_UPGRADE;
   public static Item EV_CHARGE_UPGRADE;
   public static Item IV_CHARGE_UPGRADE;
   public static Item MONITOR_CARD;
   public static Item ACTIVITY_CARD;
   public static Item ENERGY_CARD;
   public static Item LAG_CARD;
   public static Item PROGRESS_CARD;
   public static Item REACTOR_CARD;
   public static Item TEXT_CARD;
   public static Item TIME_CARD;
   public static Item DETECTOR_CARD;
   public static Item INV_CARD;
   public static Item RARE_EARTH_CHUNK;
   public static Item DEAD_MAGNET;
   public static Item MAGNET;
   public static Item PLASMA_CORE;
   public static Item SCRAP_METAL_CHUNK;
   public static Item SCRAP_METAL_BLADE;
   public static Item OBSIDIAN_BLADE_RAW;
   public static Item OBSIDIAN_BLADE;
   public static Item TURBINE_BLADE;
   public static Item FUEL_CAN;
   public static Item WOODEN_ROTOR;
   public static Item WOOL_ROTOR;
   public static Item IRON_ROTOR;
   public static Item CARBON_ROTOR;
   public static Item MIXED_ALLOY_ROTOR;
   public static Item IRIDIUM_ROTOR;
   public static Item HYDRATED_COAL_COMPRESSED;
   public static Item BIO_MESH;
   public static Item BIO_BALL;
   public static Item BIO_FIBER_RAW;
   public static Item BIO_BALL_COMPRESSED;
   public static Item BIO_CHUNK;
   public static Item BIO_FLESH_BALL;
   public static Item BIO_LEATHER_RAW;
   public static Item TFBP;
   public static Item TFBP_MUSHROOM;
   public static Item TFBP_CULTIVATION;
   public static Item TFBP_CHILLING;
   public static Item TFBP_IRRIGATION;
   public static Item TFBP_DESERTIFICATION;
   public static Item TFBP_FLATIFICATOR;
   public static Item TFBP_BIOME;
   public static Item TFBP_BIOME_CULTIVATION;
   public static Item TFBP_BIOME_DESERTIFICATION;
   public static Item TFBP_BIOME_FORESTIFICATION;
   public static Item TFBP_BIOME_IRRIGATION;
   public static Item TFBP_BIOME_MUSHROOM;
   public static Item TFBP_BIOME_UNDERGROWTH;
   public static Item TFBP_BIOME_CHILLING;
   public static Item CF_PELLET;
   public static Item COIN;
   public static Item COIN_8X;
   public static Item COIN_64X;
   public static Item COIN_512X;
   public static Item COIN_4096X;
   public static Item COIN_32768X;
   public static Item RUBBER;
   public static Item STICKY_RESIN;
   public static Item CIRCUIT;
   public static Item ADVANCED_CIRCUIT;
   public static Item COMPLEX_CIRCUIT;
   public static Item AXE_BRONZE;
   public static Item HOE_BRONZE;
   public static Item PICKAXE_BRONZE;
   public static Item SHOVEL_BRONZE;
   public static Item SWORD_BRONZE;
   public static Item AXE_ALUMINIUM;
   public static Item HOE_ALUMINIUM;
   public static Item PICKAXE_ALUMINIUM;
   public static Item SHOVEL_ALUMINIUM;
   public static Item SWORD_ALUMINIUM;
   public static Item TREETAP;
   public static Item WRENCH;
   public static Item CUTTER;
   public static Item CF_SPRAYER;
   public static Item DYNAMITE_REMOTE;
   public static Item THERMOMETER;
   public static Item TUBE_TOOL;
   public static Item TUBE_LENSE;
   public static Item COPY_TOOL;
   public static Item SCANNER_OD;
   public static Item SCANNER_OV;
   public static Item SCANNER_OD_RANGED;
   public static Item SCANNER_OV_RANGED;
   public static Item SCANNER_OD_BIG_RANGED;
   public static Item SCANNER_OV_BIG_RANGED;
   public static Item SCANNER_QUARRY;
   public static Item SCANNER_FILTERED;
   public static Item SCANNER_FLUID;
   public static Item SCANNER_FLUID_FILTERED;
   public static Item TOOL_BOX;
   public static Item TOOL_BOX_CARBON;
   public static Item TOOL_BOX_IRIDIUM;
   public static Item BRONZE_HELMET;
   public static Item BRONZE_CHEST;
   public static Item BRONZE_LEGGINGS;
   public static Item BRONZE_BOOTS;
   public static Item COMPOSITE_HELMET;
   public static Item COMPOSITE_CHEST;
   public static Item COMPOSITE_LEGGINGS;
   public static Item COMPOSITE_BOOTS;
   public static Item HAZMAT_HELMET;
   public static Item HAZMAT_CHEST;
   public static Item HAZMAT_LEGGINGS;
   public static Item HAZMAT_BOOTS;
   public static Item SHIELD_BRONZE;
   public static Item SHIELD_ALUMINUM;
   public static Item SHIELD_PLATED;
   public static Item SHIELD_NANO;
   public static Item SOLAR_HELMET;
   public static Item SOLAR_HELMET_ADVANCED;
   public static Item NIGHT_VISION_HELMET;
   public static Item CF_PACK;
   public static Item STATIC_BOOTS;
   public static Item JETPACK_FUEL;
   public static Item JETPACK_FUEL_COMPACT;
   public static Item JETPACK_ELECTRIC;
   public static Item JETPACK_ELECTRIC_COMPACT;
   public static Item JETPACK_NUCLEAR;
   public static Item JETPACK_NUCLEAR_COMPACT;
   public static IC2ModularElectricArmor NANOSUIT_HELMET;
   public static IC2ModularElectricArmor NANOSUIT_CHESTPLATE;
   public static IC2ModularElectricArmor NANOSUIT_LEGGINGS;
   public static IC2ModularElectricArmor NANOSUIT_BOOTS;
   public static IC2ModularElectricArmor QUANTUM_SUIT_HELMET;
   public static IC2ModularElectricArmor QUANTUM_SUIT_CHESTPLATE;
   public static IC2ModularElectricArmor QUANTUM_SUIT_LEGGINGS;
   public static IC2ModularElectricArmor QUANTUM_SUIT_BOOTS;
   public static Item BASE_MODULE;
   public static Item AIR_REFILL_MODULE;
   public static Item AUTO_FEED_MODULE;
   public static Item CHARGER_MODULE;
   public static Item FOOD_STORAGE_MODULE;
   public static Item JUMP_BOOST_MODULE;
   public static Item FLASH_LIGHT_MODULE;
   public static Item PARACHUTE_MODULE;
   public static Item PROTECTION_MODULE;
   public static Item SPEED_MODULE;
   public static Item STEP_ASSIST_MODULE;
   public static Item WATER_WALKER_MODULE;
   public static Item TANK_MODULE;
   public static Item EXTINGUISHING_MODULE;
   public static Item LAVA_PROTECT_MODULE;
   public static Item SONAR_MODULE;
   public static Item ENERGY_HUD_MODULE;
   public static Item ENERGY_SHIELD_MODULE;
   public static Item GOLDEN_MODULE;
   public static Item DRILL;
   public static Item DRILL_DIAMOND;
   public static Item DRILL_ADVANCED;
   public static Item DRILL_ROCK_CUTTER;
   public static Item CHAINSAW;
   public static Item CHAINSAW_ADVANCED;
   public static Item ELECTRIC_WRENCH;
   public static Item PRECISION_WRENCH;
   public static Item OBSCURATOR;
   public static Item ELECTRIC_TREETAP;
   public static Item ADVANCED_TREETAP;
   public static Item ELECTRIC_HOE;
   public static Item ADVANCED_HOE;
   public static Item MOWING_TOOL;
   public static Item CROP_ANALYZER;
   public static Item ELECTRIC_SPRAYER;
   public static Item NANO_SABER;
   public static Item PORTABLE_TELEPORTER;
   public static Item MINING_LASER;
   public static Item SU_BATTERY;
   public static Item RE_BATTERY;
   public static Item ENERGY_CRYSTAL;
   public static Item LAPOTRON_CRYSTAL;
   public static Item GLOWTRONIC_CRYSTAL;
   public static Item UESC;
   public static Item PESD;
   public static Item QUANTUM_ACCUMULATOR;
   public static Item QUANTUM_ACCUMULATOR_BIG;
   public static Item BAT_PACK;
   public static Item LAP_PACK;
   public static Item QUANTUM_PACK;
   public static Item BATTERY_BELT;
   public static Item DEBUG_ITEM;
   public static Item FLUID_DISPLAY;
   public static IconItem ICON_DISPLAY;
   public static Item TAG_ITEM;
   public static Item TAG_BLOCK;
   public static Item TIN_CABLE;
   public static Item COPPER_CABLE;
   public static Item COPPER_CABLE_1X_INSULATED;
   public static Item GOLD_CABLE;
   public static Item GOLD_CABLE_1X_INSULATED;
   public static Item GOLD_CABLE_2X_INSULATED;
   public static Item BRONZE_CABLE;
   public static Item BRONZE_CABLE_1X_INSULATED;
   public static Item BRONZE_CABLE_2X_INSULATED;
   public static Item IRON_CABLE;
   public static Item IRON_CABLE_1X_INSULATED;
   public static Item IRON_CABLE_2X_INSULATED;
   public static Item IRON_CABLE_4X_INSULATED;
   public static Item ALUMINIUM_CABLE;
   public static Item ALUMINIUM_CABLE_1X_INSULATED;
   public static Item ALUMINIUM_CABLE_2X_INSULATED;
   public static Item ALUMINIUM_CABLE_4X_INSULATED;
   public static Item ALUMINIUM_CABLE_8X_INSULATED;
   public static Item GLASSFIBER_CABLE;
   public static Item SUPER_CABLE;
   public static Item PLASMA_CABLE;
   public static Item DETECTOR_CABLE;
   public static Item SPLITTER_CABLE;
   public static Item COMPARING_CABLE;
   public static Item UNCOMPRESSED_SUPER_CABLE;
   public static Item PAINTER;
   public static Item PAINTER_BLACK;
   public static Item PAINTER_BLUE;
   public static Item PAINTER_BROWN;
   public static Item PAINTER_CYAN;
   public static Item PAINTER_GRAY;
   public static Item PAINTER_GREEN;
   public static Item PAINTER_LIGHT_BLUE;
   public static Item PAINTER_LIGHT_GRAY;
   public static Item PAINTER_LIME;
   public static Item PAINTER_MAGENTA;
   public static Item PAINTER_ORANGE;
   public static Item PAINTER_PINK;
   public static Item PAINTER_PURPLE;
   public static Item PAINTER_RED;
   public static Item PAINTER_WHITE;
   public static Item PAINTER_YELLOW;
   public static Item UNPRINTED_WIKI;
   public static Item WIKI_ITEM;
   public static Item EU_READER;
   public static Item FREQUENCY_TRANSMITTER;
   public static Item MINER_REMOTE;
   public static Item MEMORY_STICK;
   public static Item VENT_HEAT;
   public static Item VENT_HEAT_CORE;
   public static Item VENT_HEAT_OVERCLOCKED;
   public static Item VENT_HEAT_ADVANCED;
   public static Item VENT_STEAM;
   public static Item VENT_STEAM_CORE;
   public static Item VENT_STEAM_OVERCLOCKED;
   public static Item VENT_STEAM_ADVANCED;
   public static Item VENT_ELECTRIC;
   public static Item VENT_ELECTRIC_CORE;
   public static Item VENT_ELECTRIC_OVERCLOCKED;
   public static Item VENT_ELECTRIC_ADVANCED;
   public static Item HEAT_EXCHANGER;
   public static Item HEAT_EXCHANGER_CORE;
   public static Item HEAT_EXCHANGER_SPREAD;
   public static Item HEAT_EXCHANGER_ADVANCED;
   public static Item HEAT_EXCHANGER_BIG;
   public static Item HEAT_BALANCER;
   public static Item HEAT_BALANCER_CORE;
   public static Item HEAT_BALANCER_SPREAD;
   public static Item HEAT_BALANCER_ADVANCED;
   public static Item HEAT_PUMP;
   public static Item COOLANT_CELL_10K;
   public static Item COOLANT_CELL_30K;
   public static Item COOLANT_CELL_60K;
   public static Item CONDENSATOR;
   public static Item CONDENSATOR_LAP;
   public static Item HEAT_PACK;
   public static Item PLATING;
   public static Item PLATING_HEAT_CAPACITY;
   public static Item PLATING_CONTAINMENT;
   public static Item HEAT_SPREADER;
   public static Item REFLECTOR;
   public static Item REFLECTOR_THICK;
   public static Item REFLECTOR_IRIDIUM;
   public static Item REACTOR_FUEL_CELL;
   public static Item URANIUM_ROD_NEAR_DEPLETED;
   public static Item URANIUM_ROD_NEAR_DEPLETED_REDSTONE;
   public static Item URANIUM_ROD_NEAR_DEPLETED_BLAZE;
   public static Item URANIUM_ROD_NEAR_DEPLETED_ENDER_PEARL;
   public static Item URANIUM_ROD_NEAR_DEPLETED_NETHER_STAR;
   public static Item URANIUM_ROD_NEAR_DEPLETED_CHARCOAL;
   public static Item URANIUM_ROD_RE_ENRICHED;
   public static Item URANIUM_ROD_RE_ENRICHED_REDSTONE;
   public static Item URANIUM_ROD_RE_ENRICHED_BLAZE;
   public static Item URANIUM_ROD_RE_ENRICHED_ENDER_PEARL;
   public static Item URANIUM_ROD_RE_ENRICHED_NETHER_STAR;
   public static Item URANIUM_ROD_RE_ENRICHED_CHARCOAL;
   public static Item URANIUM_ROD_SINGLE;
   public static Item URANIUM_ROD_REDSTONE_SINGLE;
   public static Item URANIUM_ROD_BLAZE_SINGLE;
   public static Item URANIUM_ROD_ENDER_PEARL_SINGLE;
   public static Item URANIUM_ROD_NETHER_STAR_SINGLE;
   public static Item URANIUM_ROD_CHARCOAL_SINGLE;
   public static Item URANIUM_ROD_DUAL;
   public static Item URANIUM_ROD_REDSTONE_DUAL;
   public static Item URANIUM_ROD_BLAZE_DUAL;
   public static Item URANIUM_ROD_ENDER_PEARL_DUAL;
   public static Item URANIUM_ROD_NETHER_STAR_DUAL;
   public static Item URANIUM_ROD_CHARCOAL_DUAL;
   public static Item URANIUM_ROD_QUAD;
   public static Item URANIUM_ROD_REDSTONE_QUAD;
   public static Item URANIUM_ROD_BLAZE_QUAD;
   public static Item URANIUM_ROD_ENDER_PEARL_QUAD;
   public static Item URANIUM_ROD_NETHER_STAR_QUAD;
   public static Item URANIUM_ROD_CHARCOAL_QUAD;
   public static Item URANIUM_ROD_ISOTOPIC;
   public static Item URANIUM_ROD_ISOTOPIC_REDSTONE;
   public static Item URANIUM_ROD_ISOTOPIC_BLAZE;
   public static Item URANIUM_ROD_ISOTOPIC_ENDER_PEARL;
   public static Item URANIUM_ROD_ISOTOPIC_NETHER_STAR;
   public static Item URANIUM_ROD_ISOTOPIC_CHARCOAL;
   public static Item RUBBERWOOD_SIGN;
   public static Item RUBBERWOOD_BOAT;
   public static Item RUBBER_BOAT;
   public static Item RUBBER_BOAT_BROKEN;
   public static Item CARBON_BOAT;
   public static Item STICKY_DYNAMITE;
   public static Item SCRAPBOX;
   public static Item BATBOX_MINECART;
   public static Item MFE_MINECART;
   public static Item MFSU_MINECART;
   public static Item ESU_MINECART;
   public static Item ISU_MINECART;
   public static Item PESU_MINECART;
   public static Item COPPER_SIGN_ITEM;
   public static Item SILVER_SIGN_ITEM;
   public static Item ALUMINIUM_SIGN_ITEM;
   public static Item TIN_SIGN_ITEM;
   public static Item BRONZE_SIGN_ITEM;
   public static Item REFINED_IRON_SIGN_ITEM;

   public static <T extends Item> T registerItem(T item, ResourceLocation id) {
      ForgeRegistries.ITEMS.register(id, item);
      if (item instanceof IColorListener && ((IColorListener)item).needsColoring()) {
         COLORABLE.add(item);
      }

      return item;
   }

   public static <T extends Item & IRegistryProvider> T registerItem(T item) {
      return registerItem(item, item.getRegistryName());
   }

   public static void addItems() {
      addSimpleItems();
      addFoodAndDrinks();
      addOtherItems();
      addTools();
      addArmor();
      addCells();
      addCableItems();
      addUpgrades();
      addReactorComponents();
      DEBUG_ITEM = registerItem(new DebugItem("debug_item"));
      FLUID_DISPLAY = registerItem(new FluidDisplay());
      ICON_DISPLAY = registerItem(new IconItem());
      TAG_ITEM = registerItem(new TagItem());
      TAG_BLOCK = registerItem(new TagBlock());
   }

   static void addUpgrades() {
      UPGRADE_BASE = registerItem(new IC2SimpleItem("upgrade_base", "upgrades/machines", "base"));
      OVERCLOCKER_UPGRADE = registerItem(new OverclockerUpgradeItem());
      QUANTUM_OVERCLOCKER_UPGRADE = registerItem(new QuantumOverclockerUpgradeItem());
      TRANSFORMER_UPGRADE = registerItem(new TransformerUpgradeItem());
      ENERGY_STORAGE_UPGRADE = registerItem(new EnergyStorageUpgradeItem());
      EFFICIENCY_UPGRADE = registerItem(new EfficiencyUpgradeItem());
      ENERGY_STORAGE_MULTIPLIER_UPGRADE = registerItem(new EnergyStorageMultiplierUpgradeItem());
      REDSTONE_INVERTER_UPGRADE = registerItem(new RedstoneInverterUpgradeItem());
      REDSTONE_SENSITIVITY_UPGRADE = registerItem(new RedstoneSensitivityUpgradeItem());
      EXPORT_UPGRADE_SIMPLE = registerItem(new SimpleExportUpgradeItem());
      IMPORT_UPGRADE_SIMPLE = registerItem(new SimpleImportUpgradeItem());
      IMPORT_UPGRADE = registerItem(new ImportUpgradeItem());
      EXPORT_UPGRADE = registerItem(new ExportUpgradeItem());
      FLUID_EXPORT_UPGRADE_SIMPLE = registerItem(new SimpleFluidExportUpgrade());
      FLUID_IMPORT_UPGRADE_SIMPLE = registerItem(new SimpleFluidImportUpgrade());
      FLUID_IMPORT_UPGRADE = registerItem(new FluidImportUpgrade());
      FLUID_EXPORT_UPGRADE = registerItem(new FluidExportUpgrade());
      LOUDNESS_UPGRADE = registerItem(new LoudnessUpgradeItem());
      MUFFLER_UPGRADE = registerItem(new MufflerUpgradeItem());
      MUTE_UPGRADE = registerItem(new MuteUpgradeItem());
      SLOWNESS_UPGRADE = registerItem(new SlownessUpgradeItem());
      EXP_COLLECTOR_UPGRADE = registerItem(new ExpCollectorUpgradeItem());
      CREATIVE_UPGRADE = registerItem(new CreativeUpgrade());
      COBBLEGEN_UPGRADE = registerItem(new CobblegenUpgradeItem());
      CRAFTING_UPGRADE = registerItem(new CraftingUpgradeItem());
      SAWBLADE_EFFICIENT_UPGRADE = registerItem(new SawMillUpgrade("efficient_sawblade", 1000, 4, 1, 2));
      SAWBLADE_DURABLE_UPGRADE = registerItem(new SawMillUpgrade("durable_sawblade", 10000, 2, 2, 4));
      UPGRADE_CONTAINER = registerItem(new UpgradeContainerItem());
      DAMAGE_PAD_UPGRADE = registerItem(new PadUpgradeItem("damage_conversion_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.DAMAGE, "damage_conversion"));
      DRAIN_PAD_UPGRADE = registerItem(new PadUpgradeItem("drain_conversion_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.DRAIN, "drain_conversion"));
      PROXIMITY_PAD_UPGRADE = registerItem(
         new PadUpgradeItem("proximity_booster_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.PROXIMITY, "proximity_booster")
      );
      WIDE_BAND_PAD_UPGRADE = registerItem(
         new PadUpgradeItem("wide_band_booster_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.WIDE_BAND, "wide_band_booster")
      );
      ARMOR_PRIORITY_PAD_UPGRADE = registerItem(
         new PadUpgradeItem("armor_priority_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.ARMOR_PRIORITY, "armor_priority")
      );
      BASIC_FIELD_PAD_UPGRADE = registerItem(
         new PadUpgradeItem("basic_field_expansion_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.FIELD_MK1, "basic_field_expansion")
      );
      FIELD_PAD_UPGRADE = registerItem(new PadUpgradeItem("field_expansion_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.FIELD_MK2, "field_expansion"));
      ADVANCED_FIELD_PAD_UPGRADE = registerItem(
         new PadUpgradeItem("advanced_field_expansion_pad_upgrade", BaseChargePadTileEntity.PadUpgrade.FIELD_MK3, "advanced_field_expansion")
      );
      ITEM_LIMITER = registerItem(new ItemLimiterUpgradeItem());
      MFE_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("mfe_upgrade_kit", "upgrades/kits/storage", "mfe", IC2Blocks.BATBOX, IC2Blocks.MFE, ISwapper.energyStorage())
      );
      MFSU_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("mfsu_upgrade_kit", "upgrades/kits/storage", "mfsu", IC2Blocks.MFE, IC2Blocks.MFSU, ISwapper.energyStorage())
      );
      ESU_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("esu_upgrade_kit", "upgrades/kits/storage", "esu", IC2Blocks.MFSU, IC2Blocks.ESU, ISwapper.energyStorage())
      );
      ISU_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("isu_upgrade_kit", "upgrades/kits/storage", "isu", IC2Blocks.ESU, IC2Blocks.ISU, ISwapper.energyStorage())
      );
      PESU_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("pesu_upgrade_kit", "upgrades/kits/storage", "pesu", IC2Blocks.ISU, IC2Blocks.PESU, ISwapper.energyStorage())
      );
      CRYSTAL_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("crystal_upgrade_kit", "upgrades/kits", "crystalizor", IC2Blocks.LV_CHARGEPAD, IC2Blocks.MV_CHARGEPAD, ISwapper.chargePad())
      );
      LAPOTRONIC_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("lapotronic_upgrade_kit", "upgrades/kits", "lapotronic", IC2Blocks.MV_CHARGEPAD, IC2Blocks.HV_CHARGEPAD, ISwapper.chargePad())
      );
      FISSION_UPGRADE_KIT = registerItem(
         new SwapperUpgrade("fission_upgrade_kit", "upgrades/kits", "fission", IC2Blocks.HV_CHARGEPAD, IC2Blocks.FISSION_CHARGEPAD, ISwapper.chargePad())
      );
      MV_BATTERY_UPGRADE = registerItem(
         new SwapperUpgrade(
            "mv_battery_upgrade_kit", "upgrades/kits/battery", "mv", IC2Blocks.BATTERY_STATION_LV, IC2Blocks.BATTERY_STATION_MV, ISwapper.batteryStation()
         )
      );
      HV_BATTERY_UPGRADE = registerItem(
         new SwapperUpgrade(
            "hv_battery_upgrade_kit", "upgrades/kits/battery", "hv", IC2Blocks.BATTERY_STATION_MV, IC2Blocks.BATTERY_STATION_HV, ISwapper.batteryStation()
         )
      );
      EV_BATTERY_UPGRADE = registerItem(
         new SwapperUpgrade(
            "ev_battery_upgrade_kit", "upgrades/kits/battery", "ev", IC2Blocks.BATTERY_STATION_HV, IC2Blocks.BATTERY_STATION_EV, ISwapper.batteryStation()
         )
      );
      IV_BATTERY_UPGRADE = registerItem(
         new SwapperUpgrade(
            "iv_battery_upgrade_kit", "upgrades/kits/battery", "iv", IC2Blocks.BATTERY_STATION_EV, IC2Blocks.BATTERY_STATION_IV, ISwapper.batteryStation()
         )
      );
      MV_CHARGE_UPGRADE = registerItem(
         new SwapperUpgrade(
            "mv_charge_upgrade_kit", "upgrades/kits/charging", "mv", IC2Blocks.CHARGING_BENCH_LV, IC2Blocks.CHARGING_BENCH_MV, ISwapper.chargeBench()
         )
      );
      HV_CHARGE_UPGRADE = registerItem(
         new SwapperUpgrade(
            "hv_charge_upgrade_kit", "upgrades/kits/charging", "hv", IC2Blocks.CHARGING_BENCH_MV, IC2Blocks.CHARGING_BENCH_HV, ISwapper.chargeBench()
         )
      );
      EV_CHARGE_UPGRADE = registerItem(
         new SwapperUpgrade(
            "ev_charge_upgrade_kit", "upgrades/kits/charging", "ev", IC2Blocks.CHARGING_BENCH_HV, IC2Blocks.CHARGING_BENCH_EV, ISwapper.chargeBench()
         )
      );
      IV_CHARGE_UPGRADE = registerItem(
         new SwapperUpgrade(
            "iv_charge_upgrade_kit", "upgrades/kits/charging", "iv", IC2Blocks.CHARGING_BENCH_EV, IC2Blocks.CHARGING_BENCH_IV, ISwapper.chargeBench()
         )
      );
      MONITOR_CARD = registerItem(new IC2SimpleItem("monitor_card", "tools/monitor", "inactive_link_card"));
      ENERGY_CARD = registerItem(new EnergyCardItem());
      PROGRESS_CARD = registerItem(new ProgressCardItem());
      ACTIVITY_CARD = registerItem(new ActivityCardItem());
      TIME_CARD = registerItem(new TimeCardItem());
      DETECTOR_CARD = registerItem(new PlayerDetectorCardItem());
      LAG_CARD = registerItem(new LagCardItem());
      REACTOR_CARD = registerItem(new ReactorCardItem());
      TEXT_CARD = registerItem(new TextCardItem());
      INV_CARD = registerItem(new InventoryCardItem());
   }

   static void addCableItems() {
      TIN_CABLE = registerItem(new CableItem("tin_cable_item", IC2Blocks.TIN_CABLE.withInsulation(0), "tin"));
      COPPER_CABLE = registerItem(new CableItem("copper_cable_item", IC2Blocks.COPPER_CABLE.withInsulation(0), "copper"));
      COPPER_CABLE_1X_INSULATED = registerItem(new CableItem("copper_insulated_cable_item", IC2Blocks.COPPER_CABLE.withInsulation(1), "insulated_copper"));
      GOLD_CABLE = registerItem(new CableItem("gold_cable_item", IC2Blocks.GOLD_CABLE.withInsulation(0), "gold"));
      GOLD_CABLE_1X_INSULATED = registerItem(new CableItem("gold_insulated_cable_item", IC2Blocks.GOLD_CABLE.withInsulation(1), "insulated_gold"));
      GOLD_CABLE_2X_INSULATED = registerItem(new CableItem("gold_double_insulated_cable_item", IC2Blocks.GOLD_CABLE.withInsulation(2), "double_insulated_gold"));
      BRONZE_CABLE = registerItem(new CableItem("bronze_cable_item", IC2Blocks.BRONZE_CABLE.withInsulation(0), "bronze"));
      BRONZE_CABLE_1X_INSULATED = registerItem(new CableItem("bronze_insulated_cable_item", IC2Blocks.BRONZE_CABLE.withInsulation(1), "insulated_bronze"));
      BRONZE_CABLE_2X_INSULATED = registerItem(
         new CableItem("bronze_double_insulated_cable_item", IC2Blocks.BRONZE_CABLE.withInsulation(2), "double_insulated_bronze")
      );
      IRON_CABLE = registerItem(new CableItem("iron_cable_item", IC2Blocks.IRON_CABLE.withInsulation(0), "iron"));
      IRON_CABLE_1X_INSULATED = registerItem(new CableItem("iron_insulated_cable_item", IC2Blocks.IRON_CABLE.withInsulation(1), "insulated_iron"));
      IRON_CABLE_2X_INSULATED = registerItem(new CableItem("iron_double_insulated_cable_item", IC2Blocks.IRON_CABLE.withInsulation(2), "double_insulated_iron"));
      IRON_CABLE_4X_INSULATED = registerItem(new CableItem("iron_triple_insulated_cable_item", IC2Blocks.IRON_CABLE.withInsulation(3), "triple_insulated_iron"));
      ALUMINIUM_CABLE = registerItem(new CableItem("aluminium_cable_item", IC2Blocks.ALUMINUM_CABLE.withInsulation(0), "aluminium"));
      ALUMINIUM_CABLE_1X_INSULATED = registerItem(
         new CableItem("aluminium_insulated_cable_item", IC2Blocks.ALUMINUM_CABLE.withInsulation(1), "insulated_aluminium")
      );
      ALUMINIUM_CABLE_2X_INSULATED = registerItem(
         new CableItem("aluminium_double_insulated_cable_item", IC2Blocks.ALUMINUM_CABLE.withInsulation(2), "double_insulated_aluminium")
      );
      ALUMINIUM_CABLE_4X_INSULATED = registerItem(
         new CableItem("aluminium_triple_insulated_cable_item", IC2Blocks.ALUMINUM_CABLE.withInsulation(3), "triple_insulated_aluminium")
      );
      ALUMINIUM_CABLE_8X_INSULATED = registerItem(
         new CableItem("aluminium_quadruple_insulated_cable_item", IC2Blocks.ALUMINUM_CABLE.withInsulation(4), "quadruple_insulated_aluminium")
      );
      GLASSFIBER_CABLE = registerItem(new CableItem("glassfiber_cable_item", IC2Blocks.GLASS_CABLE.withInsulation(0), "glassfiber"));
      SUPER_CABLE = registerItem(new CableItem("super_cable_item", IC2Blocks.SUPER_CABLE.withInsulation(0), "super"));
      PLASMA_CABLE = registerItem(new CableItem("plasma_cable_item", IC2Blocks.PLASMA_CABLE.withInsulation(0), "plasma"));
      DETECTOR_CABLE = registerItem(new CableItem("detector_cable_item", IC2Blocks.DETECTOR_CABLE.withActivity(false), "detector"));
      SPLITTER_CABLE = registerItem(new CableItem("splitter_cable_item", IC2Blocks.SPLITTER_CABLE.withActivity(false), "splitter"));
      COMPARING_CABLE = registerItem(new CableItem("comparing_cable_item", IC2Blocks.COMPARING_CABLE.withActivity(false), "comparing"));
      UNCOMPRESSED_SUPER_CABLE = registerItem(new IC2SimpleItem("uncompressed_super_cable", "misc", "uncompressed_super_cable"));
   }

   static void addCells() {
      CELL_EMPTY = registerItem(new CellItem("cell_empty", "empty", Fluids.EMPTY));
      CELL_WATER = registerItem(new CellItem("cell_water", "water", Fluids.WATER));
      CELL_LAVA = registerItem(new CellItem("cell_lava", "lava", Fluids.LAVA));
      CELL_BLAZING_LAVA = registerItem(new CellItem("cell_blazing_lava", "blazing_lava", IC2Fluids.BLAZING_LAVA));
      CELL_WOODGAS = registerItem(new CellItem("cell_woodgas", "woodgas", IC2Fluids.WOOD_GAS));
      CELL_FLUID_BIO_FUEL = registerItem(new CellItem("cell_fluid_bio_fuel", "fluid_bio_fuel", IC2Fluids.BIO_FUEL));
      CELL_ALCOHOL = registerItem(new CellItem("cell_fluid_alcohol", "alcohol", IC2Fluids.ALCOHOL));
      CELL_AIR = registerItem(new IC2SimpleItem("cell_air", new PropertiesBuilder().containerItem(CELL_EMPTY), "cells", "air"));
      CELL_HYDRATED_COAL = registerItem(new IC2SimpleItem("cell_coal", "cells", "coal"));
      CELL_COAL_FUEL = registerItem(new IC2SimpleItem("cell_coal_fuel", new PropertiesBuilder().containerItem(CELL_EMPTY), "cells", "coal_fuel"));
      CELL_BIO = registerItem(new IC2SimpleItem("cell_bio", "cells", "bio"));
      CELL_BIO_FUEL = registerItem(new IC2SimpleItem("cell_bio_fuel", new PropertiesBuilder().containerItem(CELL_EMPTY), "cells", "bio_fuel"));
      CELL_ELECTROLYZED_WATER = registerItem(new IC2SimpleItem("cell_electrolyzed_water", "cells", "electrolyzed_water"));
      CELL_FUEL = registerItem(new IC2SimpleItem("cell_fuel", "cells", "fuel"));
      CELL_PLASMA = registerItem(new IC2SimpleItem("cell_plasma", "cells", "plasma"));
      EMPTY_SPLASH_POTION = registerItem(new VanillaTexturedItem("empty_splash_potion", new ResourceLocation("minecraft", "item/splash_potion")));
      EMPTY_LINGERING_POTION = registerItem(new VanillaTexturedItem("empty_lingering_potion", new ResourceLocation("minecraft", "item/lingering_potion")));
      CellItem.addCauldronInteraction(CELL_WATER, SoundEvents.BUCKET_FILL, T -> (Integer)T.getValue(LayeredCauldronBlock.LEVEL) == 3, CauldronInteraction.WATER);
      CellItem.addCauldronInteraction(CELL_LAVA, SoundEvents.BUCKET_FILL_LAVA, T -> true, CauldronInteraction.LAVA);
   }

   static void addTools() {
      AXE_BRONZE = registerItem(new AxeTool("tool_axe_bronze", "tools/bronze", "axe", ToolMaterial.BRONZE));
      HOE_BRONZE = registerItem(new HoeTool("tool_hoe_bronze", "tools/bronze", "hoe", ToolMaterial.BRONZE));
      PICKAXE_BRONZE = registerItem(new PickaxeTool("tool_pickaxe_bronze", "tools/bronze", "pickaxe", ToolMaterial.BRONZE));
      SHOVEL_BRONZE = registerItem(new ShovelTool("tool_shovel_bronze", "tools/bronze", "shovel", ToolMaterial.BRONZE));
      SWORD_BRONZE = registerItem(new SwordTool("tool_sword_bronze", "tools/bronze", "sword", ToolMaterial.BRONZE));
      AXE_ALUMINIUM = registerItem(new AxeTool("tool_axe_aluminium", "tools/aluminium", "axe", ToolMaterial.ALUMINIUM));
      HOE_ALUMINIUM = registerItem(new HoeTool("tool_hoe_aluminium", "tools/aluminium", "hoe", ToolMaterial.ALUMINIUM));
      PICKAXE_ALUMINIUM = registerItem(new PickaxeTool("tool_pickaxe_aluminium", "tools/aluminium", "pickaxe", ToolMaterial.ALUMINIUM));
      SHOVEL_ALUMINIUM = registerItem(new ShovelTool("tool_shovel_aluminium", "tools/aluminium", "shovel", ToolMaterial.ALUMINIUM));
      SWORD_ALUMINIUM = registerItem(new SwordTool("tool_sword_aluminium", "tools/aluminium", "sword", ToolMaterial.ALUMINIUM));
      TREETAP = registerItem(new TreeTapTool());
      WRENCH = registerItem(new WrenchTool());
      CUTTER = registerItem(new CutterTool());
      CF_SPRAYER = registerItem(new SprayerTool());
      DYNAMITE_REMOTE = registerItem(new DynamiteRemoteToolItem());
      THERMOMETER = registerItem(new ThermometerTool());
      DRILL = registerItem(new DrillTool());
      DRILL_DIAMOND = registerItem(new DrillTool.DiamondDrillTool());
      DRILL_ADVANCED = registerItem(new AdvancedDrill());
      DRILL_ROCK_CUTTER = registerItem(new DrillTool.RockCutterDrillTool());
      CHAINSAW = registerItem(new ChainsawTool());
      CHAINSAW_ADVANCED = registerItem(new AdvancedChainsaw());
      ELECTRIC_WRENCH = registerItem(new ElectricWrenchTool());
      PRECISION_WRENCH = registerItem(new ElectricWrenchTool.PrecisionWrenchTool());
      OBSCURATOR = registerItem(new ObscuratorTool());
      ELECTRIC_SPRAYER = registerItem(new ElectricCFoamSprayerTool());
      ELECTRIC_TREETAP = registerItem(new ElectricTreeTap());
      ADVANCED_TREETAP = registerItem(new AdvancedTreeTap());
      ELECTRIC_HOE = registerItem(new ElectricHoe());
      ADVANCED_HOE = registerItem(new AdvancedHoe());
      MOWING_TOOL = registerItem(new MowingTool());
      CROP_ANALYZER = registerItem(new CropAnalyzer());
      NANO_SABER = registerItem(new NanoSaber());
      PORTABLE_TELEPORTER = registerItem(new PortableTeleporter());
      MINING_LASER = registerItem(new MiningLaserTool());
      TUBE_TOOL = registerItem(new TubeTool());
      TUBE_LENSE = registerItem(new IC2SimpleItem("tube_lense", new PropertiesBuilder().maxStackSize(1), "tools", "tube_lense"));
      COPY_TOOL = registerItem(new CopyTool());
      SCANNER_OD = registerItem(new ScannerTool.ODScanner());
      SCANNER_OV = registerItem(new ScannerTool.OVScanner());
      SCANNER_OD_RANGED = registerItem(new ScannerTool.ODScannerRanged());
      SCANNER_OV_RANGED = registerItem(new ScannerTool.OVScannerRanged());
      SCANNER_OD_BIG_RANGED = registerItem(new ScannerTool.ODScannerBigRanged());
      SCANNER_OV_BIG_RANGED = registerItem(new ScannerTool.OVScannerBigRanged());
      SCANNER_QUARRY = registerItem(new ScannerTool.ScannerQuarry());
      SCANNER_FILTERED = registerItem(new ScannerTool.ScannerFiltered());
      SCANNER_FLUID = registerItem(new FluidScannerTool.FluidScanner());
      SCANNER_FLUID_FILTERED = registerItem(new FluidScannerTool.FluidScannerFiltered());
      TOOL_BOX = registerItem(new ToolBoxTool("tool_box", ToolBoxTool.TOOL_BOX));
      TOOL_BOX_CARBON = registerItem(new ToolBoxTool("carbon_box", ToolBoxTool.CARBON_TOOL_BOX));
      TOOL_BOX_IRIDIUM = registerItem(new ToolBoxTool("iridium_box", ToolBoxTool.QUANTUM_TOOL_BOX));
      UNPRINTED_WIKI = registerItem(new IC2SimpleItem("unprinted_wiki", "misc", "unprinted_wiki_book"));
      WIKI_ITEM = registerItem(new WikiItem(() -> IC2Wiki.IC2_WIKI));
      EU_READER = registerItem(new EUReaderTool());
      FREQUENCY_TRANSMITTER = registerItem(new FrequencyTransmitterItem());
      MINER_REMOTE = registerItem(new MinerRemoteItem());
   }

   static void addArmor() {
      BRONZE_HELMET = registerItem(new BronzeArmor("armor_bronze_helmet", EquipmentSlot.HEAD));
      BRONZE_CHEST = registerItem(new BronzeArmor("armor_bronze_chest", EquipmentSlot.CHEST));
      BRONZE_LEGGINGS = registerItem(new BronzeArmor("armor_bronze_leggings", EquipmentSlot.LEGS));
      BRONZE_BOOTS = registerItem(new BronzeArmor("armor_bronze_boots", EquipmentSlot.FEET));
      COMPOSITE_HELMET = registerItem(new CompositeArmor("armor_composite_helmet", EquipmentSlot.HEAD));
      COMPOSITE_CHEST = registerItem(new CompositeArmor("armor_composite_chest", EquipmentSlot.CHEST));
      COMPOSITE_LEGGINGS = registerItem(new CompositeArmor("armor_composite_leggings", EquipmentSlot.LEGS));
      COMPOSITE_BOOTS = registerItem(new CompositeArmor("armor_composite_boots", EquipmentSlot.FEET));
      HAZMAT_HELMET = registerItem(new HazmatArmor("armor_hazmat_helmet", EquipmentSlot.HEAD));
      HAZMAT_CHEST = registerItem(new HazmatArmor("armor_hazmat_chest", EquipmentSlot.CHEST));
      HAZMAT_LEGGINGS = registerItem(new HazmatArmor("armor_hazmat_leggings", EquipmentSlot.LEGS));
      HAZMAT_BOOTS = registerItem(new HazmatArmor("armor_hazmat_boots", EquipmentSlot.FEET));
      SHIELD_BRONZE = registerItem(new BronzeShield());
      SHIELD_ALUMINUM = registerItem(new AluminiumShield());
      SHIELD_PLATED = registerItem(new CompositeShield());
      SHIELD_NANO = registerItem(new NanoShield());
      SOLAR_HELMET = registerItem(new SolarHelmet(false));
      SOLAR_HELMET_ADVANCED = registerItem(new SolarHelmet(true));
      NIGHT_VISION_HELMET = registerItem(new NightVisionGoggles());
      CF_PACK = registerItem(new CFPack());
      STATIC_BOOTS = registerItem(new StaticBoots());
      JETPACK_FUEL = registerItem(new FuelJetpack());
      JETPACK_FUEL_COMPACT = registerItem(new CompactFuelJetpack());
      JETPACK_ELECTRIC = registerItem(new ElectricJetpack());
      JETPACK_ELECTRIC_COMPACT = registerItem(new CompactElectricJetpack());
      JETPACK_NUCLEAR = registerItem(new NuclearJetpack());
      JETPACK_NUCLEAR_COMPACT = registerItem(new CompactedNuclearJetpack());
      NANOSUIT_HELMET = registerItem(new NanoSuit("nano_suit_helmet", EquipmentSlot.HEAD));
      NANOSUIT_CHESTPLATE = registerItem(new NanoSuit("nano_suit_chestplate", EquipmentSlot.CHEST));
      NANOSUIT_LEGGINGS = registerItem(new NanoSuit("nano_suit_leggings", EquipmentSlot.LEGS));
      NANOSUIT_BOOTS = registerItem(new NanoSuit("nano_suit_boots", EquipmentSlot.FEET));
      QUANTUM_SUIT_HELMET = registerItem(new QuantumSuit("quantum_suit_helmet", EquipmentSlot.HEAD));
      QUANTUM_SUIT_CHESTPLATE = registerItem(new QuantumSuit("quantum_suit_chestplate", EquipmentSlot.CHEST));
      QUANTUM_SUIT_LEGGINGS = registerItem(new QuantumSuit("quantum_suit_leggings", EquipmentSlot.LEGS));
      QUANTUM_SUIT_BOOTS = registerItem(new QuantumSuit("quantum_suit_boots", EquipmentSlot.FEET));
      String moduleFolder = "armor/modules";
      BASE_MODULE = registerItem(new IC2SimpleItem("base_module", moduleFolder, "base"));
      AIR_REFILL_MODULE = registerItem(new AirRefillModuleItem(moduleFolder, "air_refill"));
      AUTO_FEED_MODULE = registerItem(new AutoFeedModuleItem(moduleFolder, "auto_feed"));
      CHARGER_MODULE = registerItem(new ChargerModuleItem(moduleFolder, "charger"));
      FOOD_STORAGE_MODULE = registerItem(new FoodStorageModuleItem(moduleFolder, "food_storage"));
      JUMP_BOOST_MODULE = registerItem(new JumpBoostModuleItem(moduleFolder, "jump_boost"));
      FLASH_LIGHT_MODULE = registerItem(new LightModuleItem(moduleFolder, "light"));
      PARACHUTE_MODULE = registerItem(new ParachuteModuleItem(moduleFolder, "parachute"));
      PROTECTION_MODULE = registerItem(new ProtectiveModuleItem(moduleFolder, "potion_protection"));
      SPEED_MODULE = registerItem(new SpeedBoostModuleItem(moduleFolder, "speed"));
      STEP_ASSIST_MODULE = registerItem(new StepAssistModuleItem(moduleFolder, "step_assist"));
      WATER_WALKER_MODULE = registerItem(new WaterWalkerModuleItem(moduleFolder, "water_walker"));
      TANK_MODULE = registerItem(new TankStorageModule(moduleFolder, "tank_storage"));
      EXTINGUISHING_MODULE = registerItem(new ExtinguishingModuleItem(moduleFolder, "extinguish"));
      LAVA_PROTECT_MODULE = registerItem(new LavaProtectModuleItem(moduleFolder, "lava"));
      SONAR_MODULE = registerItem(new SonarModuleItem(moduleFolder, "sonar"));
      ENERGY_HUD_MODULE = registerItem(new EnergyHudModuleItem(moduleFolder, "energy_hud"));
      ENERGY_SHIELD_MODULE = registerItem(new EnergyShieldModuleItem(moduleFolder, "energy_shield_overlay"));
      GOLDEN_MODULE = registerItem(new GoldenModuleItem(moduleFolder, "golden"));
   }

   static void addSimpleItems() {
      RAW_TIN = registerItem(new IC2SimpleItem("raw_tin", "raw", "tin"));
      RAW_SILVER = registerItem(new IC2SimpleItem("raw_silver", "raw", "silver"));
      RAW_ALUMINIUM = registerItem(new IC2SimpleItem("raw_aluminium", "raw", "aluminium"));
      DUST_IRON = registerItem(new IC2SimpleItem("dust_iron", "dust", "iron"));
      DUST_TIN = registerItem(new IC2SimpleItem("dust_tin", "dust", "tin"));
      DUST_COPPER = registerItem(new IC2SimpleItem("dust_copper", "dust", "copper"));
      DUST_SILVER = registerItem(new IC2SimpleItem("dust_silver", "dust", "silver"));
      DUST_GOLD = registerItem(new IC2SimpleItem("dust_gold", "dust", "gold"));
      DUST_BRONZE = registerItem(new IC2SimpleItem("dust_bronze", "dust", "bronze"));
      DUST_ALUMINIUM = registerItem(new IC2SimpleItem("dust_aluminium", "dust", "aluminium"));
      DUST_CHARCOAL = registerItem(new IC2SimpleItem("dust_charcoal", "dust", "charcoal"));
      DUST_HYDRATED_CHARCOAL = registerItem(new IC2SimpleItem("dust_hydrated_charcoal", "dust", "hydrated_charcoal"));
      DUST_COAL = registerItem(new IC2SimpleItem("dust_coal", "dust", "coal"));
      DUST_HYDRATED_COAL = registerItem(new IC2SimpleItem("dust_hydrated_coal", "dust", "hydrated_coal"));
      DUST_CLAY = registerItem(new IC2SimpleItem("dust_clay", "dust", "clay"));
      DUST_NETHERRACK = registerItem(new IC2SimpleItem("dust_netherrack", "dust", "netherrack"));
      DUST_OBSIDIAN = registerItem(new IC2SimpleItem("dust_obsidian", "dust", "obsidian"));
      DUST_RARE_EARTH = registerItem(new IC2SimpleItem("dust_rare_earth", "dust", "rare_earth"));
      DUST_DIAMOND = registerItem(new IC2SimpleItem("dust_diamond", "dust", "diamond"));
      INGOT_TIN = registerItem(new IC2SimpleItem("ingot_tin", "ingots", "tin"));
      INGOT_SILVER = registerItem(new IC2SimpleItem("ingot_silver", "ingots", "silver"));
      INGOT_REFINED_IRON = registerItem(new IC2SimpleItem("ingot_refined_iron", "ingots", "refined_iron"));
      INGOT_BRONZE = registerItem(new IC2SimpleItem("ingot_bronze", "ingots", "bronze"));
      INGOT_POLISHED_GOLD = registerItem(new IC2SimpleItem("ingot_polished_gold", "ingots", "polished_gold"));
      INGOT_ALUMINIUM = registerItem(new IC2SimpleItem("ingot_aluminium", "ingots", "aluminium"));
      INGOT_ADVANCED_ALLOY = registerItem(new IC2SimpleItem("ingot_advanced_alloy", "ingots", "advanced_alloy"));
      INGOT_URANIUM = registerItem(new IC2SimpleItem("ingot_uranium", "ingots", "uranium"));
      INGOT_URANIUM_ENRICHED_BLAZE = registerItem(new IC2SimpleItem("ingot_uranium_enriched_blaze", "ingots", "blaze_uranium"));
      INGOT_URANIUM_ENRICHED_CHARCOAL = registerItem(new IC2SimpleItem("ingot_uranium_enriched_charcoal", "ingots", "charcoal_uranium"));
      INGOT_URANIUM_ENRICHED_ENDERPEARL = registerItem(new IC2SimpleItem("ingot_uranium_enriched_enderpearl", "ingots", "enderpearl_uranium"));
      INGOT_URANIUM_ENRICHED_NETHERSTAR = registerItem(
         new IC2SimpleItem("ingot_uranium_enriched_netherstar", new PropertiesBuilder().setFoiled(true), "ingots", "netherstar_uranium")
      );
      INGOT_URANIUM_ENRICHED_REDSTONE = registerItem(new IC2SimpleItem("ingot_uranium_enriched_redstone", "ingots", "redstone_uranium"));
      FERTILIZER = registerItem(new IC2SimpleItem("fertilizer", "crops", "fertilizer"));
      GRIN_POWDER = registerItem(new IC2SimpleItem("grin_powder", "crops", "grin_powder"));
      HEMP = registerItem(new IC2SimpleItem("hemp", "crops", "hemp"));
      HEMP_SEEDS = registerItem(new IC2SimpleItem("hemp_seeds", "crops", "hemp_seeds"));
      OVERGROWTH_FERTILIZER = registerItem(
         new IC2SimpleItem("overgrowth_fertilizer", new PropertiesBuilder().setFoiled(true), "crops", "overgrowth_fertilizer")
      );
      RESTORING_FERTILIZER = registerItem(new IC2SimpleItem("restoring_fertilizer", "crops", "restoring_fertilizer"));
      HOPS = registerItem(new IC2SimpleItem("hops", "crops", "hops"));
      WHISKY_GRIST = registerItem(new IC2SimpleItem("whisky_grist", "food_and_drink", "whisky_grist"));
      WEEDEX = registerItem(new IC2SimpleItem("weedex", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).maxDamage(64).setNoRepair(), "crops", "weedex"))
         .addTooltip(ITooltipProvider.tooltip("tooltip.item.ic2.weedex.refill"));
      HYDRATION_CELL = registerItem(
         new IC2SimpleItem(
               "hydration_cell",
               new PropertiesBuilder().containerItem(CELL_EMPTY).group(IC2.IC2_MAIN_GROUP).maxDamage(10000).setNoRepair(),
               "crops",
               "hydration_cell"
            )
            .disableEnchantments()
      );
      ORE_IRIDIUM = registerItem(new IC2SimpleItem("ore_iridium", "ore_drops", "iridium"));
      ORE_URANIUM_DROP = registerItem(new IC2SimpleItem("ore_uranium", "ore_drops", "uranium"));
      SCRAP = registerItem(new IC2SimpleItem("scrap", "matter", "scrap"));
      SCRAP_METAL = registerItem(new IC2SimpleItem("scrap_metal", "matter", "scrapmetal"));
      UUMATTER = registerItem(new IC2SimpleItem("uumatter", "matter", "uum"));
      COAL_BALL = registerItem(new IC2SimpleItem("coal_ball", "misc", "coal_ball"));
      COAL_BALL_COMPRESSED = registerItem(new IC2SimpleItem("compressed_coal_ball", "misc", "compressed_coal_ball"));
      COAL_CHUNK = registerItem(new IC2SimpleItem("coal_chunk", "misc", "coal_chunk"));
      INDUSTRIAL_DIAMOND = registerItem(new IC2SimpleItem("industrial_diamond", "misc", "industrial_diamond"));
      CARBON_FIBER = registerItem(new IC2SimpleItem("carbon_fiber", "misc", "carbon_fiber"));
      CARBON_MESH = registerItem(new IC2SimpleItem("carbon_mesh", "misc", "carbon_mesh"));
      CARBON_PLATE = registerItem(new IC2SimpleItem("carbon_plate", "misc", "carbon_plate"));
      PLATE_ADVANCED_ALLOY = registerItem(new IC2SimpleItem("plate_advanced_alloy", "plates", "advanced_alloy"));
      PULSATING_QUARTZ = registerItem(new IC2SimpleItem("pulsating_quartz", "misc", "pulsating_quartz"));
      PLATE_IRIDIUM = registerItem(new IC2SimpleItem("plate_iridium", "plates", "iridium"));
      PLATE_DENSE_COPPER = registerItem(new IC2SimpleItem("plate_dense_copper", "plates", "dense_copper"));
      RARE_EARTH_CHUNK = registerItem(new IC2SimpleItem("rare_earth_chunk", "misc", "rare_earth_chunk"));
      DEAD_MAGNET = registerItem(new IC2SimpleItem("dead_magnet", "misc", "dead_magnet"));
      MAGNET = registerItem(new IC2SimpleItem("magnet", "misc", "magnet"));
      PLASMA_CORE = registerItem(new IC2SimpleItem("plasma_core", "misc", "plasma_core"));
      SCRAP_METAL_CHUNK = registerItem(new IC2SimpleItem("scrap_metal_chunk", "blades", "scrapmetal_chunk"));
      SCRAP_METAL_BLADE = registerItem(new IC2SimpleItem("scrap_metal_blade", "blades", "scrapmetal_blade"));
      OBSIDIAN_BLADE_RAW = registerItem(new IC2SimpleItem("raw_obsidian_blade", "blades", "raw_obsidian_blade"));
      OBSIDIAN_BLADE = registerItem(new IC2SimpleItem("obsidian_blade", "blades", "obsidian_blade"));
      TURBINE_BLADE = registerItem(new IC2SimpleItem("turbine_blade", "blades", "turbine_blade"));
      PLANT_BALL = registerItem(new IC2SimpleItem("plant_ball", "misc", "plant_ball"));
      PLANT_BALL_COMPRESSED = registerItem(new IC2SimpleItem("compressed_plant_ball", "misc", "compressed_plant_ball"));
      HYDRATED_COAL_COMPRESSED = registerItem(new IC2SimpleItem("compressed_hydrated_coal", "misc", "compressed_coal"));
      BIO_MESH = registerItem(new IC2SimpleItem("bio_mesh", "misc", "bio_mesh"));
      BIO_BALL = registerItem(new IC2SimpleItem("bio_ball", "misc", "bio_ball"));
      BIO_FIBER_RAW = registerItem(new IC2SimpleItem("raw_bio_fiber", "misc", "bio_fiber"));
      BIO_BALL_COMPRESSED = registerItem(new IC2SimpleItem("compressed_bio_ball", "misc", "compressed_bio_ball"));
      BIO_CHUNK = registerItem(new IC2SimpleItem("bio_chunk", "misc", "bio_chunk"));
      BIO_FLESH_BALL = registerItem(new IC2SimpleItem("flesh_ball", "misc", "flesh_ball"));
      BIO_LEATHER_RAW = registerItem(new IC2SimpleItem("raw_leather", "misc", "raw_leather"));
      RUBBER = registerItem(new IC2SimpleItem("rubber", "misc", "rubber"));
      CIRCUIT = registerItem(new IC2SimpleItem("circuit", "misc", "circuit"));
      ADVANCED_CIRCUIT = registerItem(new IC2SimpleItem("advanced_circuit", "misc", "advanced_circuit"));
      COMPLEX_CIRCUIT = registerItem(new IC2SimpleItem("complex_circuit", "misc", "complex_circuit"));
      CF_PELLET = registerItem(new IC2SimpleItem("cf_pellet", "misc", "cf_pellet"));
      RUBBERWOOD_SIGN = registerItem(
         new IC2SignItem("rubberwood_sign_item", IC2Blocks.RUBBERWOOD_SIGN, IC2Blocks.RUBBERWOOD_WALL_SIGN, "misc", "rubberwood_sign")
      );
      RUBBERWOOD_BOAT = registerItem(new IC2BoatItem("rubberwood_boat", (W, P) -> new RubberwoodBoat(IC2Entities.RUBBERWOOD_BOAT, W, P.x, P.y, P.z)));
      RUBBER_BOAT = registerItem(new IC2BoatItem("rubber_boat", "rubber", (W, P) -> new RubberBoat(IC2Entities.RUBBER_BOAT, W, P.x, P.y, P.z)));
      CARBON_BOAT = registerItem(new IC2BoatItem("carbon_boat", "carbon", (W, P) -> new CarbonBoat(IC2Entities.CARBON_BOAT, W, P.x, P.y, P.z)));
      RUBBER_BOAT_BROKEN = registerItem(
         new IC2SimpleItem("rubber_boat_broken", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).maxStackSize(1), "boat", "broken_rubber")
      );
      BATBOX_MINECART = registerItem(new IC2MinecartItem("batbox_minecart", "batbox", (W, P) -> new BatboxMinecart(IC2Entities.BATBOX, W, P.x, P.y, P.z)));
      MFE_MINECART = registerItem(new IC2MinecartItem("mfe_minecart", "mfe", (W, P) -> new MFEMinecart(IC2Entities.MFE, W, P.x, P.y, P.z)));
      MFSU_MINECART = registerItem(new IC2MinecartItem("mfsu_minecart", "mfsu", (W, P) -> new MFSUMinecart(IC2Entities.MFSU, W, P.x, P.y, P.z)));
      ESU_MINECART = registerItem(new IC2MinecartItem("esu_minecart", "esu", (W, P) -> new ESUMinecart(IC2Entities.ESU, W, P.x, P.y, P.z)));
      ISU_MINECART = registerItem(new IC2MinecartItem("isu_minecart", "isu", (W, P) -> new ISUMinecart(IC2Entities.ISU, W, P.x, P.y, P.z)));
      PESU_MINECART = registerItem(new IC2MinecartItem("pesu_minecart", "pesu", (W, P) -> new PESUMinecart(IC2Entities.PESU, W, P.x, P.y, P.z)));
      COPPER_SIGN_ITEM = registerItem(new IC2SignItem("copper_sign_item", IC2Blocks.COPPER_SIGN, IC2Blocks.COPPER_WALL_SIGN, "misc", "copper_sign"));
      ALUMINIUM_SIGN_ITEM = registerItem(
         new IC2SignItem("aluminium_sign_item", IC2Blocks.ALUMINIUM_SIGN, IC2Blocks.ALUMINIUM_WALL_SIGN, "misc", "aluminium_sign")
      );
      TIN_SIGN_ITEM = registerItem(new IC2SignItem("tin_sign_item", IC2Blocks.TIN_SIGN, IC2Blocks.TIN_WALL_SIGN, "misc", "tin_sign"));
      SILVER_SIGN_ITEM = registerItem(new IC2SignItem("silver_sign_item", IC2Blocks.SILVER_SIGN, IC2Blocks.SILVER_WALL_SIGN, "misc", "silver_sign"));
      BRONZE_SIGN_ITEM = registerItem(new IC2SignItem("bronze_sign_item", IC2Blocks.BRONZE_SIGN, IC2Blocks.BRONZE_WALL_SIGN, "misc", "bronze_sign"));
      REFINED_IRON_SIGN_ITEM = registerItem(
         new IC2SignItem("refined_iron_sign_item", IC2Blocks.REFINED_IRON_SIGN, IC2Blocks.REFINED_IRON_WALL_SIGN, "misc", "refined_iron_sign")
      );
   }

   static void addFoodAndDrinks() {
      COFFEE_BEANS = registerItem(new IC2SimpleItem("coffee_beans", "food_and_drink", "coffee_beans"));
      COFFEE_POWDER = registerItem(new IC2SimpleItem("coffee_powder", "food_and_drink", "coffee_powder"));
      FLOUR = registerItem(new IC2SimpleItem("flour", "food_and_drink", "flour"));
      TEA_LEAF = registerItem(new IC2SimpleItem("tea_leaf", "food_and_drink", "tea_leaf"));
      TIN_CAN = registerItem(new IC2SimpleItem("tin_can", "food_and_drink/tincan", "tin_can"));
      CHOCOLATE = registerItem(IC2Food.simpleFood("chocolate_bar", "chocolate", IC2FoodsAndDrinks.CHOCOLATE));
      POTATO_SLICES = registerItem(new IC2SimpleItem("potato_slices", "food_and_drink", "potato_slices"));
      FRIES = registerItem(IC2Food.simpleFood("fries", "fries", IC2FoodsAndDrinks.FRIES));
      CHIPS_ROLL = registerItem(IC2Food.multiFood("chips_roll", "chips_roll", 48, IC2FoodsAndDrinks.CHIPS).setAutoEatable());
      TIN_CAN_FILLED = registerItem(new TinCanItem());
      FoodCanRegistry.INSTANCE.init();
      MUG = registerItem(new MugItem());
      MUG_COFFEE = registerItem(new MugItem("mug_coffee", IC2FoodsAndDrinks.COFFEE));
      MUG_DARK_COFFEE = registerItem(new MugItem("mug_dark_coffee", IC2FoodsAndDrinks.DARK_COFFEE));
      MUG_COLD_DARK_COFFEE = registerItem(new MugItem("mug_cold_dark_coffee", IC2FoodsAndDrinks.COLD_DARK_COFFEE));
      MUG_TEA = registerItem(new MugItem("mug_tea", IC2FoodsAndDrinks.TEA));
      MUG_COLD_TEA = registerItem(new MugItem("mug_cold_tea", IC2FoodsAndDrinks.COLD_TEA));
      MUG_BLACK_TEA = registerItem(new MugItem("mug_black_tea", IC2FoodsAndDrinks.BLACK_TEA));
      MUG_COLD_BLACK_TEA = registerItem(new MugItem("mug_cold_black_tea", IC2FoodsAndDrinks.COLD_BLACK_TEA));
      MUG_ICED_TEA = registerItem(new MugItem("mug_iced_tea", IC2FoodsAndDrinks.ICED_TEA));
      MUG_FRUIT_TEA = registerItem(new MugItem("mug_fruit_tea", IC2FoodsAndDrinks.FRUIT_TEA));
      MUG_COLD_FRUIT_TEA = registerItem(new MugItem("mug_cold_fruit_tea", IC2FoodsAndDrinks.COLD_FRUIT_TEA));
      MUG_GREEN_TEA = registerItem(new MugItem("mug_green_tea", IC2FoodsAndDrinks.GREEN_TEA));
      MUG_COLD_GREEN_TEA = registerItem(new MugItem("mug_cold_green_tea", IC2FoodsAndDrinks.COLD_GREEN_TEA));
      MUG_MILK = registerItem(new MugItem("mug_milk", IC2FoodsAndDrinks.MILK));
      MUG_COCOA = registerItem(new MugItem("mug_cocoa", IC2FoodsAndDrinks.COCOA));
      MUG_HOT_COCOA = registerItem(new MugItem("mug_hot_cocoa", IC2FoodsAndDrinks.HOT_COCOA));
      MUG_RUM = registerItem(new MugItem("mug_rum", IC2FoodsAndDrinks.RUM));
      MUG_BEER = registerItem(new MugItem("mug_beer", IC2FoodsAndDrinks.BEER));
      MUG_WHISKY = registerItem(new MugItem("mug_whisky", IC2FoodsAndDrinks.WHISKY));
      GLASS = registerItem(new GlassItem());
      GLASS_COFFEE = registerItem(new GlassItem("glass_coffee", IC2FoodsAndDrinks.COFFEE));
      GLASS_DARK_COFFEE = registerItem(new GlassItem("glass_dark_coffee", IC2FoodsAndDrinks.DARK_COFFEE));
      GLASS_COLD_DARK_COFFEE = registerItem(new GlassItem("glass_cold_dark_coffee", IC2FoodsAndDrinks.COLD_DARK_COFFEE));
      GLASS_TEA = registerItem(new GlassItem("glass_tea", IC2FoodsAndDrinks.TEA));
      GLASS_COLD_TEA = registerItem(new GlassItem("glass_cold_tea", IC2FoodsAndDrinks.COLD_TEA));
      GLASS_BLACK_TEA = registerItem(new GlassItem("glass_black_tea", IC2FoodsAndDrinks.BLACK_TEA));
      GLASS_COLD_BLACK_TEA = registerItem(new GlassItem("glass_cold_black_tea", IC2FoodsAndDrinks.COLD_BLACK_TEA));
      GLASS_ICED_TEA = registerItem(new GlassItem("glass_iced_tea", IC2FoodsAndDrinks.ICED_TEA));
      GLASS_FRUIT_TEA = registerItem(new GlassItem("glass_fruit_tea", IC2FoodsAndDrinks.FRUIT_TEA));
      GLASS_COLD_FRUIT_TEA = registerItem(new GlassItem("glass_cold_fruit_tea", IC2FoodsAndDrinks.COLD_FRUIT_TEA));
      GLASS_GREEN_TEA = registerItem(new GlassItem("glass_green_tea", IC2FoodsAndDrinks.GREEN_TEA));
      GLASS_COLD_GREEN_TEA = registerItem(new GlassItem("glass_cold_green_tea", IC2FoodsAndDrinks.COLD_GREEN_TEA));
      GLASS_MILK = registerItem(new GlassItem("glass_milk", IC2FoodsAndDrinks.MILK));
      GLASS_COCOA = registerItem(new GlassItem("glass_cocoa", IC2FoodsAndDrinks.COCOA));
      GLASS_HOT_COCOA = registerItem(new GlassItem("glass_hot_cocoa", IC2FoodsAndDrinks.HOT_COCOA));
      GLASS_RUM = registerItem(new GlassItem("glass_rum", IC2FoodsAndDrinks.RUM));
      GLASS_BEER = registerItem(new GlassItem("glass_beer", IC2FoodsAndDrinks.BEER));
      GLASS_WHISKY = registerItem(new GlassItem("glass_whisky", IC2FoodsAndDrinks.WHISKY));
   }

   static void addOtherItems() {
      TERRA_WART = registerItem(new TerraWartItem());
      STICKY_RESIN = registerItem(new StickyResinItem());
      SCRAPBOX = registerItem(new ScrapBoxItem());
      CROP_SEED = registerItem(new CropSeedItem());
      STICKY_DYNAMITE = registerItem(new StickyDynamite());
      MEMORY_STICK = registerItem(new MemoryStickItem());
      FUEL_CAN = registerItem(new FuelCanItem());
      DispenserBlock.registerBehavior(SCRAPBOX, new ScrapBoxItem.DispenserScrapBoxBehavior());
      PAINTER = registerItem(new PainterTool());
      PAINTER_BLACK = registerItem(new PainterTool(DyeColor.BLACK));
      PAINTER_BLUE = registerItem(new PainterTool(DyeColor.BLUE));
      PAINTER_BROWN = registerItem(new PainterTool(DyeColor.BROWN));
      PAINTER_CYAN = registerItem(new PainterTool(DyeColor.CYAN));
      PAINTER_GRAY = registerItem(new PainterTool(DyeColor.GRAY));
      PAINTER_GREEN = registerItem(new PainterTool(DyeColor.GREEN));
      PAINTER_LIGHT_BLUE = registerItem(new PainterTool(DyeColor.LIGHT_BLUE));
      PAINTER_LIGHT_GRAY = registerItem(new PainterTool(DyeColor.LIGHT_GRAY));
      PAINTER_LIME = registerItem(new PainterTool(DyeColor.LIME));
      PAINTER_MAGENTA = registerItem(new PainterTool(DyeColor.MAGENTA));
      PAINTER_ORANGE = registerItem(new PainterTool(DyeColor.ORANGE));
      PAINTER_PINK = registerItem(new PainterTool(DyeColor.PINK));
      PAINTER_PURPLE = registerItem(new PainterTool(DyeColor.PURPLE));
      PAINTER_RED = registerItem(new PainterTool(DyeColor.RED));
      PAINTER_WHITE = registerItem(new PainterTool(DyeColor.WHITE));
      PAINTER_YELLOW = registerItem(new PainterTool(DyeColor.YELLOW));
      SU_BATTERY = registerItem(new SUBattery());
      RE_BATTERY = registerItem(new BatteryItem("re_battery", 10000, 100, 1, true, "reusable_battery", true));
      ENERGY_CRYSTAL = registerItem(new BatteryItem("energy_crystal", 100000, 250, 2, true, "energy_crystal", true, 250));
      LAPOTRON_CRYSTAL = registerItem(new BatteryItem("lapatron_crystal", 1000000, 600, 3, true, "lapotron_crystal", true, 1000));
      GLOWTRONIC_CRYSTAL = registerItem(new BatteryItem("glowtronic_crystal", 7500000, 2500, 3, true, "glowtronic_crystal", true, 5000));
      UESC = registerItem(new BatteryItem("uesc", 17500000, 9500, 4, true, "uesc", true, 10000));
      PESD = registerItem(new BatteryItem("pesd", 50000000, 25000, 4, true, "pesd", false));
      QUANTUM_ACCUMULATOR = registerItem(new BatteryItem("quantum_accumulator", 900000, 1000, 1, true, "quantum_accumulator", false));
      QUANTUM_ACCUMULATOR_BIG = registerItem(new BatteryItem("quantum_accumulator_big", 5400000, 6000, 1, true, "big_quantum_accumulator", false));
      BAT_PACK = registerItem(new ElectricPackArmor("batpack", "armor/packs", "batpack", "ic2:textures/models/armor/batpack", 60000, 1, 100));
      LAP_PACK = registerItem(new ElectricPackArmor("lappack", "armor/packs", "lappack", "ic2:textures/models/armor/lappack", Rarity.UNCOMMON, 300000, 2, 250));
      QUANTUM_PACK = registerItem(
         new ElectricPackArmor("quantum_pack", "armor/packs", "quantum", "ic2:textures/models/armor/quantum_pack", Rarity.RARE, 1200000, 3, 1000)
      );
      BATTERY_BELT = registerItem(
         new ElectricPackArmor("industrial_belt", EquipmentSlot.LEGS, "armor", "industrial_belt", "ic2:textures/models/armor/battery_belt", 30000, 1, 100)
      );
      registerItem(new FluidBlockItem(Blocks.WATER));
      registerItem(new FluidBlockItem(Blocks.LAVA));
      COIN = registerItem(new CoinItem("coin", 1, "coin")).register();
      COIN_8X = registerItem(new CoinItem("coin_8x", 8, "coin_x8")).register();
      COIN_64X = registerItem(new CoinItem("coin_64x", 64, "coin_x64")).register();
      COIN_512X = registerItem(new CoinItem("coin_512x", 512, "coin_x512")).register();
      COIN_4096X = registerItem(new CoinItem("coin_4096x", 4096, "coin_x4096")).register();
      COIN_32768X = registerItem(new CoinItem("coin_32768x", 32768, "coin_x32768")).register();
      TFBP = registerItem(new IC2SimpleItem("tfbp", "tfbp/terrain", "empty"));
      TFBP_BIOME = registerItem(new IC2SimpleItem("tfbp_biome", "tfbp/biome", "empty"));
      TFBP_MUSHROOM = registerItem(new TerraformerBlueprintItem("tfbp_mushroom", MushroomBluePrint.INSTANCE, "tfbp/terrain", "mushroom"));
      TFBP_CULTIVATION = registerItem(new TerraformerBlueprintItem("tfbp_cultivation", CultivatedBluePrint.INSTANCE, "tfbp/terrain", "cultivation"));
      TFBP_CHILLING = registerItem(new TerraformerBlueprintItem("tfbp_chilling", ChillingBluePrint.INSTANCE, "tfbp/terrain", "chilling"));
      TFBP_IRRIGATION = registerItem(new TerraformerBlueprintItem("tfbp_irrigation", IrrigationBluePrint.INSTANCE, "tfbp/terrain", "irrigation"));
      TFBP_DESERTIFICATION = registerItem(
         new TerraformerBlueprintItem("tfbp_desertification", DesertificationBluePrint.INSTANCE, "tfbp/terrain", "desertification")
      );
      TFBP_FLATIFICATOR = registerItem(new TerraformerBlueprintItem("tfbp_flatification", FlatificationBluePrint.INSTANCE, "tfbp/terrain", "flatification"));
      TFBP_BIOME_CHILLING = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_chilling", new BiomeBlueprint(35, 6000, Biomes.SNOWY_TAIGA.location()), "tfbp/biome", "chilling")
      );
      TFBP_BIOME_CULTIVATION = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_cultivation", new BiomeBlueprint(40, 4000, Biomes.PLAINS.location()), "tfbp/biome", "cultivation")
      );
      TFBP_BIOME_DESERTIFICATION = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_desertification", new BiomeBlueprint(40, 2500, Biomes.DESERT.location()), "tfbp/biome", "desertification")
      );
      TFBP_BIOME_FORESTIFICATION = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_forestification", new BiomeBlueprint(50, 2000, Biomes.FOREST.location()), "tfbp/biome", "forestification")
      );
      TFBP_BIOME_IRRIGATION = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_irrigation", new BiomeBlueprint(30, 5000, Biomes.SWAMP.location()), "tfbp/biome", "irrigation")
      );
      TFBP_BIOME_MUSHROOM = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_mushroom", new BiomeBlueprint(25, 8000, Biomes.MUSHROOM_FIELDS.location()), "tfbp/biome", "mushroom")
      );
      TFBP_BIOME_UNDERGROWTH = registerItem(
         new TerraformerBlueprintItem("tfbp_biome_undergrowth", new BiomeBlueprint(30, 5000, Biomes.JUNGLE.location()), "tfbp/biome", "undergrowth")
      );
      WOODEN_ROTOR = registerItem(new WindmillBladeItem("wooden_rotor", "wooden", 8, 1.0F, 11250, "models/windmill/wood_rotor"));
      WOOL_ROTOR = registerItem(new WindmillBladeItem("wool_rotor", "wool", 8, 1.5F, 5675, "models/windmill/wool_rotor"));
      IRON_ROTOR = registerItem(new WindmillBladeItem("iron_rotor", "iron", 10, 1.0F, 136875, "models/windmill/iron_rotor"));
      CARBON_ROTOR = registerItem(new WindmillBladeItem("carbon_rotor", "carbon", 8, 1.0F, 68437, "models/windmill/carbon_rotor"));
      MIXED_ALLOY_ROTOR = registerItem(new WindmillBladeItem("mixed_alloy_rotor", "composite", 10, 1.0F, 273750, "models/windmill/alloy_rotor"));
      IRIDIUM_ROTOR = registerItem(new WindmillBladeItem("iridium_rotor", "iridium", 10, 1.0F, 0, "models/windmill/iridium_rotor"));
   }

   static void addReactorComponents() {
      VENT_HEAT = registerItem(
         new ReactorHeatVentItem("heat_vent", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.HEAT, 6, 0, 1000, "reactor/vents", "heat", 18))
      );
      VENT_HEAT_CORE = registerItem(
         new ReactorHeatVentItem(
            "heat_vent_core", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.HEAT, 5, 5, 1000, "reactor/vents", "core_heat", 19)
         )
      );
      VENT_HEAT_OVERCLOCKED = registerItem(
         new ReactorHeatVentItem(
            "heat_vent_overclocked",
            new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.HEAT, 20, 36, 1000, "reactor/vents", "overclocked_heat", 21)
         )
      );
      VENT_HEAT_ADVANCED = registerItem(
         new ReactorHeatVentItem(
            "heat_vent_advanced", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.HEAT, 12, 0, 1000, "reactor/vents", "advanced_heat", 20)
         )
      );
      VENT_STEAM = registerItem(
         new ReactorHeatVentItem(
            "steam_vent", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.STEAM, 12, 0, 1100, "reactor/vents", "steam", 22)
         )
      );
      VENT_STEAM_CORE = registerItem(
         new ReactorHeatVentItem(
            "steam_vent_core", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.STEAM, 10, 10, 1100, "reactor/vents", "core_steam", 23)
         )
      );
      VENT_STEAM_OVERCLOCKED = registerItem(
         new ReactorHeatVentItem(
            "steam_vent_overclocked",
            new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.STEAM, 40, 72, 1100, "reactor/vents", "overclocked_steam", 24)
         )
      );
      VENT_STEAM_ADVANCED = registerItem(
         new ReactorHeatVentItem(
            "steam_vent_advanced", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.STEAM, 24, 0, 1100, "reactor/vents", "advanced_steam", 25)
         )
      );
      VENT_ELECTRIC = registerItem(
         new ReactorHeatVentItem(
            "electric_vent", new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.ELECTRIC, 12, 0, 10000, "reactor/vents", "electric", 48)
         )
      );
      VENT_ELECTRIC_CORE = registerItem(
         new ReactorHeatVentItem(
            "electric_vent_core",
            new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.ELECTRIC, 10, 10, 10000, "reactor/vents", "core_electric", 49)
         )
      );
      VENT_ELECTRIC_OVERCLOCKED = registerItem(
         new ReactorHeatVentItem(
            "electric_vent_overclocked",
            new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.ELECTRIC, 40, 72, 10000, "reactor/vents", "overclocked_electric", 50)
         )
      );
      VENT_ELECTRIC_ADVANCED = registerItem(
         new ReactorHeatVentItem(
            "electric_vent_advanced",
            new ReactorHeatVentItem.VentProperty(ReactorHeatVentItem.VentType.ELECTRIC, 24, 0, 10000, "reactor/vents", "advanced_electric", 51)
         )
      );
      HEAT_EXCHANGER = registerItem(new ReactorHeatExchangerItem("heat_exchanger", new ExchangerProperty(12, 4, 2500, "reactor/exchanger", "heat", 27)));
      HEAT_EXCHANGER_CORE = registerItem(
         new ReactorHeatExchangerItem("heat_exchanger_core", new ExchangerProperty(0, 72, 5000, "reactor/exchanger", "core_heat", 28))
      );
      HEAT_EXCHANGER_SPREAD = registerItem(
         new ReactorHeatExchangerItem("heat_exchanger_spread", new ExchangerProperty(36, 0, 5000, "reactor/exchanger", "component_heat", 29))
      );
      HEAT_EXCHANGER_ADVANCED = registerItem(
         new ReactorHeatExchangerItem("heat_exchanger_advanced", new ExchangerProperty(24, 8, 10000, "reactor/exchanger", "advanced_heat", 30))
      );
      HEAT_BALANCER = registerItem(new ReactorHeatBalancerItem("heat_balancer", new ExchangerProperty(18, 12, 2500, "reactor/balancer", "heat", 55)));
      HEAT_BALANCER_CORE = registerItem(
         new ReactorHeatBalancerItem("heat_balancer_core", new ExchangerProperty(0, 108, 5000, "reactor/balancer", "core_heat", 56))
      );
      HEAT_BALANCER_SPREAD = registerItem(
         new ReactorHeatBalancerItem("heat_balancer_spread", new ExchangerProperty(54, 0, 5000, "reactor/balancer", "component_heat", 57))
      );
      HEAT_BALANCER_ADVANCED = registerItem(
         new ReactorHeatBalancerItem("heat_balancer_advanced", new ExchangerProperty(36, 24, 10000, "reactor/balancer", "advanced_heat", 58))
      );
      HEAT_PUMP = registerItem(new ReactorHeatPumpItem("heat_pump", "reactor/pump", "heat_pump", 72, 54));
      COOLANT_CELL_10K = registerItem(new ReactorHeatStorageItem("heat_storage_single", 10000, "reactor/cells", "10k_coolant", 12));
      COOLANT_CELL_30K = registerItem(new ReactorHeatStorageItem("heat_storage_triple", 30000, "reactor/cells", "30k_coolant", 13));
      COOLANT_CELL_60K = registerItem(new ReactorHeatStorageItem("heat_storage_six", 60000, "reactor/cells", "60k_coolant", 14));
      CONDENSATOR = registerItem(new ReactorCondensatorItem("condensator", 20000, "reactor/condensator", "rsh", 15));
      CONDENSATOR_LAP = registerItem(new ReactorCondensatorItem("condensator_lap", 100000, "reactor/condensator", "lzh", 16));
      HEAT_PACK = registerItem(new ReactorHeatPackItem("heat_pack", "reactor/cells", "heating"));
      PLATING = registerItem(new ReactorPlatingItem("plating", "reactor/plating", "reactor", 1000, 0.95F, 31));
      PLATING_HEAT_CAPACITY = registerItem(new ReactorPlatingItem("plating_heat_capacity", "reactor/plating", "heat_capacity_reactor", 2000, 0.99F, 33));
      PLATING_CONTAINMENT = registerItem(new ReactorPlatingItem("plating_containment", "reactor/plating", "containment_reactor", 500, 0.9F, 32));
      HEAT_SPREADER = registerItem(new ReactorHeatSpreaderItem("heat_spreader", "reactor/vents", "spreader_heat"));
      REFLECTOR = registerItem(new ReactorReflectorItem("reflector", "reactor/reflector", "neutron", 10000, false, 34));
      REFLECTOR_THICK = registerItem(new ReactorReflectorItem("reflector_thick", "reactor/reflector", "thick_neutron", 40000, false, 35));
      REFLECTOR_IRIDIUM = registerItem(new ReactorIridiumReflectorItem());
      REACTOR_FUEL_CELL = registerItem(new ReactorFuelCellItem());
      URANIUM_ROD_NEAR_DEPLETED = registerItem(new IC2SimpleItem("uranium_rod_near_depleted", "reactor/cells/uranium/near_depleted", "base"));
      URANIUM_ROD_NEAR_DEPLETED_REDSTONE = registerItem(
         new IC2SimpleItem("uranium_rod_near_depleted_redstone", "reactor/cells/uranium/near_depleted", "redstone_enriched")
      );
      URANIUM_ROD_NEAR_DEPLETED_BLAZE = registerItem(
         new IC2SimpleItem("uranium_rod_near_depleted_blaze", "reactor/cells/uranium/near_depleted", "blaze_enriched")
      );
      URANIUM_ROD_NEAR_DEPLETED_ENDER_PEARL = registerItem(
         new IC2SimpleItem("uranium_rod_near_depleted_ender_pearl", "reactor/cells/uranium/near_depleted", "enderpearl_enriched")
      );
      URANIUM_ROD_NEAR_DEPLETED_NETHER_STAR = registerItem(
         new IC2SimpleItem("uranium_rod_near_depleted_nether_star", "reactor/cells/uranium/near_depleted", "netherstar_enriched")
      );
      URANIUM_ROD_NEAR_DEPLETED_CHARCOAL = registerItem(
         new IC2SimpleItem("uranium_rod_near_depleted_charcoal", "reactor/cells/uranium/near_depleted", "charcoal_enriched")
      );
      URANIUM_ROD_RE_ENRICHED = registerItem(new IC2SimpleItem("uranium_rod_re_enriched", "reactor/cells/uranium/re_enriched", "base"));
      URANIUM_ROD_RE_ENRICHED_REDSTONE = registerItem(
         new IC2SimpleItem("uranium_rod_re_enriched_redstone", "reactor/cells/uranium/re_enriched", "redstone_enriched")
      );
      URANIUM_ROD_RE_ENRICHED_BLAZE = registerItem(new IC2SimpleItem("uranium_rod_re_enriched_blaze", "reactor/cells/uranium/re_enriched", "blaze_enriched"));
      URANIUM_ROD_RE_ENRICHED_ENDER_PEARL = registerItem(
         new IC2SimpleItem("uranium_rod_re_enriched_ender_pearl", "reactor/cells/uranium/re_enriched", "enderpearl_enriched")
      );
      URANIUM_ROD_RE_ENRICHED_NETHER_STAR = registerItem(
         new IC2SimpleItem("uranium_rod_re_enriched_nether_star", "reactor/cells/uranium/re_enriched", "netherstar_enriched")
      );
      URANIUM_ROD_RE_ENRICHED_CHARCOAL = registerItem(
         new IC2SimpleItem("uranium_rod_re_enriched_charcoal", "reactor/cells/uranium/re_enriched", "charcoal_enriched")
      );
      URANIUM_ROD_SINGLE = registerItem(new ReactorUraniumRod("uranium_rod_single", "reactor/cells/uranium/single", "base", StandardUranium.INSTANCE, 1, 0));
      URANIUM_ROD_REDSTONE_SINGLE = registerItem(
         new ReactorUraniumRod("uranium_rod_redstone_single", "reactor/cells/uranium/single", "redstone_enriched", RedstoneUranium.INSTANCE, 1, 3)
      );
      URANIUM_ROD_BLAZE_SINGLE = registerItem(
         new ReactorUraniumRod("uranium_rod_blaze_single", "reactor/cells/uranium/single", "blaze_enriched", BlazeUranium.INSTANCE, 1, 6)
      );
      URANIUM_ROD_ENDER_PEARL_SINGLE = registerItem(
         new ReactorUraniumRod("uranium_rod_ender_pearl_single", "reactor/cells/uranium/single", "enderpearl_enriched", EnderUranium.INSTANCE, 1, 9)
      );
      URANIUM_ROD_NETHER_STAR_SINGLE = registerItem(
         new ReactorUraniumRod("uranium_rod_nether_star_single", "reactor/cells/uranium/single", "netherstar_enriched", NetherStarUranium.INSTANCE, 1, 37)
      );
      URANIUM_ROD_CHARCOAL_SINGLE = registerItem(
         new ReactorUraniumRod("uranium_rod_charcoal_single", "reactor/cells/uranium/single", "charcoal_enriched", CharcoalUranium.INSTANCE, 1, 40)
      );
      URANIUM_ROD_DUAL = registerItem(new ReactorUraniumRod("uranium_rod_dual", "reactor/cells/uranium/dual", "base", StandardUranium.INSTANCE, 2, 1));
      URANIUM_ROD_REDSTONE_DUAL = registerItem(
         new ReactorUraniumRod("uranium_rod_redstone_dual", "reactor/cells/uranium/dual", "redstone_enriched", RedstoneUranium.INSTANCE, 2, 4)
      );
      URANIUM_ROD_BLAZE_DUAL = registerItem(
         new ReactorUraniumRod("uranium_rod_blaze_dual", "reactor/cells/uranium/dual", "blaze_enriched", BlazeUranium.INSTANCE, 2, 7)
      );
      URANIUM_ROD_ENDER_PEARL_DUAL = registerItem(
         new ReactorUraniumRod("uranium_rod_ender_pearl_dual", "reactor/cells/uranium/dual", "enderpearl_enriched", EnderUranium.INSTANCE, 2, 10)
      );
      URANIUM_ROD_NETHER_STAR_DUAL = registerItem(
         new ReactorUraniumRod("uranium_rod_nether_star_dual", "reactor/cells/uranium/dual", "netherstar_enriched", NetherStarUranium.INSTANCE, 2, 38)
      );
      URANIUM_ROD_CHARCOAL_DUAL = registerItem(
         new ReactorUraniumRod("uranium_rod_charcoal_dual", "reactor/cells/uranium/dual", "charcoal_enriched", CharcoalUranium.INSTANCE, 2, 41)
      );
      URANIUM_ROD_QUAD = registerItem(new ReactorUraniumRod("uranium_rod_quad", "reactor/cells/uranium/quad", "base", StandardUranium.INSTANCE, 4, 2));
      URANIUM_ROD_REDSTONE_QUAD = registerItem(
         new ReactorUraniumRod("uranium_rod_redstone_quad", "reactor/cells/uranium/quad", "redstone_enriched", RedstoneUranium.INSTANCE, 4, 5)
      );
      URANIUM_ROD_BLAZE_QUAD = registerItem(
         new ReactorUraniumRod("uranium_rod_blaze_quad", "reactor/cells/uranium/quad", "blaze_enriched", BlazeUranium.INSTANCE, 4, 8)
      );
      URANIUM_ROD_ENDER_PEARL_QUAD = registerItem(
         new ReactorUraniumRod("uranium_rod_ender_pearl_quad", "reactor/cells/uranium/quad", "enderpearl_enriched", EnderUranium.INSTANCE, 4, 11)
      );
      URANIUM_ROD_NETHER_STAR_QUAD = registerItem(
         new ReactorUraniumRod("uranium_rod_nether_star_quad", "reactor/cells/uranium/quad", "netherstar_enriched", NetherStarUranium.INSTANCE, 4, 39)
      );
      URANIUM_ROD_CHARCOAL_QUAD = registerItem(
         new ReactorUraniumRod("uranium_rod_charcoal_quad", "reactor/cells/uranium/quad", "charcoal_enriched", CharcoalUranium.INSTANCE, 4, 42)
      );
      URANIUM_ROD_ISOTOPIC = registerItem(
         new ReactorIsotopicUraniumRod("uranium_rod_isotopic", "reactor/cells/uranium/isotopic", "base", StandardUranium.INSTANCE, 36)
      );
      URANIUM_ROD_ISOTOPIC_REDSTONE = registerItem(
         new ReactorIsotopicUraniumRod("uranium_rod_isotopic_redstone", "reactor/cells/uranium/isotopic", "redstone_enriched", RedstoneUranium.INSTANCE, 43)
      );
      URANIUM_ROD_ISOTOPIC_BLAZE = registerItem(
         new ReactorIsotopicUraniumRod("uranium_rod_isotopic_blaze", "reactor/cells/uranium/isotopic", "blaze_enriched", BlazeUranium.INSTANCE, 44)
      );
      URANIUM_ROD_ISOTOPIC_ENDER_PEARL = registerItem(
         new ReactorIsotopicUraniumRod("uranium_rod_isotopic_ender_pearl", "reactor/cells/uranium/isotopic", "enderpearl_enriched", EnderUranium.INSTANCE, 45)
      );
      URANIUM_ROD_ISOTOPIC_NETHER_STAR = registerItem(
         new ReactorIsotopicUraniumRod(
            "uranium_rod_isotopic_nether_star", "reactor/cells/uranium/isotopic", "netherstar_enriched", NetherStarUranium.INSTANCE, 46
         )
      );
      URANIUM_ROD_ISOTOPIC_CHARCOAL = registerItem(
         new ReactorIsotopicUraniumRod("uranium_rod_isotopic_charcoal", "reactor/cells/uranium/isotopic", "charcoal_enriched", CharcoalUranium.INSTANCE, 47)
      );
   }
}
