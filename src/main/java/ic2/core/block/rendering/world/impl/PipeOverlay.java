package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.platform.rendering.misc.GLUtils;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.platform.rendering.misc.VertexCache;
import ic2.core.utils.helpers.WorldRegion;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class PipeOverlay implements IWorldOverlay {
   static final int MINING_PIPE = ColorUtils.GREEN;
   private static final Executor RENDER_POOL = (Executor)Util.make(
      new ThreadPoolExecutor(
         Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
      ),
      T -> T.allowCoreThreadTimeOut(true)
   );
   ExecutorCompletionService<PipeOverlay.CacheEntry> service = new ExecutorCompletionService<>(RENDER_POOL);
   Long2ObjectLinkedOpenHashMap<PipeOverlay.CacheEntry> cacheMap = new Long2ObjectLinkedOpenHashMap();
   VertexCache.BufferedVertexCache cache = new VertexCache.BufferedVertexCache(10000, Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
   boolean wasRendering = false;
   long lastRefresh = 0L;
   int counter = 0;
   int vertecies = 0;

   @Override
   public void cleanup() {
      this.vertecies = -1;
      this.lastRefresh = 0L;
      this.cacheMap.clear();
      this.cache.clear();
      this.cache.delete();
   }

   @Override
   public void update(Level world, Player player) {
      if (this.lastRefresh <= world.getGameTime()) {
         this.lastRefresh = world.getGameTime() + 20L;
         this.counter++;
         if (this.counter > 30) {
            this.counter = 0;
            Iterator<PipeOverlay.CacheEntry> iter = this.cacheMap.values().iterator();

            while (iter.hasNext()) {
               PipeOverlay.CacheEntry entry = iter.next();
               if (Math.sqrt(entry.pos().distSqr(player.blockPosition())) > 200.0) {
                  iter.remove();
               }
            }
         }

         Box box = Box.withRange(player.blockPosition(), 50);
         WorldRegion region = new WorldRegion(world, box, true);
         LongStream.of(box.asLongArray()).parallel().forEach(T -> {
            BlockPos pos = BlockPos.of(T);
            BlockState state = region.getBlockState(pos);
            if (state.getBlock() instanceof ITubeBlock block) {
               PipeOverlay.CacheEntry entryx = (PipeOverlay.CacheEntry)this.cacheMap.get(pos.asLong());
               VoxelShape[] shapes = block.getRealShapes(state, region, pos);
               if (entryx != null && Arrays.deepEquals(entryx.shapeIn(), shapes)) {
                  return;
               }

               int color = block.getHighlightColor(state);
               DyeColor normalColor = block.getBlockColor(state);
               this.service.submit(new PipeOverlay.RenderEntry(state, shapes, normalColor, pos, color));
            } else if (this.cacheMap.containsKey(T)) {
               this.service.submit(new PipeOverlay.RenderEntry(null, null, null, pos, -1));
            }
         });
         boolean changed = false;

         Future<PipeOverlay.CacheEntry> temp;
         for (temp = null; (temp = this.service.poll()) != null; changed = true) {
            try {
               PipeOverlay.CacheEntry entry = temp.get();
               if (entry.cache() == null) {
                  PipeOverlay.CacheEntry existing = (PipeOverlay.CacheEntry)this.cacheMap.remove(entry.pos().asLong());
                  if (existing != null && existing.cache() != null) {
                     this.vertecies = this.vertecies - existing.cache().vertexCount();
                  }
               } else {
                  this.cacheMap.put(entry.pos().asLong(), entry);
                  this.vertecies = this.vertecies + entry.cache().vertexCount();
               }
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }

         if (changed || this.vertecies != this.cache.vertexCount()) {
            this.cache.clear();
            ObjectIterator var13 = this.cacheMap.values().iterator();

            while (var13.hasNext()) {
               PipeOverlay.CacheEntry entry = (PipeOverlay.CacheEntry)var13.next();
               this.cache.copyFrom(entry.cache());
            }

            this.cache.finish();
            this.vertecies = this.cache.vertexCount();
         }
      }
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      if (!this.cacheMap.isEmpty()
         && (player.getMainHandItem().getItem() == IC2Items.TUBE_LENSE.asItem() || player.getOffhandItem().getItem() == IC2Items.TUBE_LENSE.asItem())) {
         GLUtils.enableHighlight(true);
         RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
         RenderUtils.draw(event.getPoseStack(), (B, M) -> {
            Matrix4f model = new Matrix4f(M.last().pose());
            PoseStack pose = RenderSystem.getModelViewStack();
            pose.pushPose();
            pose.mulPoseMatrix(model);
            RenderSystem.applyModelViewMatrix();
            IC2RenderTypes.LINES.setupRenderState();
            this.cache.draw();
            IC2RenderTypes.LINES.clearRenderState();
            pose.popPose();
            RenderSystem.applyModelViewMatrix();
         });
         GLUtils.disableHighlight(true);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawShape(
      VertexConsumer bufferIn, VoxelShape[] shapeIn, DyeColor color, int xIn, int yIn, int zIn, float red, float green, float blue, float alpha
   ) {
      shapeIn[0]
         .forAllEdges(
            (minX, minY, minZ, maxX, maxY, maxZ) -> {
               Vector3f normal = new Vector3f((float)(maxX - minX), (float)(maxY - minY), (float)(maxZ - minZ));
               normal.normalize();
               bufferIn.vertex((double)((float)(minX + (double)xIn)), (double)((float)(minY + (double)yIn)), (double)((float)(minZ + (double)zIn)))
                  .color(red, green, blue, alpha)
                  .normal(normal.x(), normal.y(), normal.z())
                  .endVertex();
               bufferIn.vertex((double)((float)(maxX + (double)xIn)), (double)((float)(maxY + (double)yIn)), (double)((float)(maxZ + (double)zIn)))
                  .color(red, green, blue, alpha)
                  .normal(normal.x(), normal.y(), normal.z())
                  .endVertex();
            }
         );
      shapeIn[1]
         .forAllEdges(
            (minX, minY, minZ, maxX, maxY, maxZ) -> {
               Vector3f normal = new Vector3f((float)(maxX - minX), (float)(maxY - minY), (float)(maxZ - minZ));
               normal.normalize();
               bufferIn.vertex((double)((float)(minX + (double)xIn)), (double)((float)(minY + (double)yIn)), (double)((float)(minZ + (double)zIn)))
                  .color(
                     ColorUtils.getFloatR(MINING_PIPE), ColorUtils.getFloatG(MINING_PIPE), ColorUtils.getFloatB(MINING_PIPE), ColorUtils.getFloatA(MINING_PIPE)
                  )
                  .normal(normal.x(), normal.y(), normal.z())
                  .endVertex();
               bufferIn.vertex((double)((float)(maxX + (double)xIn)), (double)((float)(maxY + (double)yIn)), (double)((float)(maxZ + (double)zIn)))
                  .color(
                     ColorUtils.getFloatR(MINING_PIPE), ColorUtils.getFloatG(MINING_PIPE), ColorUtils.getFloatB(MINING_PIPE), ColorUtils.getFloatA(MINING_PIPE)
                  )
                  .normal(normal.x(), normal.y(), normal.z())
                  .endVertex();
            }
         );
      if (color != null) {
         int text = color.getTextColor() | 0xFF000000;
         Block.box(7.0, 7.0, 7.0, 9.0, 9.0, 9.0)
            .forAllEdges(
               (minX, minY, minZ, maxX, maxY, maxZ) -> {
                  Vector3f normal = new Vector3f((float)(maxX - minX), (float)(maxY - minY), (float)(maxZ - minZ));
                  normal.normalize();
                  bufferIn.vertex((double)((float)(minX + (double)xIn)), (double)((float)(minY + (double)yIn)), (double)((float)(minZ + (double)zIn)))
                     .color(ColorUtils.getFloatR(text), ColorUtils.getFloatG(text), ColorUtils.getFloatB(text), ColorUtils.getFloatA(text))
                     .normal(normal.x(), normal.y(), normal.z())
                     .endVertex();
                  bufferIn.vertex((double)((float)(maxX + (double)xIn)), (double)((float)(maxY + (double)yIn)), (double)((float)(maxZ + (double)zIn)))
                     .color(ColorUtils.getFloatR(text), ColorUtils.getFloatG(text), ColorUtils.getFloatB(text), ColorUtils.getFloatA(text))
                     .normal(normal.x(), normal.y(), normal.z())
                     .endVertex();
               }
            );
      }
   }

   public static record CacheEntry(BlockPos pos, VoxelShape[] shapeIn, VertexCache cache) {
   }

   public static record RenderEntry(BlockState state, VoxelShape[] shapeIn, DyeColor dye, BlockPos pos, int color) implements Callable<PipeOverlay.CacheEntry> {
      public PipeOverlay.CacheEntry call() throws Exception {
         if (this.state == null) {
            return new PipeOverlay.CacheEntry(this.pos, null, null);
         } else {
            VertexCache cache = new VertexCache(100, Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            PipeOverlay.drawShape(
               cache,
               this.shapeIn,
               this.dye,
               this.pos.getX(),
               this.pos.getY(),
               this.pos.getZ(),
               ColorUtils.getFloatR(this.color),
               ColorUtils.getFloatG(this.color),
               ColorUtils.getFloatB(this.color),
               ColorUtils.getFloatA(this.color)
            );
            return new PipeOverlay.CacheEntry(this.pos, this.shapeIn, cache);
         }
      }
   }
}
