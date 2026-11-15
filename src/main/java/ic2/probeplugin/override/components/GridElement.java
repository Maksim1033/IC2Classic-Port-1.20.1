package ic2.probeplugin.override.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap.BasicEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IItemStyle;
import mcjty.theoneprobe.apiimpl.client.ElementItemStackRender;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GridElement implements IElement {
   public static final ResourceLocation ELEMENT = new ResourceLocation("ic2", "gridelement");
   int gridSlots;
   int pixelPerSlot;
   int textPadding = 0;
   List<Entry<Component, List<ItemStack>>> display = new ObjectArrayList();
   int[] boundsCache = null;

   public GridElement() {
      this(6, 18);
   }

   public GridElement(Map<Component, List<ItemStack>> rows) {
      this(6, 18);
      this.addRows(rows);
   }

   public GridElement(int gridSlots, int pixelPerSlot) {
      this.gridSlots = gridSlots;
      this.pixelPerSlot = pixelPerSlot;
   }

   public GridElement setTextPadding(int text) {
      this.textPadding = text;
      return this;
   }

   public GridElement addRow(Entry<Component, List<ItemStack>> entry) {
      this.display.add(entry);
      return this;
   }

   public GridElement addRow(Component name, List<ItemStack> items) {
      this.display.add(new BasicEntry(name, items));
      return this;
   }

   public GridElement addRows(Map<Component, List<ItemStack>> rows) {
      this.display.addAll(rows.entrySet());
      return this;
   }

   public GridElement(FriendlyByteBuf buffer) {
      this.gridSlots = buffer.readVarInt();
      this.pixelPerSlot = buffer.readVarInt();
      this.textPadding = buffer.readVarInt();
      int i = 0;

      for (int m = buffer.readVarInt(); i < m; i++) {
         Component name = buffer.readComponent();
         int size = buffer.readVarInt();
         NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);

         for (int j = 0; j < size; j++) {
            ItemStack stack = buffer.readItem();
            stack.setCount(buffer.readVarInt());
            items.set(j, stack);
         }

         this.display.add(new BasicEntry(name, items));
      }
   }

   public void toBytes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.gridSlots);
      buffer.writeVarInt(this.pixelPerSlot);
      buffer.writeVarInt(this.textPadding);
      buffer.writeVarInt(this.display.size());
      int i = 0;

      for (int m = this.display.size(); i < m; i++) {
         Entry<Component, List<ItemStack>> entry = this.display.get(i);
         buffer.writeComponent(entry.getKey());
         List<ItemStack> items = entry.getValue();
         buffer.writeVarInt(items.size());
         int j = 0;

         for (int n = items.size(); j < n; j++) {
            ItemStack stack = items.get(j);
            buffer.writeItem(stack);
            buffer.writeVarInt(stack.getCount());
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public int calculateGridPadding() {
      Font font = Minecraft.getInstance().font;
      int padding = 0;
      int i = 0;

      for (int m = this.display.size(); i < m; i++) {
         Entry<Component, List<ItemStack>> entry = this.display.get(i);
         int textWidth = font.width(entry.getKey().getVisualOrderText()) + this.textPadding;
         double textSlots = (double)Mth.floor((double)textWidth / (double)this.pixelPerSlot);
         if (!(textSlots + (double)entry.getValue().size() >= (double)this.gridSlots)) {
            int left = (int)(textSlots * (double)this.pixelPerSlot - (double)textWidth) + padding;
            if (left < 0) {
               padding -= left;
            }
         }
      }

      return padding;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(GuiGraphics matrix, int x, int y) {
      IItemStyle style = new ItemStyle().bounds(this.pixelPerSlot, this.pixelPerSlot);
      Minecraft mc = Minecraft.getInstance();
      Font font = mc.font;
      int yOffset = 0;
      int gridPadding = this.calculateGridPadding();
      int i = 0;

      for (int m = this.display.size(); i < m; i++) {
         Entry<Component, List<ItemStack>> entry = this.display.get(i);
         int textSlots = Mth.floor(
            (double)(RenderHelper.renderText(mc, matrix, x, y + yOffset + 5, entry.getKey()) + this.textPadding) / (double)this.pixelPerSlot
         );
         List<ItemStack> items = entry.getValue();
         if (textSlots + items.size() <= this.gridSlots) {
            int j = 0;

            for (int n = items.size(); j < n; j++) {
               ElementItemStackRender.render(items.get(j), style, matrix, x + (textSlots + j) * this.pixelPerSlot + gridPadding + 1, y + yOffset + 1);
            }

            yOffset += this.pixelPerSlot;
         } else {
            yOffset += 9;
            int j = 0;

            for (int n = items.size(); j < n; j++) {
               ElementItemStackRender.render(
                  items.get(j),
                  style,
                  matrix,
                  x + j % this.gridSlots * this.pixelPerSlot + gridPadding + 1,
                  y + yOffset + j / this.gridSlots * this.pixelPerSlot + 6
               );
            }

            yOffset += (int)((float)(Mth.ceil((double)items.size() / (double)this.gridSlots) * this.pixelPerSlot) + (float)this.pixelPerSlot * 0.5F);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void boundsCache() {
      this.boundsCache = new int[2];
      Font font = Minecraft.getInstance().font;
      int gridPadding = this.calculateGridPadding();
      int i = 0;

      for (int m = this.display.size(); i < m; i++) {
         Entry<Component, List<ItemStack>> entry = this.display.get(i);
         int items = entry.getValue().size();
         int textWidth = font.width(entry.getKey().getVisualOrderText()) + this.textPadding;
         int textSlots = Mth.floor((double)textWidth / (double)this.pixelPerSlot);
         if (items + textSlots <= this.gridSlots) {
            this.boundsCache[0] = Math.max(this.boundsCache[0], this.pixelPerSlot * (items + textSlots) + gridPadding);
            this.boundsCache[1] = this.boundsCache[1] + this.pixelPerSlot;
         } else if (items > this.gridSlots) {
            this.boundsCache[0] = Math.max(this.boundsCache[0], this.pixelPerSlot * this.gridSlots + gridPadding);
            this.boundsCache[1] = this.boundsCache[1] + 9 + Mth.ceil((double)items / (double)this.gridSlots) * this.pixelPerSlot + this.pixelPerSlot / 2 + 2;
         } else {
            this.boundsCache[0] = Math.max(this.boundsCache[0], Math.max(textWidth, items * this.pixelPerSlot + gridPadding));
            this.boundsCache[1] = this.boundsCache[1] + 9 + this.pixelPerSlot + 5;
         }
      }
   }

   public int getHeight() {
      if (this.boundsCache == null) {
         this.boundsCache();
      }

      return this.boundsCache[1];
   }

   public int getWidth() {
      if (this.boundsCache == null) {
         this.boundsCache();
      }

      return this.boundsCache[0];
   }

   public ResourceLocation getID() {
      return ELEMENT;
   }
}
