package ic2.core.platform.player;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasCustomGui;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerHandler {
   static final int FLAG_EU_READER = 1;
   static final int FLAG_THERMOMETER = 2;
   static final int FLAG_CROP_SCANNER = 4;
   static Map<UUID, PlayerHandler> INSTANCES = CollectionUtils.<UUID, PlayerHandler>createMap();
   static PlayerHandler CLIENT_INSTANCE = new PlayerHandler();
   Player player;
   public BlockEntity trackedTile;
   public IHasCustomGui customGui;
   public Stack<PlayerHandler.GuiEntry> cachedGUIs = new Stack<>();
   public boolean doSpringBoost = true;
   public int speedTicker = 0;
   public float jumpBoost = 1.0F;
   public boolean altKeyDown = false;
   public boolean boostKeyDown = false;
   public boolean modeSwitchKeyDown = false;
   public boolean sideInventoryKeyDown = false;
   public boolean hudModeKeyDown = false;
   public boolean toggleKeyDown = false;
   public boolean jumpKeyDown = false;
   public boolean forwardKeyDown = false;
   public boolean screenOpen = false;
   int flags;
   long lastCheck = -1L;

   private PlayerHandler() {
   }

   public static PlayerHandler getHandler(Player player) {
      if (IC2.PLATFORM.isRendering()) {
         return getClientPlayerHandler(player);
      } else {
         PlayerHandler handler = INSTANCES.get(player.getUUID());
         if (handler == null) {
            handler = new PlayerHandler();
            INSTANCES.put(player.getUUID(), handler);
         }

         handler.player = player;
         return handler;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static PlayerHandler getClientPlayerHandler(Player player) {
      if (player != Minecraft.getInstance().player && player != null) {
         PlayerHandler handler = INSTANCES.get(player.getUUID());
         if (handler == null) {
            handler = new PlayerHandler();
            INSTANCES.put(player.getUUID(), handler);
         }

         handler.player = player;
         return handler;
      } else {
         return getClientHandler();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static PlayerHandler getClientHandler() {
      CLIENT_INSTANCE.player = Minecraft.getInstance().player;
      return CLIENT_INSTANCE;
   }

   public static void onPlayerLoggedOut(Player player) {
      INSTANCES.remove(player.getUUID());
   }

   public static void onServerStopped() {
      INSTANCES.clear();
   }

   public void onKeyChanged(int keyState) {
      this.altKeyDown = (keyState & 1) != 0;
      this.boostKeyDown = (keyState & 2) != 0;
      this.modeSwitchKeyDown = (keyState & 4) != 0;
      this.sideInventoryKeyDown = (keyState & 8) != 0;
      this.hudModeKeyDown = (keyState & 16) != 0;
      this.toggleKeyDown = (keyState & 32) != 0;
      this.jumpKeyDown = (keyState & 64) != 0;
      this.forwardKeyDown = (keyState & 128) != 0;
      this.screenOpen = (keyState >> 31 & 1) != 0;
   }

   public boolean hasEUReader() {
      this.handle();
      return (this.flags & 1) != 0;
   }

   public boolean hasThermometer() {
      this.handle();
      return (this.flags & 2) != 0;
   }

   public boolean hasCropScanner() {
      this.handle();
      return (this.flags & 4) != 0;
   }

   private void handle() {
      long time = this.getTime();
      if (this.lastCheck <= time) {
         this.lastCheck = time + 10L;
         this.flags = StackUtil.hasHotbarItems(this.player, SpecialFilters.EU_READER, SpecialFilters.THERMOMETER, SpecialFilters.CROP_SCANNER);
      }
   }

   private long getTime() {
      return this.player == null ? -1L : this.player.level().getGameTime();
   }

   public static class GuiEntry {
      AbstractContainerMenu container;
      @OnlyIn(Dist.CLIENT)
      Screen screen;

      public GuiEntry(AbstractContainerMenu container) {
         this.container = container;
      }

      @OnlyIn(Dist.CLIENT)
      public GuiEntry(AbstractContainerMenu container, Screen screen) {
         this.container = container;
         this.screen = screen;
      }

      public AbstractContainerMenu getContainer() {
         return this.container;
      }

      @OnlyIn(Dist.CLIENT)
      public Screen getScreen() {
         return this.screen;
      }
   }
}
