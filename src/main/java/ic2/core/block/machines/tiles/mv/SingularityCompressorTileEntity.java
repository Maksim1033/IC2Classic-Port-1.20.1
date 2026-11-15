package ic2.core.block.machines.tiles.mv;

import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.inputs.FluidInput;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.INullableInput;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.cache.TileCache;
import ic2.core.block.base.tiles.impls.machine.single.BaseAdvMachineTileEntity;
import ic2.core.block.machines.tiles.lv.PumpTileEntity;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.XPSlot;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class SingularityCompressorTileEntity extends BaseAdvMachineTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_singularity.png");
   public static final Component PRESSURE = Component.translatable("info.block.ic2.singularity_compressor.pressure");
   TileCache<PumpTileEntity> pump_cache = new TileCache<>(this, DirectionList.ALL, PumpTileEntity.class);
   IMachineRecipeList.RecipeEntry pumpRecipe = new IMachineRecipeList.RecipeEntry(
      new ResourceLocation("ic2", "dummy"), new SimpleRecipeOutput(ObjectLists.singleton(new ItemGenerator(Items.SNOWBALL))), new INullableInput() {
         IInput fluidInput = new FluidInput(Fluids.WATER);

         @Override
         public void serialize(FriendlyByteBuf buffer) {
         }

         @Override
         public boolean matches(ItemStack stack) {
            return stack.isEmpty() && SingularityCompressorTileEntity.this.getValidPump() != null;
         }

         @Override
         public int getInputSize() {
            return 1;
         }

         @Override
         public List<ItemStack> getComponents() {
            return this.fluidInput.getComponents();
         }
      }
   );

   public SingularityCompressorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 15, 4000);
      this.addCaches(new ICache[]{this.pump_cache});
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).compressor;
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.COMPRESSOR_PROCESSING;
   }

   @Override
   public int[] getOutputSlots() {
      return new int[]{2};
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   protected void onPreTick(boolean hasRecipe, boolean canWork, boolean canOperate) {
      super.onPreTick(hasRecipe, canWork, canOperate);
      if (!hasRecipe && canWork && this.getValidPump() != null) {
         this.rebuildCache = true;
      }
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(int slot, ItemStack stack) {
      return this.pumpRecipe.getInputs()[0].matches(stack) ? this.pumpRecipe : this.getRecipeList().getRecipe(stack, true);
   }

   @Override
   public boolean canWorkWithoutItems() {
      return !this.pump_cache.isEmpty();
   }

   @Override
   public void consumeInput(IInput[] inputs, int slot, boolean emptyContainer) {
      if (inputs[0] == this.pumpRecipe.getInputs()[0]) {
         PumpTileEntity tile = this.getValidPump();
         if (tile != null) {
            tile.tank.drain(1000, FluidAction.EXECUTE);
         }
      } else {
         super.consumeInput(inputs, slot, emptyContainer);
      }
   }

   public PumpTileEntity getValidPump() {
      for (Direction dir : this.pump_cache) {
         PumpTileEntity pump = this.pump_cache.getHandler(dir);
         if (pump != null && pump.tank.drain(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE).getAmount() == 1000) {
            return pump;
         }
      }

      return null;
   }

   @Override
   public Slot[] addSlots(Player player) {
      return new Slot[]{
         FilterSlot.createDischargeSlot(this, this.tier, 0, 56, 53),
         new FilterSlot(this, 1, 56, 17, T -> this.getRecipeList().getRecipe(T, false) != null),
         new XPSlot(this, 2, 116, 35)
      };
   }

   @Override
   public Component getSpeedName() {
      return PRESSURE;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.SINGULARITY_COMPRESSOR;
   }
}
