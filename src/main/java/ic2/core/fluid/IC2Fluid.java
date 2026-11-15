package ic2.core.fluid;

import ic2.core.platform.rendering.features.IRenderType;
import java.util.function.Consumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidType.Properties;

public class IC2Fluid extends Fluid implements IRenderType {
   FluidType fluidType;

   public IC2Fluid(FluidType fluidType) {
      this.fluidType = fluidType;
   }

   public static FluidType createType(Properties props, ResourceLocation still) {
      return createType(props, still, still, null);
   }

   public static FluidType createType(Properties props, ResourceLocation still, Integer color) {
      return createType(props, still, still, color);
   }

   public static FluidType createType(Properties props, ResourceLocation still, ResourceLocation flowing) {
      return createType(props, still, flowing, null);
   }

   public static FluidType createType(Properties props, final ResourceLocation still, final ResourceLocation flowing, final Integer color) {
      return new FluidType(props) {
         public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IC2Fluid.FluidInfo(still, flowing, color));
         }
      };
   }

   public FluidType getFluidType() {
      return this.fluidType;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.translucent();
   }

   public Item getBucket() {
      return Items.AIR;
   }

   public boolean canBeReplacedWith(FluidState p_215665_1_, BlockGetter p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
      return true;
   }

   public Vec3 getFlow(BlockGetter p_215663_1_, BlockPos p_215663_2_, FluidState p_215663_3_) {
      return Vec3.ZERO;
   }

   public int getTickDelay(LevelReader p_205569_1_) {
      return 0;
   }

   protected boolean isEmpty() {
      return true;
   }

   protected float getExplosionResistance() {
      return 0.0F;
   }

   public float getHeight(FluidState p_215662_1_, BlockGetter p_215662_2_, BlockPos p_215662_3_) {
      return 0.0F;
   }

   public float getOwnHeight(FluidState p_223407_1_) {
      return 0.0F;
   }

   protected BlockState createLegacyBlock(FluidState p_204527_1_) {
      return Blocks.AIR.defaultBlockState();
   }

   public boolean isSource(FluidState p_207193_1_) {
      return true;
   }

   public int getAmount(FluidState p_207192_1_) {
      return 0;
   }

   public VoxelShape getShape(FluidState p_215664_1_, BlockGetter p_215664_2_, BlockPos p_215664_3_) {
      return Shapes.empty();
   }

   private static class FluidInfo implements IClientFluidTypeExtensions {
      ResourceLocation still;
      ResourceLocation flowing;
      Integer color;

      public FluidInfo(ResourceLocation still, ResourceLocation flowing, Integer color) {
         this.still = still;
         this.flowing = flowing;
         this.color = color;
      }

      public int getTintColor() {
         return this.color != null ? this.color : 0xFFFFFFFF;
      }

      public ResourceLocation getStillTexture() {
         return this.still;
      }

      public ResourceLocation getFlowingTexture() {
         return this.flowing;
      }
   }
}
