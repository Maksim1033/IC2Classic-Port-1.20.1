package ic2.core.platform.recipes.misc;

import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.api.recipes.registries.IUUMatterRegistry;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.platform.recipes.crafting.helpers.RecipeMods;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.chars.Char2IntMaps;
import it.unimi.dsi.fastutil.chars.Char2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class UUMatterRegistry extends BaseRegistry<IUUMatterRegistry> implements IUUMatterRegistry, IRecipeList, Consumer<IAdvancedCraftingManager> {
   List<IUUMatterRegistry.UUMatterEntry> entries = CollectionUtils.<IUUMatterRegistry.UUMatterEntry>createList();
   Map<ItemStack, IUUMatterRegistry.UUMatterEntry> mappedEntries = CollectionUtils.<ItemStack, IUUMatterRegistry.UUMatterEntry>createMap(
      ItemStackStrategy.INSTANCE
   );

   public UUMatterRegistry(Consumer<IUUMatterRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   protected void reloadInternals() {
      this.entries.clear();
      this.mappedEntries.clear();
   }

   @Override
   protected void onAfterReload() {
      int i = 0;

      for (int m = this.entries.size(); i < m; i++) {
         IUUMatterRegistry.UUMatterEntry entry = this.entries.get(i);
         this.mappedEntries.putIfAbsent(entry.getStack().copy(), entry);
      }
   }

   public void accept(IAdvancedCraftingManager t) {
      this.reload();
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();
      List<IUUMatterRegistry.UUMatterEntry> entry = CollectionUtils.<IUUMatterRegistry.UUMatterEntry>createList();

      for (int i = 0; i < size; i++) {
         ItemStack stack = buffer.readItem();
         int uu = buffer.readInt();
         entry.add(new IUUMatterRegistry.UUMatterEntry(stack, uu));
      }

      this.entries = entry;
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.entries.size());
      int i = 0;

      for (int m = this.entries.size(); i < m; i++) {
         IUUMatterRegistry.UUMatterEntry entry = this.entries.get(i);
         buffer.writeItem(entry.getStack());
         buffer.writeInt(entry.getUUNeeded());
      }
   }

   @Override
   public void registerUUEntry(ItemStack output, int milliUU) {
      if (!output.isEmpty() && milliUU > 0) {
         this.entries.add(new IUUMatterRegistry.UUMatterEntry(output, milliUU));
      }
   }

   @Override
   public void registerUUShape(IUUMatterRegistry.UUMatterBuilder builder) {
      if (!builder.isSpecial()) {
         this.entries.add(builder.createEntry());
      }

      List<Object> obj = new ObjectArrayList();
      if (builder.isHidden()) {
         obj.add(RecipeMods.HIDDEN_RECIPE);
      }

      obj.addAll(ObjectArrayList.wrap(builder.getShape()));
      ObjectIterator var3 = Char2IntMaps.fastIterable(builder.getLetters()).iterator();

      while (var3.hasNext()) {
         Entry entry = (Entry)var3.next();
         obj.add(entry.getCharKey());
         obj.add(new ItemStack(IC2Items.UUMATTER, entry.getIntValue()));
      }

      AdvRecipeRegistry.INSTANCE.addShapedRecipe(builder.getId(), builder.getOutput(), obj.toArray(Object[]::new));
   }

   @Override
   public List<IUUMatterRegistry.UUMatterEntry> getEntries() {
      return this.entries;
   }

   @Override
   public void removeUUEntry(IUUMatterRegistry.UUMatterEntry entry) {
      this.entries.remove(entry);
   }

   public Map<ItemStack, IUUMatterRegistry.UUMatterEntry> getMappedEntries() {
      return this.mappedEntries;
   }
}
