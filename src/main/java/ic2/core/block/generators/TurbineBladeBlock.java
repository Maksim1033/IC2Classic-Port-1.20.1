package ic2.core.block.generators;

import ic2.core.block.base.IC2Block;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.block.rendering.block.TurbineBladeModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TurbineBladeBlock extends IC2Block implements IBlockModel, ICustomBlockModel, IRenderType {
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;
   public static final DirectionProperty FACINGS = IC2Properties.ALL_FACINGS;
   public static final IntegerProperty FORMED = IC2Properties.FORMED_STATE3X3_FLAT;

   public TurbineBladeBlock() {
      super("turbine_block", BaseMachineBlock.BASE_MACHINE);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public static BlockState createState(BlockState state, boolean active, Direction facing, int index) {
      return (BlockState)((BlockState)((BlockState)state.setValue(ACTIVE, active)).setValue(FACINGS, facing)).setValue(FORMED, index);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE}).add(new Property[]{FACINGS}).add(new Property[]{FORMED});
   }

   public void m_49811_(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return new ItemStack(IC2Blocks.MACHINE_BLOCK_PLATED);
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      return new ItemStack(IC2Blocks.MACHINE_BLOCK_PLATED);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new TurbineBladeModel(
         state,
         this,
         IC2Textures.getMappedEntriesBlockIC2("electric/generator/steam_tunnel").get(state.getValue(ACTIVE) ? "active_overlay" : "inactive_overlay")
      );
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc/machine_block_plated").get(side.getName());
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACINGS, rotation.rotate((Direction)state.getValue(FACINGS)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return (BlockState)state.setValue(FACINGS, mirror.mirror((Direction)state.getValue(FACINGS)));
   }
}
