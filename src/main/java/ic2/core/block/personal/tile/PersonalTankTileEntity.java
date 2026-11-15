package ic2.core.block.personal.tile;

import ic2.api.network.buffer.NetworkInfo;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.BucketFiller;
import ic2.core.block.base.tiles.impls.BasePersonalTileEntity;
import ic2.core.block.personal.container.PersonalTankContainer;
import ic2.core.fluid.IC2Tank;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PersonalTankTileEntity extends BasePersonalTileEntity implements ITickListener, IClickable, IDropProvider {
   @NetworkInfo
   public IC2Tank tank = new IC2Tank(128000);
   public SimpleInventory inventory = new SimpleInventory(4);
   protected BucketFiller filler = new BucketFiller(this.inventory, this, this.tank, 0, 1);
   protected BucketFiller drainer = new BucketFiller(this.inventory, this, this.tank, 2, 3);

   public PersonalTankTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"tank"});
      this.tank.addListener(T -> this.updateGuiField("tank"));
      this.addPersonalCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new PersonalTankContainer(this, player, windowID);
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      this.inventory.addToDrops(drops);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PERSONAL_TANK;
   }

   @Override
   public boolean isAllowingUI() {
      return false;
   }

   @Override
   public void voidStorage() {
      this.tank.drain(2147483647, FluidAction.EXECUTE);
   }

   @Override
   public CompoundTag writeItemData(CompoundTag nbt) {
      super.writeItemData(nbt);
      NBTUtils.put(nbt, "tank", this.tank.writeToNBT(new CompoundTag()));
      NBTUtils.put(nbt, "inv", this.inventory.save(new CompoundTag()));
      return nbt;
   }

   @Override
   public void readItemData(CompoundTag nbt, boolean place) {
      super.readItemData(nbt, place);
      this.tank.readFromNBT(nbt.getCompound("tank"));
      this.inventory.load(nbt.getCompound("inv"));
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      if (this.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_EDIT, false)) {
         return FluidHelper.drainContainers(player.getItemInHand(hand), player, this.tank)
            ? true
            : FluidHelper.fillContainers(player.getItemInHand(hand), player, this.tank);
      } else {
         return false;
      }
   }

   @Override
   public void onTick() {
      this.filler.fillTank();
      this.drainer.fillItem();
   }
}
