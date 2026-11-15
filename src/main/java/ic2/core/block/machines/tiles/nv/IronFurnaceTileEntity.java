package ic2.core.block.machines.tiles.nv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.features.IParticleSpawner;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IronFurnaceTileEntity extends StoneBasicMachineTileEntity implements IParticleSpawner {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/stone/gui_iron_furnace.png");

   public IronFurnaceTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 160);
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).furnace;
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.IRON_FURNACE_PROCESSING;
   }

   @Override
   protected boolean consumeContainers() {
      return true;
   }

   @Override
   public IMachineRecipeList.RecipeEntry getEntry(ItemStack stack) {
      return this.getRecipeList().getRecipe(stack, true);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.IRON_FURNACE;
   }

   @Override
   public int getFuel(ItemStack stack) {
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
         Direction direction = this.getFacing();
         Axis axis = direction.getAxis();
         double d0 = (double)this.worldPosition.getX() + 0.5 + (axis == Axis.X ? (double)direction.getStepX() * 0.52 : rand.nextDouble() * 0.6 - 0.3);
         double d1 = (double)this.worldPosition.getY() + rand.nextDouble() * 6.0 / 16.0;
         double d2 = (double)this.worldPosition.getZ() + 0.5 + (axis == Axis.Z ? (double)direction.getStepZ() * 0.52 : rand.nextDouble() * 0.6 - 0.3);
         if (rand.nextDouble() < 0.1) {
            this.getLevel().playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         this.getLevel().addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
         this.getLevel().addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
      }
   }
}
