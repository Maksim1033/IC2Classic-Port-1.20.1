package ic2.core.block.base.drops;

import ic2.core.platform.registries.IC2Blocks;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockDropProvider {
   IBlockDropProvider SELF = new IBlockDropProvider.SelfProvider();
   IBlockDropProvider SELF_OR_MACHINE = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.MACHINE_BLOCK));
   IBlockDropProvider SELF_OR_ADV_MACHINE = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.ADVANCED_MACHINE_BLOCK));
   IBlockDropProvider SELF_OR_STABLE_MACHINE = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.STABILIZED_MACHINE_BLOCK));
   IBlockDropProvider SELF_OR_GENERATOR = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.GENERATOR));
   IBlockDropProvider SELF_OR_COLOSSAL = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.COLOSSAL_BASE));
   IBlockDropProvider SELF_OR_BATBOX = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.BATBOX));
   IBlockDropProvider SELF_OR_MFE = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.MFE));
   IBlockDropProvider SELF_OR_MFSU = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.MFSU));
   IBlockDropProvider SELF_OR_ESU = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.ESU));
   IBlockDropProvider SELF_OR_ISU = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.ISU));
   IBlockDropProvider SELF_OR_MV_TRANSFORMER = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.TRANSFORMER_MV));
   IBlockDropProvider SELF_OR_HV_TRANSFORMER = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.TRANSFORMER_HV));
   IBlockDropProvider SELF_OR_EV_TRANSFORMER = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.TRANSFORMER_EV));
   IBlockDropProvider SELF_OR_IV_TRANSFORMER = new IBlockDropProvider.SelfOrOther(() -> new ItemStack(IC2Blocks.TRANSFORMER_IV));

   static IBlockDropProvider custom(Supplier<ItemStack> supplier) {
      return new IBlockDropProvider.CustomProvider(supplier);
   }

   ItemStack createDrop(BlockState var1, ItemStack var2, RandomSource var3, @Nullable BlockEntity var4, boolean var5);

   public static class CustomProvider implements IBlockDropProvider {
      Supplier<ItemStack> drop;

      public CustomProvider(Supplier<ItemStack> drop) {
         this.drop = drop;
      }

      @Override
      public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile, boolean wrench) {
         return this.drop.get();
      }
   }

   public static class SelfOrOther implements IBlockDropProvider {
      Supplier<ItemStack> drop;

      public SelfOrOther(Supplier<ItemStack> drop) {
         this.drop = drop;
      }

      @Override
      public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile, boolean wrench) {
         return wrench ? new ItemStack(state.getBlock()) : this.drop.get();
      }
   }

   public static class SelfProvider implements IBlockDropProvider {
      @Override
      public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile, boolean wrench) {
         return new ItemStack(state.getBlock());
      }
   }
}
