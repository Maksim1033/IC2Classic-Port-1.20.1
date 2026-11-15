package ic2.core.platform.events;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ic2.core.IC2;
import ic2.core.energy.EnergyNetGlobal;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ExportCommand {
   public static void export(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> source = Commands.literal("ic2c_export");
      source.executes(ExportCommand::execute);
      dispatcher.register(source);
   }

   public static void energyNet(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ic2c_energy_grids")
               .requires(T -> T.hasPermission(T.getServer().getOperatorUserPermissionLevel())))
            .executes(ExportCommand::executeEnet)
      );
   }

   private static int executeEnet(CommandContext<CommandSourceStack> ignored) {
      EnergyNetGlobal.buildDebugInfo();
      return 0;
   }

   public static int execute(CommandContext<CommandSourceStack> context) {
      Path path = IC2.PLATFORM.getConfigFolder().resolve("ic2c").resolve("Datapack.zip");

      try {
         Files.deleteIfExists(path);

         try (FileSystem system = FileSystems.newFileSystem(path, Map.of("create", "true"))) {
            exportRecipes(system, IC2.RECIPES.get(true).createRecipeFiles());
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("successfully exported datapack"), false);
      return 0;
   }

   private static void exportRecipes(FileSystem system, Map<ResourceLocation, JsonObject> map) {
      map.forEach((K, V) -> exportRecipe(system, K, V));
   }

   private static void exportRecipe(FileSystem system, ResourceLocation location, JsonObject obj) {
      try {
         Path path = system.getPath("data", location.getNamespace(), location.getPath() + ".json");
         Files.createDirectories(path.getParent());

         try {
            JsonWriter writer = new JsonWriter(Files.newBufferedWriter(path));

            try {
               writer.setIndent("\t");
               Streams.write(obj, writer);
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
      } catch (Exception var10) {
         var10.printStackTrace();
      }
   }
}
