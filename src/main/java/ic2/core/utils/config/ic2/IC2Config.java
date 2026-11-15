package ic2.core.utils.config.ic2;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.HashSetCache;
import carbonconfiglib.config.ConfigEntry.ArrayValue;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.config.ConfigEntry.DoubleValue;
import carbonconfiglib.config.ConfigEntry.EnumValue;
import carbonconfiglib.config.ConfigEntry.IntValue;
import carbonconfiglib.config.ConfigEntry.ParsedValue;
import carbonconfiglib.gui.api.types.CompoundType;
import carbonconfiglib.gui.api.types.EntrySettingTypes.FloatingSlider;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.impl.entries.WidgetAligner;
import ic2.core.IC2;
import ic2.core.item.renders.features.EnergyHudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2Config {
   private ConfigHandler handler;
   public BoolValue disableBronzeTools;
   public BoolValue disableAluminumTools;
   public BoolValue disableBronzeArmor;
   public BoolValue disableAlternateVanillaCrafting;
   public BoolValue disableGlowstoneCrafting;
   public BoolValue disableGunpowderCrafting;
   public BoolValue disableExplosiveCrafting;
   public IntValue limitVillagerDrinks;
   public BoolValue disableEnergyNetCrash;
   public BoolValue tileProfiler;
   public BoolValue itemNBT;
   public BoolValue recipeHiding;
   public BoolValue showUndamagedDurability;
   public BoolValue fancyFluidExpansion;
   public BoolValue showDefaultRecyclerRecipe;
   public BoolValue boostOnSprint;
   public BoolValue pauseWiki;
   public BoolValue showCropInfo;
   public DoubleValue masterVolume;
   public DoubleValue blockVolume;
   public DoubleValue itemVolume;
   public DoubleValue backVolume;
   public EnumValue<CropParticles> cropParticles;
   public BoolValue separateStorageTextures;
   public IntValue wikiGuiScale;
   public IConfigSerializer<WidgetAligner> hudSerializer;
   public ParsedValue<WidgetAligner> hud;
   public EnumValue<EnergyHudRenderer.RenderOrientation> hudOrientation;
   public IntValue reactorOutput;
   public DoubleValue steamReactorMultiplier;
   public IntValue generatorOutput;
   public IntValue slagGenOutput;
   public IntValue geothermalOutput;
   public DoubleValue fluidGenMultiplier;
   public DoubleValue solarTurbine;
   public IntValue oceanGenOutput;
   public IntValue waveGenOutput;
   public IntValue steamTunnelOutput;
   public IntValue steamTurbineOutput;
   public IntValue windTurbinePassive;
   public IntValue windMillOutput;
   public PassiveGeneratorSetting thermalGenerator;
   public PassiveGeneratorSetting waterMill;
   public PassiveGeneratorSetting lvWaterMill;
   public PassiveGeneratorSetting mvWaterMill;
   public PassiveGeneratorSetting hvWaterMill;
   public IntValue solarPanel;
   public IntValue lvSolarPanel;
   public IntValue mvSolarPanel;
   public IntValue hvSolarPanel;
   public EnumValue<CropDifficulty> cropDifficulty;
   public BoolValue cropPittySystem;
   public BoolValue energyEasyMode;
   public DoubleValue electricSuitAbsorptionScale;
   public DoubleValue electricSuitEnergyCostModifier;
   public IntValue fluxBalance;
   public IntValue industrialWorkbenchExpansionLimit;
   public IntValue maxReactorTicks;
   public BoolValue reducedWhiskyBrewTime;
   public DoubleValue nukeDamage;
   public DoubleValue reactorDamage;
   public IntValue windChangeTime;
   public IntValue windStreams;
   public BoolValue oreTin;
   public BoolValue oreSilver;
   public BoolValue oreUranium;
   public BoolValue oreNetherSilver;
   public BoolValue oreNetherAluminium;
   public BoolValue oreRubberTree;
   public BoolValue enableSpecialElectricArmor;
   public BoolValue originalQuantumBehavior;
   public BoolValue enableHardEnrichedUranium;
   public BoolValue teleporterKeepItems;
   public BoolValue experimentalSourcesSorting;
   public BoolValue gassesTravelInfinite;
   public BoolValue allowMachineDrain;
   public ArrayValue ignoreEnchantability;
   public HashSetCache<String> ignoreEnchantabilityCheck;

   public IC2Config() {
      Config config = new Config("ic2");
      this.handler = CarbonConfig.CONFIGS.createConfig(config, ConfigSettings.withFolder("ic2c"));
      ConfigSection balance = config.add("balance");
      ConfigSection generators = balance.addSubSection("generators");
      this.reactorOutput = generators.addInt("nuclearGeneratorMultiplier", 20);
      this.steamReactorMultiplier = (DoubleValue)generators.addDouble("steamReactorMultiplier", 2.0).setMin(0.1).setServerSynced();
      this.generatorOutput = (IntValue)generators.addInt("fuelGeneratorBase", 10).setServerSynced();
      this.slagGenOutput = (IntValue)generators.addInt("slagGenBase", 60).setServerSynced();
      this.geothermalOutput = (IntValue)generators.addInt("geoGeneratorBase", 20).setServerSynced();
      this.solarTurbine = (DoubleValue)generators.addDouble("solarTurbineMultiplier", 1.0).setMin(0.1).setServerSynced();
      this.steamTurbineOutput = (IntValue)generators.addInt("steamTurbineBase", 64).setServerSynced();
      this.steamTunnelOutput = (IntValue)generators.addInt("steamTunnelBase", 512).setServerSynced();
      this.fluidGenMultiplier = (DoubleValue)generators.addDouble("fluidGeneratorMultiplier", 1.0).setServerSynced();
      this.oceanGenOutput = (IntValue)generators.addInt("oceanGenBase", 100).setServerSynced();
      this.waveGenOutput = (IntValue)generators.addInt("waveGenBase", 15).setServerSynced();
      this.windTurbinePassive = (IntValue)generators.addInt(
            "windTurbinePassive", 100, new String[]{"How much passive energy should be required per fuel. Lower => more Production"}
         )
         .setServerSynced();
      this.windMillOutput = (IntValue)generators.addInt("windmillOutput", 256).setServerSynced();
      this.thermalGenerator = new PassiveGeneratorSetting(generators, "thermalGenerator", 30, 2000);
      this.waterMill = new PassiveGeneratorSetting(generators, "waterMill", 1, 100);
      this.lvWaterMill = new PassiveGeneratorSetting(generators, "lvWaterMill", 4, 100);
      this.mvWaterMill = new PassiveGeneratorSetting(generators, "mvWaterMill", 32, 100);
      this.hvWaterMill = new PassiveGeneratorSetting(generators, "hvWaterMill", 256, 100);
      this.solarPanel = (IntValue)generators.addInt("solarPanelBase", 1).setServerSynced();
      this.lvSolarPanel = (IntValue)generators.addInt("lvSolarPanelBase", 8).setServerSynced();
      this.mvSolarPanel = (IntValue)generators.addInt("mvSolarPanelBase", 64).setServerSynced();
      this.hvSolarPanel = (IntValue)generators.addInt("hvSolarPanelBase", 512).setServerSynced();
      this.energyEasyMode = balance.addBool(
         "energyEasyMode",
         false,
         new String[]{
            "Disables Explosions due to overcharging them. Energy will simply not flow if an explosion was to happened instead. Also disables Transformer Upgrades"
         }
      );
      ConfigSection blocks = balance.addSubSection("blocks");
      ConfigSection items = balance.addSubSection("items");
      this.electricSuitAbsorptionScale = (DoubleValue)items.addDouble(
            "electricSuitAbsorptionScale", 1.0, new String[]{"Sets the Absorption Ratio of Electric Armor"}
         )
         .setRange(0.01, 500.0)
         .addSettings(new FloatingSlider(0.01));
      this.electricSuitEnergyCostModifier = (DoubleValue)items.addDouble(
            "electricSuitEnergyCostModifier", 1.0, new String[]{"Modifies the Energy Cost per damage"}
         )
         .setRange(0.01, 500.0)
         .addSettings(new FloatingSlider(0.01));
      this.fluxBalance = blocks.addInt("fluxBalance", 4, new String[]{"RF per EU Produced"}).setRange(1, 1000);
      this.industrialWorkbenchExpansionLimit = (IntValue)blocks.addInt(
            "expansion_limit", 6, new String[]{"Defines the Expansion Limit for the Industrial Worktable"}
         )
         .setRange(0, 20)
         .setServerSynced();
      this.maxReactorTicks = blocks.addInt(
            "maxSimulationTicks",
            100,
            new String[]{
               "This limits how many simulation ticks can be done in the reactor planner. This logic is off-thread and is dumped into a thread pool."
            }
         )
         .setRange(0, 10000);
      this.reducedWhiskyBrewTime = blocks.addBool(
         "reducedWhiskyBrewtime", false, new String[]{"Reduces the Whisky Brew time by the factor of 10, This will only work in singleplayer"}
      );
      this.cropDifficulty = blocks.addEnum(
         "cropDifficulty",
         CropDifficulty.NORMAL,
         CropDifficulty.class,
         new String[]{"Decides how hard IC2 Crops are. This affects, growth speed/stat breeding/requirements and a few other things."}
      );
      this.cropPittySystem = blocks.addBool(
         "crop-pitty-system",
         false,
         new String[]{"Enables a Crop Pitty System that takes effect when cropsticks fail to breed for over 20 mins or around 100 attempts"}
      );
      ConfigSection client = config.add("client");
      this.showUndamagedDurability = client.addBool(
         "showUndamagedDurability", true, new String[]{"Show durability for undamaged, damageable items in advanced tooltip"}
      );
      this.fancyFluidExpansion = client.addBool("enableFluidExpansionRenderer", true, new String[]{"Show the Tanks contents in the World"});
      this.showDefaultRecyclerRecipe = client.addBool("showDefaultRecyclerRecipe", true, new String[]{"Shows the default recycle recipe in JEI"});
      this.boostOnSprint = client.addBool(
         "boostOnSprint", true, new String[]{"Decides if you use Speed Module Boost on Sprinting or requiring a dedicated key"}
      );
      this.pauseWiki = client.addBool(
         "pauseWiki",
         false,
         new String[]{"Decides if the Wiki Pauses the game in singleplayer or not. Reduces the chance to be blown up by creepers while reading"}
      );
      this.cropParticles = client.addEnum(
         "crop particles", CropParticles.ALL, CropParticles.class, new String[]{"Decides how many crop particles are shown when a ic2 crop grows"}
      );
      this.showCropInfo = client.addBool("showCropInfo", true, new String[]{"Displays the Farmland/SubSoil information on blocks that have them for IC2 Crops"});
      this.separateStorageTextures = client.addBool(
         "separateStorageTextures",
         false,
         new String[]{"Changes that the energy storage textures are separate files instead of a single one that morphs. Some resource packs may require this"}
      );
      this.wikiGuiScale = client.addInt(
         "wikiGuiScale", -1, new String[]{"Sets the Scale of the wiki. -1 uses the default system. >=0 basically sets the MC gui scale for just the wiki."}
      );
      ConfigSection audio = client.addSubSection("audio");
      this.masterVolume = (DoubleValue)audio.addDouble("master", 0.5).setRange(0.01, 5.0).addSettings(new FloatingSlider(0.01));
      this.blockVolume = (DoubleValue)audio.addDouble("block", 1.0).setRange(0.01, 5.0).addSettings(new FloatingSlider(0.01));
      this.itemVolume = (DoubleValue)audio.addDouble("item", 1.0).setRange(0.01, 5.0).addSettings(new FloatingSlider(0.01));
      this.backVolume = (DoubleValue)audio.addDouble("backpack", 1.0).setRange(0.01, 5.0).addSettings(new FloatingSlider(0.01));
      ConfigSection armorHud = client.addSubSection("armorHUD");
      this.hudSerializer = WidgetAligner.create(IC2Config.HudLinker.class, true);
      this.hud = armorHud.addParsed("hud", new WidgetAligner(Align.START, Align.CENTER, 0.0F, 0.0F), this.hudSerializer);
      this.hudOrientation = armorHud.addEnum("hudOrientation", EnergyHudRenderer.RenderOrientation.VERTICAL, EnergyHudRenderer.RenderOrientation.class);
      ConfigSection crafting = config.add("crafting");
      this.disableBronzeTools = crafting.addBool("disableBronzeTools", false, new String[]{"Disable the recipes for bronze tools"});
      this.disableAluminumTools = crafting.addBool("disableAluminumTools", false, new String[]{"Disable the recipes for aluminum tools"});
      this.disableBronzeArmor = crafting.addBool("disableBronzeArmor", false, new String[]{"Disable the recipes for bronze armor"});
      this.disableAlternateVanillaCrafting = crafting.addBool(
         "disableAlternateVanillaRecipes", false, new String[]{"Disable the alternate crafting recipes for Vanilla stuff"}
      );
      this.recipeHiding = crafting.addBool("enableSecretRecipeHiding", true, new String[]{"Enable hiding of secret recipes in CraftGuide/NEI/JEI"});
      this.disableGlowstoneCrafting = crafting.addBool("disableGlowstoneRecipe", false, new String[]{"Disable the Glowstone crafting recipe"});
      this.disableGunpowderCrafting = crafting.addBool("disableGunpowderRecipe", false, new String[]{"Disable the Gunpowder crafting recipe"});
      this.disableExplosiveCrafting = crafting.addBool("disableExplosiveRecipes", false, new String[]{"Disable the crafting recipes for IC2 Explosives"});
      this.limitVillagerDrinks = crafting.addInt(
            "limitVillagerDrinks",
            -1,
            new String[]{
               "Defines how many drinks in total are going to be added max to villagers trades. -1 as many as it wants, 5 means that it will at most add 5 trades into each tade pool. Which ones are left is random"
            }
         )
         .setRange(-1, 100);
      ConfigSection features = config.add("features");
      this.enableSpecialElectricArmor = features.addBool(
         "enableSpecialElectricArmor", true, new String[]{"Enables Electric Damage absorption on Electric Armor"}
      );
      this.originalQuantumBehavior = features.addBool("originalQuantumBehavior", false, new String[]{"Enables the original Quantum Boost behavior"});
      this.enableHardEnrichedUranium = features.addBool(
         "enableHardEnrichedUranium", true, new String[]{"Enable Harder Enriched Uranium Processing. Making it harder to create the enriched rods"}
      );
      this.teleporterKeepItems = features.addBool(
         "enableTeleporterInventory", true, new String[]{"Enable calculation of inventory weight when going through a teleporter"}
      );
      this.experimentalSourcesSorting = features.addBool(
         "experimentalEnergyNetSourcesSorting",
         true,
         new String[]{
            "Enables a Experimenting feature that changes the execution order of energy emitters.\nPrioritizing Infinite/uncontrollable energy sources over finite/controllable energy sources.\nThis should result in a much more user-friendly energy grid.\nThis change is still experimental though and should be toggleable.\nNote: This only changes the execution order and doesn't make generators smart"
         }
      );
      this.gassesTravelInfinite = features.addBool(
         "gassesFlowInfinite", false, new String[]{"Makes so that gasses have infinite pressure with the Pipe System"}
      );
      this.allowMachineDrain = features.addBool(
         "enableMachineDrain", true, new String[]{"Allows machines to be drained using batteries by clicking on the charge bar"}
      );
      ConfigSection world = config.add("world");
      this.nukeDamage = world.addDouble("nukeDamage", 35.0, new String[]{"Sets the strength of a Nuclear Explosion"});
      this.reactorDamage = world.addDouble("reactorDamage", 45.0, new String[]{"Sets the max strength of a Nuclear Reactor Explosion"});
      this.windChangeTime = world.addInt("windUpdateRate", 6000, new String[]{"Sets the time in Ticks for the Wind Update Cycle"});
      this.windStreams = world.addInt("windStreams", 5, new String[]{"Sets how many Wind Streams there are"}).setRange(1, 20);
      ConfigSection gen = world.addSubSection("gen");
      this.oreTin = gen.addBool("Tin Ore Gen", true);
      this.oreSilver = gen.addBool("Silver Ore Gen", true);
      this.oreUranium = gen.addBool("Uranium Ore Gen", true);
      this.oreRubberTree = gen.addBool("Rubber Tree Gen", true);
      this.oreNetherSilver = gen.addBool("Nether Silver Ore Gen", true);
      this.oreNetherAluminium = gen.addBool("Nether Aluminium Ore Gen", true);
      ConfigSection compatibility = config.add("compat");
      this.ignoreEnchantability = (ArrayValue)compatibility.addArray(
            "ignoreEnchantmentCompatibilityCheck", "Enchantment/Mod IDs for enchantments where canApplyAtEnchantingTable should be ignored"
         )
         .setServerSynced()
         .addSuggestionProvider(Suggestions.INSTANCE);
      this.ignoreEnchantabilityCheck = HashSetCache.create(this.ignoreEnchantability, this.handler);
      ConfigSection debug = config.add("debug");
      this.tileProfiler = debug.addBool(
         "Enable Tile Profiler",
         false,
         new String[]{
            "Enables the Tile Profiler, Doubles Lag in worst cases. Requires TheOneProbe to see data, Uses Hud Key when looking at a block shows lag of Ticking Block"
         }
      );
      this.itemNBT = debug.addBool("Display Item NBT", false, new String[]{"Enables that Ctrl shows Item NBTData and Shift Prettifies it."});
      this.disableEnergyNetCrash = debug.addBool(
         "Disable Crash Validation",
         false,
         new String[]{
            "Just a Temp Config that exists as a failsafe if a fix didn't work so it can be solved temporarily. Will be removed if validated that this crash was solved"
         }
      );
      this.handler.register();
      this.addLoadedListener(IC2.PLATFORM::sendPlayerSpecificConfig);
   }

   public void load() {
      this.handler.load();
   }

   public void save() {
      this.handler.save();
   }

   public void addLoadedListener(Runnable listener) {
      this.handler.addLoadedListener(listener);
   }

   @OnlyIn(Dist.CLIENT)
   public void registerOverlay() {
      CompoundType.registerWidgetAligner(IC2Config.HudLinker.class, new EnergyHudRenderer.HudRenderer(), this.hudSerializer);
   }

   public Config getConfig() {
      return this.handler.getConfig();
   }

   public ConfigHandler getHandler() {
      return this.handler;
   }

   public static class HudLinker {
   }
}
