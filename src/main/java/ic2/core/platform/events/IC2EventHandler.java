package ic2.core.platform.events;

import com.mojang.datafixers.util.Either;
import ic2.api.blocks.BlockRegistries;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.IFarmland;
import ic2.api.crops.ISubSoil;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.armor.IArmorModule;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.api.tiles.FakePlayerMachine;
import ic2.api.tiles.IMachine;
import ic2.api.tiles.tubes.IItemCache;
import ic2.core.IC2;
import ic2.core.audio.AudioManagerClient;
import ic2.core.block.cables.mointor.MonitorGrid;
import ic2.core.block.crops.CropRegistry;
import ic2.core.block.crops.PlanterPotBlock;
import ic2.core.block.machines.tiles.mv.BaseTeleporterTileEntity;
import ic2.core.block.misc.LightBlock;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.item.logic.TubeSyncManager;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.energy.EnergyNetGlobal;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.custom.FriendsHandler;
import ic2.core.inventory.custom.container.FriendsContainer;
import ic2.core.inventory.gui.IC2SoundGui;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.item.base.IC2Item;
import ic2.core.item.inv.SlotRegistry;
import ic2.core.item.renders.features.ToolBoxRenderer;
import ic2.core.item.wearable.base.IC2JetpackBase;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.item.wearable.base.IC2ShieldBase;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.OpenSlotPacket;
import ic2.core.networking.packets.client.PollContainerPacket;
import ic2.core.networking.packets.client.custom.ScrollPacket;
import ic2.core.networking.packets.debug.RequestServerData;
import ic2.core.networking.packets.server.gui.open.CloseCustomGuiPacket;
import ic2.core.platform.corehacks.mixins.client.SlotMixin;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2People;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.wind.WindManager;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.profiler.ProfilerHelper;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents;
import net.minecraftforge.client.event.ScreenEvent.Closing;
import net.minecraftforge.client.event.ScreenEvent.Init.Post;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent.Close;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.ChunkEvent.Load;
import net.minecraftforge.event.level.ChunkEvent.Unload;
import net.minecraftforge.event.level.LevelEvent.Save;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class IC2EventHandler {
   public static final IC2EventHandler INSTANCE = new IC2EventHandler();
   static ILangHelper HELPER = new ILangHelper() {
   };
   public CompoundTag NETWORK_DATA = null;
   public static IntConsumer XP_ABSORBER;

   @SubscribeEvent
   public void onTagsUpdated(TagsUpdatedEvent event) {
      BlockRegistries.reload();
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onEntitySpawn(EntityJoinLevelEvent event) {
      if (XP_ABSORBER != null && event.getEntity() instanceof ExperienceOrb xp) {
         XP_ABSORBER.accept(xp.getValue());
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onRightClick(RightClickBlock event) {
      Player player = event.getEntity();
      if (player != null && event.getUseBlock() != Result.DENY) {
         if (player.isShiftKeyDown() && !player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()) {
            boolean mainSneak = player.getMainHandItem().doesSneakBypassUse(event.getLevel(), event.getPos(), player);
            boolean secondSneak = player.getOffhandItem().doesSneakBypassUse(event.getLevel(), event.getPos(), player);
            if ((mainSneak || secondSneak) && (!mainSneak || !secondSneak)) {
               event.setUseBlock(Result.ALLOW);
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerTick(ServerTickEvent event) {
      if (event.phase != Phase.START) {
         MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
         TubeSyncManager.onTick();
         IC2.NETWORKING.get().onServerTick();
         IC2.TICK_HANDLER.onServerTick(server);
         ProfilerHelper.INSTANCE.onTickEnded();
         CropRegistry.REGISTRY.tickSeed();
      }
   }

   @SubscribeEvent
   public void onRecipesUpdated(OnDatapackSyncEvent event) {
      if (event.getPlayer() != null) {
         IC2.RECIPES.get(true).sendRecipesToPlayer(event.getPlayer());
      } else {
         BlockRegistries.reload();
         IC2.RECIPES.get(true).sendRecipes();
      }
   }

   @SubscribeEvent
   public void onServerStoppedEvent(ServerStoppingEvent event) {
      PlayerHandler.onServerStopped();
      IC2.NETWORKING.get().onServerStopped();
      IC2.TICK_HANDLER.onServerStopped();
      EnergyNetGlobal.onServerStopped();
      MonitorGrid.onServerStopped();
      FluidNet.onServerStopped();
      TubeNet.onServerStopped();
      TubeSyncManager.onServerStop();
      IItemCache.getCache().clearCache();
      BaseTeleporterTileEntity.ACTIVE_TELEPORTERS.clear();
      ProfilerHelper.INSTANCE.onServerStopped();
      MultiBlockManager.INSTANCE.onServerStopped();
   }

   @SubscribeEvent
   public void onLivingFallEvent(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack stack = player.getItemBySlot(EquipmentSlot.FEET);
         if (stack.isEmpty()) {
            return;
         }

         int fallDamage = (int)event.getDistance() - 3;
         if (fallDamage >= 8 && !this.absorbsFully(stack)) {
            if (this.damageItem(stack, fallDamage, player, event, true)) {
               event.setDistance(Math.max(0.0F, event.getDistance() - 11.0F));
            }

            return;
         }

         this.damageItem(stack, fallDamage, player, event, false);
      }
   }

   private boolean absorbsFully(ItemStack stack) {
      return stack.getItem() instanceof IC2ModularElectricArmor && ((IC2ModularElectricArmor)stack.getItem()).isFullyAbsorbingFallDamage(stack);
   }

   public boolean damageItem(ItemStack stack, int fallDamage, Player player, LivingFallEvent evt, boolean simulate) {
      Item item = stack.getItem();
      if (item == IC2Items.HAZMAT_BOOTS) {
         int armorDamage = (fallDamage + 1) / 2;
         if (armorDamage <= stack.getMaxDamage() - stack.getDamageValue() && armorDamage >= 0) {
            stack.hurtAndBreak(armorDamage, player, IC2Item.get(EquipmentSlot.FEET));
            if (!simulate) {
               evt.setCanceled(true);
            }

            return true;
         }
      } else if (item instanceof IC2ModularElectricArmor armor) {
         int armorDamage = armor.getEnergyPerDamage(stack) * fallDamage;
         if (armorDamage - ElectricItem.MANAGER.discharge(stack, armorDamage, 2147483647, true, false, simulate) == 0) {
            if (!simulate) {
               evt.setCanceled(true);
            }

            return true;
         }

         if (armorDamage > 0) {
            float reduction = (float)armorDamage / (float)armor.getEnergyPerDamage(stack);
            if (!simulate) {
               evt.setDistance(evt.getDistance() - reduction);
            }
         }
      }

      return false;
   }

   @SubscribeEvent
   public void onPlayerTickEvent(PlayerTickEvent event) {
      if (event.side != LogicalSide.CLIENT && event.phase != Phase.START) {
         PlayerHandler handler = PlayerHandler.getHandler(event.player);
         if (handler.trackedTile != null) {
            IC2.NETWORKING.get(true).updateGuiData(handler.trackedTile, event.player);
         } else if (handler.hudModeKeyDown && handler.toggleKeyDown && !(event.player.containerMenu instanceof FriendsContainer)) {
            IC2.PLATFORM.launchGui(event.player, new FriendsHandler());
         }
      }
   }

   @SubscribeEvent
   public void onPlayerDeathEvent(LivingDeathEvent event) {
      if (event.getEntity() instanceof Player player && !event.isCanceled()) {
         boolean hasFunkyJetpack = false;
         if (IC2.CURIO_PLUGIN != null) {
            CompoundTag nbt = IC2.CURIO_PLUGIN.getJetpack(player).getTag();
            if (nbt != null) {
               hasFunkyJetpack = nbt.getBoolean("SpecialMode");
            }
         }

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != Type.HAND) {
               ItemStack stack = player.getItemBySlot(slot);
               CompoundTag nbt = stack.getTag();
               if (stack.getItem() instanceof IC2JetpackBase && nbt != null) {
                  hasFunkyJetpack |= nbt.getBoolean("SpecialMode");
               } else if (stack.getItem() instanceof IC2ModularElectricArmor && nbt != null) {
                  hasFunkyJetpack |= nbt.getCompound("jetpack_data").getBoolean("SpecialMode");
               }
            }
         }

         if (hasFunkyJetpack && player.getPersistentData().getBoolean("SpecialMovement")) {
            IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:jetpack_kill"));
            player.getPersistentData().remove("SpecialMovement");
            player.getPersistentData().remove("SpecialMovementTicker");
         }
      }
   }

   @SubscribeEvent
   public void onArmorHit(LivingAttackEvent event) {
      if (event.getSource().is(DamageTypes.LAVA)) {
         LivingEntity living = event.getEntity();

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == Type.ARMOR) {
               ItemStack stack = living.getItemBySlot(slot);
               if (stack.getItem() instanceof IArmorModule.IArmorModuleHolder
                  && IC2ModularElectricArmor.getValue(stack, IArmorModule.ArmorMod.NO_LAVA_DAMAGE, 0) > 0) {
                  event.setCanceled(true);
                  return;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onDimensionSwitch(PlayerChangedDimensionEvent event) {
      Player player = event.getEntity();

      for (ItemStack stack : player.getArmorSlots()) {
         if (stack.getItem() instanceof IArmorModule.IArmorModuleHolder) {
            ((IArmorModule.IArmorModuleHolder)stack.getItem()).onEquipmentStateChanged(stack, true, player);
         }
      }
   }

   @SubscribeEvent
   public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack stack = event.getFrom();
         if (stack.getItem() instanceof IArmorModule.IArmorModuleHolder) {
            ((IArmorModule.IArmorModuleHolder)stack.getItem()).onEquipmentStateChanged(stack, false, player);
         }

         stack = event.getTo();
         if (stack.getItem() instanceof IArmorModule.IArmorModuleHolder) {
            ((IArmorModule.IArmorModuleHolder)stack.getItem()).onEquipmentStateChanged(stack, true, player);
         }
      }
   }

   @SubscribeEvent
   public void onContainerClosed(Close close) {
      PlayerHandler handler = PlayerHandler.getHandler(close.getEntity());
      handler.trackedTile = null;
      if (handler.customGui != null) {
         handler.customGui = null;
         PacketManager.INSTANCE.sendToPlayer(new CloseCustomGuiPacket(), close.getEntity());
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onClientGuiClosed(Closing gui) {
      PlayerHandler handler = PlayerHandler.getClientHandler();
      if (!handler.cachedGUIs.isEmpty() && handler.cachedGUIs.peek().getScreen() != gui.getScreen()) {
         PacketManager.INSTANCE.sendToServer(new PollContainerPacket());
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onGuiInit(Post event) {
      if (event.getScreen() instanceof SoundOptionsScreen sound) {
         int i = 11;
         event.addListener(
            new ExtendedButton(
               sound.width / 2 - 155 + i % 2 * 160,
               sound.height / 6 - 22 + 24 * (i >> 1),
               150,
               20,
               Component.literal("IC2 Classic Audio"),
               T -> Minecraft.getInstance().setScreen(new IC2SoundGui(sound))
            )
         );
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent handler) {
      FriendManager.getServerFriends().sendDataToPlayer(handler.getEntity());
      ItemStack stack = new ItemStack(IC2Items.WIKI_ITEM);
      if (!stack.is(IC2Tags.WIKI_SPAWN)) {
         Player player = handler.getEntity();
         CompoundTag nbt = player.getPersistentData().getCompound("PlayerPersisted");
         if (!nbt.getBoolean("ic2_wiki") && player.getInventory().add(stack)) {
            nbt.putBoolean("ic2_wiki", true);
            player.getPersistentData().put("PlayerPersisted", nbt);
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerLoggedOutEvent handler) {
      PlayerHandler.onPlayerLoggedOut(handler.getEntity());
   }

   @SubscribeEvent
   public void onChunkLoaded(Load event) {
      if (event.getChunk() instanceof LevelChunk chunk) {
         Level world = chunk.getLevel();
         if (!(world instanceof ServerLevel)) {
            return;
         }

         LightBlock.onChunkLoaded((ServerLevel)world, chunk.getPos());
      }
   }

   @SubscribeEvent
   public void onWorldTickEvent(LevelTickEvent event) {
      if (event.side != LogicalSide.CLIENT) {
         if (event.phase == Phase.START) {
            EnergyNetGlobal.onWorldTickStart(event.level);
         } else {
            IC2.NETWORKING.get().onWorldTick(event.level);
            EnergyNetGlobal.onWorldTickStop(event.level);
            FluidNet.onTick(event.level);
            TubeNet.onTick(event.level);
            IC2.TICK_HANDLER.onWorldTick(event.level);
            WindManager.INSTANCE.onTick(event.level);
         }
      }
   }

   @SubscribeEvent
   public void onWorldSave(Save event) {
      Level world = (Level)event.getLevel();
      if (!world.isClientSide) {
         ChunkSaver.getSaver(world).onSave(world);
      }
   }

   @SubscribeEvent
   public void onChunkUnload(Unload event) {
      if (event.getChunk() instanceof LevelChunk level && !level.getLevel().isClientSide && ChunkSaver.getSaver(level.getLevel()).shouldSave(level.getPos())) {
         level.setUnsaved(true);
      }
   }

   @SubscribeEvent
   public void onWorldUnload(net.minecraftforge.event.level.LevelEvent.Unload event) {
      Level world = (Level)event.getLevel();
      if (!world.isClientSide) {
         IC2.NETWORKING.get().onWorldUnload(world);
         IC2.TICK_HANDLER.onWorldUnload(world);
         EnergyNetGlobal.onWorldUnload(world);
         TubeSyncManager.onWorldUnload(world);
         FluidNet.onWorldUnload(world);
         TubeNet.onWorldUnload(world);
         WindManager.INSTANCE.onWorldUnload(world);
         ChunkSaver.onWorldUnload(world);
         MonitorGrid.onWorldUnloaded(world);
         ProfilerHelper.INSTANCE.onWorldUnload(world);
      }
   }

   public Slot findSlot(ItemStack stack, AbstractContainerMenu container) {
      for (Slot slot : container.slots) {
         if (slot.getItem() == stack) {
            return slot;
         }
      }

      return null;
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onEUToolTip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      Component tooltip = ElectricItem.MANAGER.getToolTip(stack);
      int start = 1;
      if (tooltip != null) {
         event.getToolTip().add(1, tooltip);
         start++;
      }

      if (IC2.CONFIG.showUndamagedDurability.get() && event.getFlags().isAdvanced() && stack.isDamageableItem() && !stack.isDamaged()) {
         event.getToolTip()
            .add(
               event.getToolTip().size() - (stack.hasTag() ? 2 : 1),
               Component.translatable("tooltip.item.ic2.armor_durability", new Object[]{stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()})
                  .withStyle(ChatFormatting.GRAY)
            );
      }

      if (Screen.hasControlDown() && stack.hasTag() && IC2.CONFIG.itemNBT.get()) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();

         try {
            NbtIo.write(stack.getTag(), new DataOutputStream(out));
         } catch (Exception var28) {
         }

         if (out.size() > 10000) {
            event.getToolTip().add(Component.literal("NBTSize: " + out.size() / 1000 + " KB").withStyle(ChatFormatting.GOLD));
         } else {
            event.getToolTip().add(Component.literal("NBTSize: " + out.size() + " Bytes").withStyle(ChatFormatting.GOLD));
         }

         Logger logger = (Logger)LogManager.getLogger(CompoundTag.class);
         org.apache.logging.log4j.Level before = logger.getLevel();
         if (Screen.hasShiftDown()) {
            event.getToolTip().addAll(NBTUtils.convertNBT(stack.getTag()));
         } else {
            event.getToolTip().add(new TextComponentTagVisitor("", 0).visit(stack.getTag()));
         }

         if (this.NETWORK_DATA != null) {
            event.getToolTip().add(Component.literal(""));
            event.getToolTip().add(Component.literal("Server Data: "));
            if (Screen.hasShiftDown()) {
               event.getToolTip().addAll(NBTUtils.convertNBT(this.NETWORK_DATA));
            } else {
               event.getToolTip().add(new TextComponentTagVisitor("", 0).visit(this.NETWORK_DATA));
            }
         }

         logger.setLevel(before);
         if (event.getEntity() != null && IC2People.isDevTeam(event.getEntity().getUUID())) {
            Slot slot = this.findSlot(stack, event.getEntity().containerMenu);
            if (slot != null && slot.index >= 0) {
               PacketManager.INSTANCE.sendToServer(new RequestServerData(slot.index));
            }
         }
      }

      if (Screen.hasAltDown()) {
         stack.getTags().forEach(T -> event.getToolTip().add(Component.literal(T.location().toString()).withStyle(ChatFormatting.GRAY)));
      }

      ToolTipHelper helper = new ToolTipHelper(event.getToolTip());
      helper.setEnd(start);
      helper.setAddIndex(start);
      Block block = Block.byItem(stack.getItem());
      if (stack.getItem() instanceof IAdvancedTooltip) {
         ((IAdvancedTooltip)stack.getItem()).addToolTip(stack, event.getEntity(), event.getFlags(), helper);
      } else if (block instanceof IAdvancedTooltip) {
         ((IAdvancedTooltip)block).addToolTip(stack, event.getEntity(), event.getFlags(), helper);
      }

      if (block != Blocks.AIR && IC2.CONFIG.showCropInfo.get()) {
         IFarmland farmland = ICropRegistry.INSTANCE.getFarmland(block);
         BlockState theDefault = farmland != null && farmland.isSpecial() ? farmland.getSpecialState(block) : block.defaultBlockState();
         boolean ignore = false;
         if (block instanceof PlanterPotBlock) {
            block = PlanterPotBlock.getId(stack);
            farmland = ICropRegistry.INSTANCE.getFarmland(block);
            theDefault = farmland != null && farmland.isSpecial() ? farmland.getSpecialState(block) : block.defaultBlockState();
            ignore = true;
         }

         if (farmland != null) {
            helper.addSimpleToolTip(
               "info.crop.ic2.data.info.block.farmland",
               Component.translatable("info.crop.ic2.data.qualities.humidity", new Object[]{farmland.getHumidity(theDefault)}).withStyle(ChatFormatting.AQUA),
               Component.translatable("info.crop.ic2.data.qualities.nutrients", new Object[]{farmland.getNutrients(theDefault)})
                  .withStyle(ChatFormatting.GREEN)
            );
         }

         ISubSoil soil = ICropRegistry.INSTANCE.getSubSoil(block);
         theDefault = soil != null && soil.isSpecial() ? soil.getSpecialState(block) : block.defaultBlockState();
         if (soil != null && !ignore) {
            helper.addSimpleToolTip(
               "info.crop.ic2.data.info.block.subsoil",
               Component.translatable("info.crop.ic2.data.qualities.humidity", new Object[]{soil.getHumidity(theDefault)}).withStyle(ChatFormatting.AQUA),
               Component.translatable("info.crop.ic2.data.qualities.nutrients", new Object[]{soil.getNutrients(theDefault)}).withStyle(ChatFormatting.GREEN)
            );
         }
      }

      if (PlayerHandler.getClientHandler().hasEUReader()) {
         if (stack.getItem() instanceof IElectricItem item) {
            event.getToolTip().add(start++, HELPER.translate("misc.ic2.eu_transfer", item.getTransferLimit(stack)).withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(start++, HELPER.translate("misc.ic2.tier", item.getTier(stack)).withStyle(ChatFormatting.GRAY));
            helper.setEnd(start);
            helper.setAddIndex(start);
         }

         if (stack.getItem() instanceof IUpgradeItem item) {
            ItemStack copy = stack.copy();
            IMachine machine = new FakePlayerMachine(event.getEntity());
            Screen screen = Minecraft.getInstance().screen;

            try {
               IUpgradeItem.UpgradeType type = item.getType(copy);
               if (type != IUpgradeItem.UpgradeType.REDSTONE_MOD && type != IUpgradeItem.UpgradeType.AUDIO_MOD) {
                  int size = Screen.hasControlDown() ? copy.getCount() : 1;
                  int extraSpeed = 0;
                  double speedMultiplier = 1.0;
                  int extraTime = 0;
                  double timeMultiplier = 1.0;
                  int extraUsage = 0;
                  double usageMultiplier = 1.0;
                  int extraEnergy = 0;
                  double energyMultiplier = 1.0;
                  int extraTier = 0;
                  item.onInstall(copy, machine);
                  extraSpeed += item.getExtraProcessingSpeed(copy, machine) * size;
                  speedMultiplier *= Math.pow(item.getProcessingSpeedMultiplier(copy, machine), (double)size);
                  extraTime += item.getExtraProcessingTime(copy, machine) * size;
                  timeMultiplier *= Math.pow(item.getProcessingTimeMultiplier(copy, machine), (double)size);
                  extraUsage += item.getExtraEnergyDemand(copy, machine) * size;
                  usageMultiplier *= Math.pow(item.getEnergyDemandMultiplier(copy, machine), (double)size);
                  extraEnergy += item.getExtraEnergyStorage(copy, machine) * size;
                  energyMultiplier *= Math.pow(item.getEnergyStorageMultiplier(copy, machine), (double)size);
                  extraTier += item.getExtraTier(copy, machine) * size;
                  if (extraSpeed != 0) {
                     event.getToolTip().add(HELPER.translate("tooltip.item.ic2.upgrade.speed.add", extraSpeed).withStyle(ChatFormatting.GRAY));
                  }

                  if (speedMultiplier != 1.0) {
                     event.getToolTip()
                        .add(
                           HELPER.translate("tooltip.item.ic2.upgrade.speed.mul", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((speedMultiplier - 1.0) * 100.0))
                              .withStyle(ChatFormatting.GRAY)
                        );
                  }

                  if (extraTime != 0) {
                     event.getToolTip().add(HELPER.translate("tooltip.item.ic2.upgrade.time.add", extraTime).withStyle(ChatFormatting.GRAY));
                  }

                  if (timeMultiplier != 1.0) {
                     event.getToolTip()
                        .add(
                           HELPER.translate("tooltip.item.ic2.upgrade.time.mul", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((timeMultiplier - 1.0) * 100.0))
                              .withStyle(ChatFormatting.GRAY)
                        );
                  }

                  if (extraUsage != 0) {
                     event.getToolTip().add(HELPER.translate("tooltip.item.ic2.upgrade.usage.add", extraUsage).withStyle(ChatFormatting.GRAY));
                  }

                  if (usageMultiplier != 1.0) {
                     event.getToolTip()
                        .add(
                           HELPER.translate("tooltip.item.ic2.upgrade.usage.mul", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((usageMultiplier - 1.0) * 100.0))
                              .withStyle(ChatFormatting.GRAY)
                        );
                  }

                  if (extraEnergy != 0) {
                     event.getToolTip().add(HELPER.translate("tooltip.item.ic2.upgrade.storage.add", extraEnergy).withStyle(ChatFormatting.GRAY));
                  }

                  if (energyMultiplier != 1.0) {
                     event.getToolTip()
                        .add(
                           HELPER.translate(
                                 "tooltip.item.ic2.upgrade.storage.mul", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((energyMultiplier - 1.0) * 100.0)
                              )
                              .withStyle(ChatFormatting.GRAY)
                        );
                  }

                  if (extraTier != 0) {
                     event.getToolTip()
                        .add(HELPER.translate("tooltip.item.ic2.upgrade.tier.add", (extraTier > 0 ? "+" : "") + extraTier).withStyle(ChatFormatting.GRAY));
                  }

                  if (extraTier > 0
                     && screen instanceof AbstractContainerScreen
                     && ((AbstractContainerScreen)screen).getSlotUnderMouse() instanceof UpgradeSlot) {
                     helper.addKeybindingTooltip(HELPER.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.ic2.release_upgrade"));
                  }
               }
            } catch (Exception var27) {
            }
         }
      } else if (stack.getItem() instanceof IUpgradeItem item) {
         try {
            if (item.getExtraTier(stack, new FakePlayerMachine(event.getEntity())) > 0) {
               Screen screen = Minecraft.getInstance().screen;
               if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen)screen).getSlotUnderMouse() instanceof UpgradeSlot) {
                  helper.addKeybindingTooltip(HELPER.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.ic2.release_upgrade"));
               }
            }
         } catch (Exception var26) {
         }
      }

      if (stack.getItem() instanceof IHasHeldSlotInventory && ((IHasHeldSlotInventory)stack.getItem()).hasInventory(stack)) {
         helper.addKeybindingTooltip(HELPER.buildKeyDescription(KeyHelper.SIDE_INV_KEY, "misc.ic2.gui_open"));
      }

      helper.applyKeyInfo();
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   @OnlyIn(Dist.CLIENT)
   public void fixTooltipLimits(GatherComponents event) {
      List<Either<FormattedText, TooltipComponent>> result = new ObjectArrayList();

      for (Either<FormattedText, TooltipComponent> entry : event.getTooltipElements()) {
         if (entry.right().isPresent()) {
            result.add(entry);
         } else {
            for (FormattedText text : Minecraft.getInstance().font.getSplitter().splitLines((FormattedText)entry.left().get(), 2147483647, Style.EMPTY)) {
               result.add(Either.left(text));
            }
         }
      }

      if (result.size() != event.getTooltipElements().size()) {
         event.getTooltipElements().clear();
         event.getTooltipElements().addAll(result);
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.START) {
         ((AudioManagerClient)IC2.AUDIO).onTick();
      } else {
         IC2.TICK_HANDLER.onClientTick();
         ((AudioManagerClient)IC2.AUDIO).onPostTick();
         IC2.KEYBOARD.sendKeyUpdate();
         IItemCache.getCache().updateCache();
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onKeyPress(Key event) {
      if (event.getKey() == IC2.KEYBOARD.getKey(KeyHelper.SIDE_INV_KEY) && event.getAction() == 1) {
         Screen screen = Minecraft.getInstance().screen;
         if (screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            if (screen instanceof CreativeModeInventoryScreen container) {
               Slot slot = container.getSlotUnderMouse();
               if (slot != null && slot instanceof SlotMixin) {
                  PacketManager.INSTANCE.sendToServer(new OpenSlotPacket(-(slot.index + 1)));
               }

               return;
            }

            if (abstractContainerScreen.getSlotUnderMouse() != null) {
               Slot slot = abstractContainerScreen.getSlotUnderMouse();
               ItemStack stack = slot.getItem();
               if (!(stack.getItem() instanceof IHasHeldSlotInventory)
                  || !((IHasHeldSlotInventory)stack.getItem()).hasInventory(stack)
                  || !slot.mayPlace(stack)
                  || SlotRegistry.INSTANCE.isInvalidSlot(abstractContainerScreen.getMenu(), slot)
                  || stack.getCount() > 1) {
                  return;
               }

               PacketManager.INSTANCE.sendToServer(new OpenSlotPacket(slot.index));
            }
         }
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onPlayerLoggedIn(LoggingIn event) {
      IItemCache.getCache().clearCache();
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onScroll(MouseScrollingEvent event) {
      if (ToolBoxRenderer.INSTANCE.onScrollToolbox()) {
         event.setCanceled(true);
         PacketManager.INSTANCE.sendToServer(new ScrollPacket(event.getScrollDelta() > 0.0));
      }
   }

   public void onServerScroll(Player owner, boolean result) {
      ToolBoxRenderer.INSTANCE.onServerScrollToolbox(owner, result);
   }

   @SubscribeEvent
   public void onShieldBlock(ShieldBlockEvent event) {
      LivingEntity entity = event.getEntity();
      ItemStack active = entity.getUseItem();
      if (active.getItem() instanceof IC2ShieldBase shield) {
         shield.damageShield(active, 1 + Mth.floor(event.getBlockedDamage()), entity);
         event.setShieldTakesDamage(false);
      }
   }
}
