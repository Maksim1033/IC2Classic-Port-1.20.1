package ic2.core.block.misc;

import ic2.api.items.ItemRegistries;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.base.FenceModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IronFenceBlock extends FenceBlock implements ICustomBlockModel, IAutoCreator, IDualLogged, IRegistryProvider, IToolProvider {
   Tool tool = Tool.PICKAXE.withLevel(1);
   public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
   public static final BooleanProperty LAVA_LOGGED = IC2Properties.LAVA_LOGGED;

   public IronFenceBlock() {
      super(Properties.copy(Blocks.IRON_BARS));
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(CHARGE, 0)).setValue(LAVA_LOGGED, false));
   }

   @Override
   public void registerTools() {
      this.tool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "iron_fence");
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{CHARGE}).add(new Property[]{LAVA_LOGGED});
   }

   public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
      FluidState fluidstate = p_196258_1_.getLevel().getFluidState(p_196258_1_.getClickedPos());
      return (BlockState)super.getStateForPlacement(p_196258_1_).setValue(LAVA_LOGGED, fluidstate.getType() == Fluids.LAVA);
   }

   public FluidState getFluidState(BlockState p_204507_1_) {
      return IDualLogged.getFluidState(p_204507_1_);
   }

   public BlockState updateShape(
      BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, LevelAccessor p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_
   ) {
      if ((Boolean)p_196271_1_.getValue(LAVA_LOGGED)) {
         p_196271_4_.scheduleTick(p_196271_5_, Fluids.LAVA, Fluids.LAVA.getTickDelay(p_196271_4_));
      }

      return super.updateShape(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new FenceModel(state, "misc", "iron_fence");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   public void setCharge(Level world, BlockPos pos, BlockState state, int charge) {
      world.setBlockAndUpdate(pos, (BlockState)state.setValue(CHARGE, charge));
   }

   public int getCharge(BlockState state) {
      return (Integer)state.getValue(CHARGE);
   }

   public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
      if (entityIn instanceof Player player) {
         if (ItemRegistries.isMetalArmor(player, EquipmentSlot.FEET)) {
            int charge = this.getCharge(state);
            if (charge > 0) {
               this.setCharge(worldIn, pos, state, charge - 1);
               Vec3 motion = player.getDeltaMovement();
               player.setDeltaMovement(
                  motion.x,
                  Math.min((motion.y + 0.075) * (motion.y > 0.0 ? 1.03 : 1.0), player.isShiftKeyDown() ? (motion.y > 0.0 ? motion.y - 0.03 : -0.3) : 1.5),
                  motion.z
               );
               player.fallDistance = 0.0F;
            }
         }
      }
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = CollectionUtils.<ItemStack>createList();
         drops.add(new ItemStack(this));
         return drops;
      }
   }
}
