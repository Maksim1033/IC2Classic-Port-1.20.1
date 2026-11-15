package ic2.core.platform.recipes.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.registries.IScrapBoxRegistry;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class ScrapboxRegistry extends BaseRegistry<IScrapBoxRegistry> implements IScrapBoxRegistry, IRecipeList {
   Random rand = new Random();
   List<ScrapboxRegistry.Drop> drops = CollectionUtils.<ScrapboxRegistry.Drop>createList();
   float maxChance = 0.0F;

   public ScrapboxRegistry(Consumer<IScrapBoxRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   protected void reloadInternals() {
      this.drops.clear();
      this.maxChance = 0.0F;
   }

   @Override
   protected void onAfterReload() {
      this.reloadPool();
   }

   @Override
   public void onJsonRecipeListLoaded() {
      this.reloadPool();
   }

   protected void reloadPool() {
      this.maxChance = 0.0F;
      int i = 0;

      for (int m = this.drops.size(); i < m; i++) {
         this.maxChance = this.maxChance + this.drops.get(i).getChance();
      }

      i = 0;

      for (int m = this.drops.size(); i < m; i++) {
         ScrapboxRegistry.Drop drop = this.drops.get(i);
         drop.setPoolChance(drop.getChance() / this.maxChance);
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeFloat(this.maxChance);
      buffer.writeVarInt(this.drops.size());
      int i = 0;

      for (int m = this.drops.size(); i < m; i++) {
         this.drops.get(i).write(buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      this.maxChance = buffer.readFloat();
      int size = buffer.readVarInt();
      List<ScrapboxRegistry.Drop> dropSync = CollectionUtils.<ScrapboxRegistry.Drop>createList();

      for (int i = 0; i < size; i++) {
         dropSync.add(new ScrapboxRegistry.Drop(buffer));
      }

      this.drops = dropSync;
   }

   @Override
   public String getFolder() {
      return "scrap_drops";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> data = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (ScrapboxRegistry.Drop drop : this.drops) {
         JsonObject obj = new JsonObject();
         obj.add("drop", this.writeItem(drop.getDrop()));
         obj.addProperty("chance", drop.getChance());
         data.put(new ResourceLocation("ic2", ForgeRegistryWrapper.getItemKey(drop.getDrop().getItem()).getPath()), obj);
      }

      return data;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      JsonElement element = data.get("drops");
      if (element != null) {
         IRecipeList.iterateObject(element, this::readRecipe);
      } else {
         this.readRecipe(data);
      }
   }

   private void readRecipe(JsonObject obj) {
      ItemStack stack = this.readItem(GsonHelper.getAsJsonObject(obj, "drop"));
      if (stack.isEmpty()) {
         throw new RuntimeException("Drop is Empty");
      } else {
         if (GsonHelper.getAsBoolean(obj, "remove", false)) {
            this.removeDrops(stack);
         } else {
            this.addDrop(stack, GsonHelper.getAsFloat(obj, "chance"));
         }
      }
   }

   private JsonObject writeItem(ItemStack stack) {
      return IngredientRegistry.INSTANCE
         .serializeOutputGenerator((IOutputGenerator)(stack.hasTag() ? new ItemWithNBTGenerator(stack) : new ItemGenerator(stack.getItem(), stack.getCount())));
   }

   private ItemStack readItem(JsonObject obj) {
      ItemStack[] stack = new ItemStack[]{ItemStack.EMPTY};
      new ItemWithNBTGenerator(obj).addItems(T -> stack[0] = T);
      return stack[0];
   }

   @Override
   public void addDrop(ItemStack stack, float chance) {
      if (!stack.isEmpty() && !(chance < 0.0F)) {
         this.drops.add(new ScrapboxRegistry.Drop(stack, chance));
      }
   }

   @Override
   public IScrapBoxRegistry.IDrop getRandomDrop(ItemStack source, boolean consumeItem) {
      if (source.isEmpty()) {
         return null;
      } else {
         if (consumeItem) {
            source.shrink(1);
         }

         float currentChance = this.rand.nextFloat() * this.maxChance;
         int i = 0;

         for (int m = this.drops.size(); i < m; i++) {
            IScrapBoxRegistry.IDrop drop = this.drops.get(i);
            if (!(currentChance > drop.getChance())) {
               return drop;
            }

            currentChance -= drop.getChance();
         }

         return null;
      }
   }

   @Override
   public void removeDrops(ItemStack stack) {
      int i = 0;

      for (int m = this.drops.size(); i < m; i++) {
         if (StackUtil.isStackEqual(this.drops.get(i).getDrop(), stack)) {
            this.drops.remove(i--);
            m--;
         }
      }
   }

   @Override
   public void removeDrop(IScrapBoxRegistry.IDrop drop) {
      this.drops.remove(drop);
   }

   @Override
   public List<IScrapBoxRegistry.IDrop> getAllDrops() {
      return new ObjectArrayList(this.drops);
   }

   public static class Drop implements IScrapBoxRegistry.IDrop {
      ItemStack stack;
      float chance;
      float poolChance;

      public Drop(FriendlyByteBuf buffer) {
         this.stack = buffer.readItem();
         this.chance = buffer.readFloat();
         this.poolChance = buffer.readFloat();
      }

      public Drop(ItemStack stack, float chance) {
         this.stack = stack.copy();
         this.chance = chance;
      }

      public void write(FriendlyByteBuf buffer) {
         buffer.writeItem(this.stack);
         buffer.writeFloat(this.chance);
         buffer.writeFloat(this.poolChance);
      }

      public void setPoolChance(float poolChance) {
         this.poolChance = poolChance;
      }

      @Override
      public ItemStack getDrop() {
         return this.stack;
      }

      @Override
      public float getChance() {
         return this.chance;
      }

      @Override
      public float getPoolChance() {
         return this.poolChance;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.stack, this.chance);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof IScrapBoxRegistry.IDrop drop) ? false : StackUtil.isStackEqual(this.stack, drop.getDrop()) && this.chance == drop.getChance();
      }
   }
}
