package ic2.probeplugin.base;

import ic2.core.IC2;
import ic2.core.block.base.features.IProfileListener;
import ic2.core.block.base.features.ITickListener;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.profiler.IProfilerTile;
import ic2.core.utils.helpers.profiler.ProfilerEntry;
import ic2.core.utils.helpers.profiler.ProfilerHelper;
import ic2.core.utils.math.MathUtils;
import ic2.probeplugin.info.ITileInfoComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProbeTileListener implements IProbeInfoProvider {
   static Map<Class<? extends BlockEntity>, List<ITileInfoComponent<?>>> cachedData = CollectionUtils.<Class<? extends BlockEntity>, List<ITileInfoComponent<?>>>createMap();

   public ResourceLocation getID() {
      return new ResourceLocation("ic2", "tile_provider");
   }

   public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level Level, BlockState state, IProbeHitData hitData) {
      BlockEntity tile = Level.getBlockEntity(hitData.getPos());
      if (tile != null) {
         List<ITileInfoComponent<?>> list = getData((Class<? extends BlockEntity>)tile.getClass());
         if (!list.isEmpty()) {
            PlayerHandler handler = PlayerHandler.getHandler(player);
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               ITileInfoComponent<?> comp = list.get(i);
               if (comp.isValid(mode, handler)) {
                  comp.addTileInfo(info, player, hitData.getSideHit(), tile);
               }
            }
         }
      }

      if (IC2.KEYBOARD.isHudModeKeyDown(player) && IC2.CONFIG.tileProfiler.get()) {
         if (tile instanceof IProfilerTile) {
            ProfilerHelper.ProfilerInstance instance = ProfilerHelper.INSTANCE.getOrCreate(tile);
            if (instance.getMain() == null) {
               return;
            }

            IProbeInfo profile = info.vertical();
            List<ProfilerEntry> entry = CollectionUtils.<ProfilerEntry>createList();
            find(instance.getMain(), entry);
            int i = 0;

            for (int mx = entry.size(); i < mx; i++) {
               entry.get(i).print(profile);
            }
         }

         if (tile instanceof IProfileListener listener) {
            long qs = listener.getLag() / 1000L;
            MutableComponent s = listener.showResults()
               .withStyle(qs <= 0L ? ChatFormatting.AQUA : (qs <= 4L ? ChatFormatting.GREEN : (qs <= 20L ? ChatFormatting.YELLOW : ChatFormatting.RED)));
            IProbeInfo subInfo = info.vertical();
            subInfo.mcText(s)
               .progress(
                  qs,
                  50L,
                  info.defaultProgressStyle()
                     .suffix("\u00b5s")
                     .filledColor((qs <= 0L ? Color.BLUE : (qs <= 4L ? Color.GREEN : (qs <= 20L ? Color.YELLOW : Color.RED))).getRGB())
               );
            ITickListener.GlobalLag lag = ITickListener.GLOBAL_LAG.get(tile.getClass());
            if (lag != null) {
               qs = lag.getGlobalAverage();
               long colorQS = qs / 1000L;
               ChatFormatting format = colorQS <= 0L
                  ? ChatFormatting.AQUA
                  : (colorQS <= 4L ? ChatFormatting.GREEN : (colorQS <= 20L ? ChatFormatting.YELLOW : ChatFormatting.RED));
               subInfo.mcText(Component.literal(MathUtils.toLagStringProbe("PerTile Lag Avg", qs)).withStyle(format));
               qs = lag.getGlobalTotal();
               subInfo.mcText(Component.literal(MathUtils.toLagStringProbe("Total Lag Avg", qs)).withStyle(format));
            }

            lag = ITickListener.SERVER_LAG;
            subInfo.mcText(Component.literal(MathUtils.toLagStringProbe("Mod Per Tile Lag", lag.getGlobalAverage())).withStyle(ChatFormatting.AQUA));
            subInfo.mcText(Component.literal(MathUtils.toLagStringProbe("Mod Total Lag", lag.getGlobalTotal())).withStyle(ChatFormatting.AQUA));
         }
      }
   }

   static void find(ProfilerEntry mainEntry, List<ProfilerEntry> entry) {
      if (mainEntry != null) {
         entry.add(mainEntry);
         int i = 0;

         for (int m = mainEntry.getChildCount(); i < m; i++) {
            find(mainEntry.getChild(i), entry);
         }
      }
   }

   public static List<ITileInfoComponent<?>> getData(Class<? extends BlockEntity> tile) {
      List<ITileInfoComponent<?>> list = cachedData.get(tile);
      if (list == null) {
         list = CollectionUtils.<ITileInfoComponent<?>>createList();
         cachedData.put(tile, list);
         Set<Class<?>> classes = CollectionUtils.<Class<?>>createLinkedSet();
         loopClass(tile, classes);

         for (Class<?> clz : classes) {
            ITileInfoComponent<?> info = ProbePlugin.getComponents(clz);
            if (info != null) {
               list.add(info);
               info.disableClasses(classes);
            }
         }
      }

      return list;
   }

   public static void loopClass(Class<?> clz, Set<Class<?>> set) {
      List<Class<?>> classes = new ObjectArrayList();

      try {
         while (clz != BlockEntity.class) {
            classes.add(clz);
            clz = clz.getSuperclass();
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      for (int i = 0; i < classes.size(); i++) {
         clz = classes.get(i);
         set.add(clz);

         for (Class<?> sub : clz.getInterfaces()) {
            loopInterface(sub, set);
         }
      }
   }

   public static void loopInterface(Class<?> clz, Set<Class<?>> set) {
      ObjectArrayList<Class<?>> stack = new ObjectArrayList();

      try {
         stack.push(clz);

         while (!stack.isEmpty()) {
            Class<?> entry = (Class<?>)stack.pop();
            if (set.add(entry)) {
               Class<?>[] sub = entry.getInterfaces();
               if (sub != null) {
                  stack.addAll(ObjectArrayList.wrap(sub));
               }
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }
}
