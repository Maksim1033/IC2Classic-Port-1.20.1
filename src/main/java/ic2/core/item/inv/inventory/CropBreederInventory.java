package ic2.core.item.inv.inventory;

import com.google.gson.JsonObject;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropSeed;
import ic2.core.block.crops.CropRegistry;
import ic2.core.block.crops.CropTileEntity;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.item.inv.container.CropBreederContainer;
import ic2.core.item.misc.CropSeedItem;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CropBreederInventory extends PortableInventory {
   public CropBreederInventory.CropList allCrops;
   public CropBreederInventory.ResultList results;
   public boolean waterlogged = false;

   public CropBreederInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   public void addBreed(ItemStack stack) {
      if (!stack.isEmpty()) {
         for (int i = 0; i < 4; i++) {
            if (((ItemStack)this.inventory.get(i)).isEmpty()) {
               this.inventory.set(i, stack.copy());
               this.markDirty();
               return;
            }
         }
      }
   }

   public void breed() {
      List<ICrop> breeding = CollectionUtils.<ICrop>createList();

      for (int i = 0; i < 4; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (stack.getItem() instanceof ICropSeed) {
            ICrop crop = ((ICropSeed)stack.getItem()).getCrop(stack);
            if (crop != null) {
               breeding.add(crop);
            }
         }
      }

      if (breeding.size() >= 2) {
         List<ICrop> crops = ICropRegistry.INSTANCE.getCrops();
         crops.remove(ICropRegistry.WEED);
         crops.remove(ICropRegistry.SEA_WEED);
         int[] ratios = new int[crops.size()];
         int total = 0;
         Map<ICrop, CropBreederInventory.BreedResult> resultMap = CollectionUtils.<ICrop, CropBreederInventory.BreedResult>createMap();
         int ix = 0;

         for (int m = crops.size(); ix < m; ix++) {
            ICrop card = crops.get(ix);
            CropBreederInventory.BreedResult result = new CropBreederInventory.BreedResult(card);
            int j = 0;

            for (int n = breeding.size(); j < n; j++) {
               int[] points = CropTileEntity.calculateRatiosFor(card, breeding.get(j), this.waterlogged);
               ratios[ix] += points[0];
               total += points[0];
               result.add(points);
            }

            resultMap.put(card, result);
         }

         this.results.clear();
         ix = 0;

         for (int m = crops.size(); ix < m; ix++) {
            if (ratios[ix] > 0) {
               this.results.addResult(resultMap.get(crops.get(ix)).setMax(total));
            }
         }

         this.results.sort();
         this.markDirty();
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CropBreederContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 4;
   }

   @Override
   public void save(CompoundTag nbt) {
      CompoundTag data = new CompoundTag();
      super.save(data);
      nbt.put("inv", data);
      nbt.putBoolean("water", this.waterlogged);
      nbt.put("results", this.results.save(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt.getCompound("inv"));
      this.allCrops = new CropBreederInventory.CropList();
      this.results = new CropBreederInventory.ResultList();
      this.allCrops.init();
      this.breed();
      this.results.init();
      this.results.load(nbt.getCompound("results"));
      this.waterlogged = nbt.getBoolean("water");
   }

   public static class BreedResult {
      ICrop crop;
      int max;
      int[] points = new int[4];

      public BreedResult(CompoundTag data, int max) {
         this.crop = CropRegistry.INSTANCE.getCrop(ResourceLocation.tryParse(data.getString("id")));
         this.points = data.getIntArray("stats");
         this.max = max;
      }

      public BreedResult(ICrop crop) {
         this.crop = crop;
      }

      public CropBreederInventory.BreedResult setMax(int max) {
         this.max = max;
         return this;
      }

      public void add(int[] data) {
         for (int i = 0; i < 4; i++) {
            this.points[i] = this.points[i] + data[i];
         }
      }

      public JsonObject saveAs() {
         JsonObject obj = new JsonObject();
         obj.addProperty("id", this.crop.id().toString());
         obj.addProperty("total_points", this.points[0]);
         obj.addProperty("properties_points", this.points[1]);
         obj.addProperty("attribute_points", this.points[2]);
         obj.addProperty("tier_points", this.points[3]);
         obj.addProperty("chance", (float)Math.max(0, this.points[0]) / (float)this.max * 100.0F);
         obj.addProperty("type", this.crop.getCropType().name());
         return obj;
      }

      public CompoundTag save() {
         CompoundTag nbt = new CompoundTag();
         nbt.putString("id", this.crop.id().toString());
         nbt.putIntArray("stats", this.points);
         return nbt;
      }

      public ItemStack createStack() {
         ItemStack stack = CropSeedItem.createStack(this.crop, 0, 0, 0, 1);
         CompoundTag nbt = stack.getOrCreateTag();
         nbt.putInt("points", this.points[0]);
         nbt.putIntArray("breed", new int[]{this.max, this.points[0], this.points[1], this.points[2], this.points[3]});
         CropSeedItem.setVisible(stack, true);
         return stack;
      }
   }

   public class CropList extends SimpleInventory {
      protected NonNullList<ItemStack> allCrops = NonNullList.create();
      protected int offset = 0;

      public CropList() {
         super(9);
      }

      @Override
      public int getMaxStackSize(int slot) {
         return 1;
      }

      public void init() {
         for (ICrop card : ICropRegistry.INSTANCE.getCrops()) {
            if (card != ICropRegistry.WEED && card != CropRegistry.SEA_WEED) {
               ItemStack stack = CropSeedItem.createStack(card, 0, 0, 0, 1);
               CropSeedItem.setVisible(stack, true);
               this.allCrops.add(stack);
            }
         }

         this.offset(0);
      }

      public void updateSlots() {
         for (int i = 0; i < 9; i++) {
            if (i + this.offset >= this.allCrops.size()) {
               this.setStackInSlot(i, ItemStack.EMPTY);
            } else {
               this.setStackInSlot(i, (ItemStack)this.allCrops.get(i + this.offset));
            }
         }

         CropBreederInventory.this.markDirty();
      }

      public void offset(int extra) {
         this.offset = Mth.clamp(this.offset + extra, 0, Math.max(0, this.allCrops.size() - 9));
         this.updateSlots();
      }

      @Override
      public boolean canInsert(int slot, ItemStack stack) {
         return false;
      }

      @Override
      public boolean canExtract(int slot, ItemStack stack) {
         return false;
      }

      @Override
      public CompoundTag save(CompoundTag nbt) {
         super.save(nbt);
         nbt.putInt("offset", this.offset);
         return nbt;
      }

      @Override
      public void load(CompoundTag nbt) {
         super.load(nbt);
         this.offset = nbt.getInt("offset");
      }
   }

   public class ResultList extends CropBreederInventory.CropList {
      protected NonNullList<CropBreederInventory.BreedResult> results = NonNullList.create();

      @Override
      public void init() {
      }

      public void sort() {
         this.allCrops.sort(new Comparator<ItemStack>() {
            public int compare(ItemStack o1, ItemStack o2) {
               return Integer.compare(this.getPoints(o2), this.getPoints(o1));
            }

            public int getPoints(ItemStack stack) {
               return StackUtil.getNbtData(stack).getInt("points");
            }
         });
         this.updateSlots();
      }

      public void addResult(CropBreederInventory.BreedResult result) {
         this.results.add(result);
         this.allCrops.add(result.createStack());
      }

      @Override
      public void clear() {
         this.allCrops.clear();
         this.results.clear();
         this.offset = 0;
         this.updateSlots();
      }

      @Override
      public CompoundTag save(CompoundTag nbt) {
         nbt.putInt("offset", this.offset);
         return nbt;
      }

      @Override
      public void load(CompoundTag nbt) {
         this.offset = nbt.getInt("offset");
         this.updateSlots();
      }
   }
}
