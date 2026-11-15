package ic2.core.item.block;

import ic2.core.IC2;
import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.rendering.features.item.IColoredItemModel;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;

public class FluidBlockItem extends BlockItem implements ISimpleItemModel, IColoredItemModel, IItemColorListener, IRegistryProvider {
   ResourceLocation id;

   public FluidBlockItem(Block blockIn) {
      super(blockIn, new Properties());
      IC2.MOD_CACHE.setActiveMod("minecraft");
      this.id = ForgeRegistryWrapper.getBlockKey(blockIn);
      IC2.MOD_CACHE.reset();
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(IClientFluidTypeExtensions.of(this.getBlock().defaultBlockState().getFluidState()).getStillTexture());
   }

   @Override
   public int getTintedIndexForModel(ItemStack stack, int layer) {
      return 0;
   }

   protected SoundEvent getPlaceSound(BlockState state) {
      SoundEvent event = state.getFluidState().getType().getFluidType().getSound(SoundActions.BUCKET_FILL);
      if (event == null) {
         event = state.getFluidState().getType().is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
      }

      return event;
   }

   protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
      return context.getLevel().dimension().equals(Level.NETHER) && this.getBlock() == Blocks.WATER ? false : super.placeBlock(context, state);
   }

   protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
      return this.getPlaceSound(state);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      return IClientFluidTypeExtensions.of(this.getBlock().defaultBlockState().getFluidState()).getTintColor();
   }
}
