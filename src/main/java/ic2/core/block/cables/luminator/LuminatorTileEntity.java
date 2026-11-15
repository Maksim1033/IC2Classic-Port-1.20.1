package ic2.core.block.cables.luminator;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.block.LuminatorModel;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.Tags.Blocks;

public class LuminatorTileEntity extends BaseTileEntity implements ITickListener, IEnergySink, IClickable, IWrenchRemovable, IEUStorage, ITileActivityProvider {
   int energy = 0;
   int maxEnergy = 50;
   boolean addedToEnet;
   @NetworkInfo
   ItemStack glassBlock = ItemStack.EMPTY;

   public LuminatorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"glassBlock"});
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = NBTUtils.getInt(compound, "energy", this.maxEnergy);
      this.glassBlock = NBTUtils.loadItem(compound, "glass", ItemStack.EMPTY);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, this.maxEnergy);
      NBTUtils.saveItem(compound, "glass", this.glassBlock);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack held = player.getItemInHand(hand);
      if (held.isEmpty() && !this.glassBlock.isEmpty() && player.isShiftKeyDown()) {
         this.setGlass(ItemStack.EMPTY);
         return true;
      } else {
         Block block = Block.byItem(held.getItem());
         if (!this.glassBlock.isEmpty()
            || !block.defaultBlockState().is(Blocks.GLASS) && block != IC2Blocks.REINFORCED_CLEAR_GLASS && block != IC2Blocks.REINFORCED_GLASS) {
            int adding = ElectricItem.MANAGER.discharge(held, 1000 - this.energy, 4, true, true, false);
            if (adding > 0) {
               this.energy += adding;
            }

            return false;
         } else {
            this.setGlass(held);
            if (!player.isCreative()) {
               held.shrink(1);
            }

            return true;
         }
      }
   }

   public BlockState getGlassState() {
      return this.glassBlock.isEmpty() ? null : Block.byItem(this.glassBlock.getItem()).defaultBlockState();
   }

   public void setGlass(ItemStack stack) {
      if (stack.isEmpty() || !StackUtil.isStackEqual(stack, this.glassBlock)) {
         Block.popResource(this.getLevel(), this.getBlockPos(), this.glassBlock);
      }

      this.glassBlock = StackUtil.copyWithSize(stack, 1);
      this.updateTileField("glassBlock");
      this.onStateChanged();
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("glassBlock")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }

      if (fields.contains("isActive") || fields.contains("lightLevel")) {
         this.getLevel().getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.getBlockState().hasProperty(LuminatorBlock.WATER) && (Boolean)this.getBlockState().getValue(LuminatorBlock.WATER)) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      BlockState state = this.getGlassState();
      return state != null
         ? ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)T -> LuminatorModel.createQuads(this.getLevel(), this.getBlockPos(), state)
            )
            .build()
         : super.getModelData();
   }

   @Override
   public void setFacing(Direction facing) {
      super.setFacing(facing);
      EnergyNet.INSTANCE.updateTile(this);
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.addedToEnet) {
         this.addedToEnet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.addedToEnet) {
         this.addedToEnet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.LUMINATOR;
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 1.0;
   }

   public int getLightLevel() {
      return this.isActive() ? 15 : 0;
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getTier() {
      return this.getSinkTier();
   }

   @Override
   public void onTick() {
      if (this.clock(10)) {
         if (this.energy > 0) {
            if (!this.isActive()) {
               this.energy--;
            }

            this.setActive(true);
            this.energy--;
         } else {
            this.setActive(false);
         }
      }
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return side == this.getFacing().getOpposite();
   }

   @Override
   public int getSinkTier() {
      return 4;
   }

   @Override
   public int getRequestedEnergy() {
      return this.maxEnergy - this.energy;
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      if (amount <= 0) {
         return 0;
      } else {
         int added = Math.min(amount, this.maxEnergy - this.energy);
         if (added > 0) {
            this.energy += added;
         }

         return amount - added;
      }
   }
}
