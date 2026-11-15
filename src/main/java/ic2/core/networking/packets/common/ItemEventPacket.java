package ic2.core.networking.packets.common;

import ic2.api.network.item.INetworkItemEvent;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;

public class ItemEventPacket extends IC2Packet {
   ItemStack stack;
   int key;
   int value;
   boolean client;

   public ItemEventPacket() {
   }

   public ItemEventPacket(ItemStack stack, int key, int value, boolean client) {
      this.stack = stack;
      this.key = key;
      this.value = value;
      this.client = client;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeItem(this.stack);
      buffer.writeBoolean(this.client);
      buffer.writeInt(this.key);
      buffer.writeInt(this.value);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.stack = buffer.readItem();
      this.client = buffer.readBoolean();
      this.key = buffer.readInt();
      this.value = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      if (this.stack.getItem() instanceof INetworkItemEvent) {
         ((INetworkItemEvent)this.stack.getItem()).onEventReceived(this.stack, source, this.key, this.value, this.client ? Dist.CLIENT : Dist.DEDICATED_SERVER);
      }
   }
}
