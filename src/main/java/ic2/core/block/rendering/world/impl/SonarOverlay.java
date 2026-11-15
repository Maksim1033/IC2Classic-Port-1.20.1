package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.item.base.features.ISonarProvider;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.platform.rendering.misc.GLUtils;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class SonarOverlay implements IWorldOverlay {
   public static final SonarOverlay INSTANCE = new SonarOverlay();
   List<SonarOverlay.SonarInstance> providers = CollectionUtils.<SonarOverlay.SonarInstance>createList();

   public void addSonar(ISonarProvider prov, Supplier<ItemStack> stack) {
      this.providers.add(new SonarOverlay.SonarInstance(prov, stack));
   }

   @Override
   public void cleanup() {
      this.providers.clear();
   }

   @Override
   public void update(Level world, Player player) {
      if (!this.providers.isEmpty()) {
         this.providers.removeIf(T -> T.update(world));
      }
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      if (!this.providers.isEmpty()) {
         GLUtils.enableHighlight(true);
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderUtils.draw(event.getPoseStack(), (B, M) -> {
            B.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            long time = Minecraft.getInstance().level.getGameTime();
            int i = 0;

            for (int m = this.providers.size(); i < m; i++) {
               this.providers.get(i).render(B, M, helper, time);
            }
         }).end();
         GLUtils.disableHighlight(true);
      }
   }

   static class SonarEntry {
      long startTime;
      BlockPos offset;
      AABB box;

      public SonarEntry(BlockPos offset, long startTime) {
         this.offset = offset;
         this.startTime = startTime;
         this.box = new AABB(offset);
      }

      public boolean shouldBeRemoved(long currentTime) {
         return currentTime - this.startTime >= 25L;
      }

      public BlockPos getOffset() {
         return this.offset;
      }

      public int getAliveTime(long currentTime) {
         return (int)Math.max(0L, currentTime - this.startTime);
      }

      public AABB getBox() {
         return this.box;
      }
   }

   static class SonarInstance {
      private static final int MAX_PROGRESS = 15;
      private static final AABB[] SONAR_EFFECT = createArray(15);
      ISonarProvider provider;
      Supplier<ItemStack> item;
      List<SonarOverlay.SonarEntry> entries = new LinkedList<>();
      MutableBlockPos search = new MutableBlockPos();
      BlockPos startPos = null;
      int sonarColor;
      int lastRadius = 0;
      int targetRadius = 0;
      boolean alive = true;

      public SonarInstance(ISonarProvider provider, Supplier<ItemStack> item) {
         this.provider = provider;
         this.item = item;
      }

      public void render(BufferBuilder builder, PoseStack stack, Frustum helper, long time) {
         int rendered = 0;

         for (SonarOverlay.SonarEntry entry : this.entries) {
            if (helper.isVisible(entry.getBox())) {
               AABB box = SONAR_EFFECT[Math.min(entry.getAliveTime(time), 15)].move(entry.getOffset());
               int alpha = Mth.ceil(entry.getAliveTime(time) >= 15 ? 200.0F : (float)entry.getAliveTime(time) / 15.0F * 200.0F);
               RenderShapes.renderColorCube(box, this.sonarColor | alpha << 24, builder, stack);
               if (++rendered >= 2000) {
                  break;
               }
            }
         }
      }

      public boolean update(Level world) {
         long time = world.getGameTime();
         Iterator<SonarOverlay.SonarEntry> iter = this.entries.iterator();

         while (iter.hasNext() && iter.next().shouldBeRemoved(time)) {
            iter.remove();
         }

         if (!this.alive) {
            if (this.targetRadius > this.lastRadius) {
               this.lastRadius++;
               this.updateRadius(this.lastRadius, world);
            }

            return this.entries.isEmpty();
         } else {
            ItemStack stack = this.item.get();
            if (this.startPos == null) {
               this.startPos = this.provider.getStartPosition(stack);
               this.sonarColor = this.provider.getSonarColor(stack) & 16777215;
               this.updateRadius(0, world);
            } else {
               int newRadius = this.provider.getViewRadius(stack);
               if (newRadius > this.targetRadius) {
                  this.targetRadius = newRadius;
               }

               if (this.targetRadius > this.lastRadius) {
                  this.lastRadius++;
                  this.updateRadius(this.lastRadius, world);
               }
            }

            if (this.provider.isDone(stack)) {
               this.alive = false;
            }

            return false;
         }
      }

      private void updateRadius(int newRadius, Level world) {
         long time = world.getGameTime();

         for (BlockPos pos : Box.withRange(this.startPos, 0).expandSide(Axis.Y, newRadius).getHollowHorizontalIterator()) {
            this.entries.add(new SonarOverlay.SonarEntry(this.getHeight(world, pos), time));
         }
      }

      private BlockPos getHeight(Level world, BlockPos pos) {
         this.search.set(pos);
         Predicate<BlockState> state = Types.MOTION_BLOCKING_NO_LEAVES.isOpaque();
         if (!state.test(world.getBlockState(this.search))) {
            do {
               this.search.move(Direction.DOWN);
            } while (!state.test(world.getBlockState(this.search)) && this.search.getY() > 0);
         } else {
            do {
               this.search.move(Direction.UP);
            } while (state.test(world.getBlockState(this.search)) && this.search.getY() <= 255);

            this.search.move(Direction.DOWN);
         }

         return this.search.immutable();
      }

      private static AABB[] createArray(int progress) {
         AABB[] data = new AABB[progress + 1];

         for (int i = 0; i <= progress; i++) {
            data[i] = new AABB(BlockPos.ZERO).inflate(i == progress ? 0.0 : (double)((float)i / (float)progress * 0.5F - 0.5F));
         }

         return data;
      }
   }
}
