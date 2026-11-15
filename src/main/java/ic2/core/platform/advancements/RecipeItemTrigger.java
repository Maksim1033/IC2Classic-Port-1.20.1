package ic2.core.platform.advancements;

import com.google.gson.JsonObject;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTrigger.Listener;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeItemTrigger implements CriterionTrigger<RecipeItemTrigger.Instance> {
   static final ResourceLocation ID = new ResourceLocation("ic2", "recipe_trigger");
   private final Map<PlayerAdvancements, RecipeItemTrigger.Listeners> listeners = CollectionUtils.<PlayerAdvancements, RecipeItemTrigger.Listeners>createLinkedMap();

   public ResourceLocation getId() {
      return ID;
   }

   public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<RecipeItemTrigger.Instance> listener) {
      RecipeItemTrigger.Listeners playerListeners = this.listeners.get(playerAdvancementsIn);
      if (playerListeners == null) {
         playerListeners = new RecipeItemTrigger.Listeners(playerAdvancementsIn);
         this.listeners.put(playerAdvancementsIn, playerListeners);
      }

      playerListeners.add(listener);
   }

   public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<RecipeItemTrigger.Instance> listener) {
      RecipeItemTrigger.Listeners playerListeners = this.listeners.get(playerAdvancementsIn);
      if (playerListeners != null && playerListeners.remove(listener) && playerListeners.isEmpty()) {
         this.listeners.remove(playerAdvancementsIn);
      }
   }

   public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
      this.listeners.remove(playerAdvancementsIn);
   }

   public RecipeItemTrigger.Instance createInstance(JsonObject object, DeserializationContext conditions) {
      return new RecipeItemTrigger.Instance(new ResourceLocation(object.get("recipe_id").getAsString()), EntityPredicate.fromJson(object, "player", conditions));
   }

   public void onTrigger(Player player, ItemStack stack) {
      if (player instanceof ServerPlayer) {
         RecipeItemTrigger.Listeners listener = this.listeners.get(((ServerPlayer)player).getAdvancements());
         if (listener != null) {
            listener.trigger(stack);
         }
      }
   }

   public static class Instance extends AbstractCriterionTriggerInstance {
      ResourceLocation location;
      List<Ingredient> cache = null;

      public Instance(ResourceLocation location) {
         super(RecipeItemTrigger.ID, ContextAwarePredicate.ANY);
         this.location = location;
      }

      public Instance(ResourceLocation location, ContextAwarePredicate pre) {
         super(RecipeItemTrigger.ID, pre);
         this.location = location;
      }

      public boolean test(ItemStack stack) {
         if (this.cache == null) {
            this.cache = CollectionUtils.<Ingredient>createList();
            Optional<? extends Recipe<?>> recipe = IC2.PLATFORM.getRecipes().byKey(this.location);
            if (recipe.isPresent()) {
               this.cache.addAll(recipe.get().getIngredients());
            }
         }

         int i = 0;

         for (int m = this.cache.size(); i < m; i++) {
            if (this.cache.get(i).test(stack)) {
               return true;
            }
         }

         return false;
      }

      public JsonObject serializeToJson(SerializationContext conditions) {
         JsonObject object = super.serializeToJson(conditions);
         object.addProperty("recipe_id", this.location.toString());
         return object;
      }
   }

   static class Listeners {
      private final PlayerAdvancements playerAdvancements;
      private final Set<Listener<RecipeItemTrigger.Instance>> listeners = CollectionUtils.<Listener<RecipeItemTrigger.Instance>>createLinkedSet();

      public Listeners(PlayerAdvancements playerAdvancementsIn) {
         this.playerAdvancements = playerAdvancementsIn;
      }

      public boolean isEmpty() {
         return this.listeners.isEmpty();
      }

      public void add(Listener<RecipeItemTrigger.Instance> listener) {
         this.listeners.add(listener);
      }

      public boolean remove(Listener<RecipeItemTrigger.Instance> listener) {
         return this.listeners.remove(listener);
      }

      public void trigger(ItemStack stack) {
         List<Listener<RecipeItemTrigger.Instance>> list = CollectionUtils.<Listener<RecipeItemTrigger.Instance>>createList();

         for (Listener<RecipeItemTrigger.Instance> listener : this.listeners) {
            if (((RecipeItemTrigger.Instance)listener.getTriggerInstance()).test(stack)) {
               list.add(listener);
            }
         }

         if (!list.isEmpty()) {
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               list.get(i).run(this.playerAdvancements);
            }
         }
      }
   }
}
