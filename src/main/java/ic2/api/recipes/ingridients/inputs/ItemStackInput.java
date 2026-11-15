package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ItemStackInput implements IInput {
   ItemStack input;
   int size;

   public ItemStackInput(JsonObject obj) {
      this.input = CraftingHelper.getItemStack(obj, true);
      this.size = obj.get("size").getAsInt();
   }

   public ItemStackInput(FriendlyByteBuf buffer) {
      this.input = buffer.readItem();
      this.size = buffer.readByte();
   }

   public ItemStackInput(ItemStack input, int size) {
      this.input = input.copy();
      this.size = size;
   }

   public ItemStackInput(ItemStack input) {
      this(input, input.getCount());
   }

   @Override
   public List<ItemStack> getComponents() {
      return ObjectLists.singleton(IInput.copyWithSize(this.input, this.size));
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return IInput.isStackEqual(this.input, stack);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItem(this.input);
      buffer.writeByte(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = IInput.writeItemStack(this.input, false);
      obj.addProperty("size", this.size);
      return obj;
   }
}
