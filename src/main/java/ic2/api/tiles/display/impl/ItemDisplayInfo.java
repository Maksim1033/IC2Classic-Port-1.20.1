package ic2.api.tiles.display.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemDisplayInfo implements IDisplayInfo {
   ItemStack item;
   boolean showCount;
   Supplier<ItemStack> itemProvider;
   BooleanSupplier aliveProvider;

   public ItemDisplayInfo(FriendlyByteBuf buffer) {
      this.showCount = buffer.readBoolean();
      this.item = buffer.readItem();
      if (this.showCount) {
         this.item.setCount(buffer.readVarInt());
      }
   }

   public ItemDisplayInfo(boolean showCount, Supplier<ItemStack> itemProvider, BooleanSupplier aliveProvider) {
      this.showCount = showCount;
      this.itemProvider = itemProvider;
      this.aliveProvider = aliveProvider;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      stack.pushPose();
      stack.last().normal().scale(1.0F, -1.0F, 1.0F);
      helper.renderGuiItems(stack, this.item, (float)x, (float)y);
      if (this.showCount) {
         helper.renderGuiItemText(stack, this.item, (float)x, (float)y, null);
      }

      stack.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getHeight(int width, IDisplayInfo.Alignment align) {
      return 18;
   }

   @Override
   public boolean isValid() {
      return this.aliveProvider.getAsBoolean();
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      ItemStack stack = this.itemProvider.get();
      buffer.writeBoolean(this.showCount);
      buffer.writeItem(stack);
      if (this.showCount) {
         buffer.writeVarInt(stack.getCount());
      }
   }

   @Override
   public Tag getServerData() {
      ItemStack stack = this.itemProvider.get();
      CompoundTag data = stack.save(new CompoundTag());
      if (this.showCount) {
         data.putInt("Count", stack.getCount());
      } else {
         data.remove("Count");
      }

      return data;
   }
}
