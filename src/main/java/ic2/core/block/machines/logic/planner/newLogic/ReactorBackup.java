package ic2.core.block.machines.logic.planner.newLogic;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.inventory.inv.SimpleInventory;
import net.minecraft.nbt.CompoundTag;

public class ReactorBackup implements INetworkDataBuffer, INBTSavable {
   public SimpleInventory inventory = new SimpleInventory(54);
   public int customTicks;
   public int customHeat;
   public int reactorSize;
   public boolean hasBackup = false;
   public boolean isSteam;

   @Override
   public CompoundTag save(CompoundTag nbt) {
      nbt.putInt("CustomTicks", this.customTicks);
      nbt.putInt("CustomHeat", this.customHeat);
      nbt.putInt("Size", this.reactorSize);
      nbt.putBoolean("HasBackup", this.hasBackup);
      nbt.putBoolean("isSteam", this.isSteam);
      nbt.put("setup", this.inventory.save(new CompoundTag()));
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.customTicks = nbt.getInt("CustomTicks");
      this.customHeat = nbt.getInt("CustomHeat");
      this.reactorSize = nbt.getInt("Size");
      this.hasBackup = nbt.getBoolean("HasBackup");
      this.isSteam = nbt.getBoolean("isSteam");
      this.inventory.load(nbt.getCompound("setup"));
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.hasBackup = buffer.readBoolean();
      this.reactorSize = buffer.readByte();
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeBoolean(this.hasBackup);
      buffer.writeByte((byte)this.reactorSize);
   }
}
