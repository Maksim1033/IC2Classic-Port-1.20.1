package ic2.core.platform.recipes.misc;

import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.misc.ICanEffect;
import ic2.api.recipes.registries.IFoodCanRegistry;
import ic2.core.IC2;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.item.food_and_drink.TinCanItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SimpleRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoodCanRegistry implements IFoodCanRegistry {
   public static final ResourceLocation EMPTY = new ResourceLocation("empty");
   public static final FoodCanRegistry INSTANCE = new FoodCanRegistry();
   SimpleRegistry<TinCanItem> registry = new SimpleRegistry<>();
   Map<ItemStack, ResourceLocation> ids = CollectionUtils.<ItemStack, ResourceLocation>createMap(ItemStackStrategy.INSTANCE);

   public void preInit() {
      RecipeRegistry.CAN_EFFECTS = INSTANCE;
   }

   public void init() {
      this.registry.register(EMPTY, (TinCanItem)IC2Items.TIN_CAN_FILLED);
      String textureLocation = "food_and_drink/tincan";
      this.registerItemForEffect(new ItemStack(Items.ROTTEN_FLESH), new ResourceLocation("ic2", "bad"));
      IC2SimpleCanEffect badTinCan = new IC2SimpleCanEffect(
         new ResourceLocation("ic2", "bad"), new MobEffectInstance(MobEffects.CONFUSION, 150), new MobEffectInstance(MobEffects.HUNGER, 150, 0)
      );
      badTinCan.setTexture(textureLocation, "bad_tin_can");
      badTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.bad"));
      badTinCan.setRandom(0.8F);
      this.registerEffect(badTinCan);
      this.registerItemForEffect(new ItemStack(Items.SPIDER_EYE), new ResourceLocation("ic2", "poison"));
      this.registerItemForEffect(new ItemStack(Items.POISONOUS_POTATO), new ResourceLocation("ic2", "poison"));
      IC2SimpleCanEffect poisonTinCan = new IC2SimpleCanEffect(new ResourceLocation("ic2", "poison"), new MobEffectInstance(MobEffects.POISON, 50, 0));
      poisonTinCan.setTexture(textureLocation, "bad_tin_can");
      poisonTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.poison"));
      this.registerEffect(poisonTinCan);
      this.registerItemForEffect(new ItemStack(Items.CHICKEN), new ResourceLocation("ic2", "chicken"));
      IC2SimpleCanEffect chickenTinCan = new IC2SimpleCanEffect(new ResourceLocation("ic2", "chicken"), new MobEffectInstance(MobEffects.HUNGER, 300, 0));
      chickenTinCan.setTexture(textureLocation, "chicken_tin_can");
      chickenTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.chicken"));
      chickenTinCan.setRandom(0.3F);
      this.registerEffect(chickenTinCan);
      this.registerItemForEffect(new ItemStack(Items.GOLDEN_APPLE), new ResourceLocation("ic2", "golden_apple"));
      IC2SimpleCanEffect goldenAppleTinCan = new IC2SimpleCanEffect(
         new ResourceLocation("ic2", "golden_apple"),
         new MobEffectInstance(MobEffects.REGENERATION, 25, 1),
         new MobEffectInstance(MobEffects.ABSORPTION, 600, 0)
      );
      goldenAppleTinCan.setTexture(textureLocation, "golden_tin_can");
      goldenAppleTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.golden_apple"));
      this.registerEffect(goldenAppleTinCan);
      this.registerItemForEffect(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), new ResourceLocation("ic2", "enchanted_golden_apple"));
      IC2SimpleCanEffect enchantedGoldenAppleTinCan = new IC2SimpleCanEffect(
         new ResourceLocation("ic2", "enchanted_golden_apple"),
         new MobEffectInstance(MobEffects.REGENERATION, 100, 1),
         new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1500, 0),
         new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1500, 0),
         new MobEffectInstance(MobEffects.ABSORPTION, 600, 3)
      );
      enchantedGoldenAppleTinCan.setTexture(textureLocation, "golden_tin_can");
      enchantedGoldenAppleTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.enchanted_golden_apple"));
      this.registerEffect(enchantedGoldenAppleTinCan);
      this.registerItemForEffect(new ItemStack(Items.CHORUS_FRUIT), new ResourceLocation("ic2", "chorus_fruit"));
      ChorusFruitEffect chorusTinCan = new ChorusFruitEffect(new ResourceLocation("ic2", "chorus_fruit"));
      chorusTinCan.setTexture(textureLocation, "chorus_tin_can");
      chorusTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.chorus_fruit"));
      this.registerEffect(chorusTinCan);
      this.registerItemForEffect(new ItemStack(Items.PUFFERFISH), new ResourceLocation("ic2", "pufferfish"));
      IC2SimpleCanEffect pufferfishTinCan = new IC2SimpleCanEffect(
         new ResourceLocation("ic2", "pufferfish"),
         new MobEffectInstance(MobEffects.POISON, 1200, 3),
         new MobEffectInstance(MobEffects.HUNGER, 300, 2),
         new MobEffectInstance(MobEffects.CONFUSION, 300, 1)
      );
      pufferfishTinCan.setTexture(textureLocation, "bad_tin_can");
      pufferfishTinCan.setTooltip(Component.translatable("tooltip.item.ic2.filled_tin_can.pufferfish"));
      this.registerEffect(pufferfishTinCan);
   }

   @Override
   public Item registerEffect(ICanEffect effect) {
      if (effect.getID().equals(EMPTY)) {
         IC2.LOGGER.info("Failed");
         return null;
      } else {
         TinCanItem item = new TinCanItem(effect);
         IC2Items.registerItem(item);
         this.registry.register(effect.getID(), item);
         return item;
      }
   }

   @Override
   public ICanEffect getEffect(ResourceLocation location) {
      TinCanItem item = this.registry.getValue(location);
      return item == null ? null : item.getEffect();
   }

   @Override
   public List<ResourceLocation> getAllEffects() {
      return new ObjectArrayList(this.registry.keySet());
   }

   @Override
   public void registerItemForEffect(ItemStack stack, ResourceLocation location) {
      this.ids.put(stack.copy(), location);
   }

   @Override
   public ResourceLocation getEffectForFood(ItemStack stack) {
      return this.ids.get(stack);
   }

   @Override
   public Item getEffectItem(ResourceLocation item) {
      return this.registry.getValue(item);
   }
}
