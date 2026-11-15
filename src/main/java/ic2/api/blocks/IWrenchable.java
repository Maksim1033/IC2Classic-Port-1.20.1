package ic2.api.blocks;

import ic2.api.blocks.wrench.ChestWrenchHandler;
import ic2.api.blocks.wrench.DispenserWrenchHandler;
import ic2.api.blocks.wrench.HopperWrenchHandler;
import ic2.api.blocks.wrench.HorizontalWrenchHandler;
import ic2.api.blocks.wrench.InvertedHorizontalWrenchHandler;
import ic2.api.blocks.wrench.ObserverBlockWrenchHandler;
import ic2.api.blocks.wrench.PillarWrenchHandler;
import ic2.api.blocks.wrench.PistonWrenchHandler;
import ic2.api.blocks.wrench.StairWrenchHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public interface IWrenchable {
   Direction getFacing(BlockState var1, Level var2, BlockPos var3);

   boolean canSetFacing(BlockState var1, Level var2, BlockPos var3, Player var4, Direction var5);

   boolean setFacing(BlockState var1, Level var2, BlockPos var3, Player var4, Direction var5);

   boolean doSpecialAction(BlockState var1, Level var2, BlockPos var3, Direction var4, Player var5, Vec3 var6);

   AABB hasSpecialAction(BlockState var1, Level var2, BlockPos var3, Direction var4, Player var5, Vec3 var6);

   boolean canRemoveBlock(BlockState var1, Level var2, BlockPos var3, Player var4);

   double getDropRate(BlockState var1, Level var2, BlockPos var3, Player var4);

   List<ItemStack> getDrops(BlockState var1, Level var2, BlockPos var3, Player var4);

   public static final class WrenchRegistry {
      public static final IWrenchable.WrenchRegistry INSTANCE = new IWrenchable.WrenchRegistry();
      Map<Block, IWrenchable> wrenches = new Object2ObjectOpenHashMap();

      public void init() {
         this.registerWrenchHandler(PistonWrenchHandler.INSTANCE, Blocks.PISTON, Blocks.STICKY_PISTON);
         this.registerWrenchHandler(ObserverBlockWrenchHandler.INSTANCE, Blocks.OBSERVER);
         this.registerWrenchHandler(ChestWrenchHandler.INSTANCE, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST);
         this.registerWrenchHandler(HorizontalWrenchHandler.INSTANCE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER);
         this.registerWrenchHandler(InvertedHorizontalWrenchHandler.INSTANCE, Blocks.REPEATER, Blocks.COMPARATOR);
         this.registerWrenchHandler(
            PillarWrenchHandler.INSTANCE,
            Blocks.ACACIA_LOG,
            Blocks.BIRCH_LOG,
            Blocks.DARK_OAK_LOG,
            Blocks.JUNGLE_LOG,
            Blocks.OAK_LOG,
            Blocks.SPRUCE_LOG,
            Blocks.STRIPPED_ACACIA_LOG,
            Blocks.STRIPPED_BIRCH_LOG,
            Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.STRIPPED_OAK_LOG,
            Blocks.STRIPPED_SPRUCE_LOG
         );
         this.registerWrenchHandler(DispenserWrenchHandler.INSTANCE, Blocks.DISPENSER, Blocks.DROPPER);
         this.registerWrenchHandler(HopperWrenchHandler.INSTANCE, Blocks.HOPPER);

         for (Block block : ForgeRegistries.BLOCKS) {
            if (block instanceof StairBlock) {
               this.registerWrenchHandler(StairWrenchHandler.INSTANCE, block);
            } else if (block instanceof GlazedTerracottaBlock) {
               this.registerWrenchHandler(InvertedHorizontalWrenchHandler.INSTANCE, block);
            }
         }
      }

      public void registerWrenchHandler(IWrenchable wrench, Block... blocks) {
         for (Block block : blocks) {
            this.wrenches.put(block, wrench);
         }
      }

      public IWrenchable getWrenchable(BlockState state) {
         IWrenchable wrench = this.wrenches.get(state.getBlock());
         return wrench != null ? wrench : (state.getBlock() instanceof IWrenchable result ? result : null);
      }
   }
}
