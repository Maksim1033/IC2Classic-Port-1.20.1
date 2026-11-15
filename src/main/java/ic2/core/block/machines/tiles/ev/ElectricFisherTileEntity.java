package ic2.core.block.machines.tiles.ev;

import ic2.api.energy.impl.LinkedSink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.tiles.BaseMultiElectricTileEntity;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.StructureBuilder;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class ElectricFisherTileEntity extends BaseMultiElectricTileEntity implements IProgressMachine, IWrenchRemovable {
   public static final ResourceLocation LOOT_TABLE = new ResourceLocation("ic2", "electric_fisher");
   ICache<IItemHandler> cache = new CapabilityCache<>(this, DirectionList.UP, ForgeCapabilities.ITEM_HANDLER);
   private final ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
   public int length;
   public int progress = 0;

   public ElectricFisherTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 512, 5000);
      this.addCaches(new ICache[]{this.cache});
      this.fishingRod.enchant(Enchantments.FISHING_LUCK, 3);
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      return 300.0F;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("length", this.length);
      compound.putInt("progress", this.progress);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.length = compound.getInt("length");
      this.progress = compound.getInt("progress");
   }

   @Override
   public void onStructureTick() {
      if (!this.hasEnergy(150)) {
         if (this.progress > 0) {
            this.progress--;
         }
      } else {
         this.useEnergy(150);
         this.progress++;
         if (this.progress >= 300) {
            this.progress = 0;
            IItemTransporter transporter = TransporterManager.getTransporter(this.cache.getHandler(Direction.UP));
            if (transporter != null) {
               LootParams.Builder context = new LootParams.Builder((ServerLevel)this.getLevel())
                  .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition.below()))
                  .withParameter(LootContextParams.TOOL, this.fishingRod)
                  .withLuck(1.0F + (float)this.length - 5.0F);
               LootTable loottable = this.getLevel().getServer().getLootData().getLootTable(LOOT_TABLE);

               for (int i = 0; i < 32; i++) {
                  for (ItemStack stack : loottable.getRandomItems(context.create(LootContextParamSets.FISHING))) {
                     transporter.addItem(stack, Direction.DOWN, false);
                  }
               }
            }
         }
      }
   }

   @Override
   public List<IEnergyTile> getTiles() {
      List<IEnergyTile> tiles = CollectionUtils.<IEnergyTile>createList();
      tiles.add(this);
      int baseY = this.getBlockPos().getY();

      for (long entry : this.structure.structurePositions) {
         BlockPos pos = BlockPos.of(entry);
         if (pos.getY() == baseY) {
            tiles.add(new LinkedSink(this.getLevel(), pos));
         }
      }

      return tiles;
   }

   @Override
   public boolean isStateStillValid(BlockPos actualPos, BlockPos structurePos, BlockState newState) {
      if (structurePos.getY() == 0) {
         int max = Math.max(Math.abs(structurePos.getX()), Math.abs(structurePos.getZ()));
         return newState.is(max == 1 ? BlockTags.PLANKS : BlockTags.LOGS);
      } else if (structurePos.getY() > 0) {
         return this.isValidContainer(this.getLevel().getBlockEntity(actualPos));
      } else {
         return structurePos.getX() == 0 && structurePos.getX() == 0 && newState.getBlock() != Blocks.CHAIN ? false : this.isWaterLogged(newState);
      }
   }

   public boolean isValidContainer(BlockEntity tile) {
      if (tile == null) {
         return false;
      } else {
         IItemHandler handler = (IItemHandler)tile.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElse(EmptyHandler.INSTANCE);
         return handler.getSlots() > 20;
      }
   }

   public boolean isWaterLogged(BlockState state) {
      if (state.hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)state.getValue(BlockStateProperties.WATERLOGGED)) {
         return true;
      } else {
         FluidState fluid = state.getFluidState();
         return fluid.isSource() && fluid.getType() == Fluids.WATER;
      }
   }

   @Override
   public boolean testStructure(StructureBuilder builder) {
      if (!this.isValidContainer(builder.up().getTile())) {
         return false;
      } else if (!builder.resetPosition().left(1).forward(1).circleSquareStructure(3, 3, () -> builder.isBlock(BlockTags.PLANKS))) {
         return false;
      } else if (!builder.left(1).forward(1).circleSquareStructure(5, 5, () -> builder.isBlock(BlockTags.LOGS))) {
         return false;
      } else {
         builder.resetPosition();
         int length = builder.findLength(10, false, Direction.DOWN, T -> T.isBlock(Blocks.CHAIN) && T.isWaterlogged());
         if (length < 5) {
            return false;
         } else {
            for (BlockPos pos : Box.fromPos(BlockPos.ZERO.offset(-2, -1, -2), BlockPos.ZERO.offset(2, -length, 2))) {
               if (!builder.setOffset(pos).isWaterlogged()) {
                  return false;
               }
            }

            this.length = length;
            return true;
         }
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FISHER;
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.7;
   }
}
