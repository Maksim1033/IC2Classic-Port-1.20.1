package ic2.core.block.misc.rubberwood;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.rendering.block.base.DoorModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.base.IC2TexturedBlockItem;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodDoorBlock extends DoorBlock implements ICustomBlockModel, IAutoCreator, IRenderType, IRegistryProvider, IToolProvider {
   Tool harvestTool = Tool.AXE.withLevel(1);

   public RubberwoodDoorBlock() {
      super(Properties.of().strength(3.0F, 3.0F).sound(SoundType.WOOD), BlockSetType.OAK);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                     .setValue(OPEN, Boolean.FALSE))
                  .setValue(HINGE, DoorHingeSide.LEFT))
               .setValue(POWERED, Boolean.FALSE))
            .setValue(HALF, DoubleBlockHalf.LOWER)
      );
   }

   @Override
   public void registerTools() {
      this.harvestTool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "rubberwood_door");
   }

   @Override
   public List<BlockState> getCustomStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new DoorModel(state, "resources/rubberwood", "door_upper", "door_lower");
   }

   public IC2BlockItem createItem() {
      return new IC2TexturedBlockItem(this, "misc", "rubberwood_door");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = CollectionUtils.<ItemStack>createList();
         if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            drops.add(new ItemStack(this));
         }

         return drops;
      }
   }
}
