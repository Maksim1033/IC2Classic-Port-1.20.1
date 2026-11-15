package ic2.api.crops;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ICrop {
   public static final Component UNKNOWN = Component.translatable("crop.ic2.unknown").withStyle(ChatFormatting.RED);
   public static final ItemStack[] EMPTY_DROPS = new ItemStack[0];

   public abstract ResourceLocation id();

   public Component discoveredBy() {
      return UNKNOWN;
   }

   public abstract Component getName();

   public ItemStack getDisplayItem() {
      return ItemStack.EMPTY;
   }

   public abstract CropProperties getProperties();

   public ICrop.CropType getCropType() {
      return ICrop.CropType.AIR;
   }

   public boolean isWaterCleaningCrop(ICropTile tile) {
      return false;
   }

   public abstract String[] getAttributes();

   @OnlyIn(Dist.CLIENT)
   public abstract List<ResourceLocation> getTextures();

   public abstract int getGrowthSteps();

   public int getOptimalHarvestStep(ICropTile cropTile) {
      return this.getGrowthSteps();
   }

   public int getGrowthDuration(ICropTile cropTile) {
      return this.getProperties().getTier() * 200;
   }

   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return humidity + nutrients + air;
   }

   public boolean canGrow(ICropTile cropTile) {
      return cropTile.getGrowthStage() < this.getGrowthSteps();
   }

   public boolean canBeHarvested(ICropTile cropTile) {
      return cropTile.getGrowthStage() == this.getGrowthSteps();
   }

   public int getAfterHarvestStage(ICropTile cropTile) {
      return 1;
   }

   public double getDropChance(ICropTile cropTile) {
      return Math.pow(0.95, (double)this.getProperties().getTier());
   }

   public abstract ItemStack[] getDrops(ICropTile var1);

   public float getSeedDropChance(ICropTile cropTile) {
      if (cropTile.getGrowthStage() == 1) {
         return 0.0F;
      } else {
         float base = 0.5F;
         if (cropTile.getGrowthStage() == 2) {
            base /= 2.0F;
         }

         for (int i = 0; i < this.getProperties().getTier(); i++) {
            base *= 0.8F;
         }

         return base;
      }
   }

   public ItemStack getSeeds(ICropTile cropTile) {
      return cropTile.createSeeds(cropTile.getCrop(), cropTile.getGrowthStat(), cropTile.getGainStat(), cropTile.getResistanceStat(), cropTile.getScanLevel());
   }

   public boolean canBreed(ICropTile cropTile) {
      return cropTile.getGrowthStage() >= 3;
   }

   public boolean isWeed(ICropTile cropTile) {
      return cropTile.getGrowthStage() >= 2 && (cropTile.getCrop() == ICropRegistry.WEED || cropTile.getGrowthStat() >= 24);
   }

   public boolean onRightClick(ICropTile cropTile, Player player, InteractionHand hand) {
      return cropTile.performManualHarvest();
   }

   public boolean onLeftClick(ICropTile cropTile, Player player) {
      return cropTile.pickCrop();
   }

   public void onTick(ICropTile cropTile) {
   }

   public void onBlockUpdate(ICropTile cropTile) {
   }

   public void onEntityCollision(ICropTile cropTile, Entity entity) {
      if (entity instanceof LivingEntity && entity.isSprinting()) {
         cropTile.pickCrop();
      }
   }

   public void onCropPlaceFailed(BaseSeed seed, ICropTile cropTile, Player player) {
      if (seed.crop.canGrow(cropTile)) {
         if (seed.crop.getCropType().isCompatible(cropTile.isWaterLogged())) {
            player.displayClientMessage(Component.translatable("info.crop.ic2.plant.placement_error.not_compatible.water"), false);
         } else {
            player.displayClientMessage(Component.translatable("info.crop.ic2.plant.placement_error.not_compatible.land"), false);
         }
      } else if (this.hasCustomCropPlaceFailedMessage(cropTile)) {
         player.displayClientMessage(
            Component.translatable("info.crop.ic2.plant.placement_error", new Object[]{this.getCustomCropPlaceFailedMessage(cropTile)}), false
         );
      } else {
         player.displayClientMessage(Component.translatable("info.crop.ic2.plant.placement_error.not_compatible"), false);
      }
   }

   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return false;
   }

   public abstract Component getCustomCropPlaceFailedMessage(ICropTile var1);

   public boolean canEmitRedstone(ICropTile cropTile) {
      return false;
   }

   public int getRedstoneLevel(ICropTile cropTile) {
      return 0;
   }

   public int getEmittedLight(ICropTile cropTile) {
      return 0;
   }

   public String desc(int i) {
      String[] att = this.getAttributes();
      if (att == null || att.length == 0) {
         return "";
      } else if (i == 0) {
         String s = att[0];
         if (att.length >= 2) {
            s = s + ", " + att[1];
            if (att.length >= 3) {
               s = s + ",";
            }
         }

         return s;
      } else if (att.length < 3) {
         return "";
      } else {
         String s = att[2];
         if (att.length >= 4) {
            s = s + ", " + att[3];
         }

         return s;
      }
   }

   public static int getGrowth(int combo) {
      return combo >> 10 & 31;
   }

   public static int getGain(int combo) {
      return combo >> 5 & 31;
   }

   public static int getResistance(int combo) {
      return combo & 31;
   }

   public static short combineStats(int growth, int gain, int resistance) {
      return (short)((growth & 31) << 10 | (gain & 31) << 5 | resistance & 31);
   }

   public static enum CropType {
      WATER,
      BOTH,
      AIR;

      public boolean isCompatible(boolean waterlogged) {
         return this == BOTH || (waterlogged ? this == WATER : this == AIR);
      }
   }
}
