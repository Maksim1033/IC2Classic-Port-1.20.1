package ic2.core.item.tool.infos.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import ic2.core.utils.math.ColorUtils;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimeDisplayInfo implements IDisplayInfo {
   int color;
   boolean cut;

   public TimeDisplayInfo(int color, boolean cut) {
      this.color = color;
      this.cut = cut;
   }

   public TimeDisplayInfo(FriendlyByteBuf buffer) {
      this.color = buffer.readInt();
      this.cut = buffer.readBoolean();
   }

   @Override
   public Tag getServerData() {
      return LongTag.valueOf((this.cut ? 1L : 0L) << 33 | (long)this.color);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      Font font = helper.getFont();
      int heightConsumed = 0;

      for (FormattedCharSequence sub : font.split(this.getTime(), width)) {
         if (height - heightConsumed < 9) {
            return;
         }

         font.drawInBatch(
            sub,
            (float)(x - align.getXOffset(font.width(sub))),
            (float)(y + heightConsumed),
            -1,
            false,
            stack.last().pose(),
            helper.getBatcher(),
            Font.DisplayMode.NORMAL,
            0,
            15728880
         );
         heightConsumed += 9;
      }
   }

   private Component getTime() {
      return this.cut
         ? Component.literal(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))).withStyle(ColorUtils.toColor(this.color))
         : Component.translatable("tooltip.item.ic2.time_card.real", new Object[]{LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))})
            .withStyle(ColorUtils.toColor(this.color));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getHeight(int width, IDisplayInfo.Alignment align) {
      Font font = Minecraft.getInstance().font;
      return font.getSplitter().splitLines(this.getTime(), width, Style.EMPTY).size() * 9;
   }

   @Override
   public boolean isValid() {
      return true;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeInt(this.color);
      buffer.writeBoolean(this.cut);
   }
}
