package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoralCrop extends BaseCrop {
   private String name;
   private Item drop_coral;
   private Item drop_fan;
   private Item drop_block;
   private static Map<Item, Item> DEAD_DROPS = fillDeadMap();

   public CoralCrop(String name, Item drop_coral, Item drop_fan, Item drop_block, CropProperties properties, String... attributes) {
      super(name + "_coral", properties, attributes);
      this.name = name;
      this.drop_coral = drop_coral;
      this.drop_fan = drop_fan;
      this.drop_block = drop_block;
   }

   public CoralCrop(String name, Item drop_coral, Item drop_fan, Item drop_block, String color) {
      this(name, drop_coral, drop_fan, drop_block, new CropProperties(2, 3, 0, 0, 5, 1), "Coral", "Decoration", color);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String stageTexture = "";
      switch (stage) {
         case 0:
         case 1:
            stageTexture = "growing_" + stage;
            break;
         case 2:
         case 3:
         case 4:
            stageTexture = "growing_" + this.name + "_" + stage;
            break;
         case 5:
         case 6:
         case 7:
            stageTexture = "dead_" + this.name + "_" + (stage - 3);
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/corals").get(stageTexture);
   }

   @Override
   public ICrop.CropType getCropType() {
      return ICrop.CropType.WATER;
   }

   protected boolean isDead(ICropTile tile) {
      return tile.getCustomData().getBoolean("isDead");
   }

   protected void setDead(ICropTile tile, boolean value) {
      tile.getCustomData().putBoolean("isDead", value);
   }

   @Override
   public void onTick(ICropTile cropTile) {
      if (!cropTile.isWaterLogged()) {
         this.setDead(cropTile, true);
         if (cropTile.getGrowthStage() > 1 && cropTile.getGrowthStage() < 5) {
            cropTile.setGrowthStage(cropTile.getGrowthStage() + 3);
         }
      }
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 2;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return super.getGrowthDuration(cropTile) * 2;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && !this.isDead(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getEnvironmentQuality() >= 3;
   }

   @Override
   public int getOptimalHarvestStep(ICropTile cropTile) {
      return this.isDead(cropTile) ? 7 : 4;
   }

   @Override
   public boolean canBeHarvested(ICropTile cropTile) {
      return this.isDead(cropTile) ? cropTile.getGrowthStage() > 5 : cropTile.getGrowthStage() == 4;
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.MOJANG;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(this.drop_coral);
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      return 0.8;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      List<ItemStack> drops = new ArrayList<>();
      int random = BaseCrop.getRandom(cropTile).nextInt() % 13;
      boolean isDead = this.isDead(cropTile);
      drops.add(new ItemStack((ItemLike)(isDead ? (ItemLike)DEAD_DROPS.get(this.drop_coral) : this.drop_coral), BaseCrop.getRandom(cropTile).nextInt(2) + 1));
      if (random % 6 == 0) {
         drops.add(new ItemStack((ItemLike)(isDead ? (ItemLike)DEAD_DROPS.get(this.drop_fan) : this.drop_fan), BaseCrop.getRandom(cropTile).nextInt(2)));
      }

      if (random % 4 == 0 || random % 9 == 0) {
         drops.add(new ItemStack((ItemLike)(isDead ? (ItemLike)DEAD_DROPS.get(this.drop_block) : this.drop_block), BaseCrop.getRandom(cropTile).nextInt(2)));
      }

      if (isDead && random % 3 == 0) {
         drops.add(new ItemStack(Items.GRAY_DYE));
      }

      return drops.toArray(new ItemStack[0]);
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate(
            "info.crop.ic2.plant.placement_error.quality",
            new Object[]{this.translate("info.crop.ic2.plant.placement_error.better"), this.translate("info.crop.ic2.plant.placement_error.water")}
         )
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }

   private static Map<Item, Item> fillDeadMap() {
      Map<Item, Item> map = new HashMap<>();
      map.putIfAbsent(Items.BRAIN_CORAL, Items.DEAD_BRAIN_CORAL);
      map.putIfAbsent(Items.BRAIN_CORAL_BLOCK, Items.DEAD_BRAIN_CORAL_BLOCK);
      map.putIfAbsent(Items.BRAIN_CORAL_FAN, Items.DEAD_BRAIN_CORAL_FAN);
      map.putIfAbsent(Items.BUBBLE_CORAL, Items.DEAD_BUBBLE_CORAL);
      map.putIfAbsent(Items.BUBBLE_CORAL_BLOCK, Items.DEAD_BUBBLE_CORAL_BLOCK);
      map.putIfAbsent(Items.BUBBLE_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN);
      map.putIfAbsent(Items.FIRE_CORAL, Items.DEAD_FIRE_CORAL);
      map.putIfAbsent(Items.FIRE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK);
      map.putIfAbsent(Items.FIRE_CORAL_FAN, Items.DEAD_FIRE_CORAL_FAN);
      map.putIfAbsent(Items.HORN_CORAL, Items.DEAD_HORN_CORAL);
      map.putIfAbsent(Items.HORN_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK);
      map.putIfAbsent(Items.HORN_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN);
      map.putIfAbsent(Items.TUBE_CORAL, Items.DEAD_TUBE_CORAL);
      map.putIfAbsent(Items.TUBE_CORAL_BLOCK, Items.DEAD_TUBE_CORAL_BLOCK);
      map.putIfAbsent(Items.TUBE_CORAL_FAN, Items.DEAD_TUBE_CORAL_FAN);
      return map;
   }
}
