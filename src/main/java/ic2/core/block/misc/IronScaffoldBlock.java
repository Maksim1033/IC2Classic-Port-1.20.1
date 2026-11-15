package ic2.core.block.misc;

import ic2.core.block.misc.base.ScaffoldBlock;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IDoubleSidedModel;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IronScaffoldBlock extends ScaffoldBlock implements IBlockModel, IDoubleSidedModel {
   public static IntegerProperty STRENGTH = IntegerProperty.create("strength", 0, 12);

   public IronScaffoldBlock() {
      super("scaffold_iron", 5, 12, Properties.of().strength(0.8F, 10.0F).requiresCorrectToolForDrops());
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   public IntegerProperty createProps() {
      return STRENGTH;
   }

   @Override
   public ItemStack getUpgradeItem() {
      return new ItemStack(IC2Items.INGOT_REFINED_IRON);
   }

   @Override
   public double getMaxSpeed(boolean fast) {
      return fast ? 0.6 : 0.3;
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("scaffolding")
         .get(side.getAxis().isVertical() ? "iron_top" : (this.getStrength(state) == this.maxStrength ? "iron_reinforced" : "iron_side"));
   }

   @Override
   protected void onScaffoldPlaced(Player player, Level level, BlockPos clickPos, BlockPos placePos, boolean vertical) {
      if (clickPos.getY() <= level.getMinBuildHeight() + 10 && placePos.getY() >= level.getMaxBuildHeight() - 1) {
         IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:scaffold"));
      }
   }
}
