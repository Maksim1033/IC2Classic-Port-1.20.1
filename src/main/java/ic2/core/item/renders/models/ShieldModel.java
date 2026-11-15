package ic2.core.item.renders.models;

import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.models.BaseModel;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShieldModel extends BaseModel {
   private static final ResourceLocation modelNormal = new ResourceLocation("minecraft:models/item/shield");
   private static final ResourceLocation modelBlocking = new ResourceLocation("minecraft:models/item/shield_blocking");
   ModelResourceLocation location;

   public ShieldModel(boolean isBlocking) {
      this.setTransforms(IC2Models.getTransformMap(isBlocking ? modelBlocking : modelNormal));
   }

   public ShieldModel setOther(ModelResourceLocation texture) {
      this.location = texture;
      return this;
   }

   @Override
   public ItemOverrides getOverrides() {
      return new ShieldModel.ShieldOverrideList(this.location);
   }

   @Override
   public boolean isCustomRenderer() {
      return true;
   }

   @Override
   public void init() {
   }

   public static class ShieldOverrideList extends ItemOverrides {
      ModelResourceLocation model;

      public ShieldOverrideList(ModelResourceLocation location) {
         this.model = location;
      }

      @Nullable
      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int randomValue) {
         return this.model != null && entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack
            ? Minecraft.getInstance().getModelManager().getModel(this.model)
            : super.resolve(model, stack, worldIn, entityIn, randomValue);
      }
   }
}
