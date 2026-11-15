package ic2.core.item.base;

import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import ic2.core.utils.tooltips.helper.ITooltipProvider;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2Item extends Item implements ILangHelper, IAdvancedTooltip, IRegistryProvider, IItemCategoryFiller {
   static final Predicate<Entity> PREDICATE = EntitySelector.NO_SPECTATORS
      .and(T -> T != null && T.isPickable() && (T instanceof LivingEntity || T instanceof EnderDragonPart || T instanceof EndCrystal));
   public static final boolean SHOULD_STATUS = false;
   public static final RandomSource RANDOM = RandomSource.create();
   static final Consumer<LivingEntity>[] HAND_BASE = create();
   static final Consumer<LivingEntity>[] SLOT_BASE = createSlots();
   static final Consumer<LivingEntity> EMPTY_BASE = T -> {
   };
   protected ResourceLocation id;
   Boolean foiled;
   List<ITooltipProvider> providers = null;

   public IC2Item(String itemName, @Nullable PropertiesBuilder properties) {
      super((properties == null ? new PropertiesBuilder() : properties).group(IC2.IC2_MAIN_GROUP).build());
      this.id = GameData.checkPrefix(itemName.toLowerCase(), false);
      this.foiled = properties == null ? null : properties.isFoiled();
   }

   public IC2Item(String itemName) {
      this(itemName, null);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public static Consumer<LivingEntity> empty() {
      return EMPTY_BASE;
   }

   public static Consumer<LivingEntity> get(InteractionHand hand) {
      return hand == InteractionHand.OFF_HAND ? HAND_BASE[1] : HAND_BASE[0];
   }

   public static Consumer<LivingEntity> get(EquipmentSlot slot) {
      return slot == null ? SLOT_BASE[0] : SLOT_BASE[slot.getIndex()];
   }

   static Consumer<LivingEntity>[] create() {
      Consumer<LivingEntity>[] result = new Consumer[2];
      result[0] = entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      result[1] = entity -> entity.broadcastBreakEvent(EquipmentSlot.OFFHAND);
      return result;
   }

   static Consumer<LivingEntity>[] createSlots() {
      Consumer<LivingEntity>[] slots = new Consumer[EquipmentSlot.values().length];

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         slots[slot.getIndex()] = entity -> entity.broadcastBreakEvent(slot);
      }

      return slots;
   }

   public IC2Item addTooltip(ITooltipProvider provider) {
      if (provider == null) {
         Thread.dumpStack();
         return this;
      } else {
         if (this.providers == null) {
            this.providers = CollectionUtils.<ITooltipProvider>createList();
         }

         this.providers.add(provider);
         return this;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      if (this.providers != null) {
         Level world = Minecraft.getInstance().level;
         int i = 0;

         for (int m = this.providers.size(); i < m; i++) {
            this.providers.get(i).addInformation(stack, world, helper.getTooltips(), type);
         }
      }
   }

   public boolean isFoil(ItemStack p_77636_1_) {
      return this.foiled != null ? this.foiled : super.isFoil(p_77636_1_);
   }

   public void setDamage(ItemStack stack, int damage) {
      if (damage <= 0) {
         stack.removeTagKey("Damage");
      } else {
         stack.getOrCreateTag().putInt("Damage", damage);
      }
   }

   public static HitResult rayTrace(Level worldIn, LivingEntity playerIn, boolean useLiquids, double maxDistance) {
      float f = playerIn.getXRot();
      float f1 = playerIn.getYRot();
      double d0 = playerIn.getX();
      double d1 = playerIn.getY() + (double)playerIn.getEyeHeight();
      double d2 = playerIn.getZ();
      Vec3 Vector3d = new Vec3(d0, d1, d2);
      float f2 = Mth.cos(-f1 * 0.017453292F - 3.1415927F);
      float f3 = Mth.sin(-f1 * 0.017453292F - 3.1415927F);
      float f4 = -Mth.cos(-f * 0.017453292F);
      float f5 = Mth.sin(-f * 0.017453292F);
      float f6 = f3 * f4;
      float f7 = f2 * f4;
      Vec3 vec3d1 = Vector3d.add((double)f6 * maxDistance, (double)f5 * maxDistance, (double)f7 * maxDistance);
      return worldIn.clip(new ClipContext(Vector3d, vec3d1, Block.COLLIDER, useLiquids ? Fluid.SOURCE_ONLY : Fluid.NONE, playerIn));
   }

   public static EntityHitResult rayTraceEntities(Level worldIn, LivingEntity playerIn, boolean useLiquids, double maxDistance) {
      return rayTraceEntities(worldIn, playerIn, maxDistance);
   }

   public static EntityHitResult rayTraceEntities(Level worldIn, LivingEntity playerIn, double maxDistance) {
      Vec3 angle = playerIn.getLookAngle();
      Vec3 start = playerIn.getEyePosition(1.0F);
      Vec3 end = start.add(angle.x * maxDistance, angle.y * maxDistance, angle.z * maxDistance);
      Entity pointedEntity = null;
      Vec3 resultVec = null;
      double closestEntity = maxDistance;
      List<Entity> list = worldIn.getEntities(
         playerIn, playerIn.getBoundingBox().expandTowards(angle.x * maxDistance, angle.y * maxDistance, angle.z * maxDistance).inflate(1.0), PREDICATE
      );

      for (int j = 0; j < list.size(); j++) {
         Entity entity = list.get(j);
         Optional<Vec3> result = entity.getBoundingBox().inflate((double)entity.getPickRadius()).clip(start, end);
         if (result.isPresent()) {
            double distance = start.distanceTo(result.get());
            if (distance < closestEntity && (entity.getRootVehicle() != playerIn.getRootVehicle() || playerIn.canRiderInteract())) {
               pointedEntity = entity;
               resultVec = result.get();
               closestEntity = distance;
            }
         }
      }

      return pointedEntity != null ? new EntityHitResult(pointedEntity, resultVec) : null;
   }
}
