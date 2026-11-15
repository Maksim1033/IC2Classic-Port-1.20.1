package ic2.core.utils.codegen;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;

public class AutomatedTextureGen {
   public static void generateTextures(String basePath) {
      try {
         generateTextures("ic2", basePath, true, "block", "item", "misc");
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public static void deletePackages(String mod, String basePath) throws IOException {
      Files.walk(Paths.get(basePath + "/resources/assets/" + mod + "/textures")).filter(AutomatedTextureGen::isPackage).forEach(AutomatedTextureGen::remove);
   }

   public static void generateTextures(String mod, String basePath, boolean filterDuplicates, String... validFolders) throws IOException {
      deletePackages(mod, basePath);
      AutomatedTextureGen.TextureCache cache = new AutomatedTextureGen.TextureCache(filterDuplicates);
      Path path = Paths.get(basePath + "/resources/assets/" + mod + "/textures");
      System.out.println("Finding Texture Folders");
      ObjectList<Path> files = ObjectIterators.pour(Arrays.stream(validFolders).map(path::resolve).flatMap(AutomatedTextureGen::walk).iterator());
      System.out.println("Filtering Textures: [" + files.size() + "] Potential Textures Found");
      files.removeIf(x$0 -> Files.isDirectory(x$0));
      files.removeIf(AutomatedTextureGen::isInvalidFile);
      files.sort(AutomatedTextureGen.PathComparator.INSTANCE);
      System.out.println("Generating Texture Cache: [" + files.size() + "] Textures Found");
      long time = System.nanoTime();
      files.forEach(T -> cache.addTexture(T, getTexture(mod, T, path)));
      time = System.nanoTime() - time;
      System.out
         .println(
            "Textures Processed: "
               + cache.processed
               + ", Textures Left: "
               + cache.newEntries
               + ", Texture Exact Duplicates: "
               + (cache.processed - cache.newEntries)
               + MathUtils.toLagString(" Process Time", time)
         );

      for (String s : validFolders) {
         generateTextureFiles(path, mod, s, cache);
      }

      System.out.println("Updated packages");
   }

   public static void generateTextureFiles(Path basePath, String mod, String folder, AutomatedTextureGen.TextureCache cache) throws IOException {
      for (Path path : IterableWrapper.wrap(Files.walk(basePath.resolve(folder)).filter(x$0 -> Files.isDirectory(x$0)).iterator())) {
         ObjectList<Path> files = ObjectIterators.pour(Files.walk(path, 1).iterator());
         files.removeIf(AutomatedTextureGen::isInvalidFile);
         files.sort(AutomatedTextureGen.PathComparator.INSTANCE);
         if (!files.isEmpty()) {
            JsonObject obj = new JsonObject();
            ObjectListIterator e = files.iterator();

            while (e.hasNext()) {
               Path file = (Path)e.next();
               obj.addProperty(getFileName(file), cache.getTexturePath(getTexture(mod, file, basePath)).toString());
            }

            try {
               JsonWriter writer = new JsonWriter(Files.newBufferedWriter(path.resolve("texture_package.json")));

               try {
                  writer.setIndent("\t");
                  Streams.write(obj, writer);
               } catch (Throwable var12) {
                  try {
                     writer.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }

                  throw var12;
               }

               writer.close();
            } catch (Exception var13) {
               var13.printStackTrace();
            }
         }
      }
   }

   private static Stream<Path> walk(Path path) {
      try {
         return Files.walk(path);
      } catch (Exception var2) {
         return Stream.empty();
      }
   }

   private static ResourceLocation getTexture(String mod, Path texture, Path basePath) {
      String s = basePath.relativize(texture).toString().replace("\\", "/");
      return new ResourceLocation(mod, s.substring(0, s.lastIndexOf(".")));
   }

   private static String getFileName(Path path) {
      String s = path.getFileName().toString();
      return s.substring(0, s.lastIndexOf("."));
   }

   private static boolean isPackage(Path path) {
      return path.getFileName().toString().equals("texture_package.json");
   }

   private static boolean isInvalidFile(Path path) {
      return !path.getFileName().toString().endsWith("png");
   }

   private static void remove(Path path) {
      try {
         Files.deleteIfExists(path);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   private static class PathComparator implements Comparator<Path> {
      public static Comparator<Path> INSTANCE = new AutomatedTextureGen.PathComparator();

      public int compare(Path o1, Path o2) {
         return Collator.getInstance(Locale.GERMANY).compare(o1.toString(), o2.toString());
      }
   }

   public static class TextureCache {
      Object2ObjectMap<ResourceLocation, int[]> images = CollectionUtils.<ResourceLocation, int[]>createLinkedMap();
      Object2ObjectMap<ResourceLocation, ResourceLocation> paths = CollectionUtils.<ResourceLocation, ResourceLocation>createLinkedMap();
      int processed = 0;
      int newEntries = 0;
      boolean filterDuplicates;

      public TextureCache(boolean filterDuplicates) {
         this.filterDuplicates = filterDuplicates;
      }

      public void addTexture(Path path, ResourceLocation location) {
         this.processed++;
         if (!this.images.containsKey(location)) {
            try {
               BufferedImage image = ImageIO.read(Files.newInputStream(path));
               if (image == null) {
                  System.out.println("No Image found: " + path);
                  return;
               }

               int[] imageData = this.toPixels(image);
               ResourceLocation match = this.findMatchingImage(imageData);
               if (match == null) {
                  this.images.put(location, imageData);
                  this.paths.put(location, location);
                  this.newEntries++;
                  return;
               }

               this.images.put(location, (int[])this.images.get(match));
               this.paths.put(location, match);
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }

      public ResourceLocation getTexturePath(ResourceLocation mainPath) {
         return (ResourceLocation)this.paths.get(mainPath);
      }

      private ResourceLocation findMatchingImage(int[] image) {
         if (!this.filterDuplicates) {
            return null;
         } else {
            ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.images).iterator();

            while (var2.hasNext()) {
               Entry<ResourceLocation, int[]> entry = (Entry<ResourceLocation, int[]>)var2.next();
               if (Arrays.equals((int[])entry.getValue(), image)) {
                  return (ResourceLocation)entry.getKey();
               }
            }

            return null;
         }
      }

      private int[] toPixels(BufferedImage image) {
         int[] pixels = new int[image.getWidth() * image.getHeight()];
         image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
         return pixels;
      }
   }
}
