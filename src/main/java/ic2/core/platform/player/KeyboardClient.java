package ic2.core.platform.player;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import ic2.core.IC2;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.KeyPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyboardClient extends Keyboard {
   public KeyMapping altKey;
   public KeyMapping boostKey;
   public KeyMapping modeSwitchKey;
   public KeyMapping sideInventoryKey;
   public KeyMapping expandInfoKey;
   public KeyMapping toggleKey;
   private KeyMapping[] array;
   Minecraft mc;
   Window monitor;
   boolean enableGui;
   int lastKey = 0;

   @Override
   public void init() {
      this.altKey = new KeyMapping("key.ic2.alt", 342, "key.ic2.keys");
      this.boostKey = new KeyMapping("key.ic2.boost", 341, "key.ic2.keys");
      this.modeSwitchKey = new KeyMapping("key.ic2.mode", 77, "key.ic2.keys");
      this.sideInventoryKey = new KeyMapping("key.ic2.inv", 67, "key.ic2.keys");
      this.expandInfoKey = new KeyMapping("key.ic2.hud", 88, "key.ic2.keys");
      this.toggleKey = new KeyMapping("key.ic2.toggle", 86, "key.ic2.keys");
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeys);
      this.mc = Minecraft.getInstance();
      this.monitor = this.mc.getWindow();
      Options setting = this.mc.options;
      this.array = new KeyMapping[]{
         this.altKey, this.boostKey, this.modeSwitchKey, this.sideInventoryKey, this.expandInfoKey, this.toggleKey, setting.keyJump, setting.keyUp
      };
      this.enableGui = true;
   }

   public void registerKeys(RegisterKeyMappingsEvent event) {
      event.register(this.altKey);
      event.register(this.boostKey);
      event.register(this.modeSwitchKey);
      event.register(this.sideInventoryKey);
      event.register(this.expandInfoKey);
      event.register(this.toggleKey);
   }

   @Override
   public MutableComponent getKeyName(KeyHelper helper) {
      switch (helper.getIndex()) {
         case 0:
            return (MutableComponent)this.altKey.getTranslatedKeyMessage();
         case 1:
            return (MutableComponent)this.boostKey.getTranslatedKeyMessage();
         case 2:
            return (MutableComponent)this.modeSwitchKey.getTranslatedKeyMessage();
         case 3:
            return (MutableComponent)this.sideInventoryKey.getTranslatedKeyMessage();
         case 4:
            return (MutableComponent)this.expandInfoKey.getTranslatedKeyMessage();
         case 5:
            return (MutableComponent)this.toggleKey.getTranslatedKeyMessage();
         case 6:
            return (MutableComponent)this.mc.options.keyJump.getTranslatedKeyMessage();
         case 7:
            return (MutableComponent)this.mc.options.keyUp.getTranslatedKeyMessage();
         case 8:
            return (MutableComponent)this.mc.options.keyDown.getTranslatedKeyMessage();
         case 9:
            return (MutableComponent)this.mc.options.keyShift.getTranslatedKeyMessage();
         case 10:
            return (MutableComponent)this.mc.options.keyUse.getTranslatedKeyMessage();
         default:
            return super.getKeyName(helper);
      }
   }

   @Override
   public int getKey(KeyHelper helper) {
      switch (helper.getIndex()) {
         case 0:
            return this.altKey.getKey().getValue();
         case 1:
            return this.boostKey.getKey().getValue();
         case 2:
            return this.modeSwitchKey.getKey().getValue();
         case 3:
            return this.sideInventoryKey.getKey().getValue();
         case 4:
            return this.expandInfoKey.getKey().getValue();
         case 5:
            return this.toggleKey.getKey().getValue();
         case 6:
            return this.mc.options.keyJump.getKey().getValue();
         case 7:
            return this.mc.options.keyUp.getKey().getValue();
         case 8:
            return this.mc.options.keyDown.getKey().getValue();
         case 9:
            return this.mc.options.keyShift.getKey().getValue();
         case 10:
            return this.mc.options.keyUse.getKey().getValue();
         default:
            return -1;
      }
   }

   @Override
   public void sendKeyUpdate() {
      int binding = 0;
      if ((this.mc.screen == null || this.enableGui) && !(this.mc.screen instanceof ChatScreen)) {
         for (int i = 0; i < this.array.length; i++) {
            binding |= this.isKeyPressed(this.array[i]) ? 1 << i : 0;
         }
      }

      binding |= (this.mc.screen != null ? 1 : 0) << 31;
      if (binding != this.lastKey) {
         Player player = IC2.PLATFORM.getClientPlayerInstance();
         if (player != null) {
            PacketManager.INSTANCE.sendToServer(new KeyPacket(binding));
            this.processKeyUpdate(player, binding);
         }

         this.lastKey = binding;
      }
   }

   @Override
   public void processKeyUpdate(Player player, int keyState) {
      super.processKeyUpdate(player, keyState);
      if (player == IC2.PLATFORM.getClientPlayerInstance()) {
         PlayerHandler.getClientHandler().onKeyChanged(keyState);
      }
   }

   public boolean isKeyPressed(KeyMapping binding) {
      IKeyConflictContext context = binding.getKeyConflictContext();
      binding.setKeyConflictContext(this.enableGui ? KeyConflictContext.UNIVERSAL : KeyConflictContext.IN_GAME);
      boolean result = this.isKeyDown(binding) && binding.isConflictContextAndModifierActive();
      binding.setKeyConflictContext(context);
      return result;
   }

   private boolean isKeyDown(KeyMapping binding) {
      if (binding.isUnbound()) {
         return false;
      } else {
         Key input = binding.getKey();
         return input.getType() == Type.MOUSE
            ? GLFW.glfwGetMouseButton(this.monitor.getWindow(), input.getValue()) == 1
            : InputConstants.isKeyDown(this.monitor.getWindow(), input.getValue());
      }
   }
}
