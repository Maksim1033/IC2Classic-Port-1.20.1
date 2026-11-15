package ic2.jeiplugin.core;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IRecipeMachine;
import ic2.core.block.base.tiles.impls.machine.multi.BaseColossalMachineTileEntity;
import ic2.core.block.generators.tiles.LiquidFuelGenTileEntity;
import ic2.core.block.machines.components.ev.ColossalMachineComponent;
import ic2.core.block.machines.containers.ev.ColossalMachineContainer;
import ic2.core.block.machines.containers.mv.ReactorPlannerContainer;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.simple.FuelComponent;
import ic2.core.inventory.gui.components.simple.ProgressComponent;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IC2GuiHandler implements IGuiContainerHandler<IC2Screen> {
   Map<IMachineRecipeList, RecipeType<?>> locations;
   Map<Class<?>, RecipeType<?>> classBased;

   public IC2GuiHandler(Map<IMachineRecipeList, RecipeType<?>> locations, Map<Class<?>, RecipeType<?>> classBased) {
      this.locations = locations;
      this.classBased = classBased;
   }

   public List<Rect2i> getGuiExtraAreas(IC2Screen containerScreen) {
      List<Rect2i> extraBoxes = CollectionUtils.<Rect2i>createList();
      if (containerScreen.getMenu() instanceof ReactorPlannerContainer) {
         extraBoxes.add(new Rect2i(containerScreen.getGuiLeft() + 80, containerScreen.getGuiTop(), 212, 212));
      }

      if (containerScreen instanceof ComponentContainerScreen screen) {
         for (GuiWidget comp : screen.getComponents()) {
            Box2i box = comp.getBox();
            if (box != Box2i.EMPTY_BOX && comp.isVisible()) {
               extraBoxes.add(new Rect2i(containerScreen.getGuiLeft() + box.getX(), containerScreen.getGuiTop() + box.getY(), box.getWidth(), box.getHeight()));
            }
         }
      }

      return extraBoxes;
   }

   public Collection<IGuiClickableArea> getGuiClickableAreas(IC2Screen containerScreen, double mouseX, double mouseY) {
      List<IGuiClickableArea> areas = CollectionUtils.<IGuiClickableArea>createList();
      if (containerScreen instanceof ComponentContainerScreen) {
         ContainerComponent<?> comp = containerScreen.getCastedContainer(ContainerComponent.class);
         if (comp != null) {
            if (comp.getHolder() instanceof IRecipeMachine recipe) {
               RecipeType<?> location = this.locations.get(recipe.getRecipeList());
               if (location != null) {
                  ProgressComponent progress = ((ComponentContainerScreen)containerScreen).getComponentFromClass(ProgressComponent.class);
                  if (progress != null) {
                     Box2i box = progress.getBox();
                     areas.add(IGuiClickableArea.createBasic(box.getX(), box.getY(), box.getWidth(), box.getHeight(), new RecipeType[]{location}));
                  }
               }
            }

            if (comp.getHolder() != null) {
               RecipeType<?> location = this.classBased.get(comp.getHolder().getClass());
               if (location != null) {
                  ProgressComponent progress = ((ComponentContainerScreen)containerScreen).getComponentFromClass(ProgressComponent.class);
                  if (progress != null) {
                     Box2i box = progress.getBox();
                     areas.add(IGuiClickableArea.createBasic(box.getX(), box.getY(), box.getWidth(), box.getHeight(), new RecipeType[]{location}));
                  } else if (comp.getHolder() instanceof LiquidFuelGenTileEntity) {
                     FuelComponent fuel = ((ComponentContainerScreen)containerScreen).getComponentFromClass(FuelComponent.class);
                     if (fuel != null) {
                        Box2i box = fuel.getBox();
                        areas.add(IGuiClickableArea.createBasic(box.getX(), box.getY(), box.getWidth(), box.getHeight(), new RecipeType[]{location}));
                     }
                  }
               }

               if (comp instanceof ColossalMachineContainer && comp.getComponent(ColossalMachineComponent.class) != null) {
                  for (Vec2i pos : ColossalMachineComponent.PROGRESS[((BaseColossalMachineTileEntity)comp.getHolder()).getSlotsInUse()]) {
                     areas.add(IGuiClickableArea.createBasic(pos.getX(), pos.getY(), 24, 16, new RecipeType[]{location}));
                  }
               }
            }
         }
      }

      return areas;
   }
}
