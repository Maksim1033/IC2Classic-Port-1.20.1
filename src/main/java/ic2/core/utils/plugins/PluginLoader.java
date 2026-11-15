package ic2.core.utils.plugins;

import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.impl.ReloadMode;
import ic2.api.addons.IC2Feature;
import ic2.api.addons.IC2Plugin;
import ic2.api.addons.IModule;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public class PluginLoader {
   Map<String, IModule> modules = CollectionUtils.<String, IModule>createMap();
   Map<String, PluginLoader.Feature> featuresMap = CollectionUtils.<String, PluginLoader.Feature>createMap();

   public void init(int apiVersion, ConfigSection section) {
      for (PluginLoader.Module entry : this.getClasses()) {
         IModule module = entry.getModule();
         int api = entry.getPlugin().requiredAPIVersion();
         if (api <= 0 || api <= apiVersion) {
            List<PluginLoader.Feature> features = entry.getFeatures();
            if (module.canLoad(FMLEnvironment.dist)) {
               if (features.isEmpty()) {
                  BoolValue enabled = (BoolValue)section.addBool(entry.getPlugin().id(), true).setRequiredReload(ReloadMode.GAME);
                  if (enabled.get()) {
                     this.modules.put(entry.getPlugin().id(), entry.getModule());
                     entry.getModule().loadConfigs();
                  }
               } else {
                  ConfigSection pluginSection = section.addSubSection(entry.getPlugin().id());
                  BoolValue enabled = (BoolValue)pluginSection.addBool("enabled", true).setRequiredReload(ReloadMode.GAME);
                  if (!enabled.get()) {
                     IC2.LOGGER.info("Plugin disabled in config: {}", entry.getPlugin().id());
                  } else {
                     this.modules.put(entry.getPlugin().id(), entry.getModule());
                     entry.getModule().loadConfigs();

                     for (PluginLoader.Feature feature : features) {
                        int featureApi = feature.getFeature().requiredApiVersion();
                        if ((featureApi <= 0 || featureApi <= apiVersion)
                           && this.areDependenciesLoaded(feature.getFeature().depends(), feature.getFeature().requireAll())) {
                           enabled = (BoolValue)pluginSection.addBool(feature.getFeature().key(), true, new String[]{feature.getFeature().comment()})
                              .setRequiredReload(ReloadMode.GAME);
                           if (!enabled.get()) {
                              IC2.LOGGER.info("Feature disabled in config: {}", feature.getFeature().key());
                           } else {
                              this.featuresMap.put(entry.getPlugin().id() + "-" + feature.getFeature().key(), feature);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean areDependenciesLoaded(String[] depends, boolean requireAll) {
      if (depends != null && depends.length != 0) {
         int i = 0;

         for (int m = depends.length; i < m; i++) {
            if (ModList.get().isLoaded(depends[i]) != requireAll) {
               return !requireAll;
            }
         }

         return requireAll;
      } else {
         return true;
      }
   }

   public void fireEvent(Consumer<IModule> module) {
      this.modules.values().forEach(module);
   }

   public void preInitFeatures() {
      this.load(this::invokeFeature, IC2Feature.LoadingStage.PRE);
   }

   public void postInitFeatures() {
      this.load(this::invokeFeature, IC2Feature.LoadingStage.POST);
   }

   public void load(Consumer<PluginLoader.Feature> consumer, IC2Feature.LoadingStage stage) {
      for (PluginLoader.Feature feature : this.featuresMap.values()) {
         if (feature.getFeature().loadingStage() == stage) {
            consumer.accept(feature);
         }
      }
   }

   private void invokeFeature(PluginLoader.Feature feature) {
      try {
         Method method = feature.getMethod();
         method.setAccessible(true);
         method.invoke(feature.getInstance());
         IC2.LOGGER.info("Loaded feature: {} [{}]", feature.getFeature().name(), feature.getFeature().loadingStage());
      } catch (Exception var3) {
         IC2.LOGGER.error("Error loading feature: {}", feature.getFeature().key(), var3);
      }
   }

   public IModule getModule(String id) {
      return this.modules.get(id);
   }

   private List<PluginLoader.Module> getClasses() {
      Type type = Type.getType(IC2Plugin.class);
      List<PluginLoader.Module> lists = CollectionUtils.<PluginLoader.Module>createList();

      for (ModFileScanData mod : ModList.get().getAllScanData()) {
         for (AnnotationData data : mod.getAnnotations()) {
            if (data.annotationType().equals(type)) {
               try {
                  Class<?> clz = Class.forName(data.memberName());
                  IC2Plugin plug = clz.getAnnotation(IC2Plugin.class);
                  IC2.LOGGER.info("Loading Plugin: [name={}, version={}]", plug.name(), plug.version());
                  IModule module = (IModule)clz.newInstance();
                  List<PluginLoader.Feature> features = CollectionUtils.<PluginLoader.Feature>createList();

                  for (Method method : clz.getDeclaredMethods()) {
                     IC2Feature feature = method.getAnnotation(IC2Feature.class);
                     if (feature != null) {
                        if (method.getParameterCount() != 0) {
                           IC2.LOGGER.error("Skipping invalid IC2Feature: {}.{}() - methods must not have parameters", clz.getName(), method.getName());
                        } else {
                           features.add(new PluginLoader.Feature(feature, module, method));
                        }
                     }
                  }

                  lists.add(new PluginLoader.Module(plug, module, features));
               } catch (Exception var16) {
               }
            }
         }
      }

      return lists;
   }

   static class Feature {
      private final IC2Feature feature;
      private final Object instance;
      private final Method method;

      public Feature(IC2Feature annotation, Object instance, Method method) {
         this.feature = annotation;
         this.instance = instance;
         this.method = method;
      }

      public IC2Feature getFeature() {
         return this.feature;
      }

      public Object getInstance() {
         return this.instance;
      }

      public Method getMethod() {
         return this.method;
      }
   }

   static class Module {
      IC2Plugin plugin;
      IModule module;
      private final List<PluginLoader.Feature> features;

      public Module(IC2Plugin plugin, IModule module, List<PluginLoader.Feature> features) {
         this.plugin = plugin;
         this.module = module;
         this.features = features;
      }

      public IModule getModule() {
         return this.module;
      }

      public IC2Plugin getPlugin() {
         return this.plugin;
      }

      public List<PluginLoader.Feature> getFeatures() {
         return this.features;
      }
   }
}
