package ic2.core.item.misc;

import ic2.core.item.base.IC2Item;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2MinecartItem extends IC2Item implements ISimpleItemModel {
   BiFunction<Level, Vec3, AbstractMinecart> creator;
   String texture;

   public IC2MinecartItem(String itemName, String texture, BiFunction<Level, Vec3, AbstractMinecart> creator) {
      super(itemName);
      this.texture = texture;
      this.creator = creator;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "minecart").get(this.texture);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = level.getBlockState(pos);
      if (!state.is(BlockTags.RAILS)) {
         return InteractionResult.FAIL;
      } else {
         ItemStack itemstack = context.getItemInHand();
         if (!level.isClientSide) {
            RailShape railshape = state.getBlock() instanceof BaseRailBlock
               ? ((BaseRailBlock)state.getBlock()).getRailDirection(state, level, pos, null)
               : RailShape.NORTH_SOUTH;
            double d0 = 0.0;
            if (railshape.isAscending()) {
               d0 = 0.5;
            }

            AbstractMinecart abstractminecart = this.creator.apply(level, Vec3.upFromBottomCenterOf(pos, d0));
            if (itemstack.hasCustomHoverName()) {
               abstractminecart.setCustomName(itemstack.getHoverName());
            }

            level.addFreshEntity(abstractminecart);
            level.gameEvent(GameEvent.ENTITY_PLACE, pos, Context.of(context.getPlayer(), level.getBlockState(pos.below())));
         }

         itemstack.shrink(1);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }
}
