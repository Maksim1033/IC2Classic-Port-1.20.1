package ic2.core.block.rendering.block;

import ic2.core.block.crops.CropRegistry;
import ic2.core.block.rendering.props.CropProperty;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class CropModel extends BaseModel {
   public static final CropModel INSTANCE = new CropModel();
   public static final CropEntry FALLBACK = new CropEntry(null, 0);
   Map<CropEntry, List<BakedQuad>> quads = CollectionUtils.<CropEntry, List<BakedQuad>>createLinkedMap();

   @Override
   public void init() {
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2("crops").get("cropsticks"));
      this.quads = CropRegistry.REGISTRY.createQuads(Minecraft.useFancyGraphics());
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      CropEntry entry = (CropEntry)extraData.get(CropProperty.CROP);
      return entry != null ? this.quads.getOrDefault(entry, Collections.emptyList()) : this.quads.getOrDefault(FALLBACK, Collections.emptyList());
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }
}
