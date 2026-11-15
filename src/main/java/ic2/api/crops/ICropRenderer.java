package ic2.api.crops;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICropRenderer {
   @OnlyIn(Dist.CLIENT)
   List<BakedQuad> createQuadsForStage(int var1, boolean var2, FaceBakery var3);
}
