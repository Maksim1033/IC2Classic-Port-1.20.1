package ic2.core.block.machines.tiles.hv;

import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.hv.ElectricEnchanterContainer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.EnchantUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ElectricEnchanterTileEntity
   extends BaseElectricTileEntity
   implements ITickListener,
   ITileGui,
   IProgressMachine,
   INotifyInventory,
   ITileActivityProvider {
   static final Random RAND = new Random();
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int mode = 0;
   @NetworkInfo
   public boolean checked = false;
   @NetworkInfo
   public boolean valid = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int enchantLevel = 0;
   @NetworkInfo
   public int storedExperience = 0;
   @NetworkInfo
   public int neededExperience = 0;
   @NetworkInfo
   public boolean running = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int progress = 0;
   public int seed;
   public ElectricEnchanterTileEntity.BookInformation renderData = new ElectricEnchanterTileEntity.BookInformation();

   public ElectricEnchanterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 7, 512, 1000000);
      this.setFuelSlot(0);
      this.redstoneIsFull = false;
      this.addGuiFields(new String[]{"mode", "checked", "valid", "enchantLevel", "storedExperience", "neededExperience", "running", "progress"});
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putByte("mode", (byte)this.mode);
      compound.putBoolean("checked", this.checked);
      compound.putBoolean("isValid", this.valid);
      compound.putByte("enchantLevel", (byte)this.enchantLevel);
      compound.putShort("storedExp", (short)this.storedExperience);
      compound.putShort("neededXP", (short)this.neededExperience);
      compound.putBoolean("running", this.running);
      compound.putShort("progress", (short)this.progress);
      compound.putInt("seed", this.seed);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.mode = compound.getInt("mode");
      this.checked = compound.getBoolean("checked");
      this.valid = compound.getBoolean("isValid");
      this.enchantLevel = compound.getInt("enchantLevel");
      this.storedExperience = compound.getInt("storedExp");
      this.neededExperience = compound.getInt("neededExp");
      this.running = compound.getBoolean("running");
      this.progress = compound.getInt("progress");
      this.seed = compound.getInt("seed");
   }

   @Override
   public boolean supportsNotify() {
      return true;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ELECTRIC_ENCHANTER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ElectricEnchanterContainer(this, player, windowID);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      switch (key) {
         case 0:
            this.mode = ++this.mode % 3;
            this.checked = false;
            this.updateGuiField("mode");
            break;
         case 1:
            if (value < 0 && !this.running) {
               int removing = Math.min(-value, this.storedExperience);
               if (!entity.isCreative()) {
                  entity.giveExperiencePoints(removing);
               }

               entity.onEnchantmentPerformed(ItemStack.EMPTY, 0);
               this.storedExperience -= removing;
               this.updateGuiField("storedExperience");
            } else if (value > 0) {
               this.storedExperience = this.storedExperience + EnchantUtil.drainExperience(entity, Math.min(1000 - this.storedExperience, value));
               this.updateGuiField("storedExperience");
            }
            break;
         case 2:
            if (this.valid) {
               this.running = true;
               this.seed = value;
               if (this.mode == 0) {
                  entity.onEnchantmentPerformed(ItemStack.EMPTY, 0);
               }

               this.updateGuiField("running");
               this.addToTick();
            }
      }
   }

   @Override
   public void onNotify(IHasInventory inv, int slot) {
      if (this.isSimulating() && ((ItemStack)this.inventory.get(6)).isEmpty() && !((ItemStack)this.inventory.get(5)).isEmpty()) {
         this.checked = false;
         this.updateGuiField("checked");
      }
   }

   @Override
   public void onTick() {
      this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      if (((ItemStack)this.inventory.get(6)).isEmpty() && !((ItemStack)this.inventory.get(5)).isEmpty()) {
         switch (this.mode) {
            case 0:
               this.enchant();
               break;
            case 1:
               this.anvil();
               break;
            case 2:
               this.disenchant();
         }
      } else if (this.checked) {
         this.checked = false;
         this.valid = false;
         this.running = false;
         this.neededExperience = 0;
         this.progress = 0;
         this.updateGuiFields(new String[]{"checked", "valid", "running", "neededExperience", "progress"});
      }

      this.handleComparators();
   }

   public void disenchant() {
      if (!this.checked) {
         this.checked = true;
         this.valid = this.getBookCount(ElectricEnchanterTileEntity.EnchantType.DISENCHANT) > 0 && this.isAdvancedEnchanted((ItemStack)this.inventory.get(5));
         this.neededExperience = this.valid ? 50 : 0;
         this.enchantLevel = 0;
         if (this.neededExperience > this.storedExperience && this.running) {
            this.running = false;
            this.progress = 0;
            this.updateGuiFields(new String[]{"running", "progress"});
         }

         this.updateGuiFields(new String[]{"checked", "valid", "enchantLevel", "neededExperience"});
      }

      if (this.valid) {
         if (this.neededExperience > 0 && this.running) {
            if (this.hasEnergy(500)) {
               this.setActive(true);
               this.useEnergy(500);
               this.progress++;
               if (this.progress >= 250) {
                  this.disenchantItem();
                  this.storedExperience = this.storedExperience - this.neededExperience;
                  this.neededExperience = 0;
                  this.progress = 0;
                  this.running = false;
                  this.checked = false;
                  this.updateGuiFields(new String[]{"storedExperience", "neededExperience", "running", "checked"});
               }

               this.updateGuiField("progress");
            } else {
               this.setActive(false);
            }
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
         if (this.progress > 0 || this.running) {
            this.progress = 0;
            this.running = false;
            this.updateGuiFields(new String[]{"running", "progress"});
         }
      }
   }

   public void disenchantItem() {
      ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantmentInstance data = this.consumeFirstEnchantment((ItemStack)this.inventory.get(5));
      if (data != null) {
         EnchantedBookItem.addEnchantment(stack, data);
      }

      this.inventory.set(6, stack);

      for (int i = 2; i < 5; i++) {
         ItemStack item = (ItemStack)this.inventory.get(i);
         if (!item.isEmpty()) {
            item.shrink(1);
            break;
         }
      }
   }

   public ItemStack getDisenchantedItem() {
      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments((ItemStack)this.inventory.get(5));
      Iterator<Entry<Enchantment, Integer>> iter = map.entrySet().iterator();
      if (!iter.hasNext()) {
         return ItemStack.EMPTY;
      } else {
         Entry<Enchantment, Integer> entry = iter.next();

         while (entry != null && !entry.getKey().isAllowedOnBooks()) {
            entry = iter.hasNext() ? iter.next() : null;
         }

         if (entry == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(entry.getKey(), entry.getValue()));
            return stack;
         }
      }
   }

   public void anvil() {
      if (!this.checked) {
         this.checked = true;
         this.valid = this.getBookCount(ElectricEnchanterTileEntity.EnchantType.ANVIL) > 0 && this.isItemValidToEnchant((ItemStack)this.inventory.get(5));
         this.calculateAnvilCost();
         this.neededExperience = this.neededExperience * (this.valid ? 1 : 0);
         if (this.neededExperience > this.storedExperience && this.running) {
            this.running = false;
            this.progress = 0;
            this.updateGuiFields(new String[]{"running", "progress"});
         }

         this.updateGuiFields(new String[]{"checked", "valid"});
      }

      if (this.valid) {
         if (this.neededExperience > 0 && this.running) {
            if (this.hasEnergy(500)) {
               this.setActive(true);
               this.useEnergy(500);
               this.progress++;
               if (this.progress >= 100) {
                  this.anvilItem();
                  this.storedExperience = this.storedExperience - this.neededExperience;
                  this.neededExperience = 0;
                  this.progress = 0;
                  this.valid = false;
                  this.running = false;
                  this.checked = false;
                  this.updateGuiFields(new String[]{"storedExperience", "neededExperience", "running", "checked", "valid"});
               }

               this.updateGuiField("progress");
            } else {
               this.setActive(false);
            }
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
         if (this.progress > 0 || this.running) {
            this.progress = 0;
            this.running = false;
            this.updateGuiFields(new String[]{"running", "progress"});
         }
      }
   }

   public void anvilItem() {
      this.inventory.set(6, this.getAnvilItem());
      ((ItemStack)this.inventory.get(5)).shrink(1);

      for (int i = 2; i < 5; i++) {
         ItemStack item = (ItemStack)this.inventory.get(i);
         if (!item.isEmpty()) {
            item.shrink(1);
         }
      }
   }

   public ItemStack getAnvilItem() {
      ItemStack stack = ((ItemStack)this.inventory.get(5)).copy();
      Object2IntMap<Enchantment> enchantments = new Object2IntLinkedOpenHashMap(EnchantmentHelper.getEnchantments(stack));
      EnchantUtil.mergeEnchantments(enchantments, this.getBookEnchantment(3), stack, true);
      if (stack.getItem() != Items.BOOK && stack.getItem() != Items.ENCHANTED_BOOK) {
         stack.setRepairCost(stack.getBaseRepairCost() + this.getLevel().random.nextInt(1 + enchantments.size()));
      } else {
         stack = new ItemStack(Items.ENCHANTED_BOOK);
      }

      EnchantmentHelper.setEnchantments(enchantments, stack);
      return stack;
   }

   public void enchant() {
      if (!this.checked) {
         this.checked = true;
         this.valid = this.getBookCount(ElectricEnchanterTileEntity.EnchantType.ENCHANT) > 0
            && ((ItemStack)this.inventory.get(1)).getCount() > 0
            && this.isItemValidToEnchant((ItemStack)this.inventory.get(5));
         this.calculateEnchantmentCost();
         this.neededExperience = this.neededExperience * (this.valid ? 1 : 0);
         if (this.neededExperience > this.storedExperience && this.running) {
            this.running = false;
            this.progress = 0;
            this.updateGuiFields(new String[]{"running", "progress"});
         }

         this.updateGuiFields(new String[]{"checked", "valid"});
      }

      if (this.valid) {
         if (!this.running && this.clock(128)) {
            this.calculateEnchantmentCost();
         }

         if (this.enchantLevel > 0 && this.running) {
            if (this.hasEnergy(500)) {
               this.setActive(true);
               this.useEnergy(500);
               this.progress++;
               if (this.progress >= 100) {
                  this.enchantItem();
                  this.storedExperience = this.storedExperience - this.neededExperience;
                  this.neededExperience = 0;
                  this.progress = 0;
                  this.running = false;
                  this.updateGuiFields(new String[]{"storedExperience", "neededExperience", "running", "checked"});
               }

               this.updateGuiField("progress");
            } else {
               this.setActive(false);
            }
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
         if (this.progress > 0 || this.running) {
            this.progress = 0;
            this.running = false;
            this.updateGuiFields(new String[]{"running", "progress"});
         }
      }
   }

   protected void enchantItem() {
      Tuple<ItemStack, Integer> result = this.enchantItem(this.seed);
      this.inventory.set(6, (ItemStack)result.getA());
      ((ItemStack)this.inventory.get(5)).shrink(1);
      ((ItemStack)this.inventory.get(1)).shrink((Integer)result.getB());

      for (int i = 2; i < 5; i++) {
         ItemStack item = (ItemStack)this.inventory.get(i);
         if (!item.isEmpty()) {
            item.shrink(1);
         }
      }
   }

   public List<EnchantmentInstance> getBookEnchantment(int size) {
      List<EnchantmentInstance> result = CollectionUtils.<EnchantmentInstance>createList();
      int i = 2;

      for (int m = 0; i < 5 && m < size; i++) {
         ItemStack item = (ItemStack)this.inventory.get(i);
         if (item.getItem() == Items.ENCHANTED_BOOK) {
            for (Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(item).entrySet()) {
               result.add(new EnchantmentInstance(entry.getKey(), entry.getValue()));
            }

            m++;
         }
      }

      return result;
   }

   public int getBookCount(ElectricEnchanterTileEntity.EnchantType enchant) {
      int found = 0;

      for (int i = 2; i < 5; i++) {
         Item item = ((ItemStack)this.inventory.get(i)).getItem();
         if (item != Items.AIR) {
            if (!enchant.isValid(item)) {
               return -1;
            }

            found++;
         }
      }

      return found;
   }

   public ItemStack getPotentialOutput(int seed) {
      switch (this.mode) {
         case 1:
            return this.getAnvilItem();
         case 2:
            return this.getDisenchantedItem();
         default:
            return this.getEnchantedItem(seed);
      }
   }

   private Tuple<ItemStack, Integer> enchantItem(int seed) {
      int count = this.getBookCount(ElectricEnchanterTileEntity.EnchantType.ENCHANT);
      RandomSource rand = this.getLevel().getRandom();
      rand.setSeed((long)(seed + this.enchantLevel));
      ItemStack stack = ((ItemStack)this.inventory.get(5)).copy();
      stack.setCount(1);
      stack.removeTagKey("Enchantments");
      List<EnchantmentInstance> enchantments = EnchantUtil.createElectricEnchantments(
         EnchantmentHelper.getEnchantmentCost(rand, count, this.enchantLevel, stack), stack, false, rand
      );
      List<EnchantmentInstance> books = this.getBookEnchantment(count);
      List<EnchantmentInstance> validEnchantments = CollectionUtils.<EnchantmentInstance>createList();
      rand.setSeed(rand.nextLong());
      MathUtils.shuffle(enchantments, rand);
      int i = 0;

      for (int m = enchantments.size(); i < m && i < 6; i++) {
         EnchantmentInstance data = enchantments.get(i);
         if (EnchantUtil.canInsertEnchantment(data, validEnchantments) && EnchantUtil.canInsertEnchantment(data, books)) {
            validEnchantments.add(data);
         }
      }

      rand.setSeed(rand.nextLong());
      MathUtils.shuffle(enchantments, rand);
      Object2IntMap<Enchantment> result = new Object2IntOpenHashMap();

      for (EnchantmentInstance data : validEnchantments.subList(0, Math.min(count * 2, validEnchantments.size()))) {
         result.put(data.enchantment, data.level);
      }

      EnchantUtil.mergeEnchantments(result, books, stack, false);
      if (stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK) {
         stack = new ItemStack(Items.ENCHANTED_BOOK);
      }

      EnchantmentHelper.setEnchantments(result, stack);
      return new Tuple(stack, count);
   }

   public ItemStack getEnchantedItem(int seed) {
      return (ItemStack)this.enchantItem(seed).getA();
   }

   public void calculateAnvilCost() {
      this.enchantLevel = 0;
      this.neededExperience = this.getAnvilCost() * 10;
      this.updateGuiFields(new String[]{"enchantLevel", "neededExperience"});
   }

   public int getAnvilCost() {
      return EnchantUtil.getEnchantmentCount((ItemStack)this.inventory.get(5))
         + EnchantUtil.getEnchantmentCount((ItemStack)this.inventory.get(2))
         + EnchantUtil.getEnchantmentCount((ItemStack)this.inventory.get(3))
         + EnchantUtil.getEnchantmentCount((ItemStack)this.inventory.get(4));
   }

   public void calculateEnchantmentCost() {
      this.enchantLevel = Math.min(this.getBookShelves(), this.getMaxLevel());
      this.neededExperience = (int)Math.pow(
         (double)(7 + (Mth.ceil((double)this.enchantLevel / 10.0) - 1) * 2),
         ((ItemStack)this.inventory.get(5)).getItem() != Items.ENCHANTED_BOOK && !((ItemStack)this.inventory.get(5)).isEnchanted() ? 2.3 : 2.6
      );
      this.updateGuiFields(new String[]{"enchantLevel", "neededExperience"});
   }

   public boolean isItemValidToEnchant(ItemStack item) {
      return item.getItem() == Items.BOOK
         || item.getItem() == Items.ENCHANTED_BOOK
         || item.getItem().isEnchantable(item)
         || item.getItem() instanceof IElectricEnchantable;
   }

   public boolean isEnchanted(ItemStack item) {
      return item.getItem() == Items.ENCHANTED_BOOK
         || (item.getItem().isEnchantable(item) || item.getItem() instanceof IElectricEnchantable) && item.isEnchanted();
   }

   public boolean isAdvancedEnchanted(ItemStack stack) {
      return this.isEnchanted(stack) && !EnchantmentHelper.getEnchantments(stack).isEmpty();
   }

   public EnchantmentInstance consumeFirstEnchantment(ItemStack stack) {
      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
      Iterator<Entry<Enchantment, Integer>> iter = map.entrySet().iterator();
      Entry<Enchantment, Integer> entry = iter.next();

      while (entry != null && !entry.getKey().isAllowedOnBooks()) {
         entry = iter.hasNext() ? iter.next() : null;
      }

      if (entry == null) {
         return null;
      } else {
         EnchantmentInstance data = new EnchantmentInstance(entry.getKey(), entry.getValue());
         iter.remove();
         if (stack.getItem() == Items.ENCHANTED_BOOK) {
            if (map.isEmpty()) {
               this.inventory.set(5, new ItemStack(Items.BOOK));
            } else {
               ItemStack newStack = new ItemStack(Items.ENCHANTED_BOOK);
               EnchantmentHelper.setEnchantments(map, newStack);
               this.inventory.set(5, newStack);
            }
         } else {
            EnchantmentHelper.setEnchantments(map, stack);
         }

         return data;
      }
   }

   public int getBookShelves() {
      int amount = 0;
      MutableBlockPos pos = this.getBlockPos().mutable();

      for (int y = 0; y < 2; y++) {
         for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
               pos.set(this.getBlockPos()).move(x, y, z);
               if (this.getLevel().isLoaded(pos)) {
                  amount += (int)this.getLevel().getBlockState(pos).getEnchantPowerBonus(this.getLevel(), pos);
               }
            }
         }
      }

      return Math.min(amount, 30);
   }

   public int getMaxLevel() {
      return Mth.clamp(Math.min(((ItemStack)this.inventory.get(1)).getCount(), this.getBookCount(ElectricEnchanterTileEntity.EnchantType.ENCHANT)), 0, 3) * 10;
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      return this.mode == 2 ? 250.0F : 100.0F;
   }

   public class BookInformation {
      public int tickCount;
      public float pageFlip;
      public float pageFlipPrev;
      public float flipT;
      public float flipA;
      public float bookSpread;
      public float bookSpreadPrev;
      public float bookRotation;
      public float bookRotationPrev;
      public float tRot;
      public long lastTick = -1L;

      public void tick() {
         Level world = ElectricEnchanterTileEntity.this.getLevel();
         if (world.getGameTime() != this.lastTick) {
            this.lastTick = world.getGameTime();
            BlockPos pos = ElectricEnchanterTileEntity.this.getBlockPos();
            this.bookSpreadPrev = this.bookSpread;
            this.bookRotationPrev = this.bookRotation;
            Player player = world.getNearestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 4.0, false);
            if (player != null && Math.sqrt(player.distanceToSqr(Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5))) <= 3.0) {
               double d0 = player.getX() - (double)((float)pos.getX() + 0.5F);
               double d1 = player.getZ() - (double)((float)pos.getZ() + 0.5F);
               this.tRot = (float)Mth.atan2(d1, d0);
               this.bookSpread += 0.1F;
               if (this.bookSpread < 0.5F || ElectricEnchanterTileEntity.RAND.nextInt(40) == 0) {
                  int offset;
                  while ((offset = ElectricEnchanterTileEntity.RAND.nextInt(4) - ElectricEnchanterTileEntity.RAND.nextInt(4)) == 0) {
                  }

                  this.flipT += (float)offset;
               }
            } else {
               this.tRot += 0.02F;
               this.bookSpread -= 0.1F;
            }

            if ((double)this.bookRotation < -3.141592653589793) {
               this.bookRotation += 6.2831855F;
            }

            if ((double)this.tRot >= 3.141592653589793) {
               this.tRot -= 6.2831855F;
            }

            if ((double)this.tRot < -3.141592653589793) {
               this.tRot += 6.2831855F;
            }

            double f2 = (double)(this.tRot - this.bookRotation);
            if (f2 >= 3.141592653589793) {
               f2 -= 6.283185307179586;
            }

            if (f2 < -3.141592653589793) {
               f2 += 6.283185307179586;
            }

            this.bookRotation += (float)(f2 * 0.4000000059604645);
            this.bookSpread = Mth.clamp(this.bookSpread, 0.0F, 1.0F);
            this.tickCount++;
            this.pageFlipPrev = this.pageFlip;
            this.flipA = this.flipA + (Mth.clamp((this.flipT - this.pageFlip) * 0.4F, -0.2F, 0.2F) - this.flipA) * 0.9F;
            this.pageFlip = this.pageFlip + this.flipA;
         }
      }
   }

   public static enum EnchantType {
      ENCHANT,
      ANVIL,
      DISENCHANT;

      public boolean isValid(Item item) {
         switch (this) {
            case DISENCHANT:
               return item == Items.BOOK;
            case ANVIL:
               return item == Items.ENCHANTED_BOOK;
            default:
               return item == Items.BOOK || item == Items.ENCHANTED_BOOK;
         }
      }
   }
}
