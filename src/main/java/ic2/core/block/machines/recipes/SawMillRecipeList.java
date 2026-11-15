package ic2.core.block.machines.recipes;

import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.ItemStackInput;
import ic2.api.recipes.ingridients.recipes.SawMillOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SawMillRecipeList extends MachineRecipeList {
   public static final CraftingContainer CRAFTING = new TransientCraftingContainer(new AbstractContainerMenu(null, 0) {
      public boolean stillValid(Player playerIn) {
         return true;
      }

      public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
         return ItemStack.EMPTY;
      }
   }, 3, 3);

   public SawMillRecipeList(Consumer<IMachineRecipeList> reloader) {
      super("sawmill", reloader);
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ItemStack stack, boolean hasStackSize) {
      IMachineRecipeList.RecipeEntry entry = super.getRecipe(stack, hasStackSize);
      if (stack.is(ItemTags.LOGS) && (entry == null || entry.getLocation().getPath().equalsIgnoreCase("display_log"))) {
         ItemStack output = findOutput(stack);
         return !output.isEmpty()
            ? new IMachineRecipeList.RecipeEntry(
               new ResourceLocation("ic2", "log_recipe"),
               new SawMillOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output.copy(), 6))),
               new ItemStackInput(stack, 1)
            )
            : null;
      } else {
         return entry;
      }
   }

   public static ItemStack findOutput(ItemStack recipe) {
      CRAFTING.setItem(4, StackUtil.copyWithSize(recipe, 1));

      try {
         Optional<CraftingRecipe> result = ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, CRAFTING, getLevel());
         return result.<ItemStack>map(craftingRecipe -> craftingRecipe.assemble(CRAFTING, getLevel() != null ? getLevel().registryAccess() : net.minecraft.core.RegistryAccess.EMPTY)).orElse(ItemStack.EMPTY);
      } catch (Exception var2) {
         var2.printStackTrace();
         return ItemStack.EMPTY;
      }
   }

   private static Level getLevel() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      return server == null ? null : server.overworld();
   }
}
