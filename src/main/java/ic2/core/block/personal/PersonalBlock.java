package ic2.core.block.personal;

import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.base.tiles.impls.BasePersonalTileEntity;
import ic2.core.block.base.tiles.impls.BaseTradeOMatTileEntity;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraftforge.common.ForgeHooks;

public class PersonalBlock extends BaseFacingBlock<BaseTileEntity> {
   public PersonalBlock(String blockName, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type) {
      this(blockName, provider, type, Properties.of().strength(-1.0F, 6000000.0F));
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   protected PersonalBlock(String blockName, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type, Properties properties) {
      super(blockName, properties.requiresCorrectToolForDrops(), provider, type);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void setPlaceData(BaseTileEntity tile, BlockState state, LivingEntity placer, ItemStack stack) {
      if (tile instanceof BasePersonalTileEntity) {
         if (placer instanceof Player) {
            ((BasePersonalTileEntity)tile).setOwner(placer.getUUID());
         }

         CompoundTag data = StackUtil.getNbtData(stack).getCompound("tile_data");
         if (!data.isEmpty()) {
            ((BasePersonalTileEntity)tile).readItemData(data, true);
         }
      } else if (tile instanceof BaseTradeOMatTileEntity && placer instanceof Player) {
         ((BaseTradeOMatTileEntity)tile).setOwner(placer.getUUID());
      }
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack stack, RandomSource rand, BlockEntity tile, boolean wrench) {
      ItemStack drop = new ItemStack(this);
      if (tile instanceof BasePersonalTileEntity) {
         drop.addTagElement("tile_data", ((BasePersonalTileEntity)tile).writeItemData(new CompoundTag()));
      }

      return drop;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof BasePersonalTileEntity personal && personal.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ADMIN, false)) {
         boolean isCorrect = ForgeHooks.isCorrectToolForDrops(state, player);
         return player.getDigSpeed(state, pos) / (isCorrect ? 240.0F : 800.0F);
      }

      return super.getDestroyProgress(state, player, world, pos);
   }
}
