package ic2.core.block.machines.containers.hv;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.block.machines.components.hv.ElectricEnchanterComponent;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.gui.components.simple.ProgressComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.item.manager.GatewayElectricItemManager;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ElectricEnchanterContainer extends ContainerComponent<ElectricEnchanterTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/hv/gui_electric_enchanter.png");
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);
   public static final Box2i CHARGE_BOX = new Box2i(14, 20, 14, 14);
   public static final Vec2i PROGRESS_POS = new Vec2i(176, 14);
   public static final Box2i PROGRESS_BOX = new Box2i(90, 39, 24, 16);
   DataSlot seed = DataSlot.standalone();
   Player player;

   public ElectricEnchanterContainer(ElectricEnchanterTileEntity key, Player player, int id) {
      super(key, player, id);
      this.player = player;
      this.addSlot(FilterSlot.createDischargeSlot(key, key.tier, 0, 13, 39));
      this.addSlot(new FilterSlot(key, 1, 42, 39, new SimpleFilter(Items.LAPIS_LAZULI)));
      this.addSlot(new FilterSlot(key, 2, 46, 16, SpecialFilters.BOOK_FILTER));
      this.addSlot(new FilterSlot(key, 3, 64, 16, SpecialFilters.BOOK_FILTER));
      this.addSlot(new FilterSlot(key, 4, 82, 16, SpecialFilters.BOOK_FILTER));
      this.addSlot(new FilterSlot(key, 5, 64, 39, SpecialFilters.ENCHANT_FILTER));
      this.addSlot(new ElectricEnchanterContainer.EnchantOutput(key, player, 6, 126, 39));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 6);
      this.addComponent(new ElectricEnchanterComponent(key, this.seed));
      this.addComponent(new ProgressComponent(PROGRESS_BOX, key, PROGRESS_POS, false));
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true));
      this.addDataSlot(this.seed);
      this.seed.set(player.getEnchantmentSeed());
   }

   public void broadcastChanges() {
      this.seed.set(this.player.getEnchantmentSeed());
      super.broadcastChanges();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, 6);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public static class EnchantOutput extends FilterSlot {
      Player player;

      public EnchantOutput(IHasInventory inv, Player player, int index, int xPosition, int yPosition) {
         super(inv, index, xPosition, yPosition, SpecialFilters.ALWAYS_FALSE);
         this.player = player;
      }

      protected void checkTakeAchievements(ItemStack stack) {
         super.checkTakeAchievements(stack);
         this.player.awardStat(Stats.ENCHANT_ITEM);
         if (stack.getItem() instanceof IElectricEnchantable && ((GatewayElectricItemManager)ElectricItem.MANAGER).getManager(stack) != null) {
            this.player.awardStat(IC2Stats.ELECTRIC_ENCHANTED);
         }
      }
   }
}
