package ic2.core.block.misc;

import ic2.core.block.misc.base.ScaffoldBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IDoubleSidedModel;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WoodenScaffoldBlock extends ScaffoldBlock implements IBlockModel, IDoubleSidedModel {
   public static IntegerProperty STRENGTH = IntegerProperty.create("strength", 0, 5);

   public WoodenScaffoldBlock() {
      super("scaffold_wooden", 2, 5, Properties.of().strength(0.5F, 0.2F));
      this.setHarvestTool(Tool.AXE);
   }

   @Override
   public IntegerProperty createProps() {
      return STRENGTH;
   }

   @Override
   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      return useContext.getItemInHand().getItem() == IC2Blocks.BARREL.asItem() || super.canBeReplaced(state, useContext);
   }

   @Override
   public ItemStack getUpgradeItem() {
      return new ItemStack(Items.STICK, 2);
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("scaffolding")
         .get(side.getAxis().isVertical() ? "wood_top" : (this.getStrength(state) == this.maxStrength ? "wood_reinforced" : "wood_side"));
   }

   @Override
   public double getMaxSpeed(boolean fast) {
      return fast ? 0.5 : 0.25;
   }
}
