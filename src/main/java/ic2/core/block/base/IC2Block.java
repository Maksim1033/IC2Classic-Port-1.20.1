package ic2.core.block.base;

import ic2.core.block.base.drops.IRarityProvider;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.ILangHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.GameData;

public abstract class IC2Block extends Block implements IAutoCreator, IRarityProvider, IRegistryProvider, ILangHelper, IToolProvider {
   Rarity rarityOverride = null;
   protected EnumMap<Tool.ToolType, Tool> harvestLevels = new EnumMap<>(Tool.ToolType.class);
   protected ResourceLocation id;

   public IC2Block(String blockName, Properties properties) {
      super(properties);
      this.id = GameData.checkPrefix(blockName, false);
   }

   @Override
   public void registerTools() {
      for (Tool tool : this.harvestLevels.values()) {
         tool.register(this);
      }
   }

   public IC2Block setHarvestTool(Tool tier) {
      this.harvestLevels.put(tier.getType(), tier);
      return this;
   }

   public IC2Block setOverrideRarity(Rarity rarity) {
      this.rarityOverride = rarity;
      return this;
   }

   @Override
   public Rarity getRarity(ItemStack stack) {
      return this.rarityOverride;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
         stack.setHoverName(((Nameable)tile).getCustomName());
      }

      return stack;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         ObjectArrayList<ItemStack> drops = new ObjectArrayList<>();
         BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
         ItemStack stack = this.createDrop(state, tool != null ? tool : ItemStack.EMPTY, builder.getLevel().random, tile);
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

         LootParams params = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
         LootContext context = new LootContext.Builder(params).create(this.getLootTable());
         ForgeHooks.modifyLoot(this.getLootTable(), drops, context);
         return drops;
      }
   }

   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, @Nullable BlockEntity tile) {
      return new ItemStack(this);
   }

   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
   }
}
