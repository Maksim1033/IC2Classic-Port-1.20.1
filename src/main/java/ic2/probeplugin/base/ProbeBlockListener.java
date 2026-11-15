package ic2.probeplugin.base;

import ic2.core.block.misc.TreeTapAndBucketBlock;
import ic2.core.platform.events.StructureManager;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Blocks;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import java.util.List;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.apiimpl.ProbeHitData;
import mcjty.theoneprobe.apiimpl.elements.ElementIcon;
import mcjty.theoneprobe.apiimpl.providers.DefaultProbeInfoProvider;
import mcjty.theoneprobe.apiimpl.styles.IconStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public class ProbeBlockListener implements IBlockDisplayOverride {
   public boolean overrideStandardInfo(ProbeMode probe, IProbeInfo info, Player player, Level Level, BlockState state, IProbeHitData hitData) {
      boolean override = false;
      IProbeModifier mod = ProbePlugin.getMod(state.getBlock());
      if (mod != null) {
         if (mod.hasOverrideStandardInfo()) {
            mod.overrideStandardInfo(Config.getRealConfig(), probe, info, state, state.getBlock(), Level, hitData.getPos(), player, hitData);
            override = true;
         }

         mod.modifyData(probe, info, player, Level, state, hitData);
      }

      if (Level.getBlockEntity(hitData.getPos()) == null && StructureManager.INSTANCE.getListener(Level, hitData.getPos()) instanceof BlockEntity tile) {
         BlockState newState = tile.getBlockState();
         DefaultProbeInfoProvider.showStandardBlockInfo(
            Config.getRealConfig(),
            probe,
            info,
            newState,
            newState.getBlock(),
            Level,
            hitData.getPos(),
            player,
            new ProbeHitData(hitData.getPos(), hitData.getHitVec(), hitData.getSideHit(), new ItemStack(newState.getBlock()))
         );
         List<ITileInfoComponent<?>> list = ProbeTileListener.getData((Class<? extends BlockEntity>)tile.getClass());
         if (!list.isEmpty()) {
            PlayerHandler handler = PlayerHandler.getHandler(player);
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               ITileInfoComponent<?> comp = list.get(i);
               if (comp.isValid(probe, handler)) {
                  comp.addTileInfo(info, player, hitData.getSideHit(), tile);
               }
            }
         }

         return true;
      } else {
         if (state.getBlock() == IC2Blocks.TREETAP_AND_BUCKET) {
            int current = (Integer)state.getValue(TreeTapAndBucketBlock.FILL_STAGE);
            info.element((IElement)new Panel(Panel.Type.VERTICAL).progress(current, 5, IC2Styles.SCRAP_BAR.copy().prefix("Progress: " + current + " / 5")));
         }

         return override;
      }
   }

   public static void showStandardBlockInfo(
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
      String modId = ((ModContainer)ModList.get().getModContainerById(ForgeRegistryWrapper.getBlockKey(block).getNamespace()).orElseThrow())
         .getModInfo()
         .getDisplayName();
      ItemStack pickBlock = data.getPickBlock();
      if (block instanceof InfestedBlock && mode != ProbeMode.DEBUG && !Tools.show(mode, config.getShowSilverfish())) {
         block = ((InfestedBlock)block).getHostBlock();
         pickBlock = new ItemStack(block, 1);
      }

      FluidState state = blockState.getFluidState();
      if (state.getType() != Fluids.EMPTY && block instanceof LiquidBlock) {
         Fluid fluid = state.getType();
         FluidStack fluidStack = new FluidStack(fluid, 1000);
         IProbeInfo horizontal = probeInfo.horizontal();
         horizontal.element(
            new ElementIcon(
               IClientFluidTypeExtensions.of(fluid).getStillTexture(),
               -1,
               -1,
               16,
               16,
               new IconStyle().width(20).color(IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack))
            )
         );
         horizontal.vertical()
            .text(Component.literal(TextStyleClass.NAME + "{*" + fluidStack.getTranslationKey() + "*}"))
            .text(Component.literal(TextStyleClass.MODNAME + modId));
      } else {
         if (!pickBlock.isEmpty()) {
            if (Tools.show(mode, config.getShowModName())) {
               probeInfo.horizontal().item(pickBlock).vertical().itemLabel(pickBlock).text(Component.literal(TextStyleClass.MODNAME + modId));
            } else {
               probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER)).item(pickBlock).itemLabel(pickBlock);
            }
         } else if (Tools.show(mode, config.getShowModName())) {
            probeInfo.vertical()
               .text(Component.literal(TextStyleClass.NAME + "{*" + block.getDescriptionId() + ".name*}"))
               .text(Component.literal(TextStyleClass.MODNAME + modId));
         } else {
            probeInfo.vertical().text(Component.literal(TextStyleClass.NAME + "{*" + block.getDescriptionId() + ".name*}"));
         }
      }
   }
}
