package ic2.core.block.machines.tiles.mv;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropTile;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.mv.CropHarvesterContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.InventoryWrapper;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CropHarvesterTileEntity extends BaseElectricTileEntity implements ITickListener, ITileGui, IDropProvider, IAreaOfEffect {
   public static final AABBUtil.IBlockFilter FILTER = new AABBUtil.IBlockFilter() {
      @Override
      public boolean isValid(LevelReader world, BlockPos pos) {
         return world.getBlockEntity(pos) instanceof ICropTile;
      }

      @Override
      public boolean isValid(BlockState state) {
         return state.getBlock() == IC2Blocks.CROP_STICK;
      }
   };
   LongSet knownPositions = new LongOpenHashSet();
   LongPriorityQueue queue = new LongArrayFIFOQueue();
   LinkedList<ItemStack> items = new LinkedList<>();
   IItemTransporter transporter = null;
   @NetworkInfo
   public int radius = 1;
   int delay = 20;
   int networkId = -1;
   public SimpleInventory upgrades = new SimpleInventory(5).setMaxStackSize(1);

   public CropHarvesterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 9, 128, 5800);
      this.addNetworkFields(new String[]{"radius"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] array = MathUtils.fromTo(0, 9);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.EXPORT);
      handler.registerSlotAccess(AccessRule.EXPORT, array);
      handler.registerSlotsForSide(DirectionList.ALL, array);
      handler.registerNamedSlot(SlotType.OUTPUT, array);
   }

   @Override
   public boolean supportsNotify() {
      return true;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CROP_HARVESTER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CropHarvesterContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("upgrades", this.upgrades.save(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.upgrades.load(compound.getCompound("upgrades"));
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.updateUpgrades();
      }
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      super.addDrops(drops);
      this.upgrades.addToDrops(drops);
   }

   public void updateUpgrades() {
      int speed = 0;
      int rangeUpgrade = 1;
      int i = 0;

      for (int m = this.upgrades.getSlotCount(); i < m; i++) {
         Item item = this.upgrades.getStackInSlot(i).getItem();
         if (item == IC2Items.OVERCLOCKER_UPGRADE) {
            speed++;
         } else if (item == IC2Items.QUANTUM_OVERCLOCKER_UPGRADE) {
            speed += 2;
         } else if (item == IC2Items.BASIC_FIELD_PAD_UPGRADE) {
            rangeUpgrade++;
         } else if (item == IC2Items.FIELD_PAD_UPGRADE) {
            rangeUpgrade += 2;
         } else if (item == IC2Items.ADVANCED_FIELD_PAD_UPGRADE) {
            rangeUpgrade += 3;
         }
      }

      if (rangeUpgrade > 5) {
         rangeUpgrade = 5;
      }

      if (this.radius > rangeUpgrade) {
         this.queue.clear();
         this.knownPositions.clear();
      }

      this.radius = rangeUpgrade;
      this.updateTileField("radius");
      if (speed < 1) {
         this.delay = 20;
      } else if (speed < 2) {
         this.delay = 10;
      } else if (speed < 3) {
         this.delay = 5;
      } else if (speed < 4) {
         this.delay = 2;
      } else {
         this.delay = 1;
      }

      this.updateQueue();
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      this.handleComparators();
      this.addItemsToInventory();
      if (this.hasEnergy(25) && !this.invClock(this.delay) && !this.isRedstonePowered()) {
         if (this.clock(1024)) {
            this.updateQueue();
         }

         if (!this.queue.isEmpty() && this.items.isEmpty()) {
            this.useEnergy(5);
            long position = this.queue.dequeueLong();
            BlockPos pos = BlockPos.of(position);
            if (this.getLevel().getBlockEntity(pos) instanceof ICropTile cropTile) {
               this.queue.enqueue(position);
               ICrop crop = cropTile.getCrop();
               if (crop != null && crop.canBeHarvested(cropTile) && cropTile.getGrowthStage() >= crop.getOptimalHarvestStep(cropTile)) {
                  this.useEnergy(20);
                  this.items.addAll(cropTile.performHarvest(false));
                  this.addItemsToInventory();
               }
            } else {
               this.knownPositions.remove(position);
            }
         }
      }
   }

   public void addItemsToInventory() {
      if (!this.items.isEmpty()) {
         if (this.transporter == null) {
            this.transporter = TransporterManager.getTransporter(new InventoryWrapper(this));
         }

         Iterator<ItemStack> iter = this.items.iterator();

         while (iter.hasNext()) {
            ItemStack stack = iter.next();
            if (stack.isEmpty()) {
               iter.remove();
            } else {
               int added = this.transporter.addItem(stack, null, false);
               if (added > 0) {
                  stack.shrink(added);
                  if (stack.isEmpty()) {
                     iter.remove();
                  }
               }
            }
         }
      }
   }

   public void updateQueue() {
      ObjectListIterator var1 = AABBUtil.getValidBlocks(this.getLevel(), this.getBlockPos(), this.radius, FILTER, 2, DirectionList.ALL)
         .getPositions()
         .iterator();

      while (var1.hasNext()) {
         BlockPos pos = (BlockPos)var1.next();
         long position = pos.asLong();
         if (this.knownPositions.add(position)) {
            this.queue.enqueue(position);
         }
      }
   }

   @Override
   public AABB getAreaOfEffect() {
      return new AABB(
         this.getBlockPos().offset(-this.radius, -this.radius, -this.radius), this.getBlockPos().offset(this.radius + 1, this.radius + 1, this.radius + 1)
      );
   }

   @Override
   public int getAreaOfEffectColor() {
      return -2147050257;
   }

   @Override
   public void setVisualizationId(int id) {
      this.networkId = id;
   }

   @Override
   public int getVisualizationId() {
      return this.networkId;
   }
}
