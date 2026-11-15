package ic2.core.block.machines.tiles.luv;

import com.google.common.base.Strings;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.api.tiles.teleporter.ITeleporterTarget;
import ic2.api.tiles.teleporter.TargetRegistry;
import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.core.IC2;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.luv.TeleporterHubContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.TeleportUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleporterHubTileEntity extends BaseElectricTileEntity implements ITeleporterTarget, INetworkDataEventListener, ITileGui, ITileActivityProvider {
   public static final AtomicInteger INDEX = new AtomicInteger();
   @NetworkInfo
   public TeleporterHubTileEntity.TargetStorage targets = new TeleporterHubTileEntity.TargetStorage();

   public TeleporterHubTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 32768, 100000000);
      this.addGuiFields(new String[]{"targets"});
   }

   @Override
   public boolean allowsUI() {
      return false;
   }

   @Override
   public boolean isAllowingUI() {
      return false;
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TELEPORTER_HUB;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new TeleporterHubContainer(this, player, windowID);
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (target.isDedicatedServer() && data instanceof NBTBuffer buffer && player != null) {
         if (id.equalsIgnoreCase("internal_rename")) {
            this.setCustomName(Component.literal(buffer.getNBT().getString("newName")));
            return;
         }

         TeleporterTarget destination = TeleporterTarget.read(buffer.getNBT());
         if (destination == null || !this.targets.contains(destination)) {
            return;
         }

         if (id.equalsIgnoreCase("delete") && this.targets.removeTarget(destination)) {
            this.updateGuiField("targets");
         } else if (id.equalsIgnoreCase("teleport")) {
            player.closeContainer();
            this.teleport(player, destination);
         } else if (id.equalsIgnoreCase("rename")) {
            this.targets.setCustomName(destination, buffer.getNBT().getString("newName"));
            this.updateGuiField("targets");
         }
      }
   }

   protected void teleport(Player player, TeleporterTarget target) {
      BlockEntity tile = target.getTile();
      if (!(tile instanceof ITeleporterTarget)) {
         this.targets.removeTarget(target);
         this.updateGuiField("targets");
      } else {
         this.targets.updateName(target);
         this.updateGuiField("targets");
         int cost = this.getCost(target, player);
         if (cost > 0 && this.hasEnergy(cost)) {
            this.useEnergy(cost);
            int distance = (int)Math.sqrt(this.getBlockPos().distSqr(target.getTargetPosition()));
            player.awardStat(IC2Stats.DISTANCE_TELEPORTED, distance * 1000);
            if (distance >= 1000) {
               IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:teleport_far"));
            }

            TeleportUtil.teleportEntity(player, target.getWorld(), target.getTargetPosition(), ((ITeleporterTarget)tile).getFacing());
            IC2.AUDIO.playSound(player, new ResourceLocation("ic2", "sounds/machines/teleport.ogg"));
         }
      }
   }

   public int getCost(TeleporterTarget target, Player player) {
      int weight = TeleportUtil.getWeightOfEntity(player, true);
      if (weight == 0) {
         return 0;
      } else {
         double cost = this.isRendering()
            ? TeleportUtil.getDistanceCost(this.getLevel().dimension(), this.worldPosition, target.getDimension(), target.getTargetPosition())
            : TeleportUtil.getDistanceCost(this.getLevel(), this.worldPosition, target.getWorld(), target.getTargetPosition());
         return (int)((double)weight * cost * 5.0);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && !chunk) {
         TargetRegistry.getRegistry().removeTarget(new TeleporterTarget(this));
      }

      super.onUnloaded(chunk);
   }

   @Override
   public Component setCustomName(Component comp) {
      Component result = super.setCustomName(comp);
      if (this.isSimulating() && this.hasCustomName()) {
         String name = this.getCustomName().getString();
         if (Strings.isNullOrEmpty(name)) {
            TargetRegistry.getRegistry().removeTarget(new TeleporterTarget(this));
         } else {
            TargetRegistry.getRegistry().addTarget(new TeleporterTarget(this), name);
         }
      }

      return result;
   }

   @Override
   public ITeleporterTarget.TeleportType getSendType() {
      return ITeleporterTarget.TeleportType.ENTITY;
   }

   @Override
   public boolean canReceive(ITeleporterTarget.TeleportType type) {
      return type == ITeleporterTarget.TeleportType.ENTITY;
   }

   @Override
   public boolean setTarget(TeleporterTarget target) {
      this.targets.put(target, "Target " + INDEX.getAndIncrement());
      this.updateGuiField("targets");
      this.setActive(this.targets.size() > 0);
      return true;
   }

   @Override
   public boolean hasTarget(TeleporterTarget target) {
      return this.targets.contains(target);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("targets", this.targets.write(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.targets.read(compound.getCompound("targets"));
   }

   public static class TargetStorage implements INetworkDataBuffer {
      Object2ObjectMap<TeleporterTarget, String> targets = CollectionUtils.<TeleporterTarget, String>createLinkedMap();
      Set<TeleporterTarget> custom = CollectionUtils.<TeleporterTarget>createSet();
      List<Tuple<TeleporterTarget, String>> clientTargets = CollectionUtils.<Tuple<TeleporterTarget, String>>createList();

      public int size() {
         return this.targets.size();
      }

      public boolean contains(TeleporterTarget target) {
         return this.targets.containsKey(target);
      }

      public void put(TeleporterTarget target, String defaultName) {
         String name = TargetRegistry.getRegistry().getTargetName(target);
         this.targets.put(target, name != null ? name : defaultName);
      }

      public void updateName(TeleporterTarget target) {
         if (this.targets.containsKey(target) && !this.custom.contains(target)) {
            String name = TargetRegistry.getRegistry().getTargetName(target);
            if (name != null) {
               this.targets.put(target, name);
            }
         }
      }

      public void setCustomName(TeleporterTarget target, String name) {
         if (this.targets.containsKey(target)) {
            this.custom.add(target);
            this.targets.put(target, name);
         }
      }

      public boolean removeTarget(TeleporterTarget target) {
         if (this.targets.remove(target) != null) {
            this.custom.remove(target);
            return true;
         } else {
            return false;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public List<Tuple<TeleporterTarget, String>> getClientTargets() {
         return this.clientTargets;
      }

      @Override
      public void write(IOutputBuffer buffer) {
         buffer.writeShort((short)this.targets.size());
         ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.targets).iterator();

         while (var2.hasNext()) {
            Entry<TeleporterTarget, String> entry = (Entry<TeleporterTarget, String>)var2.next();
            buffer.writeString((String)entry.getValue());
            ((TeleporterTarget)entry.getKey()).write(buffer);
         }
      }

      @Override
      public void read(IInputBuffer buffer) {
         List<Tuple<TeleporterTarget, String>> newClientTargets = CollectionUtils.<Tuple<TeleporterTarget, String>>createList();
         Object2ObjectMap<TeleporterTarget, String> newTargets = CollectionUtils.<TeleporterTarget, String>createLinkedMap();
         int size = buffer.readShort();

         for (int i = 0; i < size; i++) {
            String name = buffer.readString();
            TeleporterTarget target = TeleporterTarget.readFromBuffer(buffer);
            if (target != null) {
               newClientTargets.add(new Tuple(target, name));
               newTargets.put(target, name);
            }
         }

         this.clientTargets = newClientTargets;
         this.targets = newTargets;
      }

      public CompoundTag write(CompoundTag nbt) {
         ListTag list = new ListTag();
         ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.targets).iterator();

         while (var3.hasNext()) {
            Entry<TeleporterTarget, String> entry = (Entry<TeleporterTarget, String>)var3.next();
            CompoundTag data = new CompoundTag();
            data.put("target", ((TeleporterTarget)entry.getKey()).write(new CompoundTag()));
            data.putString("name", (String)entry.getValue());
            data.putBoolean("custom", this.custom.contains(entry.getKey()));
            list.add(data);
         }

         nbt.put("data", list);
         return nbt;
      }

      public void read(CompoundTag nbt) {
         for (CompoundTag entry : NBTListWrapper.wrap(nbt.getList("data", 10), CompoundTag.class)) {
            TeleporterTarget target = TeleporterTarget.read(entry.getCompound("target"));
            if (target != null) {
               this.targets.put(target, entry.getString("name"));
               if (nbt.getBoolean("custom")) {
                  this.custom.add(target);
               }
            }
         }
      }
   }
}
