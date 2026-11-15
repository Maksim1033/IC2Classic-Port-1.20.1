package ic2.core.item.base;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public class PropertiesBuilder {
   private Integer maxStackSize = null;
   private Integer maxDamage = null;
   private Item containerItem = null;
   private CreativeModeTab group = null;
   private Rarity rarity = null;
   private FoodProperties food = null;
   private Boolean canRepair = null;
   private Boolean foiled;

   public PropertiesBuilder maxStackSize(int maxStackSize) {
      if (this.maxStackSize == null) {
         this.maxStackSize = maxStackSize;
      }

      return this;
   }

   public PropertiesBuilder maxStackSizeOverride(int maxStackSize) {
      this.maxStackSize = maxStackSize;
      return this;
   }

   public PropertiesBuilder maxDamage(int maxDamage) {
      if (this.maxDamage == null) {
         this.maxDamage = maxDamage;
      }

      return this;
   }

   public PropertiesBuilder maxDamageOverride(int maxDamage) {
      this.maxDamage = maxDamage;
      return this;
   }

   public PropertiesBuilder containerItem(Item containerItem) {
      if (this.containerItem == null) {
         this.containerItem = containerItem;
      }

      return this;
   }

   public PropertiesBuilder containerItemOverride(Item containerItem) {
      this.containerItem = containerItem;
      return this;
   }

   public PropertiesBuilder setFoiled(boolean value) {
      if (this.foiled == null) {
         this.foiled = value;
      }

      return this;
   }

   public PropertiesBuilder setFoiledOverride(boolean value) {
      this.foiled = value;
      return this;
   }

   public PropertiesBuilder group(CreativeModeTab group) {
      if (this.group == null) {
         this.group = group;
      }

      return this;
   }

   public PropertiesBuilder groupOverride(CreativeModeTab group) {
      this.group = group;
      return this;
   }

   public PropertiesBuilder rarity(Rarity rarity) {
      if (this.rarity == null) {
         this.rarity = rarity;
      }

      return this;
   }

   public PropertiesBuilder rarityOverride(Rarity rarity) {
      this.rarity = rarity;
      return this;
   }

   public PropertiesBuilder food(FoodProperties food) {
      if (this.food == null) {
         this.food = food;
      }

      return this;
   }

   public PropertiesBuilder foodOverride(FoodProperties food) {
      this.food = food;
      return this;
   }

   public PropertiesBuilder canRepair(boolean canRepair) {
      if (this.canRepair == null) {
         this.canRepair = canRepair;
      }

      return this;
   }

   public PropertiesBuilder canRepairOverride(boolean canRepair) {
      this.canRepair = canRepair;
      return this;
   }

   public PropertiesBuilder setRepair() {
      return this.canRepair(true);
   }

   public PropertiesBuilder setRepairOverride() {
      return this.canRepairOverride(true);
   }

   public PropertiesBuilder setNoRepair() {
      return this.canRepair(false);
   }

   public PropertiesBuilder setNoRepairOverride() {
      return this.canRepairOverride(false);
   }

   public Boolean isFoiled() {
      return this.foiled;
   }

   public Properties build() {
      Properties props = new Properties();
      if (this.maxStackSize != null) {
         props.stacksTo(this.maxStackSize);
      }

      if (this.maxDamage != null) {
         props.durability(this.maxDamage);
      }

      if (this.containerItem != null) {
         props.craftRemainder(this.containerItem);
      }

      if (this.rarity != null) {
         props.rarity(this.rarity);
      }

      if (this.food != null) {
         props.food(this.food);
      }

      if (this.canRepair != null && !this.canRepair) {
         props.setNoRepair();
      }

      return props;
   }
}
