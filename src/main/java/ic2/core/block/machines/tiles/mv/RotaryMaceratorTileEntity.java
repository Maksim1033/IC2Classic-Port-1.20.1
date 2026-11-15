package ic2.core.block.machines.tiles.mv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BaseAdvMachineTileEntity;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.XPSlot;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotaryMaceratorTileEntity extends BaseAdvMachineTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_rotary.png");
   public static final Component SPEED = Component.translatable("info.block.ic2.rotary_macerator.speed");

   public RotaryMaceratorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 4, 15, 4000);
   }

   @Override
   public int[] getOutputSlots() {
      return new int[]{2, 3};
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ROTARY_MACERATOR;
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
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public Slot[] addSlots(Player player) {
      return new Slot[]{
         FilterSlot.createDischargeSlot(this, this.tier, 0, 56, 53),
         new FilterSlot(this, 1, 56, 17, T -> this.getRecipeList().getRecipe(T, false) != null),
         new XPSlot(this, 2, 113, 35),
         new XPSlot(this, 3, 131, 35)
      };
   }

   @Override
   public Component getSpeedName() {
      return SPEED;
   }
}
