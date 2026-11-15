package ic2.core.block.base.tiles.impls;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.tiles.IMachine;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.storage.container.ChargePadContainer;
import ic2.core.entity.renderer.particle.AuraParticle;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.item.upgrades.PadUpgradeItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseChargePadTileEntity
   extends BaseTileEntity
   implements IHasInventory,
   IMachine,
   ITickListener,
   IRedstoneListener,
   IRedstoneProvider,
   ITileGui,
   IEUStorage,
   IEnergySink,
   IWrenchRemovable,
   IDropProvider,
   ITileActivityProvider {
   static final EnumSet<IUpgradeItem.UpgradeType> TYPES = EnumSet.of(IUpgradeItem.UpgradeType.MACHINE_MOD, IUpgradeItem.UpgradeType.RECIPE_MOD);
   static final Predicate<Entity> PREDICATE = EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS);
   public final BaseChargePadTileEntity.ChargePadData data;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int tier;
   @NetworkInfo
   public int maxInput;
   @NetworkInfo
   public int transferLimit;
   @NetworkInfo
   public int maxEnergy;
   @NetworkInfo
   public int energy;
   @NetworkInfo
   public float range;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int installedUpgrades = 0;
   public int activeTime = 0;
   public int inactiveTime = 0;
   public SimpleInventory inventory;
   public boolean charge_slot = false;
   protected boolean addedToEnergyNet = false;

   protected BaseChargePadTileEntity(BlockPos pos, BlockState state, BaseChargePadTileEntity.ChargePadData data) {
      super(pos, state);
      this.data = data;
      this.inventory = new SimpleInventory(1 + data.getSlots() + data.getUpgradeSlots());
      this.tier = data.getBaseTier();
      this.maxInput = EnergyNet.INSTANCE.getPowerFromTier(this.tier);
      this.transferLimit = this.maxInput;
      this.maxEnergy = data.getMaxEnergy();
      this.range = data.getBaseRange();
      this.addGuiFields(new String[]{"tier", "maxInput", "transferLimit", "maxEnergy", "energy"});
      this.addNetworkFields(new String[]{"range", "installedUpgrades"});
      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.activeTime = compound.getInt("activeTime");
      this.inactiveTime = compound.getInt("inactiveTime");
      this.energy = compound.getInt("energy");
      this.charge_slot = compound.getBoolean("charge_slot");
      this.inventory.load(compound);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "activeTime", this.activeTime, 0);
      NBTUtils.putInt(compound, "inactiveTime", this.inactiveTime, 0);
      NBTUtils.putInt(compound, "energy", this.energy, 0);
      NBTUtils.putBoolean(compound, "charge_slot", this.charge_slot, false);
      this.inventory.save(compound);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ChargePadContainer(this, player, windowID);
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return side.equals(Direction.DOWN);
   }

   @Override
   public int getSinkTier() {
      return this.tier;
   }

   @Override
   public int getRequestedEnergy() {
      return this.maxEnergy - this.energy;
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      if (amount <= this.maxEnergy && amount > 0) {
         int added = Math.min(amount, this.maxEnergy - this.energy);
         if (added > 0) {
            this.energy += added;
            this.updateGuiField("energy");
         }

         return amount - added;
      } else {
         return 0;
      }
   }

   public float getEffectHeight() {
      return 0.5F;
   }

   @OnlyIn(Dist.CLIENT)
   protected abstract int getMaxParticleAge();

   @OnlyIn(Dist.CLIENT)
   protected abstract float[] getParticleColour(RandomSource var1);

   @OnlyIn(Dist.CLIENT)
   protected abstract double[] getParticleVelocity(RandomSource var1);

   @OnlyIn(Dist.CLIENT)
   protected int getParticleAmount(RandomSource rand) {
      return 6;
   }

   @OnlyIn(Dist.CLIENT)
   public void spawnParticles(RandomSource rand) {
      float multiplicationPos = this.range * 2.0F + 0.9F;
      double blockX = (double)this.getBlockPos().getX();
      double blockZ = (double)this.getBlockPos().getZ();
      ParticleEngine er = Minecraft.getInstance().particleEngine;

      for (int i = (int)(1.0F + this.range * 2.0F); i > 0; i--) {
         for (int k = this.getParticleAmount(rand); k > 0; k--) {
            double x = blockX + 0.05000000074505806 + (double)(rand.nextFloat() * multiplicationPos) - (double)(this.range * 1.1F);
            double y = (double)((float)this.getBlockPos().getY() + 0.2F + rand.nextFloat() * 0.2F);
            double z = blockZ + 0.05000000074505806 + (double)(rand.nextFloat() * multiplicationPos) - (double)(this.range * 1.1F);
            double[] velocity = this.getParticleVelocity(rand);
            if (k < 4) {
               velocity[2] *= 0.55;
            }

            float[] colour = this.getParticleColour(rand);
            er.add(new AuraParticle((ClientLevel)this.getLevel(), x, y, z, this.getMaxParticleAge(), velocity, colour));
         }
      }
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getTier() {
      return this.tier;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.onPadUpgradeChanged();
         this.onUpgradesChanged();
         if (!this.addedToEnergyNet) {
            this.addedToEnergyNet = true;
            EnergyNet.INSTANCE.addTile(this);
         }
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnergyNet) {
         this.addedToEnergyNet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      this.inventory.addToDrops(drops);
   }

   @Override
   public void onTick() {
      this.provideEnergy();
      if (!this.isActive() && this.inactiveTime > 0) {
         this.inactiveTime--;
      } else if (this.activeTime <= 0) {
         this.setActive(false);
      } else {
         int transfer = Math.min(this.energy, this.transferLimit);
         if (transfer <= 0) {
            this.setActive(false);
            this.inactiveTime = 20;
            this.activeTime = 0;
         } else {
            this.activeTime--;
            List<LivingEntity> livings = this.getLevel()
               .getEntitiesOfClass(
                  LivingEntity.class, new AABB(this.getBlockPos()).inflate((double)this.range, (double)this.getEffectHeight(), (double)this.range), PREDICATE
               );
            if (livings.isEmpty()) {
               this.setActive(false);
               this.inactiveTime = 20;
               this.activeTime = 0;
            } else {
               boolean pvp = IC2.PLATFORM.getServer().isPvpAllowed();
               if ((this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DAMAGE.getIndex()) != 0 && pvp) {
                  for (LivingEntity living : livings) {
                     living.hurt(IC2DamageSource.newShockDamage(living), 1.0F);
                     living.addEffect(new MobEffectInstance(MobEffects.POISON, this.data.getBaseTier() * 20, this.data.getBaseTier() * 2));
                  }

                  this.energy -= transfer;
                  this.updateGuiField("energy");
               } else {
                  boolean drain = this.data.getBaseTier() > 2
                     && (this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DRAIN.getIndex()) != 0
                     && pvp;
                  boolean armor = (this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.ARMOR_PRIORITY.getIndex()) != 0;
                  List<ItemStack> toCharge = CollectionUtils.<ItemStack>createList();

                  for (LivingEntity living : livings) {
                     if (living instanceof Player) {
                        Player player = (Player)living;
                        if (armor) {
                           int last = toCharge.size();
                           this.addPlayerArmor(player, toCharge, drain);
                           if (last == toCharge.size()) {
                              this.addPlayerHotbar(player, toCharge, drain);
                           }
                        } else {
                           int last = toCharge.size();
                           this.addPlayerHotbar(player, toCharge, drain);
                           if (last == toCharge.size()) {
                              this.addPlayerArmor(player, toCharge, drain);
                           }
                        }
                     }
                  }

                  if (toCharge.isEmpty()) {
                     this.setActive(false);
                     this.activeTime = 0;
                     this.inactiveTime = 20;
                  } else {
                     if (toCharge.size() > 1) {
                        Collections.shuffle(toCharge);
                        if (toCharge.size() > transfer) {
                           toCharge = toCharge.subList(0, transfer);
                        }
                     }

                     int size = toCharge.size();

                     for (ItemStack stack : toCharge) {
                        int toAdd = transfer / size--;
                        int added = drain
                           ? ElectricItem.MANAGER.discharge(stack, toAdd, this.tier, false, false, false)
                           : ElectricItem.MANAGER.charge(stack, toAdd, this.tier, false, false);
                        transfer -= added;
                        this.energy -= added;
                     }

                     this.updateGuiField("energy");
                  }
               }
            }
         }
      }
   }

   public void provideEnergy() {
      if (this.charge_slot) {
         int needed = this.maxEnergy - this.energy;
         if (needed > 0) {
            ItemStack stack = this.inventory.getStackInSlot(0);
            if (!stack.isEmpty()) {
               if (stack.getItem() == Items.REDSTONE) {
                  this.energy += 500;
                  stack.shrink(1);
               } else if (stack.getItem() == Items.REDSTONE_BLOCK) {
                  this.energy += 4500;
                  stack.shrink(1);
               } else if (stack.getItem() == IC2Items.SU_BATTERY) {
                  this.energy += 1000;
                  stack.shrink(1);
               } else {
                  int added = ElectricItem.MANAGER.discharge(stack, needed, this.tier, false, true, false);
                  if (added > 0) {
                     this.energy += added;
                  } else {
                     this.charge_slot = false;
                  }
               }

               this.updateGuiField("energy");
            }
         }
      }
   }

   @Override
   public int getSlotCount() {
      return this.inventory.getSlotCount();
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return this.inventory.getStackInSlot(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot == 0) {
         this.charge_slot = !stack.isEmpty();
      }
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
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
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.85 - 0.05 * (double)this.data.getBaseTier();
   }

   protected void addPlayerHotbar(Player player, List<ItemStack> list, boolean drain) {
      int start = player.getInventory().selected;
      int end = start;
      if ((this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.PROXIMITY.getIndex()) != 0) {
         start = Math.max(0, start - 1);
         end = Math.min(8, end + 1);
      } else if ((this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.WIDE_BAND.getIndex()) != 0) {
         start = 0;
         end = 8;
      }

      if (start >= 0 && end <= 8) {
         for (int i = start; i <= end; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if ((drain ? ElectricItemFilter.DISCHARGE_INTERNAL_FILTER : ElectricItemFilter.CHARGE_FILTER).matches(stack)) {
               list.add(stack);
            }
         }

         ItemStack stack = player.getItemInHand(InteractionHand.OFF_HAND);
         if ((drain ? ElectricItemFilter.DISCHARGE_INTERNAL_FILTER : ElectricItemFilter.CHARGE_FILTER).matches(stack)) {
            list.add(stack);
         }
      }
   }

   protected void addPlayerArmor(Player player, List<ItemStack> list, boolean drain) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() != Type.HAND) {
            ItemStack stack = player.getItemBySlot(slot);
            if ((drain ? ElectricItemFilter.DISCHARGE_INTERNAL_FILTER : ElectricItemFilter.CHARGE_FILTER).matches(stack)) {
               list.add(stack);
            }
         }
      }

      if (IC2.CURIO_PLUGIN != null) {
         IItemHandler handler = IC2.CURIO_PLUGIN.getCurioHandler(player);
         int i = 0;

         for (int m = handler.getSlots(); i < m; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if ((drain ? ElectricItemFilter.DISCHARGE_INTERNAL_FILTER : ElectricItemFilter.CHARGE_FILTER).matches(stack)) {
               list.add(stack);
            }
         }
      }
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      return 0;
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return TYPES;
   }

   @Override
   public int getAvailableEnergy() {
      return this.energy;
   }

   @Override
   public boolean useEnergy(int toUse, boolean doUse) {
      if (this.energy >= toUse) {
         if (doUse) {
            this.energy -= toUse;
            this.updateGuiField("energy");
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isMachineWorking() {
      return false;
   }

   @Override
   public boolean isRedstoneSensitive() {
      return false;
   }

   @Override
   public void setRedstoneSensitive(boolean flag) {
   }

   public void onPadUpgradeChanged() {
      this.range = this.data.getBaseRange();
      this.installedUpgrades = 0;

      for (int i = 0; i < this.data.getSlots(); i++) {
         ItemStack stack = this.inventory.getStackInSlot(1 + i);
         if (stack.getItem() instanceof PadUpgradeItem) {
            this.installedUpgrades = this.installedUpgrades | 1 << ((PadUpgradeItem)stack.getItem()).getUpgrade().getIndex();
         }
      }

      for (int ix = 0; ix < 3; ix++) {
         if ((this.installedUpgrades & 1 << ix + 5) != 0) {
            this.range += (float)(ix + 1);
         }
      }

      this.updateTileFields(new String[]{"installedUpgrades", "range"});
   }

   @Override
   public void onUpgradesChanged() {
      int extraSpeed = 0;
      double speedMultiplier = 1.0;
      int extraEnergy = 0;
      double energyMultiplier = 1.0;
      int extraTier = 0;
      int slotOffset = this.data.getBaseTier() > 1 ? 3 : 1;

      for (int i = 0; i < this.data.getUpgradeSlots() && i + slotOffset < this.inventory.getSlotCount(); i++) {
         ItemStack item = this.inventory.getStackInSlot(i + slotOffset);
         if (item.getItem() instanceof IUpgradeItem upgrade) {
            upgrade.onInstall(item, this);
            extraSpeed += upgrade.getExtraProcessingSpeed(item, this) * item.getCount();
            speedMultiplier *= Math.pow(upgrade.getProcessingSpeedMultiplier(item, this), (double)item.getCount());
            extraEnergy += upgrade.getExtraEnergyStorage(item, this) * item.getCount();
            energyMultiplier *= Math.pow(upgrade.getEnergyStorageMultiplier(item, this), (double)item.getCount());
            extraTier += upgrade.getExtraTier(item, this) * item.getCount();
         }
      }

      this.transferLimit = RecipeMods.apply(EnergyNet.INSTANCE.getPowerFromTier(this.data.getBaseTier()), extraSpeed, speedMultiplier);
      this.maxEnergy = RecipeMods.apply(this.data.getMaxEnergy(), extraEnergy, energyMultiplier);
      this.tier = Mth.clamp(this.data.getBaseTier() + extraTier, 1, 13);
      this.maxInput = EnergyNet.INSTANCE.getPowerFromTier(this.tier);
      this.updateGuiFields(new String[]{"transferLimit", "maxEnergy", "tier", "maxInput"});
   }

   @Override
   public IItemHandler getConnectedInventory(Direction dir) {
      return null;
   }

   @Override
   public ITube getConnectedTube(Direction dir) {
      return null;
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return side == Direction.DOWN ? 0 : (this.isActive() ? 15 : 0);
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   public static class ChargePadData {
      int maxEnergy;
      int baseTier;
      int slots;
      int upgradeSlots;
      float baseRange;
      float maxImpact;

      public ChargePadData(int maxEnergy, int baseTier, int slots, int upgradeSlots, float baseRange, float maxImpact) {
         this.maxEnergy = maxEnergy;
         this.baseTier = baseTier;
         this.slots = slots;
         this.upgradeSlots = upgradeSlots;
         this.baseRange = baseRange;
         this.maxImpact = maxImpact;
      }

      public int getBaseTier() {
         return this.baseTier;
      }

      public int getMaxEnergy() {
         return this.maxEnergy;
      }

      public float getMaxImpact() {
         return this.maxImpact;
      }

      public int getSlots() {
         return this.slots;
      }

      public int getUpgradeSlots() {
         return this.upgradeSlots;
      }

      public float getBaseRange() {
         return this.baseRange;
      }
   }

   public static enum PadUpgrade implements CollectionUtils.IIndexEnum {
      DAMAGE(false, true, 2, 0),
      DRAIN(false, false, 2, 1),
      PROXIMITY(true, false, 2, 2),
      WIDE_BAND(true, true, 2, 3),
      ARMOR_PRIORITY(false, false, 2, 4),
      FIELD_MK1(true, false, 2, 5),
      FIELD_MK2(true, false, 3, 6),
      FIELD_MK3(true, true, 3, 7);

      static final BaseChargePadTileEntity.PadUpgrade[] UPGRADES = CollectionUtils.createSortedArray(values());
      final boolean isProjection;
      final boolean isRare;
      final int baseTier;
      final int index;

      private PadUpgrade(boolean isProjection, boolean isRare, int baseTier, int index) {
         this.isProjection = isProjection;
         this.isRare = isRare;
         this.baseTier = baseTier;
         this.index = index;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      public boolean isProjection() {
         return this.isProjection;
      }

      public boolean isRare() {
         return this.isRare;
      }

      public int getBaseTier() {
         return this.baseTier;
      }

      public static BaseChargePadTileEntity.PadUpgrade byIndex(int index) {
         return UPGRADES[index % UPGRADES.length];
      }
   }
}
