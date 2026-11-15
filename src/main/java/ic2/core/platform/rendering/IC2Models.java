package ic2.core.platform.rendering;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.joml.Quaternionf;
import net.minecraftforge.common.util.TransformationHelper;
import com.mojang.math.Transformation;
import ic2.core.IC2;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.rendering.features.ICachedModel;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.features.block.IDoubleSidedModel;
import ic2.core.platform.rendering.features.block.IEmptyBlockModel;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.features.item.ICustomItemModelTransform;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.platform.rendering.features.item.ILayeredItemModel;
import ic2.core.platform.rendering.features.item.IMultiItemModel;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.DoubleSidedBlockModel;
import ic2.core.platform.rendering.models.blocks.SimpleBlockModel;
import ic2.core.platform.rendering.models.items.MultiItemModel;
import ic2.core.platform.rendering.models.items.SimpleItemModel;
import ic2.core.platform.rendering.models.items.SimpleLayeredItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.plugins.IRegistryProvider;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import ic2.core.platform.corehacks.mixins.client.BlockModelShaperAccessor;
import ic2.core.platform.corehacks.mixins.client.ModelManagerMixin;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class IC2Models {
   private static final ResourceLocation BUILT_IN = new ResourceLocation("minecraft:builtin/generated");
   public static final ResourceLocation HELD_LOCATION = new ResourceLocation("minecraft:models/item/handheld");
   public static final ResourceLocation ITEM_LOCATION = new ResourceLocation("minecraft:models/item/generated");
   public static final ResourceLocation BLOCK_LOCATION = new ResourceLocation("minecraft:models/block/block");
   public static final IC2Models INSTANCE = new IC2Models();
   static Map<ResourceLocation, ItemTransforms> TRANSFORM_CACHE = CollectionUtils.<ResourceLocation, ItemTransforms>createMap();
   static ItemTransforms[] DEFAULT_TYPES = new ItemTransforms[3];
   Map<BaseModel, IRegistryProvider> allModels = CollectionUtils.<BaseModel, IRegistryProvider>createLinkedMap();
   List<ICachedModel> cached = CollectionUtils.<ICachedModel>createList();
   private final Map<BlockState, BakedModel> blockModels = new IdentityHashMap<>();

   public BakedModel getBlockModel(BlockState state) {
      return this.blockModels.get(state);
   }

   public static ItemTransforms getItemTransforms() {
      return DEFAULT_TYPES[0];
   }

   public static ItemTransforms getToolTransforms() {
      return DEFAULT_TYPES[1];
   }

   public static ItemTransforms getBlockTransforms() {
      return DEFAULT_TYPES[2];
   }

   public boolean loadModels() {
      boolean crashed = false;

      for (Entry<BaseModel, IRegistryProvider> entry : this.allModels.entrySet()) {
         BaseModel model = entry.getKey();
         if (model instanceof ICachedModel) {
            this.cached.add((ICachedModel)model);
         }

         try {
            model.init();
         } catch (Exception var6) {
            crashed = true;
            IC2.LOGGER
               .error(
                  "[{}] Model has a Crash. Finishing Model Loading for easier fixing, then Aborting Game. Error: ", entry.getValue().getRegistryName(), var6
               );
         }
      }

      return crashed;
   }

   @SubscribeEvent
   public void onModelLoadEvent(BakingCompleted event) {
      for (ICachedModel model : this.cached) {
         model.clearCache();
      }

      this.cached.clear();
      this.allModels.clear();
      TRANSFORM_CACHE.clear();
      DEFAULT_TYPES[1] = getTransformMap(ITEM_LOCATION);
      DEFAULT_TYPES[0] = getTransformMap(HELD_LOCATION);
      DEFAULT_TYPES[2] = getTransformMap(BLOCK_LOCATION);
      Map<ResourceLocation, BakedModel> models = ((ModelManagerMixin) event.getModelManager()).getBakedRegistry();
      this.handleBlocks(models);
      this.handleItems(models);
      if (this.loadModels()) {
         IC2.LOGGER.info("Found Crashing Models. Closing Game. Fix Models first");
         ServerLifecycleHooks.handleExit(0);
      }
   }

   public static boolean hasCustomModelStates(ResourceLocation location) {
      Block block = ForgeRegistryWrapper.getBlockValue(location);
      return block instanceof IBlockModel || block instanceof ICustomBlockModel || block instanceof IEmptyBlockModel;
   }

   public static boolean hasCustomItemModel(ResourceLocation location) {
      Item item = ForgeRegistryWrapper.getItemValue(location);
      return item instanceof ISimpleItemModel || item instanceof IItemModel || item instanceof ICustomItemModel || hasCustomModelStates(location);
   }

   private void handleBlocks(Map<ResourceLocation, BakedModel> models) {
      ItemModelShaper modelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
      BlockModelShaper blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
      Map<BlockState, BakedModel> newBlockModels = new IdentityHashMap<>();
      this.blockModels.clear();

      for (Block block : ForgeRegistries.BLOCKS) {
         int count = 0;
         if (block instanceof IBlockModel simple) {
            boolean doubleModel = block instanceof IDoubleSidedModel;

            for (BlockState state : simple.getModelStates()) {
               BaseModel model = (BaseModel)(doubleModel ? new DoubleSidedBlockModel(state, simple) : new SimpleBlockModel(state, simple));
               model.setTransforms(DEFAULT_TYPES[2]);
               if (state.getBlock() instanceof IRenderType type) {
                  model.setRenderTypes(this.create(state, type));
               }

               this.allModels.put(model, (IRegistryProvider)block);
               this.putIfNotPresent(models, model, BlockModelShaper.stateToModelLocation(state));
               newBlockModels.put(state, model);
               this.blockModels.put(state, model);
               count++;
            }
         }

         if (block instanceof ICustomBlockModel custom) {
            for (BlockState state : custom.getCustomStates()) {
               BaseModel model = custom.getForCustomState(state);
               model.setTransforms(DEFAULT_TYPES[2]);
               if (state.getBlock() instanceof IRenderType type) {
                  model.setRenderTypes(this.create(state, type));
               }

               this.allModels.put(model, (IRegistryProvider)block);
               this.putIfNotPresent(models, model, BlockModelShaper.stateToModelLocation(state));
               newBlockModels.put(state, model);
               this.blockModels.put(state, model);
               count++;
            }
         }

         if (count > 0) {
            modelShaper.register(block.asItem(), BlockModelShaper.stateToModelLocation(block.defaultBlockState()));
         }
      }

      if (!newBlockModels.isEmpty()) {
         try {
            Map<BlockState, BakedModel> currentCache = ((BlockModelShaperAccessor) blockModelShaper).getModelByStateCache();
            Map<BlockState, BakedModel> updatedCache = new IdentityHashMap<>(currentCache != null ? currentCache : java.util.Collections.emptyMap());
            updatedCache.putAll(newBlockModels);
            blockModelShaper.replaceCache(updatedCache);
         } catch (Exception e) {
            IC2.LOGGER.error("Failed to update BlockModelShaper cache with IC2 models", e);
         }
      }
   }

   private void handleItems(Map<ResourceLocation, BakedModel> models) {
      ItemModelShaper modelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

      for (Item item : ForgeRegistries.ITEMS) {
         List<BaseModel> modelsList = new ArrayList<>();
         if (item instanceof ISimpleItemModel simple && simple.shouldLoadModel()) {
            ItemStack stack = new ItemStack(item);
            BaseModel model = (BaseModel)(this.isLayered(stack)
               ? new SimpleLayeredItemModel(stack, simple.getTexture(), (ILayeredItemModel)item)
               : new SimpleItemModel(stack, simple.getTexture()));
            model.setTransforms(getTransforms(stack, DEFAULT_TYPES[1]));
            modelsList.add(model);
         }

         if (item instanceof IItemModel simple) {
            for (ItemStack stack : simple.getModelTypes()) {
               BaseModel model = (BaseModel)(this.isLayered(stack)
                  ? new SimpleLayeredItemModel(stack, simple.getSprite(stack), (ILayeredItemModel)item)
                  : new SimpleItemModel(stack, simple.getSprite(stack)));
               model.setTransforms(getTransforms(stack, DEFAULT_TYPES[1]));
               modelsList.add(model);
            }
         }

         if (item instanceof ICustomItemModel custom) {
            for (ItemStack stack : custom.getCustomTypes()) {
               BaseModel model = custom.getModel(stack);
               model.setTransforms(getTransforms(stack, DEFAULT_TYPES[1]));
               modelsList.add(model);
            }
         }

         if (!modelsList.isEmpty()) {
            for (BaseModel model : modelsList) {
               this.allModels.put(model, (IRegistryProvider)item);
            }

            ModelResourceLocation location = fromItem(item);
            if (item instanceof IMultiItemModel && modelsList.size() > 1) {
               this.putIfNotPresent(models, new MultiItemModel(modelsList, (IMultiItemModel)item), location);
               modelShaper.register(item, location);
            } else {
               this.putIfNotPresent(models, modelsList.get(0), location);
               modelShaper.register(item, location);
            }
         }
      }

      try {
         modelShaper.rebuildCache();
      } catch (Exception e) {
         IC2.LOGGER.error("Failed to rebuild ItemModelShaper cache", e);
      }
   }

   private void putIfNotPresent(Map<ResourceLocation, BakedModel> models, BakedModel model, ModelResourceLocation location) {
      if (isMissingModel(models.get(location))) {
         models.put(location, model);
      }
   }

   public static boolean isMissingModel(BakedModel model) {
      return model.getParticleIcon(ModelData.EMPTY)
         == Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
   }

   public static List<BakedQuad> generateItemQuads(int tintedIndex, TextureAtlasSprite sprite, ItemTransform transform) {
      return UnbakedGeometryHelper.bakeElements(
         UnbakedGeometryHelper.createUnbakedItemElements(tintedIndex, sprite.contents()),
         $ -> sprite,
         new SimpleModelState(convert(transform)),
         new ResourceLocation("ic2:item_baker")
      );
   }

   public static ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource source) {
      return getRenderTypes(state, source, ModelData.EMPTY);
   }

   public static ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource source, ModelData data) {
      return Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getRenderTypes(state, source, data);
   }

   private boolean isLayered(ItemStack stack) {
      if (stack.getItem() instanceof ILayeredItemModel layer && layer.isLayered(stack)) {
         return true;
      }

      return false;
   }

   public static ModelResourceLocation fromItem(Item item) {
      return new ModelResourceLocation(ForgeRegistryWrapper.getItemKey(item), "inventory");
   }

   public static TextureAtlasSprite getSafeIcon(TextureAtlasSprite sprite) {
      return sprite != null
         ? sprite
         : (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
   }

   public static ItemTransforms getTransforms(ItemStack stack, ItemTransforms defaultValue) {
      if (stack.getItem() instanceof ICustomItemModelTransform custom && custom.hasCustomTransform(stack)) {
         return getTransformMap(custom.getCustomTransform(stack));
      }

      return defaultValue;
   }

   private static Transformation convert(ItemTransform transform) {
      return new Transformation(
         transform.translation,
         TransformationHelper.quatFromXYZ(transform.rotation, true),
         transform.scale,
         TransformationHelper.quatFromXYZ(transform.rightRotation, true)
      );
   }

   private ChunkRenderTypeSet create(BlockState state, IRenderType type) {
      List<RenderType> types = CollectionUtils.<RenderType>createList();

      for (RenderType toCheck : RenderType.chunkBufferLayers()) {
         if (type.canRenderInLayer(state, toCheck)) {
            types.add(toCheck);
         }
      }

      return ChunkRenderTypeSet.of(types);
   }

   public static ItemTransforms getTransformMap(ResourceLocation location) {
      ItemTransforms result = TRANSFORM_CACHE.get(location);
      if (result == null) {
         result = getTransforms(location);
         TRANSFORM_CACHE.put(location, result);
      }

      return result;
   }

   public static ItemTransforms getTransformMap(ResourceLocation base, ResourceLocation customID, ResourceLocation other) {
      ItemTransforms result = TRANSFORM_CACHE.get(customID);
      if (result == null) {
         result = merge(getTransforms(other), getTransforms(base));
         TRANSFORM_CACHE.put(customID, result);
      }

      return result;
   }

   public static ItemTransforms getTransformMap(ResourceLocation base, ResourceLocation customID, String jsonText) {
      ItemTransforms result = TRANSFORM_CACHE.get(customID);
      if (result == null) {
         result = merge(getTransforms(jsonText), getTransforms(base));
         TRANSFORM_CACHE.put(customID, result);
      }

      return result;
   }

   public static ItemTransforms getTransforms(ResourceLocation location) {
      Optional<Resource> resource = Minecraft.getInstance()
         .getResourceManager()
         .getResource(new ResourceLocation(location.getNamespace(), location.getPath() + ".json"));
      if (resource.isPresent()) {
         try {
            ItemTransforms var4;
            try (BufferedReader reader = resource.get().openAsReader()) {
               BlockModel model = BlockModel.fromStream(reader);
               if (model.getParentLocation() == null || model.getParentLocation().equals(BUILT_IN)) {
                  return model.getTransforms();
               }

               var4 = merge(
                  model.getTransforms(),
                  getTransformMap(new ResourceLocation(model.getParentLocation().getNamespace(), "models/" + model.getParentLocation().getPath()))
               );
            }

            return var4;
         } catch (Exception var7) {
         }
      }

      return ItemTransforms.NO_TRANSFORMS;
   }

   public static ItemTransforms merge(ItemTransforms base, ItemTransforms other) {
      Builder<ItemDisplayContext, ItemTransform> builder = ImmutableMap.builder();

      for (ItemDisplayContext type : ItemDisplayContext.values()) {
         if (type.isModded()) {
            builder.put(type, select(base, other, type));
         }
      }

      return new ItemTransforms(
         select(base, other, ItemDisplayContext.THIRD_PERSON_LEFT_HAND),
         select(base, other, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
         select(base, other, ItemDisplayContext.FIRST_PERSON_LEFT_HAND),
         select(base, other, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND),
         select(base, other, ItemDisplayContext.HEAD),
         select(base, other, ItemDisplayContext.GUI),
         select(base, other, ItemDisplayContext.GROUND),
         select(base, other, ItemDisplayContext.FIXED),
         builder.build()
      );
   }

   private static ItemTransform select(ItemTransforms base, ItemTransforms other, ItemDisplayContext selected) {
      return base.hasTransform(selected) ? base.getTransform(selected) : other.getTransform(selected);
   }

   public static ItemTransforms getTransforms(String json) {
      try {
         return BlockModel.fromStream(new StringReader(json)).getTransforms();
      } catch (Exception var2) {
         var2.printStackTrace();
         return ItemTransforms.NO_TRANSFORMS;
      }
   }
}
