package ic2.core;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import ic2.api.items.armor.IEnergyShieldArmor;
import ic2.api.tiles.tubes.IItemCache;
import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.IAtlasProvider;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.block.rendering.tile.AdvancedComparatorRenderer;
import ic2.core.block.rendering.tile.ChunkloaderRenderer;
import ic2.core.block.rendering.tile.EnchanterRenderer;
import ic2.core.block.rendering.tile.ExpansionRenderer;
import ic2.core.block.rendering.tile.FusionReactorRenderer;
import ic2.core.block.rendering.tile.MonitorRenderer;
import ic2.core.block.rendering.tile.PersonalChestRenderer;
import ic2.core.block.rendering.tile.PlasmaRenderer;
import ic2.core.block.rendering.tile.TankRenderer;
import ic2.core.block.rendering.tile.TubeRenderer;
import ic2.core.block.rendering.tile.WindmillRenderer;
import ic2.core.block.rendering.world.WorldOverlays;
import ic2.core.block.rendering.world.WrenchOverlay;
import ic2.core.block.rendering.world.impl.MultiBlockOverlay;
import ic2.core.block.transport.item.cache.ClientItemCache;
import ic2.core.entity.renderer.CrossRenderer;
import ic2.core.entity.renderer.EnergyShieldLayer;
import ic2.core.entity.renderer.IC2BoatRenderer;
import ic2.core.entity.renderer.IC2ExplosiveRenderer;
import ic2.core.entity.renderer.RocketMinerRenderer;
import ic2.core.entity.renderer.ThrowItemRenderer;
import ic2.core.inventory.base.IHasCustomGui;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.item.renders.IC2TileEntityStackRenderer;
import ic2.core.item.renders.features.EnergyHudRenderer;
import ic2.core.item.renders.features.ToolBoxRenderer;
import ic2.core.item.renders.tooltip.InventoryToolTipRenderer;
import ic2.core.item.renders.tooltip.InventoryTooltip;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.custom.PlayerConfigSetting;
import ic2.core.platform.corehacks.mixins.client.DebugMixin;
import ic2.core.platform.corehacks.mixins.client.rendering.RenderTypeBuffersMixin;
import ic2.core.platform.corehacks.mixins.client.rendering.WorldRenderMixin;
import ic2.core.platform.events.DebugHighlighter;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Materials;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Colors;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.PackHack;
import ic2.core.platform.rendering.events.RegisterBatchEvent;
import ic2.core.platform.rendering.events.TilesRenderedEvent;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.base.managers.RecipeProvider;
import ic2.core.wiki.base.managers.WikiBuilder;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class PlatformClient extends Platform implements ResourceManagerReloadListener {
   Minecraft minecraft = Minecraft.getInstance();
   List<WikiBuilder> builders = CollectionUtils.<WikiBuilder>createList();
   int cooldown;

   @Override
   public void preInit() {
      super.preInit();
      IItemCache.CACHE.set(new ClientItemCache(), false);
      PackHack.doResourcePackHack();
      IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
      bus.register(IC2Textures.INSTANCE);
      bus.register(IC2Models.INSTANCE);
      bus.addListener(this::addLayers);
      bus.addListener(this::registerColors);
      bus.addListener(this::registerTooltip);
      bus.addListener(this::registerOverlays);
      MinecraftForge.EVENT_BUS.register(WorldOverlays.INSTANCE);
      MinecraftForge.EVENT_BUS.register(WrenchOverlay.INSTANCE);
      MinecraftForge.EVENT_BUS.register(MultiBlockOverlay.INSTANCE);
      WorldOverlays.INSTANCE.init();
   }

   @Override
   public void onClientLoad() {
      IC2Materials.ALL_TYPES.forEach(Sheets::addWoodType);
      ((ReloadableResourceManager)this.minecraft.getResourceManager()).registerReloadListener(this);

      for (Block block : ForgeRegistries.BLOCKS) {
         if (block instanceof IRenderType type) {
            ItemBlockRenderTypes.setRenderLayer(block, T -> type.canRenderInLayer(block.defaultBlockState(), T));
         }

         if (block instanceof IAtlasProvider) {
            IC2Textures.INSTANCE.registerRenderMaterial(((IAtlasProvider)block).createMaterial());
         }
      }

      for (Item item : ForgeRegistries.ITEMS) {
         if (item instanceof IAtlasProvider) {
            IC2Textures.INSTANCE.registerRenderMaterial(((IAtlasProvider)item).createMaterial());
         }
      }
   }

   @Override
   public void sendPlayerSpecificConfig() {
      if (this.minecraft.player != null) {
         boolean sprint = IC2.CONFIG.boostOnSprint.get();
         PlayerHandler.getHandler(this.minecraft.player).doSpringBoost = sprint;
         PacketManager.INSTANCE.sendToServer(new PlayerConfigSetting(sprint));
      }
   }

   @SubscribeEvent
   public void onDebugKey(Key event) {
      long window = this.minecraft.getWindow().getWindow();
      if (this.cooldown <= 0 && InputConstants.isKeyDown(window, 292) && InputConstants.isKeyDown(window, 82) && this.minecraft.screen == null) {
         this.minecraft
            .player
            .displayClientMessage(
               Component.literal("[Debug]: ")
                  .withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD})
                  .append(Component.literal("Reloading IC2C Wikis").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false))),
               false
            );
         ((DebugMixin)this.minecraft.keyboardHandler).setDebugHandled(true);
         this.minecraft.getLanguageManager().onResourceManagerReload(this.minecraft.getResourceManager());
         int i = 0;

         while (i < this.builders.size()) {
            this.builders.get(i++).build();
         }

         this.cooldown = 5;
      }
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END && this.cooldown > 0) {
         this.cooldown--;
         if (this.cooldown <= 0) {
            ((DebugMixin)this.minecraft.keyboardHandler).setDebugHandled(true);
            this.minecraft.getLanguageManager().onResourceManagerReload(this.minecraft.getResourceManager());
            this.onResourceManagerReload(null);
            this.minecraft
               .player
               .displayClientMessage(
                  Component.literal("[Debug]: ")
                     .withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD})
                     .append(Component.literal("Reloaded IC2C Wikis").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false))),
                  false
               );
         }
      }
   }

   private void registerColors(RegisterColorHandlersEvent event) {
      if (event instanceof net.minecraftforge.client.event.RegisterColorHandlersEvent.Block block) {
         block.register(IC2Colors.INSTANCE, IC2Blocks.COLORABLE.toArray(Block[]::new));
      } else if (event instanceof net.minecraftforge.client.event.RegisterColorHandlersEvent.Item item) {
         item.register(IC2Colors.INSTANCE, (ItemLike[])IC2Items.COLORABLE.toArray(Item[]::new));
      }
   }

   private void registerOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("ic2_overlays", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
         MultiBlockOverlay.INSTANCE.onRenderUI(guiGraphics.pose());
         EnergyHudRenderer.INSTANCE.renderOverlay(guiGraphics.pose(), screenWidth, screenHeight);
         ToolBoxRenderer.INSTANCE.renderOverlay(guiGraphics, screenWidth, screenHeight);
         DebugHighlighter.INSTANCE.onToolTip(guiGraphics.pose());
      });
   }

   private void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
      event.register(InventoryTooltip.class, tooltip -> new InventoryToolTipRenderer(tooltip.getItems()));
   }

   @Override
   public void init() {
      super.init();
      RecipeProvider.INSTANCE.init();
      EntityRenderers.register(IC2Entities.ITNT, IC2ExplosiveRenderer::new);
      EntityRenderers.register(IC2Entities.NUKE, IC2ExplosiveRenderer::new);
      EntityRenderers.register(IC2Entities.RUBBERWOOD_BOAT, T -> new IC2BoatRenderer(T, false));
      EntityRenderers.register(IC2Entities.RUBBER_BOAT, T -> new IC2BoatRenderer(T, false));
      EntityRenderers.register(IC2Entities.CARBON_BOAT, T -> new IC2BoatRenderer(T, false));
      EntityRenderers.register(IC2Entities.DYNAMITE, ThrowItemRenderer::new);
      EntityRenderers.register(IC2Entities.MINING_LASER, T -> new CrossRenderer(T, new ResourceLocation("ic2", "textures/models/misc/mining_laser_shot.png")));
      EntityRenderers.register(
         IC2Entities.TRACKING_MINING_LASER, T -> new CrossRenderer(T, new ResourceLocation("ic2", "textures/models/misc/mining_laser_shot.png"))
      );
      EntityRenderers.register(IC2Entities.ROCKET_MINER, RocketMinerRenderer::new);
      EntityRenderers.register(IC2Entities.BATBOX, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      EntityRenderers.register(IC2Entities.MFE, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      EntityRenderers.register(IC2Entities.MFSU, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      EntityRenderers.register(IC2Entities.ESU, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      EntityRenderers.register(IC2Entities.ISU, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      EntityRenderers.register(IC2Entities.PESU, T -> new MinecartRenderer(T, ModelLayers.MINECART));
      BlockEntityRenderers.register(IC2Tiles.SIMPLE_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.SPEED_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.TRANSPORT_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.VOID_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.HOVER_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.DIRECTIONAL_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.INSERTION_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.DIRECTIONAL_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.EXTRACTION_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.FILTERED_EXTRACTION_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.STACKING_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.PICKUP_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.REDSTONE_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.FILTER_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.SWITCH_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.ROUND_ROBIN_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.TELEPORT_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.DROPPING_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.DYEING_TUBE, TubeRenderer.ColorTubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.COLOR_FILTER_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.REQUEST_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.PROVIDER_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.FLUID_TUBE, TubeRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.SIGN, SignRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.CHUNKLOADER, ChunkloaderRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.ELECTRIC_ENCHANTER, EnchanterRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.PLASMAFIER, PlasmaRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.FLUID_EXPANSION, ExpansionRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.PERSONAL_CHEST, PersonalChestRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.ADVANCED_COMPARATOR, AdvancedComparatorRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.TANK, TankRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.GENERATOR_WINDMILL, WindmillRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.MONITOR, MonitorRenderer::new);
      BlockEntityRenderers.register(IC2Tiles.FUSION_REACTOR, FusionReactorRenderer::new);
      MinecraftForge.EVENT_BUS.register(this);
      IC2.EVENT_BUS.addListener(this::onBatchesCreated);
      SortedMap<RenderType, BufferBuilder> buffer = ((RenderTypeBuffersMixin)((WorldRenderMixin)this.minecraft.levelRenderer).getBuffers()).getFixedBuffers();
      IC2.EVENT_BUS.post(new RegisterBatchEvent(T -> buffer.put(T, new BufferBuilder(T.bufferSize()))));
   }

   public void addLayers(AddLayers event) {
      EntityModelSet sets = event.getEntityModels();

      for (String skin : event.getSkins()) {
         LivingEntityRenderer<Player, PlayerModel<Player>> render = event.getSkin(skin);
         render.addLayer(new EnergyShieldLayer(render, new PlayerModel(sets.bakeLayer(ModelLayers.PLAYER), false)));
      }

      this.addToClass(EntityType.SKELETON, new SkeletonModel(sets.bakeLayer(ModelLayers.SKELETON)));
      this.addToClass(EntityType.STRAY, new SkeletonModel(sets.bakeLayer(ModelLayers.SKELETON)));
      this.addToClass(EntityType.WITHER_SKELETON, new SkeletonModel(sets.bakeLayer(ModelLayers.WITHER_SKELETON)));
      this.addToClass(EntityType.ARMOR_STAND, new ArmorStandArmorModel(sets.bakeLayer(ModelLayers.ARMOR_STAND)));
      this.addToClass(EntityType.GIANT, new GiantZombieModel(sets.bakeLayer(ModelLayers.GIANT)));
      this.addToClass(EntityType.ZOMBIFIED_PIGLIN, new ZombieModel(sets.bakeLayer(ModelLayers.ZOMBIFIED_PIGLIN)));
      this.addToClass(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerModel(sets.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)));
      this.addToClass(EntityType.DROWNED, new DrownedModel(sets.bakeLayer(ModelLayers.DROWNED)));
      this.addToClass(EntityType.ZOMBIE, new ZombieModel(sets.bakeLayer(ModelLayers.ZOMBIE)));
      this.addToClass(EntityType.HUSK, new ZombieModel(sets.bakeLayer(ModelLayers.HUSK)));
   }

   private <T extends LivingEntity> void addToClass(EntityType<T> entity, HumanoidModel<T> model) {
      LivingEntityRenderer<T, HumanoidModel<T>> type = (LivingEntityRenderer<T, HumanoidModel<T>>)this.minecraft
         .getEntityRenderDispatcher()
         .renderers
         .get(entity);
      if (type != null) {
         type.addLayer(new EnergyShieldLayer(type, model));
      }
   }

   public void onBatchesCreated(RegisterBatchEvent event) {
      event.register(IC2RenderTypes.FLUID_RENDER);
      event.register(IC2RenderTypes.TUBE_ITEM_HIGHLIGHT);
      event.register(IC2RenderTypes.WINDMILL_ROTORS);
   }

   @SubscribeEvent
   public void onTilesRendered(TilesRenderedEvent event) {
      if (event.isTranslucent()) {
         event.finishType(IC2RenderTypes.FLUID_RENDER);
         event.finishType(IC2RenderTypes.TUBE_ITEM_HIGHLIGHT);
      } else {
         event.finishType(IC2RenderTypes.WINDMILL_ROTORS);
      }
   }

   @Override
   public void registerWiki(WikiBuilder builder) {
      this.builders.add(builder);
   }

   public void onResourceManagerReload(@Nullable ResourceManager p_10758_) {
      RecipeProvider.INSTANCE.clearReferences();
      int i = 0;

      while (i < this.builders.size()) {
         this.builders.get(i++).build();
      }

      if (this.minecraft.level != null) {
         RecipeProvider.INSTANCE.reloadReferences();
      }
   }

   @SubscribeEvent
   public void onEntityDamageEvent(Pre<LivingEntity, EntityModel<LivingEntity>> event) {
      LivingEntity entity = event.getEntity();
      if (entity instanceof Player && IEnergyShieldArmor.addsEnergyShieldEffect(entity)) {
         entity.hurtTime = -entity.hurtTime;
      }
   }

   @SubscribeEvent
   public void onEntityDamageEvent(Post<LivingEntity, EntityModel<LivingEntity>> event) {
      LivingEntity entity = event.getEntity();
      if (entity.hurtTime < 0) {
         entity.hurtTime = -entity.hurtTime;
      }
   }

   @Override
   public boolean launchGuiClient(Player player, IHasCustomGui gui, int windowID) {
      try {
         IC2Container container = gui.createContainer(player, windowID);
         player.containerMenu = container;
         Screen screen = gui.createGui(player, container);
         if (screen == null) {
            return false;
         } else {
            PlayerHandler.getClientHandler().customGui = gui;
            this.minecraft.setScreen(screen);
            return true;
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         return false;
      }
   }

   @Override
   public boolean launchGuiClient(Player player, InteractionHand hand, Direction side, IHasGui gui, int windowID) {
      try {
         IC2Container container = gui.createContainer(player, hand, side, windowID);
         player.containerMenu = container;
         Screen screen = gui.createGui(player, hand, side, container);
         if (screen == null) {
            return false;
         } else {
            this.minecraft.setScreen(screen);
            return true;
         }
      } catch (Exception var8) {
         IC2.LOGGER.catching(var8);
         return false;
      }
   }

   @Override
   public Player getClientPlayerInstance() {
      return this.isSimulating() ? null : this.minecraft.player;
   }

   @Override
   public boolean isOp(UUID id) {
      return id.equals(this.minecraft.player.getUUID()) && this.minecraft.player.hasPermissions(1);
   }

   @Override
   public RecipeManager getRecipes() {
      return this.isSimulating() ? super.getRecipes() : this.minecraft.player.connection.getRecipeManager();
   }

   @Override
   public Level getWorld(ResourceKey<Level> dimID) {
      if (this.isSimulating()) {
         return super.getWorld(dimID);
      } else {
         return dimID != this.minecraft.level.dimension() ? null : this.minecraft.level;
      }
   }

   @Override
   public void markBlockForRenderUpdate(BlockPos pos) {
      this.minecraft.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
   }

   @Override
   public ModelData createValveData(Block block) {
      return ModelData.builder()
         .with(
            CamouflageProperty.INSTANCE,
            (Function<RenderType, CamouflageShape.QuadResults>)T -> new CamouflageStorage(CamouflageShape.FULL_CUBE)
                  .setTexture(DirectionList.ALL, block)
                  .getQuads(block.defaultBlockState(), this.minecraft.level, BlockPos.ZERO, T)
         )
         .build();
   }

   @Override
   public boolean isInWorld() {
      return this.minecraft.level != null;
   }

   @Override
   public Registry<Biome> getBiomeRegistry(Level level) {
      return level != null ? level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME) : null;
   }

   @Override
   public RegistryAccess getRegistryAccess() {
      if (this.isSimulating()) {
         return super.getRegistryAccess();
      }
      return this.minecraft.level != null ? this.minecraft.level.registryAccess() : super.getRegistryAccess();
   }

   @Override
   public IClientItemExtensions getTileRenderer() {
      return PlatformClient.TileProperties.INSTANCE;
   }

   private static class TileProperties implements IClientItemExtensions {
      public static final IClientItemExtensions INSTANCE = new PlatformClient.TileProperties();

      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
         return IC2TileEntityStackRenderer.INSTANCE;
      }
   }
}
