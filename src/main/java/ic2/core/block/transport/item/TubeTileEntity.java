package ic2.core.block.transport.item;

import ic2.api.blocks.PainterHelper;
import ic2.api.core.IC2Classic;
import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.api.network.tile.INetworkEventListener;
import ic2.api.network.tile.PacketRange;
import ic2.api.tiles.IAnchorTile;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import ic2.core.IC2;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.DirectionalCapability;
import ic2.core.block.base.cache.DirectionalCapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.block.transport.item.logic.TransportList;
import ic2.core.block.transport.item.logic.TransportSyncer;
import ic2.core.block.transport.item.logic.TubeSyncManager;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public abstract class TubeTileEntity
   extends BaseTileEntity
   implements IClickable,
   ITickListener,
   ILocation,
   ICamouflagable,
   ISpecialWrenchable,
   IAnchorTile,
   ITube,
   INetworkEventListener,
   INetworkDataEventListener,
   IDropProvider {
   public static final int MAX_MANAGED_ITEMS = 200;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int connectivity = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   protected int anchors = 0;
   @NetworkInfo
   public byte foamed = 0;
   @NetworkInfo
   public boolean synchronize = true;
   @NetworkInfo
   public CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);
   @NetworkInfo
   public TransportList items = new TransportList(200);
   public TransportSyncer sync = new TransportSyncer();
   public ObjectSet<TransportedItem> itemsToDelete = CollectionUtils.createSet();
   int maxSpeed = 5;
   int halfSpeed = 2;
   protected boolean addedToTubeNet = false;
   public ICache<IItemHandler> inventories = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.ITEM_HANDLER);
   public ICache<ITube> tubes = new DirectionalCapabilityCache<>(this, DirectionList.ALL, IC2Classic.TUBE_CAPABILITY, this::canConnectToTube);
   public DirectionalCapability<ITube> self = new DirectionalCapability<>(IC2Classic.TUBE_CAPABILITY, this);

   public TubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.clearNetworkFields();
      this.addNetworkFields(new String[]{"items", "connectivity", "anchors", "storage", "foamed", "synchronize"});
      if (this.useDefaultCache()) {
         this.addCaches(new ICache[]{this.inventories, this.tubes});
      }
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "connections", this.connectivity, 0);
      NBTUtils.putByte(compound, "anchors", this.anchors, 0);
      NBTUtils.putByte(compound, "foamed", this.foamed, 0);
      NBTUtils.putIf(compound, "camouflage", this.storage.save(new CompoundTag()), this.foamed > 0);
      NBTUtils.put(compound, "items", this.items.write());
      NBTUtils.putBoolean(compound, "sync_items", this.synchronize, true);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.connectivity = compound.getInt("connection");
      this.anchors = compound.getInt("anchors");
      this.items.read(compound.getList("items", 10));
      this.storage.load(compound.getCompound("camouflage"));
      this.foamed = compound.getByte("foamed");
      this.synchronize = NBTUtils.getBoolean(compound, "sync_items", true);
      if (!this.synchronize) {
         this.getNetworkFields().remove("items");
      }
   }

   protected boolean isRotatable() {
      return false;
   }

   public boolean useDefaultCache() {
      return true;
   }

   protected boolean canSetFacingInternal(Direction dir) {
      return false;
   }

   public int getPrioritySide() {
      return 0;
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      for (int i = 0; i < this.items.size(); i++) {
         ItemStack stack = ((TransportedItem)this.items.get(i)).getServerStack();
         if (!stack.isEmpty()) {
            StackUtil.expand(stack, stack.getCount(), drops);
         }
      }

      int count = DirectionList.ofNumber(this.anchors).size();
      if (count > 0) {
         drops.add(new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, count));
      }
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == IC2Classic.TUBE_CAPABILITY ? this.self.getCapability(cap, side) : super.getCapability(cap, side);
   }

   @Override
   public boolean needsUpdateTick() {
      return this.isRendering() || !this.items.isEmpty() || !this.canLoseUpdateTick();
   }

   public boolean canLoseUpdateTick() {
      return false;
   }

   public DirectionList getExtraFacings() {
      return DirectionList.EMPTY;
   }

   @Override
   public ITube.TubeType getTubeType() {
      return ITube.TubeType.SIMPLE;
   }

   public int getConnectivity() {
      return this.connectivity | this.anchors;
   }

   public int getValidDirections() {
      return this.connectivity;
   }

   public int getAnchors() {
      return this.anchors;
   }

   public void setMaxSpeed(int maxSpeed) {
      this.maxSpeed = Mth.clamp(maxSpeed, 1, 100);
      this.halfSpeed = Math.max(1, this.maxSpeed / 2);
   }

   public int getMaxSpeed() {
      return this.maxSpeed;
   }

   public int getHalfSpeed() {
      return this.halfSpeed;
   }

   public void onTubeUpdate() {
   }

   public void onItemEntered(TransportedItem item, Direction dir) {
   }

   public boolean onCenterReached(TransportedItem item) {
      return false;
   }

   public boolean onSideReached(TransportedItem item) {
      return false;
   }

   public boolean onLeftOver(TransportedItem item) {
      return false;
   }

   public DirectionList getValidDirections(TransportedItem item) {
      return DirectionList.ofNumber(this.connectivity).remove(item.getAttemptedDirections());
   }

   public boolean insertIntoInventory(IItemHandler handler, Direction flowDirection, TransportedItem item) {
      ItemStack stack = item.getStack();
      stack.shrink(TransporterManager.getTransporter(handler).addItem(stack, flowDirection.getOpposite(), false));
      return item.isInvalid();
   }

   protected int initTick(Level world) {
      if (!this.isRemoved()) {
         this.tick();
      }

      return 0;
   }

   @Override
   public void onTick() {
      if (this.isLoaded()) {
         this.onTubeUpdate();
         int i = 0;

         for (int m = Math.min(this.items.size(), 200); i < m; i++) {
            TransportedItem item = (TransportedItem)this.items.get(i);
            if (item.isInvalid()) {
               this.itemsToDelete.add(item);
            } else {
               item.update();
               if (item.hasReachedCenter()) {
                  if (!this.calculateNextDirection(item)) {
                     this.itemsToDelete.add(item);
                  }
               } else if (item.hasReachedEnd() && this.moveIntoTarget(item)) {
                  this.itemsToDelete.add(item);
               }
            }
         }

         if (!this.itemsToDelete.isEmpty()) {
            this.items.removeAll(this.itemsToDelete);
            this.sync.removeAll(this.itemsToDelete);
            this.itemsToDelete.clear();
         }

         if (this.isSimulating()) {
            if (this.sync.hasData()) {
               if (!this.synchronize) {
                  this.sync.clear();
               } else {
                  TubeSyncManager.markDirty(this, this.sync.serialize());
               }
            }

            if (this.items.isEmpty() && this.canLoseUpdateTick()) {
               this.removeFromTick();
            }
         }
      }
   }

   public void onSyncPacketReceived(byte[] data) {
      this.sync.deserialize(data, this, "request", this.items);
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (target.isDedicatedServer() && id.equals("request")) {
         if (!(data instanceof NBTBuffer buffer)) {
            return;
         }

         ByteBuf var14 = Unpooled.buffer();
         OutputBuffer var15 = new OutputBuffer(var14);
         IntSet set = new IntOpenHashSet(buffer.getNBT().getIntArray("data"));
         if (set.isEmpty()) {
            return;
         }

         int amount = 0;
         int i = 0;

         for (int m = this.items.size(); i < m; i++) {
            TransportedItem item = (TransportedItem)this.items.get(i);
            if (set.contains(item.getId())) {
               item.write(var15);
               amount++;
            }
         }

         byte[] bytes = new byte[var14.writerIndex()];
         var14.readBytes(bytes);
         CompoundTag nbt = new CompoundTag();
         nbt.putInt("size", amount);
         nbt.putByteArray("data", bytes);
         this.sendToPlayer(player, "requested_items", new NBTBuffer(nbt));
      } else if (id.equals("requested_items")) {
         if (!(data instanceof NBTBuffer buffer)) {
            return;
         }

         int size = buffer.getNBT().getInt("size");
         InputBuffer reader = new InputBuffer(buffer.getNBT().getByteArray("data"));

         for (int i = 0; i < size; i++) {
            this.items.add(TransportedItem.readFromNetwork(reader));
         }
      }
   }

   @Override
   public void onServerDataReceived(int key, int value) {
      if (key == 1) {
         int i = 0;

         for (int m = this.items.size(); i < m; i++) {
            TransportedItem item = (TransportedItem)this.items.get(i);
            if (item.getId() == value) {
               item.invalidate();
               return;
            }
         }
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("connectivity") || fields.contains("anchors") || fields.contains("storage") || fields.contains("foamed")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToTubeNet) {
         this.addedToTubeNet = true;
         TubeNet.INSTANCE.addTube(this);
      }

      if (this.isSimulating()) {
         this.tickFoamed();
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToTubeNet) {
         this.addedToTubeNet = false;
         TubeNet.INSTANCE.removeTube(this);
      }

      this.self.unload();
      super.onUnloaded(chunk);
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.getValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false))) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
      } else if (this.getValue(IC2Properties.LAVA_LOGGED, Boolean.valueOf(false))) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.LAVA, Fluids.LAVA.getTickDelay(this.getLevel()));
      }
   }

   @Override
   protected void onCachesUpdated() {
      this.connectivity = DirectionList.EMPTY
         .add(this.inventories.getPresentSides())
         .add(this.tubes.getPresentSides())
         .remove(DirectionList.ofNumber(this.anchors))
         .getCode();
      this.updateTileField("connectivity");
   }

   public void applySpeed(TransportedItem item) {
      Direction dir = item.getTravelingDirection();
      if (dir.getAxis().isHorizontal()) {
         item.updateSpeed(this.maxSpeed, 1);
      } else if (dir.getAxisDirection() == AxisDirection.NEGATIVE) {
         item.updateSpeed(100, 3);
      } else {
         item.updateSpeed(this.halfSpeed, 1);
      }
   }

   public boolean moveIntoTarget(TransportedItem item) {
      this.applySpeed(item);
      if (this.onSideReached(item)) {
         return false;
      } else {
         Direction dir = item.getTransferDirection();
         if (this.isRendering()) {
            ITube tube = (ITube)DirectionList.getNeighborCapability(this, dir, IC2Classic.TUBE_CAPABILITY).orElse(null);
            if (tube != null) {
               tube.addItem(item, dir.getOpposite());
            }

            return true;
         } else {
            ITube tube = this.tubes.getHandler(dir);
            if (tube != null && tube.canAddItem(item, dir.getOpposite())) {
               tube.addItem(item, dir.getOpposite());
               return true;
            } else {
               IItemHandler handler = this.inventories.getHandler(dir);
               if (handler != null && this.insertIntoInventory(handler, dir, item)) {
                  return true;
               } else if (this.onLeftOver(item)) {
                  return item.isInvalid();
               } else {
                  DirectionList list = this.getValidDirections(item);
                  if (list.isEmpty()) {
                     this.onItemLost(item, false);
                     StackUtil.pop(this.getLevel(), this.getBlockPos(), item.getStack());
                     this.sendToClient(1, item.getId(), PacketRange.CHUNK_TRACKED);
                     return true;
                  } else {
                     item.setInsertionDirection(item.getTransferDirection());
                     this.sync.markDirty(item);
                     return false;
                  }
               }
            }
         }
      }
   }

   public boolean calculateNextDirection(TransportedItem item) {
      this.applySpeed(item);
      if (this.onCenterReached(item)) {
         return !item.isInvalid();
      } else if (this.isRendering()) {
         return !this.getValidDirections(item).isEmpty();
      } else {
         DirectionList list = this.getValidDirections(item);

         for (Direction dir : list) {
            ITube tube = this.tubes.getHandler(dir);
            if ((tube == null || !tube.canAddItem(item, dir.getOpposite())) && this.inventories.getHandler(dir) == null) {
               list = list.remove(dir);
            }
         }

         if (list.isEmpty()) {
            this.onItemLost(item, true);
            StackUtil.pop(this.getLevel(), this.getBlockPos(), item.getStack());
            this.sendToClient(1, item.getId(), PacketRange.CHUNK_TRACKED);
            return false;
         } else {
            item.setExportDirection(list.getRandomFacing());
            this.sync.markDirty(item);
            return true;
         }
      }
   }

   public void onItemLost(TransportedItem item, boolean center) {
      if (item.getRequestId() != null && this.isSimulating()) {
         TubeNet.INSTANCE.onRequestLost(this, item.getServerStack(), item.getRequestId());
      }
   }

   @Override
   public void addItem(ItemStack item, Direction side, DyeColor color) {
      if (!item.isEmpty()) {
         this.addItem(new TransportedItem(item).setColor(color), side);
      }
   }

   @Override
   public void addItem(TransportedItem item, Direction side) {
      if (this.items.add(item)) {
         item.clearDirections();
         item.setInsertionDirection(side);
         if (this.isSimulating()) {
            if (this.items.size() < 200 && this.synchronize) {
               this.sync.markDirty(item);
            }

            if (!this.isTicking() || this.isRemoving()) {
               if (!this.isAdding()) {
                  IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), this::initTick);
               }

               this.addToTick();
            }
         }

         this.applySpeed(item);
         this.onItemEntered(item, side);
      }
   }

   @Override
   public boolean canAddItem(TransportedItem item, Direction side) {
      return true;
   }

   @Override
   public boolean canConnect(ITube other, Direction dir) {
      return (this.anchors & 1 << dir.get3DDataValue()) == 0;
   }

   protected boolean canConnectToTube(Direction dir, ITube tile) {
      return this.canConnect(tile, dir) && tile.canConnect(this, dir.getOpposite());
   }

   @Override
   public AABB hasSpecialAction(Direction side, Vec3 hit, Player player) {
      Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
      if (clicked == null) {
         return null;
      } else {
         return (!this.isRotatable() || !this.canSetFacingInternal(clicked)) && !this.hasAnchor(clicked) ? null : ITubeBlock.getHitBox(hit, side, 0.1875F);
      }
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
      if (clicked == null) {
         return false;
      } else if (this.isRotatable() && this.canSetFacingInternal(clicked)) {
         this.setFacing(clicked);
         return true;
      } else if (this.removeAnchor(clicked)) {
         Block.popResource(this.getLevel(), this.getBlockPos(), new ItemStack(IC2Blocks.MINING_PIPE_SHAFT));
         return true;
      } else {
         return false;
      }
   }

   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.VISIBLE_ITEMS) {
         this.toggleSync(!this.synchronize, new LongOpenHashSet());
         if (this.isSimulating()) {
            player.sendSystemMessage(
               this.translate(this.synchronize ? "tooltip.item.ic2.tube_tool.visible_items.enable" : "tooltip.item.ic2.tube_tool.visible_items.disable")
            );
         }

         return true;
      } else {
         return false;
      }
   }

   private void toggleSync(boolean value, LongSet tubes) {
      tubes.add(this.getBlockPos().asLong());
      this.synchronize = value;
      this.updateTileField("synchronize");
      if (this.synchronize) {
         this.addNetworkFields(new String[]{"items"});
         this.updateTileField("items");
      } else {
         this.getNetworkFields().remove("items");
      }

      for (Direction dir : DirectionList.ALL) {
         TubeTileEntity tile = DirectionList.getNeighborInterface(this, dir, TubeTileEntity.class);
         if (tile != null && !tubes.contains(tile.getBlockPos().asLong())) {
            tile.toggleSync(value, tubes);
         }
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem() == IC2Blocks.MINING_PIPE_SHAFT.asItem()) {
         Direction dir = ITubeBlock.isClickingAt(hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos())), side, 0.1875F);
         if (dir != null && this.addAnchor(dir)) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }
      } else if (stack.getItem() == IC2Blocks.CFOAM_WET.asItem()) {
         if (this.foamed == 0) {
            this.changeFoam((byte)1, false);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }
      } else {
         if (stack.is(ItemTags.SAND) && this.foamed == 1) {
            this.changeFoam((byte)2, false);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }

         if (this.foamed == 1) {
            BlockState dustState = Block.byItem(stack.getItem()).defaultBlockState();
            if (PainterHelper.CONCRETE_DUST.contains(dustState.getBlock())) {
               this.storage.setColor(DirectionList.ALL, PainterHelper.INSTANCE.getColor(dustState));
               this.changeFoam((byte)2, false);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean hasAnchor(Direction side) {
      return (this.anchors & 1 << side.get3DDataValue()) != 0;
   }

   @Override
   public boolean addAnchor(Direction dir) {
      if ((this.anchors & 1 << dir.get3DDataValue()) == 0) {
         this.anchors = this.anchors | 1 << dir.get3DDataValue();
         this.self.setActiveDirections(DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
         this.updateTileField("anchors");
         TubeNet.INSTANCE.updateTube(this);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean removeAnchor(Direction dir) {
      if ((this.anchors & 1 << dir.get3DDataValue()) != 0) {
         this.anchors = this.anchors & ~(1 << dir.get3DDataValue());
         this.self.setActiveDirections(DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
         this.updateTileField("anchors");
         TubeNet.INSTANCE.updateTube(this);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public CamouflageStorage getStorage() {
      return this.storage;
   }

   public boolean setColor(Direction dir, DyeColor color) {
      if (this.foamed == 2 && this.isSimulating() && this.storage.canApply(dir == null ? DirectionList.ALL : DirectionList.ofFacing(dir), color)) {
         this.storage.setColor(dir == null ? DirectionList.ALL : DirectionList.ofFacings(dir), color);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean applyTexture(Direction dir, RetextureEvent.TextureContainer container) {
      if (this.foamed == 2 && this.storage.canApply(dir, container)) {
         this.storage.setTexture(DirectionList.ofFacing(dir), container);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSideEnabled(Direction dir) {
      return this.foamed > 1;
   }

   @Override
   public boolean removeCamouflage() {
      return this.changeFoam((byte)0, false);
   }

   @Override
   public ItemLike getCamouflageDrop() {
      DyeColor color = this.storage.isSingleColor();
      return (ItemLike)(color == null ? Items.AIR : ColorMaps.CFOAM_BLOCKS.getBlock(color));
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      Builder builder = ModelData.builder().with(CableProperty.INSTANCE, this.connectivity << 6 | this.anchors);
      if (this.foamed == 2) {
         builder.with(
            CamouflageProperty.INSTANCE,
            (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
         );
      }

      DirectionList extra = this.getExtraFacings();
      if (!extra.isEmpty()) {
         builder.with(CableProperty.TUBE_EXTRA_SIDE, extra.getCode());
      }

      return builder.build();
   }

   public boolean changeFoam(byte level, boolean duringLoad) {
      if (this.foamed == level) {
         return false;
      } else {
         if (this.isSimulating()) {
            if ((this.foamed = level) == 1) {
               this.storage.setColor(DirectionList.ALL, DyeColor.LIGHT_GRAY);
               if (!duringLoad) {
                  this.updateTileField("storage");
               }

               this.tickFoamed();
            }

            if (!duringLoad) {
               this.updateTileField("foamed");
               this.onStateChanged();
            }
         }

         return true;
      }
   }

   private void tickFoamed() {
      if (this.foamed == 1) {
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), world -> {
            if (!this.isRemoved() && this.foamed == 1) {
               if (!world.dimension().equals(Level.NETHER)) {
                  if (world.getMaxLocalRawBrightness(this.worldPosition) * 6 > world.random.nextInt(1000)) {
                     this.changeFoam((byte)2, false);
                     return 0;
                  } else {
                     return 500;
                  }
               } else if (world.random.nextInt(5) == 0) {
                  this.changeFoam((byte)2, false);
                  return 0;
               } else {
                  return 20;
               }
            } else {
               return 0;
            }
         });
      }
   }
}
