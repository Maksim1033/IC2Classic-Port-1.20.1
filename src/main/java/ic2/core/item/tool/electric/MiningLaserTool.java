package ic2.core.item.tool.electric;

import ic2.api.events.LaserEvent;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.items.electric.IMiningDrill;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.entity.misc.MiningLaserEntity;
import ic2.core.entity.misc.TrackingMiningLaserEntity;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

public class MiningLaserTool extends IC2ElectricItem implements ISimpleItemModel, IMiningDrill, IElectricEnchantable, IToolModel {
   public static final int[] MODE_COST = new int[]{1250, 100, 5000, 0, 2500, 10000, 5000, 10000};

   public MiningLaserTool() {
      super("mining_laser");
      this.capacity = 200000;
      this.tier = 2;
      this.transferLimit = 120;
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return 80;
   }

   public Component getModeName(int mode) {
      switch (mode) {
         case 0:
            return this.translate("tooltip.item.ic2.mining_laser.mode.mining");
         case 1:
            return this.translate("tooltip.item.ic2.mining_laser.mode.low_focus");
         case 2:
            return this.translate("tooltip.item.ic2.mining_laser.mode.long_range");
         case 3:
            return this.translate("tooltip.item.ic2.mining_laser.mode.horizontal");
         case 4:
            return this.translate("tooltip.item.ic2.mining_laser.mode.super_heat");
         case 5:
            return this.translate("tooltip.item.ic2.mining_laser.mode.scatter");
         case 6:
            return this.translate("tooltip.item.ic2.mining_laser.mode.explosive");
         case 7:
            return this.translate("tooltip.item.ic2.mining_laser.mode.tracking");
         default:
            return this.translate("tooltip.item.ic2.error");
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addSimpleToolTip("tooltip.item.ic2.mining_laser.mode", this.getModeName(this.getMode(stack)));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.mining_laser.mode_switch", new Object[0]));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("mining_laser");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isRendering()) {
         return InteractionResultHolder.success(stack);
      } else {
         int mode = this.getMode(stack);
         if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
            mode = ++mode % 8;
            this.setMode(stack, mode);
            playerIn.displayClientMessage(this.translate("tooltip.item.ic2.mining_laser.mode", new Object[]{this.getModeName(mode)}), false);
            return InteractionResultHolder.success(stack);
         } else {
            if (mode != 3 && mode != 7) {
               if (!ElectricItem.MANAGER.use(stack, MODE_COST[mode], playerIn)) {
                  return InteractionResultHolder.pass(stack);
               }

               if (mode == 5) {
                  boolean didShoot = false;

                  for (int x = -2; x <= 2; x++) {
                     for (int y = -2; y <= 2; y++) {
                        MiningLaserEntity laser = this.getMiningLaser(
                           IC2Entities.MINING_LASER,
                           mode,
                           worldIn,
                           playerIn,
                           (double)(playerIn.getYRot() + (float)(20 * x)),
                           (double)(playerIn.getXRot() + (float)(20 * y))
                        );
                        LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(
                           worldIn, laser, playerIn, laser.range, laser.power, laser.blockBreaks, laser.explosive, laser.smelt, stack
                        );
                        if (!MinecraftForge.EVENT_BUS.post(event)) {
                           worldIn.addFreshEntity(laser);
                           didShoot = true;
                        }
                     }
                  }

                  if (didShoot) {
                     IC2.AUDIO.playSound(playerIn, IC2Sounds.LASER_SCATTER, AudioManager.SoundType.ITEM);
                  }

                  return didShoot ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
               }

               MiningLaserEntity laser = this.getMiningLaser(IC2Entities.MINING_LASER, mode, worldIn, playerIn, 0.0, 0.0);
               if (laser != null) {
                  LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(
                     worldIn, laser, playerIn, laser.range, laser.power, laser.blockBreaks, laser.explosive, laser.smelt, stack
                  );
                  MinecraftForge.EVENT_BUS.post(event);
                  if (event.isCanceled()) {
                     return InteractionResultHolder.fail(stack);
                  }

                  worldIn.addFreshEntity(laser);
                  IC2.AUDIO.playSound(playerIn, this.getShootingSound(mode), AudioManager.SoundType.ITEM);
                  return InteractionResultHolder.success(stack);
               }
            } else if (mode == 7) {
               EntityHitResult result = rayTraceEntities(worldIn, playerIn, false, 60.0);
               if (result != null) {
                  Entity entity = result.getEntity();
                  if ((entity instanceof LivingEntity || entity instanceof EnderDragonPart || entity instanceof EndCrystal)
                     && ElectricItem.MANAGER.use(stack, MODE_COST[mode], playerIn)) {
                     MiningLaserEntity laser = new TrackingMiningLaserEntity(IC2Entities.TRACKING_MINING_LASER, worldIn, playerIn, 4.0F, 64.0F, 5, entity);
                     LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(
                        worldIn, laser, playerIn, laser.range, laser.power, laser.blockBreaks, laser.explosive, laser.smelt, stack
                     );
                     MinecraftForge.EVENT_BUS.post(event);
                     if (event.isCanceled() || !laser.canHitPlayer(result)) {
                        return InteractionResultHolder.fail(stack);
                     }

                     worldIn.addFreshEntity(laser);
                     IC2.AUDIO.playSound(playerIn, this.getShootingSound(mode), AudioManager.SoundType.ITEM);
                  }
               } else {
                  playerIn.displayClientMessage(this.translate("tooltip.item.ic2.mining_laser.error.no_target"), false);
               }

               return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.pass(stack);
         }
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null && !IC2.KEYBOARD.isModeSwitchKeyDown(player) && this.getMode(stack) == 3) {
         if (!(Math.abs(player.getY() + (double)player.getEyeHeight() - 0.1 - ((double)context.getClickedPos().getY() + 0.5)) < 1.5)) {
            player.displayClientMessage(this.translate("tooltip.item.ic2.mining_laser.error.steep"), false);
            return InteractionResult.FAIL;
         } else if (!ElectricItem.MANAGER.canUse(stack, 3000)) {
            return InteractionResult.PASS;
         } else if (IC2.PLATFORM.isRendering()) {
            return InteractionResult.SUCCESS;
         } else if (ElectricItem.MANAGER.use(stack, 3000, player)) {
            MiningLaserEntity laser = new MiningLaserEntity(
               IC2Entities.MINING_LASER,
               context.getLevel(),
               player,
               1.5F,
               5.0F,
               2147483647,
               false,
               (double)this.getRotationIndex((int)player.getYRot(), 8),
               0.0,
               (double)((float)context.getClickedPos().getY() + 0.5F)
            );
            LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(
               context.getLevel(), laser, player, laser.range, laser.power, laser.blockBreaks, laser.explosive, laser.smelt, stack
            );
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
               return InteractionResult.FAIL;
            } else {
               context.getLevel().addFreshEntity(laser);
               IC2.AUDIO.playSound(player, IC2Sounds.LASER_DEFAULT, AudioManager.SoundType.ITEM);
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }

   private int getRotationIndex(int playerRotation, int rotations) {
      int rotationPerIndex = 360 / rotations;
      int rotation = playerRotation % 360;
      if (rotation < 0) {
         rotation += 360;
      }

      int spot = rotation / rotationPerIndex;
      return (Math.abs(rotation - spot * rotationPerIndex) > Math.abs(rotation - (spot + 1) * rotationPerIndex) ? ++spot % rotations : spot) * rotationPerIndex;
   }

   public int getMode(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("mode");
   }

   public void setMode(ItemStack stack, int mode) {
      stack.getOrCreateTag().putInt("mode", mode);
   }

   @Override
   public boolean canMineBlock(ItemStack drill, BlockState state, LevelReader access, BlockPos pos) {
      return true;
   }

   @Override
   public int getMiningBoost(ItemStack stack, BlockState state) {
      return 100;
   }

   @Override
   public int getExtraEnergyCost(ItemStack stack) {
      return 80;
   }

   @Override
   public boolean canDrillBeUsed(ItemStack stack) {
      return ElectricItem.MANAGER.canUse(stack, 250);
   }

   @Override
   public void onDrillUsed(ItemStack stack) {
      ElectricItem.MANAGER.discharge(stack, 250, 2, true, false, false);
   }

   public ResourceLocation getShootingSound(int mode) {
      switch (mode) {
         case 1:
            return IC2Sounds.LASER_LOW_FOCUS;
         case 3:
            return IC2Sounds.LASER_LONG_RANGE;
         case 6:
            return IC2Sounds.LASER_EXPLOSIVE;
         default:
            return IC2Sounds.LASER_DEFAULT;
      }
   }

   public MiningLaserEntity getMiningLaser(EntityType<MiningLaserEntity> type, int mode, Level world, Player player, double yaw, double pitch) {
      switch (mode) {
         case 0:
            return new MiningLaserEntity(type, world, player, 2.0F, 5.0F, 2147483647, false);
         case 1:
            return new MiningLaserEntity(type, world, player, 1.1F, 5.0F, 1, false);
         case 2:
            return new MiningLaserEntity(type, world, player, 2.0F, 32.0F, 2147483647, false);
         case 3:
         default:
            return null;
         case 4:
            return new MiningLaserEntity(type, world, player, 2.0F, 8.0F, 2147483647, false, true);
         case 5:
            return new MiningLaserEntity(type, world, player, 2.0F, 12.0F, 2147483647, false, yaw, pitch);
         case 6:
            return new MiningLaserEntity(type, world, player, 2.0F, 12.0F, 2147483647, true);
      }
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.UNBREAKING
         ? InteractionResult.SUCCESS
         : (enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }
}
