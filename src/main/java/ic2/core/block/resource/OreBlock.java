package ic2.core.block.resource;

import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OreBlock extends IC2Block implements IBlockModel {
   private String name;
   private IntProvider dropRate;
   private OreBlock.Ore drop;

   public OreBlock(String blockName, float hardness, float resistance, int harvestLevel, boolean deepslate, IntProvider dropRate, OreBlock.Ore drop) {
      super(
         blockName,
         Properties.of()
            .sound(SoundType.STONE)
            .mapColor(deepslate ? MapColor.DEEPSLATE : MapColor.STONE)
            .strength(hardness, resistance)
            .requiresCorrectToolForDrops()
      );
      this.setHarvestTool(Tool.PICKAXE.withLevel(harvestLevel));
      this.name = blockName;
      this.dropRate = dropRate;
      this.drop = drop;
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, @Nullable BlockEntity tile) {
      return tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0
         ? new ItemStack(this)
         : new ItemStack(this.drop.getDrop(), this.generateLoot(rand, tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE)));
   }

   private int generateLoot(RandomSource source, int lootLevel) {
      return this.dropRate.sample(source) * (Math.max(0, source.nextInt(lootLevel + 2) - 1) + 1);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.singletonList(this.defaultBlockState());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("resources/ore").get(this.name);
   }

   public static enum Ore {
      TIN,
      SILVER,
      URANIUM,
      ALUMINIUM;

      public Item getDrop() {
         switch (this) {
            case ALUMINIUM:
               return IC2Items.RAW_ALUMINIUM;
            case SILVER:
               return IC2Items.RAW_SILVER;
            case TIN:
               return IC2Items.RAW_TIN;
            case URANIUM:
               return IC2Items.ORE_URANIUM_DROP;
            default:
               throw new IllegalStateException("Unknown Drop");
         }
      }
   }
}
