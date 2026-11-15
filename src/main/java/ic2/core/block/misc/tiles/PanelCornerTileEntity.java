package ic2.core.block.misc.tiles;

import ic2.api.blocks.DyeableMap;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.IC2;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.misc.base.IC2CornerRailingBlock;
import ic2.core.block.rendering.props.ColorProperty;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public class PanelCornerTileEntity extends BaseTileEntity implements IDropProvider {
   @NetworkInfo
   public DyeColor first;
   @NetworkInfo
   public DyeColor second;
   @NetworkInfo
   public DyeColor third;
   @NetworkInfo
   public DyeColor fourth;

   public PanelCornerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"first", "second", "third", "fourth"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PANEL_CORNER;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.remove("active");
      compound.remove("facing");
      NBTUtils.putEnum(compound, "first", this.first);
      NBTUtils.putEnum(compound, "second", this.second);
      NBTUtils.putEnum(compound, "third", this.third);
      NBTUtils.putEnum(compound, "fourth", this.fourth);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.first = NBTUtils.getEnum(compound, "first", DyeColor.class);
      this.second = NBTUtils.getEnum(compound, "second", DyeColor.class);
      this.third = NBTUtils.getEnum(compound, "third", DyeColor.class);
      this.fourth = NBTUtils.getEnum(compound, "fourth", DyeColor.class);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("first") || fields.contains("second") || fields.contains("third") || fields.contains("fourth")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return ModelData.builder()
         .with(ColorProperty.FIRST, this.first)
         .with(ColorProperty.SECOND, this.second)
         .with(ColorProperty.THIRD, this.third)
         .with(ColorProperty.FOURTH, this.fourth)
         .build();
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      Block block = this.getBlockState().getBlock();
      if (block instanceof IC2CornerRailingBlock) {
         DyeableMap map = ((IC2CornerRailingBlock)block).getColorMap();
         if (this.first != null) {
            drops.add(new ItemStack(map.getBlock(this.first)));
         }

         if (this.second != null) {
            drops.add(new ItemStack(map.getBlock(this.second)));
         }

         if (this.third != null) {
            drops.add(new ItemStack(map.getBlock(this.third)));
         }

         if (this.fourth != null) {
            drops.add(new ItemStack(map.getBlock(this.fourth)));
         }
      }
   }

   public int getBoundId() {
      return (this.first != null ? 1 : 0) | (this.second != null ? 2 : 0) | (this.third != null ? 4 : 0) | (this.fourth != null ? 8 : 0);
   }

   public boolean isSet(int index) {
      switch (index) {
         case 0:
            return this.first != null;
         case 1:
            return this.second != null;
         case 2:
            return this.third != null;
         case 3:
            return this.fourth != null;
         default:
            return false;
      }
   }

   public DyeColor getColor(int index) {
      switch (index) {
         case 0:
            return this.first;
         case 1:
            return this.second;
         case 2:
            return this.third;
         case 3:
            return this.fourth;
         default:
            return null;
      }
   }

   public void setColor(int index, DyeColor color) {
      switch (index) {
         case 0:
            this.setFirst(color);
            break;
         case 1:
            this.setSecond(color);
            break;
         case 2:
            this.setThird(color);
            break;
         case 3:
            this.setFourth(color);
      }
   }

   public void setFirst(DyeColor first) {
      if (this.first != first) {
         this.first = first;
         this.updateTileField("first");
         this.onStateChanged();
      }
   }

   public void setSecond(DyeColor second) {
      if (this.second != second) {
         this.second = second;
         this.updateTileField("second");
         this.onStateChanged();
      }
   }

   public void setThird(DyeColor third) {
      if (this.third != third) {
         this.third = third;
         this.updateTileField("third");
         this.onStateChanged();
      }
   }

   public void setFourth(DyeColor forth) {
      if (this.fourth != forth) {
         this.fourth = forth;
         this.updateTileField("fourth");
         this.onStateChanged();
      }
   }
}
