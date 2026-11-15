package ic2.core.item.reactor.urantypes;

import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.ColorUtils;
import net.minecraft.world.item.ItemStack;

public class StandardUranium extends UraniumBaseType {
   public static final StandardUranium INSTANCE = new StandardUranium();

   private StandardUranium() {
   }

   @Override
   public int getRodDurability() {
      return 10000;
   }

   @Override
   public float getPulseEU() {
      return 1.0F;
   }

   @Override
   public int getUraniumPulses() {
      return 1;
   }

   @Override
   public int getPulsesForConnection() {
      return 1;
   }

   @Override
   public float getPulseHeatModifier() {
      return 1.0F;
   }

   @Override
   public float getExplosionModifier() {
      return 2.0F;
   }

   @Override
   public boolean isEnrichedUranium() {
      return false;
   }

   @Override
   public int getFusionHeat() {
      return 5;
   }

   @Override
   public ItemStack getBaseIngot() {
      return new ItemStack(IC2Items.INGOT_URANIUM);
   }

   @Override
   public String getName() {
      return "";
   }

   @Override
   public int getColor() {
      return ColorUtils.rgb(96, 174, 17, 255);
   }

   @Override
   public ItemStack createNearDepletedRod(int stacksize) {
      return new ItemStack(IC2Items.URANIUM_ROD_NEAR_DEPLETED, stacksize);
   }

   @Override
   public ItemStack createReEnrichedRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_RE_ENRICHED);
   }

   @Override
   public ItemStack createIsotopicRod() {
      ItemStack stack = new ItemStack(IC2Items.URANIUM_ROD_ISOTOPIC);
      stack.setDamageValue(stack.getMaxDamage());
      return stack;
   }

   @Override
   public ItemStack createSingleRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_SINGLE);
   }

   @Override
   public ItemStack createDualRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_DUAL);
   }

   @Override
   public ItemStack createQuadRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_QUAD);
   }
}
