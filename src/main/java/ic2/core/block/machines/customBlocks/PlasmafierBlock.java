package ic2.core.block.machines.customBlocks;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.block.rendering.block.PlasmafierModel;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlasmafierBlock extends BaseMachineBlock implements ICustomBlockModel, IRenderType {
   public static final Properties PLASMAFIER = Properties.of()
      .sound(SoundType.METAL)
      .strength(5.0F, 25.0F)
      .requiresCorrectToolForDrops()
      .emissiveRendering((S, W, P) -> true);

   public PlasmafierBlock() {
      super("plasmafier", PLASMAFIER, IBlockDropProvider.SELF_OR_ADV_MACHINE, ITextureProvider.toggleIC2("machine/ev/plasmafier"), IC2Tiles.PLASMAFIER);
   }

   @Override
   public List<BlockState> getModelStates() {
      return ObjectLists.emptyList();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new PlasmafierModel(state, this);
   }

   @Deprecated
   public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return world instanceof EmptyBlockGetter ? Shapes.block() : super.getCollisionShape(state, world, pos, context);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }
}
