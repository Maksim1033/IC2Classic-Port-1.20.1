package ic2.core.block.machines.tiles.luv;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.registries.IFusionRecipeList;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.blocks.ValveBlock;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.features.multiblock.IMultiBlockClickable;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.block.machines.containers.luv.FusionReactorContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.item.reactor.ReactorUraniumRod;
import ic2.core.item.reactor.base.IUraniumRod;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.StructureBuilder;
import ic2.core.utils.math.geometry.Box;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

public class FusionReactorTileEntity extends BaseMultiBlockTileEntity implements IFluidHandler, ITileGui, IMultiBlockClickable, IWrenchRemovable {
   public static final int REQUIRED_LAVA = 10000;
   public static final int MAX_MATERIAL = 10000;
   @NetworkInfo
   public int storedFluid = 0;
   @NetworkInfo
   public int heat = 0;
   @NetworkInfo
   public FusionReactorTileEntity.FuelInfo renderInfo = new FusionReactorTileEntity.FuelInfo(this);
   @NetworkInfo
   public int material = 0;
   @NetworkInfo
   public float productionRate = 1.0F;
   public float producedFuel = 0.0F;
   boolean recalculate = true;
   @NetworkInfo
   float production = 0.0F;
   @NetworkInfo
   int rodCount = 0;
   @NetworkInfo
   int individualRods = 0;

   public FusionReactorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 7);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
      this.addNetworkFields(new String[]{"storedFluid", "renderInfo"});
      this.addGuiFields(new String[]{"heat", "material", "maxMaterial", "productionRate", "production", "rodCount", "individualRods"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] array = MathUtils.fromTo(0, 6);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, array);
      handler.registerSlotAccess(AccessRule.IMPORT, 6);
      handler.registerSlotsForSide(DirectionList.ALL, array);
      handler.registerSlotsForSide(DirectionList.ALL, 6);
      handler.registerInputFilter(new ClassFilter(ReactorUraniumRod.class), array);
      handler.registerInputFilter(this::isValidFuel, 6);
      handler.registerNamedSlot(SlotType.INPUT, array);
      handler.registerNamedSlot(SlotType.FUEL, 6);
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.55;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FUSION_REACTOR;
   }

   public boolean isValidFuel(ItemStack stack) {
      return IC2.RECIPES.get(this.isSimulating()).fusionFuel.getFuel(stack.getItem()) != null;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("lava", this.storedFluid);
      compound.putInt("heat", this.heat);
      compound.putInt("material", this.material);
      compound.putFloat("produced", this.producedFuel);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.storedFluid = compound.getInt("lava");
      this.heat = compound.getInt("heat");
      this.material = compound.getInt("material");
      this.producedFuel = compound.getFloat("produced");
   }

   private FluidStack create(int amount) {
      return new FluidStack(Fluids.LAVA, amount);
   }

   private int accessible() {
      return Math.max(0, this.storedFluid - 10000);
   }

   public float getMaterial() {
      return (float)this.material / 10000.0F;
   }

   public AABB getBox(float progress) {
      AABB box = this.boundingBox.toInclusiveAxis().deflate(1.0);
      return box.contract(0.0, Math.max(0.0010000000474974513, (box.maxY - box.minY) * (double)(1.0F - progress)), 0.0)
         .move((double)(-this.worldPosition.getX()), (double)(-this.worldPosition.getY()), (double)(-this.worldPosition.getZ()));
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FusionReactorContainer(this, player, windowID);
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.isValid;
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && this.isValid;
   }

   @Override
   public void onStructureTick() {
      if (!this.invClock(10)) {
         if (this.recalculate) {
            this.recalculate = false;
            this.rodCount = 0;
            this.production = 0.0F;
            this.individualRods = 0;

            for (int i = 0; i < 6; i++) {
               ItemStack stack = (ItemStack)this.inventory.get(i);
               if (stack.getItem() instanceof ReactorUraniumRod comp) {
                  IUraniumRod rod = comp.getUranium();
                  this.production = this.production + (float)rod.getFusionHeat();
                  this.rodCount = this.rodCount + comp.getRodCount();
                  this.individualRods++;
               }
            }

            this.production = this.production / (float)Math.max(1, this.individualRods);
            this.updateGuiFields(new String[]{"production", "rodCount", "individualRods"});
         }

         while (this.material <= 9000 && !((ItemStack)this.inventory.get(6)).isEmpty()) {
            IFusionRecipeList.FusionFuel fuel = this.gainFuel(true);
            if (fuel != null) {
               this.material = this.material + fuel.fuelValue();
               this.productionRate = fuel.productionRate();
               this.updateGuiFields(new String[]{"material", "maxMaterial", "productionRate"});
            }
         }

         if (this.heat < 48000 && this.production > 2.0F) {
            this.heat = (int)Math.min(48000.0F, (float)this.heat + Math.min(5.0F, this.production - 2.0F) * (float)this.individualRods * 10.0F);
            this.damageRods(this.rodCount / Math.max(1, this.individualRods));
            this.updateGuiField("heat");
         } else if (this.heat == 48000 && this.material > 0 && this.storedFluid < 522000) {
            int left = 522000 - this.storedFluid;
            if (this.producedFuel < (float)left) {
               boolean fullProduction = this.storedFluid >= 10000;
               this.producedFuel = this.producedFuel
                  + (fullProduction ? this.production : Math.min(5.0F, this.production - 2.0F)) * (float)this.rodCount * this.productionRate * 10.0F / 1.5F;
            }

            int added = Math.min(Math.min(this.material, (int)this.producedFuel), left);
            if (added > 0) {
               this.material -= added;
               this.producedFuel -= (float)added;
               this.storedFluid += added;
               if (this.storedFluid < 10000) {
                  this.heat = (int)((float)this.heat - (1.0F - Mth.clamp((float)this.storedFluid / 10000.0F, 0.0F, 1.0F)) * 24000.0F);
                  this.damageRods(this.rodCount / Math.max(1, this.individualRods));
               }

               this.updateGuiFields(new String[]{"material"});
               this.updateTileField("storedFluid");
            }

            if (this.clock(200)) {
               this.damageRods(1);
            }
         }

         if (this.clock(this.rodCount == 0 ? 10 : 10240)) {
            if (this.rodCount == 0 && this.heat > 24000 || this.storedFluid <= 0 && this.heat > 0) {
               this.heat = Math.max(this.rodCount == 0 && this.heat > 24000 ? 24000 : 0, this.heat - 10);
               this.updateGuiField("heat");
            } else if (this.storedFluid > 0) {
               this.storedFluid--;
               this.updateTileField("storedFluid");
            }
         }
      }
   }

   protected void damageRods(int amount) {
      for (int i = 0; i < 6; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         Item damage = stack.getItem();
         if (damage instanceof ReactorUraniumRod) {
            ReactorUraniumRod rod = (ReactorUraniumRod)damage;
            int damagex = stack.getDamageValue();
            if (damagex + amount > stack.getMaxDamage()) {
               this.inventory.set(i, this.getLevel().random.nextInt(3) == 0 ? rod.getUranium().createNearDepletedRod(rod.getRodCount()) : ItemStack.EMPTY);
               this.recalculate = true;
            } else {
               stack.setDamageValue(damagex + amount);
            }
         }
      }
   }

   public IFusionRecipeList.FusionFuel gainFuel(boolean consume) {
      ItemStack stack = (ItemStack)this.inventory.get(6);
      if (stack.isEmpty()) {
         return null;
      } else {
         IFusionRecipeList.FusionFuel fuel = IC2.RECIPES.get(this.isSimulating()).fusionFuel.getFuel(stack.getItem());
         if (fuel.consumed() && consume) {
            if (stack.hasCraftingRemainingItem()) {
               this.inventory.set(6, stack.getCraftingRemainingItem());
            } else {
               stack.shrink(1);
            }
         }

         return fuel;
      }
   }

   public long getTicksTillHeatUp() {
      if (this.storedFluid < 10000 && !(this.production <= 2.0F)) {
         float heatProduction = Math.min(5.0F, this.production - 2.0F) * (float)this.individualRods;
         float lavaProduction = heatProduction * (float)this.rodCount;
         long ticks = (long)((int)((float)(48000 - this.heat) / heatProduction));
         int totalLava = this.storedFluid;
         float lavaProduced = 0.0F;

         while (totalLava < 10000) {
            lavaProduced += lavaProduction / 6.0F;
            int consume = (int)lavaProduced;
            if (consume > 0) {
               lavaProduced -= (float)consume;
               totalLava += consume;
               ticks = (long)((float)ticks + (1.0F - Mth.clamp((float)(totalLava / 10000), 0.0F, 1.0F)) * 24000.0F / heatProduction);
            }
         }

         return ticks;
      } else {
         return 0L;
      }
   }

   public int getTanks() {
      return 1;
   }

   @Nonnull
   public FluidStack getFluidInTank(int tank) {
      return this.create(this.accessible());
   }

   public int getTankCapacity(int tank) {
      return 512000;
   }

   public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
      return false;
   }

   public int fill(FluidStack resource, FluidAction action) {
      return 0;
   }

   @Nonnull
   public FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource.getFluid() != Fluids.LAVA) {
         return FluidStack.EMPTY;
      } else {
         int left = Math.min(this.accessible(), resource.getAmount());
         if (left <= 0) {
            return FluidStack.EMPTY;
         } else {
            if (action.execute()) {
               this.storedFluid -= left;
               this.updateTileField("storedFluid");
            }

            return this.create(left);
         }
      }
   }

   @Nonnull
   public FluidStack drain(int maxDrain, FluidAction action) {
      int left = Math.min(this.accessible(), maxDrain);
      if (left <= 0) {
         return FluidStack.EMPTY;
      } else {
         if (action.execute()) {
            this.storedFluid -= left;
            this.updateTileField("storedFluid");
         }

         return this.create(left);
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating()) {
         this.updateTileField("renderInfo");
         if (slot < 6) {
            this.recalculate = true;
         }
      }
   }

   private boolean isReinforcedStone(BlockState state) {
      return state.is(IC2Blocks.REINFORCED_STONE) || state.is(IC2Blocks.REINFORCED_BRICK) || state.is(IC2Blocks.REINFORCED_CRACKED_STONE);
   }

   private boolean isReinforcedGlass(BlockState state) {
      return state.is(IC2Blocks.REINFORCED_GLASS) || state.is(IC2Blocks.REINFORCED_CLEAR_GLASS);
   }

   @Override
   public boolean isStateStillValid(BlockPos actualPos, BlockPos structurePos, BlockState newState) {
      if (!this.boundingBox.isBorder(structurePos)) {
         return newState.isAir();
      } else if (newState.getBlock() instanceof ValveBlock) {
         return true;
      } else if (newState.is(IC2Blocks.FUSION_REACTOR)) {
         return true;
      } else {
         return !this.boundingBox.isEdge(structurePos) && this.boundingBox.getMinY() != structurePos.getY()
            ? this.isReinforcedGlass(newState)
            : this.isReinforcedStone(newState);
      }
   }

   @Override
   public boolean testStructure(StructureBuilder builder) {
      MutableBlockPos pos = new MutableBlockPos();
      Box box = new Box(0, 0, 0, 4, 4, 4);

      for (int i = 1; builder.cubicStructure(i, 5, 5, 5, pos); i++) {
         if (box.isBorder(pos)) {
            if (builder.isBlock((Predicate<Block>)(T -> T instanceof ValveBlock))) {
               this.children.add(builder.getTile(BaseLinkingTileEntity.class));
            } else {
               if ((box.isEdge(pos) || pos.getY() == box.getMaxY()) && !builder.isState(this::isReinforcedStone)) {
                  return false;
               }

               if (!box.isEdge(pos) && pos.getY() != box.getMaxY() && !builder.isState(this::isReinforcedGlass)) {
                  return false;
               }
            }
         } else if (!builder.isAir()) {
            return false;
         }
      }

      return true;
   }

   public static void loadRecipes(IFusionRecipeList list) {
      list.addFuel(IC2Items.COBBLEGEN_UPGRADE, 1000, 1.0F, false);
      list.addFuel(Items.COBBLESTONE, 1000, 1.0F, true);
      list.addFuel(Items.NETHERRACK, 1000, 1.5F, true);
   }

   public static class FuelInfo implements INetworkDataBuffer {
      final FusionReactorTileEntity tile;
      Block block = Blocks.AIR;
      int[] colors = new int[12];

      public FuelInfo(FusionReactorTileEntity tile) {
         this.tile = tile;
      }

      public boolean isVisible(Direction dir) {
         return this.colors[6 + dir.get3DDataValue()] > 0;
      }

      public int getColor(Direction dir) {
         return this.colors[dir.get3DDataValue()];
      }

      public int getCount(Direction dir) {
         return this.colors[6 + dir.get3DDataValue()];
      }

      @Override
      public void write(IOutputBuffer buffer) {
         for (int i = 0; i < 6; i++) {
            ItemStack stack = this.tile.getStackInSlot(i);
            if (stack.getItem() instanceof ReactorUraniumRod comp) {
               IUraniumRod rod = comp.getUranium();
               buffer.writeInt(rod.getColor());
               buffer.writeVarInt(comp.getRodCount());
            } else {
               buffer.writeInt(-1);
               buffer.writeVarInt(0);
            }
         }

         ItemStack stack = this.tile.getStackInSlot(6);
         Block block = stack.is(IC2Items.COBBLEGEN_UPGRADE) ? Blocks.COBBLESTONE : Block.byItem(stack.getItem());
         buffer.writeForgeEntry(block, ForgeRegistries.BLOCKS);
      }

      @Override
      public void read(IInputBuffer buffer) {
         for (int i = 0; i < 6; i++) {
            this.colors[i] = buffer.readInt();
            this.colors[i + 6] = buffer.readVarInt();
         }

         this.block = buffer.readForgeRegistryEntry(ForgeRegistries.BLOCKS);
      }

      public BlockState getMeltingBlock() {
         return this.block.defaultBlockState();
      }
   }

   public static class Tank implements IFluidTank {
      FusionReactorTileEntity tile;
      boolean heatTank;

      public Tank(FusionReactorTileEntity tile, boolean heatTank) {
         this.tile = tile;
         this.heatTank = heatTank;
      }

      public FluidStack getFluid() {
         return new FluidStack(Fluids.LAVA, this.heatTank ? Math.min(10000, this.tile.storedFluid) : Math.max(0, this.tile.storedFluid - 10000));
      }

      public int getFluidAmount() {
         return this.heatTank ? Math.min(10000, this.tile.storedFluid) : Math.max(0, this.tile.storedFluid - 10000);
      }

      public int getCapacity() {
         return this.heatTank ? 10000 : this.tile.getTankCapacity(0);
      }

      public boolean isFluidValid(FluidStack stack) {
         return false;
      }

      public int fill(FluidStack resource, FluidAction action) {
         return 0;
      }

      @Nonnull
      public FluidStack drain(int maxDrain, FluidAction action) {
         return FluidStack.EMPTY;
      }

      @Nonnull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         return FluidStack.EMPTY;
      }
   }
}
