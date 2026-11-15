package ic2.api.tiles.display.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StringDisplayInfo implements IDisplayInfo {
   Component comp;
   Supplier<Component> textProvider;
   BooleanSupplier aliveProvider;

   public StringDisplayInfo(Supplier<Component> textProvider, BooleanSupplier aliveProvider) {
      this.textProvider = textProvider;
      this.aliveProvider = aliveProvider;
   }

   public StringDisplayInfo(FriendlyByteBuf buffer) {
      this.comp = buffer.readComponent();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      Font font = helper.getFont();
      int heightConsumed = 0;

      for (FormattedCharSequence sub : font.split(this.comp, width)) {
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

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getHeight(int width, IDisplayInfo.Alignment align) {
      Font font = Minecraft.getInstance().font;
      return font.getSplitter().splitLines(this.comp, width, Style.EMPTY).size() * 9;
   }

   @Override
   public boolean isValid() {
      return this.aliveProvider.getAsBoolean();
   }

   @Override
   public Tag getServerData() {
      return StringTag.valueOf(this.textProvider.get().getString());
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeComponent(this.textProvider.get());
   }
}
