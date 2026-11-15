package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.api.network.buffer.EmptyDataBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.core.IC2;
import ic2.core.block.rendering.world.impl.SonarOverlay;
import ic2.core.item.base.features.ISonarProvider;
import ic2.core.item.wearable.base.IArmorToolTip;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SonarModuleItem extends BaseModuleItem implements ISonarProvider, IArmorToolTip {
   public static final String DATA = "sonar";
   public static final int MAX_RADIUS = 250;
   public static final int SMALL_SCAN_RADIUS = 25;

   public SonarModuleItem(String textureFolder, String textureName) {
      super("sonar_module", null, textureFolder, textureName, IArmorModule.ModuleType.HUD);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
      helper.addKeybindingTooltip(
         this.buildKeyDescription(
            new KeyHelper[]{KeyHelper.SNEAK_KEY, KeyHelper.ALT_KEY, KeyHelper.HUD_KEY}, "tooltip.item.ic2.sonar_module.toggle", new Object[0]
         )
      );
      helper.addKeybindingTooltip(
         this.buildKeyDescription(new KeyHelper[]{KeyHelper.ALT_KEY, KeyHelper.HUD_KEY}, "tooltip.item.ic2.sonar_module.long_scan", new Object[0])
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack armor, ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(
         this.buildKeyDescription(
            new KeyHelper[]{KeyHelper.SNEAK_KEY, KeyHelper.ALT_KEY, KeyHelper.HUD_KEY}, "tooltip.item.ic2.sonar_module.toggle", new Object[0]
         )
      );
      helper.addKeybindingTooltip(
         this.buildKeyDescription(new KeyHelper[]{KeyHelper.ALT_KEY, KeyHelper.HUD_KEY}, "tooltip.item.ic2.sonar_module.long_scan", new Object[0])
      );
      helper.addSimpleToolTip(
         StackUtil.getNbtData(armor).getCompound("sonar").getBoolean("scan")
            ? "tooltip.item.ic2.sonar_module.enabled"
            : "tooltip.item.ic2.sonar_module.disabled"
      );
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      PlayerHandler handler = PlayerHandler.getHandler(player);
      CompoundTag data = armor.getOrCreateTagElement("sonar");
      if (handler.hudModeKeyDown && handler.altKeyDown) {
         if (player.isShiftKeyDown() && world.getGameTime() >= data.getLong("last_toggle")) {
            data.putLong("last_toggle", world.getGameTime() + 20L);
            boolean result = !data.getBoolean("scan");
            data.putBoolean("scan", result);
            if (IC2.PLATFORM.isSimulating()) {
               player.displayClientMessage(this.translate("tooltip.item.ic2.sonar_module." + (result ? "enable" : "disable")), false);
            }
         } else if (!player.isShiftKeyDown() && !data.contains("positions") && this.canUseEnergy(armor, 20000)) {
            this.useEnergy(armor, 20000, player);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(250.0));
            int[] ids = new int[entities.size()];
            int i = 0;

            for (int m = entities.size(); i < m; i++) {
               ids[i] = entities.get(i).getId();
            }

            data.putIntArray("positions", ids);
            data.putLong("pos", player.blockPosition().asLong());
            if (IC2.PLATFORM.isSimulating()) {
               IC2.NETWORKING.get(true).sendItemBuffer(player, armor, "sonar", EmptyDataBuffer.INSTANCE);
            }
         }
      }

      if (data.getBoolean("scan") && world.getGameTime() % 20L == 0L) {
         for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(25.0))) {
            if (entity != player) {
               entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
            }
         }
      }

      if (data.contains("positions")) {
         double progress = data.getDouble("progress");
         AABB box = new AABB(BlockPos.of(data.getLong("pos"))).inflate(250.0 * progress);

         for (int id : data.getIntArray("positions")) {
            Entity entityx = world.getEntity(id);
            if (entityx instanceof LivingEntity && entityx != player && box.intersects(entityx.getBoundingBox())) {
               ((LivingEntity)entityx).addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            }
         }

         if (progress >= 1.0) {
            data.remove("positions");
            data.remove("progress");
            return;
         }

         data.putDouble("progress", progress + 0.005);
      }
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      if (id.equals("sonar")) {
         EquipmentSlot find = this.findPlayerSlot(player, armor);
         if (find == null) {
            return true;
         } else {
            player.getItemBySlot(find).setTag(armor.getTag());
            SonarOverlay.INSTANCE.addSonar(this, () -> player.getItemBySlot(find));
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      armor.removeTagKey("sonar");
   }

   @Override
   public int getViewRadius(ItemStack stack) {
      return (int)(255.0 * StackUtil.getNbtData(stack).getCompound("sonar").getDouble("progress"));
   }

   @Override
   public BlockPos getStartPosition(ItemStack stack) {
      return BlockPos.of(StackUtil.getNbtData(stack).getCompound("sonar").getLong("pos"));
   }

   @Override
   public boolean isDone(ItemStack stack) {
      return !StackUtil.getNbtData(stack).getCompound("sonar").contains("positions");
   }

   @Override
   public int getSonarColor(ItemStack stack) {
      return 2041227946;
   }
}
