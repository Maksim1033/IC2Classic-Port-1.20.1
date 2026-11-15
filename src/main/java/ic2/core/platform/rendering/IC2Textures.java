package ic2.core.platform.rendering;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class IC2Textures {
   public static final IC2Textures INSTANCE = new IC2Textures();
   Map<ResourceLocation, Map<String, TextureAtlasSprite>> spriteMap = CollectionUtils.<ResourceLocation, Map<String, TextureAtlasSprite>>createMap();
   List<Material> materials = CollectionUtils.<Material>createList();
   List<IC2Textures.ITextureEntry> toLoad = new ObjectArrayList();

   public void registerRenderMaterial(Material material) {
      this.materials.add(material);
   }

   public static Map<String, TextureAtlasSprite> getMappedEntries(ResourceLocation location) {
      Map<String, TextureAtlasSprite> m = INSTANCE.spriteMap.get(location);
      if (m == null) {
         throw new ResourceLocationException(location.toString() + " is not registered as a texture location");
      } else {
         return m;
      }
   }

   public static Map<String, TextureAtlasSprite> getMappedEntries(String mod, String id) {
      return getMappedEntries(new ResourceLocation(mod, id));
   }

   public static Map<String, TextureAtlasSprite> getMappedEntriesItem(String mod, String id) {
      return getMappedEntries(new ResourceLocation(mod, id.isEmpty() ? "item" : "item/" + id));
   }

   public static Map<String, TextureAtlasSprite> getMappedEntriesBlock(String mod, String id) {
      return getMappedEntries(new ResourceLocation(mod, id.isEmpty() ? "block" : "block/" + id));
   }

   public static Map<String, TextureAtlasSprite> getMappedEntriesIC2(String id) {
      return getMappedEntries(new ResourceLocation(IC2.MOD_ID, id));
   }

   public static Map<String, TextureAtlasSprite> getMappedEntriesBlockIC2(String id) {
      return getMappedEntries(new ResourceLocation(IC2.MOD_ID, id.isEmpty() ? "block" : "block/" + id));
   }

   public static Map<String, TextureAtlasSprite> getMappedEntriesItemIC2(String id) {
      return getMappedEntries(new ResourceLocation(IC2.MOD_ID, id.isEmpty() ? "item" : "item/" + id));
   }

   private void loadTextures(Consumer<ResourceLocation> loader) {
      this.load();

      for (IC2Textures.ITextureEntry load : this.toLoad) {
         load.load(loader);
      }
   }

   @SubscribeEvent
   public void postLoad(Post event) {
      if (event.getAtlas().location().getPath().equals("textures/atlas/blocks.png")) {
         this.load();
         for (IC2Textures.ITextureEntry load : this.toLoad) {
            this.spriteMap.put(load.genKey(), load.mapData(event.getAtlas()));
         }
      }
   }

   public void loadRenderMaterials(Consumer<Material> materials) {
      this.materials.forEach(materials);
   }

   private void load() {
      this.toLoad.clear();
      ResourceManager manager = Minecraft.getInstance().getResourceManager();
      Map<ResourceLocation, Resource> resources = manager.listResources("textures", T -> T.getPath().endsWith("texture_package.json"));

      for (Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
         String path = this.toPath(entry.getKey());
         int slash = path.indexOf(47);
         String target;
         String type;
         if (slash == -1) {
            target = path;
            type = "";
         } else {
            target = path.substring(0, slash);
            type = path.substring(slash + 1);
         }

         boolean isBlock = target.equals("block");

         try (BufferedReader reader = entry.getValue().openAsReader()) {
            Map<String, ResourceLocation> map = CollectionUtils.<String, ResourceLocation>createMap();
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            for (Entry<String, JsonElement> subEntry : obj.entrySet()) {
               if (subEntry.getValue().isJsonPrimitive()) {
                  ResourceLocation texture = ResourceLocation.tryParse(subEntry.getValue().getAsString());
                  if (texture != null) {
                     if (isBlock) {
                        this.decodeSides(subEntry.getKey(), texture, map);
                     } else {
                        map.put(subEntry.getKey(), texture);
                     }
                  }
               }
            }

            this.toLoad.add(new IC2Textures.MappedTextureEntry(entry.getKey().getNamespace(), target, type, map));
         } catch (Exception var18) {
            var18.printStackTrace();
         }
      }
   }

   private void decodeSides(String name, ResourceLocation texture, Map<String, ResourceLocation> map) {
      if (this.isSpecialDirection(name)) {
         for (String s : this.convertToVariants(name)) {
            map.put(s, texture);
         }
      } else {
         int lastUnderscore = name.lastIndexOf(95);
         if (lastUnderscore >= 0) {
            String subName = name.substring(lastUnderscore + 1);
            if (this.isSpecialDirection(subName)) {
               String pre = name.substring(0, lastUnderscore);

               for (String s : this.convertToVariants(subName)) {
                  map.put(pre + "_" + s, texture);
               }

               return;
            }
         }

         map.put(name, texture);
      }
   }

   private Iterable<String> convertToVariants(String s) {
      switch (s) {
         case "vertical":
            return CollectionUtils.<String>asList("up", "down", s);
         case "horizontal":
            return CollectionUtils.<String>asList("east", "west", "north", "south", s);
         case "side":
            return CollectionUtils.<String>asList("east", "west", "north", "south", s);
         case "xaxis":
            return CollectionUtils.<String>asList("east", "west", s);
         case "yaxis":
            return CollectionUtils.<String>asList("up", "down", s);
         case "zaxis":
            return CollectionUtils.<String>asList("north", "south", s);
         case "back":
            return CollectionUtils.<String>asList("north", s);
         case "front":
            return CollectionUtils.<String>asList("south", s);
         case "left":
            return CollectionUtils.<String>asList("west", s);
         case "right":
            return CollectionUtils.<String>asList("east", s);
         case "top":
            return CollectionUtils.<String>asList("up", s);
         case "bottom":
            return CollectionUtils.<String>asList("down", s);
         case "all":
            return CollectionUtils.<String>asList("east", "west", "north", "south", "up", "down", s);
         default:
            return Collections.emptyList();
      }
   }

   private boolean isSpecialDirection(String s) {
      switch (s) {
         case "all":
         case "vertical":
         case "horizontal":
         case "xaxis":
         case "yaxis":
         case "zaxis":
         case "side":
         case "top":
         case "bottom":
         case "left":
         case "right":
         case "front":
         case "back":
            return true;
         default:
            return false;
      }
   }

   private String toPath(ResourceLocation id) {
      String s = id.getPath();
      return s.substring(s.indexOf("/") + 1, s.lastIndexOf("/"));
   }

   public interface ITextureEntry {
      ResourceLocation genKey();

      Map<String, TextureAtlasSprite> mapData(TextureAtlas var1);

      void load(Consumer<ResourceLocation> var1);
   }

   public static class MappedTextureEntry implements IC2Textures.ITextureEntry {
      String mod;
      String target;
      String type;
      Map<String, ResourceLocation> sprites;

      public MappedTextureEntry(String mod, String target, String type, Map<String, ResourceLocation> sprites) {
         this.mod = mod;
         this.target = target;
         this.type = type;
         this.sprites = sprites;
      }

      @Override
      public ResourceLocation genKey() {
         return new ResourceLocation(this.mod, this.type.isEmpty() ? this.target : this.target + "/" + this.type);
      }

      @Override
      public Map<String, TextureAtlasSprite> mapData(TextureAtlas texture) {
         Map<String, TextureAtlasSprite> spriteMap = CollectionUtils.<String, TextureAtlasSprite>createLinkedMap();

         for (Entry<String, ResourceLocation> entry : this.sprites.entrySet()) {
            spriteMap.put(entry.getKey(), texture.getSprite(entry.getValue()));
         }

         return spriteMap;
      }

      @Override
      public void load(Consumer<ResourceLocation> location) {
         this.sprites.values().forEach(location);
      }
   }
}
