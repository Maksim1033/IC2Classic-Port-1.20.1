package ic2.api.crops;

import ic2.api.util.ILocation;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ICropTile extends ILocation {
   IFarmland getFarmland();

   boolean isWaterLogged();

   void setWaterlogged(boolean var1);

   ICrop getCrop();

   void setCrop(ICrop var1);

   int getGrowthStage();

   void setGrowthStage(int var1);

   int getGrowthPoints();

   void setGrowthPoints(int var1);

   int getScanLevel();

   void setScanLevel(int var1);

   boolean isCrossBreeding();

   void setCrossBreeding(boolean var1);

   int getGainStat();

   void setGainStat(int var1);

   int getGrowthStat();

   void setGrowthStat(int var1);

   int getResistanceStat();

   void setResistanceStat(int var1);

   boolean canBreed();

   void requestStateUpdate();

   void onCustomDataChanged();

   int calculateGrowthSpeed();

   boolean performManualHarvest();

   List<ItemStack> performHarvest(boolean var1);

   boolean pickCrop();

   void removeCrop();

   boolean isBlockBelow(BlockState var1);

   boolean isBlockBelow(Block var1);

   boolean isBlockBelow(TagKey<Block> var1);

   Set<Block> getBlocksBelow();

   CompoundTag getCustomData();

   int getLightLevel();

   int getEnvironmentQuality();

   int getNutrients();

   int getHumidity();

   int getWaterStorage();

   void setWaterStorage(int var1);

   int getFertilizerStorage();

   void setFertilizerStorage(int var1);

   int getWeedExStorage();

   void setWeedExStorage(int var1);

   ItemStack createSeeds(ICrop var1, int var2, int var3, int var4, int var5);
}
