package ic2.core.block.rendering.world;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.block.rendering.world.impl.ChunkLoaderOverlay;
import ic2.core.block.rendering.world.impl.EuReaderOverlay;
import ic2.core.block.rendering.world.impl.MultiBlockOverlay;
import ic2.core.block.rendering.world.impl.PipeOverlay;
import ic2.core.block.rendering.world.impl.SonarOverlay;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class WorldOverlays {
   public static final WorldOverlays INSTANCE = new WorldOverlays();
   List<IWorldOverlay> overlays = CollectionUtils.<IWorldOverlay>createList();
   Level lastWorld;

   public synchronized void addOverlay(IWorldOverlay renderer) {
      this.overlays.add(renderer);
   }

   public void init() {
      this.overlays.add(BlockHighlighter.INSTANCE);
      this.overlays.add(ChunkLoaderOverlay.INSTANCE);
      this.overlays.add(new PipeOverlay());
      this.overlays.add(new EuReaderOverlay());
      this.overlays.add(SonarOverlay.INSTANCE);
      this.overlays.add(MultiBlockOverlay.INSTANCE);
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.phase != Phase.START && !this.overlays.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.player != null) {
            Level world = mc.level;
            if (world != this.lastWorld) {
               this.cleanup();
               this.lastWorld = world;
            }

            int i = 0;

            for (int m = this.overlays.size(); i < m; i++) {
               this.overlays.get(i).update(world, mc.player);
            }
         } else {
            if (this.lastWorld != null) {
               this.cleanup();
            }
         }
      }
   }

   @SubscribeEvent
   public void onRender(RenderLevelStageEvent event) {
      if (!this.overlays.isEmpty() && event.getStage() == Stage.AFTER_WEATHER) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.player != null) {
            Frustum helper = RenderUtils.createClipper(event);
            PoseStack stack = event.getPoseStack();
            stack.pushPose();
            stack.setIdentity();
            int i = 0;

            for (int m = this.overlays.size(); i < m; i++) {
               this.overlays.get(i).render(mc.level, mc.player, event, helper);
            }

            stack.popPose();
         }
      }
   }

   public void cleanup() {
      int i = 0;

      for (int m = this.overlays.size(); i < m; i++) {
         this.overlays.get(i).cleanup();
      }

      this.lastWorld = null;
   }
}
