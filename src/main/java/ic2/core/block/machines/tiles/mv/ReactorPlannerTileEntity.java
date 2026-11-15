package ic2.core.block.machines.tiles.mv;

import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.mv.ReactorPlannerContainer;
import ic2.core.block.machines.logic.planner.IReactorProvider;
import ic2.core.block.machines.logic.planner.PlannerInventory;
import ic2.core.block.machines.logic.planner.UserSettings;
import ic2.core.block.machines.logic.planner.encoder.EncoderRegistry;
import ic2.core.block.machines.logic.planner.encoder.IEncoder;
import ic2.core.block.machines.logic.planner.newLogic.ReactorBackup;
import ic2.core.block.machines.logic.planner.newLogic.ReactorInventory;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.logic.planner.newLogic.ReactorPrediction;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorPlannerTileEntity
   extends BaseElectricTileEntity
   implements IReactorProvider,
   ITickListener,
   ITileGui,
   INetworkClientEventListener,
   INetworkDataEventListener,
   IClickable,
   ITileActivityProvider {
   @NetworkInfo
   boolean isSteamReactor = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   int reactorSize = 6;
   @NetworkInfo
   public String setupName = "";
   @NetworkInfo
   public UserSettings settings = new UserSettings();
   @NetworkInfo
   public ReactorLogic simulation = new ReactorLogic(this);
   @NetworkInfo
   public ReactorPrediction prediction = new ReactorPrediction();
   @NetworkInfo
   public ReactorBackup backup = new ReactorBackup();
   public SimpleInventory components = new ReactorInventory(54, this.simulation);
   @NetworkInfo
   public int selectedSlot = -1;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int stackSize = 1;
   @NetworkInfo
   public PlannerInventory selection = new PlannerInventory(this);
   @NetworkInfo
   public int selectedView = 0;
   protected boolean singleTick = false;
   protected boolean tickStarted = false;

   public ReactorPlannerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 128, 100000);
      this.addGuiFields(
         new String[]{
            "isSteamReactor", "reactorSize", "setupName", "settings", "prediction", "simulation", "backup", "selectedSlot", "stackSize", "selectedView"
         }
      );
   }

   @Override
   public boolean allowsUI() {
      return false;
   }

   @Override
   public boolean isAllowingUI() {
      return false;
   }

   public void updateLogic() {
      this.updateGuiField("simulation");
   }

   public void updateData() {
      this.updateGuiFields(new String[]{"prediction", "simulation"});
   }

   public void clearInventory() {
      this.components.clear();
   }

   @Override
   public IHasInventory getComponents() {
      return this.components;
   }

   protected void onSizeChanged() {
      int size = this.getReactorSize() + 3;

      for (int x = size; x < 9; x++) {
         for (int y = 0; y < 6; y++) {
            this.components.setStackInSlot(y * 9 + x, ItemStack.EMPTY);
         }
      }
   }

   public void setReactorSize(int size) {
      size = Mth.clamp(size, 0, 6);
      if (size != this.reactorSize) {
         this.reactorSize = size;
         if (this.simulation.isValid) {
            this.simulation.resetSimulation();
         }

         this.onSizeChanged();
         this.updateGuiField("reactorSize");
         this.updateLogic();
      }
   }

   @Override
   public int getReactorSize() {
      return this.reactorSize;
   }

   @Override
   public boolean isSteamReactor() {
      return this.isSteamReactor;
   }

   @Override
   public long getGameTime(long tick) {
      return this.getLevel().getGameTime();
   }

   public void setSteamReactor(boolean value) {
      if (this.isSteamReactor != value) {
         this.simulation.resetSimulation();
         this.clearInventory();
         this.isSteamReactor = value;
         this.selectedSlot = -1;
         this.selection.init();
         this.updateGuiFields(new String[]{"isSteamReactor", "selectedSlot", "selection"});
         this.updateData();
      }
   }

   @Override
   public UserSettings getSettings() {
      return this.settings;
   }

   @Override
   public void onTick() {
      if ((this.isActive() || this.singleTick) && this.hasEnergy(100)) {
         this.singleTick = false;
         this.useEnergy(100);
         if (this.simulation.isFinished()) {
            this.setActive(false);
         } else {
            if (this.simulation.isValid && !this.simulation.isBroken()) {
               if (this.tickStarted) {
                  return;
               }

               this.tickStarted = true;
               IC2.OFF_THREAD_WORKER.execute(new ReactorPlannerTileEntity.SimulationTask(this.simulation, this.settings.ticksPerTick, () -> {
                  this.simulation.syncComponents();
                  this.updateData();
                  this.tickStarted = false;
               }));
            } else {
               this.setActive(false);
            }
         }
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      this.selection.init();
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return false;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ReactorPlannerContainer(this, player, windowID);
   }

   public void setComponentInSlot(int slot, boolean clear) {
      if (clear && this.components.getStackInSlot(slot).isEmpty()) {
         this.selectedSlot = -1;
         this.updateGuiField("selectedSlot");
      }

      if (this.selectedSlot != -1 && !clear) {
         if (this.simulation.isValid) {
            this.simulation.resetSimulation();
            this.prediction.clear();
            this.updateData();
         }

         this.components.setStackInSlot(slot, this.selection.getCurrentItem(this.selectedSlot));
      } else {
         if (this.simulation.isValid) {
            this.simulation.resetSimulation();
            this.prediction.clear();
            this.updateData();
         }

         this.components.setStackInSlot(slot, ItemStack.EMPTY);
      }
   }

   public void pickItem(int slot) {
      this.selectedSlot = this.selection.findItem(this.components.getStackInSlot(slot));
      this.updateGuiFields(new String[]{"selectedSlot", "selection"});
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.selectedSlot = -1;
         if (value == 1) {
            this.selection.setPrevFilter();
         } else {
            this.selection.setNextFilter();
         }

         this.updateGuiFields(new String[]{"selection", "selectedSlot"});
      } else if (key == 1) {
         this.setReactorSize(this.getReactorSize() + value);
      } else if (key == 2) {
         this.selection.scroll(value);
         this.updateGuiField("selection");
      } else if (key == 3) {
         this.selection.setScroll(value);
         this.updateGuiField("selection");
      } else if (key == 4) {
         if (value < 0 || value >= this.selection.getTotalItems()) {
            this.selectedSlot = -1;
            this.updateGuiField("selectedSlot");
            return;
         }

         this.selectedSlot = value;
         this.updateGuiField("selectedSlot");
      } else if (key == 5) {
         this.setSteamReactor(value == 1);
      } else if (key == 6) {
         this.selectedView = value;
         this.updateGuiField("selectedView");
      } else if (key == 7) {
         if (this.simulation.isValid) {
            this.setActive(value == 0);
         }
      } else if (key == 8) {
         if (this.simulation.isValid) {
            this.simulation.isProducing = !this.simulation.isProducing;
            this.updateLogic();
         }
      } else if (key == 9) {
         if (value == 0 && !this.simulation.isValid && this.simulation.slotsFilled > 0L) {
            this.simulation.validateLogic();
            this.prediction.load(this.simulation.prediction.save(new CompoundTag()));
            this.updateData();
         } else if (this.simulation.isValid) {
            this.simulation.resetSimulation();
            this.prediction.clear();
            this.updateData();
         }
      } else if (key == 10) {
         if (value == 0 && this.simulation.isValid) {
            this.backup = new ReactorBackup();
            this.backup.isSteam = this.isSteamReactor;
            this.backup.customHeat = this.settings.startingHeat;
            this.backup.customTicks = this.settings.maxTicks;
            this.backup.reactorSize = this.getReactorSize();
            this.backup.inventory.copyFrom(this.components);
            this.backup.hasBackup = true;
            this.updateGuiField("backup");
         } else if (this.backup.hasBackup && !this.isActive()) {
            this.simulation.resetSimulation();
            this.clearInventory();
            this.prediction.clear();
            this.setSteamReactor(this.backup.isSteam);
            this.setReactorSize(this.backup.reactorSize);
            this.settings.maxTicks = this.backup.customTicks;
            this.settings.startingHeat = this.backup.customHeat;
            this.backup.inventory.pasteTo(this.components);
            this.simulation.validateLogic();
            this.prediction.load(this.simulation.prediction.save(new CompoundTag()));
            this.updateData();
            this.updateGuiField("setupName");
         }
      } else if (key == 11) {
         this.settings.startingHeat = value;
         this.updateGuiField("settings");
      } else if (key == 12) {
         this.settings.maxTicks = value;
         this.updateGuiField("settings");
         if (!this.isActive() && this.simulation.isValid) {
            if (this.simulation.tick > 0) {
               this.simulation.resetSimulation();
            }

            this.simulation.validateLogic();
            this.prediction.load(this.simulation.prediction.save(new CompoundTag()));
            this.updateData();
         }
      } else if (key == 13) {
         this.stackSize = Mth.clamp(this.stackSize + value, 1, 64);
         this.selection.update();
         this.updateGuiFields(new String[]{"stackSize", "selection", "selectedSlot"});
      } else if (key == 14) {
         if (value == 0) {
            this.setActive(false);
            this.simulation.resetSimulation();
            this.prediction.clear();
            this.clearInventory();
            this.updateData();
         } else {
            this.backup = new ReactorBackup();
            this.updateGuiField("backup");
         }
      } else if (key == 15) {
         this.settings.ticksPerTick = Mth.clamp(value, 1, IC2.CONFIG.maxReactorTicks.get());
         this.updateGuiField("settings");
      } else if (key == 16) {
         this.settings.ticksPerTick = Mth.clamp(this.settings.ticksPerTick + value, 1, IC2.CONFIG.maxReactorTicks.get());
         this.updateGuiField("settings");
      } else if (key == 18) {
         this.singleTick = true;
      }
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (data instanceof StringDataBuffer buffer) {
         if (buffer.getId() == 0 && !this.setupName.equalsIgnoreCase(buffer.getValue())) {
            this.setupName = buffer.getValue();
            this.updateGuiField("setupName");
         } else if (buffer.getId() == 1) {
            IEncoder coder = EncoderRegistry.INSTANCE.getEncoderFromID(buffer.getValue());
            if (coder == null) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.no_encoder"), false);
               return;
            }

            if (!this.simulation.isValid) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.invalid_logic"), false);
               return;
            }

            if (this.simulation.tick > 0) {
               this.simulation.resetSimulation();
            }

            String s = coder.createEncodedData(this);
            if (s == null) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.encode_fail", new Object[]{coder.getName()}), false);
               return;
            }

            this.sendToPlayer(player, "clipboard", new StringDataBuffer(2, s));
            player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.encode_success", new Object[]{coder.getName()}), false);
         } else if (buffer.getId() == 2 && id.equalsIgnoreCase("clipboard") && target.isClient()) {
            this.storeToClipboard(buffer.getValue());
         } else if (buffer.getId() == 3) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.is(IC2Items.MEMORY_STICK)) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.no_memory_stick"), false);
               return;
            }

            IEncoder coderx = EncoderRegistry.INSTANCE.getEncoderFromID(buffer.getValue());
            if (coderx == null) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.no_encoder"), false);
               return;
            }

            if (!this.simulation.isValid) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.invalid_logic"), false);
               return;
            }

            if (this.simulation.tick > 0) {
               this.simulation.resetSimulation();
            }

            String s = coderx.createEncodedData(this);
            if (s == null) {
               player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.encode_fail", new Object[]{coderx.getName()}), false);
               return;
            }

            stack.getOrCreateTag().putString("setupId", s);
            stack.getOrCreateTag().putString("setupName", this.setupName);
            player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.encode_success", new Object[]{coderx.getName()}), false);
         }
      } else if (data instanceof NBTBuffer bufferx && id.equalsIgnoreCase("import")) {
         CompoundTag nbt = bufferx.getNBT();
         IEncoder coderxx = EncoderRegistry.INSTANCE.getEncoderFromID(nbt.getString("decoder"));
         if (coderxx == null) {
            player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.decoder_not_found", new Object[]{nbt.getString("decoder")}), false);
            return;
         }

         this.setActive(false);
         this.simulation.resetSimulation();
         this.prediction.clear();
         this.clearInventory();
         this.updateData();
         coderxx.processData(nbt.getCompound("data"), this);
         player.displayClientMessage(this.translate("gui.ic2.reactor_planner.io.decoder_success", new Object[]{coderxx.getName()}), false);
         ReactorPlannerContainer container = this.getContainer(player, ReactorPlannerContainer.class);
         if (container != null) {
            container.clearSlots();
            container.broadcastChanges();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void storeToClipboard(String name) {
      Minecraft.getInstance().keyboardHandler.setClipboard(name);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("reactorSize")) {
         this.getContainer(player, ReactorPlannerContainer.class).updateSlots();
      }

      if (fields.contains("setupName") || fields.contains("settings")) {
         this.getContainer(player, ReactorPlannerContainer.class).setupNameChanged();
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.isSteamReactor = compound.getBoolean("isSteam");
      this.reactorSize = compound.getInt("reactorSize");
      this.setupName = compound.getString("setupName");
      this.settings.load(compound.getCompound("settings"));
      this.backup.load(compound.getCompound("backup"));
      this.components.load(compound.getCompound("components"));
      this.simulation.load(compound.getCompound("simulation"));
      this.selectedSlot = compound.getInt("selectedSlot");
      this.stackSize = compound.getInt("stackSize");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putBoolean("isSteam", this.isSteamReactor);
      compound.putByte("reactorSize", (byte)this.reactorSize);
      compound.putString("setupName", this.setupName);
      compound.put("settings", this.settings.save(new CompoundTag()));
      compound.put("backup", this.backup.save(new CompoundTag()));
      compound.put("components", this.components.save(new CompoundTag()));
      compound.put("simulation", this.simulation.save(new CompoundTag()));
      compound.putInt("selectedSlot", this.selectedSlot);
      compound.putByte("stackSize", (byte)this.stackSize);
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.REACTOR_PLANNER;
   }

   public static class SimulationTask implements Runnable {
      Runnable completion;
      ReactorLogic simulation;
      int ticks;

      public SimulationTask(ReactorLogic simulation, int ticks, Runnable completion) {
         this.completion = completion;
         this.simulation = simulation;
         this.ticks = ticks;
      }

      @Override
      public void run() {
         int i = 0;

         for (int m = this.ticks; i < m && !this.simulation.isFinished() && !this.simulation.isBroken(); i++) {
            this.simulation.tick();
         }

         IC2.PLATFORM.getServer().execute(this.completion);
      }
   }
}
