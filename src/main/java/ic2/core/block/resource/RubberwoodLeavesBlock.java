package ic2.core.block.resource;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.misc.color.IBlockColorListener;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IColoredBlockModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodLeavesBlock
   extends LeavesBlock
   implements IAutoCreator,
   IBlockModel,
   IColoredBlockModel,
   IBlockColorListener,
   IRenderType,
   IRegistryProvider,
   IToolProvider {
   Tool harvestTool = Tool.HOE;

   public RubberwoodLeavesBlock() {
      super(
         Properties.copy(Blocks.OAK_LEAVES)
            .noOcclusion()
            .isValidSpawn((S, W, P, E) -> E == EntityType.OCELOT || E == EntityType.PARROT)
            .isSuffocating((K, V, E) -> false)
            .isViewBlocking((K, V, E) -> false)
      );
   }

   @Override
   public void registerTools() {
      this.harvestTool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "rubber_leaves");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/birch_leaves"));
   }

   public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return 30;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return 60;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      return 8431445;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
      return 8431445;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return Minecraft.useFancyGraphics() ? RenderType.cutoutMipped() : RenderType.solid();
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         ItemStack stack = builder.getOptionalParameter(LootContextParams.TOOL);
         if (stack != null && (stack.getItem() == Items.SHEARS || stack.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0)) {
            drops.add(new ItemStack(this));
         }

         if (builder.getLevel().random.nextInt(35) == 0) {
            drops.add(new ItemStack(IC2Blocks.RUBBER_SAPLING));
         }

         return drops;
      }
   }

   @Override
   public int getTintedIndexFor(BlockState state, Direction dir, int layer) {
      return 1;
   }
}
