package ic2.core.block.misc;

import ic2.core.block.base.IC2Block;
import ic2.core.block.misc.tiles.IridiumStoneTileEntity;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

public class IridiumStone extends IC2Block implements IBlockModel, EntityBlock {
   public IridiumStone() {
      super("iridium_stone", Properties.of().strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops());
      this.setHarvestTool(Tool.PICKAXE.withLevel(3));
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("personal").get("iridium_stone");
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.IRIDIUM_STONE.create(pos, state);
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (placer instanceof Player) {
         BlockEntity tile = worldIn.getBlockEntity(pos);
         if (tile instanceof IridiumStoneTileEntity) {
            ((IridiumStoneTileEntity)tile).setOwner(placer.getUUID());
         }
      }
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
      return false;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof IridiumStoneTileEntity iridium) {
         if (iridium.canDoAction(player.getUUID(), Action.BREAK_IRIDIUM, false)) {
            boolean isCorrect = ForgeHooks.isCorrectToolForDrops(state, player);
            return player.getDigSpeed(state, pos) / (isCorrect ? 240.0F : 800.0F);
         } else {
            return 0.0F;
         }
      } else {
         return super.getDestroyProgress(state, player, world, pos);
      }
   }
}
