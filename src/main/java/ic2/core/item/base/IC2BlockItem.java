package ic2.core.item.base;

import ic2.core.IC2;
import ic2.core.block.base.drops.IRarityProvider;
import ic2.core.block.base.misc.color.IColorListener;
import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2BlockItem extends BlockItem implements IItemColorListener, IRegistryProvider, IItemCategoryFiller {
   boolean noCreative;
   protected ResourceLocation id;

   public IC2BlockItem(Block block, Properties properties) {
      super(block, properties);
      this.id = ForgeRegistryWrapper.getBlockKey(block);
   }

   public IC2BlockItem(Block block) {
      this(block, new Properties());
   }

   public IC2BlockItem(Block block, String name) {
      super(block, new Properties());
      this.id = GameData.checkPrefix(name, false);
   }

   public IC2BlockItem disableCreative() {
      this.noCreative = true;
      return this;
   }

   public boolean isCreativeDisabled() {
      return this.noCreative;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public Rarity getRarity(ItemStack stack) {
      if (this.getBlock() instanceof IRarityProvider) {
         Rarity rarity = ((IRarityProvider)this.getBlock()).getRarity(stack);
         if (rarity != null) {
            return rarity;
         }
      }

      return super.getRarity(stack);
   }

   @Override
   public boolean needsColoring() {
      Block block = this.getBlock();
      return block instanceof IColorListener && ((IColorListener)block).needsColoring();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      Block block = this.getBlock();
      return block instanceof IItemColorListener ? ((IItemColorListener)block).getItemColor(stack, tintIndex) : -1;
   }


}
