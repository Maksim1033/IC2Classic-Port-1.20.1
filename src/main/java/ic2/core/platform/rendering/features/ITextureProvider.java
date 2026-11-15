package ic2.core.platform.rendering.features;

import ic2.core.IC2;
import ic2.core.platform.rendering.features.providers.NoStateProvider;
import ic2.core.platform.rendering.features.providers.SimpleToggleProvider;
import ic2.core.platform.rendering.features.providers.SingleProvider;
import ic2.core.platform.rendering.features.providers.StateProvider;
import ic2.core.platform.rendering.features.providers.ToggleProvider;
import ic2.core.platform.rendering.features.providers.TriDirectionProvider;
import ic2.core.platform.rendering.features.providers.TriToggleProvider;
import ic2.core.platform.rendering.features.providers.VanillaProvider;
import java.util.function.IntSupplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITextureProvider {
   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getTexture(BlockState var1, Direction var2);

   static ITextureProvider vanilla(String mod, String name) {
      return vanilla(new ResourceLocation(name, name));
   }

   static ITextureProvider vanilla(ResourceLocation location) {
      return new VanillaProvider(location);
   }

   static ITextureProvider multiState(IntSupplier provider, ITextureProvider... providers) {
      return new StateProvider(provider, providers);
   }

   static ITextureProvider toggleIC2(String path) {
      return new ToggleProvider(IC2.MOD_ID, path);
   }

   static ITextureProvider noStateIC2(String path) {
      return new NoStateProvider(IC2.MOD_ID, path);
   }

   static ITextureProvider triToggleIC2(String path) {
      return new TriToggleProvider(IC2.MOD_ID, path);
   }

   static ITextureProvider simpleToggleIC2(String path, String on, String off) {
      return new SimpleToggleProvider(IC2.MOD_ID, path, on, off);
   }

   static ITextureProvider triDirIC2(String path) {
      return new TriDirectionProvider(IC2.MOD_ID, path);
   }

   static ITextureProvider singleIC2(String path, String name) {
      return new SingleProvider(IC2.MOD_ID, path, name);
   }

   static ITextureProvider toggle(String mod, String path) {
      return new ToggleProvider(mod, path);
   }

   static ITextureProvider noState(String mod, String path) {
      return new NoStateProvider(mod, path);
   }

   static ITextureProvider triToggle(String mod, String path) {
      return new TriToggleProvider(mod, path);
   }

   static ITextureProvider simpleToggle(String mod, String path, String on, String off) {
      return new SimpleToggleProvider(mod, path, on, off);
   }

   static ITextureProvider triDir(String mod, String path) {
      return new TriDirectionProvider(mod, path);
   }

   static ITextureProvider single(String mod, String path, String name) {
      return new SingleProvider(mod, path, name);
   }
}
