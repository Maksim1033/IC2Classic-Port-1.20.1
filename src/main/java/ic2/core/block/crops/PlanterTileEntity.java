package ic2.core.block.crops;

import ic2.api.crops.IFarmland;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.props.BlockProperty;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

public class PlanterTileEntity extends BaseTileEntity implements IClickable {
   @NetworkInfo
   public Block block = Blocks.AIR;

   public PlanterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.clearNetworkFields();
      this.addNetworkFields(new String[]{"block"});
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.block = NBTUtils.getForgeEntry(compound, "block", ForgeRegistries.BLOCKS, Blocks.AIR);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putForgeEntry(compound, "block", this.block, Blocks.AIR, ForgeRegistries.BLOCKS);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      if (side == Direction.UP) {
         Block block = Block.byItem(player.getItemInHand(hand).getItem());
         if (block != Blocks.AIR && this.getLand(block) != null && block != IC2Blocks.PLANTER_POT) {
            if (this.setBlock(block)) {
               player.getItemInHand(hand).shrink(1);
            }

            return true;
         }
      }

      return false;
   }

   public IFarmland getLand(Block block) {
      IFarmland farmland = CropRegistry.INSTANCE.getFarmland(block);
      return farmland != null && !farmland.isSpecial() ? farmland : null;
   }

   public boolean setBlock(Block block) {
      if (this.block != block && block != null && block != Blocks.AIR) {
         if (this.block != Blocks.AIR) {
            Block.popResource(this.getLevel(), this.getBlockPos().above(), new ItemStack(this.block));
         }

         this.block = block;
         this.updateTileField("block");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      if (fields.contains("block")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }

      super.onNetworkFieldChanged(fields, player);
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return ModelData.builder().with(BlockProperty.INSTANCE, this.block).build();
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PLANTER_POT;
   }
}
