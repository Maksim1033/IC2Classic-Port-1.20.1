package ic2.core;

import ic2.api.blocks.ExplosionWhitelist;
import ic2.api.items.ItemRegistries;
import ic2.api.tiles.readers.ReaderProvider;
import ic2.api.tiles.tubes.IItemCache;
import ic2.core.block.transport.item.cache.ServerItemCache;
import ic2.core.inventory.base.IHasCustomGui;
import ic2.core.inventory.base.IHasGui;
import ic2.core.platform.corehacks.mixins.server.BlockMixin;
import ic2.core.platform.corehacks.mixins.server.BlockPropertiesMixin;
import ic2.core.platform.corehacks.mixins.server.PlayerNetMixin;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.recipes.misc.MonitorRegistry;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.compat.BrewingStandHelper;
import ic2.core.utils.compat.FurnaceHelper;
import ic2.core.utils.compat.MobSpawnerHelper;
import ic2.core.wiki.base.managers.WikiBuilder;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Platform {
   List<ResourceLocation> customLang = CollectionUtils.<ResourceLocation>createList();

   public void preInit() {
      MonitorRegistry.INSTANCE.init();
      IItemCache.CACHE.set(new ServerItemCache(), true);
   }

   public void init() {
      this.setResistance(Blocks.OBSIDIAN, 60.0F);
      this.setResistance(Blocks.ENCHANTING_TABLE, 60.0F);
      this.setResistance(Blocks.ENDER_CHEST, 60.0F);
      this.setResistance(Blocks.ANVIL, 60.0F);
      this.setResistance(Blocks.CHIPPED_ANVIL, 60.0F);
      this.setResistance(Blocks.DAMAGED_ANVIL, 60.0F);
      this.setResistance(Blocks.WATER, 30.0F);
      this.setResistance(Blocks.LAVA, 30.0F);
      ExplosionWhitelist.addWhitelist(IC2Blocks.IRIDIUM_STONE, Blocks.BEDROCK);
      ItemRegistries.registerMetalArmor((S, P, E) -> true, Items.NETHERITE_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS);
      ReaderProvider.registerActiveProvider(AbstractFurnaceBlockEntity.class, FurnaceHelper::new);
      ReaderProvider.registerActiveProvider(SpawnerBlockEntity.class, MobSpawnerHelper::new);
      ReaderProvider.registerActiveProvider(BrewingStandBlockEntity.class, BrewingStandHelper::new);
      ReaderProvider.registerFuelProvider(AbstractFurnaceBlockEntity.class, FurnaceHelper::new);
      ReaderProvider.registerFuelProvider(BrewingStandBlockEntity.class, BrewingStandHelper::new);
      ReaderProvider.registerProgressProvider(AbstractFurnaceBlockEntity.class, FurnaceHelper::new);
      ReaderProvider.registerProgressProvider(SpawnerBlockEntity.class, MobSpawnerHelper::new);
      ReaderProvider.registerProgressProvider(BrewingStandBlockEntity.class, BrewingStandHelper::new);
   }

   public void onClientLoad() {
   }

   public void sendPlayerSpecificConfig() {
   }

   public void displayError(String string) {
      IC2.LOGGER.error(string);
      ServerLifecycleHooks.handleExit(0);
   }

   public Player getClientPlayerInstance() {
      return null;
   }

   public RecipeManager getRecipes() {
      return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
   }

   public Registry<Biome> getBiomeRegistry(Level level) {
      return level != null ? level.registryAccess().registryOrThrow(Registries.BIOME) : null;
   }

   public RegistryAccess getRegistryAccess() {
      MinecraftServer server = this.getServer();
      return server != null ? server.registryAccess() : RegistryAccess.EMPTY;
   }

   public final BlockEntity getTileEntity(ResourceKey<Level> dim, BlockPos pos, boolean optional) {
      Level world = this.getWorld(dim);
      return world == null || optional && !world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) ? null : world.getBlockEntity(pos);
   }

   public final Entity getEntity(ResourceKey<Level> dim, int id) {
      Level world = this.getWorld(dim);
      return world == null ? null : world.getEntity(id);
   }

   public boolean isOp(UUID owner) {
      Player player = this.getServer().getPlayerList().getPlayer(owner);
      return player == null ? false : this.getServer().getProfilePermissions(player.getGameProfile()) >= this.getServer().getOperatorUserPermissionLevel();
   }

   public boolean isSimulating() {
      return EffectiveSide.get().isServer();
   }

   public boolean isRendering() {
      return EffectiveSide.get().isClient();
   }

   public Level getWorld(ResourceKey<Level> dim) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      return server != null && dim != null ? server.getLevel(dim) : null;
   }

   public MinecraftServer getServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   public IClientItemExtensions getTileRenderer() {
      return IClientItemExtensions.DEFAULT;
   }

   public boolean launchGui(Player player, IHasCustomGui gui) {
      if (player instanceof ServerPlayer server && gui != null && gui.hasGui(player)) {
         PlayerHandler.getHandler(player).customGui = gui;
         server.nextContainerCounter();
         server.doCloseContainer();
         IC2.NETWORKING.get(true).sendCustomUIPacket(player, gui, server.containerCounter);
         player.containerMenu = gui.createContainer(player, server.containerCounter);
         server.initMenu(player.containerMenu);
         return true;
      }

      return false;
   }

   public boolean launchGuiClient(Player player, IHasCustomGui gui, int windowID) {
      return false;
   }

   public boolean launchGui(Player player, InteractionHand hand, Direction side, IHasGui gui) {
      if (player instanceof ServerPlayer server && gui != null && gui.hasGui(player, hand, side)) {
         server.nextContainerCounter();
         server.doCloseContainer();
         IC2.NETWORKING.get(true).sendUIPacket(player, hand, side, gui, server.containerCounter);
         player.containerMenu = gui.createContainer(player, hand, side, server.containerCounter);
         server.initMenu(player.containerMenu);
         return true;
      }

      return false;
   }

   public boolean launchGuiClient(Player player, InteractionHand hand, Direction side, IHasGui gui, int windowID) {
      return false;
   }

   public void markBlockForRenderUpdate(BlockPos pos) {
   }

   public Path getConfigFolder() {
      return FMLPaths.CONFIGDIR.get();
   }

   public void registerWiki(WikiBuilder builder) {
   }

   public ModelData createValveData(Block block) {
      return ModelData.EMPTY;
   }

   public void resetSleep(Player player) {
      player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
   }

   public void addLangFile(ResourceLocation key) {
      this.customLang.add(key);
   }

   public void registerCustomLang(Consumer<ResourceLocation> filePrefix) {
      this.customLang.forEach(filePrefix);
   }

   public void setResistance(Block block, float newResistance) {
      ((BlockPropertiesMixin)((BlockMixin)block).getProperties()).setResistance(newResistance);
   }

   public boolean isPVP() {
      return this.getServer().isPvpAllowed();
   }

   public void resetPlayerInAirTime(Entity player) {
      if (player instanceof ServerPlayer) {
         PlayerNetMixin mixin = (PlayerNetMixin)((ServerPlayer)player).connection;
         mixin.setAboveGroundTickCount(0);
         mixin.setAboveGroundVehicleTickCount(0);
         player.getRootVehicle().getSelfAndPassengers().forEach(Entity::resetFallDistance);
      }
   }

   public boolean isInWorld() {
      return false;
   }
}
