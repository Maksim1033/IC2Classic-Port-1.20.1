package ic2.core.item.renders.models;

import ic2.core.item.misc.FluidDisplay;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.items.BaseItemModel;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidModel extends BaseItemModel<FluidModel> {
   Map<Fluid, List<BakedQuad>> fluidMap = CollectionUtils.<Fluid, List<BakedQuad>>createMap();
   Fluid last = Fluids.WATER;
   FluidModel.CustomOverride custom = new FluidModel.CustomOverride();

   @Override
   public void init() {
      this.setParticleTexture(
         (TextureAtlasSprite)Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(IClientFluidTypeExtensions.of(Fluids.WATER).getStillTexture())
      );

      for (Fluid fluid : ForgeRegistries.FLUIDS) {
         if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource()) {
            this.fluidMap
               .put(
                  fluid,
                  QuadBaker.createQuadsFromTexture(
                     0,
                     (TextureAtlasSprite)Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(IClientFluidTypeExtensions.of(fluid).getStillTexture()),
                     this.getTransformMap().gui
                  )
               );
         }
      }

      this.quads.addAll(this.fluidMap.getOrDefault(Fluids.WATER, Collections.emptyList()));
      this.initOther(new FluidModel());
   }

   @Override
   public ItemOverrides getOverrides() {
      return this.custom;
   }

   public void setFluid(Fluid fluid) {
      if (this.last != fluid) {
         this.last = fluid;
         this.quads.clear();
         List<BakedQuad> quads = this.fluidMap.get(fluid);
         if (quads == null) {
            quads = this.fluidMap.getOrDefault(Fluids.WATER, Collections.emptyList());
         }

         this.quads.addAll(quads);
         this.syncQuads();
      }
   }

   class CustomOverride extends ItemOverrides {
      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int randomValue) {
         FluidModel.this.setFluid(FluidDisplay.getFluid(stack));
         return model;
      }
   }
}
