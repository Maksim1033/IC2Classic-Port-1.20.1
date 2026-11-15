package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.api.items.IRepairable;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.IC2;
import ic2.core.platform.recipes.crafting.helpers.RecipeMods;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class RepairRecipe extends RecipeIC2Base {
   IRepairable repair;
   IInput repairMaterial;
   int amount;

   public RepairRecipe(FriendlyByteBuf buffer, ResourceLocation id) {
      super(id, buffer.readItem(), buffer.readBoolean(), buffer.readBoolean(), null);
      this.repair = (IRepairable)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
      this.repairMaterial = IngredientRegistry.INSTANCE.readInput(buffer);
      this.amount = buffer.readInt();
   }

   protected RepairRecipe(ResourceLocation id, boolean hidden, boolean consume, IRepairable input, IInput repairMaterial, int amount) {
      super(id, new ItemStack(input), hidden, consume, null);
      this.repair = input;
      this.repairMaterial = repairMaterial;
      this.amount = amount;
   }

   public boolean matches(CraftingContainer inv, Level worldIn) {
      return !this.getResult(inv).isEmpty();
   }

   public ItemStack assemble(CraftingContainer inv) {
      return this.getResult(inv);
   }

   public ItemStack getResult(CraftingContainer inv) {
      ItemStack found = ItemStack.EMPTY;
      int amount = 0;
      int i = 0;

      for (int m = inv.getContainerSize(); i < m; i++) {
         ItemStack stack = inv.getItem(i);
         if (stack.getItem() == this.repair) {
            if (!found.isEmpty()) {
               return ItemStack.EMPTY;
            }

            found = stack;
         } else if (this.repairMaterial.matches(stack) && stack.getCount() >= this.repairMaterial.getInputSize()) {
            amount++;
         }
      }

      if (!found.isEmpty() && amount > 0) {
         found = found.copy();
         return this.repair.repairDamage(found, amount * this.amount) ? found : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      IntSet slotsToClear = new IntOpenHashSet(inv.getContainerSize());
      int chargeIndex = -1;
      int i = 0;

      for (int m = inv.getContainerSize(); i < m; i++) {
         ItemStack stack = inv.getItem(i);
         if (stack.getItem() == this.repair) {
            if (chargeIndex != -1) {
               return NonNullList.create();
            }

            chargeIndex = i;
         } else if (this.repairMaterial.matches(stack) && stack.getCount() >= this.repairMaterial.getInputSize()) {
            slotsToClear.add(i);
         }
      }

      if (chargeIndex != -1 && !slotsToClear.isEmpty()) {
         NonNullList<ItemStack> array = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
         int ix = 0;

         for (int mx = inv.getContainerSize(); ix < mx; ix++) {
            if (ix != chargeIndex) {
               if (slotsToClear.contains(ix)) {
                  inv.removeItem(ix, this.repairMaterial.getInputSize());
                  array.set(ix, inv.getItem(ix).copy());
                  inv.setItem(ix, ItemStack.EMPTY);
               } else {
                  array.set(ix, inv.getItem(ix).getCraftingRemainingItem());
               }
            }
         }

         return array;
      } else {
         return super.getRemainingItems(inv);
      }
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 2;
   }

   @Override
   public ResourceLocation getMetaSerializer() {
      return new ResourceLocation("ic2", "repair");
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItemStack(this.output, false);
      buffer.writeBoolean(this.hidden);
      buffer.writeBoolean(this.consume);
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.repair.asItem());
      IngredientRegistry.INSTANCE.writeInput(this.repairMaterial, buffer);
      buffer.writeInt(this.amount);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("type", "ic2:crafting_ic2_automated");
      obj.addProperty("sub_type", "ic2:repair");
      obj.addProperty("enabled", true);
      obj.addProperty("repair", ForgeRegistryWrapper.getItemKey(this.repair.asItem()).toString());
      obj.add("material", IngredientRegistry.INSTANCE.serializeInput(this.repairMaterial));
      obj.addProperty("size", this.amount);
      obj.addProperty("hidden", this.hidden);
      obj.addProperty("consume", this.consume);
      return obj;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> result = NonNullList.create();
      result.add(Ingredient.of(new ItemLike[]{this.repair}));

      try {
         result.add(this.repairMaterial.asIngredient());
         return result;
      } catch (Exception var3) {
         IC2.LOGGER.info("Recipe id [{}] has had a crash during Ingredient creation", this.getId(), var3);
         throw var3;
      }
   }

   public static RepairRecipe create(ResourceLocation id, JsonObject object) {
      if (ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(GsonHelper.getAsString(object, "repair"))) instanceof IRepairable repair) {
         IInput input = IngredientRegistry.INSTANCE.readInput(GsonHelper.getAsJsonObject(object, "material"));
         boolean hidden = GsonHelper.getAsBoolean(object, "hidden", false);
         boolean consume = GsonHelper.getAsBoolean(object, "consume", false);
         int amount = GsonHelper.getAsInt(object, "size");
         return new RepairRecipe(id, hidden, consume, repair, input, amount);
      } else {
         IC2.LOGGER.error("{}, is not a IRepairable Item", GsonHelper.getAsString(object, "repair"));
         return null;
      }
   }

   public static RepairRecipe create(ResourceLocation id, IRepairable input, IInput repairMaterial, int effect, Object... mods) {
      boolean hidden = false;
      boolean consume = false;

      for (Object obj : mods) {
         if (obj != null) {
            if (obj instanceof Boolean) {
               hidden = (Boolean)obj;
            } else if (obj instanceof RecipeMods) {
               switch ((RecipeMods)obj) {
                  case CONSUME_CONTAINERS:
                     consume = true;
                     break;
                  case HIDDEN_RECIPE:
                     hidden = true;
               }
            }
         }
      }

      return new RepairRecipe(id, hidden, consume, input, repairMaterial, effect);
   }
}
