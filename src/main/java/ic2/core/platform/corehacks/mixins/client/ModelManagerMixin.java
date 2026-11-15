package ic2.core.platform.corehacks.mixins.client;

import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(ModelManager.class)
public interface ModelManagerMixin {
   @Accessor("bakedRegistry")
   Map<ResourceLocation, BakedModel> getBakedRegistry();
}
