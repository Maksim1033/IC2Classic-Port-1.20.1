package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.ChunkRequestPacket;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.math.ColorUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class ChunkLoaderOverlay implements IWorldOverlay {
   public static final ChunkLoaderOverlay INSTANCE = new ChunkLoaderOverlay();
   boolean lastCheck = false;
   Long2IntMap map = Long2IntMaps.EMPTY_MAP;

   @Override
   public void cleanup() {
      this.map.clear();
      this.lastCheck = false;
   }

   @Override
   public void update(Level world, Player player) {
      Minecraft mc = Minecraft.getInstance();
      DebugRenderer render = mc.debugRenderer;
      render.switchRenderChunkborder();
      if (!render.switchRenderChunkborder()) {
         this.lastCheck = false;
         this.map.clear();
      } else {
         if (!this.lastCheck || mc.level.getGameTime() % 100L == 0L) {
            this.lastCheck = true;
            PacketManager.INSTANCE.sendToServer(new ChunkRequestPacket());
         }
      }
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      if (!this.map.isEmpty()) {
         BlockPos playerPos = player.blockPosition();
         LongList higher = new LongArrayList();
         IntList number = new IntArrayList();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderUtils.draw(
               event.getPoseStack(),
               (B, M) -> {
                  B.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                  ObjectIterator var7x = Long2IntMaps.fastIterable(this.map).iterator();

                  while (var7x.hasNext()) {
                     Entry entry = (Entry)var7x.next();
                     long chunk = entry.getLongKey();
                     int x = ChunkPos.getX(chunk) * 16;
                     int yx = (playerPos.getY() >> 4 << 4) - 16;
                     int z = ChunkPos.getZ(chunk) * 16;
                     if (helper.isVisible(new AABB((double)(x + 7), (double)yx, (double)(z + 7), (double)x + 9.1, (double)(yx + 48), (double)z + 9.1))) {
                        for (int jx = 0; jx < 4; jx++) {
                           RenderShapes.renderColorCube(
                              new AABB((double)(x + 7), (double)yx, (double)(z + 9), (double)x + 9.1, (double)yx + 0.1, (double)z + 9.1), ColorUtils.BLUE, B, M
                           );
                           RenderShapes.renderColorCube(
                              new AABB((double)(x + 7), (double)yx, (double)(z + 7), (double)x + 7.1, (double)yx + 0.1, (double)z + 9.1), ColorUtils.BLUE, B, M
                           );
                           RenderShapes.renderColorCube(
                              new AABB((double)(x + 9), (double)yx, (double)(z + 7), (double)x + 9.1, (double)yx + 0.1, (double)z + 9.1), ColorUtils.BLUE, B, M
                           );
                           RenderShapes.renderColorCube(
                              new AABB((double)(x + 7), (double)yx, (double)(z + 7), (double)x + 9.1, (double)yx + 0.1, (double)z + 7.1), ColorUtils.BLUE, B, M
                           );
                           yx += 16;
                        }

                        if (entry.getIntValue() > 0) {
                           higher.add(chunk);
                           number.add(entry.getIntValue());
                        }
                     }
                  }
               }
            )
            .end();
         if (!higher.isEmpty()) {
            PoseStack stack = RenderUtils.createPlayer(event.getPoseStack());
            Font font = Minecraft.getInstance().font;
            BufferSource impl = Minecraft.getInstance().renderBuffers().bufferSource();
            int i = 0;

            for (int m = higher.size(); i < m; i++) {
               ChunkPos pos = new ChunkPos(higher.getLong(i));
               int y = (playerPos.getY() >> 4 << 4) - 16;

               for (int j = 0; j < 4; j++) {
                  stack.pushPose();
                  stack.translate((double)((float)pos.getMinBlockX() + 8.0F), (double)y + 1.0, (double)((float)pos.getMinBlockZ() + 8.0F));
                  stack.scale(-0.1F, -0.1F, -0.1F);
                  stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                  String s = Integer.toString(number.getInt(i));
                  font.drawInBatch(s, 0.0F - (float)font.width(s) * 0.5F, 0.0F, ColorUtils.RED, false, stack.last().pose(), impl, Font.DisplayMode.NORMAL, 0, 15728880);
                  stack.popPose();
                  y += 16;
               }
            }

            impl.endBatch();
            stack.popPose();
         }
      }
   }

   public void setForceLoadedChunks(Long2IntMap map) {
      this.map = map;
   }
}
