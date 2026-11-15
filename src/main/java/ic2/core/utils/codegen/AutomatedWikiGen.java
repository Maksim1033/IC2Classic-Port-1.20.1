package ic2.core.utils.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.FMLPaths;

public class AutomatedWikiGen {
   public static final String NEW_PAGE_STRING = "\u00a7<newPage>";

   public static void reload() {
      Path path = getBasePath();
      String wiki = path.resolve("wiki").toString();
      String base = path.resolve("src/main/resources/assets/ic2/lang/wiki_en_us.json").toString();
      String backup = path.resolve("src/main/resources/assets/ic2/lang/wiki_en_us_backup.json").toString();
      main("build_wiki", wiki, base, backup);
   }

   public static void main(String... args) {
      if (args[0].equalsIgnoreCase("build_wiki")) {
         backup(Paths.get(args[2]), Paths.get(args[3]));
         buildWiki(Paths.get(args[1]), Paths.get(args[2]));
      } else if (args[0].equalsIgnoreCase("extract_wiki")) {
         extractWiki(Paths.get(args[1]), Paths.get(args[2]));
      }
   }

   private static void backup(Path main, Path backup) {
      try {
         Files.copy(main, backup, StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   private static Path getBasePath() {
      return FMLPaths.GAMEDIR.get().getParent();
   }

   public static void buildWiki(Path baseFolder, Path wikiFile) {
      Map<String, String> resultMap = new Object2ObjectAVLTreeMap();
      Path path = baseFolder.getParent();

      try (Stream<Path> stream = Files.walk(baseFolder)) {
         stream.filter(x$0 -> Files.isRegularFile(x$0)).forEach(T -> {
            Path entry = path.relativize(T);
            resultMap.put(entry.toString().replace(File.separatorChar, '.').replace("_desc", ".desc").replace(".txt", ""), buildText(T));
         });
      } catch (Exception var12) {
         var12.printStackTrace();
      }

      try {
         JsonWriter writer = new JsonWriter(Files.newBufferedWriter(wikiFile));

         try {
            JsonObject result = new JsonObject();
            resultMap.forEach(result::addProperty);
            writer.setIndent("  ");
            Streams.write(result, writer);
         } catch (Throwable var8) {
            try {
               writer.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         writer.close();
      } catch (Exception var9) {
         var9.printStackTrace();
      }
   }

   public static void extractWiki(Path baseFolder, Path wikiFile) {
      try (BufferedReader reader = Files.newBufferedReader(wikiFile)) {
         JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

         for (Entry<String, JsonElement> element : obj.entrySet()) {
            String[] values = filterDesc(element.getKey().split("\\."));
            Path folder = createPath(baseFolder, values);
            if (Files.notExists(folder)) {
               Files.createDirectories(folder);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(folder.resolve(values[values.length - 1] + ".txt"))) {
               for (String s : element.getValue().getAsString().split("\n")) {
                  String[] split = s.split("\u00a7<newPage>");
                  if (split.length == 1) {
                     writer.write(s);
                     writer.newLine();
                  } else {
                     writer.write(split[0]);

                     for (int i = 1; i < split.length; i++) {
                        writer.newLine();
                        writer.newLine();
                        writer.newLine();
                        writer.write("\u00a7<newPage>");
                        writer.newLine();
                        writer.write(split[i]);
                     }

                     writer.newLine();
                  }
               }
            } catch (Exception var18) {
               var18.printStackTrace();
            }
         }
      } catch (Exception var20) {
         var20.printStackTrace();
      }
   }

   private static String buildText(Path file) {
      StringJoiner joiner = new StringJoiner("\n");

      try {
         List<String> list = Files.readAllLines(file);
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            if (list.get(i).startsWith("\u00a7<newPage>")) {
               int index;
               for (index = i - 1; index >= 0 && list.get(index).isEmpty(); m--) {
                  list.remove(index--);
                  i--;
               }

               list.set(index, list.get(index) + list.get(i));
               list.remove(i--);
               if (i + 1 < --m) {
                  list.set(i, list.get(i) + list.remove(i + 1));
                  m--;
               }
            }
         }

         for (String s : list) {
            joiner.add(s);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return joiner.toString();
   }

   private static String[] filterDesc(String[] array) {
      int length = array.length - 1;
      if (array[length].equals("desc")) {
         String[] result = new String[length];
         System.arraycopy(array, 0, result, 0, length);
         result[result.length - 1] = array[length - 1] + "_" + array[length];
         return result;
      } else {
         return array;
      }
   }

   private static Path createPath(Path basePath, String[] array) {
      int i = 1;

      for (int m = array.length - 1; i < m; i++) {
         basePath = basePath.resolve(array[i]);
      }

      return basePath;
   }
}
