package ic2.core.item.renders.models;

import ic2.core.item.misc.CropSeedItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.items.BaseItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CropItemModel extends BaseItemModel<CropItemModel> {
   @Override
   public void init() {
      this.setParticleTexture(IC2Textures.getMappedEntriesItemIC2("crops").get("seed_bag"));
      this.quads.addAll(QuadBaker.createQuadsFromTexture(-1, this.getParticleIcon(), this.getTransformMap().gui));
      this.initOther(new CropItemModel());
   }

   @Override
   public ItemOverrides getOverrides() {
      return CropItemModel.CropOverride.INSTANCE;
   }

   public static class CropOverride extends ItemOverrides {
      public static final CropItemModel.CropOverride INSTANCE = new CropItemModel.CropOverride();

      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int randomValue) {
         ItemStack modelStack = CropSeedItem.getDisplayItem(stack);
         return !modelStack.isEmpty() ? Minecraft.getInstance().getItemRenderer().getModel(modelStack, worldIn, entityIn, randomValue) : model;
      }
   }
}
