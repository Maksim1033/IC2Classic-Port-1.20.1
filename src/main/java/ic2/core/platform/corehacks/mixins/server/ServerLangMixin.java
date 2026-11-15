package ic2.core.platform.corehacks.mixins.server;

import ic2.core.IC2;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.server.LanguageHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({LanguageHook.class})
public class ServerLangMixin {
   @Shadow(
      remap = false
   )
   private static Map<String, String> modTable;

   @Inject(
      method = {"loadLanguage"},
      at = {@At("RETURN")},
      locals = LocalCapture.CAPTURE_FAILHARD,
      remap = false
   )
   private static void hookLangLoad(String language, MinecraftServer server, CallbackInfo info) {
      IC2.PLATFORM.registerCustomLang(T -> {
         String s = String.format("lang/%1$s_%2$s.json", T.getPath(), language);

         try {
            loadLang(server.getResourceManager().getResourceStack(new ResourceLocation(T.getNamespace(), s)), modTable);
         } catch (Exception var5) {
            IC2.LOGGER.warn("Skipped language file: {}:{} ({})", T.getNamespace(), s, var5.toString());
         }
      });
   }

   private static boolean loadLang(List<Resource> resources, Map<String, String> lang) {
      int old = lang.size();

      for (Resource iresource : resources) {
         try (InputStream inputstream = iresource.open()) {
            Language.loadFromJson(inputstream, lang::put);
         } catch (IOException var10) {
            IC2.LOGGER.warn("Failed to load translations from {}", iresource, var10);
         }
      }

      return old != lang.size();
   }
}
