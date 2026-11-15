package ic2.core.block.machines.tiles.mv;

import org.joml.Vector3f;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.block.base.features.personal.IPersonalWrenchable;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.components.mv.ChunkloaderComponent;
import ic2.core.block.machines.containers.mv.ChunkloaderContainer;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.MathUtils;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeChunkManager;

public class ChunkloaderTileEntity
   extends BaseElectricTileEntity
   implements ITickListener,
   ITileGui,
   BlockHighlighter.IHighlightController,
   ITileActivityProvider,
   IPersonalTile,
   IPersonalWrenchable {
   public static final int[] POWER_COST = new int[]{5, 15, 60, 300};
   @NetworkInfo
   public int radius = 1;
   @NetworkInfo
   public boolean doesChunkProcessing = false;
   public float animationProgress = 0.0F;
   @NetworkInfo
   UUID owner;
   @NetworkInfo
   public int mode = 1;

   public ChunkloaderTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 128, 60000);
      this.addNetworkFields(new String[]{"owner", "mode"});
      this.addGuiFields(new String[]{"radius", "doesChunkProcessing"});
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
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
   public BlockEntityType<?> createType() {
      return IC2Tiles.CHUNKLOADER;
   }

   @Override
   public boolean needsUpdateTick() {
      return true;
   }

   public AABB getRenderBoundingBox() {
      return new AABB(this.getBlockPos(), this.getBlockPos().offset(1, 1, 1));
   }

   @Override
   public boolean keepAlive(BlockHighlighter.Highlighter owner) {
      owner.ticksLeft = 100;
      return !this.isRemoved();
   }

   @Override
   public void setOwner(UUID owner) {
      if (this.owner == null && owner != null) {
         this.owner = owner;
      }
   }

   @Override
   public UUID getOwner() {
      return this.owner;
   }

   @Override
   public boolean canDoAction(UUID user, Action action, boolean ignorePublic) {
      if (this.owner == null) {
         this.setOwner(user);
         return true;
      } else if (this.owner.equals(user) || !ignorePublic && this.mode == 0 || IC2.PLATFORM.isOp(user)) {
         return true;
      } else if (this.mode == 2) {
         return false;
      } else {
         Friend friend = FriendManager.getFriends().getFriend(this.owner, user);
         return friend != null && friend.canApplyAction(action);
      }
   }

   public boolean isPublic() {
      return this.mode == 0;
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.canDoAction(player.getUUID(), Action.CHUNKLOADER_ACCESS, false);
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && this.canDoAction(player.getUUID(), Action.CHUNKLOADER_ACCESS, false);
   }

   @Override
   public boolean canSetFacing(Direction dir, Player player) {
      return dir != this.getFacing() && dir.getAxis().isHorizontal() && this.canDoAction(player.getUUID(), Action.CHUNKLOADER_EDIT, false);
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return this.canDoAction(player.getUUID(), Action.CHUNKLOADER_EDIT, false);
   }

   @Override
   public void onTick() {
      if (this.isRendering()) {
         if (this.isActive()) {
            this.animationProgress = this.animationProgress + (this.animationProgress < 1.0F ? 0.02F : 0.01F);
            if (this.animationProgress > 2.0F) {
               this.animationProgress--;
            }

            this.getLevel()
               .addParticle(
                  new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
                  (double)this.worldPosition.getY() + 0.5,
                  (double)this.worldPosition.getY() + 1.0,
                  (double)this.worldPosition.getZ() + 0.5,
                  0.10000000149011612,
                  0.10000000149011612,
                  0.10000000149011612
               );
         } else if (this.animationProgress > 1.0F) {
            this.animationProgress += 0.01F;
            if (this.animationProgress > 2.0F) {
               this.animationProgress = 1.0F;
            }
         } else if (this.animationProgress > 0.0F) {
            this.animationProgress = Math.max(0.0F, this.animationProgress - 0.02F);
         }
      } else {
         this.handleRedstone();
         if (this.isActive() && this.clock(4)) {
            ((ServerLevel)this.getLevel())
               .sendParticles(
                  ParticleTypes.PORTAL,
                  (double)this.worldPosition.getX() + 0.5,
                  (double)this.worldPosition.getY() + 1.5,
                  (double)this.worldPosition.getZ() + 0.5,
                  1,
                  0.0,
                  0.20000000298023224,
                  0.0,
                  0.20000000298023224
               );
         }

         int cost = POWER_COST[this.radius] * (this.doesChunkProcessing ? 2 : 1);
         if (!this.isActive() || this.hasEnergy(cost) && !this.isRedstonePowered()) {
            if (!this.isActive() && this.hasEnergy(Math.min(cost * 100, this.maxEnergy / 2)) && !this.isRedstonePowered()) {
               this.setActive(true);
               this.loadChunks();
            }
         } else {
            this.setActive(false);
            this.unloadChunks(false);
         }

         if (this.isActive()) {
            this.useEnergy(cost);
         }

         this.handleComparators();
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isRendering()) {
         this.animationProgress = this.isActive() ? 1.0F : 0.0F;
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.isActive() && !chunk) {
         this.unloadChunks(true);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("radius", this.radius);
      compound.putBoolean("processing", this.doesChunkProcessing);
      compound.putInt("mode", this.mode);
      if (this.owner != null) {
         compound.putUUID("owner", this.owner);
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.radius = compound.getInt("radius");
      this.doesChunkProcessing = compound.getBoolean("processing");
      this.mode = NBTUtils.getInt(compound, "mode", 1);
      if (compound.hasUUID("owner")) {
         this.owner = compound.getUUID("owner");
      }
   }

   private void loadChunks() {
      ChunkPos pos = new ChunkPos(this.worldPosition);

      for (int x = -this.radius; x <= this.radius; x++) {
         for (int z = -this.radius; z <= this.radius; z++) {
            ForgeChunkManager.forceChunk((ServerLevel)this.getLevel(), "ic2", this.worldPosition, x + pos.x, z + pos.z, true, this.doesChunkProcessing);
         }
      }

      this.setActive(true);
   }

   private void unloadChunks(boolean unload) {
      ChunkPos pos = new ChunkPos(this.worldPosition);

      for (int x = -this.radius; x <= this.radius; x++) {
         for (int z = -this.radius; z <= this.radius; z++) {
            ForgeChunkManager.forceChunk((ServerLevel)this.getLevel(), "ic2", this.worldPosition, x + pos.x, z + pos.z, false, this.doesChunkProcessing);
         }
      }

      if (!unload) {
         this.setActive(false);
      }
   }

   public int getRadius() {
      return this.radius;
   }

   private void setRadius(int radius) {
      if (radius >= 0 && radius <= 3 && this.radius != radius) {
         if (this.isActive()) {
            this.unloadChunks(false);
         }

         this.radius = radius;
         this.updateGuiField("radius");
         if (this.isActive()) {
            this.loadChunks();
         }
      }
   }

   private void setProcessing(boolean value) {
      if (value != this.doesChunkProcessing) {
         if (this.isActive()) {
            this.unloadChunks(false);
         }

         this.doesChunkProcessing = value;
         this.updateGuiField("doesChunkProcessing");
         if (this.isActive()) {
            this.loadChunks();
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         if (!this.canDoAction(entity.getUUID(), Action.CHUNKLOADER_EDIT, false)) {
            entity.displayClientMessage(this.translate("gui.ic2.chunkloader.permission"), false);
            return;
         }

         this.setRadius(MathUtils.getShortKey(value));
         this.setProcessing(MathUtils.getShortValue(value) != 0);
      } else if (key == 1) {
         if (entity.getUUID().equals(this.owner)) {
            this.mode = (this.mode + 1) % 3;
            this.updateTileField("mode");
         } else {
            entity.displayClientMessage(this.translate("gui.ic2.personal.perm"), false);
         }
      }
   }

   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("doesChunkProcessing") || fields.contains("radius")) {
         this.reSyncUI();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void reSyncUI() {
      AbstractContainerMenu c = Minecraft.getInstance().player.containerMenu;
      if (c instanceof ChunkloaderContainer) {
         ChunkloaderComponent comp = ((ChunkloaderContainer)c).getComponent(ChunkloaderComponent.class);
         if (comp != null) {
            comp.onSync();
         }
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ChunkloaderContainer(this, player, windowID);
   }
}
