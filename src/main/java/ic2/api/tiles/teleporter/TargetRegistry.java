package ic2.api.tiles.teleporter;

import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TargetRegistry extends SavedData {
   Map<TeleporterTarget, String> names = new Object2ObjectLinkedOpenHashMap();

   public TargetRegistry() {
   }

   public TargetRegistry(CompoundTag tag) {
      for (CompoundTag nbt : NBTListWrapper.wrap(tag.getList("names", 10), CompoundTag.class)) {
         TeleporterTarget target = TeleporterTarget.read(nbt);
         if (target != null) {
            this.names.put(target, nbt.getString("name"));
         }
      }
   }

   public void addTarget(TeleporterTarget target, String name) {
      this.names.put(target, name);
   }

   public void removeTarget(TeleporterTarget target) {
      this.names.remove(target);
   }

   public String getTargetName(TeleporterTarget target) {
      return this.names.get(target);
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag list = new ListTag();

      for (Entry<TeleporterTarget, String> entry : this.names.entrySet()) {
         CompoundTag nbt = new CompoundTag();
         nbt.put("target", entry.getKey().write());
         nbt.putString("name", entry.getValue());
         list.add(nbt);
      }

      tag.put("names", list);
      return tag;
   }

   public static TargetRegistry getRegistry() {
      return (TargetRegistry)ServerLifecycleHooks.getCurrentServer()
         .overworld()
         .getDataStorage()
         .computeIfAbsent(TargetRegistry::new, TargetRegistry::new, "ic2_teleporter_names");
   }
}
