package ic2.core.platform.advancements;

import com.google.gson.JsonObject;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.Map;
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

public class IdTrigger implements CriterionTrigger<IdTrigger.Instance> {
   static final ResourceLocation ID = new ResourceLocation("ic2", "id");
   private final Map<PlayerAdvancements, IdTrigger.Listeners> listeners = CollectionUtils.<PlayerAdvancements, IdTrigger.Listeners>createLinkedMap();

   public ResourceLocation getId() {
      return ID;
   }

   public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<IdTrigger.Instance> listener) {
      IdTrigger.Listeners playerListeners = this.listeners.get(playerAdvancementsIn);
      if (playerListeners == null) {
         playerListeners = new IdTrigger.Listeners(playerAdvancementsIn);
         this.listeners.put(playerAdvancementsIn, playerListeners);
      }

      playerListeners.add(listener);
   }

   public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, Listener<IdTrigger.Instance> listener) {
      IdTrigger.Listeners playerListeners = this.listeners.get(playerAdvancementsIn);
      if (playerListeners != null && playerListeners.remove(listener) && playerListeners.isEmpty()) {
         this.listeners.remove(playerAdvancementsIn);
      }
   }

   public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
      this.listeners.remove(playerAdvancementsIn);
   }

   public IdTrigger.Instance createInstance(JsonObject object, DeserializationContext conditions) {
      return new IdTrigger.Instance(new ResourceLocation(object.get("location").getAsString()), EntityPredicate.fromJson(object, "player", conditions));
   }

   public void onTrigger(Player player, ResourceLocation id) {
      if (player instanceof ServerPlayer) {
         IdTrigger.Listeners listener = this.listeners.get(((ServerPlayer)player).getAdvancements());
         if (listener != null) {
            listener.trigger(id);
         }
      }
   }

   public static class Instance extends AbstractCriterionTriggerInstance {
      ResourceLocation location;

      public Instance(ResourceLocation location) {
         super(IdTrigger.ID, ContextAwarePredicate.ANY);
         this.location = location;
      }

      public Instance(ResourceLocation location, ContextAwarePredicate pre) {
         super(IdTrigger.ID, pre);
         this.location = location;
      }

      public boolean test(ResourceLocation id) {
         return this.location.equals(id);
      }

      public JsonObject serializeToJson(SerializationContext conditions) {
         JsonObject object = super.serializeToJson(conditions);
         object.addProperty("location", this.location.toString());
         return object;
      }
   }

   static class Listeners {
      private final PlayerAdvancements playerAdvancements;
      private final Set<Listener<IdTrigger.Instance>> listeners = CollectionUtils.<Listener<IdTrigger.Instance>>createLinkedSet();

      public Listeners(PlayerAdvancements playerAdvancementsIn) {
         this.playerAdvancements = playerAdvancementsIn;
      }

      public boolean isEmpty() {
         return this.listeners.isEmpty();
      }

      public void add(Listener<IdTrigger.Instance> listener) {
         this.listeners.add(listener);
      }

      public boolean remove(Listener<IdTrigger.Instance> listener) {
         return this.listeners.remove(listener);
      }

      public void trigger(ResourceLocation location) {
         List<Listener<IdTrigger.Instance>> list = CollectionUtils.<Listener<IdTrigger.Instance>>createList();

         for (Listener<IdTrigger.Instance> listener : this.listeners) {
            if (((IdTrigger.Instance)listener.getTriggerInstance()).test(location)) {
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
