package ic2.core.item.block;

import ic2.core.block.cables.CableBlock;
import ic2.core.block.cables.CableTileEntity;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CableItem extends IC2BlockItem implements ISimpleItemModel, IAdvancedTooltip {
   String texture;
   BlockState state;
   int euTransfer = 0;
   double euLoss = 0.0;

   public CableItem(String name, BlockState state, String texture) {
      super(state.getBlock(), name);
      this.texture = texture;
      this.state = state;
      if ((state.hasBlockEntity() ? ((EntityBlock)state.getBlock()).newBlockEntity(BlockPos.ZERO, state) : null) instanceof CableTileEntity cable) {
         cable.insulation = (byte)((CableBlock)state.getBlock()).getInsulation(state);
         this.euTransfer = cable.getConductorBreakdownEnergy() - 1;
         this.euLoss = cable.getConductionLoss();
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      if (PlayerHandler.getHandler(player).hasEUReader()) {
         helper.addSimpleToolTip("tooltip.item.ic2.eu_reader.cable_limit", this.euTransfer);
         helper.addSimpleToolTip("tooltip.item.ic2.eu_reader.cable_loss", Formatters.CABLE_LOSS_FORMAT.format(this.euLoss));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "cable").get(this.texture);
   }

   public InteractionResult useOn(UseOnContext context) {
      InteractionResult actionResultType = this.place(new BlockPlaceContext(context));
      return actionResultType != InteractionResult.SUCCESS && this.isEdible()
         ? this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult()
         : actionResultType;
   }

   public InteractionResult place(BlockPlaceContext context) {
      if (!context.canPlace()) {
         return InteractionResult.FAIL;
      } else if (!context.getLevel()
         .setBlock(
            context.getClickedPos(),
            (BlockState)this.state.setValue(CableBlock.WATER, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER),
            11
         )) {
         return InteractionResult.FAIL;
      } else {
         BlockPos blockpos = context.getClickedPos();
         Level world = context.getLevel();
         Player Player = context.getPlayer();
         ItemStack itemstack = context.getItemInHand();
         BlockState blockState1 = world.getBlockState(blockpos);
         Block block = blockState1.getBlock();
         if (block == this.state.getBlock()) {
            BlockItem.updateCustomBlockEntityTag(world, Player, blockpos, itemstack);
            block.setPlacedBy(world, blockpos, blockState1, Player, itemstack);
            if (Player instanceof ServerPlayer) {
               CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)Player, blockpos, itemstack);
            }

            if (world.getBlockEntity(blockpos) instanceof CableTileEntity cable && this.state.getBlock() instanceof CableBlock cableBlock) {
               cable.lock();
               if (cableBlock.hasInsulation()) {
                  cable.setInsulation((Integer)this.state.getValue(cableBlock.getInsulation()));
               }

               cable.unlock();
            }
         }

         SoundType soundtype = blockState1.getSoundType(world, blockpos, context.getPlayer());
         world.playSound(
            Player,
            blockpos,
            blockState1.getSoundType().getPlaceSound(),
            SoundSource.BLOCKS,
            (soundtype.getVolume() + 1.0F) / 2.0F,
            soundtype.getPitch() * 0.8F
         );
         itemstack.shrink(1);
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         items.add(new ItemStack(this));
      }
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public void registerBlocks(Map<Block, Item> blockToItemMap, Item itemIn) {
   }

   public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
   }
}
