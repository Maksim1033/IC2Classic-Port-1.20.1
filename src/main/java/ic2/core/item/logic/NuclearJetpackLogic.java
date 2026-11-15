package ic2.core.item.logic;

import ic2.api.items.electric.ElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.entity.explosion.IC2Explosion;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.item.wearable.base.IC2ElectricJetpackBase;
import ic2.core.utils.helpers.SanityHelper;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;

public class NuclearJetpackLogic extends TickableItemLogic implements IReactor {
   SimpleInventory inventory;
   public int tick;
   public float output;
   public int heat;
   public int componentHeat;
   public int maxHeat;
   public float hem;
   public boolean active;
   public EquipmentSlot slot;

   public NuclearJetpackLogic(ItemStack stack, Player player, EquipmentSlot type) {
      super(stack, player);
      this.slot = type;
   }

   @Override
   public CompoundTag getNBTData(ItemStack stack, boolean create) {
      CompoundTag data = super.getNBTData(stack, create);
      if (!(stack.getItem() instanceof IC2ElectricJetpackBase)) {
         CompoundTag subData = data.getCompound("jetpack_data");
         if (create) {
            data.put("jetpack_data", subData);
         }

         return subData;
      } else {
         return data;
      }
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      this.inventory.save(nbt);
      nbt.putInt("tick", this.tick);
      nbt.putFloat("output", this.output);
      nbt.putInt("heat", this.heat);
      nbt.putInt("component_heat", this.componentHeat);
      nbt.putInt("max", this.maxHeat);
      nbt.putFloat("hem", this.hem);
      nbt.putBoolean("active", this.active);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.inventory = new SimpleInventory(25);
      this.inventory.load(nbt);
      this.tick = nbt.getInt("tick");
      this.output = nbt.getFloat("output");
      this.heat = nbt.getInt("heat");
      this.componentHeat = nbt.getInt("component_heat");
      this.maxHeat = nbt.getInt("max");
      this.hem = nbt.getFloat("hem");
      this.active = nbt.getBoolean("active");
   }

   @Override
   public void onTick(ItemStack stack) {
      if (this.tick++ % this.getTickRate() == 0) {
         this.updateComponentState(stack);
         if (this.updateReactor()) {
            return;
         }

         this.updateComponentInfo();
      }

      if (this.active) {
         ElectricItem.MANAGER.charge(stack, (int)this.getEnergyOutput(), 2147483647, true, false);
         this.updateComponentState(stack);
      }
   }

   protected void updateComponentState(ItemStack stack) {
      double max = (double)ElectricItem.MANAGER.getCapacity(stack);
      if (max <= 0.0) {
         this.active = false;
      } else if (!this.active) {
         if ((double)ElectricItem.MANAGER.getCharge(stack) / max <= 0.3) {
            this.active = true;
         }
      } else {
         if ((double)ElectricItem.MANAGER.getCharge(stack) >= max) {
            this.active = false;
         }
      }
   }

   protected void updateComponentInfo() {
      double value = 0.0;
      int total = 0;

      for (int i = 0; i < 25; i++) {
         ItemStack stack = this.inventory.getStackInSlot(i);
         Item max = stack.getItem();
         if (max instanceof IReactorComponent) {
            IReactorComponent comp = (IReactorComponent)max;
            double maxx = (double)comp.getMaxStoredHeat(stack, this, i % 5, i / 5);
            if (maxx > 0.0) {
               value += (double)comp.getStoredHeat(stack, this, i % 5, i / 5) / maxx;
               total++;
            }
         }
      }

      this.componentHeat = (int)(total > 0 ? value / (double)total * 100.0 : 0.0);
   }

   public boolean updateReactor() {
      this.output = 0.0F;
      this.maxHeat = 10000;
      this.hem = 1.0F;
      this.processChamber();
      return this.calculateHeatEffects();
   }

   public void processChamber() {
      for (int pass = 0; pass < 2; pass++) {
         boolean tick = pass == 0;

         for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
               ItemStack stack = this.getStackInReactor(x, y);
               if (stack.getItem() instanceof IReactorComponent) {
                  ((IReactorComponent)stack.getItem()).processChamber(stack, this, x, y, tick, tick);
               }
            }
         }
      }
   }

   @Override
   public Level getWorldObj() {
      return this.player.level();
   }

   @Override
   public BlockPos getPosition() {
      return this.player.blockPosition();
   }

   public boolean calculateHeatEffects() {
      if (this.heat < 4000) {
         return false;
      } else if (IC2.CONFIG.reactorDamage.get() <= 0.0) {
         return false;
      } else {
         float power = (float)this.heat / (float)this.maxHeat;
         if (power >= 1.0F) {
            this.explode();
            return true;
         } else {
            RandomSource rand = this.getWorldObj().random;
            if (power >= 0.85F && rand.nextFloat() <= 0.2F * this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getWorldObj().getBlockState(nextPos);
                  if (state.getBlock() == Blocks.AIR) {
                     this.getWorldObj().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                  } else {
                     float hardness = state.getDestroySpeed(this.getWorldObj(), nextPos);
                     if (hardness >= 0.0F && hardness <= 10.0F && this.getWorldObj().getBlockEntity(this.getPosition()) == null) {
                        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                           this.getWorldObj()
                              .setBlockAndUpdate(
                                 nextPos,
                                 ((FluidState)Fluids.FLOWING_LAVA.defaultFluidState().setValue(BlockStateProperties.LEVEL_FLOWING, 8)).createLegacyBlock()
                              );
                        } else {
                           this.getWorldObj().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                        }
                     }
                  }
               }
            }

            if (power >= 0.7F) {
               for (LivingEntity entity : this.getWorldObj().getEntitiesOfClass(LivingEntity.class, new AABB(this.getPosition()).inflate(3.0))) {
                  entity.hurt(IC2DamageSource.RADIATION, (float)rand.nextInt(4) * this.hem);
               }
            }

            if (power >= 0.5F && rand.nextFloat() <= this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getWorldObj().getBlockState(nextPos);
                  Fluid fluid = state.getFluidState().getType();
                  if (state.getBlock() instanceof BucketPickup && fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                     ((BucketPickup)state.getBlock()).pickupBlock(this.getWorldObj(), nextPos, state);
                  }
               }
            }

            if (power >= 0.4F && rand.nextFloat() <= this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getWorldObj().getBlockState(nextPos);
                  if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES) || state.ignitedByLava()) {
                     this.getWorldObj().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                  }
               }
            }

            return false;
         }
      }
   }

   public BlockPos getRandomPos(RandomSource rand, int radius) {
      if (radius <= 0) {
         return null;
      } else {
         int x = rand.nextInt(2 * radius + 1) - radius;
         int y = rand.nextInt(2 * radius + 1) - radius;
         int z = rand.nextInt(2 * radius + 1) - radius;
         BlockPos pos = this.getPosition();
         BlockPos nextPos = pos.offset(x, y, z);
         return pos.equals(nextPos) ? null : nextPos;
      }
   }

   @Override
   public int getHeat() {
      return this.heat;
   }

   @Override
   public void setHeat(int newHeat) {
      this.heat = newHeat;
   }

   @Override
   public void addHeat(int heat) {
      this.heat += heat;
   }

   @Override
   public int getMaxHeat() {
      return this.maxHeat;
   }

   @Override
   public void setMaxHeat(int heat) {
      this.maxHeat = heat;
   }

   @Override
   public float getHeatEffectModifier() {
      return this.hem;
   }

   @Override
   public void setHeatEffectModifier(float hem) {
      this.hem = hem;
   }

   @Override
   public double getEnergyOutput() {
      return (double)(this.output * (float)IC2.CONFIG.reactorOutput.get());
   }

   @Override
   public void addOutput(float output) {
      this.output += output;
   }

   @Override
   public ItemStack getStackInReactor(int x, int y) {
      return x >= 0 && x <= 4 && y >= 0 && y <= 4 ? this.inventory.getStackInSlot(x + y * 5) : ItemStack.EMPTY;
   }

   @Override
   public void setStackInReactor(int x, int y, ItemStack stack) {
      if (x >= 0 && x <= 4 && y >= 0 && y <= 4) {
         this.inventory.setStackInSlot(x + y * 5, stack);
      }
   }

   @Override
   public void explode() {
      if (!IC2.PLATFORM.isRendering()) {
         float boomPower = 10.0F;
         float boomMod = 1.0F;

         for (int i = 0; i < 25; i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof IReactorComponent) {
               float value = ((IReactorComponent)stack.getItem()).getExplosionInfluence(stack, this);
               if (value > 0.0F && value < 1.0F) {
                  boomMod *= value;
               } else {
                  boomPower += value;
               }
            }
         }

         Player player = this.getPlayer();
         boomPower = Math.min((float)IC2.CONFIG.reactorDamage.get(), boomPower * this.hem * boomMod);
         IC2.LOGGER
            .info(
               "Nuclear Jetpack at {}:({},{},{}) melted (explosion power {})",
               SanityHelper.toPascalCase(player.level().dimension().location().getPath()),
               player.getX(),
               player.getY(),
               player.getZ(),
               boomPower
            );
         this.setStack(player, ItemStack.EMPTY);
         player.containerMenu.broadcastChanges();
         new IC2Explosion(player.level(), player, new Vec3(player.getX(), player.getY(), player.getZ()), boomPower, 1.5F, IC2DamageSource.NUKE).doExplosion();
      }
   }

   @Override
   public int getTickRate() {
      return 20;
   }

   @Override
   public boolean isProducingEnergy() {
      return this.active;
   }

   protected void setStack(Player player, ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      if (data.contains("curio_index") && IC2.CURIO_PLUGIN != null) {
         data = data.getCompound("curio_index");
         int index = data.getInt("index");
         IItemHandlerModifiable handler = IC2.CURIO_PLUGIN.getCurioInv(player, data.getString("id"));
         if (index < handler.getSlots()) {
            handler.setStackInSlot(index, stack);
            return;
         }
      }

      player.setItemSlot(this.slot, stack);
   }
}
