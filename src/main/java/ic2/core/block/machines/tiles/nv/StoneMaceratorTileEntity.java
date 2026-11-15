package ic2.core.block.machines.tiles.nv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.features.IParticleSpawner;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StoneMaceratorTileEntity extends StoneBasicMachineTileEntity implements IParticleSpawner {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/stone/gui_stone_macerator.png");

   public StoneMaceratorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 400);
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.MACERATOR_PROCESSING;
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).macerator;
   }

   @Override
   public IMachineRecipeList.RecipeEntry getEntry(ItemStack stack) {
      return this.getRecipeList().getRecipe(stack, true);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.STONE_MACERATOR;
   }

   @Override
   public int getFuel(ItemStack stack) {
      return IC2.RECIPES.get(this.isSimulating()).getFuel((ItemStack)this.inventory.get(0), true) / 2;
   }

   @Override
   public int getRawFuel(ItemStack stack) {
      return IC2.RECIPES.get(this.isSimulating()).getFuel((ItemStack)this.inventory.get(0), true);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void animationTick(RandomSource rand) {
      if (this.isActive()) {
         double x = (double)((float)this.worldPosition.getX() + 1.0F);
         double y = (double)((float)this.worldPosition.getY() + 1.0F);
         double z = (double)((float)this.worldPosition.getZ() + 1.0F);

         for (int i = 0; i < 4; i++) {
            float xOffset = -0.2F - rand.nextFloat() * 0.6F;
            float yOffset = -0.1F + rand.nextFloat() * 0.2F;
            float zOffset = -0.2F - rand.nextFloat() * 0.6F;
            this.getLevel().addParticle(ParticleTypes.SMOKE, x + (double)xOffset, y + (double)yOffset, z + (double)zOffset, 0.0, 0.0, 0.0);
         }
      }
   }
}
