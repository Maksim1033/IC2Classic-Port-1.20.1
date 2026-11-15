package ic2.core.block.machines.logic.planner.encoder.impl;

import ic2.core.block.machines.logic.planner.IReactorProvider;
import ic2.core.block.machines.logic.planner.PlannerRegistry;
import ic2.core.block.machines.logic.planner.encoder.ByteShifter;
import ic2.core.block.machines.logic.planner.encoder.IEncoder;
import ic2.core.block.machines.logic.planner.encoder.ReactorSetup;
import ic2.core.block.machines.logic.planner.newLogic.ReactorInventory;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.logic.planner.newLogic.SimulationResult;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class V2Encoder implements IEncoder {
   @Override
   public CompoundTag createDecodedData(String data) {
      ByteShifter shifter = new ByteShifter(data);
      if (shifter.readInt(2) != 2) {
         return null;
      } else {
         CompoundTag nbt = new CompoundTag();
         nbt.putBoolean("steam", shifter.readBoolean());
         nbt.putInt("size", shifter.readInt(3));
         nbt.putInt("max_heat", shifter.readBoolean() ? shifter.readInt(shifter.readBoolean() ? 20 : 15) : 0);
         StringBuilder name = new StringBuilder(63);

         for (int i = shifter.readInt(6); i > 0; i--) {
            name.append((char)shifter.readChar());
         }

         nbt.putString("name", name.reverse().toString());
         ShortList list = new ShortArrayList();
         int idBits = shifter.readInt(4) + 1;
         int i = 0;

         for (int m = shifter.readInt(6); i < m; i++) {
            list.add((short)shifter.readInt(idBits));
         }

         ListTag slotList = new ListTag();
         int bits = shifter.readInt(4) + 1;
         int x = 8;

         for (int m = shifter.readInt(9); x >= 0; x--) {
            if ((m & 1 << x) != 0) {
               int y = 5;

               for (int n = shifter.readInt(6); y >= 0; y--) {
                  if ((n & 1 << y) != 0) {
                     int id = list.getShort(shifter.readInt(bits));
                     int size = shifter.readBoolean() ? shifter.readInt(6) + 1 : 1;
                     slotList.add(new IntArrayTag(new int[]{x, y, id, size}));
                  }
               }
            }
         }

         nbt.put("items", slotList);
         return nbt;
      }
   }

   @Override
   public ReactorSetup createSetup(String data, boolean simulate) {
      CompoundTag mappedData = this.createDecodedData(data);
      if (mappedData == null) {
         return null;
      } else {
         int size = mappedData.getInt("size");
         ReactorLogic logic = new ReactorLogic(IReactorProvider.dummy(size, mappedData.getBoolean("steam")));
         SimpleInventory inventory = new ReactorInventory(54, logic);
         boolean valid = false;

         for (IntArrayTag nbt : NBTListWrapper.wrap(mappedData.getList("items", 11), IntArrayTag.class)) {
            int[] array = nbt.getAsIntArray();
            ItemStack stack = PlannerRegistry.getByID(array[2]);
            if (!stack.isEmpty()) {
               inventory.setStackInSlot(array[1] * 9 + array[0], PlannerRegistry.setStackSize(stack.copy(), array[3]));
               valid = true;
            }
         }

         if (!valid) {
            return null;
         } else {
            logic.validateLogic();
            ReactorSetup setup = this.finishSetup(logic.prediction, logic.isSteamReactor(), size, mappedData.getInt("max_heat"));
            setup.setup = data;
            setup.items = inventory;
            if (simulate) {
               SimulationResult sim = new SimulationResult();
               logic.simulate(sim);
               setup.simulation = sim;
            }

            return setup;
         }
      }
   }

   @Override
   public void processData(CompoundTag decodedData, ReactorPlannerTileEntity planner) {
      planner.setSteamReactor(decodedData.getBoolean("steam"));
      planner.setReactorSize(decodedData.getInt("size"));
      planner.setupName = decodedData.getString("name");
      planner.getSettings().startingHeat = decodedData.getInt("max_heat");
      boolean valid = false;

      for (IntArrayTag nbt : NBTListWrapper.wrap(decodedData.getList("items", 11), IntArrayTag.class)) {
         int[] array = nbt.getAsIntArray();
         ItemStack stack = PlannerRegistry.getByID(array[2]);
         if (!stack.isEmpty()) {
            planner.components.setStackInSlot(array[1] * 9 + array[0], PlannerRegistry.setStackSize(stack.copy(), array[3]));
            valid = true;
         }
      }

      if (valid) {
         planner.simulation.validateLogic();
      }
   }

   @Override
   public String createEncodedData(ReactorPlannerTileEntity planner) {
      short[] slotToId = new short[54];
      Arrays.fill(slotToId, (short)-1);
      int largestId = 0;
      ShortSet storage = new ShortOpenHashSet();

      for (int i = 0; i < 54; i++) {
         ItemStack stack = planner.components.getStackInSlot(i);
         if (!stack.isEmpty()) {
            short id = PlannerRegistry.getId(stack);
            if (id >> this.getBitLimit() <= 0) {
               storage.add(id);
               slotToId[i] = id;
               largestId = Math.max(id, largestId);
            }
         }
      }

      ShortList list = new ShortArrayList(storage);
      int bits = Integer.bitCount(HashCommon.nextPowerOfTwo(list.size() + 1) - 1);
      ByteShifter shifter = new ByteShifter();
      int rows = 0;
      int x = 0;

      for (int m = planner.getReactorSize() + 3; x < m; x++) {
         int count = 0;

         for (int y = 0; y < 6; y++) {
            ItemStack stack = planner.components.getStackInSlot(y * 9 + x);
            if (!stack.isEmpty()) {
               short id = slotToId[y * 9 + x];
               if (id >= 0) {
                  int size = PlannerRegistry.getStackSize(stack);
                  if (size > 1) {
                     shifter.writeInteger(size - 1, 6);
                  }

                  shifter.writeBoolean(size > 1);
                  shifter.writeInteger(list.indexOf(id), bits);
                  count |= 1 << y;
               }
            }
         }

         if (count > 0) {
            shifter.writeInteger(count, 6);
            rows |= 1 << x;
         }
      }

      shifter.writeInteger(rows, 9);
      shifter.writeInteger(bits - 1, 4);
      x = Integer.bitCount(HashCommon.nextPowerOfTwo(largestId + 1) - 1);

      for (int ix = list.size() - 1; ix >= 0; ix--) {
         shifter.writeInteger(list.getShort(ix), x);
      }

      shifter.writeInteger(list.size(), 6);
      shifter.writeInteger(x - 1, 4);
      char[] chars = planner.setupName.toCharArray();

      for (int ix = 0; ix < chars.length && ix < 63; ix++) {
         shifter.writeCharacter(chars[ix]);
      }

      shifter.writeInteger(Math.min(63, chars.length), 6);
      int heat = Math.max(0, Math.min(planner.simulation.maxHeat - 100, planner.getSettings().startingHeat));
      if (heat > 0) {
         int heatBitsRequired = Integer.bitCount(HashCommon.nextPowerOfTwo(heat + 1) - 1);
         shifter.writeInteger(heat, heatBitsRequired > 15 ? 20 : 15);
         shifter.writeBoolean(heatBitsRequired > 15);
      }

      shifter.writeBoolean(heat > 0);
      shifter.writeInteger(planner.getReactorSize(), 3);
      shifter.writeBoolean(planner.isSteamReactor());
      shifter.writeInteger(2, 2);
      return shifter.getDataCode();
   }

   @Override
   public String getName() {
      return "V2 Encoder";
   }

   @Override
   public boolean hasBitLimit() {
      return true;
   }

   @Override
   public int getBitLimit() {
      return 10;
   }
}
