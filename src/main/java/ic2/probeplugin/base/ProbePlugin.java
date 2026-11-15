package ic2.probeplugin.base;

import ic2.api.crops.ICropTile;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.block.base.tiles.impls.BaseChargingBenchTileEntity;
import ic2.core.block.base.tiles.impls.BaseCropLibraryTileEntity;
import ic2.core.block.base.tiles.impls.BaseElectricLoaderTileEntity;
import ic2.core.block.base.tiles.impls.BaseElectricUnloaderTileEntity;
import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import ic2.core.block.base.tiles.impls.BaseFluxGeneratorTileEntity;
import ic2.core.block.base.tiles.impls.BaseGeneratorTileEntity;
import ic2.core.block.base.tiles.impls.BaseTransformerTileEntity;
import ic2.core.block.base.tiles.impls.machine.multi.BaseMultiMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.cables.CableTileEntity;
import ic2.core.block.cables.luminator.ConstructionLightTileEntity;
import ic2.core.block.cables.luminator.LuminatorTileEntity;
import ic2.core.block.cables.mointor.MonitorTileEntity;
import ic2.core.block.generators.tiles.FuelBoilerTileEntity;
import ic2.core.block.generators.tiles.OceanGeneratorTileEntity;
import ic2.core.block.generators.tiles.SolarPanelTileEntity;
import ic2.core.block.generators.tiles.SteamTurbineTileEntity;
import ic2.core.block.generators.tiles.WaveGenTileEntity;
import ic2.core.block.generators.tiles.WindmillTileEntity;
import ic2.core.block.machines.tiles.ev.CrafterTileEntity;
import ic2.core.block.machines.tiles.ev.PlasmafierTileEntity;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import ic2.core.block.machines.tiles.hv.MassFabricatorTileEntity;
import ic2.core.block.machines.tiles.hv.OreScannerTileEntity;
import ic2.core.block.machines.tiles.hv.TeleporterTileEntity;
import ic2.core.block.machines.tiles.hv.TerraformerTileEntity;
import ic2.core.block.machines.tiles.hv.UraniumEnricherTileEntity;
import ic2.core.block.machines.tiles.hv.VillagerOMatTileEntity;
import ic2.core.block.machines.tiles.luv.TeleporterHubTileEntity;
import ic2.core.block.machines.tiles.lv.CropMatronTileEntity;
import ic2.core.block.machines.tiles.lv.ElectrolyzerTileEntity;
import ic2.core.block.machines.tiles.lv.MachineBufferTileEntity;
import ic2.core.block.machines.tiles.lv.MachineTankTileEntity;
import ic2.core.block.machines.tiles.lv.MagnetizerTileEntity;
import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.block.machines.tiles.lv.PumpTileEntity;
import ic2.core.block.machines.tiles.lv.WoodGassifierTileEntity;
import ic2.core.block.machines.tiles.mv.BaseTeleporterTileEntity;
import ic2.core.block.machines.tiles.mv.ChargedElectrolyzerTileEntity;
import ic2.core.block.machines.tiles.mv.ChunkloaderTileEntity;
import ic2.core.block.machines.tiles.mv.CropHarvesterTileEntity;
import ic2.core.block.machines.tiles.mv.RangedPumpTileEntity;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.block.machines.tiles.mv.TeslaCoilTileEntity;
import ic2.core.block.machines.tiles.nv.StoneBasicMachineTileEntity;
import ic2.core.block.machines.tiles.nv.StoneCannerTileEntity;
import ic2.core.block.machines.tiles.nv.StoneWoodGassifierTileEntity;
import ic2.core.block.misc.tiles.BarrelTileEntity;
import ic2.core.block.storage.tiles.RedirectorMasterTileEntity;
import ic2.core.block.storage.tiles.RedirectorSlaveTileEntity;
import ic2.core.block.storage.tiles.tank.PushingValveTileEntity;
import ic2.core.block.storage.tiles.transformer.AdjustableTransformerTileEntity;
import ic2.core.block.transport.fluid.tiles.SimplePipePumpTileEntity;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.probeplugin.ProbePluginListener;
import ic2.probeplugin.info.BarrelComponent;
import ic2.probeplugin.info.CropComponent;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.info.LinkingComponent;
import ic2.probeplugin.info.MultiBlockComponent;
import ic2.probeplugin.info.WrenchComponent;
import ic2.probeplugin.info.electric.AdjustableTransformerComponent;
import ic2.probeplugin.info.electric.BaseEnergyStorageComponent;
import ic2.probeplugin.info.electric.ChargepadsComponent;
import ic2.probeplugin.info.electric.ChargingBenchComponent;
import ic2.probeplugin.info.electric.ConstructionLightComponent;
import ic2.probeplugin.info.electric.ElectricLoaderComponent;
import ic2.probeplugin.info.electric.ElectricUnloaderComponent;
import ic2.probeplugin.info.electric.LuminatorComponent;
import ic2.probeplugin.info.electric.RedirectorMasterComponent;
import ic2.probeplugin.info.electric.RedirectorSlaveComponent;
import ic2.probeplugin.info.electric.TransformerComponent;
import ic2.probeplugin.info.generators.FuelBoilerComponent;
import ic2.probeplugin.info.generators.GeneratorComponent;
import ic2.probeplugin.info.generators.NuclearReactorComponent;
import ic2.probeplugin.info.generators.OceanGeneratorComponent;
import ic2.probeplugin.info.generators.ReactorChamberComponent;
import ic2.probeplugin.info.generators.SolarPanelComponent;
import ic2.probeplugin.info.generators.SteamTurbineComponent;
import ic2.probeplugin.info.generators.WaveGenComponent;
import ic2.probeplugin.info.generators.WindmillGenComponent;
import ic2.probeplugin.info.machines.BaseMachineComponent;
import ic2.probeplugin.info.machines.BaseMultiBlockMachineComponent;
import ic2.probeplugin.info.machines.BaseTeleporterComponent;
import ic2.probeplugin.info.machines.BatteryStationComponent;
import ic2.probeplugin.info.machines.ChargedElectrolyzerComponent;
import ic2.probeplugin.info.machines.CropLibraryComponent;
import ic2.probeplugin.info.machines.CropMatronComponent;
import ic2.probeplugin.info.machines.ElectricBlockComponent;
import ic2.probeplugin.info.machines.ElectricEnchanterComponent;
import ic2.probeplugin.info.machines.ElectricWoodGassifierComponent;
import ic2.probeplugin.info.machines.ElectrolyzerComponent;
import ic2.probeplugin.info.machines.MassFabricatorComponent;
import ic2.probeplugin.info.machines.MinerComponent;
import ic2.probeplugin.info.machines.OreScannerComponent;
import ic2.probeplugin.info.machines.PlasmafierComponent;
import ic2.probeplugin.info.machines.PumpComponent;
import ic2.probeplugin.info.machines.RangedPumpComponent;
import ic2.probeplugin.info.machines.StoneCannerComponent;
import ic2.probeplugin.info.machines.StoneMachineComponent;
import ic2.probeplugin.info.machines.TeleporterComponent;
import ic2.probeplugin.info.machines.UraniumEnricherComponent;
import ic2.probeplugin.info.machines.VillagerOMatComponent;
import ic2.probeplugin.info.machines.WoodGassifierComponent;
import ic2.probeplugin.info.transport.CableProvider;
import ic2.probeplugin.info.transport.PipePumpProvider;
import ic2.probeplugin.info.transport.PushingValveProvider;
import ic2.probeplugin.info.transport.TubeProvider;
import ic2.probeplugin.override.components.GridElement;
import ic2.probeplugin.override.components.Panel;
import java.util.Map;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.items.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ProbePlugin {
   static Map<Class<?>, ITileInfoComponent<?>> TILE_COMPS = CollectionUtils.<Class<?>, ITileInfoComponent<?>>createMap();
   static Map<Block, IProbeModifier> BLOCK_MODS = CollectionUtils.<Block, IProbeModifier>createMap();

   public static void loadPlugin() {
      TheOneProbe.theOneProbeImp.registerProvider(new ProbeTileListener());
      TheOneProbe.theOneProbeImp.registerBlockDisplayOverride(new ProbeBlockListener());
      TheOneProbe.theOneProbeImp.registerElementFactory(new BaseFactory(Panel.ELEMENT_ID, Panel::new));
      TheOneProbe.theOneProbeImp.registerElementFactory(new BaseFactory(GridElement.ELEMENT, GridElement::new));
      loadTileData();
   }

   public static void loadRecipe(IAdvancedCraftingManager manager) {
      manager.addShapelessIC2Recipe("probe", new ItemStack(ProbePluginListener.PROBE), IC2Items.CIRCUIT, ModItems.PROBE);
   }

   static void loadTileData() {
      register(IWrenchableTile.class, new WrenchComponent());
      register(ICropTile.class, CropComponent.INSTANCE);
      register(IC2Blocks.CROP_STICK, CropComponent.INSTANCE);
      register(BarrelTileEntity.class, new BarrelComponent());
      register(BaseMachineTileEntity.class, new BaseMachineComponent());
      register(
         new ElectricBlockComponent(),
         CrafterTileEntity.class,
         TerraformerTileEntity.class,
         MonitorTileEntity.class,
         ReactorPlannerTileEntity.class,
         BaseFluxGeneratorTileEntity.class,
         ChunkloaderTileEntity.class,
         CropHarvesterTileEntity.class,
         MachineBufferTileEntity.class,
         MachineTankTileEntity.class,
         MagnetizerTileEntity.class,
         TeslaCoilTileEntity.class,
         TeleporterHubTileEntity.class
      );
      register(CropMatronTileEntity.class, new CropMatronComponent());
      register(ElectricEnchanterTileEntity.class, new ElectricEnchanterComponent());
      register(MassFabricatorTileEntity.class, new MassFabricatorComponent());
      register(MinerTileEntity.class, new MinerComponent());
      register(OreScannerTileEntity.class, new OreScannerComponent());
      register(PlasmafierTileEntity.class, new PlasmafierComponent());
      register(PumpTileEntity.class, new PumpComponent());
      register(RangedPumpTileEntity.class, new RangedPumpComponent());
      register(UraniumEnricherTileEntity.class, new UraniumEnricherComponent());
      register(WoodGassifierTileEntity.class, new ElectricWoodGassifierComponent());
      register(StoneWoodGassifierTileEntity.class, new WoodGassifierComponent());
      register(StoneBasicMachineTileEntity.class, new StoneMachineComponent());
      register(StoneCannerTileEntity.class, new StoneCannerComponent());
      register(ElectrolyzerTileEntity.class, new ElectrolyzerComponent());
      register(ChargedElectrolyzerTileEntity.class, new ChargedElectrolyzerComponent());
      register(BaseCropLibraryTileEntity.class, new CropLibraryComponent());
      register(BaseTeleporterTileEntity.class, new BaseTeleporterComponent());
      register(BaseMultiMachineTileEntity.class, new BaseMultiBlockMachineComponent());
      register(BaseGeneratorTileEntity.class, new GeneratorComponent());
      register(SolarPanelTileEntity.class, new SolarPanelComponent());
      register(WaveGenTileEntity.class, new WaveGenComponent());
      register(IReactor.class, NuclearReactorComponent.INSTANCE);
      register(IReactorChamber.class, new ReactorChamberComponent());
      register(OceanGeneratorTileEntity.class, new OceanGeneratorComponent());
      register(FuelBoilerTileEntity.class, new FuelBoilerComponent());
      register(WindmillTileEntity.class, new WindmillGenComponent());
      register(SteamTurbineTileEntity.class, new SteamTurbineComponent());
      register(BaseEnergyStorageTileEntity.class, new BaseEnergyStorageComponent());
      register(BaseBatteryStationTileEntity.class, new BatteryStationComponent());
      register(BaseTransformerTileEntity.class, new TransformerComponent());
      register(AdjustableTransformerTileEntity.class, new AdjustableTransformerComponent());
      register(LuminatorTileEntity.class, new LuminatorComponent());
      register(ConstructionLightTileEntity.class, new ConstructionLightComponent());
      register(BaseChargePadTileEntity.class, new ChargepadsComponent());
      register(BaseChargingBenchTileEntity.class, new ChargingBenchComponent());
      register(TeleporterTileEntity.class, new TeleporterComponent());
      register(BaseElectricLoaderTileEntity.class, new ElectricLoaderComponent());
      register(BaseElectricUnloaderTileEntity.class, new ElectricUnloaderComponent());
      register(BaseMultiBlockTileEntity.class, new MultiBlockComponent());
      register(BaseLinkingTileEntity.class, new LinkingComponent());
      register(TubeTileEntity.class, new TubeProvider());
      register(CableTileEntity.class, new CableProvider());
      register(RedirectorMasterTileEntity.class, new RedirectorMasterComponent());
      register(RedirectorSlaveTileEntity.class, new RedirectorSlaveComponent());
      register(SimplePipePumpTileEntity.class, new PipePumpProvider());
      register(PushingValveTileEntity.class, new PushingValveProvider());
      register(VillagerOMatTileEntity.class, new VillagerOMatComponent());
   }

   public static void register(Block block, IProbeModifier mod) {
      BLOCK_MODS.put(block, mod);
   }

   public static void register(Class<?> tile, ITileInfoComponent<?> info) {
      TILE_COMPS.put(tile, info);
   }

   public static void register(ITileInfoComponent<?> info, Class<?>... tiles) {
      for (Class<?> tile : tiles) {
         TILE_COMPS.put(tile, info);
      }
   }

   public static IProbeModifier getMod(Block block) {
      return BLOCK_MODS.get(block);
   }

   public static ITileInfoComponent<?> getComponents(Class<?> tile) {
      return TILE_COMPS.get(tile);
   }
}
