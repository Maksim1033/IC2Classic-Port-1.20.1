package ic2.core.item.wearable.modules;

import ic2.api.items.IAutoEatable;
import ic2.api.items.armor.IArmorModule;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AutoFeedModuleItem extends BaseModuleItem {
   static final List<BiFunction<Player, ItemStack, IItemTransporter>> TRANSPORTERS = CollectionUtils.<BiFunction<Player, ItemStack, IItemTransporter>>createList();

   public static synchronized void addFoodSource(BiFunction<Player, ItemStack, IItemTransporter> provider) {
      TRANSPORTERS.add(provider);
   }

   public AutoFeedModuleItem(String textureFolder, String textureName) {
      super("auto_feeder_module", null, textureFolder, textureName, IArmorModule.ModuleType.GENERIC);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      if (player.getFoodData().needsFood() && this.canUseEnergy(armor, 1000)) {
         int i = 0;

         for (int m = TRANSPORTERS.size(); i < m; i++) {
            IItemTransporter transporter = TRANSPORTERS.get(i).apply(player, armor);
            if (transporter != null) {
               ItemStack food = transporter.removeItem(SpecialFilters.AUTO_EATABLE, null, 1, false);
               if (!food.isEmpty()) {
                  food = ((IAutoEatable)food.getItem()).onEaten(food, world, player);
                  if (!food.isEmpty()) {
                     food.shrink(transporter.addItem(food, null, false));
                     StackUtil.addOrDrop(player, food);
                     this.useEnergy(armor, 1000, player);
                     return;
                  }
               }
            }
         }

         IItemTransporter transporter = TransporterManager.getTransporter(player);
         if (transporter != null) {
            ItemStack food = transporter.removeItem(SpecialFilters.AUTO_EATABLE, Direction.DOWN, 1, false);
            if (!food.isEmpty()) {
               food = ((IAutoEatable)food.getItem()).onEaten(food, world, player);
               StackUtil.addOrDrop(player, food);
               this.useEnergy(armor, 1000, player);
            }
         }
      } else if (player.getFoodData().getFoodLevel() <= 0) {
         IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:starve"));
      }
   }
}
