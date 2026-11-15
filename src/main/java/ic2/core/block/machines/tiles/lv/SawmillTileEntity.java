package ic2.core.block.machines.tiles.lv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SawmillTileEntity extends BasicMachineTileEntity {
   public static final String DISPLAY_RECIPE = "display_log";
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/lv/gui_sawmill.png");

   public SawmillTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 2, 400, 32);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).sawmill;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.SAWMILL;
   }

   public static void loadRecipes(IMachineRecipeList list) {
      ItemStack stack = new ItemStack(Blocks.OAK_PLANKS, 6);
      StackUtil.addTooltip(stack, "" + ChatFormatting.RESET + ChatFormatting.GOLD + "Planks keep the log type");
      list.addIC2SimpleRecipe("display_log", stack, IC2Tags.LOGS);
      list.addIC2SimpleRecipe("melon_sawmill", new ItemStack(Items.MELON_SLICE, 9), Items.MELON);
      list.addIC2SimpleRecipe("sliced_potatoes", new ItemStack(IC2Items.POTATO_SLICES, 8), Items.POTATO);
   }
}
