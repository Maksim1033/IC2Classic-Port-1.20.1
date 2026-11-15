package ic2.core.platform.corehacks;

import ic2.api.items.armor.ICustomArmor;
import ic2.core.item.base.IC2Item;
import ic2.core.item.wearable.base.IC2ArmorBase;
import ic2.core.platform.events.InternalEventHandler;
import ic2.core.platform.rendering.events.TilesRenderedEvent;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

public final class ASMHacks {
   public static float applyArmor(Player e, DamageSource source, float damage) {
      return IC2ArmorBase.applyArmor(e, source, (double)damage);
   }

   public static void thornsSelfArmorDamage(Entry<EquipmentSlot, ItemStack> entry, int amount, LivingEntity entity) {
      if (entry.getKey() != null && entry.getValue().getItem() instanceof ICustomArmor armor) {
         armor.damageArmor(entity, entry.getValue(), entity.damageSources().thorns(entity), amount, entry.getKey(), ICustomArmor.DamageType.THORNS_SELF);
      } else {
         entry.getValue().hurtAndBreak(2, entity, IC2Item.get(entry.getKey()));
      }
   }

   public static BlockState applyCamouflage(
      BlockState ownerState, BlockGetter world, BlockPos position, Direction side, BlockPos offsetPos, BlockState neighborState
   ) {
      return InternalEventHandler.applyCamouflage(ownerState, world, position, side, offsetPos, neighborState);
   }

   public static List<MobEffectInstance> getFixedPotionEffectsFromStack(ItemStack itemIn, float durationFactor) {
      List<MobEffectInstance> results = CollectionUtils.<MobEffectInstance>createList();
      CompoundTag nbt = StackUtil.getNbtData(itemIn);
      results.addAll(PotionUtils.getPotion(nbt).getEffects());
      ListTag list = nbt.getList("CustomPotionEffects", 10);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         CompoundTag data = list.getCompound(i);
         if (data.contains("Duration")) {
            data = data.copy();
            data.putInt("Duration", (int)((float)data.getInt("Duration") / durationFactor));
         }

         MobEffectInstance instance = MobEffectInstance.load(data);
         if (instance != null) {
            results.add(instance);
         }
      }

      return results;
   }

   @OnlyIn(Dist.CLIENT)
   public static void onTileRenderFinished(BufferSource renderTypeBuffer, boolean translucencyMode) {
      MinecraftForge.EVENT_BUS.post(new TilesRenderedEvent(renderTypeBuffer::endBatch, translucencyMode));
   }

   public static float getStepHeight(Player player) {
      return player.isShiftKeyDown() ? 0.6F : player.getStepHeight();
   }
}
