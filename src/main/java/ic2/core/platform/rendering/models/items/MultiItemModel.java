package ic2.core.platform.rendering.models.items;

import ic2.core.platform.rendering.features.item.IMultiItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiItemModel extends BaseModel {
   List<BaseModel> models;
   IMultiItemModel lexer;
   ItemOverrides list = new MultiItemModel.MultiItemOverrides();

   public MultiItemModel(List<BaseModel> models, IMultiItemModel lexer) {
      this.models = models;
      this.lexer = lexer;
   }

   @Override
   public void init() {
      this.setParticleTexture(this.models.get(0).getParticleIcon());
   }

   @Override
   public ItemOverrides getOverrides() {
      return this.list;
   }

   class MultiItemOverrides extends ItemOverrides {
      public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entityIn, int randomValue) {
         return MultiItemModel.this.models
            .get(Mth.clamp(MultiItemModel.this.lexer.getModelIndexForStack(stack, entityIn), 0, MultiItemModel.this.models.size() - 1));
      }
   }
}
