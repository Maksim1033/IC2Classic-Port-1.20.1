package ic2.core.platform.corehacks.mixins.client;

import ic2.core.IC2;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@OnlyIn(Dist.CLIENT)
@Mixin({ClientLanguage.class})
public class LangMixin {
   @Inject(
      method = {"loadFrom"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;",
         remap = false
      )},
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private static void onResourceCallBack(
      ResourceManager manager, List<String> languages, boolean defaultRightToLeft, CallbackInfoReturnable<ClientLanguage> info, Map<String, String> map
   ) {
      IC2.PLATFORM.registerCustomLang(T -> {
         int i = 0;

         for (int m = languages.size(); i < m; i++) {
            String s = String.format("lang/%1$s_%2$s.json", T.getPath(), languages.get(i));

            try {
               loadLang(manager.getResourceStack(new ResourceLocation(T.getNamespace(), s)), map);
            } catch (Exception var8) {
               IC2.LOGGER.warn("Skipped language file: {}:{} ({})", T.getNamespace(), s, var8.toString());
            }
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
