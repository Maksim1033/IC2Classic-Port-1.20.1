package ic2.core.block.machines.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.events.ScrapBoxEvent;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IScrapBoxRegistry;
import ic2.core.IC2;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ScrapBoxOutput implements IRecipeOutput {
   List<ItemStack> allDrops = null;

   public ScrapBoxOutput(JsonObject obj) {
   }

   public ScrapBoxOutput(FriendlyByteBuf buffer) {
   }

   public ScrapBoxOutput() {
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      IScrapBoxRegistry.IDrop drop = IC2.RECIPES.get().scrapBoxes.getRandomDrop(new ItemStack(IC2Items.SCRAPBOX), false);
      if (drop != null) {
         list.add(drop.getDrop().copy());
         MinecraftForge.EVENT_BUS.post(new ScrapBoxEvent(list, new ItemStack(IC2Items.SCRAPBOX)));
      }

      return list;
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      if (this.allDrops == null) {
         this.allDrops = CollectionUtils.<ItemStack>createList();

         for (IScrapBoxRegistry.IDrop drop : IC2.RECIPES.get().scrapBoxes.getAllDrops()) {
            this.allDrops.add(drop.getDrop().copy());
         }
      }

      return this.allDrops;
   }

   @Override
   public CompoundTag getMetadata() {
      return EMPTY_COMPOUND;
   }

   @Override
   public float getExperience() {
      return 0.0F;
   }

   @Override
   public JsonObject serialize() {
      return new JsonObject();
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
   }
}
