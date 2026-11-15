package ic2.core.block.misc.rubberwood;

import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodPressurePlateBlock extends PressurePlateBlock implements IAutoCreator, IBlockModel, IBlockModifiers, IRegistryProvider, IToolProvider {
   Tool tool = Tool.AXE.withLevel(1);

   public RubberwoodPressurePlateBlock() {
      super(Sensitivity.EVERYTHING, Properties.copy(IC2Blocks.RUBBERWOOD_PLANKS), BlockSetType.OAK);
   }

   @Override
   public void registerTools() {
      this.tool.register(this);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "rubberwood_pressure_plate");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("planks");
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return state.getValue(POWERED) ? new AABB(1.0, 0.0, 1.0, 15.0, 0.5, 15.0) : new AABB(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      return false;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      return 0;
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return true;
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      return side != Direction.DOWN && side != Direction.UP
         ? new float[]{1.0F, state.getValue(POWERED) ? 14.5F : 14.0F, 15.0F, 15.0F}
         : new float[]{1.0F, 1.0F, 15.0F, 15.0F};
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
