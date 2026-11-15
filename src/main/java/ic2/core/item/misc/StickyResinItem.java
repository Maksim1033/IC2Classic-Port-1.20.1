package ic2.core.item.misc;

import ic2.core.item.base.IC2Item;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class StickyResinItem extends IC2Item implements ISimpleItemModel {
   public StickyResinItem() {
      super("sticky_resin");
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("misc").get("resin");
   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         ItemStack stack = player.getItemInHand(context.getHand());
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() == Blocks.PISTON
            && state.getValue(BlockStateProperties.FACING) == context.getClickedFace()
            && !(Boolean)state.getValue(BlockStateProperties.EXTENDED)) {
            world.setBlockAndUpdate(
               pos,
               (BlockState)Blocks.STICKY_PISTON
                  .defaultBlockState()
                  .setValue(BlockStateProperties.FACING, (Direction)state.getValue(BlockStateProperties.FACING))
            );
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
         } else {
            pos = pos.above();
            state = world.getBlockState(pos);
            if (world.isEmptyBlock(pos) && IC2Blocks.RESIN_SHEET.canSurvive(state, world, pos)) {
               world.setBlockAndUpdate(pos, IC2Blocks.RESIN_SHEET.defaultBlockState());
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         }
      }
   }
}
