package ic2.core.item.tool.infos.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import ic2.core.utils.tooltips.ILangHelper;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.time.Duration;
import java.util.Iterator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ViewerLogInfo implements IDisplayInfo, ILangHelper {
   Supplier<Object2LongMap<String>> dataProvider;
   BooleanSupplier aliveProvider;
   Object2LongMap<String> data;
   int color;

   public ViewerLogInfo(Supplier<Object2LongMap<String>> dataProvider, BooleanSupplier aliveProvider, int color) {
      this.dataProvider = dataProvider;
      this.aliveProvider = aliveProvider;
      this.color = color;
   }

   public ViewerLogInfo(FriendlyByteBuf buffer) {
      this.data = new Object2LongOpenHashMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         this.data.put(buffer.readUtf(), buffer.readLong());
      }

      this.color = buffer.readInt();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      Font font = helper.getFont();
      int heightConsumed = 0;

      for (FormattedCharSequence sub : font.split(this.buildText(), width)) {
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
      return font.getSplitter().splitLines(this.buildText(), width, Style.EMPTY).size() * 9;
   }

   private Component buildText() {
      long time = Minecraft.getInstance().level.getGameTime();
      MutableComponent comp = this.string();
      Iterator<Entry<String>> iter = Object2LongMaps.fastIterator(this.data);

      while (iter.hasNext()) {
         Entry<String> entry = iter.next();
         comp.append((String)entry.getKey()).append(": ").append(this.getDuration(Duration.ofSeconds((time - entry.getLongValue()) / 20L)));
         if (iter.hasNext()) {
            comp.append("\n");
         }
      }

      return comp;
   }

   private Component getDuration(Duration duration) {
      if (duration.toHours() > 1L) {
         return this.translate("gui.ic2.cards.ago.hours", new Object[]{duration.toHours()});
      } else {
         return duration.toMinutes() > 1L
            ? this.translate("gui.ic2.cards.ago.minutes", new Object[]{duration.toMinutes()})
            : this.translate("gui.ic2.cards.ago.seconds", new Object[]{duration.getSeconds()});
      }
   }

   @Override
   public boolean isValid() {
      return this.aliveProvider.getAsBoolean();
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      Object2LongMap<String> data = this.dataProvider.get();
      buffer.writeVarInt(data.size());
      ObjectIterator var3 = Object2LongMaps.fastIterable(data).iterator();

      while (var3.hasNext()) {
         Entry<String> entry = (Entry<String>)var3.next();
         buffer.writeUtf((String)entry.getKey());
         buffer.writeLong(entry.getLongValue());
      }

      buffer.writeInt(this.color);
   }

   @Override
   public Tag getServerData() {
      ListTag list = new ListTag();
      Object2LongMap<String> data = this.dataProvider.get();
      ObjectIterator var3 = Object2LongMaps.fastIterable(data).iterator();

      while (var3.hasNext()) {
         Entry<String> entry = (Entry<String>)var3.next();
         CompoundTag nbt = new CompoundTag();
         nbt.putString("user", (String)entry.getKey());
         nbt.putLong("lastView", entry.getLongValue());
         list.add(nbt);
      }

      return list;
   }
}
