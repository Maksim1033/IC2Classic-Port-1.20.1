package ic2.core.item.wearable.base;

import ic2.api.network.item.INetworkItemEvent;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioSource;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.RocketModePacket;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class IC2JetpackBase extends IC2AdvancedArmorBase implements IArmorToolTip, INetworkItemEvent {
   public static ResourceLocation full = new ResourceLocation("ic2", "sounds/tools/jetpack/jetpack_loop.ogg");
   public static ResourceLocation empty = new ResourceLocation("ic2", "sounds/tools/jetpack/jetpack_fire.ogg");
   public static final String DISABLED = "disabled";
   public static final String TOGGLE_TIMER = "ToggleTimer";
   public static final String JETPACK_TICKER = "JetpackTicker";
   public static final String HOVER_MODE = "HoverMode";
   public static final String ROCKET_MODE = "RocketMode";
   public static final String ROCKET_DELAY = "RocketDelay";
   public static final String LAST_USE_MODE = "LastUseMode";
   public static final String USED_ENERGY = "UsedEnergy";
   public static final String FUEL_USE_TIME = "FuelUseTime";
   public static final String FUNKY_MODE = "SpecialMode";
   public static final String DOWNWARDS_ACCELERATING = "SpecialMovement";
   public static final String DOWNWARDS_ACCELERATING_TICKER = "SpecialMovementTicker";

   public IC2JetpackBase(String itemName, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      super(itemName, slot, props);
   }

   public abstract boolean canDoRocketMode(ItemStack var1);

   public abstract boolean canDoAdvHoverMode(ItemStack var1);

   public abstract boolean isElectricJetpack(ItemStack var1);

   public abstract float getPower(ItemStack var1);

   public abstract float getThruster(ItemStack var1, IC2JetpackBase.HoverMode var2);

   public abstract float getDropPercentage(ItemStack var1);

   public abstract int getMaxHeight(ItemStack var1, int var2);

   public abstract int getFuel(ItemStack var1);

   public abstract int getMaxFuel(ItemStack var1);

   public abstract int getMaxRocketCharge(ItemStack var1);

   public abstract int getFuelCost(ItemStack var1, IC2JetpackBase.HoverMode var2);

   public abstract void useEnergy(Player var1, ItemStack var2, int var3);

   public boolean canDisable(ItemStack stack) {
      return true;
   }

   public CompoundTag getNBTData(ItemStack stack, boolean create) {
      return create ? stack.getOrCreateTag() : StackUtil.getNbtData(stack);
   }

   public IAudioSource createAudioSource(ItemStack stack, Player player, IC2JetpackBase.JetpackUseMode mode) {
      return IC2.AUDIO.createSource(player, mode == IC2JetpackBase.JetpackUseMode.FULL ? full : empty, AudioManager.SoundType.BACKPACK, 1.0F, true, false);
   }

   @Override
   public Ingredient getRepairMaterial() {
      return Ingredient.EMPTY;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.JUMP_KEY, "tooltip.item.ic2.jetpack.jump_to_fly", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.jetpack.mode_switch", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.jetpack.quick_toggle", new Object[0]));
      if (this.canDisable(stack)) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.TOGGLE_KEY, "tooltip.item.ic2.jetpack.toggle", new Object[0]));
      }

      if (this.canDoRocketMode(stack)) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, KeyHelper.BOOST_KEY, "tooltip.item.ic2.jetpack.toggle_rocket", new Object[0]));
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BOOST_KEY, "tooltip.item.ic2.jetpack.charge_rocket", new Object[0]));
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.jetpack.release_rocket", new Object[0]));
      }

      if (this instanceof IHasHeldGui) {
         helper.addKeybindingTooltip("");
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      }

      CompoundTag nbt = this.getNBTData(stack, false);
      if (nbt.getBoolean("RocketMode")) {
         helper.addSimpleToolTip(
            "tooltip.item.ic2.jetpack.rocket_charge", (int)((double)nbt.getInt("UsedEnergy") / (double)this.getMaxRocketCharge(stack) * 100.0)
         );
         helper.addSimpleToolTip("");
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack armor, ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.JUMP_KEY, "tooltip.item.ic2.jetpack.jump_to_fly", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.jetpack.mode_switch", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.jetpack.quick_toggle", new Object[0]));
      if (this.canDisable(stack)) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.TOGGLE_KEY, "tooltip.item.ic2.jetpack.toggle", new Object[0]));
      }

      if (this.canDoRocketMode(stack)) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, KeyHelper.BOOST_KEY, "tooltip.item.ic2.jetpack.toggle_rocket", new Object[0]));
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BOOST_KEY, "tooltip.item.ic2.jetpack.charge_rocket", new Object[0]));
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.jetpack.release_rocket", new Object[0]));
      }

      if (this instanceof IHasHeldGui) {
         helper.addKeybindingTooltip("");
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      }

      CompoundTag nbt = this.getNBTData(stack, false);
      if (nbt.getBoolean("RocketMode")) {
         helper.addSimpleToolTip(
            "tooltip.item.ic2.jetpack.rocket_charge", (int)((double)nbt.getInt("UsedEnergy") / (double)this.getMaxRocketCharge(stack) * 100.0)
         );
         helper.addSimpleToolTip("");
      }
   }

   public IC2JetpackBase.JetpackUseMode useJetpack(
      ItemStack stack, Player player, PlayerHandler handler, IC2JetpackBase.HoverMode hoverMode, Entity affectedEntity
   ) {
      int fuel = this.getFuel(stack);
      if (fuel <= 0) {
         return IC2JetpackBase.JetpackUseMode.NONE;
      } else {
         IC2JetpackBase.JetpackUseMode jetpackMode = IC2JetpackBase.JetpackUseMode.NONE;
         boolean electric = this.isElectricJetpack(stack);
         float power = this.getPower(stack);
         float dropPercentage = this.getDropPercentage(stack);
         if ((double)fuel / (double)this.getMaxFuel(stack) <= (double)dropPercentage) {
            power *= (float)((double)fuel / (double)((float)this.getMaxFuel(stack) * dropPercentage));
            jetpackMode = IC2JetpackBase.JetpackUseMode.DROPPED;
         }

         boolean elytra = player.isFallFlying();
         if (handler.forwardKeyDown && !elytra) {
            float thruster = this.getThruster(stack, hoverMode);
            float forwardPower = power * thruster * 2.0F;
            if (forwardPower > 0.0F) {
               affectedEntity.moveRelative(0.02F, new Vec3(0.0, 0.0, 0.4 * (double)forwardPower));
            }
         }

         int maxFlightHeight = this.getMaxHeight(stack, player.level().getMaxBuildHeight());
         double y = affectedEntity.getY();
         if (y > (double)(maxFlightHeight - 25)) {
            if (y > (double)maxFlightHeight) {
               y = (double)maxFlightHeight;
            }

            power *= (float)(((double)maxFlightHeight - y) / 25.0);
         }

         Vec3 motion = affectedEntity.getDeltaMovement();
         CompoundTag nbt = this.getNBTData(stack, true);
         double min_y = Math.min(motion.y + (double)(power * 0.2F), 0.6000000238418579);
         if (nbt.getBoolean("SpecialMode") && !player.onGround()) {
            affectedEntity.setDeltaMovement(motion.add(0.0, min_y, 0.0));
            if (IC2.PLATFORM.isSimulating()) {
               player.getPersistentData().putBoolean("SpecialMovement", affectedEntity.getDeltaMovement().y < 0.0);
               player.getPersistentData().putInt("SpecialMovementTicker", 40);
            }
         } else if (elytra) {
            float thruster = this.getThruster(stack, hoverMode);
            float forwardPower = power * thruster * 2.0F;
            if (forwardPower > 0.0F) {
               affectedEntity.moveRelative(0.1F, new Vec3(0.0, 0.0, 0.4 * (double)Math.min(forwardPower, 0.7F)));
            }
         } else {
            affectedEntity.setDeltaMovement(motion.x, min_y, motion.z);
         }

         if (hoverMode != IC2JetpackBase.HoverMode.NONE) {
            float maxHoverY = hoverMode == IC2JetpackBase.HoverMode.BASIC ? -0.2F : 0.0F;
            if (!player.isShiftKeyDown() || !handler.jumpKeyDown) {
               maxHoverY = hoverMode != IC2JetpackBase.HoverMode.BASIC && !player.isShiftKeyDown() ? 0.0F : -0.2F;
               if (electric && handler.jumpKeyDown) {
                  maxHoverY = hoverMode == IC2JetpackBase.HoverMode.BASIC ? 0.1F : 0.3F;
               }
            }

            Vec3 affectedMotion = affectedEntity.getDeltaMovement();
            if (affectedMotion.y > (double)maxHoverY) {
               affectedEntity.setDeltaMovement(affectedMotion.x, Math.max(motion.y, (double)maxHoverY), affectedMotion.z);
            }
         }

         byte time = nbt.getByte("FuelUseTime");
         if (time == 0) {
            this.useEnergy(player, stack, this.getFuelCost(stack, hoverMode));
            if (jetpackMode != IC2JetpackBase.JetpackUseMode.DROPPED) {
               jetpackMode = IC2JetpackBase.JetpackUseMode.FULL;
            }

            if (hoverMode == IC2JetpackBase.HoverMode.BASIC && !handler.jumpKeyDown) {
               time = 5;
               nbt.putByte("FuelUseTime", time);
            }
         } else if (time <= 1 && jetpackMode != IC2JetpackBase.JetpackUseMode.DROPPED) {
            jetpackMode = IC2JetpackBase.JetpackUseMode.FULL;
         }

         if (!nbt.getBoolean("SpecialMode")) {
            affectedEntity.fallDistance = 0.0F;
            affectedEntity.walkDist = 0.0F;
            IC2.PLATFORM.resetPlayerInAirTime(player);
            IC2.NETWORKING.get().sendClientItemEvent(stack, 100, 0);
         }

         if (jetpackMode != IC2JetpackBase.JetpackUseMode.NONE) {
            float yOff = elytra ? 0.2F : 0.75F;
            Vec2 offset = this.createOffset(player, -0.65F, elytra);
            affectedEntity.level()
               .addParticle(
                  ParticleTypes.SMALL_FLAME, player.getX() + (double)offset.x, player.getY() + (double)yOff, player.getZ() + (double)offset.y, 0.0, 0.0, 0.0
               );
            offset = this.createOffset(player, 0.65F, elytra);
            affectedEntity.level()
               .addParticle(
                  ParticleTypes.SMALL_FLAME, player.getX() + (double)offset.x, player.getY() + (double)yOff, player.getZ() + (double)offset.y, 0.0, 0.0, 0.0
               );
         }

         return jetpackMode;
      }
   }

   private Vec2 createOffset(Player player, float offset, boolean elytra) {
      float yRotation = player.yBodyRot - (float)Math.toDegrees((double)offset);
      float scale = elytra ? 0.1F : 0.25F;
      float xOff = Mth.sin(-yRotation * 0.017453292F - 3.1415927F) * scale;
      float zOff = Mth.cos(-yRotation * 0.017453292F - 3.1415927F) * scale;
      return new Vec2(xOff, zOff);
   }

   public void onArmorTick(ItemStack stack, Level world, Player player) {
      CompoundTag nbt = this.getNBTData(stack, true);
      boolean disabled = nbt.getBoolean("disabled");
      byte jetpackTicker = nbt.getByte("JetpackTicker");
      byte toggleTimer = nbt.getByte("ToggleTimer");
      PlayerHandler handler = PlayerHandler.getHandler(player);
      Entity entity = player.getRootVehicle();
      boolean server = IC2.PLATFORM.isSimulating();
      if (disabled) {
         if (jetpackTicker > 0) {
            nbt.putByte("JetpackTicker", --jetpackTicker);
         } else if (handler.toggleKeyDown && !handler.screenOpen) {
            nbt.putByte("JetpackTicker", (byte)10);
            nbt.putBoolean("disabled", false);
            if (server) {
               player.displayClientMessage(this.translate("info.item.ic2.jetpack.on"), false);
            }
         }
      } else if (this.canDisable(stack) && handler.toggleKeyDown && !handler.screenOpen && jetpackTicker <= 0) {
         nbt.putByte("JetpackTicker", (byte)10);
         nbt.putBoolean("disabled", true);
         nbt.putByte("LastUseMode", IC2JetpackBase.JetpackUseMode.NONE.index);
         if (server) {
            player.displayClientMessage(this.translate("info.item.ic2.jetpack.off"), false);
         }
      } else {
         if (jetpackTicker > 0) {
            nbt.putByte("JetpackTicker", --jetpackTicker);
         }

         if (server) {
            int accelerationTicker = player.getPersistentData().getInt("SpecialMovementTicker");
            if (accelerationTicker > 0) {
               player.getPersistentData().putInt("SpecialMovementTicker", --accelerationTicker);
               if (accelerationTicker == 0) {
                  player.getPersistentData().putBoolean("SpecialMovement", false);
               }
            }
         }

         IC2JetpackBase.HoverMode hoverMode = IC2JetpackBase.HoverMode.byIndex(nbt.getByte("HoverMode"));
         boolean rocketMode = nbt.getBoolean("RocketMode");
         short rocketDelay = nbt.getShort("RocketDelay");
         boolean jetpackUsed = false;
         IC2JetpackBase.JetpackUseMode useMode = IC2JetpackBase.JetpackUseMode.NONE;
         if (rocketMode && !this.canDoRocketMode(stack)) {
            rocketMode = false;
            nbt.putBoolean("RocketMode", false);
         }

         if (handler.jumpKeyDown && handler.modeSwitchKeyDown && !handler.altKeyDown && toggleTimer <= 0) {
            toggleTimer = 10;
            hoverMode = hoverMode.getNext(this.canDoAdvHoverMode(stack));
            nbt.putByte("HoverMode", hoverMode.index);
            if (hoverMode != IC2JetpackBase.HoverMode.BASIC && hoverMode != IC2JetpackBase.HoverMode.ADV) {
               if (server) {
                  player.displayClientMessage(this.translate("info.item.ic2.jetpack.hover_off"), false);
               }
            } else {
               if (server) {
                  player.displayClientMessage(
                     this.translate(hoverMode == IC2JetpackBase.HoverMode.BASIC ? "info.item.ic2.jetpack.hover_on" : "info.item.ic2.jetpack.adv_hover_on"),
                     false
                  );
               }

               if (rocketMode) {
                  rocketMode = false;
                  nbt.putBoolean("RocketMode", false);
                  if (server) {
                     player.displayClientMessage(this.translate("info.item.ic2.jetpack.rocket_mode_off_forced"), false);
                  }

                  this.validateRocketCharge(nbt);
               }
            }
         }

         if (handler.modeSwitchKeyDown && handler.boostKeyDown && this.canDoRocketMode(stack) && toggleTimer == 0) {
            toggleTimer = 10;
            rocketMode = !rocketMode;
            nbt.putBoolean("RocketMode", rocketMode);
            if (rocketMode) {
               if (server) {
                  player.displayClientMessage(this.translate("info.item.ic2.jetpack.rocket_mode_on"), false);
               }

               if (hoverMode != IC2JetpackBase.HoverMode.NONE) {
                  hoverMode = IC2JetpackBase.HoverMode.NONE;
                  nbt.putByte("HoverMode", hoverMode.index);
                  if (server) {
                     player.displayClientMessage(this.translate("info.item.ic2.jetpack.hover_off_forced"), false);
                  }
               }
            } else {
               if (server) {
                  player.displayClientMessage(this.translate("info.item.ic2.jetpack.rocket_mode_off"), false);
               }

               this.validateRocketCharge(nbt);
            }
         }

         if (rocketMode && handler.boostKeyDown && rocketDelay == 0) {
            jetpackUsed = this.chargeRocketMode(player, stack);
            useMode = IC2JetpackBase.JetpackUseMode.FULL;
         }

         if (handler.altKeyDown && rocketMode && rocketDelay <= 0) {
            rocketDelay = this.releaseRocket(player, stack, entity);
            nbt.putBoolean("RocketMode", false);
            world.playSound(
               null,
               player.getX(),
               player.getY(),
               player.getZ(),
               SoundEvents.GENERIC_EXPLODE,
               SoundSource.PLAYERS,
               4.0F,
               (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F
            );
            if (server && rocketDelay > 0) {
               player.awardStat(IC2Stats.ROCKET_MODE_USED);
            }
         } else if (handler.altKeyDown && rocketDelay <= 0 && !handler.modeSwitchKeyDown) {
            hoverMode = hoverMode.getNext(false);
            toggleTimer = 10;
         }

         if (handler.jumpKeyDown && !rocketMode && rocketDelay == 0
            || hoverMode != IC2JetpackBase.HoverMode.NONE
               && (
                  hoverMode == IC2JetpackBase.HoverMode.ADV && !entity.onGround()
                     || hoverMode == IC2JetpackBase.HoverMode.BASIC && !entity.onGround() && entity.getDeltaMovement().y < -0.15
               )) {
            useMode = this.useJetpack(stack, player, handler, hoverMode, entity);
            jetpackUsed = useMode != IC2JetpackBase.JetpackUseMode.NONE;
         }

         if (toggleTimer > 0) {
            nbt.putByte("ToggleTimer", --toggleTimer);
         }

         if (rocketDelay > 0) {
            nbt.putShort("RocketDelay", --rocketDelay);
         }

         byte time = nbt.getByte("FuelUseTime");
         if (time > 0) {
            nbt.putByte("FuelUseTime", --time);
         }

         if (server && jetpackUsed) {
            player.awardStat(IC2Stats.JETPACK_FLY_TIME);
         }

         nbt.putByte("LastUseMode", useMode.index);
      }
   }

   public short releaseRocket(Player player, ItemStack stack, Entity affectedEntity) {
      CompoundTag nbt = this.getNBTData(stack, true);
      int charge = nbt.getInt("UsedEnergy");
      if (charge <= 0) {
         return 0;
      } else {
         nbt.putInt("UsedEnergy", 0);
         this.useEnergy(player, stack, 50);
         int charges = charge / this.getFuelCost(stack, IC2JetpackBase.HoverMode.NONE) / 50;
         float speed = this.getPower(stack) * (float)charges / 4.0F;
         if (player.isFallFlying()) {
            float thruster = this.getThruster(stack, IC2JetpackBase.HoverMode.NONE);
            float power = this.getPower(stack);
            float forwardPower = power * thruster * 2.0F * Math.min(speed * 2.0F, 10.0F);
            if (forwardPower > 0.0F) {
               affectedEntity.moveRelative(10.0F, new Vec3(0.0, 0.0, 0.4 * (double)forwardPower));
               if (player instanceof ServerPlayer) {
                  PacketManager.INSTANCE.sendToPlayer(new RocketModePacket(true, forwardPower, affectedEntity.getId()), player);
               }
            }

            return (short)((int)(speed * 10.0F));
         } else {
            affectedEntity.setDeltaMovement(affectedEntity.getDeltaMovement().add(0.0, (double)Math.min(speed, 10.0F), 0.0));
            if (player instanceof ServerPlayer) {
               PacketManager.INSTANCE.sendToPlayer(new RocketModePacket(false, Math.min(speed, 10.0F), affectedEntity.getId()), player);
            }

            affectedEntity.fallDistance = 0.0F;
            affectedEntity.walkDist = 0.0F;
            IC2.PLATFORM.resetPlayerInAirTime(player);
            return (short)((int)(speed * 10.0F));
         }
      }
   }

   public boolean chargeRocketMode(Player player, ItemStack stack) {
      if (this.getFuel(stack) == 0) {
         return false;
      } else {
         CompoundTag nbt = this.getNBTData(stack, true);
         int chargedEnergy = nbt.getInt("UsedEnergy");
         if (chargedEnergy > this.getMaxRocketCharge(stack)) {
            return false;
         } else {
            int used = Math.min(this.getFuelCost(stack, IC2JetpackBase.HoverMode.NONE) * 5, this.getFuel(stack));
            this.useEnergy(player, stack, used);
            nbt.putInt("UsedEnergy", chargedEnergy + used);
            return true;
         }
      }
   }

   public void validateRocketCharge(CompoundTag nbt) {
      nbt.remove("UsedEnergy");
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (!target.isClient() && key == 100) {
         IC2.PLATFORM.resetPlayerInAirTime(player);
      }
   }

   public boolean isDamaged(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      return (int)Math.round((double)this.getFuel(stack) / (double)this.getMaxFuel(stack) * 13.0);
   }

   public int getBarColor(ItemStack stack) {
      return this.isElectricJetpack(stack) ? IC2ElectricItem.getRGBDurability(stack) : super.getBarColor(stack);
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public IC2JetpackBase.JetpackUseMode getJetpackUse(ItemStack stack) {
      return IC2JetpackBase.JetpackUseMode.byIndex(this.getNBTData(stack, false).getInt("LastUseMode"));
   }

   public static enum HoverMode implements CollectionUtils.IIndexEnum {
      NONE(0),
      BASIC(1),
      ADV(2);

      final byte index;
      static final IC2JetpackBase.HoverMode[] VALUES = CollectionUtils.createSortedArray(values());

      private HoverMode(int index) {
         this.index = (byte)index;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      public static IC2JetpackBase.HoverMode byIndex(int index) {
         return VALUES[index % VALUES.length];
      }

      public IC2JetpackBase.HoverMode getNext(boolean canAdv) {
         switch (this) {
            case NONE:
               return BASIC;
            case BASIC:
               return canAdv ? ADV : NONE;
            default:
               return NONE;
         }
      }
   }

   public interface IIndirectJetpack {
      IC2JetpackBase getJetpack(ItemStack var1);
   }

   public static enum JetpackUseMode implements CollectionUtils.IIndexEnum {
      FULL(0),
      DROPPED(1),
      NONE(2);

      final byte index;
      static final IC2JetpackBase.JetpackUseMode[] VALUES = CollectionUtils.createSortedArray(values());

      private JetpackUseMode(int index) {
         this.index = (byte)index;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      public static IC2JetpackBase.JetpackUseMode byIndex(int index) {
         return VALUES[index % VALUES.length];
      }
   }
}
