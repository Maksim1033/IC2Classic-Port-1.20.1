package ic2.core.block.storage.tiles.tank;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public class BaseValveTileEntity extends BaseLinkingTileEntity implements ICamouflagable {
   @NetworkInfo
   public CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);
   @NetworkInfo
   public boolean textureCopied = false;

   public BaseValveTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"storage", "textureCopied"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.VALVE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.put(compound, "storage", this.storage.save(new CompoundTag()));
      NBTUtils.putBoolean(compound, "tex", this.textureCopied, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.textureCopied = compound.getBoolean("tex");
      if (this.textureCopied) {
         this.storage.load(compound.getCompound("storage"));
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("storage")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public CamouflageStorage getStorage() {
      return this.storage;
   }

   @Override
   public boolean applyTexture(Direction dir, RetextureEvent.TextureContainer container) {
      if (this.storage.canApply(DirectionList.ALL, container)) {
         this.storage.setTexture(DirectionList.ALL, container);
         this.textureCopied = true;
         this.updateTileFields(new String[]{"storage", "textureCopied"});
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSideEnabled(Direction dir) {
      return this.textureCopied;
   }

   @Override
   public boolean removeCamouflage() {
      return false;
   }

   @Override
   public ItemLike getCamouflageDrop() {
      return Items.AIR;
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.textureCopied
         ? ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
            )
            .build()
         : ModelData.EMPTY;
   }
}
