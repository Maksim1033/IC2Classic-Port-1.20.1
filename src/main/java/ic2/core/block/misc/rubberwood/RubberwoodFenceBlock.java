package ic2.core.block.misc.rubberwood;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.rendering.block.base.FenceModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodFenceBlock extends FenceBlock implements ICustomBlockModel, IAutoCreator, IRegistryProvider, IToolProvider {
   Tool tool = Tool.AXE.withLevel(1);

   public RubberwoodFenceBlock() {
      super(Properties.copy(IC2Blocks.RUBBERWOOD_PLANKS));
   }

   @Override
   public void registerTools() {
      this.tool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "rubber_fence");
   }

   @Override
   public List<BlockState> getCustomStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new FenceModel(state, "resources/rubberwood", "planks");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
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
