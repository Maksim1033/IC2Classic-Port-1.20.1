package ic2.probeplugin.info;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropTile;
import ic2.core.block.crops.CropRegistry;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.ColorUtils;
import ic2.probeplugin.base.IProbeModifier;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.override.styles.ILayoutStyleBuilder;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.apiimpl.elements.ElementItemStack;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public class CropComponent implements ITileInfoComponent<ICropTile>, IProbeModifier {
   public static final CropComponent INSTANCE = new CropComponent();
   public static final int COLOR_GROWTH = ColorUtils.rgb(0, 170, 0);
   public static final int COLOR_WATER = ColorUtils.rgb(93, 105, 255);
   public static final int COLOR_NUTRIENTS = ColorUtils.rgb(0, 255, 5);
   public static final int COLOR_LIGHT = ColorUtils.rgb(255, 255, 85);
   public static final int COLOR_ENV = ColorUtils.rgb(85, 255, 255);
   public static final int COLOR_WEEDEX = ColorUtils.rgb(255, 85, 255);
   public static final int COLOR_FERTILIZER = ColorUtils.rgb(86, 54, 36);
   public static final int COLOR_STAT_GROWTH = ColorUtils.rgb(0, 170, 170);
   public static final int COLOR_STAT_GAIN = ColorUtils.rgb(170, 0, 170);
   public static final int COLOR_STAT_RESIST = ColorUtils.rgb(255, 170, 0);
   public static final int COLOR_STAT_NEEDS = ColorUtils.darker(COLOR_ENV);

   @Override
   public boolean hasValidReader(PlayerHandler handler) {
      return handler.hasCropScanner();
   }

   public void addInfo(IProbeInfo info, Player player, Direction dir, ICropTile tile) {
      Panel crops = new Panel(ILayoutStyleBuilder.spacing(3), Panel.Type.VERTICAL);
      ILayoutStyle barContainerPaddableLayoutStyle = ILayoutStyleBuilder.spacing(1).alignment(ElementAlignment.ALIGN_CENTER);
      ICrop crop = tile.getCrop();
      if (crop != null) {
         int maxSize = crop.getGrowthSteps();
         int currentSize = tile.getGrowthStage();
         if (tile.getScanLevel() < 4 && currentSize < maxSize) {
            crops.element(createCropBar(tile.getScanLevel(), 4, "ic2.probe.crop.info.scan", COLOR_GROWTH));
         } else {
            Panel growth = new Panel(barContainerPaddableLayoutStyle, Panel.Type.VERTICAL);
            Panel growth_bars = new Panel(IC2Styles.BARS_STYLE, Panel.Type.VERTICAL);
            growth.text("ic2.probe.crop.growth");
            growth.element(growth_bars);
            crops.element(growth);
            if (tile.getGrowthStage() < crop.getGrowthSteps()) {
               growth_bars.element(createCropBar(currentSize, maxSize, "ic2.probe.crop.info.stage", COLOR_GROWTH));
               growth_bars.element(createCropBar(tile.getGrowthPoints(), crop.getGrowthDuration(tile), "ic2.probe.crop.info.points", COLOR_GROWTH));
               growth.padding(2, 1);
               if (crop.canGrow(tile) && crop.getCropType().isCompatible(tile.isWaterLogged())) {
                  growth.text("ic2.probe.crop.grow.rate", new Object[]{tile.calculateGrowthSpeed()});
               } else {
                  growth.text(this.translate("ic2.probe.crop.grow.not").withStyle(ColorUtils.toColor(ColorUtils.RED)));
               }
            } else {
               growth_bars.element(createCropBarDisplay(currentSize, maxSize, "ic2.probe.crop.info.stage_done", COLOR_GROWTH));
            }

            if (tile.getScanLevel() >= 4) {
               Panel stats = new Panel(barContainerPaddableLayoutStyle, Panel.Type.VERTICAL);
               Panel stats_bars = new Panel(IC2Styles.BARS_STYLE, Panel.Type.VERTICAL);
               stats.text("ic2.probe.crop.stats");
               stats_bars.element(createCropBar(tile.getGrowthStat(), 31, "ic2.probe.crop.info.growth", COLOR_STAT_GROWTH));
               stats_bars.element(createCropBar(tile.getGainStat(), 31, "ic2.probe.crop.info.gain", COLOR_STAT_GAIN));
               stats_bars.element(createCropBar(tile.getResistanceStat(), 31, "ic2.probe.crop.info.resistance", COLOR_STAT_RESIST));
               int need = (crop.getProperties().getTier() - 1) * 4 + tile.getGrowthStat() + tile.getGainStat() + tile.getResistanceStat();
               int have = crop.getStatInfluence(tile, tile.getHumidity(), tile.getNutrients(), tile.getEnvironmentQuality()) * 5;
               stats_bars.element(createCropBar(need, have, "ic2.probe.crop.info.needs", COLOR_STAT_NEEDS));
               stats.element(stats_bars);
               crops.element(stats);
            }
         }
      }

      Panel storage = new Panel(barContainerPaddableLayoutStyle, Panel.Type.VERTICAL);
      Panel storage_bars = new Panel(IC2Styles.BARS_STYLE, Panel.Type.VERTICAL);
      storage.text("ic2.probe.crop.storage");
      storage_bars.element(createCropBar(tile.getFertilizerStorage(), 300, "ic2.probe.crop.info.fertilizer", COLOR_FERTILIZER));
      storage_bars.element(createCropBar(tile.getWaterStorage(), 200, "ic2.probe.crop.info.water", COLOR_WATER));
      storage_bars.element(createCropBar(tile.getWeedExStorage(), 150, "ic2.probe.crop.info.weedex", COLOR_WEEDEX));
      storage.element(storage_bars);
      crops.element(storage);
      Panel environment = new Panel(barContainerPaddableLayoutStyle, Panel.Type.VERTICAL);
      Panel environment_bars = new Panel(IC2Styles.BARS_STYLE, Panel.Type.VERTICAL);
      environment.text("ic2.probe.crop.env");
      environment_bars.element(createCropBar(tile.getNutrients(), 20, "ic2.probe.crop.info.nutrients", COLOR_NUTRIENTS));
      environment_bars.element(createCropBar(tile.getHumidity(), 20, "ic2.probe.crop.info.humidity", COLOR_WATER));
      environment_bars.element(createCropBar(tile.getEnvironmentQuality(), 10, "ic2.probe.crop.info.env", COLOR_ENV));
      environment_bars.element(createCropBar(tile.getLightLevel(), 15, "ic2.probe.crop.info.light", COLOR_LIGHT));
      environment.element(environment_bars);
      crops.element(environment);
      this.addSecurely(info, 1, crops);
   }

   @Override
   public boolean hasOverrideStandardInfo() {
      return true;
   }

   @Override
   public void overrideStandardInfo(
      IProbeConfig config,
      ProbeMode mode,
      IProbeInfo probeInfo,
      BlockState blockState,
      Block block,
      Level Level,
      BlockPos pos,
      Player player,
      IProbeHitData data
   ) {
      Panel header = new Panel(IC2Styles.HEADER_STYLE, Panel.Type.HORIZONTAL);
      ICropTile cropTile = (ICropTile)Level.getBlockEntity(data.getPos());
      ItemStack icon = new ItemStack(IC2Items.CROP_STICKS);
      ICrop crop = cropTile.getCrop();
      Component name = data.getPickBlock().getHoverName();
      Component discovered = null;
      if (crop != null) {
         int maxSize = crop.getGrowthSteps();
         int currentSize = cropTile.getGrowthStage();
         if (cropTile.getScanLevel() < 1 && currentSize < maxSize && crop != ICropRegistry.WEED && crop != CropRegistry.SEA_WEED) {
            icon = new ItemStack(IC2Items.CROP_SEED);
            name = this.translate("info.crop.ic2.data.unknown");
         } else {
            name = crop.getName();
            icon = !crop.getDisplayItem().isEmpty() ? crop.getDisplayItem() : new ItemStack(IC2Items.CROP_SEED);
            discovered = crop.discoveredBy();
         }
      }

      Panel right = new Panel(ILayoutStyleBuilder.create().spacing(1), Panel.Type.VERTICAL);
      header.element(new ElementItemStack(icon, new ItemStyle().height(20).width(20)));
      right.text(CompoundText.create().name(name));
      if (discovered != null) {
         right.text(CompoundText.create().text("\u00a7oby ").text(discovered).text("\u00a7r").style(TextStyleClass.INFO));
      }

      right.text(
         CompoundText.create()
            .style(TextStyleClass.MODNAME)
            .text(
               ((ModContainer)ModList.get().getModContainerById(ForgeRegistryWrapper.getBlockKey(block).getNamespace()).orElseThrow())
                  .getModInfo()
                  .getDisplayName()
            )
      );
      header.element(right);
      probeInfo.getElements().add(0, header);
   }

   public static ElementProgress createCropBar(int current, int max, String type, int color) {
      return new ElementProgress((long)current, (long)max, IC2Styles.crops(type, color, current, max));
   }

   public static ElementProgress createCropBarDisplay(int current, int max, String display, int color) {
      return new ElementProgress((long)current, (long)max, IC2Styles.crops(display, color));
   }
}
