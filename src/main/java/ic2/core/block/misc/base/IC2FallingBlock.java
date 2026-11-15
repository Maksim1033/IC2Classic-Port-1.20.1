package ic2.core.block.misc.base;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2FallingBlock extends FallingBlock implements IBlockModel, IAutoCreator, IToolProvider {
   ResourceLocation id;
   String name;
   Tool tool;

   public IC2FallingBlock(String name, Tool tool, Properties properties) {
      super(properties.requiresCorrectToolForDrops());
      this.name = name;
      this.tool = tool;
      this.id = GameData.checkPrefix(name, false);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public void registerTools() {
      this.tool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("resources/dust").get(this.name);
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         ItemStack stack = this.createDrop(state, builder.getOptionalParameter(LootContextParams.TOOL), builder.getLevel().random, tile);
         if (!stack.isEmpty()) {
            if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
               stack.setHoverName(((Nameable)tile).getCustomName());
            }

            drops.add(stack);
         }

         this.addExtraDrops(drops, state, stack, builder.getLevel().getRandom());
         if (tile instanceof IDropProvider) {
            ((IDropProvider)tile).addDrops(drops);
         }

         return drops;
      }
   }

   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, @Nullable BlockEntity tile) {
      return new ItemStack(this);
   }

   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
   }
}
