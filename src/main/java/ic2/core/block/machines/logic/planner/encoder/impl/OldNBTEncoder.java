package ic2.core.block.machines.logic.planner.encoder.impl;

import ic2.core.block.machines.logic.planner.IReactorProvider;
import ic2.core.block.machines.logic.planner.PlannerRegistry;
import ic2.core.block.machines.logic.planner.encoder.IEncoder;
import ic2.core.block.machines.logic.planner.encoder.ReactorSetup;
import ic2.core.block.machines.logic.planner.newLogic.ReactorInventory;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.logic.planner.newLogic.SimulationResult;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.inv.SimpleInventory;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap.Entry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class OldNBTEncoder implements IEncoder {
   @Override
   public CompoundTag createDecodedData(String data) {
      try {
         DataInputStream stream = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(new BigInteger(data, 36).toByteArray())));
         String exportedNBT = stream.readUTF();
         stream.close();
         if (exportedNBT.startsWith("{") && exportedNBT.endsWith("}")) {
            return TagParser.parseTag(exportedNBT);
         }
      } catch (Exception var4) {
      }

      return null;
   }

   @Override
   public ReactorSetup createSetup(String data, boolean simulate) {
      CompoundTag mappedData = this.createDecodedData(data);
      if (mappedData == null) {
         return null;
      } else {
         ReactorLogic logic = new ReactorLogic(IReactorProvider.dummy(6, mappedData.getBoolean("SteamReactor")));
         SimpleInventory inventory = new ReactorInventory(54, logic);
         ListTag list = mappedData.getList("Data", 10);
         boolean valid = false;

         for (int i = 0; i < list.size(); i++) {
            CompoundTag nbt = list.getCompound(i);
            short id = nbt.getShort("ID");
            int[] array = nbt.getIntArray("Slots");
            ItemStack item = PlannerRegistry.getByID(id);

            for (int slot : array) {
               if (slot != -1) {
                  inventory.setStackInSlot(slot, item.copy());
                  valid = true;
               }
            }
         }

         if (!valid) {
            return null;
         } else {
            logic.validateLogic();
            ReactorSetup setup = this.finishSetup(logic.prediction, logic.isSteamReactor(), 6, 0);
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
      planner.setSteamReactor(decodedData.getBoolean("SteamReactor"));
      planner.setReactorSize(6);
      ReactorLogic base = planner.simulation;
      ListTag list = decodedData.getList("Data", 10);
      boolean valid = false;

      for (int i = 0; i < list.size(); i++) {
         CompoundTag data = list.getCompound(i);
         short id = data.getShort("ID");
         int[] array = data.getIntArray("Slots");
         ItemStack item = PlannerRegistry.getByID(id);

         for (int slot : array) {
            if (slot != -1) {
               planner.components.setStackInSlot(slot, item.copy());
               valid = true;
            }
         }
      }

      if (valid) {
         base.validateLogic();
      }
   }

   @Override
   public String createEncodedData(ReactorPlannerTileEntity planner) {
      Short2ObjectMap<IntList> amounts = new Short2ObjectLinkedOpenHashMap();

      for (int i = 0; i < 54; i++) {
         ItemStack stack = planner.components.getStackInSlot(i);
         if (!stack.isEmpty()) {
            short id = PlannerRegistry.getId(stack);
            if (id != -1) {
               IntList list = (IntList)amounts.get(id);
               if (list == null) {
                  list = new IntArrayList();
                  amounts.put(id, list);
               }

               list.add(i);
            }
         }
      }

      ListTag dataList = new ListTag();
      ObjectIterator var10 = Short2ObjectMaps.fastIterable(amounts).iterator();

      while (var10.hasNext()) {
         Entry<IntList> entry = (Entry<IntList>)var10.next();
         CompoundTag nbt = new CompoundTag();
         nbt.putShort("ID", entry.getShortKey());
         IntList slots = (IntList)entry.getValue();
         if (slots.size() == 1) {
            slots.add(-1);
         }

         nbt.putIntArray("Slots", slots.toIntArray());
         dataList.add(nbt);
      }

      CompoundTag nbt = new CompoundTag();
      nbt.put("Data", dataList);
      nbt.putBoolean("SteamReactor", planner.isSteamReactor());

      try {
         ByteArrayOutputStream array = new ByteArrayOutputStream();
         DataOutputStream stream = new DataOutputStream(new GZIPOutputStream(array));
         stream.writeUTF(nbt.toString());
         stream.close();
         return new BigInteger(array.toByteArray()).toString(36);
      } catch (Exception var8) {
         return null;
      }
   }

   @Override
   public String getName() {
      return "First (NBT)";
   }

   @Override
   public boolean hasBitLimit() {
      return false;
   }

   @Override
   public int getBitLimit() {
      return 0;
   }
}
