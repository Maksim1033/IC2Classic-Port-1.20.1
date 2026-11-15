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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class V1Encoder implements IEncoder {
   @Override
   public CompoundTag createDecodedData(String data) {
      ByteShifter shifter = new ByteShifter(data);
      if (shifter.readInt(6) != 63) {
         return null;
      } else if (shifter.readInt(5) != 1) {
         return null;
      } else {
         CompoundTag nbt = new CompoundTag();
         nbt.putBoolean("Steam", shifter.readBoolean());
         nbt.putInt("Size", shifter.readInt(3));
         nbt.putInt("MaxHeat", shifter.readInt(20));
         ListTag list = new ListTag();
         int expectedNameLength = shifter.readInt(6);
         StringBuilder name = new StringBuilder(63);

         for (int i = expectedNameLength; i > 0; i--) {
            name.append((char)shifter.readChar());
         }

         nbt.putString("PlanName", name.reverse().toString());
         int expectedTypes = shifter.readInt(6);

         for (int i = 0; i < expectedTypes; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putShort("ID", (short)shifter.readInt(this.getBitLimit()));
            ListTag slotList = new ListTag();
            int expectedSlots = shifter.readInt(6);

            for (int slot = 0; slot < expectedSlots; slot++) {
               int currentSlot = shifter.readInt(6);
               int stackSize = shifter.readInt(6) + 1;
               slotList.add(new IntArrayTag(new int[]{currentSlot, stackSize}));
            }

            entry.put("Slots", slotList);
            list.add(entry);
         }

         nbt.put("Data", list);
         return nbt;
      }
   }

   @Override
   public ReactorSetup createSetup(String data, boolean simulate) {
      CompoundTag mappedData = this.createDecodedData(data);
      if (mappedData == null) {
         return null;
      } else {
         ReactorLogic logic = new ReactorLogic(IReactorProvider.dummy(mappedData.getInt("Size"), mappedData.getBoolean("Steam")));
         SimpleInventory inventory = new ReactorInventory((mappedData.getInt("Size") + 3) * 6, logic);
         boolean valid = false;
         ListTag list = mappedData.getList("Data", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag nbt = list.getCompound(i);
            ItemStack stack = PlannerRegistry.getByID(nbt.getShort("ID"));
            if (!stack.isEmpty()) {
               ListTag slotList = nbt.getList("Slots", 11);

               for (int x = 0; x < slotList.size(); x++) {
                  int[] array = slotList.getIntArray(x);
                  stack = PlannerRegistry.setStackSize(stack.copy(), array[1]);
                  logic.setStackInSlot(array[0], stack);
                  inventory.setStackInSlot(array[0], stack);
                  valid = true;
               }
            }
         }

         if (!valid) {
            return null;
         } else {
            logic.validateLogic();
            ReactorSetup setup = this.finishSetup(logic.prediction, logic.isSteamReactor(), mappedData.getInt("Size"), mappedData.getInt("MaxHeat"));
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
      planner.setSteamReactor(decodedData.getBoolean("Steam"));
      planner.setReactorSize(decodedData.getInt("Size"));
      planner.setupName = decodedData.getString("PlanName");
      planner.getSettings().startingHeat = decodedData.getInt("MaxHeat");
      ListTag list = decodedData.getList("Data", 10);
      boolean valid = false;

      for (int i = 0; i < list.size(); i++) {
         CompoundTag nbt = list.getCompound(i);
         ItemStack stack = PlannerRegistry.getByID(nbt.getShort("ID"));
         if (!stack.isEmpty()) {
            ListTag slotList = nbt.getList("Slots", 11);

            for (int x = 0; x < slotList.size(); x++) {
               int[] array = slotList.getIntArray(x);
               planner.components.setStackInSlot(array[0], PlannerRegistry.setStackSize(stack.copy(), array[1]));
               valid = true;
            }
         }
      }

      if (valid) {
         planner.simulation.validateLogic();
      }
   }

   @Override
   public String createEncodedData(ReactorPlannerTileEntity planner) {
      ByteShifter shifter = new ByteShifter();
      Short2ObjectMap<IntList> storage = new Short2ObjectLinkedOpenHashMap();

      for (int i = 0; i < 54; i++) {
         ItemStack stack = planner.components.getStackInSlot(i);
         if (!stack.isEmpty()) {
            short id = PlannerRegistry.getId(stack);
            if (id >> this.getBitLimit() <= 0) {
               IntList list = (IntList)storage.get(id);
               if (list == null) {
                  list = new IntArrayList();
                  storage.put(id, list);
               }

               list.add(i);
            }
         }
      }

      ObjectIterator var9 = Short2ObjectMaps.fastIterable(storage).iterator();

      while (var9.hasNext()) {
         Entry<IntList> entry = (Entry<IntList>)var9.next();
         IntListIterator var13 = ((IntList)entry.getValue()).iterator();

         while (var13.hasNext()) {
            int slot = (Integer)var13.next();
            ItemStack stack = planner.components.getStackInSlot(slot);
            if (!stack.isEmpty()) {
               shifter.writeInteger(PlannerRegistry.getStackSize(stack) - 1, 6);
               shifter.writeInteger(slot, 6);
            }
         }

         shifter.writeInteger(((IntList)entry.getValue()).size(), 6);
         shifter.writeInteger(entry.getShortKey(), this.getBitLimit());
      }

      shifter.writeInteger(storage.size(), 6);
      char[] chars = planner.setupName.toCharArray();

      for (char aChar : chars) {
         shifter.writeCharacter(aChar);
      }

      shifter.writeInteger(chars.length, 6);
      shifter.writeInteger(Math.min(planner.simulation.maxHeat - 100, planner.getSettings().startingHeat), 20);
      shifter.writeInteger(planner.getReactorSize(), 3);
      shifter.writeBoolean(planner.isSteamReactor());
      shifter.writeInteger(1, 5);
      shifter.writeInteger(63, 6);
      return shifter.getDataCode();
   }

   @Override
   public String getName() {
      return "V1 Encoder";
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
