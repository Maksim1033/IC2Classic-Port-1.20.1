package ic2.core.block.machines.tiles.hv;

import com.mojang.authlib.GameProfile;
import ic2.api.core.IC2Classic;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.components.hv.villager.VillagerSelectorComponent;
import ic2.core.block.machines.containers.hv.VillagerOMatContainer;
import ic2.core.block.machines.logic.villager.VillagerList;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.inventory.transporter.transporters.special.DirectionalTransporter;
import ic2.core.networking.buffers.data.VillagerBuffer;
import ic2.core.platform.corehacks.mixins.server.LevelMixin;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class VillagerOMatTileEntity extends BaseElectricTileEntity implements ITileGui, ITickListener, INetworkDataEventListener, IAreaOfEffect {
   public UUID owner = null;
   @NetworkInfo
   public VillagerList trades = new VillagerList();
   @NetworkInfo
   public Direction importDir = Direction.NORTH;
   @NetworkInfo
   public Direction exportDir = Direction.SOUTH;
   @NetworkInfo
   public int storedXP;
   public int visualId = -1;
   ICache<IItemHandler> invCache = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.ITEM_HANDLER);
   ICache<ITube> tubeCache = new CapabilityCache<>(this, DirectionList.ALL, IC2Classic.TUBE_CAPABILITY);

   public VillagerOMatTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 512, 15000000);
      this.addCaches(new ICache[]{this.invCache, this.tubeCache});
      this.addCapability(ForgeCapabilities.ITEM_HANDLER, EmptyHandler.INSTANCE);
      this.addGuiFields(new String[]{"trades", "importDir", "exportDir", "storedXP"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.VILLAGER_O_MAT;
   }

   @Override
   public boolean supportsNotify() {
      return false;
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
   public void load(CompoundTag compound) {
      super.load(compound);
      this.trades.load(compound);
      this.importDir = Direction.from3DDataValue(compound.getInt("importdir"));
      this.exportDir = Direction.from3DDataValue(compound.getInt("exportdir"));
      this.storedXP = compound.getInt("xp");
      this.owner = compound.hasUUID("owner") ? compound.getUUID("owner") : null;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      this.trades.save(compound);
      compound.putByte("importdir", (byte)this.importDir.get3DDataValue());
      compound.putByte("exportdir", (byte)this.exportDir.get3DDataValue());
      compound.putInt("xp", this.storedXP);
      if (this.owner != null) {
         compound.putUUID("owner", this.owner);
      }
   }

   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("trades")) {
         this.getContainer(player, VillagerOMatContainer.class, T -> T.getComponent(VillagerSelectorComponent.class).reloadVillagerData());
      }
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (data instanceof VillagerBuffer buffer && target.isDedicatedServer() && this.trades.loadData(buffer.getId(), buffer.getIndex(), buffer.getState())) {
         this.updateGuiField("trades");
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.importDir = Direction.from3DDataValue(value);
         this.updateGuiField("importDir");
      } else if (key == 1) {
         this.exportDir = Direction.from3DDataValue(value);
         this.updateGuiField("exportDir");
      } else if (key == 2) {
         entity.giveExperiencePoints(this.storedXP);
         this.storedXP = 0;
         this.updateGuiField("storedXP");
      } else if (key == 3) {
         Entity type = (Entity)((LevelMixin)this.getLevel()).getEntityAccessor().get(value);
         if (type instanceof Villager villager) {
            villager.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
            entity.closeContainer();
         }
      }
   }

   private void acceptXP(int amount) {
      this.storedXP += amount;
      if (this.storedXP > 100000) {
         this.storedXP = 100000;
      }

      this.updateGuiField("storedXP");
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new VillagerOMatContainer(this, player, windowID);
   }

   @Override
   public AABB getAreaOfEffect() {
      return new AABB(this.worldPosition).inflate(15.0);
   }

   @Override
   public int getAreaOfEffectColor() {
      return -2131340288;
   }

   @Override
   public void setVisualizationId(int id) {
      this.visualId = id;
   }

   @Override
   public int getVisualizationId() {
      return this.visualId;
   }

   @Override
   public void onTick() {
      if (this.clock(80)) {
         LevelEntityGetter<Entity> getter = ((LevelMixin)this.getLevel()).getEntityAccessor();
         Player trader = this.getOwner();
         if (this.trades.update(this.getLevel().getEntitiesOfClass(Villager.class, new AABB(this.worldPosition).inflate(15.0)), trader)) {
            this.updateGuiField("trades");
         }

         if (this.clock(1200) && this.hasEnergy(this.trades.getActiveTrades() * 6000)) {
            IItemTransporter output = this.get(this.exportDir);
            IItemTransporter input = TransporterManager.getTransporter(this.invCache.getHandler(this.importDir));
            if (output == null || input == null) {
               return;
            }

            if (this.trades
               .trade(
                  getter,
                  trader,
                  new DirectionalTransporter(input, this.importDir.getOpposite()),
                  new DirectionalTransporter(output, this.exportDir.getOpposite()),
                  this::acceptXP
               )) {
               IC2.AUDIO.playSound(this, IC2Sounds.TRADED_SOUND);
               this.useEnergy(this.trades.getActiveTrades() * 6000);
            }
         }
      }
   }

   private IItemTransporter get(Direction dir) {
      return this.tubeCache.contains(dir)
         ? TransporterManager.getTransporter(this.tubeCache.getHandler(dir))
         : TransporterManager.getTransporter(this.invCache.getHandler(dir));
   }

   public Player getOwner() {
      if (this.getLevel() instanceof ServerLevel levelInstance) {
         if (this.owner == null) {
            return FakePlayerFactory.getMinecraft(levelInstance);
         } else {
            Player player = levelInstance.getServer().getPlayerList().getPlayer(this.owner);
            return (Player)(player != null ? player : FakePlayerFactory.get(levelInstance, this.getProfile()));
         }
      } else {
         return null;
      }
   }

   public GameProfile getProfile() {
      return new GameProfile(this.owner, "Trader [" + this.owner.toString() + "]");
   }
}
