package ic2.core.block.machines.tiles.mv;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.block.machines.containers.mv.SlowGrinderContainer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.filter.special.MachineFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SlowGrinderTileEntity extends BasicMachineTileEntity implements INetworkClientEventListener, IRecipeOutput.IRecipeOverride {
   public static final int[][] SLOT_ROTATION = new int[][]{{3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}, {9, 10}, {10, 1}};
   public static final EnumSet<IUpgradeItem.UpgradeType> SUPPORTED_UPGRADES = EnumSet.of(
      IUpgradeItem.UpgradeType.TRANSPORT_MOD,
      IUpgradeItem.UpgradeType.REDSTONE_MOD,
      IUpgradeItem.UpgradeType.AUDIO_MOD,
      IUpgradeItem.UpgradeType.CUSTOM_MOD,
      IUpgradeItem.UpgradeType.PROCESSING_MOD
   );
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_slow_grinder.png");
   @NetworkInfo
   public byte speed = 1;
   private byte lastSpeed = 1;

   public SlowGrinderTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 11, 2, 10, 100, 128);
      this.addGuiFields(new String[]{"speed"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] slots = new int[]{1, 10, 9, 8, 7, 6, 5, 4, 3};
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.IMPORT, slots);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.EXPORT, 2);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), slots);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new MachineFilter(this), slots);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 2);
      handler.registerNamedSlot(SlotType.INPUT, slots);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return SUPPORTED_UPGRADES;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.speed = compound.getByte("speed");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putByte("speed", this.speed);
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.RECYCLER_PROCESSING;
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1, 10, 9, 8, 7, 6, 5, 4, 3);
      this.inOut[1] = new RangedInventory(this, 2).setOutputOnly();
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).recycler;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new SlowGrinderContainer(this, player, windowID);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.SLOW_GRINDER;
   }

   @Override
   protected boolean consumeContainers() {
      return true;
   }

   @Override
   public void onTick() {
      this.updateSpeed();
      if (this.clock(10)) {
         this.sortInventory();
      }

      super.onTick();
   }

   public void sortInventory() {
      for (int z = 0; z < SLOT_ROTATION.length; z++) {
         if (this.transferStack(SLOT_ROTATION[z][0], SLOT_ROTATION[z][1]) && z == SLOT_ROTATION.length - 1) {
            this.rebuildCache = true;
         }
      }
   }

   @Override
   protected boolean canStopTicking(boolean charging) {
      if (super.canStopTicking(charging) && ((ItemStack)this.inventory.get(1)).isEmpty()) {
         for (int i = 3; i < 11; i++) {
            if (!((ItemStack)this.inventory.get(i)).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 1) {
         this.speed++;
         if (this.speed > 5) {
            this.speed = 1;
         }

         this.updateGuiField("speed");
      }

      super.onClientDataReceived(entity, key, value);
   }

   private void updateSpeed() {
      if (this.speed != this.lastSpeed) {
         this.lastSpeed = this.speed;
         this.defaultEnergyConsume = this.speed * 10;
         this.onUpgradesChanged();
         this.progressPerTick = (float)this.speed;
      }
   }

   @Override
   public float getChance(float original) {
      switch (this.speed) {
         case 1:
            return 1.0F;
         case 2:
            return 0.84F;
         case 3:
            return 0.71F;
         case 4:
            return 0.59F;
         case 5:
            return 0.5F;
         default:
            return 1.0F;
      }
   }
}
