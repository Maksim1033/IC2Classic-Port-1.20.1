package ic2.core.entity.explosion;

import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.item.tool.WrenchTool;
import ic2.core.platform.registries.IC2Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class NukeEntity extends IC2ExplosiveEntity {
   public NukeEntity(EntityType<?> entityTypeIn, Level worldIn) {
      super(entityTypeIn, worldIn);
   }

   public NukeEntity(EntityType<?> entityTypeIn, Level worldIn, double x, double y, double z) {
      super(entityTypeIn, worldIn, x, y, z, 300, (float)IC2.CONFIG.nukeDamage.get(), 0.05F, IC2Blocks.NUKE.defaultBlockState(), IC2DamageSource.NUKE);
   }

   public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
      if (this.getState().getBlock() == Blocks.BEACON) {
         return InteractionResult.PASS;
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (IC2.PLATFORM.isSimulating() && stack.getItem() instanceof WrenchTool wrench && wrench.canTakeDamage(stack, 1)) {
            wrench.damageWrench(stack, 1, player, hand);
            this.remove(RemovalReason.DISCARDED);
            Block.popResource(this.level(), this.blockPosition(), new ItemStack(IC2Blocks.NUKE));
            return InteractionResult.SUCCESS;
         } else {
            return super.interactAt(player, vec, hand);
         }
      }
   }
}
