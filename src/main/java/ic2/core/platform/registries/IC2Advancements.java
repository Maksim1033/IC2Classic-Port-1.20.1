package ic2.core.platform.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.core.platform.advancements.IdTrigger;
import ic2.core.platform.advancements.RecipeItemTrigger;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SimpleRegistry;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class IC2Advancements {
   static final int SHOW_TOAST = 1;
   static final int ANNOUNCE = 2;
   static final int HIDDEN = 4;
   static final int DEFAULT_TOAST = 3;
   public static final SimpleRegistry<Builder> ADVANCEMENTS = CollectionUtils.createRegistry();
   public static final IdTrigger ID_TRIGGER = (IdTrigger)CriteriaTriggers.register(new IdTrigger());
   public static final RecipeItemTrigger RECIPE_ITEM_TRIGGER = (RecipeItemTrigger)CriteriaTriggers.register(new RecipeItemTrigger());
   static final List<Consumer<SimpleRegistry<Builder>>> LISTENERS = ObjectLists.synchronize(CollectionUtils.createList());

   public static void registerIC2Advancement(String name, Builder builder) {
      registerAdvancement(new ResourceLocation("ic2", name), builder);
   }

   public static void registerAdvancement(String owner, String name, Builder builder) {
      registerAdvancement(new ResourceLocation(owner, name), builder);
   }

   public static void registerAdvancement(ResourceLocation location, Builder builder) {
      ADVANCEMENTS.register(location, builder);
   }

   private static void loadInternal() {
      registerIC2Advancement(
         "root",
         builder()
            .display(task(IC2Items.MINING_LASER, 0, "root", new ResourceLocation("ic2", "textures/gui_sprites/misc/advancement.png")))
            .addCriterion("hasCrafting", TriggerInstance.hasItems(new ItemLike[]{Items.CRAFTING_TABLE}))
      );
      registerIC2Advancement(
         "acquire_resin",
         builder()
            .display(task(IC2Items.STICKY_RESIN, 3, "acquire_resin"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.STICKY_RESIN}))
            .parent(new ResourceLocation("ic2:root"))
      );
      registerIC2Advancement(
         "mine_ore",
         builder()
            .display(task(IC2Blocks.URANIUM_ORE, 3, "mine_ore"))
            .addCriterion(
               "hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.ORE_URANIUM_DROP, Items.RAW_COPPER, IC2Items.RAW_TIN, IC2Items.RAW_SILVER})
            )
            .parent(new ResourceLocation("ic2:root"))
      );
      registerIC2Advancement(
         "acquire_refined_iron",
         builder()
            .display(task(IC2Items.INGOT_REFINED_IRON, 3, "acquire_refined_iron"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.INGOT_REFINED_IRON}))
            .parent(new ResourceLocation("ic2:root"))
      );
      registerIC2Advancement(
         "build_cable",
         builder()
            .display(task(IC2Items.COPPER_CABLE_1X_INSULATED, 3, "build_cable"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.COPPER_CABLE_1X_INSULATED}))
            .parent(new ResourceLocation("ic2:acquire_refined_iron"))
      );
      registerIC2Advancement(
         "build_reader",
         builder()
            .display(task(IC2Items.EU_READER, 3, "build_reader"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.EU_READER}))
            .parent(new ResourceLocation("ic2:build_cable"))
      );
      registerIC2Advancement(
         "build_generator",
         builder()
            .display(task(IC2Blocks.GENERATOR, 3, "build_generator"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.GENERATOR}))
            .parent(new ResourceLocation("ic2:build_cable"))
      );
      registerIC2Advancement(
         "build_macerator",
         builder()
            .display(task(IC2Blocks.MACERATOR, 3, "build_macerator"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.MACERATOR}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_coal_diamond",
         builder()
            .display(task(IC2Items.INDUSTRIAL_DIAMOND, 3, "build_coal_diamond"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.INDUSTRIAL_DIAMOND}))
            .parent(new ResourceLocation("ic2:build_macerator"))
      );
      registerIC2Advancement(
         "build_rotary_macerator",
         builder()
            .display(task(IC2Blocks.ROTARY_MACERATOR, 3, "build_rotary_macerator"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.ROTARY_MACERATOR}))
            .parent(new ResourceLocation("ic2:build_macerator"))
      );
      registerIC2Advancement(
         "build_canner",
         builder()
            .display(task(IC2Blocks.CANNER, 3, "build_canner"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.CANNER}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_vacuum_canner",
         builder()
            .display(task(IC2Blocks.VACUUM_CANNER, 3, "build_vacuum_canner"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.VACUUM_CANNER}))
            .parent(new ResourceLocation("ic2:build_canner"))
      );
      registerIC2Advancement(
         "build_electric_furnace",
         builder()
            .display(task(IC2Blocks.ELECTRIC_FURNACE, 3, "build_electric_furnace"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.ELECTRIC_FURNACE}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_induction_furnace",
         builder()
            .display(task(IC2Blocks.INDUCTION_FURNACE, 3, "build_induction_furnace"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.INDUCTION_FURNACE}))
            .parent(new ResourceLocation("ic2:build_electric_furnace"))
      );
      registerIC2Advancement(
         "build_compressor",
         builder()
            .display(task(IC2Blocks.COMPRESSOR, 3, "build_compressor"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.COMPRESSOR}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_singularity_compressor",
         builder()
            .display(task(IC2Blocks.SINGULARITY_COMPRESSOR, 3, "build_singularity_compressor"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.SINGULARITY_COMPRESSOR}))
            .parent(new ResourceLocation("ic2:build_compressor"))
      );
      registerIC2Advancement(
         "compress_uranium",
         builder()
            .display(task(IC2Items.INGOT_URANIUM, 3, "compress_uranium"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.INGOT_URANIUM}))
            .parent(new ResourceLocation("ic2:build_compressor"))
      );
      registerIC2Advancement(
         "die_from_own_nuke",
         builder()
            .display(challenge(IC2Blocks.NUKE, 3, "die_from_own_nuke"))
            .addCriterion("death_trigger", new IdTrigger.Instance(new ResourceLocation("ic2:nuke_death")))
            .parent(new ResourceLocation("ic2:compress_uranium"))
      );
      registerIC2Advancement(
         "build_recycler",
         builder()
            .display(task(IC2Blocks.RECYCLER, 3, "build_recycler"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.RECYCLER}))
            .parent(new ResourceLocation("ic2:build_compressor"))
      );
      registerIC2Advancement(
         "build_compacting_recycler",
         builder()
            .display(task(IC2Blocks.COMPACTING_RECYCLER, 3, "build_compacting_recycler"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.COMPACTING_RECYCLER}))
            .parent(new ResourceLocation("ic2:build_recycler"))
      );
      registerIC2Advancement(
         "build_extractor",
         builder()
            .display(task(IC2Blocks.EXTRACTOR, 3, "build_extractor"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.EXTRACTOR}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_centrifugal_extractor",
         builder()
            .display(task(IC2Blocks.CENTRIFUGAL_EXTRACTOR, 3, "build_centrifugal_extractor"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.CENTRIFUGAL_EXTRACTOR}))
            .parent(new ResourceLocation("ic2:build_extractor"))
      );
      registerIC2Advancement(
         "build_batbox",
         builder()
            .display(task(IC2Blocks.BATBOX, 3, "build_batbox"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.BATBOX}))
            .parent(new ResourceLocation("ic2:build_generator"))
      );
      registerIC2Advancement(
         "build_mfe",
         builder()
            .display(task(IC2Blocks.MFE, 3, "build_mfe"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.MFE}))
            .parent(new ResourceLocation("ic2:build_batbox"))
      );
      registerIC2Advancement(
         "build_mfsu",
         builder()
            .display(task(IC2Blocks.MFSU, 3, "build_mfsu"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.MFSU}))
            .parent(new ResourceLocation("ic2:build_mfe"))
      );
      registerIC2Advancement(
         "build_pesu",
         builder()
            .display(task(IC2Blocks.PESU, 3, "build_pesu"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.PESU}))
            .parent(new ResourceLocation("ic2:build_mfsu"))
      );
      registerIC2Advancement(
         "build_drill",
         builder()
            .display(task(IC2Items.DRILL, 3, "build_drill"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.DRILL}))
            .parent(new ResourceLocation("ic2:build_batbox"))
      );
      registerIC2Advancement(
         "build_chainsaw",
         builder()
            .display(task(IC2Items.CHAINSAW, 3, "build_chainsaw"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.CHAINSAW}))
            .parent(new ResourceLocation("ic2:build_batbox"))
      );
      registerIC2Advancement(
         "build_massfab",
         builder()
            .display(task(IC2Blocks.MASS_FABRICATOR, 3, "build_massfab"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.MASS_FABRICATOR}))
            .parent(new ResourceLocation("ic2:build_batbox"))
      );
      registerIC2Advancement(
         "build_diamond_drill",
         builder()
            .display(task(IC2Items.DRILL_DIAMOND, 3, "build_diamond_drill"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.DRILL_DIAMOND}))
            .parent(new ResourceLocation("ic2:build_drill"))
      );
      registerIC2Advancement(
         "kill_creeper_chainsaw",
         builder()
            .display(challenge(IC2Items.CHAINSAW, 3, "kill_creeper_chainsaw"))
            .addCriterion("kill_creeper", new IdTrigger.Instance(new ResourceLocation("ic2:creeper_kill")))
            .parent(new ResourceLocation("ic2:build_chainsaw"))
      );
      registerIC2Advancement(
         "acquire_matter",
         builder()
            .display(task(IC2Items.UUMATTER, 3, "acquire_matter"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.UUMATTER}))
            .parent(new ResourceLocation("ic2:build_massfab"))
      );
      registerIC2Advancement(
         "build_quantum_armor",
         builder()
            .display(task(IC2Items.QUANTUM_SUIT_CHESTPLATE, 3, "build_quantum_armor"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.QUANTUM_SUIT_CHESTPLATE}))
            .parent(new ResourceLocation("ic2:acquire_matter"))
      );
      registerIC2Advancement(
         "starve_with_quantum_helmet",
         builder()
            .display(challenge(IC2Items.QUANTUM_SUIT_HELMET, 3, "starve_with_quantum_helmet"))
            .addCriterion("starve", new IdTrigger.Instance(new ResourceLocation("ic2:starve")))
            .parent(new ResourceLocation("ic2:build_quantum_armor"))
      );
      registerIC2Advancement(
         "build_teleporter",
         builder()
            .display(task(IC2Blocks.TELEPORTER, 3, "build_teleporter"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.TELEPORTER}))
            .parent(new ResourceLocation("ic2:build_mfsu"))
      );
      registerIC2Advancement(
         "teleport_far_away",
         builder()
            .display(challenge(IC2Blocks.TELEPORTER, 3, "teleport_far_away"))
            .addCriterion("to_far", new IdTrigger.Instance(new ResourceLocation("ic2:teleport_far")))
            .parent(new ResourceLocation("ic2:build_teleporter"))
      );
      registerIC2Advancement(
         "build_mining_laser",
         builder()
            .display(task(IC2Items.MINING_LASER, 3, "build_mining_laser"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Items.MINING_LASER}))
            .parent(new ResourceLocation("ic2:build_mfe"))
      );
      registerIC2Advancement(
         "kill_dragon_mining_laser",
         builder()
            .display(challenge(Items.DRAGON_HEAD, 3, "kill_dragon_mining_laser"))
            .addCriterion("target", new IdTrigger.Instance(IC2Stats.DRAGONS_SHOT))
            .parent(new ResourceLocation("ic2:build_mining_laser"))
      );
      registerIC2Advancement(
         "build_terraformer",
         builder()
            .display(task(IC2Blocks.TERRAFORMER, 3, "build_terraformer"))
            .addCriterion("hasItem", TriggerInstance.hasItems(new ItemLike[]{IC2Blocks.TERRAFORMER}))
            .parent(new ResourceLocation("ic2:build_mfsu"))
      );
      registerIC2Advancement(
         "terraform_end_cultivation",
         builder()
            .display(challenge(IC2Items.TFBP_CULTIVATION, 3, "terraform_end_cultivation"))
            .addCriterion("task", new IdTrigger.Instance(new ResourceLocation("ic2:cultivate_end")))
            .parent(new ResourceLocation("ic2:build_terraformer"))
      );
      registerIC2Advancement(
         "jetpack_kill",
         builder()
            .display(challenge(IC2Items.JETPACK_ELECTRIC, 7, "jetpack_kill"))
            .addCriterion("task", new IdTrigger.Instance(new ResourceLocation("ic2:jetpack_kill")))
            .parent(new ResourceLocation("ic2:root"))
      );
      registerIC2Advancement(
         "kill_wither_with_nuke",
         builder()
            .display(challenge(Blocks.WITHER_SKELETON_SKULL, 3, "kill_wither_with_nuke"))
            .addCriterion("death_trigger", new IdTrigger.Instance(new ResourceLocation("ic2:nuke_wither")))
            .parent(new ResourceLocation("ic2:compress_uranium"))
      );
      registerIC2Advancement(
         "scaffolded",
         builder()
            .display(task(IC2Blocks.SCAFFOLD_WOOD, 7, "scaffolded"))
            .addCriterion("entry", new IdTrigger.Instance(new ResourceLocation("ic2:scaffold")))
            .parent(new ResourceLocation("ic2:root"))
      );
   }

   static Builder builder() {
      return Builder.advancement().rewards(new net.minecraft.advancements.AdvancementRewards.Builder().build());
   }

   static DisplayInfo task(ItemLike provider, int flags, String name) {
      return new DisplayInfo(
         new ItemStack(provider),
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.TASK,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo task(ItemLike provider, int flags, String name, ResourceLocation background) {
      return new DisplayInfo(
         new ItemStack(provider),
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         background,
         FrameType.TASK,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo task(ItemStack stack, int flags, String name) {
      return new DisplayInfo(
         stack,
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.TASK,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo task(ItemStack stack, int flags, String name, Component desc) {
      return new DisplayInfo(
         stack, Component.translatable("advancement.ic2." + name), desc, null, FrameType.TASK, (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0
      );
   }

   static DisplayInfo challenge(ItemLike provider, int flags, String name) {
      return new DisplayInfo(
         new ItemStack(provider),
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.CHALLENGE,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo challenge(ItemStack stack, int flags, String name) {
      return new DisplayInfo(
         stack,
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.CHALLENGE,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo challenge(ItemStack stack, int flags, String name, Component desc) {
      return new DisplayInfo(
         stack, Component.translatable("advancement.ic2." + name), desc, null, FrameType.CHALLENGE, (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0
      );
   }

   static DisplayInfo goal(ItemLike provider, int flags, String name) {
      return new DisplayInfo(
         new ItemStack(provider),
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.GOAL,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo goal(ItemStack stack, int flags, String name) {
      return new DisplayInfo(
         stack,
         Component.translatable("advancement.ic2." + name),
         Component.translatable("advancement.ic2." + name + ".desc"),
         null,
         FrameType.GOAL,
         (flags & 1) != 0,
         (flags & 2) != 0,
         (flags & 4) != 0
      );
   }

   static DisplayInfo goal(ItemStack stack, int flags, String name, Component desc) {
      return new DisplayInfo(
         stack, Component.translatable("advancement.ic2." + name), desc, null, FrameType.GOAL, (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0
      );
   }

   private static void loadRecipes() {
      for (Recipe<?> recipe : AdvRecipeRegistry.INSTANCE.getRecipes()) {
         if (recipe != null) {
            ResourceLocation location = recipe.getId();
            Builder builder = Builder.advancement();
            builder.parent(new ResourceLocation("minecraft:recipes/root"));
            builder.rewards(new net.minecraft.advancements.AdvancementRewards.Builder().addRecipe(location));
            builder.addCriterion("hasItem", new RecipeItemTrigger.Instance(location));
            registerAdvancement(location.getNamespace(), location.getPath() + "_recipe_advancement", builder);
         }
      }
   }

   public static void reload() {
      ADVANCEMENTS.clear();
      loadInternal();
      loadRecipes();
      int i = 0;

      for (int m = LISTENERS.size(); i < m; i++) {
         LISTENERS.get(i).accept(ADVANCEMENTS);
      }
   }

   public static void registerListener(Consumer<SimpleRegistry<Builder>> listener) {
      LISTENERS.add(listener);
   }

   static JsonObject createCriteria(Item item) {
      JsonObject hasItem = new JsonObject();
      hasItem.addProperty("trigger", "minecraft:inventory_changed");
      JsonObject itemObject = new JsonObject();
      JsonArray array = new JsonArray();
      array.add(ForgeRegistryWrapper.getItemKey(item).toString());
      itemObject.add("items", array);
      hasItem.add("conditions", itemObject);
      return hasItem;
   }

   static void handle(JsonElement element, Consumer<String> result) {
      iterate(element, T -> convert(T, result));
   }

   static void convert(JsonObject obj, Consumer<String> result) {
      if (obj.has("tag")) {
         result.accept(obj.get("tag").getAsString());
      } else if (obj.has("item")) {
         result.accept(obj.get("item").getAsString());
      } else if (obj.has("items")) {
         for (JsonElement el : obj.getAsJsonArray("items")) {
            convert(el.getAsJsonObject(), result);
         }
      }
   }

   static void iterate(JsonElement element, Consumer<JsonObject> receiver) {
      if (element.isJsonObject()) {
         receiver.accept(element.getAsJsonObject());
      } else if (element.isJsonArray()) {
         for (JsonElement entry : element.getAsJsonArray()) {
            iterate(entry, receiver);
         }
      }
   }
}
