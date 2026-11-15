package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.IC2;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ReactorHeatStorageBase;
import ic2.core.item.reactor.planner.SimulatedHeatVent;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ReactorHeatVentItem extends ReactorHeatStorageBase {
   private ReactorHeatVentItem.VentProperty props;

   public ReactorHeatVentItem(String name, ReactorHeatVentItem.VentProperty props) {
      super(name, props.getTextureFolder(), props.getTextureName(), new PropertiesBuilder().maxDamage(props.maxHeat));
      this.props = props;
   }

   public ReactorHeatVentItem(String name, PropertiesBuilder properties, ReactorHeatVentItem.VentProperty props) {
      super(name, props.getTextureFolder(), props.getTextureName(), (properties == null ? new PropertiesBuilder() : properties).maxDamage(props.maxHeat));
      this.props = props;
   }

   public ReactorHeatVentItem.VentProperty getProperty() {
      return this.props;
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      if (reactor instanceof ISteamReactor steamReactor) {
         if (this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC) {
            return;
         }

         if (this.props.getType() == ReactorHeatVentItem.VentType.HEAT) {
            this.processNormalReactor(stack, reactor, x, y, heatCalculation);
            return;
         }

         if (heatCalculation) {
            int reactorVent = this.props.getReactor();
            int selfVent = this.props.getSelf();
            if (reactorVent > 0) {
               int reactorDrain;
               int rHeat = reactorDrain = reactor.getHeat();
               if (reactorDrain > reactorVent) {
                  reactorDrain = reactorVent;
               }

               rHeat -= reactorDrain;
               if (this.storeHeat(stack, reactor, x, y, reactorDrain) > 0) {
                  return;
               }

               reactor.setHeat(rHeat);
            }

            int heat = this.getDamage(stack);
            if (heat < 100) {
               if (reactor.getWorldObj().getGameTime() % 40L == 0L) {
                  this.storeHeat(stack, reactor, x, y, -1);
               }

               return;
            }

            double water = this.getWater(stack);
            if (water <= 0.0) {
               if (reactor.getWorldObj().getGameTime() % 40L == 0L) {
                  this.storeHeat(stack, reactor, x, y, -1);
               }

               return;
            }

            double heatLevel = (double)heat / ((double)this.props.getMaxHeat() - 100.0);
            double lvl = (double)selfVent * heatLevel / 40.0 * IC2.CONFIG.steamReactorMultiplier.get();
            double consume = Math.min(water, lvl);
            if (consume <= 0.0) {
               return;
            }

            int steam = (int)(160.0 * consume);
            FluidTank tank = steamReactor.getSteamTank();
            if (steam <= 0 || tank.getCapacity() - tank.getFluidAmount() < steam) {
               return;
            }

            consume = (double)steam / 160.0;
            tank.fill(new FluidStack(IC2Fluids.STEAM, steam), FluidAction.EXECUTE);
            reactor.addOutput((float)steam / 3.2F);
            water -= consume;
            double heatStorage = this.getHeatStorage(stack);
            heatStorage += consume * 40.0 / IC2.CONFIG.steamReactorMultiplier.get();
            int copy = -((int)heatStorage);
            copy -= this.storeHeat(stack, reactor, x, y, copy);
            heatStorage += (double)copy;
            this.setWater(stack, water);
            this.setHeatStorage(stack, heatStorage);
         } else {
            this.refill(steamReactor, stack, this.props);
         }
      } else {
         this.processNormalReactor(stack, reactor, x, y, heatCalculation);
      }
   }

   private void processNormalReactor(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation) {
      if (heatCalculation) {
         boolean producing = reactor.isProducingEnergy();
         boolean electric = this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC;
         int reactorVent = electric && !producing ? this.props.getReactor() / 2 : this.props.getReactor();
         int selfVent = this.props.getSelf();
         if (reactorVent > 0) {
            int reactorDrain;
            int rHeat = reactorDrain = reactor.getHeat();
            if (reactorDrain > reactorVent) {
               reactorDrain = reactorVent;
            }

            rHeat -= reactorDrain;
            if (this.storeHeat(stack, reactor, x, y, reactorDrain) > 0) {
               return;
            }

            reactor.setHeat(rHeat);
         }

         if (electric) {
            if (producing) {
               reactor.addOutput(-((float)selfVent * 0.005F));
            }

            this.storeHeat(stack, reactor, x, y, producing ? -this.props.getSelf() : -(this.props.getSelf() / 2));
            return;
         }

         this.storeHeat(stack, reactor, x, y, -selfVent);
      }
   }

   private void refill(ISteamReactor reactor, ItemStack yourStack, ReactorHeatVentItem.VentProperty prop) {
      double water = this.getWater(yourStack);
      if (!(water > (double)(prop.getSelf() - 1))) {
         int needed = prop.getSelf() - (int)water;
         FluidTank tank = reactor.getWaterTank();
         if (tank.getFluidAmount() > 0) {
            int min = Math.min(tank.getFluidAmount(), needed);
            FluidStack drained = tank.drain(min, FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
               this.setWater(yourStack, water + (double)drained.getAmount());
            }
         }
      }
   }

   private double getWater(ItemStack stack) {
      return StackUtil.getNbtData(stack).getDouble("WaterBuffer");
   }

   private void setWater(ItemStack stack, double water) {
      stack.getOrCreateTag().putDouble("WaterBuffer", water);
   }

   private double getHeatStorage(ItemStack stack) {
      return StackUtil.getNbtData(stack).getDouble("HeatStorage");
   }

   private void setHeatStorage(ItemStack stack, double amount) {
      stack.getOrCreateTag().putDouble("HeatStorage", amount);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      if (this.props != null && this.props.getType() == ReactorHeatVentItem.VentType.STEAM) {
         int waterPer = (int)(this.getWater(stack) / (double)this.props.getSelf() * 100.0);
         int damage = this.getDamage(stack);
         double efficiency = damage < 100 ? 0.0 : (double)damage / ((double)this.getMaxDamage(stack) - 100.0) * 100.0;
         double steamProduction = (double)this.props.getSelf() * efficiency / 25.0 * IC2.CONFIG.steamReactorMultiplier.get();
         helper.addDataTooltip("tooltip.ic2.steam_vent.water", waterPer + "%");
         helper.addDataTooltip("tooltip.ic2.steam_vent.heat", (int)efficiency + "%");
         helper.addDataTooltip("tooltip.ic2.steam_vent.steam", (int)steamProduction + "~ mB");
      }
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedHeatVent(this.props);
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return this.props.getComponentID();
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.HEAT_VENT;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      if (this.props != null) {
         switch (stat) {
            case SELF_COOLING:
               return IntTag.valueOf(this.props.getSelf());
            case REACTOR_COOLING:
               return IntTag.valueOf(this.props.getReactor());
            case ENERGY_USAGE:
               return FloatTag.valueOf((float)this.props.getSelf() * 0.005F);
            case STEAM_PRODUCTION:
               return FloatTag.valueOf((float)(this.props.getSelf() * 4) * (float)IC2.CONFIG.steamReactorMultiplier.get());
            case WATER_CONSUMPTION:
               return FloatTag.valueOf((float)this.props.getSelf() / 40.0F * (float)IC2.CONFIG.steamReactorMultiplier.get());
            case WATER_STORAGE:
               return FloatTag.valueOf((float)this.props.getSelf());
            default:
               return NULL_VALUE;
         }
      } else {
         return NULL_VALUE;
      }
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack, IReactor planner, int x, int y) {
      return super.getReactorStat(stat, stack, planner, x, y);
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      if (this.props != null) {
         if (this.props.getType() == ReactorHeatVentItem.VentType.STEAM) {
            return IReactorPlannerComponent.ReactorType.STEAM;
         }

         if (this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC) {
            return IReactorPlannerComponent.ReactorType.ELECTRIC;
         }
      }

      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      if (this.props != null) {
         list.add(IReactorPlannerComponent.ReactorStat.SELF_COOLING);
         list.add(IReactorPlannerComponent.ReactorStat.REACTOR_COOLING);
         if (this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC) {
            list.add(IReactorPlannerComponent.ReactorStat.ENERGY_USAGE);
         } else if (this.props.getType() == ReactorHeatVentItem.VentType.STEAM) {
            list.add(IReactorPlannerComponent.ReactorStat.WATER_CONSUMPTION);
            list.add(IReactorPlannerComponent.ReactorStat.STEAM_PRODUCTION);
            list.add(IReactorPlannerComponent.ReactorStat.WATER_STORAGE);
         }
      }

      return list;
   }

   public static class VentProperty {
      ReactorHeatVentItem.VentType type;
      int self;
      int reactor;
      int maxHeat;
      String textureFolder;
      String textureName;
      short id;

      public VentProperty(ReactorHeatVentItem.VentType type, int self, int reactor, int maxHeat, String textureFolder, String textureName, int componentId) {
         this.type = type;
         this.self = self;
         this.reactor = reactor;
         this.maxHeat = maxHeat;
         this.textureFolder = textureFolder;
         this.textureName = textureName;
         this.id = (short)componentId;
      }

      public short getComponentID() {
         return this.id;
      }

      public String getTextureFolder() {
         return this.textureFolder;
      }

      public String getTextureName() {
         return this.textureName;
      }

      public ReactorHeatVentItem.VentType getType() {
         return this.type;
      }

      public int getMaxHeat() {
         return this.maxHeat;
      }

      public int getSelf() {
         return this.self;
      }

      public int getReactor() {
         return this.reactor;
      }
   }

   public static enum VentType {
      HEAT,
      STEAM,
      ELECTRIC;
   }
}
