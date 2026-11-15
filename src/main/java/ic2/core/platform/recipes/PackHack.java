package ic2.core.platform.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class PackHack {
   public static void loadHack(Consumer<RepositorySource> list) {
      list.accept(new PackHack.PackFinder());
   }

   public static class IC2RecipeHacker implements PackResources {
      Object2IntMap<ResourceLocation> recipesToSerialize = new Object2IntOpenHashMap<>();
      String[] loaders = new String[]{
         "{\"type\": \"ic2:crafting_ic2_automated\",\"enabled\": true}",
         "{\"type\": \"ic2:cooking_ic2_serializer\",\"enabled\": true}",
         "{\"type\": \"ic2:single_item_ic2_serializer\",\"enabled\": true}",
         "{\"type\": \"ic2:smithing_ic2_serializer\",\"enabled\": true}"
      };

      @Override
      public void close() {
      }

      @Override
      public String packId() {
         return "mod:ic2c";
      }

      @Override
      public boolean isBuiltin() {
         return true;
      }

      @Nullable
      @Override
      public IoSupplier<InputStream> getRootResource(String... paths) {
         return null;
      }

      @Nullable
      @Override
      public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
         if (type == PackType.SERVER_DATA) {
            String path = location.getPath();
            if (path.startsWith("tags/")) {
               ResourceLocation cleanLoc = new ResourceLocation(location.getNamespace(), path.substring(0, path.length() - 5));
               java.util.Collection<String> tags = IC2Tags.TAGS_TO_LOAD.getValue(cleanLoc);
               if (tags != null && !tags.isEmpty()) {
                  JsonArray array = new JsonArray();
                  for (String tag : tags) {
                     array.add(tag);
                  }
                  JsonObject obj = new JsonObject();
                  obj.addProperty("replace", false);
                  obj.add("values", array);
                  byte[] bytes = obj.toString().getBytes(StandardCharsets.UTF_8);
                  return () -> new ByteArrayInputStream(bytes);
               }
               return null;
            }

            if (path.startsWith("recipes/")) {
               int id = this.recipesToSerialize.getInt(location);
               if (id == 0) {
                  return null;
               }
               byte[] bytes = this.loaders[id - 1].getBytes(StandardCharsets.UTF_8);
               return () -> new ByteArrayInputStream(bytes);
            }

            if (path.startsWith("advancements/")) {
               Builder builder = IC2Advancements.ADVANCEMENTS.getValue(
                  new ResourceLocation(location.getNamespace(), path.substring("advancements/".length(), path.length() - 5))
               );
               if (builder != null) {
                  byte[] bytes = builder.serializeToJson().toString().getBytes(StandardCharsets.UTF_8);
                  return () -> new ByteArrayInputStream(bytes);
               }
               return null;
            }
         }
         return null;
      }

      @Override
      public void listResources(PackType type, String namespaceIn, String pathIn, PackResources.ResourceOutput output) {
         if (type == PackType.SERVER_DATA) {
            if (pathIn.startsWith("recipes")) {
               for (ResourceLocation location : AdvRecipeRegistry.INSTANCE.getRecipes().getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), "recipes/" + location.getPath() + ".json");
                  this.recipesToSerialize.put(resLoc, 1);
                  output.accept(resLoc, () -> new ByteArrayInputStream(this.loaders[0].getBytes(StandardCharsets.UTF_8)));
               }
               for (ResourceLocation location : AdvRecipeRegistry.INSTANCE.getCooking().getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), "recipes/" + location.getPath() + ".json");
                  this.recipesToSerialize.put(resLoc, 2);
                  output.accept(resLoc, () -> new ByteArrayInputStream(this.loaders[1].getBytes(StandardCharsets.UTF_8)));
               }
               for (ResourceLocation location : AdvRecipeRegistry.INSTANCE.getSingleRecipes().getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), "recipes/" + location.getPath() + ".json");
                  this.recipesToSerialize.put(resLoc, 3);
                  output.accept(resLoc, () -> new ByteArrayInputStream(this.loaders[2].getBytes(StandardCharsets.UTF_8)));
               }
               for (ResourceLocation location : AdvRecipeRegistry.INSTANCE.getSmithingRecipes().getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), "recipes/" + location.getPath() + ".json");
                  this.recipesToSerialize.put(resLoc, 4);
                  output.accept(resLoc, () -> new ByteArrayInputStream(this.loaders[3].getBytes(StandardCharsets.UTF_8)));
               }
            } else if (pathIn.startsWith("tags")) {
               for (ResourceLocation location : IC2Tags.TAGS_TO_LOAD.getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), location.getPath() + ".json");
                  if (location.getPath().startsWith(pathIn)) {
                     IoSupplier<InputStream> sup = this.getResource(type, resLoc);
                     if (sup != null) {
                        output.accept(resLoc, sup);
                     }
                  }
               }
            } else if (pathIn.startsWith("advancements")) {
               for (ResourceLocation location : IC2Advancements.ADVANCEMENTS.getResourceForMod(namespaceIn)) {
                  ResourceLocation resLoc = new ResourceLocation(location.getNamespace(), "advancements/" + location.getPath() + ".json");
                  IoSupplier<InputStream> sup = this.getResource(type, resLoc);
                  if (sup != null) {
                     output.accept(resLoc, sup);
                  }
               }
            }
         }
      }

      @Override
      public Set<String> getNamespaces(PackType type) {
         if (type == PackType.CLIENT_RESOURCES) {
            return ObjectSets.emptySet();
         } else {
            Set<String> set = CollectionUtils.<String>createSet();
            for (IModInfo info : ModList.get().getMods()) {
               set.add(info.getModId());
            }
            set.add("ic2");
            set.add("minecraft");
            return set;
         }
      }

      @Nullable
      @Override
      public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
         return (T)(deserializer.getMetadataSectionName().equalsIgnoreCase("pack") ? new PackMetadataSection(Component.literal("IC2ServerPack"), 15) : null);
      }
   }

   public static class PackFinder implements RepositorySource {
      @Override
      public void loadPacks(Consumer<Pack> acceptor) {
         Pack pack = Pack.readMetaAndCreate(
            "mod:ic2c",
            Component.literal("IC2Classic Resources"),
            true,
            id -> new PackHack.IC2RecipeHacker(),
            PackType.SERVER_DATA,
            Position.TOP,
            PackSource.BUILT_IN
         );
         if (pack != null) {
            acceptor.accept(pack);
         }
      }
   }
}
