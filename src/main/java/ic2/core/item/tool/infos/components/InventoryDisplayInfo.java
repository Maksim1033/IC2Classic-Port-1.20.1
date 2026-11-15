package ic2.core.item.tool.infos.components;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InventoryDisplayInfo implements IDisplayInfo {
   Supplier<IHasInventory> inventoryProvider;
   Supplier<InventoryDisplayInfo.InvSettings> settingsProvider;
   BooleanSupplier aliveProvider;
   List<InventoryDisplayInfo.RenderObject> inv;
   InventoryDisplayInfo.InvSettings settings;

   public InventoryDisplayInfo(
      Supplier<IHasInventory> inventoryProvider, Supplier<InventoryDisplayInfo.InvSettings> settingsProvider, BooleanSupplier aliveProvider
   ) {
      this.inventoryProvider = inventoryProvider;
      this.settingsProvider = settingsProvider;
      this.aliveProvider = aliveProvider;
   }

   public InventoryDisplayInfo(FriendlyByteBuf buffer) {
      this.inv = new ObjectArrayList();
      this.settings = new InventoryDisplayInfo.InvSettings(buffer);
      int count = buffer.readVarInt();

      for (int i = 0; i < count; i++) {
         ItemStack stack = buffer.readItem();
         stack.setCount(buffer.readVarInt());
         this.inv.add(new InventoryDisplayInfo.RenderObject(stack));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      stack.pushPose();
      stack.last().normal().scale(1.0F, -1.0F, 1.0F);
      float xOff = 0.0F;
      float yOff = 0.0F;
      float objHeight = 0.0F;
      Font font = helper.getFont();
      if (align == IDisplayInfo.Alignment.CENTER) {
         int i = 0;

         for (int m = this.inv.size(); i < m; i++) {
            this.inv.get(i).computeBounds(font, this.settings);
         }

         i = 0;

         for (int m = this.inv.size(); i < m; i++) {
            InventoryDisplayInfo.RenderObject obj = this.inv.get(i);
            objHeight = Math.max(objHeight, obj.height);
            if (obj.width + xOff > (float)width) {
               xOff = 0.0F;
               yOff += objHeight;
               objHeight = 0.0F;
               if (yOff >= (float)height) {
                  break;
               }
            }

            helper.renderGuiItems(stack, obj.item, xOff + obj.itemOff, (float)y + yOff);
            if (this.settings.showCount) {
               stack.pushPose();
               helper.renderGuiItemText(stack, obj.item, 0.0F, 0.0F, this.calculateTextScale(stack, xOff + obj.itemOff, (float)y + yOff, obj.text));
               stack.popPose();
            }

            if (this.settings.showCountSide || this.settings.showName) {
               stack.pushPose();
               stack.scale(0.75F, 0.75F, 0.75F);
               stack.translate((double)((xOff + obj.width * 0.5F) * 1.34F), (double)((float)y + yOff * 1.34F + 4.0F), 0.0);
               float base = 20.0F;
               if (this.settings.showCountSide) {
                  font.drawInBatch(obj.count, (float)(-font.width(obj.count)) * 0.5F, base, -1, false, stack.last().pose(), helper.getBatcher(), Font.DisplayMode.NORMAL, 0, 15728880);
                  base += 10.0F;
               }

               if (this.settings.showName) {
                  font.drawInBatch(obj.name, (float)(-font.width(obj.name)) * 0.5F, base, -1, false, stack.last().pose(), helper.getBatcher(), Font.DisplayMode.NORMAL, 0, 15728880);
               }

               stack.popPose();
            }

            xOff += obj.width;
         }
      } else {
         int count = height / 18;
         boolean inverted = align == IDisplayInfo.Alignment.RIGHT;
         int i = 0;

         for (int m = this.inv.size(); i < count && i < m; i++) {
            InventoryDisplayInfo.RenderObject objx = this.inv.get(i);
            int realY = y + i * 18;
            helper.renderGuiItems(stack, objx.item, (float)(x - (inverted ? 18 : 0)), (float)realY);
            if (this.settings.showCount) {
               stack.pushPose();
               helper.renderGuiItemText(stack, objx.item, 0.0F, 0.0F, this.calculateTextScale(stack, (float)(x - (inverted ? 18 : 0)), (float)realY, objx.text));
               stack.popPose();
            }

            if (this.settings.showCountSide || this.settings.showName) {
               stack.pushPose();
               stack.scale(0.75F, 0.75F, 0.75F);
               stack.translate(inverted ? (double)((float)(x - 21) * 1.34F) : (double)((float)x + 26.800001F), (double)((float)realY * 1.34F + 2.0F), 0.0);
               float basex = 0.0F;
               if (this.settings.showName) {
                  font.drawInBatch(objx.name, inverted ? (float)(-font.width(objx.name)) : 0.0F, basex, -1, false, stack.last().pose(), helper.getBatcher(), Font.DisplayMode.NORMAL, 0, 15728880);
                  basex += 10.0F;
               }

               if (this.settings.showCountSide) {
                  font.drawInBatch(objx.count, inverted ? (float)(-font.width(objx.count)) : 0.0F, basex, -1, false, stack.last().pose(), helper.getBatcher(), Font.DisplayMode.NORMAL, 0, 15728880);
               }

               stack.popPose();
            }
         }
      }

      stack.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private String calculateTextScale(PoseStack matrix, float xOff, float zOff, String s) {
      int length = s.length() - 2;
      if (length >= 2) {
         matrix.scale(0.5F, 0.5F, 0.0F);
         matrix.translate((double)(xOff * 2.0F + 14.0F), (double)(zOff * 2.0F + 13.0F), 0.0);
      } else if (length == 0) {
         matrix.scale(0.75F, 0.75F, 0.75F);
         matrix.translate((double)(xOff * 1.34F + 3.0F), (double)(zOff * 1.34F + 4.0F), 0.0);
      } else {
         matrix.translate((double)xOff, (double)zOff, 0.0);
      }

      return s;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getHeight(int width, IDisplayInfo.Alignment align) {
      if (align == IDisplayInfo.Alignment.CENTER) {
         float xOff = 0.0F;
         float yOff = 0.0F;
         float objHeight = 0.0F;
         int i = 0;

         for (int m = this.inv.size(); i < m; i++) {
            InventoryDisplayInfo.RenderObject obj = this.inv.get(i);
            objHeight = Math.max(objHeight, obj.height);
            if (obj.width + xOff > (float)width) {
               xOff = 0.0F;
               yOff += objHeight;
               objHeight = 0.0F;
            }
         }

         return (int)yOff;
      } else {
         return this.inv.size() * 18;
      }
   }

   @Override
   public boolean isValid() {
      return this.aliveProvider.getAsBoolean();
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      IHasInventory inventory = this.inventoryProvider.get();
      InventoryDisplayInfo.InvSettings settings = this.settingsProvider.get();
      List<ItemStack> list = new ObjectArrayList();
      if (settings.mergeInv) {
         Object2IntLinkedOpenHashMap<Item> map = new Object2IntLinkedOpenHashMap();
         Map<Item, ItemStack> refMap = CollectionUtils.<Item, ItemStack>createMap();
         int i = 0;

         for (int m = inventory.getSlotCount(); i < m; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               map.addTo(stack.getItem(), stack.getCount());
               refMap.putIfAbsent(stack.getItem(), stack);
            }
         }

         ObjectIterator var14 = Object2IntMaps.fastIterable(map).iterator();

         while (var14.hasNext()) {
            Entry<Item> entry = (Entry<Item>)var14.next();
            list.add(StackUtil.copyWithSize(refMap.get(entry.getKey()), entry.getIntValue()));
         }
      } else {
         int i = 0;

         for (int mx = inventory.getSlotCount(); i < mx; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               list.add(stack);
            }
         }
      }

      settings.serialize(buffer);
      buffer.writeVarInt(list.size());
      int i = 0;

      for (int mxx = list.size(); i < mxx; i++) {
         ItemStack stack = list.get(i);
         buffer.writeItem(stack);
         buffer.writeVarInt(stack.getCount());
      }
   }

   @Override
   public Tag getServerData() {
      IHasInventory inventory = this.inventoryProvider.get();
      ListTag list = new ListTag();
      int i = 0;

      for (int m = inventory.getSlotCount(); i < m; i++) {
         ItemStack stack = inventory.getStackInSlot(i);
         if (!stack.isEmpty()) {
            CompoundTag data = stack.save(new CompoundTag());
            data.putInt("Count", stack.getCount());
            list.add(data);
         }
      }

      CompoundTag data = new CompoundTag();
      if (!list.isEmpty()) {
         data.put("items", list);
      }

      return data;
   }

   public static class InvSettings {
      public boolean showCount;
      public boolean showCountSide;
      public boolean showName;
      public boolean mergeInv;

      public InvSettings(boolean showCount, boolean showCountSide, boolean showName, boolean mergeInv) {
         this.showCount = showCount;
         this.showCountSide = showCountSide;
         this.showName = showName;
         this.mergeInv = mergeInv;
      }

      public InvSettings(FriendlyByteBuf buffer) {
         this(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
      }

      public void serialize(FriendlyByteBuf buffer) {
         buffer.writeBoolean(this.showCount);
         buffer.writeBoolean(this.showCountSide);
         buffer.writeBoolean(this.showName);
         buffer.writeBoolean(this.mergeInv);
      }
   }

   public static class RenderObject {
      public ItemStack item;
      public String text;
      public String count;
      public Component name;
      public float width;
      public float height;
      public float itemOff;

      public RenderObject(ItemStack stack) {
         this.item = stack;
         this.text = this.generateText(stack);
         this.count = Integer.toString(stack.getCount());
         this.name = stack.getHoverName();
      }

      @OnlyIn(Dist.CLIENT)
      public void computeBounds(Font font, InventoryDisplayInfo.InvSettings setting) {
         this.height = 18.0F + (setting.showCountSide ? 9.0F * 0.75F : 0.0F) + (setting.showName ? 9.0F * 0.75F : 0.0F);
         this.width = 18.0F;
         if (setting.showCountSide) {
            this.width = Math.max(this.width, (float)font.width(this.count) * 0.75F + 4.0F);
         }

         if (setting.showName) {
            this.width = Math.max(this.width, (float)font.width(this.name) * 0.75F + 4.0F);
         }

         this.itemOff = this.width / 2.0F - 8.0F;
      }

      private String generateText(ItemStack stack) {
         int count = stack.getCount();
         if (count >= 2000000) {
            return count / 1000000 + "m";
         } else if (count >= 2000) {
            return count / 1000 + "k";
         } else {
            return count > 1 ? String.valueOf(count) : "";
         }
      }
   }
}
