package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OreCrop extends BaseCrop {
   private String name;
   private Component discovered;
   private List<TagKey<Block>> validBlocks;
   private Item drop;

   public OreCrop(String name, CropProperties props, List<TagKey<Block>> blocks, Item drop, Component discovered, String attribute) {
      super(name, props, attribute, "Leaves", "Ores");
      this.name = name;
      this.validBlocks = new ObjectArrayList(blocks);
      this.drop = drop;
      this.discovered = discovered;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String stageTexture = "";
      switch (stage) {
         case 0:
         case 1:
         case 2:
            stageTexture = "growing_" + stage;
            break;
         case 3:
            stageTexture = "growing_" + this.name + "_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/ores").get(stageTexture);
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 2;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      if (cropTile.getGrowthStage() < 3) {
         return true;
      } else {
         if (cropTile.getGrowthStage() == 3) {
            for (Block block : cropTile.getBlocksBelow()) {
               for (TagKey<Block> key : this.validBlocks) {
                  if (block.defaultBlockState().is(key)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      if (cropTile.getGrowthStage() == 3) {
         return !this.name.equals("argentum") && !this.name.equals("aurelia") && !this.name.equals("adamantum") && !this.name.equals("alumen") ? 2000 : 2200;
      } else {
         return !this.name.equals("argentum") && !this.name.equals("aurelia") && !this.name.equals("adamantum") && !this.name.equals("alumen") ? 800 : 1000;
      }
   }

   @Override
   public Component discoveredBy() {
      return this.discovered;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(this.drop);
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      if (this.name.equals("adamantum")) {
         return super.getDropChance(cropTile) / 4.0;
      } else {
         return this.name.equals("alumen") ? super.getDropChance(cropTile) / 3.0 : super.getDropChance(cropTile) / 2.0;
      }
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(this.drop)};
   }
}
