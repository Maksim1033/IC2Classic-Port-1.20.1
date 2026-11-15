package ic2.core.block.generators.containers;

import ic2.core.block.generators.tiles.GeoGenTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.gui.components.simple.FuelComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;

public class GeoGenContainer extends ContainerComponent<GeoGenTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/generators/gui_geo_generator.png");
   public static Box2i FUEL_BOX = new Box2i(66, 36, 14, 14);
   public static Vec2i FUEL_POS = new Vec2i(176, 0);
   public static Box2i ENERGY_BOX = new Box2i(94, 35, 24, 17);
   public static Vec2i ENERGY_POS = new Vec2i(176, 14);

   public GeoGenContainer(GeoGenTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(FilterSlot.createChargeSlot(key, key.tier, 0, 131, 36));
      this.addSlot(FilterSlot.createFluidDrainSlot(key, 1, 65, 17, Fluids.LAVA, IC2Fluids.BLAZING_LAVA));
      this.addSlot(FilterSlot.createFluidOutputSlot(key, 2, 65, 53));
      this.addPlayerInventory(player.getInventory());
      this.addComponent(new FuelComponent(FUEL_BOX, key, FUEL_POS, true));
      this.addComponent(new ChargeBarComponent(ENERGY_BOX, key, ENERGY_POS, false));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
