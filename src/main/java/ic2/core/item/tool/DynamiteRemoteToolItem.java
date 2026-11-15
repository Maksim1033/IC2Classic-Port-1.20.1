package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.misc.explosive.DynamiteBlock;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DynamiteRemoteToolItem extends IC2Item implements IToolModel, ISimpleItemModel {
   public static ResourceLocation AUDIO_FILE = new ResourceLocation("ic2", "sounds/tools/dynamite_remote.ogg");

   public DynamiteRemoteToolItem() {
      super("dynamite_remote", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).maxStackSize(1));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("explosive").get("dynamite-o-remote");
   }

   public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      int count = this.getStoredLinkCount(stack);
      if (count > 0) {
         tooltip.add(this.translate("tooltip.item.ic2.dynamite_remote.count", new Object[]{count}));
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (this.getStoredLinkCount(stack) > 0 && playerIn.isShiftKeyDown()) {
         if (IC2.PLATFORM.isSimulating()) {
            this.blowUp(worldIn, stack);
            IC2.AUDIO.playSound(playerIn, AUDIO_FILE, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
         }

         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public int getStoredLinkCount(ItemStack stack) {
      return StackUtil.getNbtData(stack).getList("positions", 10).size();
   }

   public void blowUp(Level world, ItemStack stack) {
      String dim = world.dimension().location().toString();
      MutableBlockPos mutable = new MutableBlockPos();
      Iterator<CompoundTag> iter = NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("positions", 10), CompoundTag.class).iterator();

      while (iter.hasNext()) {
         CompoundTag nbt = iter.next();
         mutable.set(nbt.getLong("pos"));
         if (dim.equals(nbt.getString("dim")) && world.isLoaded(mutable)) {
            BlockState state = world.getBlockState(mutable);
            if (state.getBlock() == IC2Blocks.DYNAMITE) {
               DynamiteBlock.ignite(state, world, mutable);
               iter.remove();
            }
         }
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() == IC2Blocks.DYNAMITE && !context.getPlayer().isShiftKeyDown()) {
         if (DynamiteBlock.isLinked(state)) {
            if (this.removeDynamite(world, pos, stack)) {
               DynamiteBlock.setLinked(state, world, pos, false);
               return InteractionResult.SUCCESS;
            }
         } else {
            DynamiteBlock.setLinked(state, world, pos, true);
         }

         this.addDynamite(world, pos, stack);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void addDynamite(Level world, BlockPos pos, ItemStack stack) {
      ListTag list = stack.getOrCreateTag().getList("positions", 10);
      CompoundTag nbt = new CompoundTag();
      nbt.putString("dim", world.dimension().location().toString());
      nbt.putLong("pos", pos.asLong());
      list.add(nbt);
      stack.addTagElement("positions", list);
   }

   public boolean removeDynamite(Level world, BlockPos pos, ItemStack stack) {
      String dim = world.dimension().location().toString();
      MutableBlockPos mutable = new MutableBlockPos();
      Iterator<CompoundTag> iter = NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("positions", 10), CompoundTag.class).iterator();

      while (iter.hasNext()) {
         CompoundTag nbt = iter.next();
         mutable.set(nbt.getLong("pos"));
         if (dim.equals(nbt.getString("dim")) && mutable.equals(pos)) {
            iter.remove();
            return true;
         }
      }

      return false;
   }
}
