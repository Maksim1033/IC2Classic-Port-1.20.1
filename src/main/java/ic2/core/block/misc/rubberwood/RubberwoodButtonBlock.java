package ic2.core.block.misc.rubberwood;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.rendering.block.base.ButtonModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodButtonBlock extends ButtonBlock implements IAutoCreator, ICustomBlockModel, IRegistryProvider {
   public RubberwoodButtonBlock() {
      super(Properties.copy(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "rubberwood_button");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new ButtonModel(state, "resources/rubberwood", "planks");
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
