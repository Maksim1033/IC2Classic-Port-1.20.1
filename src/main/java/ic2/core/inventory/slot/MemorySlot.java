package ic2.core.inventory.slot;

import com.mojang.datafixers.util.Pair;
import ic2.core.block.machines.logic.crafter.IMemorySlotProvider;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MemorySlot extends SlotBase {
   IMemorySlotProvider bench;
   ItemStack display = IC2Items.ICON_DISPLAY.create(1);

   public MemorySlot(IMemorySlotProvider inv, int index, int xPosition, int yPosition) {
      super(inv.getRecipes(), index, xPosition, yPosition);
      this.bench = inv;
   }

   public boolean mayPickup(Player playerIn) {
      return false;
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isActive() {
      return (this.bench.getEnabledSlots() & 1 << this.slot) != 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
      return Pair.of(InventoryMenu.BLOCK_ATLAS, IC2Textures.getMappedEntriesIC2("misc/gui").get("memory_stick").contents().name());
   }

   @Override
   public ItemStack getItem() {
      return !this.bench.isServerSided() && (this.bench.getEnabledSlots() & 1 << this.slot) == 0 ? this.display : super.getItem();
   }
}
