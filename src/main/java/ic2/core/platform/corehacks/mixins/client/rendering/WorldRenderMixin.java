package ic2.core.platform.corehacks.mixins.client.rendering;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({LevelRenderer.class})
public interface WorldRenderMixin {
   @Accessor("renderBuffers")
   RenderBuffers getBuffers();
}
