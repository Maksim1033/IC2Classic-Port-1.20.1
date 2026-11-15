package ic2.api.items;

import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IUpgradeItem {
   IUpgradeItem.UpgradeType getType(ItemStack var1);

   EnumSet<IUpgradeItem.Functions> getFunctions(ItemStack var1);

   void onInstall(ItemStack var1, IMachine var2);

   double getProcessingSpeedMultiplier(ItemStack var1, IMachine var2);

   int getExtraProcessingSpeed(ItemStack var1, IMachine var2);

   double getProcessingTimeMultiplier(ItemStack var1, IMachine var2);

   int getExtraProcessingTime(ItemStack var1, IMachine var2);

   double getEnergyDemandMultiplier(ItemStack var1, IMachine var2);

   int getExtraEnergyDemand(ItemStack var1, IMachine var2);

   double getEnergyStorageMultiplier(ItemStack var1, IMachine var2);

   int getExtraEnergyStorage(ItemStack var1, IMachine var2);

   int getExtraTier(ItemStack var1, IMachine var2);

   float getSoundMultiplier(ItemStack var1, IMachine var2);

   boolean useRedstoneInvertion(ItemStack var1, IMachine var2);

   void onTick(ItemStack var1, IMachine var2);

   void onMachineFinishedRecipePre(ItemStack var1, IMachine var2, IRecipeOutput var3, CompoundTag var4);

   void onMachineFinishedRecipePost(ItemStack var1, IMachine var2, @Nullable IMachineRecipeList.RecipeEntry var3, List<IStackOutput> var4);

   void onMachineProcessed(ItemStack var1, IMachine var2);

   public static enum Functions {
      TICK,
      RECIPE;
   }

   public static enum UpgradeType {
      RECIPE_MOD("ProcessingTime", "EnergyDemand", "ProcessingSpeed"),
      MACHINE_MOD("EnergyStorage", "ExtraTier"),
      REDSTONE_MOD("RedstoneInvertion"),
      PROCESSING_MOD(),
      TRANSPORT_MOD(),
      AUDIO_MOD(),
      CUSTOM_MOD();

      final String[] names;

      private UpgradeType(String... names) {
         this.names = names;
      }

      public String[] getNames() {
         return this.names;
      }
   }
}
