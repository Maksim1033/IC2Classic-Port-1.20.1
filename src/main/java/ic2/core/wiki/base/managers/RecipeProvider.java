package ic2.core.wiki.base.managers;

import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.IC2;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.helper.ProgressBar;
import ic2.core.wiki.recipes.providers.IItemReference;
import ic2.core.wiki.recipes.providers.IRecipeProvider;
import ic2.core.wiki.recipes.providers.IRecipeReference;
import ic2.core.wiki.recipes.providers.MachineRecipeProvider;
import ic2.core.wiki.recipes.providers.VanillaRecipeProvider;
import ic2.core.wiki.recipes.renderers.IRecipeRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecipeProvider {
   public static final RecipeProvider INSTANCE = new RecipeProvider();
   List<IRecipeProvider> providers = CollectionUtils.<IRecipeProvider>createList();
   List<RecipeProvider.RecipeReference> references = CollectionUtils.<RecipeProvider.RecipeReference>createList();
   List<RecipeProvider.ItemReference> itemRefs = CollectionUtils.<RecipeProvider.ItemReference>createList();

   @OnlyIn(Dist.CLIENT)
   public void init() {
      MinecraftForge.EVENT_BUS.register(this);
      this.registerProvider(new VanillaRecipeProvider(RecipeType.CRAFTING));
      this.registerProvider(new VanillaRecipeProvider(RecipeType.SMELTING));
      this.registerProvider(new VanillaRecipeProvider(RecipeType.BLASTING));
      this.registerProvider(new VanillaRecipeProvider(RecipeType.SMOKING));
      this.registerProvider(new VanillaRecipeProvider(RecipeType.CAMPFIRE_COOKING));
      this.registerProvider(
         new MachineRecipeProvider(IC2.RECIPES.get(false).macerator, new ItemStack(IC2Blocks.MACERATOR), new ProgressBar(234, 16, 212, 16, 22, 16))
      );
      this.registerProvider(
         new MachineRecipeProvider(IC2.RECIPES.get(false).compressor, new ItemStack(IC2Blocks.COMPRESSOR), new ProgressBar(234, 32, 212, 32, 22, 16))
      );
      this.registerProvider(
         new MachineRecipeProvider(IC2.RECIPES.get(false).extractor, new ItemStack(IC2Blocks.EXTRACTOR), new ProgressBar(234, 64, 212, 64, 22, 16))
      );
      this.registerProvider(
         new MachineRecipeProvider(IC2.RECIPES.get(false).recycler, new ItemStack(IC2Blocks.RECYCLER), new ProgressBar(234, 48, 212, 48, 22, 16))
      );
      this.registerProvider(
         new MachineRecipeProvider(IC2.RECIPES.get(false).sawmill, new ItemStack(IC2Blocks.SAWMILL), new ProgressBar(234, 80, 212, 80, 22, 16))
      );
   }

   public void clearReferences() {
      this.references.clear();
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onClientReloaded(RecipesUpdatedEvent event) {
      this.onReloaded();
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onClientReloaded(TagsUpdatedEvent event) {
      this.reloadItemRefs();
   }

   public void onReloaded() {
      int i = 0;

      for (int m = this.providers.size(); i < m; i++) {
         this.providers.get(i).reloadRecipes();
      }

      this.reloadReferences();
   }

   public void reloadReferences() {
      int i = 0;

      for (int m = this.references.size(); i < m; i++) {
         this.references.get(i).reload();
      }
   }

   public void reloadItemRefs() {
      int i = 0;

      for (int m = this.itemRefs.size(); i < m; i++) {
         this.itemRefs.get(i).reload();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void registerProvider(IRecipeProvider provider) {
      this.providers.add(provider);
   }

   public List<IRecipeRenderer> addRecipesFromInputs(ItemStack stack, boolean ignoreHidden) {
      List<IRecipeRenderer> renderers = CollectionUtils.<IRecipeRenderer>createList();

      for (IRecipeProvider prov : this.providers) {
         renderers.addAll(prov.getFromInput(stack, ignoreHidden));
      }

      return renderers;
   }

   public List<IRecipeRenderer> addRecipesFromOutputs(ItemStack stack, boolean ignoreHidden) {
      List<IRecipeRenderer> renderers = CollectionUtils.<IRecipeRenderer>createList();

      for (IRecipeProvider prov : this.providers) {
         renderers.addAll(prov.getFromOutput(stack, ignoreHidden));
      }

      return renderers;
   }

   public List<IRecipeRenderer> getRecipesForInput(List<ItemStack> items, boolean ignoreHidden) {
      Set<IRecipeRenderer> renders = CollectionUtils.<IRecipeRenderer>createLinkedSet();

      for (ItemStack stack : items) {
         renders.addAll(this.addRecipesFromInputs(stack, ignoreHidden));
      }

      return new ObjectArrayList(renders);
   }

   public List<IRecipeRenderer> getRecipesForOutput(List<ItemStack> items, boolean ignoreHidden) {
      Set<IRecipeRenderer> renders = CollectionUtils.<IRecipeRenderer>createLinkedSet();

      for (ItemStack stack : items) {
         renders.addAll(this.addRecipesFromOutputs(stack, ignoreHidden));
      }

      return new ObjectArrayList(renders);
   }

   public IRecipeReference getRecipe(List<ItemStack> items, boolean ignoreHidden) {
      RecipeProvider.RecipeReference reference = new RecipeProvider.RecipeReference(items, ignoreHidden);
      this.references.add(reference);
      return reference;
   }

   public IItemReference getItems(List<IInput> inputs) {
      RecipeProvider.ItemReference reference = new RecipeProvider.ItemReference(inputs);
      this.itemRefs.add(reference);
      return reference;
   }

   public static class ItemReference implements IItemReference {
      List<ItemStack> items = CollectionUtils.<ItemStack>createList();
      List<IInput> inputs;

      public ItemReference(List<IInput> inputs) {
         this.inputs = inputs;
      }

      public void reload() {
         this.items.clear();

         for (IInput input : this.inputs) {
            this.items.addAll(input.getComponents());
         }
      }

      @Override
      public List<ItemStack> getItemList() {
         return this.items;
      }
   }

   public static class RecipeReference implements IRecipeReference {
      List<ItemStack> items;
      boolean ignoreHidden;
      List<IRecipeRenderer> inputs = CollectionUtils.<IRecipeRenderer>createList();
      List<IRecipeRenderer> outputs = CollectionUtils.<IRecipeRenderer>createList();
      Runnable listener;

      public RecipeReference(List<ItemStack> items, boolean ignoreHidden) {
         this.items = items;
         this.ignoreHidden = ignoreHidden;
      }

      public void reload() {
         this.inputs = RecipeProvider.INSTANCE.getRecipesForInput(this.items, this.ignoreHidden);
         this.outputs = RecipeProvider.INSTANCE.getRecipesForOutput(this.items, this.ignoreHidden);
         if (this.listener != null) {
            this.listener.run();
         }
      }

      @Override
      public void addSearchInfo(Consumer<Component> search) {
         for (ItemStack stack : this.items) {
            search.accept(stack.getDisplayName());
         }
      }

      @Override
      public void setListener(Runnable listener) {
         this.listener = listener;
      }

      @Override
      public List<IRecipeRenderer> getInputs() {
         return this.inputs;
      }

      @Override
      public List<IRecipeRenderer> getOutputs() {
         return this.outputs;
      }
   }
}
