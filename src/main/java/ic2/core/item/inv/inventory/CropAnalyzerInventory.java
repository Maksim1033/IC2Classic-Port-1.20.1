package ic2.core.item.inv.inventory;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropSeed;
import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.CropAnalyzerContainer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CropAnalyzerInventory extends PortableInventory {
   boolean open = true;

   public CropAnalyzerInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot, boolean tick) {
      super(owner, held, stack, slot);
      if (tick) {
         IC2.TICK_HANDLER.addWorldCallback(owner.level(), T -> this.tick() ? 5 : 0, 5);
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CropAnalyzerContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 3;
   }

   @Override
   public void onGuiClosed(Player player) {
      this.open = false;
      super.onGuiClosed(player);
   }

   protected boolean tick() {
      if (!this.open) {
         return false;
      } else {
         ItemStack stack = this.getOwnerStack(this.owner);
         if (!((ItemStack)this.inventory.get(2)).isEmpty()) {
            int needed = ElectricItem.MANAGER.charge(stack, ElectricItem.MANAGER.getCharge((ItemStack)this.inventory.get(2)), 1, true, false);
            if (needed > 0) {
               ElectricItem.MANAGER.discharge((ItemStack)this.inventory.get(2), needed, 2147483647, true, false, false);
               this.markDirty();
            }
         }

         if (((ItemStack)this.inventory.get(1)).isEmpty() && ((ItemStack)this.inventory.get(0)).getItem() instanceof ICropSeed seed) {
            int lvl = seed.getScanLevel((ItemStack)this.inventory.get(0));
            if (lvl == 4) {
               this.inventory.set(1, (ItemStack)this.inventory.get(0));
               this.inventory.set(0, ItemStack.EMPTY);
               this.markDirty();
            } else if (ElectricItem.MANAGER.use(stack, ICropSeed.SCAN_COST[lvl], this.owner)) {
               seed.increaseScanLevel((ItemStack)this.inventory.get(0));
               this.inventory.set(1, (ItemStack)this.inventory.get(0));
               this.inventory.set(0, ItemStack.EMPTY);
               this.markDirty();
            }
         }

         return true;
      }
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
   }

   @Override
   public void save(CompoundTag nbt) {
      super.save(nbt);
   }

   public ICropSeed getSeed() {
      Item item = ((ItemStack)this.inventory.get(1)).getItem();
      return item instanceof ICropSeed ? (ICropSeed)item : null;
   }

   public int getLevel() {
      ICropSeed seed = this.getSeed();
      return seed == null ? -1 : seed.getScanLevel((ItemStack)this.inventory.get(1));
   }

   public ICrop getCrop() {
      ICropSeed seed = this.getSeed();
      return seed == null ? null : seed.getCrop((ItemStack)this.inventory.get(1));
   }

   public Component getName() {
      ICrop crop = this.getCrop();
      return (Component)(crop == null ? Component.translatable("info.crop.ic2.data.unknown") : crop.getName());
   }

   public Component getOwner() {
      ICrop crop = this.getCrop();
      return crop == null ? null : crop.discoveredBy();
   }

   public String getDesc(int index) {
      ICrop crop = this.getCrop();
      return crop == null ? null : crop.desc(index);
   }

   public String getStat(int type) {
      ICropSeed seed = this.getSeed();
      if (seed == null) {
         return "I AM ERROR";
      } else {
         switch (type) {
            case 0:
               return seed.getGrowth((ItemStack)this.inventory.get(1)) + "/31";
            case 1:
               return seed.getGain((ItemStack)this.inventory.get(1)) + "/31";
            case 2:
               return seed.getResistance((ItemStack)this.inventory.get(1)) + "/31";
            default:
               return "I AM ERROR";
         }
      }
   }

   public String getTier() {
      ICrop crop = this.getCrop();
      return crop == null ? null : ICropSeed.TIERS[crop.getProperties().getTier()];
   }
}
