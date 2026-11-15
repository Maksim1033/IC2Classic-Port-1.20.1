package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public class BlockHighlighter implements IWorldOverlay {
   public static final BlockHighlighter INSTANCE = new BlockHighlighter();
   List<BlockHighlighter.Highlighter> highlights = CollectionUtils.<BlockHighlighter.Highlighter>createList();

   public void addHighlight(BlockPos pos) {
      this.highlights.add(new BlockHighlighter.Highlighter(pos, 1200, -2130720768, false, null));
   }

   public void addHighlight(BlockPos pos, int ticks) {
      this.highlights.add(new BlockHighlighter.Highlighter(pos, ticks, -2130720768, false, null));
   }

   public void addHighlight(BlockPos pos, int ticks, int color) {
      this.highlights.add(new BlockHighlighter.Highlighter(pos, ticks, color, false, null));
   }

   public void addHighlight(BlockPos pos, int ticks, int color, boolean doubleSided) {
      this.highlights.add(new BlockHighlighter.Highlighter(pos, ticks, color, doubleSided, null));
   }

   public void addHighlight(BlockPos pos, int ticks, int color, boolean doubleSided, BlockHighlighter.IHighlightController controller) {
      this.highlights.add(new BlockHighlighter.Highlighter(pos, ticks, color, doubleSided, controller));
   }

   @Override
   public void cleanup() {
      this.highlights.clear();
   }

   @Override
   public void update(Level world, Player player) {
      if (!this.highlights.isEmpty()) {
         this.highlights.removeIf(BlockHighlighter.Highlighter::update);
      }
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      if (!this.highlights.isEmpty()) {
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderUtils.draw(event.getPoseStack(), (B, M) -> {
            B.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            for (BlockHighlighter.Highlighter entry : this.highlights) {
               if (helper.isVisible(entry.getRenderBox())) {
                  if (entry.isDoubleSided()) {
                     RenderShapes.renderInvertedColorCube(entry.getRenderBox(), entry.getColor(), B, M);
                  } else {
                     RenderShapes.renderColorCube(entry.getRenderBox(), entry.getColor(), B, M);
                  }
               }
            }
         }).end();
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
      }
   }

   public static class Highlighter {
      public int ticksLeft;
      BlockHighlighter.IHighlightController controller;
      public BlockPos pos;
      public AABB renderBox;
      public boolean doubleSided;
      public int color;

      public Highlighter(BlockPos pos, int ticks, int color, boolean doubleSided, BlockHighlighter.IHighlightController controller) {
         this.ticksLeft = ticks;
         this.pos = pos;
         this.color = color;
         this.doubleSided = doubleSided;
         this.controller = controller;
         this.renderBox = new AABB(pos).inflate(0.1);
      }

      public boolean update() {
         this.ticksLeft--;
         return this.ticksLeft <= 0 || this.controller != null && !this.controller.keepAlive(this);
      }

      public boolean isDoubleSided() {
         return this.doubleSided;
      }

      public int getColor() {
         return this.color;
      }

      public AABB getRenderBox() {
         return this.renderBox;
      }
   }

   public interface IHighlightController {
      boolean keepAlive(BlockHighlighter.Highlighter var1);
   }
}
