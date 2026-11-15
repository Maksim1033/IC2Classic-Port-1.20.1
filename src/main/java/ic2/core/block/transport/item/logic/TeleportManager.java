package ic2.core.block.transport.item.logic;

import ic2.core.block.transport.item.tubes.TeleportTubeTileEntity;
import ic2.core.platform.player.friends.Action;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TeleportManager {
   public static final TeleportManager INSTANCE = new TeleportManager();
   Map<String, List<TeleportTubeTileEntity>> tiles = CollectionUtils.<String, List<TeleportTubeTileEntity>>createMap();
   Map<TeleportTubeTileEntity, String> tubes = CollectionUtils.<TeleportTubeTileEntity, String>createMap();

   public void add(TeleportTubeTileEntity tile) {
      String freq = tile.getFrequency();
      if (freq != null) {
         this.tubes.put(tile, freq);
         List<TeleportTubeTileEntity> list = this.tiles.computeIfAbsent(freq, k -> CollectionUtils.createList());
         list.add(tile);
      }
   }

   public void remove(TeleportTubeTileEntity tile) {
      String oldId = this.tubes.remove(tile);
      if (oldId != null) {
         List<TeleportTubeTileEntity> list = this.tiles.get(oldId);
         if (list != null && list.remove(tile) && list.isEmpty()) {
            this.tiles.remove(oldId);
         }
      }
   }

   public void update(TeleportTubeTileEntity tile) {
      String id = tile.getFrequency();
      if (id != null && this.tubes.containsKey(tile)) {
         String oldId = this.tubes.put(tile, id);
         List<TeleportTubeTileEntity> list = this.tiles.get(oldId);
         if (list != null && list.remove(tile) && list.isEmpty()) {
            this.tiles.remove(oldId);
         }

         list = this.tiles.computeIfAbsent(id, k -> CollectionUtils.createList());
         list.add(tile);
      }
   }

   public TeleportTubeTileEntity getRandomTarget(TeleportTubeTileEntity tile) {
      List<TeleportTubeTileEntity> targets = this.getAllPossibleTarget(tile);
      return targets.isEmpty() ? null : targets.get(tile.getLevel().random.nextInt(targets.size()));
   }

   public List<TeleportTubeTileEntity> getAllPossibleTarget(TeleportTubeTileEntity tile) {
      if (tile.getFrequency() != null && tile.canSend()) {
         List<TeleportTubeTileEntity> targets = this.tiles.get(tile.getFrequency());
         if (targets != null && !targets.isEmpty()) {
            List<TeleportTubeTileEntity> result = CollectionUtils.<TeleportTubeTileEntity>createList();
            boolean isPublic = tile.isPublic();
            int i = 0;

            for (int m = targets.size(); i < m; i++) {
               TeleportTubeTileEntity target = targets.get(i);
               if (target != tile
                  && target.canReceive()
                  && isPublic == target.isPublic()
                  && target.canDoAction(tile.getOwner(), Action.TELEPORT_NETWORK, false)) {
                  result.add(target);
               }
            }

            return result;
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }
}
