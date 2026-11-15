package ic2.core.block.machines.tiles.hv;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IScanner;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.hv.OreScannerContainer;
import ic2.core.block.machines.logic.scanner.ScannerHighlighter;
import ic2.core.block.machines.logic.scanner.ScannerInventory;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.networking.PacketManager;
import ic2.core.networking.buffers.data.HighlightBuffer;
import ic2.core.networking.packets.client.TileDataBufferEventPacket;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.PositionedRegion;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;

public class OreScannerTileEntity
   extends BaseElectricTileEntity
   implements ITickListener,
   INotifyInventory,
   ITileGui,
   INetworkDataEventListener,
   ITileActivityProvider {
   @NetworkInfo
   public int offset = 0;
   public ScannerInventory results = new ScannerInventory(5, this);
   public Set<Block> ignoreBlocks = CollectionUtils.<Block>createSet();
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int state = 0;
   @NetworkInfo
   public int maxBlocks = 0;
   @NetworkInfo
   public int blocks = 0;
   public int index = 0;
   public List<PositionedRegion> regionsToCheck = CollectionUtils.<PositionedRegion>createList();
   ScannerHighlighter highlighter = new ScannerHighlighter(this);

   public OreScannerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 2, 512, 50000);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"offset", "state", "maxBlocks", "blocks"});
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   @Override
   public boolean allowsUI() {
      return false;
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ORE_SCANNER;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("results", this.results.save(new CompoundTag()));
      compound.putShort("offset", (short)this.offset);
      compound.putByte("state", (byte)this.state);
      compound.putInt("max", this.maxBlocks);
      compound.putInt("current", this.blocks);
      compound.putInt("index", this.index);
      ListTag list = new ListTag();

      for (Block block : this.ignoreBlocks) {
         list.add(StringTag.valueOf(ForgeRegistryWrapper.getBlockKey(block).toString()));
      }

      compound.put("ignored", list);
   }

   @Override
   public void load(CompoundTag compound) {
      this.results.load(compound.getCompound("results"));
      this.offset = compound.getInt("offset");
      this.state = compound.getInt("state");
      this.maxBlocks = compound.getInt("max");
      this.blocks = compound.getInt("current");
      this.index = compound.getInt("index");

      for (StringTag nbt : NBTListWrapper.wrap(compound.getList("ignored", 8), StringTag.class)) {
         Block block = ForgeRegistryWrapper.getBlockValue(new ResourceLocation(nbt.getAsString()));
         if (block != Blocks.AIR) {
            this.ignoreBlocks.add(block);
         }
      }

      super.load(compound);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new OreScannerContainer(this, player, windowID);
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
      if (slot == 1 && this.isSimulating()) {
         this.interrupt();
      }
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (target.isClient() && data instanceof HighlightBuffer buffer) {
         this.highlighter.clearOrSet(buffer.getList(), buffer.getColor());
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         if (((ItemStack)this.inventory.get(1)).getItem() instanceof IScanner && this.state != 1) {
            this.state = 1;
            this.regionsToCheck.addAll(PositionedRegion.create(this.getLevel(), Box.withRange(this.getBlockPos(), 128)));
            this.index = 0;
            this.blocks = 0;
            this.maxBlocks = this.regionsToCheck.size() * 256;
            this.results.clearData();
            this.ignoreBlocks.clear();
            this.updateGuiFields(new String[]{"state", "offset", "blocks", "maxBlocks"});
         }
      } else if (key == 1) {
         this.offset = Mth.clamp(this.offset + value, 0, Math.max(0, this.results.resultSize() - 5));
         this.results.updateSelection();
         this.updateGuiField("offset");
      }
   }

   public void displayIndexes(Player player, int index) {
      if (!this.isRendering()) {
         LongList list = this.results.getBlocks(index);
         if (!list.isEmpty()) {
            PacketManager.INSTANCE.sendToPlayer(new TileDataBufferEventPacket(this, "", new HighlightBuffer(list, this.results.getColor(index)), true), player);
         }
      }
   }

   @Override
   public void onTick() {
      this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      if (this.energy > 0 && !((ItemStack)this.inventory.get(1)).isEmpty()) {
         this.useEnergy(ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(1), this.energy, this.tier, false, false));
      }

      if (this.state == 1 && this.hasEnergy(1000)) {
         this.setActive(true);
         ItemStack stack = (ItemStack)this.inventory.get(1);
         if (stack.getItem() instanceof IScanner scanner) {
            if (this.regionsToCheck.isEmpty()) {
               int radius = scanner.getScanRadius((ItemStack)this.inventory.get(1), false) * 2 + 1;
               this.regionsToCheck.addAll(PositionedRegion.create(this.getLevel(), Box.withRange(this.getBlockPos(), radius)));
            } else {
               this.collectBlocks(this.results.getPositionMap(), this.ignoreBlocks, scanner, stack);
               this.index++;
               this.blocks += 256;
               this.useEnergy(100);
               this.updateGuiField("blocks");
               if (this.blocks == this.maxBlocks) {
                  this.state = 2;
                  this.results.populate();
                  this.ignoreBlocks.clear();
                  this.regionsToCheck.clear();
                  this.index = 0;
                  this.updateGuiFields(new String[]{"state", "offset"});
               } else if (this.clock(20)) {
                  int last = this.offset;
                  this.results.populate();
                  this.offset = last;
                  this.results.updateSelection();
               }

               this.handleComparators();
            }
         } else {
            this.interrupt();
            this.handleComparators();
         }
      } else {
         this.setActive(false);
         this.handleComparators();
      }
   }

   protected void interrupt() {
      if (this.state == 1) {
         this.state = 0;
         this.results.clearData();
         this.ignoreBlocks.clear();
         this.regionsToCheck.clear();
         this.index = 0;
         this.updateGuiFields(new String[]{"state", "offset"});
      }
   }

   public void collectBlocks(Map<Block, LongList> positions, Set<Block> ignore, IScanner scanner, ItemStack stack) {
      if (this.index < this.regionsToCheck.size()) {
         for (Entry<Block, LongList> entry : this.regionsToCheck.get(this.index).getBlockPositions().entrySet()) {
            Block block = entry.getKey();
            if (!ignore.contains(block)) {
               LongList list = positions.get(block);
               if (list != null) {
                  list.addAll(entry.getValue());
               } else if (this.isValidState(block, scanner, stack)) {
                  positions.put(block, new LongArrayList(entry.getValue()));
               } else {
                  ignore.add(block);
               }
            }
         }
      }
   }

   public boolean isValidState(Block block, IScanner scanner, ItemStack stack) {
      BlockState state = block.defaultBlockState();
      return !state.isAir() && scanner.isOreValuable(stack, state);
   }
}
