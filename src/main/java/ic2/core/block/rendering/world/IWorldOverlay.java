package ic2.core.block.rendering.world;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public interface IWorldOverlay {
   void cleanup();

   void update(Level var1, Player var2);

   void render(Level var1, Player var2, RenderLevelStageEvent var3, Frustum var4);
}
