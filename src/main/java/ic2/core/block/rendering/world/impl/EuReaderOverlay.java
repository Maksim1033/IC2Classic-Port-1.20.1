package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.item.tool.EUReaderTool;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.platform.rendering.misc.GLUtils;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class EuReaderOverlay implements IWorldOverlay {
   public static final int EU_READER_COLOR = -2130720768;

   @Override
   public void cleanup() {
   }

   @Override
   public void update(Level world, Player player) {
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof EUReaderTool)) {
         stack = player.getOffhandItem();
         if (!(stack.getItem() instanceof EUReaderTool)) {
            return;
         }
      }

      CompoundTag nbt = StackUtil.getNbtData(stack);
      String dim = nbt.getString("dim");
      BlockPos pos = BlockPos.of(nbt.getLong("pos"));
      if (dim.equals(world.dimension().location().toString()) && !(pos.distSqr(player.blockPosition()) > 6400.0)) {
         GLUtils.enableHighlight(true);
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderUtils.draw(event.getPoseStack(), (B, M) -> {
            B.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            RenderShapes.renderColorCube(new AABB(pos).inflate(0.10000000149011612), -2130720768, B, M);
         }).end();
         GLUtils.disableHighlight(true);
      }
   }
}
