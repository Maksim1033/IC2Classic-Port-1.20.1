package ic2.core.platform.events;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.misc.GLUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DebugHighlighter {
   public static final DebugHighlighter INSTANCE = new DebugHighlighter();
   List<Component> toRender = new ObjectArrayList();

   public void storeDebug(List<Component> s, CompoundTag nbt) {
      this.toRender.clear();
      this.toRender.addAll(s);
   }

   @OnlyIn(Dist.CLIENT)
   public void onToolTip(PoseStack pose) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player != null) {
         boolean found = false;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == IC2Items.DEBUG_ITEM && StackUtil.getNbtData(stack).getBoolean("active")) {
               found = true;
               break;
            }
         }

         if (found) {
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 1.0F);
            GLUtils.drawTooltip(pose, this.toRender, -5, 0);
            pose.popPose();
         }
      }
   }
}
