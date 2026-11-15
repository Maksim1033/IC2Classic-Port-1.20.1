package ic2.core.platform.player;

import ic2.api.crops.ICrop;
import ic2.core.IC2;
import ic2.core.block.crops.CropRegistry;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class CropStatistics extends SavedData {
   Map<UUID, CropStatistics.PlayerInfo> allCropsFound = CollectionUtils.<UUID, CropStatistics.PlayerInfo>createLinkedMap();

   public CropStatistics() {
   }

   public CropStatistics(CompoundTag tag) {
      for (CompoundTag nbt : NBTListWrapper.wrap(tag.getList("data", 10), CompoundTag.class)) {
         this.allCropsFound.put(nbt.getUUID("player"), new CropStatistics.PlayerInfo(nbt));
      }
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag list = new ListTag();

      for (Entry<UUID, CropStatistics.PlayerInfo> entry : this.allCropsFound.entrySet()) {
         CompoundTag nbt = entry.getValue().save();
         nbt.putUUID("player", entry.getKey());
         list.add(nbt);
      }

      tag.put("data", list);
      return tag;
   }

   public void onCropReceived(UUID id, ICrop crop, int growth, int gain, int resistance) {
      Player player = IC2.PLATFORM.getServer().getPlayerList().getPlayer(id);
      this.allCropsFound.computeIfAbsent(id, T -> new CropStatistics.PlayerInfo()).onReceived(player, crop, growth, gain, resistance);
   }

   public static CropStatistics getStats() {
      MinecraftServer server = IC2.PLATFORM.getServer();
      return server == null
         ? null
         : (CropStatistics)server.overworld().getDataStorage().computeIfAbsent(CropStatistics::new, CropStatistics::new, "ic2_crop_stats");
   }

   public static class PlayerInfo {
      Set<ICrop> discovered = CollectionUtils.<ICrop>createLinkedSet();
      int tier;
      int growth;
      int gain;
      int resistance;

      public PlayerInfo() {
      }

      public PlayerInfo(CompoundTag tag) {
         for (StringTag id : NBTListWrapper.wrap(tag.getList("discovered", 8), StringTag.class)) {
            ICrop crop = CropRegistry.INSTANCE.getCrop(ResourceLocation.tryParse(id.getAsString()));
            if (crop != null) {
               this.discovered.add(crop);
            }
         }

         this.tier = tag.getInt("tier");
         this.growth = tag.getInt("growth");
         this.gain = tag.getInt("gain");
         this.resistance = tag.getInt("resistance");
      }

      public CompoundTag save() {
         CompoundTag nbt = new CompoundTag();
         ListTag allFound = new ListTag();

         for (ICrop crop : this.discovered) {
            allFound.add(StringTag.valueOf(crop.id().toString()));
         }

         nbt.put("discovered", allFound);
         nbt.putInt("tier", this.tier);
         nbt.putInt("growth", this.growth);
         nbt.putInt("gain", this.gain);
         nbt.putInt("resistance", this.resistance);
         return nbt;
      }

      public void onReceived(Player player, ICrop crop, int growth, int gain, int resistance) {
         this.discovered.add(crop);
         this.tier = Math.max(this.tier, crop.getProperties().getTier());
         this.growth = Math.max(this.growth, growth);
         this.gain = Math.max(this.gain, gain);
         this.resistance = Math.max(this.resistance, resistance);
         this.setValue(player, this.discovered.size(), IC2Stats.CROPS_DISCOVERED);
         this.setValue(player, this.tier, IC2Stats.HIGHEST_CROP_TIER);
         this.setValue(player, this.growth, IC2Stats.HIGHEST_CROP_GROWTH);
         this.setValue(player, this.gain, IC2Stats.HIGHEST_CROP_GAIN);
         this.setValue(player, this.resistance, IC2Stats.HIGHEST_CROP_RESISTANCE);
      }

      private void setValue(Player player, int newValue, ResourceLocation id) {
         Stat<ResourceLocation> stat = Stats.CUSTOM.get(id);
         player.resetStat(stat);
         player.awardStat(id, newValue);
      }
   }
}
