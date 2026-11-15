package ic2.core.block.rendering.camouflage;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.INetworkDataBuffer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface IQuadCache extends INetworkDataBuffer {
   CompoundTag write(CompoundTag var1);

   void read(CompoundTag var1);

   BlockState getDisplayBlock();

   int[] getColors();

   RetextureEvent.Rotation[] getRotations();

   Direction getSide();
}
