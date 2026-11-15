package ic2.core.block.base.tiles;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkEventListener;
import ic2.api.network.tile.PacketRange;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.multiblock.IMultiBlockClickable;
import ic2.core.block.base.features.multiblock.IStructureListener;
import ic2.core.block.base.misc.LinkStorage;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.platform.events.MultiBlockManager;
import ic2.core.platform.events.StructureManager;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.capabilities.IToggleableCapabilityProvider;
import ic2.core.utils.helpers.capabilities.StructureCapabilityProvider;
import ic2.core.utils.math.StructureBuilder;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public abstract class BaseMultiBlockTileEntity extends BaseInventoryTileEntity implements IStructureListener, ITickListener, INetworkEventListener {
   @NetworkInfo
   public boolean isValid = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public BaseMultiBlockTileEntity.StructurePos structure = new BaseMultiBlockTileEntity.StructurePos();
   @NetworkInfo
   public LinkStorage children = new LinkStorage("children", this);
   protected boolean registered = false;
   protected boolean dynamic = false;
   protected Box boundingBox;
   Map<Capability<?>, IToggleableCapabilityProvider> capabilities = CollectionUtils.<Capability<?>, IToggleableCapabilityProvider>createMap();

   public BaseMultiBlockTileEntity(BlockPos pos, BlockState state, int size) {
      super(pos, state, size);
      this.addNetworkFields(new String[]{"isValid", "structure"});
      this.addCapability(ForgeCapabilities.ITEM_HANDLER, (IToggleableCapabilityProvider)this.handler);
   }

   @Override
   public void addCapability(Capability<?> cap, Object value) {
      this.addCapability(cap, (IToggleableCapabilityProvider)StructureCapabilityProvider.create(cap, value));
   }

   public void addCapability(Capability<?> cap, IToggleableCapabilityProvider provider) {
      this.capabilities.put(cap, provider);
   }

   @Override
   public void removeCapability(Capability<?> cap) {
      IToggleableCapabilityProvider entry = this.capabilities.remove(cap);
      if (entry != null) {
         entry.setActive(false);
      }
   }

   @Override
   public <T> T getInternalCapability(Capability<T> cap) {
      IToggleableCapabilityProvider entry = this.capabilities.get(cap);
      return (T)(entry != null ? entry.getCapability(cap).orElse(null) : null);
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      IToggleableCapabilityProvider entry = this.capabilities.get(cap);
      return entry != null ? entry.getCapability(cap, side) : super.getCapability(cap, side);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "structure_valid", this.isValid, true);
      if (this.boundingBox != null) {
         compound.put("bounds", this.boundingBox.saveRelative());
      }

      this.structure.save(compound);
      this.children.save(compound);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.isValid = NBTUtils.getBoolean(compound, "structure_valid", true);
      this.boundingBox = compound.contains("bounds") ? Box.relative(compound.getByteArray("bounds")) : null;
      this.structure.load(compound);
      this.children.load(compound);
   }

   public abstract boolean isStateStillValid(BlockPos var1, BlockPos var2, BlockState var3);

   public abstract boolean testStructure(StructureBuilder var1);

   public void onStructureInvalidated(boolean wasValid, boolean removeMaster) {
      this.children.clear(wasValid);
   }

   public void onStructureValidated(boolean revalidation) {
      if (revalidation) {
         this.children.refreshReferences();
      } else {
         this.children.finish();
         this.onInitialValidation();
         this.sendToClient(0, 0, PacketRange.CHUNK_TRACKED);
      }
   }

   public void onStructureTick() {
   }

   protected void onInitialValidation() {
   }

   public boolean doesNeedTickAfterValidation() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return this.boundingBox != null ? this.boundingBox.toInclusiveAxis() : super.getRenderBoundingBox();
   }

   public Box getBox() {
      return !this.isValid ? new Box() : this.boundingBox.copy();
   }

   public boolean isDynamic() {
      return this.dynamic;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isValid") || fields.contains("structure")) {
         if (this.isValid && this.structure.structurePositions != null) {
            if (this instanceof IMultiBlockClickable) {
               MultiBlockManager.INSTANCE.addListeners(this.getLevel(), this.worldPosition, this.structure.structurePositions, (IMultiBlockClickable)this);
            }

            this.boundingBox = Box.fromPosArray(this.structure.structurePositions);
            StructureManager.INSTANCE.addListeners(this.getLevel(), LongArrayList.wrap(this.structure.structurePositions), this);
         } else {
            MultiBlockManager.INSTANCE.removeListeners(this.getLevel(), this.worldPosition);
            StructureManager.INSTANCE.clearListeners(this.getLevel(), this);
            this.boundingBox = null;
         }
      }
   }

   @Override
   public void onServerDataReceived(int key, int value) {
      if (key == 0) {
         BlockHighlighter.INSTANCE.addHighlight(this.getBlockPos(), 100, 2013265920, true, T -> {
            if (this.isRemoved()) {
               return false;
            } else if (this.boundingBox == null) {
               return true;
            } else {
               T.renderBox = this.boundingBox.toInclusiveAxis();
               return true;
            }
         });
      }
   }

   @Override
   public void markStructureDirty(BlockPos pos, BlockState newState) {
      if (!this.isStateStillValid(pos, this.toRelativePosition(pos), newState)) {
         this.invalidateStructure();
      }
   }

   @Override
   public void handleRedstone() {
      if (this.needsRedstoneUpdate && this.sensitive) {
         this.needsRedstoneUpdate = false;
         if (this.directionalSignal) {
            byte max = 0;

            for (Direction dir : DirectionList.ALL) {
               byte strength = (byte)Mth.clamp(this.getLevel().getSignal(this.getBlockPos(), dir), 0, 15);
               max = (byte)Math.max(max, strength);
               this.sidedSignals[dir.get3DDataValue()] = strength;
            }

            for (BaseLinkingTileEntity tile : this.children.getTiles()) {
               for (Direction dir : DirectionList.ALL) {
                  byte strength = (byte)Mth.clamp(this.getLevel().getSignal(tile.getBlockPos(), dir), 0, 15);
                  max = (byte)Math.max(max, strength);
                  this.sidedSignals[dir.get3DDataValue()] = strength;
               }
            }

            this.signal = max;
            return;
         }

         this.signal = (byte)Mth.clamp(this.getLevel().getBestNeighborSignal(this.worldPosition), 0, 15);
         if (this.signal >= 15) {
            return;
         }

         List<BaseLinkingTileEntity> tiles = this.children.getTiles();
         int i = 0;

         for (int m = tiles.size(); i < m && this.signal < 15; i++) {
            this.signal = (byte)Math.max(this.signal, Mth.clamp(this.getLevel().getBestNeighborSignal(tiles.get(i).getBlockPos()), 0, 15));
         }
      }
   }

   @Override
   protected boolean handleComparators(boolean ignoreTick) {
      boolean result = super.handleComparators(ignoreTick);
      if (result && this.isSimulating()) {
         for (BaseLinkingTileEntity tile : this.children.getTiles()) {
            this.getLevel().updateNeighbourForOutputSignal(tile.getBlockPos(), tile.getBlockState().getBlock());
         }
      }

      return result;
   }

   protected final BlockPos toRelativePosition(BlockPos origin) {
      return origin.subtract(this.getBlockPos());
   }

   @Override
   public void onTick() {
      if ((!this.isValid || this.dynamic) && this.clock(512)) {
         this.checkStructure();
      }

      if (this.isValid) {
         this.onStructureTick();
         this.handleComparators();
      }
   }

   @Override
   public void onLoaded() {
      if (this.isSimulating()) {
         this.checkStructure();
      }

      super.onLoaded();
   }

   @Override
   public void onUnloaded(boolean chunk) {
      for (IToggleableCapabilityProvider provider : this.capabilities.values()) {
         provider.setActive(false);
      }

      this.capabilities.clear();
      if (this.isValid) {
         StructureManager.INSTANCE.clearListeners(this.getLevel(), this);
         if (this.isSimulating()) {
            this.registered = false;
            if (!chunk) {
               this.onStructureInvalidated(true, true);
            }
         }

         MultiBlockManager.INSTANCE.removeListeners(this.getLevel(), this.worldPosition);
      }

      super.onUnloaded(chunk);
   }

   protected void checkStructure() {
      boolean skip = false;
      if (this.registered) {
         StructureManager.INSTANCE.clearListeners(this.getLevel(), this);
         this.registered = false;
         skip = this.dynamic;
      }

      if ((this.isValid || this.structure.structurePositions != null) && !skip) {
         this.isValid = !StructureManager.INSTANCE.isBlocked(this.getLevel(), LongArrayList.wrap(this.structure.structurePositions));
         if (this.isValid) {
            if (this.boundingBox == null) {
               this.boundingBox = Box.fromPosArray(this.structure.structurePositions)
                  .offset(-this.worldPosition.getX(), -this.worldPosition.getY(), -this.worldPosition.getZ());
            }

            this.validateStructure(true);
         }
      } else {
         StructureBuilder builder = new StructureBuilder(this.getLevel(), this.getBlockPos(), this.getFacing().getOpposite());
         if (this.testStructure(builder)) {
            this.structure.setStructurePos(builder.getPositions());
            this.boundingBox = Box.fromPosArray(this.structure.structurePositions)
               .offset(-this.worldPosition.getX(), -this.worldPosition.getY(), -this.worldPosition.getZ());
            this.isValid = !StructureManager.INSTANCE.isBlocked(this.getLevel(), LongArrayList.wrap(this.structure.structurePositions));
            if (this.isValid) {
               this.validateStructure(false);
            }

            this.updateTileFields(new String[]{"structure", "isValid"});
         } else {
            this.invalidateStructure();
         }
      }
   }

   protected void validateStructure(boolean revalidate) {
      StructureManager.INSTANCE.addListeners(this.getLevel(), LongArrayList.wrap(this.structure.structurePositions), this);
      if (this instanceof IMultiBlockClickable) {
         MultiBlockManager.INSTANCE.addListeners(this.getLevel(), this.worldPosition, this.structure.structurePositions, (IMultiBlockClickable)this);
      }

      for (IToggleableCapabilityProvider provider : this.capabilities.values()) {
         provider.setActive(true);
      }

      if (!this.doesNeedTickAfterValidation()) {
         this.removeFromTick();
      }

      this.onStructureValidated(revalidate);
      this.registered = true;
      this.notifyChanges(false, DirectionList.ALL);
   }

   protected void invalidateStructure() {
      boolean lastValid = this.isValid;
      if (this.isValid) {
         StructureManager.INSTANCE.clearListeners(this.getLevel(), this);
         MultiBlockManager.INSTANCE.removeListeners(this.getLevel(), this.worldPosition);
         this.registered = false;
         this.isValid = false;
         this.updateTileField("isValid");
         if (!this.doesNeedTickAfterValidation()) {
            this.addToTick();
         }
      }

      for (IToggleableCapabilityProvider provider : this.capabilities.values()) {
         provider.setActive(false);
      }

      this.structure.setStructurePos(null);
      this.boundingBox = null;
      this.updateTileField("structure");
      this.onStructureInvalidated(lastValid, false);
      this.notifyChanges(false, DirectionList.ALL);
   }

   public class StructurePos implements INBTSavable, INetworkDataBuffer {
      public long[] structurePositions;

      public void setStructurePos(long[] array) {
         this.structurePositions = array;
         if (this.structurePositions != null) {
            LongArrays.quickSort(array, this::sort);
         }
      }

      private int sort(long key, long value) {
         int result = Integer.compare(BlockPos.getZ(key), BlockPos.getZ(value));
         if (result != 0) {
            return result;
         } else {
            result = Integer.compare(BlockPos.getY(key), BlockPos.getY(value));
            return result != 0 ? result : Integer.compare(BlockPos.getX(key), BlockPos.getX(value));
         }
      }

      public boolean isFullyUsed() {
         return this.structurePositions == null
            ? false
            : BaseMultiBlockTileEntity.this.boundingBox != null && BaseMultiBlockTileEntity.this.boundingBox.size() - 1 == this.structurePositions.length;
      }

      public boolean isFrameUsed() {
         return this.structurePositions == null
            ? false
            : BaseMultiBlockTileEntity.this.boundingBox != null && BaseMultiBlockTileEntity.this.boundingBox.hollowSize() - 1 == this.structurePositions.length;
      }

      @Override
      public void write(IOutputBuffer buffer) {
         if (this.structurePositions == null) {
            buffer.writeByte((byte)0);
         } else if (this.isFullyUsed()) {
            buffer.writeByte((byte)1);
            buffer.writeBytes(BaseMultiBlockTileEntity.this.boundingBox.saveRelative().getAsByteArray());
         } else if (this.isFrameUsed()) {
            buffer.writeByte((byte)2);
            buffer.writeBytes(BaseMultiBlockTileEntity.this.boundingBox.saveRelative().getAsByteArray());
         } else {
            buffer.writeByte((byte)3);
            buffer.writeVarInt(this.structurePositions.length);

            for (long pos : this.structurePositions) {
               buffer.writeByte((byte)(BlockPos.getX(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getX()));
               buffer.writeByte((byte)(BlockPos.getY(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getY()));
               buffer.writeByte((byte)(BlockPos.getZ(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getZ()));
            }
         }
      }

      @Override
      public void read(IInputBuffer buffer) {
         byte value = buffer.readByte();
         if (value == 1) {
            this.structurePositions = Box.relative(buffer.readBytes()).asLongArray(BaseMultiBlockTileEntity.this.getBlockPos());
         } else if (value == 2) {
            this.structurePositions = Box.relative(buffer.readBytes()).asHollowArray(BaseMultiBlockTileEntity.this.getBlockPos());
         } else if (value == 3) {
            int size = buffer.readVarInt();
            this.structurePositions = new long[size];
            long base = BaseMultiBlockTileEntity.this.getBlockPos().asLong();

            for (int i = 0; i < size; i++) {
               this.structurePositions[i] = BlockPos.offset(base, buffer.readByte(), buffer.readByte(), buffer.readByte());
            }
         }
      }

      @Override
      public CompoundTag save(CompoundTag nbt) {
         if (this.structurePositions == null) {
            nbt.remove("structure_pos");
         } else if (this.isFullyUsed()) {
            nbt.putBoolean("fullyused", true);
         } else if (this.isFrameUsed()) {
            nbt.putBoolean("frameused", true);
         } else {
            byte[] data = new byte[this.structurePositions.length * 3];
            int i = 0;

            for (int m = this.structurePositions.length; i < m; i++) {
               long pos = this.structurePositions[i];
               data[i * 3] = (byte)(BlockPos.getX(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getX());
               data[i * 3 + 1] = (byte)(BlockPos.getY(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getY());
               data[i * 3 + 2] = (byte)(BlockPos.getZ(pos) - BaseMultiBlockTileEntity.this.getBlockPos().getZ());
            }

            nbt.putByteArray("structure_pos", data);
            nbt.putByte("structureSize", (byte)this.structurePositions.length);
         }

         return nbt;
      }

      @Override
      public void load(CompoundTag nbt) {
         if (nbt.contains("fullyused")) {
            this.structurePositions = BaseMultiBlockTileEntity.this.boundingBox.asLongArray(BaseMultiBlockTileEntity.this.getBlockPos());
         } else if (nbt.contains("frameused")) {
            this.structurePositions = BaseMultiBlockTileEntity.this.boundingBox.asHollowArray(BaseMultiBlockTileEntity.this.getBlockPos());
         } else {
            byte[] array = nbt.getByteArray("structure_pos");
            if (array.length > 0) {
               long base = BaseMultiBlockTileEntity.this.getBlockPos().asLong();
               this.structurePositions = new long[array.length / 3];
               int i = 0;

               for (int m = array.length / 3; i < m; i++) {
                  this.structurePositions[i] = BlockPos.offset(base, array[i * 3], array[i * 3 + 1], array[i * 3 + 2]);
               }
            }
         }
      }
   }
}
