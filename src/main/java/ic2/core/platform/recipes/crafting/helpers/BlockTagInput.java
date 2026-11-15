package ic2.core.platform.recipes.crafting.helpers;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class BlockTagInput implements IInput {
   TagKey<Block> tag;

   public BlockTagInput(TagKey<Block> tag) {
      this.tag = tag;
   }

   @Override
   public List<ItemStack> getComponents() {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
      if (tags != null) {
         for (Block block : tags.getTag(this.tag)) {
            ItemStack stack = new ItemStack(block);
            if (!stack.isEmpty()) {
               list.add(stack);
            }
         }
      }

      return list;
   }

   @Override
   public int getInputSize() {
      return 1;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return false;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
   }

   @Override
   public JsonObject serialize() {
      return new JsonObject();
   }
}
