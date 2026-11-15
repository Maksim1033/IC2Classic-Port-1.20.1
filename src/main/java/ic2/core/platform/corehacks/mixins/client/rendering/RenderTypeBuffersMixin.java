package ic2.core.platform.corehacks.mixins.client.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.SortedMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({RenderBuffers.class})
public interface RenderTypeBuffersMixin {
   @Accessor("fixedBuffers")
   SortedMap<RenderType, BufferBuilder> getFixedBuffers();
}
