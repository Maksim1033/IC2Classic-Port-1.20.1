package ic2.core.block.machines.tiles.mv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BaseAdvMachineTileEntity;
import ic2.core.block.machines.recipes.misc.ScrapOutput;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.XPSlot;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CompactingRecyclerTileEntity extends BaseAdvMachineTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_compacting.png");
   public static final Component SPEED = Component.translatable("info.block.ic2.compacting_recycler.speed");
   public static final IMachineRecipeList.RecipeEntry SCRAP_BOX = ScrapOutput.createEntry(23643);

   public CompactingRecyclerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 10, 333);
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).recycler;
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.RECYCLER_PROCESSING;
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
   protected boolean consumeContainers() {
      return true;
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(int slot, ItemStack stack) {
      return stack.getItem() == IC2Items.SCRAP ? (stack.getCount() >= 9 ? SCRAP_BOX : null) : this.getRecipeList().getRecipe(stack, true);
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
      return SPEED;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.COMPACTING_RECYCLER;
   }
}
