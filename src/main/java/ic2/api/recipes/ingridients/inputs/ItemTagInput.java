package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class ItemTagInput implements IInput {
   TagKey<Item> tag;
   int size;

   public ItemTagInput(JsonObject obj) {
      this.tag = ItemTags.create(new ResourceLocation(obj.get("tag").getAsString()));
      this.size = obj.get("size").getAsInt();
   }

   public ItemTagInput(FriendlyByteBuf buffer) {
      this.tag = ItemTags.create(buffer.readResourceLocation());
      this.size = buffer.readByte();
   }

   public ItemTagInput(TagKey<Item> tag, int size) {
      this.tag = tag;
      this.size = size;
   }

   public ItemTagInput(TagKey<Item> tag) {
      this(tag, 1);
   }

   @Override
   public List<ItemStack> getComponents() {
      List<ItemStack> list = new ArrayList<>();
      ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
      if (tags != null) {
         for (Item item : tags.getTag(this.tag)) {
            list.add(new ItemStack(item, this.size));
         }
      }

      return Collections.unmodifiableList(list);
   }

   @Override
   public Ingredient asIngredient() {
      return this.size == 1 ? Ingredient.of(this.tag) : IInput.super.asIngredient();
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return stack.is(this.tag);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeResourceLocation(this.tag.location());
      buffer.writeByte(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("tag", this.tag.location().toString());
      obj.addProperty("size", this.size);
      return obj;
   }
}
