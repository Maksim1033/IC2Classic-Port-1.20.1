package ic2.api.recipes.registries;

import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IUUMatterRegistry extends IListenableRegistry<IUUMatterRegistry> {
   void registerUUShape(IUUMatterRegistry.UUMatterBuilder var1);

   void registerUUEntry(ItemStack var1, int var2);

   List<IUUMatterRegistry.UUMatterEntry> getEntries();

   void removeUUEntry(IUUMatterRegistry.UUMatterEntry var1);

   public static class UUMatterBuilder {
      ItemStack output;
      ResourceLocation id;
      String[] shape;
      boolean special = false;
      boolean hidden = true;
      Char2IntMap letters = new Char2IntOpenHashMap();

      public UUMatterBuilder(String name) {
         this("ic2", name);
      }

      public UUMatterBuilder(String mod, String name) {
         this(new ResourceLocation(mod, name));
      }

      public UUMatterBuilder(ResourceLocation id) {
         this.id = id;
         this.letters.put('M', 1);
      }

      public IUUMatterRegistry.UUMatterBuilder setSpecial() {
         this.special = true;
         return this;
      }

      public IUUMatterRegistry.UUMatterBuilder setVisible() {
         this.hidden = false;
         return this;
      }

      public IUUMatterRegistry.UUMatterBuilder addLetters(char letter, int uuMatterItems) {
         this.letters.put(letter, uuMatterItems);
         return this;
      }

      public IUUMatterRegistry.UUMatterBuilder setOutput(ItemLike provider, int amount) {
         this.output = new ItemStack(provider, amount);
         return this;
      }

      public IUUMatterRegistry.UUMatterBuilder setOutput(ItemStack output) {
         this.output = output.copy();
         return this;
      }

      public IUUMatterRegistry.UUMatterBuilder setShape(String... shapes) {
         int length = shapes[0].length();

         for (int i = 1; i < shapes.length; i++) {
            if (shapes[i].length() != length) {
               throw new IllegalStateException("Each row needs to be the same length");
            }
         }

         this.shape = shapes;
         return this;
      }

      public IUUMatterRegistry.UUMatterEntry createEntry() {
         int totalUUMatter = 0;

         for (String s : this.shape) {
            int j = 0;

            for (int m = s.length(); j < m; j++) {
               totalUUMatter += this.letters.get(s.charAt(j)) * 1000;
            }
         }

         totalUUMatter /= this.output.getCount();
         return new IUUMatterRegistry.UUMatterEntry(this.output, totalUUMatter);
      }

      public ItemStack getOutput() {
         return this.output.copy();
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public String[] getShape() {
         return this.shape;
      }

      public boolean isSpecial() {
         return this.special;
      }

      public boolean isHidden() {
         return this.hidden;
      }

      public Char2IntMap getLetters() {
         return this.letters;
      }
   }

   public static class UUMatterEntry {
      final ItemStack stack;
      final int uuNeeded;

      public UUMatterEntry(ItemStack stack, int uuNeeded) {
         this.stack = stack.copy();
         this.stack.setCount(1);
         this.uuNeeded = uuNeeded;
      }

      public ItemStack getStack() {
         return this.stack.copy();
      }

      public int getUUNeeded() {
         return this.uuNeeded;
      }
   }
}
