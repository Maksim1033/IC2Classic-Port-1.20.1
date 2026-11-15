package ic2.core.item.misc.tfbp.bp;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BiomeBlueprint implements ITerraformerBP {
   int radius;
   int energy;
   ResourceLocation biome;

   public BiomeBlueprint(int radius, int energy, ResourceLocation biome) {
      this.radius = radius;
      this.energy = energy;
      this.biome = biome;
   }

   @Override
   public boolean canInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
      return true;
   }

   @Override
   public boolean isRandomized(ItemStack stack) {
      return false;
   }

   @Override
   public int getEnergyUsage(ItemStack stack) {
      return this.energy;
   }

   @Override
   public int getRadius(ItemStack stack) {
      return this.radius;
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      return terraformer.setBiome(world, position, this.biome);
   }
}
