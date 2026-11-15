package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.IUsableUranium;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.IC2;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.IUraniumRod;
import ic2.core.item.reactor.base.ItemStackCoord;
import ic2.core.item.reactor.base.ReactorComponentBase;
import ic2.core.item.reactor.planner.SimulatedUranium;
import ic2.core.item.reactor.urantypes.BlazeUranium;
import ic2.core.item.reactor.urantypes.CharcoalUranium;
import ic2.core.item.reactor.urantypes.EnderUranium;
import ic2.core.item.reactor.urantypes.NetherStarUranium;
import ic2.core.item.reactor.urantypes.RedstoneUranium;
import ic2.core.item.reactor.urantypes.StandardUranium;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class ReactorUraniumRod extends ReactorComponentBase implements IUsableUranium {
   final IUraniumRod uranium;
   final int rodCount;
   final short componentId;
   public static final IUraniumRod[] TYPES = new IUraniumRod[6];
   RandomSource random = RandomSource.create();

   public ReactorUraniumRod(String itemName, String textureFolder, String textureName, IUraniumRod uranium, int rodCount, int componentId) {
      super(itemName, textureFolder, textureName, new PropertiesBuilder().maxDamage(uranium.getRodDurability()));
      this.uranium = uranium;
      this.rodCount = rodCount;
      this.componentId = (short)componentId;
   }

   public ReactorUraniumRod(
      String itemName, String textureFolder, String textureName, PropertiesBuilder builder, IUraniumRod uranium, int rodCount, int componentId
   ) {
      super(itemName, textureFolder, textureName, (builder == null ? new PropertiesBuilder() : builder).maxDamage(uranium.getRodDurability()));
      this.uranium = uranium;
      this.rodCount = rodCount;
      this.componentId = (short)componentId;
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      if (reactor.isProducingEnergy()) {
         int pulsePerTick = this.uranium.getUraniumPulses();
         List<int[]> affectedArea = this.uranium.getPulseArea();
         if (heatCalculation) {
            List<int[]> heatArea = this.uranium.getHeatArea();

            for (int iteration = 0; iteration < this.rodCount; iteration++) {
               int pulses = (1 + this.rodCount / 2) * pulsePerTick;

               for (int pulse = 0; pulse < pulsePerTick; pulse++) {
                  int i = 0;

                  for (int m = affectedArea.size(); i < m; i++) {
                     int[] offsets = affectedArea.get(i);
                     pulses += this.pulseNeighbor(reactor, x + offsets[0], y + offsets[1], stack, x, y, true, damageTick);
                  }
               }

               int heat = (int)((float)(MathUtils.sumUp(pulses) * 4) * this.uranium.getPulseHeatModifier());
               PriorityQueue<ItemStackCoord> queue = CollectionUtils.createInsertionQueue();
               int i = 0;

               for (int m = heatArea.size(); i < m; i++) {
                  int[] offsets = heatArea.get(i);
                  this.findNeighbor(reactor, x + offsets[0], y + offsets[1], queue);
               }

               while (!queue.isEmpty() && heat > 0) {
                  i = heat / queue.size();
                  heat -= i;
                  ItemStackCoord target = (ItemStackCoord)queue.dequeue();
                  heat += ((IReactorComponent)target.stack.getItem()).storeHeat(target.stack, reactor, target.x, target.y, i);
               }

               if (heat > 0) {
                  reactor.addHeat(heat);
               }
            }
         } else {
            int pulses = (1 + this.rodCount / 2) * pulsePerTick;

            for (int iteration = 0; iteration < this.rodCount; iteration++) {
               for (int i = 0; i < pulses; i++) {
                  this.acceptUraniumPulse(stack, reactor, stack, x, y, x, y, false, damageTick);
               }

               for (int pulse = 0; pulse < pulsePerTick; pulse++) {
                  int i = 0;

                  for (int m = affectedArea.size(); i < m; i++) {
                     int[] offsets = affectedArea.get(i);
                     this.pulseNeighbor(reactor, x + offsets[0], y + offsets[1], stack, x, y, false, damageTick);
                  }
               }
            }
         }

         if (damageTick) {
            int damage = stack.getDamageValue();
            if (damage + 1 > stack.getMaxDamage()) {
               reactor.setStackInReactor(x, y, this.random.nextInt(3) == 0 ? this.uranium.createNearDepletedRod(this.rodCount) : ItemStack.EMPTY);
            } else {
               stack.setDamageValue(damage + 1);
            }
         }
      }
   }

   @Override
   public void addAffectedSlots(int x, int y, BiPredicate<Integer, Integer> slots) {
      slots.test(x, y);

      for (int[] pos : this.uranium.getHeatArea()) {
         slots.test(x + pos[0], y + pos[1]);
      }

      for (int[] pos : this.uranium.getPulseArea()) {
         slots.test(x + pos[0], y + pos[1]);
      }
   }

   @Override
   public boolean acceptUraniumPulse(
      ItemStack stack, IReactor reactor, ItemStack source, int myX, int myY, int sourceX, int sourceY, boolean heatRun, boolean damageTick
   ) {
      if (reactor instanceof ISteamReactor) {
         return true;
      } else {
         if (!heatRun) {
            reactor.addOutput(this.uranium.getPulseEU());
         }

         return true;
      }
   }

   @Override
   public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return false;
   }

   @Override
   public int getStoredHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int getMaxStoredHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int storeHeat(ItemStack stack, IReactor reactor, int x, int y, int heatChange) {
      return heatChange;
   }

   @Override
   public float getExplosionInfluence(ItemStack stack, IReactor reactor) {
      return this.uranium.getExplosionModifier() * (float)this.rodCount;
   }

   protected int pulseNeighbor(IReactor reactor, int targetX, int targetY, ItemStack stack, int x, int y, boolean heatRun, boolean damageTick) {
      ItemStack other = reactor.getStackInReactor(targetX, targetY);
      return other.getItem() instanceof IReactorComponent
            && ((IReactorComponent)other.getItem()).acceptUraniumPulse(other, reactor, stack, targetX, targetY, x, y, heatRun, damageTick)
         ? this.uranium.getPulsesForConnection()
         : 0;
   }

   protected void findNeighbor(IReactor reactor, int x, int y, PriorityQueue<ItemStackCoord> coords) {
      ItemStack stack = reactor.getStackInReactor(x, y);
      if (stack.getItem() instanceof IReactorComponent && ((IReactorComponent)stack.getItem()).canStoreHeat(stack, reactor, x, y)) {
         coords.enqueue(new ItemStackCoord(stack, x, y));
      }
   }

   @Override
   public ItemStack createDepletedUraniumRod() {
      return this.uranium.createNearDepletedRod(this.rodCount);
   }

   public IUraniumRod getUranium() {
      return this.uranium;
   }

   public int getRodCount() {
      return this.rodCount;
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedUranium(this.componentId, this.getMaxDamage(self), this.uranium, this.rodCount);
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return this.componentId;
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.FUEL_ROD;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.ROD_COUNT);
      list.add(IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY);
      list.add(IReactorPlannerComponent.ReactorStat.PULSE_COUNT);
      list.add(IReactorPlannerComponent.ReactorStat.HEAT_PRODUCTION);
      list.add(IReactorPlannerComponent.ReactorStat.ENERGY_PRODUCTION);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      switch (stat) {
         case ROD_COUNT:
            return IntTag.valueOf(this.rodCount);
         case MAX_COMPONENT_DURABILITY:
            return IntTag.valueOf(stack.getMaxDamage());
         case PULSE_COUNT:
            return IntTag.valueOf((1 + this.rodCount / 2) * this.uranium.getUraniumPulses() * this.rodCount);
         case HEAT_PRODUCTION:
            return IntTag.valueOf(
               (int)((float)(MathUtils.sumUp((1 + this.rodCount / 2) * this.uranium.getUraniumPulses()) * 4) * this.uranium.getPulseHeatModifier())
                  * this.rodCount
            );
         case ENERGY_PRODUCTION:
            return FloatTag.valueOf(
               (float)((1 + this.rodCount / 2) * this.uranium.getUraniumPulses())
                  * this.uranium.getPulseEU()
                  * (float)this.rodCount
                  * (float)IC2.CONFIG.reactorOutput.get()
            );
         default:
            return NULL_VALUE;
      }
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack, IReactor planner, int x, int y) {
      switch (stat) {
         case HEAT_PRODUCTION: {
            int pulsePerTick = this.uranium.getUraniumPulses();
            List<int[]> affectedArea = this.uranium.getPulseArea();
            int heat = 0;

            for (int iteration = 0; iteration < this.rodCount; iteration++) {
               int pulses = (1 + this.rodCount / 2) * pulsePerTick;

               for (int pulse = 0; pulse < pulsePerTick; pulse++) {
                  int i = 0;

                  for (int m = affectedArea.size(); i < m; i++) {
                     int[] offsets = affectedArea.get(i);
                     pulses += this.pulseNeighbor(planner, x + offsets[0], y + offsets[1], stack, x, y, true, false);
                  }
               }

               heat += (int)((float)(MathUtils.sumUp(pulses) * 4) * this.uranium.getPulseHeatModifier());
            }

            return IntTag.valueOf(heat);
         }
         case ENERGY_PRODUCTION: {
            int pulsePerTick = this.uranium.getUraniumPulses();
            List<int[]> affectedArea = this.uranium.getPulseArea();
            int pulses = (1 + this.rodCount / 2) * pulsePerTick;

            for (int iteration = 0; iteration < this.rodCount; iteration++) {
               for (int i = 0; i < pulses; i++) {
                  this.acceptUraniumPulse(stack, planner, stack, x, y, x, y, false, false);
               }

               for (int pulse = 0; pulse < pulsePerTick; pulse++) {
                  int i = 0;

                  for (int m = affectedArea.size(); i < m; i++) {
                     int[] offsets = affectedArea.get(i);
                     this.pulseNeighbor(planner, x + offsets[0], y + offsets[1], stack, x, y, false, false);
                  }
               }
            }

            return FloatTag.valueOf((float)planner.getEnergyOutput());
         }
         default:
            return super.getReactorStat(stat, stack, planner, x, y);
      }
   }

   static {
      TYPES[0] = StandardUranium.INSTANCE;
      TYPES[1] = RedstoneUranium.INSTANCE;
      TYPES[2] = BlazeUranium.INSTANCE;
      TYPES[3] = EnderUranium.INSTANCE;
      TYPES[4] = NetherStarUranium.INSTANCE;
      TYPES[5] = CharcoalUranium.INSTANCE;
   }
}
