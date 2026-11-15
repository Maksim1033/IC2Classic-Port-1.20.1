package ic2.core;

import ic2.api.addons.IModule;
import ic2.api.blocks.IWrenchable;
import ic2.api.blocks.PainterHelper;
import ic2.api.core.APIHelper;
import ic2.api.core.IC2Classic;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.INetworkManager;
import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.api.ticks.ITickScheduler;
import ic2.core.audio.AudioManager;
import ic2.core.audio.AudioManagerClient;
import ic2.core.block.crops.CropRegistry;
import ic2.core.block.machines.logic.planner.PlannerRegistry;
import ic2.core.energy.EnergyNetGlobal;
import ic2.core.inventory.base.ICurioGetter;
import ic2.core.item.food_and_drink.IC2FoodsAndDrinks;
import ic2.core.item.manager.GatewayElectricItemManager;
import ic2.core.item.misc.tfbp.bp.IrrigationBluePrint;
import ic2.core.item.tool.MinerRemoteItem;
import ic2.core.networking.NetworkManager;
import ic2.core.platform.events.ExportCommand;
import ic2.core.platform.events.IC2EventHandler;
import ic2.core.platform.events.InternalEventHandler;
import ic2.core.platform.events.MultiBlockManager;
import ic2.core.platform.events.MultiMineToolHandler;
import ic2.core.platform.events.StructureManager;
import ic2.core.platform.events.WorldGenerator;
import ic2.core.platform.player.Keyboard;
import ic2.core.platform.player.KeyboardClient;
import ic2.core.platform.player.ReactorSubmissions;
import ic2.core.platform.recipes.PackHack;
import ic2.core.platform.recipes.crafting.IC2RecipeSerializer;
import ic2.core.platform.recipes.helpers.Boxables;
import ic2.core.platform.recipes.helpers.IC2RecipeLoader;
import ic2.core.platform.recipes.helpers.ItemStackCache;
import ic2.core.platform.recipes.helpers.LootOverride;
import ic2.core.platform.recipes.misc.GlobalRecipes;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Capabilities;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Fuels;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Paintings;
import ic2.core.platform.registries.IC2People;
import ic2.core.platform.registries.IC2Potions;
import ic2.core.platform.registries.IC2Recipes;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.registries.IC2Villagers;
import ic2.core.platform.registries.IC2Wiki;
import ic2.core.platform.registries.TickScheduler;
import ic2.core.utils.IC2ItemGroup;
import ic2.core.utils.SidedGateway;
import ic2.core.utils.compat.NamedThreadFactory;
import ic2.core.utils.config.ic2.IC2Config;
import ic2.core.utils.helpers.ModCache;
import ic2.core.utils.plugins.IJEIHelper;
import ic2.core.utils.plugins.PluginLoader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("ic2")
public class IC2 implements APIHelper {
   public static final int API_VERSION = 2;
   public static final ExecutorService OFF_THREAD_WORKER = new ThreadPoolExecutor(
      0,
      Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
      1L,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(),
      new NamedThreadFactory("IC2 Off-thread Tasks ")
   );
   public static Logger LOGGER = LogManager.getLogger("IC2Classic");
   public static IC2 INSTANCE;
   public static String MOD_ID = "ic2";
   public static CreativeModeTab IC2_MAIN_GROUP = new IC2ItemGroup();
   public static CreativeModeTab FOOD_AND_DRINK_GROUP = new IC2ItemGroup.IC2FoodAndDinkItemGroup();
   public static CreativeModeTab CFOAM_GROUP = new IC2ItemGroup.IC2CFoamItemGroup();
   public static final Platform PLATFORM = (Platform)DistExecutor.unsafeRunForDist(() -> PlatformClient::new, () -> Platform::new);
   public static final SidedGateway<NetworkManager> NETWORKING = new SidedGateway<>(
      "ic2.core.networking.NetworkManager", "ic2.core.networking.NetworkManagerClient"
   );
   public static final SidedGateway<GlobalRecipes> RECIPES = new SidedGateway<>(
      "ic2.core.platform.recipes.misc.GlobalRecipes", "ic2.core.platform.recipes.misc.GlobalRecipesClient"
   );
   public static final AudioManager AUDIO = (AudioManager)DistExecutor.unsafeRunForDist(() -> AudioManagerClient::new, () -> AudioManager::new);
   public static final Keyboard KEYBOARD = (Keyboard)DistExecutor.unsafeRunForDist(() -> KeyboardClient::new, () -> Keyboard::new);
   public static final TickScheduler TICK_HANDLER = new TickScheduler();
   public static IC2Config CONFIG;
   public static final PluginLoader PLUGINS = new PluginLoader();
   public static final ModCache MOD_CACHE = new ModCache();
   public static ICurioGetter CURIO_PLUGIN = new ICurioGetter.DummyGetter();
   public static IJEIHelper JEI_PLUGIN = new IJEIHelper.Dummy();
   public static final IEventBus EVENT_BUS = BusBuilder.builder().build();

   public IC2() {
      ForgeMod.enableMilkFluid();
      IC2Classic.setHelper(this);
      IC2People.init();
      CONFIG = new IC2Config();
      INSTANCE = this;
      IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
      bus.addListener(this::postInit);
      if (FMLEnvironment.dist.isClient()) {
         bus.addListener(this::onClientInit);
      }

      bus.addListener(EventPriority.LOWEST, this::complete);
      bus.addListener(this::registerContent);
      bus.addListener(this::onRegistriesAdded);
      bus.addListener(IC2Capabilities::registerCapabilities);
      MinecraftForge.EVENT_BUS.register(IC2EventHandler.INSTANCE);
      MinecraftForge.EVENT_BUS.addListener(this::onCommandLoad);
      MinecraftForge.EVENT_BUS.addListener(this::onServerStartingEvent);
      MinecraftForge.EVENT_BUS.register(InternalEventHandler.INSTANCE);
      MinecraftForge.EVENT_BUS.register(IC2Fuels.INSTANCE);
      MinecraftForge.EVENT_BUS.register(StructureManager.INSTANCE);
      MinecraftForge.EVENT_BUS.register(MultiBlockManager.INSTANCE);
      MinecraftForge.EVENT_BUS.register(MultiMineToolHandler.INSTANCE);
      MinecraftForge.EVENT_BUS.addListener(IC2Villagers::onVillagerEvent);
      MinecraftForge.EVENT_BUS.addListener(IC2Villagers::onWanderingEvent);
      MinecraftForge.EVENT_BUS.addListener(this::onResourceReload);
      IC2RecipeSerializer.register();
      GatewayElectricItemManager.init();
      PainterHelper.init();
      PLATFORM.preInit();
      KEYBOARD.init();
      NETWORKING.get(FMLEnvironment.dist.isDedicatedServer()).init();
      RECIPES.get(true).init();
      if (FMLEnvironment.dist.isClient()) {
         RECIPES.get(false).setRecipes(false);
         CONFIG.registerOverlay();
      }

      AUDIO.init();
      PLUGINS.init(2, CONFIG.getConfig().add("plugins"));
      CONFIG.save();
      PLUGINS.preInitFeatures();
      PLUGINS.fireEvent(t -> t.preInit(bus));
   }

   @Override
   public List<ItemStack> getFluidContainers(Fluid fluid) {
      return ItemStackCache.INSTANCE.getFluidContainers(fluid);
   }

   @Override
   public FluidStack createSteam(int amount) {
      return new FluidStack(IC2Fluids.STEAM, amount);
   }

   @Override
   public ITickScheduler getTickHelper() {
      return TICK_HANDLER;
   }

   @Override
   public INetworkManager getNetworkManager() {
      return NETWORKING.get();
   }

   @Override
   public INetworkManager getNetworkManager(Dist side) {
      return NETWORKING.get(side.isDedicatedServer());
   }

   public void registerContent(RegisterEvent event) {
      if (event.getRegistryKey().equals(Keys.ENTITY_TYPES)) {
         LOGGER.info("Loading Entities");
         IC2Entities.init();
         LOGGER.info("Loaded Entities");
      } else if (event.getRegistryKey().equals(Keys.BLOCKS)) {
         LOGGER.info("Loading TileEntities");
         IC2Tiles.loadTiles();
         LOGGER.info("Loading Blocks");
         IC2Blocks.addBlocks();
         LOGGER.info("Loaded Blocks & Tiles");
      } else if (event.getRegistryKey().equals(Keys.ITEMS)) {
         LOGGER.info("Loading Items");
         IC2Items.addItems();
         IC2FoodsAndDrinks.fillContainerList();
         LOGGER.info("Loaded Items");
      } else if (event.getRegistryKey().equals(Keys.FLUIDS)) {
         LOGGER.info("Loading Fluids");
         IC2Fluids.loadFluids();
         LOGGER.info("Loaded Fluids");
      } else if (event.getRegistryKey().equals(Keys.MOB_EFFECTS)) {
         LOGGER.info("Loading Potions");
         IC2Potions.init();
         LOGGER.info("Loaded Potions");
      } else if (event.getRegistryKey().equals(Keys.POI_TYPES)) {
         LOGGER.info("Loading Villager Workstations");
         IC2Villagers.loadPOIs();
         LOGGER.info("Loaded Villager Workstations");
      } else if (event.getRegistryKey().equals(Keys.VILLAGER_PROFESSIONS)) {
         LOGGER.info("Loading Villager Noises");
         IC2Villagers.loadVillagers();
         LOGGER.info("Loaded Villager Noises");
      } else if (event.getRegistryKey().equals(Keys.PAINTING_VARIANTS)) {
         LOGGER.info("Loading Child Drawings");
         IC2Paintings.init();
         LOGGER.info("Loaded Child Drawings");
      } else if (event.getRegistryKey().equals(Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS)) {
         event.getForgeRegistry().register(new ResourceLocation("ic2", "ic2_loot"), LootOverride.CODEC);
      } else if (event.getRegistryKey().equals(Keys.FEATURES)) {
         WorldGenerator.generateOres();
      } else if (event.getRegistryKey().equals(Registries.FOLIAGE_PLACER_TYPE)) {
         event.register(Registries.FOLIAGE_PLACER_TYPE, new ResourceLocation("ic2", "rubber_leaves"), () -> WorldGenerator.RUBBER_FOLIAGE);
      } else if (event.getRegistryKey().equals(Registries.TRUNK_PLACER_TYPE)) {
         event.register(Registries.TRUNK_PLACER_TYPE, new ResourceLocation("ic2", "rubber_trunk"), () -> WorldGenerator.RUBBER_TRUNK);
      } else if (event.getRegistryKey().equals(Keys.BIOME_MODIFIER_SERIALIZERS)) {
         event.getForgeRegistry().register(new ResourceLocation("ic2", "modifier"), WorldGenerator.IC2Modifier.CODEC);
      } else if (event.getRegistryKey().equals(Keys.BIOME_MODIFIERS)) {
         event.getForgeRegistry().register(new ResourceLocation("ic2", "modifier"), new WorldGenerator.IC2Modifier());
      } else if (event.getRegistryKey().equals(Keys.STAT_TYPES)) {
         IC2Stats.init();
      } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
         event.register(Registries.CREATIVE_MODE_TAB, new ResourceLocation("ic2", "main"), () -> IC2_MAIN_GROUP);
         event.register(Registries.CREATIVE_MODE_TAB, new ResourceLocation("ic2", "cfoam"), () -> CFOAM_GROUP);
         event.register(Registries.CREATIVE_MODE_TAB, new ResourceLocation("ic2", "food_and_drink"), () -> FOOD_AND_DRINK_GROUP);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void onClientInit(RegisterParticleProvidersEvent setup) {
      PLATFORM.onClientLoad();
   }

   public void postInit(FMLCommonSetupEvent setup) {
      LOGGER.info("CommonSetup");
      IrrigationBluePrint.INSTANCE.init();
      IWrenchable.WrenchRegistry.INSTANCE.init();
      CropRegistry.REGISTRY.register();
      EnergyNetGlobal.INSTANCE.init();
      PLATFORM.init();
      PainterHelper.INSTANCE.registerPaintHelper(IC2Blocks.RUBBERWOOD_SIGN, new PainterHelper.SignPainter());
      PainterHelper.INSTANCE.registerPaintHelper(IC2Blocks.RUBBERWOOD_WALL_SIGN, new PainterHelper.SignPainter());
      ComposterBlock.COMPOSTABLES.put(IC2Items.HEMP_SEEDS, 0.3F);
      ComposterBlock.COMPOSTABLES.put(IC2Items.HEMP, 0.65F);
      IC2Tags.initTags();
      Boxables.loadBoxableItems();
      PlannerRegistry.init();
      ReactorSubmissions.INSTANCE.init();
      IC2Wiki.init();
      PLATFORM.addLangFile(new ResourceLocation("ic2", "blocks"));
      PLATFORM.addLangFile(new ResourceLocation("ic2", "items"));
      PLATFORM.addLangFile(new ResourceLocation("ic2", "crops"));
      PLATFORM.addLangFile(new ResourceLocation("ic2", "advancements"));
      PLATFORM.addLangFile(new ResourceLocation("ic2", "stats"));
      PLATFORM.addLangFile(new ResourceLocation("ic2", "probe"));
      if (!(CURIO_PLUGIN instanceof ICurioGetter.DummyGetter)) {
         ElectricItem.setCurioSupport(CURIO_PLUGIN::getCurioHandler);
      }

      PLUGINS.postInitFeatures();
      PLUGINS.fireEvent(IModule::postInit);
   }

   public void onServerStartingEvent(ServerAboutToStartEvent event) {
      if (event.getServer() instanceof DedicatedServer) {
         ItemStackCache.INSTANCE.init();
      }
   }

   public void complete(FMLLoadCompleteEvent event) {
      if (FMLEnvironment.dist.isClient()) {
         ItemStackCache.INSTANCE.init();
      }
   }

   public void onRegistriesAdded(AddPackFindersEvent event) {
      if (event.getPackType() == PackType.SERVER_DATA) {
         PackHack.loadHack(event::addRepositorySource);
      }
   }

   public void onCommandLoad(RegisterCommandsEvent starting) {
      if (starting.getCommandSelection() == CommandSelection.INTEGRATED) {
         ExportCommand.export(starting.getDispatcher());
      }

      ExportCommand.energyNet(starting.getDispatcher());
      MinerRemoteItem.requestRocketMinerBack(starting.getDispatcher());
   }

   public void onResourceReload(AddReloadListenerEvent listener) {
      listener.addListener(IC2RecipeLoader.INSTANCE);

      try {
         RECIPES.get(true).reloadRecipes();
      } catch (Exception var4) {
         LOGGER.info("Reloading Recipes Crash", var4);
      }

      try {
         IC2Advancements.reload();
      } catch (Exception var3) {
         LOGGER.info("Reloading Advancements Crash", var3);
      }
   }

   public void loadRecipes(IAdvancedCraftingManager registry) {
      LOGGER.info("Loading Recipes");
      IC2Recipes.addRecipes(registry);
   }
}
