package ic2.core.block.machines.tiles.mv;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.containers.mv.BaseTeleporterContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.networking.buffers.data.BaseTeleporterBuffer;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SerializableCollection;
import ic2.core.utils.helpers.TeleportUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;

public class BaseTeleporterTileEntity
   extends BaseTileEntity
   implements IEnergySink,
   IEUStorage,
   ITileGui,
   INetworkClientEventListener,
   INetworkDataEventListener,
   IWrenchableTile,
   ITileActivityProvider {
   public static final Map<ResourceKey<Level>, Map<String, Set<BaseTeleporterTileEntity.LocalTarget>>> ACTIVE_TELEPORTERS = Object2ObjectMaps.synchronize(
      CollectionUtils.createMap()
   );
   public int maxEnergy = 100000;
   @NetworkInfo
   public int energy = 0;
   boolean addedToEnet = false;
   @NetworkInfo
   public String networkID = "Default";
   @NetworkInfo
   public String name = "New Teleporter";
   @NetworkInfo
   public SerializableCollection<BaseTeleporterTileEntity.LocalTarget> targetList = SerializableCollection.createClientList(
      this.isRendering(), BaseTeleporterTileEntity.LocalTarget::new
   );
   BaseTeleporterTileEntity.LocalTarget myTarget;

   public BaseTeleporterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"energy", "targetList"});
      this.addNetworkFields(new String[]{"networkID", "name"});
      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("energy", this.energy);
      compound.putString("name", this.name);
      compound.putString("network", this.networkID);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.networkID = compound.getString("network");
      this.name = compound.getString("name");
   }

   @Override
   public boolean isAllowingUI() {
      return false;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new BaseTeleporterContainer(this, player, windowID);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.BASE_TELEPORTER;
   }

   public Set<BaseTeleporterTileEntity.LocalTarget> getTargets() {
      Map<String, Set<BaseTeleporterTileEntity.LocalTarget>> map = ACTIVE_TELEPORTERS.computeIfAbsent(
         this.getLevel().dimension(), k -> CollectionUtils.createMap()
      );
      return map.computeIfAbsent(this.networkID, k -> CollectionUtils.createLinkedSet());
   }

   public void syncTargets() {
      Set<BaseTeleporterTileEntity.LocalTarget> globalTargets = new ObjectLinkedOpenHashSet(this.getTargets());
      globalTargets.remove(this.myTarget);
      Iterator<BaseTeleporterTileEntity.LocalTarget> iter = globalTargets.iterator();

      while (iter.hasNext()) {
         if (!iter.next().isInReach(this.myTarget)) {
            iter.remove();
         }
      }

      Set<BaseTeleporterTileEntity.LocalTarget> localTargets = this.targetList.getCasted();
      if (localTargets.retainAll(globalTargets) || localTargets.addAll(globalTargets)) {
         this.updateTileField("targetList");
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         if (!this.addedToEnet) {
            this.addedToEnet = true;
            EnergyNet.INSTANCE.addTile(this);
         }

         this.myTarget = new BaseTeleporterTileEntity.LocalTarget(this.getBlockPos().asLong(), this.name);
         Set<BaseTeleporterTileEntity.LocalTarget> targets = this.getTargets();
         targets.add(this.myTarget);
         this.notifyTargets(targets);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating()) {
         if (this.addedToEnet) {
            this.addedToEnet = false;
            EnergyNet.INSTANCE.removeTile(this);
         }

         Set<BaseTeleporterTileEntity.LocalTarget> targets = this.getTargets();
         targets.remove(this.myTarget);
         this.notifyTargets(targets);
      }

      super.onUnloaded(chunk);
   }

   public void notifyTargets(Set<BaseTeleporterTileEntity.LocalTarget> targets) {
      for (BaseTeleporterTileEntity.LocalTarget target : targets) {
         BlockPos pos = target.getPos();
         if (this.getLevel().isLoaded(pos)) {
            BlockEntity tile = this.getLevel().getBlockEntity(pos);
            if (tile instanceof BaseTeleporterTileEntity) {
               ((BaseTeleporterTileEntity)tile).syncTargets();
            }
         }
      }
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (target.isDedicatedServer() && data instanceof BaseTeleporterBuffer buffer) {
         switch (buffer.getType()) {
            case 1: {
               Set<BaseTeleporterTileEntity.LocalTarget> targets = this.getTargets();
               targets.remove(this.myTarget);
               this.notifyTargets(targets);
               this.networkID = buffer.getNetworkID();
               this.updateTileField("networkID");
               targets = this.getTargets();
               targets.add(this.myTarget);
               this.notifyTargets(targets);
               break;
            }
            case 2: {
               Set<BaseTeleporterTileEntity.LocalTarget> targets = this.getTargets();
               targets.remove(this.myTarget);
               this.notifyTargets(targets);
               this.name = buffer.getName();
               this.updateTileField("name");
               this.myTarget = new BaseTeleporterTileEntity.LocalTarget(this.getBlockPos().asLong(), this.name);
               targets.add(this.myTarget);
               this.notifyTargets(targets);
               break;
            }
            case 3: {
               Set<BaseTeleporterTileEntity.LocalTarget> targets = this.getTargets();
               targets.remove(this.myTarget);
               this.notifyTargets(targets);
               this.networkID = buffer.getNetworkID();
               this.name = buffer.getName();
               this.myTarget = new BaseTeleporterTileEntity.LocalTarget(this.getBlockPos().asLong(), this.name);
               targets = this.getTargets();
               targets.add(this.myTarget);
               this.notifyTargets(targets);
               this.updateTileFields(new String[]{"networkID", "name"});
            }
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 1 && value >= 0) {
         if (this.energy < 4000) {
            return;
         }

         BaseTeleporterTileEntity.LocalTarget[] targets = this.targetList.get().toArray(new BaseTeleporterTileEntity.LocalTarget[this.targetList.size()]);
         if (targets.length > value) {
            BlockEntity tile = this.getLevel().getBlockEntity(targets[value].getPos());
            if (tile instanceof BaseTeleporterTileEntity base) {
               this.energy -= 4000;
               this.setActive(this.energy >= 4000);
               this.updateGuiField("energy");
               entity.awardStat(IC2Stats.DISTANCE_TELEPORTED, (int)Math.sqrt(this.getBlockPos().distSqr(tile.getBlockPos())) * 1000);
               TeleportUtil.teleportEntity(entity, (ServerLevel)base.getLevel(), tile.getBlockPos(), base.getFacing());
               entity.closeContainer();
            }
         }
      }
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return dir != this.getFacing();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.75;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getTier() {
      return 2;
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return true;
   }

   @Override
   public int getSinkTier() {
      return 2;
   }

   @Override
   public int getRequestedEnergy() {
      return this.maxEnergy - this.energy;
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      if (amount <= 128 && amount > 0) {
         int added = Math.min(amount, this.maxEnergy - this.energy);
         if (added > 0) {
            this.energy += added;
            this.updateGuiField("energy");
            this.setActive(this.energy >= 4000);
            this.handleNonTickComparators();
         }

         return amount - added;
      } else {
         return 0;
      }
   }

   public static class LocalTarget implements INetworkDataBuffer {
      public static final int MAX_DISTANCE = 10000;
      long position;
      String name;

      public LocalTarget() {
      }

      public LocalTarget(long position, String name) {
         this.position = position;
         this.name = name;
      }

      @Override
      public void write(IOutputBuffer buffer) {
         buffer.writeLong(this.position);
         buffer.writeString(this.name);
      }

      @Override
      public void read(IInputBuffer buffer) {
         this.position = buffer.readLong();
         this.name = buffer.readString();
      }

      public CompoundTag write() {
         CompoundTag nbt = new CompoundTag();
         nbt.putLong("pos", this.position);
         nbt.putString("name", this.name);
         return nbt;
      }

      public void read(CompoundTag nbt) {
         this.position = nbt.getLong("pos");
         this.name = nbt.getString("name");
      }

      public long getPosition() {
         return this.position;
      }

      public String getName() {
         return this.name;
      }

      public BlockPos getPos() {
         return BlockPos.of(this.position);
      }

      public boolean isInReach(BaseTeleporterTileEntity.LocalTarget target) {
         BlockPos from = this.getPos();
         BlockPos to = target.getPos();
         int x = from.getX() - to.getX();
         int z = from.getZ() - to.getZ();
         return x * x + z * z <= 10000;
      }
   }
}
