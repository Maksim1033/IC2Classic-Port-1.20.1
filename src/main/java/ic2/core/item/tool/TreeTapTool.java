package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.misc.tiles.BarrelTileEntity;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TreeTapTool extends IC2Item implements ISimpleItemModel, IToolModel {
   public TreeTapTool() {
      this("tree_tap", null);
   }

   protected TreeTapTool(String itemName, @Nullable PropertiesBuilder properties) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).group(IC2.IC2_MAIN_GROUP).maxDamage(16));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/treetap").get("manual");
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   public InteractionResult useOn(UseOnContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      if (state.getBlock() instanceof RubberwoodLogBlock) {
         boolean server = IC2.PLATFORM.isSimulating();
         if (server && extractRubber(state, context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getItemInHand(), null)) {
            IC2.AUDIO.playSound(context.getPlayer(), IC2Sounds.TREE_TAP, AudioManager.SoundType.ITEM);
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), get(context.getHand()));
            return InteractionResult.SUCCESS;
         }

         if (!server && (Boolean)state.getValue(RubberwoodLogBlock.RESIN) && state.getValue(RubberwoodLogBlock.RESIN_FACING) == context.getClickedFace()) {
            return InteractionResult.SUCCESS;
         }
      } else if (state.getBlock() == IC2Blocks.BARREL
         && context.getClickedFace().getAxis().isHorizontal()
         && this == IC2Items.TREETAP
         && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BarrelTileEntity barrel
         && !barrel.hasTreeTap()
         && barrel.isSimulating()) {
         barrel.setFacing(context.getClickedFace());
         barrel.setHasTreeTap(true);
         barrel.openBarrel();
         context.getItemInHand().shrink(1);
         return InteractionResult.SUCCESS;
      }

      return InteractionResult.PASS;
   }

   public static boolean extractRubber(BlockState state, Level world, BlockPos pos, Direction side, ItemStack stack, List<ItemStack> list) {
      if (!(Boolean)state.getValue(RubberwoodLogBlock.RESIN) || state.getValue(RubberwoodLogBlock.RESIN_FACING) != side) {
         return false;
      } else if ((Boolean)state.getValue(RubberwoodLogBlock.COLLECTABLE)) {
         world.setBlockAndUpdate(pos, (BlockState)state.setValue(RubberwoodLogBlock.COLLECTABLE, false));
         ItemStack drop = new ItemStack(IC2Items.STICKY_RESIN, getResinAmount(stack, 1, 3));
         if (list != null) {
            list.add(drop);
         } else {
            Block.popResourceFromFace(world, pos.relative(side), side.getOpposite(), drop);
         }

         return true;
      } else {
         if (world.random.nextInt(5) == 0) {
            world.setBlockAndUpdate(pos, (BlockState)state.setValue(RubberwoodLogBlock.RESIN, false));
         }

         if (world.random.nextInt(5) == 0) {
            ItemStack drop = new ItemStack(IC2Items.STICKY_RESIN, getResinAmount(stack, 1, 3));
            if (list != null) {
               list.add(drop);
            } else {
               Block.popResourceFromFace(world, pos.relative(side), side.getOpposite(), drop);
            }
         }

         return true;
      }
   }

   public static int getResinAmount(ItemStack stack, int base, int extra) {
      return IC2Item.RANDOM.nextInt(extra + EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack)) + base;
   }

   public static boolean extractRubber(UseOnContext context, List<ItemStack> list) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      return extractRubber(state, context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getItemInHand(), list);
   }
}
