package ic2.core.item.renders.models;

import org.joml.Quaternionf;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import ic2.api.events.RetextureEvent;
import ic2.core.item.tool.electric.ObscuratorTool;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.items.BaseItemModel;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class ObscuratorModel extends BaseItemModel<ObscuratorModel> {
   static final float NORTH_Z = 0.468625F;
   static final float SOUTH_Z = 0.531375F;
   static final Transformation MATRIX = new Transformation(new Vector3f(-0.25F, 0.0F, 0.0F), null, null, null);
   static final Vector3f[] OFFSETS = new Vector3f[]{
      new Vector3f(), new Vector3f(1.0F, 0.25F, 0.0F), new Vector3f(0.75F, 1.25F, 0.0F), new Vector3f(-0.25F, 1.0F, 0.0F)
   };
   static final Object2ObjectLinkedOpenHashMap<ObscuratorModel.RenderEntry, List<BakedQuad>> QUADS_STORAGE = new Object2ObjectLinkedOpenHashMap();
   List<BakedQuad> baseQuads = CollectionUtils.<BakedQuad>createList();
   ObscuratorModel.RenderEntry lastEntry = null;

   public ObscuratorModel(TextureAtlasSprite sprite) {
      this.setParticleTexture(sprite);
   }

   @Override
   public void init() {
      this.baseQuads.addAll(QuadBaker.createQuadsFromTexture(-1, this.getParticleIcon(), this.getTransformMap().gui));
      this.quads.addAll(this.baseQuads);
      this.initOther(new ObscuratorModel(this.getParticleIcon()));
      QUADS_STORAGE.clear();
   }

   @Override
   public ItemOverrides getOverrides() {
      return ObscuratorModel.ObscuratorOverrides.INSTANCE;
   }

   public void syncQuads(ItemStack stack) {
      RetextureEvent.TextureContainer container = ObscuratorTool.getContainer(stack);
      if (container == null && this.lastEntry != null) {
         this.quads.clear();
         this.quads.addAll(this.baseQuads);
         this.lastEntry = null;
         this.syncQuads();
      } else if (container != null) {
         ObscuratorModel.RenderEntry entry = new ObscuratorModel.RenderEntry(container);
         if (entry.equals(this.lastEntry)) {
            return;
         }

         List<BakedQuad> storage = (List<BakedQuad>)QUADS_STORAGE.getAndMoveToLast(entry);
         if (storage == null) {
            storage = entry.createQuads(this);
            QUADS_STORAGE.putAndMoveToLast(entry, storage);
            if (QUADS_STORAGE.size() > 500) {
               QUADS_STORAGE.removeFirst();
            }
         }

         this.quads.clear();
         this.quads.addAll(this.baseQuads);
         this.quads.addAll(storage);
         this.lastEntry = entry;
         this.syncQuads();
      }
   }

   public static class ObscuratorOverrides extends ItemOverrides {
      public static final ObscuratorModel.ObscuratorOverrides INSTANCE = new ObscuratorModel.ObscuratorOverrides();

      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int randomValue) {
         if (model instanceof ObscuratorModel) {
            ((ObscuratorModel)model).syncQuads(stack);
         }

         return model;
      }
   }

   public static class RenderEntry {
      BlockState state;
      Direction side;
      RetextureEvent.Rotation[] rotations;

      public RenderEntry(RetextureEvent.TextureContainer container) {
         this.state = container.getState();
         this.side = container.getSide();
         this.rotations = container.getRotations();
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.state, this.side, Arrays.hashCode((Object[])this.rotations));
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof ObscuratorModel.RenderEntry entry)
            ? false
            : entry.state.equals(this.state) && entry.side == this.side && Arrays.equals((Object[])entry.rotations, (Object[])this.rotations);
      }

      public List<BakedQuad> createQuads(BaseModel model) {
         BlockModelShaper shapes = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
         BakedModel shape = shapes.getBlockModel(this.state);
         if (shape == shapes.getModelManager().getMissingModel()) {
            return CollectionUtils.<BakedQuad>createList();
         } else {
            List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
            int colorState = 0;

            for (BakedQuad quad : shape.getQuads(this.state, this.side, RandomSource.create(this.state.getSeed(BlockPos.ZERO)), ModelData.EMPTY, null)) {
               RetextureEvent.Rotation rotation = this.rotations[colorState];
               Transformation matrix = new Transformation(
                  ObscuratorModel.OFFSETS[rotation.getIndex()], Axis.ZP.rotationDegrees((float)rotation.getRotation()), null, null
               );
               BakedQuad newQuad = QuadBaker.genQuad(
                  matrix.compose(ObscuratorModel.MATRIX), 6.0F, 2.0F, 14.0F, 10.0F, 0.468625F, quad.getSprite(), Direction.NORTH, -1, -1
               );
               quads.add(QuadBaker.copyWithTint(newQuad, quad.isTinted() ? colorState : -1));
               newQuad = QuadBaker.genQuad(
                  matrix.compose(ObscuratorModel.MATRIX), 6.0F, 2.0F, 14.0F, 10.0F, 0.531375F, quad.getSprite(), Direction.SOUTH, -1, -1
               );
               quads.add(QuadBaker.copyWithTint(newQuad, quad.isTinted() ? colorState : -1));
               colorState++;
            }

            return quads;
         }
      }
   }
}
