package ic2.core.platform.rendering;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;

@OnlyIn(Dist.CLIENT)
public class PackHack {
   public static void doResourcePackHack() {
      Minecraft instance = Minecraft.getInstance();
      if (instance != null) {
         instance.getResourcePackRepository().addPackFinder(new PackHack.PackFinder());
      }
   }

   public static class CustomResourcePack implements PackResources {
      @Override
      public void close() {
      }

      @Override
      public String packId() {
         return "mod:ic2loader";
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
         if (type == PackType.CLIENT_RESOURCES) {
            String path = location.getPath();
            if (path.endsWith(".json")) {
               if (path.startsWith("models/item") && IC2Models.hasCustomItemModel(this.getPath(location, "models/item"))) {
                  return () -> new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
               }
               if (path.startsWith("blockstates") && IC2Models.hasCustomModelStates(this.getPath(location, "blockstates"))) {
                  return () -> new ByteArrayInputStream("{\"variants\":{\"\":{\"model\":\"block/air\"}}}".getBytes(StandardCharsets.UTF_8));
               }
            }
         }
         return null;
      }

      @Override
      public void listResources(PackType type, String namespaceIn, String pathIn, PackResources.ResourceOutput output) {
      }

      private ResourceLocation getPath(ResourceLocation id, String search) {
         String path = id.getPath();
         String s = path.substring(search.length() + 1);
         return new ResourceLocation(id.getNamespace(), s.substring(0, s.indexOf(".json")));
      }

      @Override
      public Set<String> getNamespaces(PackType type) {
         if (type == PackType.SERVER_DATA) {
            return ObjectSets.emptySet();
         } else {
            Set<String> set = new ObjectLinkedOpenHashSet<>(Lists.transform(ModList.get().getMods(), IModInfo::getModId));
            set.add("ic2");
            set.add("minecraft");
            return set;
         }
      }

      @Nullable
      @Override
      public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
         return (T)(deserializer.getMetadataSectionName().equalsIgnoreCase("pack") ? new PackMetadataSection(Component.literal("IC2ClientPack"), 15) : null);
      }
   }

   public static class DeveloperPack implements PackResources {
      private final IModFile modFile;

      public DeveloperPack(IModFile modFile) {
         this.modFile = modFile;
      }

      @Override
      public void close() {
      }

      @Override
      public String packId() {
         return "mod:ic2devpack";
      }

      @Override
      public boolean isBuiltin() {
         return false;
      }

      @Nullable
      @Override
      public IoSupplier<InputStream> getRootResource(String... paths) {
         return null;
      }

      @Nullable
      @Override
      public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
         if (type == PackType.CLIENT_RESOURCES) {
            String name = type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
            name = name.replaceFirst("ic2", "ic2dev");
            Path path = this.modFile.findResource(new String[]{name});
            if (Files.exists(path)) {
               return () -> Files.newInputStream(path, StandardOpenOption.READ);
            }
         }
         return null;
      }

      @Override
      public void listResources(PackType type, String namespaceIn, String pathIn, PackResources.ResourceOutput output) {
         if (type == PackType.CLIENT_RESOURCES) {
            Path root = this.modFile.findResource(new String[]{type.getDirectory(), namespaceIn.replaceFirst("ic2", "ic2dev")}).toAbsolutePath();
            Path inputPath = root.getFileSystem().getPath(pathIn.replaceFirst("ic2", "ic2dev"));

            try {
               Files.walk(root)
                  .filter(path -> !path.toString().endsWith(".mcmeta"))
                  .filter(path -> path.startsWith(inputPath))
                  .forEach(path -> {
                     ResourceLocation res = new ResourceLocation(namespaceIn, Joiner.on('/').join(root.relativize(path.toAbsolutePath())));
                     output.accept(res, () -> Files.newInputStream(path, StandardOpenOption.READ));
                  });
            } catch (IOException ignored) {
            }
         }
      }

      @Override
      public Set<String> getNamespaces(PackType type) {
         return type == PackType.CLIENT_RESOURCES ? Collections.singleton("ic2") : Collections.emptySet();
      }

      @Nullable
      @Override
      public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
         return (T)(deserializer.getMetadataSectionName().equalsIgnoreCase("pack")
            ? new PackMetadataSection(Component.literal("Restores some of the older IC2Classic textures"), 15)
            : null);
      }
   }

   public static class PackFinder implements RepositorySource {
      @Override
      public void loadPacks(Consumer<Pack> acceptor) {
         Pack pack1 = Pack.readMetaAndCreate(
            "mod:ic2loader",
            Component.literal("IC2Loader"),
            true,
            id -> new PackHack.CustomResourcePack(),
            PackType.CLIENT_RESOURCES,
            Position.BOTTOM,
            PackSource.BUILT_IN
         );
         if (pack1 != null) {
            acceptor.accept(pack1);
         }
         Pack pack2 = Pack.readMetaAndCreate(
            "mod:ic2devpack",
            Component.literal("IC2 Developer Pack"),
            false,
            id -> new PackHack.DeveloperPack(ModList.get().getModFileById("ic2").getFile()),
            PackType.CLIENT_RESOURCES,
            Position.TOP,
            PackSource.BUILT_IN
         );
         if (pack2 != null) {
            acceptor.accept(pack2);
         }
      }
   }
}
