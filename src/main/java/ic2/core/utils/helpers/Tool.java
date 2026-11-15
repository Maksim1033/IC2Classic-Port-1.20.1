package ic2.core.utils.helpers;

import ic2.core.platform.registries.IC2Tags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public final class Tool {
   public static final Tool PICKAXE = new Tool(Tool.ToolType.PICKAXE);
   public static final Tool AXE = new Tool(Tool.ToolType.AXE);
   public static final Tool SHOVEL = new Tool(Tool.ToolType.SHOVEL);
   public static final Tool HOE = new Tool(Tool.ToolType.HOE);
   final Tool.ToolType type;
   final int level;

   private Tool(Tool.ToolType type) {
      this(type, 0);
   }

   private Tool(Tool.ToolType type, int level) {
      this.type = type;
      this.level = level;
   }

   public Tool withLevel(int level) {
      return new Tool(this.type, level);
   }

   public int getLevel() {
      return this.level;
   }

   public Tool.ToolType getType() {
      return this.type;
   }

   public boolean isCorrectTool(BlockState state) {
      return this.type.isCorrectTool(state, this.level);
   }

   public boolean isCorrectTool(BlockState state, Tier tier) {
      return this.type.isCorrectTool(state, tier);
   }

   public void register(Block block) {
      this.type.registerBlock(block, this.level);
   }

   public static enum ToolType {
      PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE),
      AXE(BlockTags.MINEABLE_WITH_AXE),
      SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL),
      HOE(BlockTags.MINEABLE_WITH_HOE);

      final TagKey<Block> type;

      private ToolType(TagKey<Block> type) {
         this.type = type;
      }

      void registerBlock(Block block, int level) {
         ResourceLocation tag = this.getTagForLevel(level).location();
         IC2Tags.registerTags(new ResourceLocation(tag.getNamespace(), "tags/blocks/" + tag.getPath()), ForgeRegistries.BLOCKS, block);
         IC2Tags.registerTags(
            new ResourceLocation(this.type.location().getNamespace(), "tags/blocks/" + this.type.location().getPath()), ForgeRegistries.BLOCKS, block
         );
      }

      boolean isCorrectTool(BlockState state, int level) {
         return this.isCorrectTool(state, this.getTierForLevel(level));
      }

      boolean isCorrectTool(BlockState state, Tier tier) {
         return state.is(this.type) && TierSortingRegistry.isCorrectTierForDrops(tier, state);
      }

      private Tier getTierForLevel(int level) {
         switch (level) {
            case 0:
               return Tiers.WOOD;
            case 1:
               return Tiers.STONE;
            case 2:
               return Tiers.IRON;
            case 3:
               return Tiers.DIAMOND;
            case 4:
               return Tiers.NETHERITE;
            default:
               throw new IllegalStateException("Unsupported Tier");
         }
      }

      private TagKey<Block> getTagForLevel(int level) {
         switch (level) {
            case 0:
               return Blocks.NEEDS_WOOD_TOOL;
            case 1:
               return BlockTags.NEEDS_STONE_TOOL;
            case 2:
               return BlockTags.NEEDS_IRON_TOOL;
            case 3:
               return BlockTags.NEEDS_DIAMOND_TOOL;
            case 4:
               return Blocks.NEEDS_NETHERITE_TOOL;
            default:
               throw new IllegalStateException("Unsupported Tier");
         }
      }
   }
}
