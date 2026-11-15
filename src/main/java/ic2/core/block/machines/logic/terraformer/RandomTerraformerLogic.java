package ic2.core.block.machines.logic.terraformer;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RandomTerraformerLogic implements ITerraformerLogic {
   BlockPos lastPos = null;
   int failedAttempts = 0;

   @Override
   public boolean canRun() {
      return true;
   }

   @Override
   public boolean run(ITerraformer terraformer, ItemStack stack, ITerraformerBP bluePrint) {
      Level world = terraformer.getWorldObj();
      BlockPos pos = terraformer.getPosition();
      int maxRadius = bluePrint.getRadius(stack);
      BlockPos nextPos;
      if (this.lastPos != null) {
         int range = Math.max(1, maxRadius / 10);
         nextPos = new BlockPos(
            this.lastPos.getX() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1),
            pos.getY(),
            this.lastPos.getZ() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1)
         );
      } else {
         if (this.failedAttempts > 4) {
            this.failedAttempts = 4;
         }

         int range = maxRadius * (this.failedAttempts + 1) / 5;
         nextPos = new BlockPos(
            pos.getX() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1),
            pos.getY(),
            pos.getZ() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1)
         );
      }

      if (pos.closerThan(nextPos, (double)maxRadius) && bluePrint.terraform(stack, terraformer.getWorldObj(), nextPos, terraformer)) {
         this.failedAttempts = 0;
         this.lastPos = nextPos;
         return true;
      } else {
         this.failedAttempts++;
         this.lastPos = null;
         return false;
      }
   }

   @Override
   public CompoundTag save(CompoundTag tag) {
      tag.putByte("type", (byte)2);
      tag.putInt("fails", this.failedAttempts);
      if (this.lastPos != null) {
         tag.putLong("last", this.lastPos.asLong());
      }

      return tag;
   }

   @Override
   public void load(CompoundTag tag) {
      this.failedAttempts = tag.getInt("fails");
      if (tag.contains("last")) {
         this.lastPos = BlockPos.of(tag.getLong("last"));
      }
   }
}
