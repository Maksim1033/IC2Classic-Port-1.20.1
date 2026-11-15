package ic2.core.block.generators.containers;

import ic2.api.reactor.ISteamReactor;
import ic2.core.block.base.tiles.impls.BaseNuclearReactorTileEntity;
import ic2.core.block.generators.components.ReactorComponent;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.TankComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorContainer extends ContainerComponent<BaseNuclearReactorTileEntity> {
   public static final ResourceLocation[] TEXTURES_ELECTRIC = new ResourceLocation[]{
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by3.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by4.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by5.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by6.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by7.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by8.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor/gui6by9.png")
   };
   public static final ResourceLocation[] TEXTURES_STEAM = new ResourceLocation[]{
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by3.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by4.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by5.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by6.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by7.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by8.png"),
      new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/reactor_steam/gui6by9.png")
   };
   public int size;
   public boolean isSteam;

   public ReactorContainer(BaseNuclearReactorTileEntity key, Player player, int id) {
      super(key, player, id);
      this.size = key.getWidth();
      int startX = 89 - 9 * this.size;
      int startY = 18;

      for (int i = 0; i < 54; i++) {
         int x = i % 9;
         if (x < this.size) {
            this.addSlot(new ReactorContainer.ReactorFilter(key, i, startX + 18 * x, startY + 18 * (i / 9), key::isUsefulItem, key.filter));
         }
      }

      if (key instanceof ISteamReactor reactor) {
         this.isSteam = true;
         if (this.size >= 8) {
            this.addComponent(new TankComponent(new Box2i(-10, 18, 12, 106), new Vec2i(216, 18), reactor.getWaterTank()));
            this.addComponent(new TankComponent(new Box2i(174, 18, 12, 106), new Vec2i(216, 18), reactor.getSteamTank()));
         } else {
            this.addComponent(new TankComponent(new Box2i(8, 18, 12, 106), new Vec2i(216, 18), reactor.getWaterTank()));
            this.addComponent(new TankComponent(new Box2i(156, 18, 12, 106), new Vec2i(216, 18), reactor.getSteamTank()));
         }
      }

      this.addComponent(new ReactorComponent(key, this.size));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 72);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      if (this.isSteam && this.size >= 8) {
         screen.setXSize(194);
      }

      screen.setYSize(238);
   }

   @Override
   public int getInventorySize() {
      return this.getHolder().getWidth() * 6;
   }

   @Override
   public ResourceLocation getTexture() {
      return this.isSteam ? TEXTURES_STEAM[this.size - 3] : TEXTURES_ELECTRIC[this.size - 3];
   }

   @Override
   public Vec2i getPreviewButtonOffset() {
      return new Vec2i(11, -11);
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return new Vec2i(11, 0);
   }

   public static class ReactorFilter extends FilterSlot {
      IHasInventory filterInv;

      public ReactorFilter(IHasInventory inv, int index, int xPosition, int yPosition, IFilter filter, IHasInventory filterInv) {
         super(inv, index, xPosition, yPosition, filter);
         this.filterInv = filterInv;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         if (!super.mayPlace(stack)) {
            return false;
         } else {
            ItemStack filter = this.filterInv.getStackInSlot(this.slot);
            return filter.isEmpty() || filter.is(stack.getItem());
         }
      }
   }
}
