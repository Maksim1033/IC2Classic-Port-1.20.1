package ic2.core.item.reactor.urantypes;

import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.ColorUtils;
import net.minecraft.world.item.ItemStack;

public class CharcoalUranium extends UraniumBaseType {
   public static final CharcoalUranium INSTANCE = new CharcoalUranium();

   private CharcoalUranium() {
   }

   @Override
   public int getRodDurability() {
      return 20000;
   }

   @Override
   public float getPulseEU() {
      return 0.6F;
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
      return 0.6F;
   }

   @Override
   public float getExplosionModifier() {
      return 2.0F;
   }

   @Override
   public boolean isEnrichedUranium() {
      return true;
   }

   @Override
   public int getFusionHeat() {
      return 1;
   }

   @Override
   public ItemStack getBaseIngot() {
      return new ItemStack(IC2Items.INGOT_URANIUM_ENRICHED_CHARCOAL);
   }

   @Override
   public String getName() {
      return "charcoal";
   }

   @Override
   public int getColor() {
      return ColorUtils.rgb(54, 54, 54, 255);
   }

   @Override
   public ItemStack createNearDepletedRod(int stacksize) {
      return new ItemStack(IC2Items.URANIUM_ROD_NEAR_DEPLETED_CHARCOAL, stacksize);
   }

   @Override
   public ItemStack createReEnrichedRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_RE_ENRICHED_CHARCOAL);
   }

   @Override
   public ItemStack createIsotopicRod() {
      ItemStack stack = new ItemStack(IC2Items.URANIUM_ROD_ISOTOPIC_CHARCOAL);
      stack.setDamageValue(stack.getMaxDamage());
      return stack;
   }

   @Override
   public ItemStack createSingleRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_CHARCOAL_SINGLE);
   }

   @Override
   public ItemStack createDualRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_CHARCOAL_DUAL);
   }

   @Override
   public ItemStack createQuadRod() {
      return new ItemStack(IC2Items.URANIUM_ROD_CHARCOAL_QUAD);
   }
}
