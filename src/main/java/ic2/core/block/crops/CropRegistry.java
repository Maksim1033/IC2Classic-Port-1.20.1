package ic2.core.block.crops;

import ic2.api.crops.BaseSeed;
import ic2.api.crops.CropProperties;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropRenderer;
import ic2.api.crops.ICropSeed;
import ic2.api.crops.IFarmland;
import ic2.api.crops.ISubSoil;
import ic2.core.IC2;
import ic2.core.block.crops.crops.BambooCrop;
import ic2.core.block.crops.crops.BaseCrop;
import ic2.core.block.crops.crops.BeetRootCrop;
import ic2.core.block.crops.crops.BerryBushCrop;
import ic2.core.block.crops.crops.BlueWheatCrop;
import ic2.core.block.crops.crops.BoneFlowerCrop;
import ic2.core.block.crops.crops.CactusCrop;
import ic2.core.block.crops.crops.CocoaCrop;
import ic2.core.block.crops.crops.CoffeeCrop;
import ic2.core.block.crops.crops.CoralCrop;
import ic2.core.block.crops.crops.DyeFlowerCrop;
import ic2.core.block.crops.crops.FungusCrop;
import ic2.core.block.crops.crops.GlowBerryCrop;
import ic2.core.block.crops.crops.GlowshroomCrop;
import ic2.core.block.crops.crops.HempCrop;
import ic2.core.block.crops.crops.HopsCrop;
import ic2.core.block.crops.crops.InkbergineCrop;
import ic2.core.block.crops.crops.KelpCrop;
import ic2.core.block.crops.crops.LumiliaCrop;
import ic2.core.block.crops.crops.MelonCrop;
import ic2.core.block.crops.crops.MushroomCrop;
import ic2.core.block.crops.crops.NetherWartCrop;
import ic2.core.block.crops.crops.OreCrop;
import ic2.core.block.crops.crops.PumpkinCrop;
import ic2.core.block.crops.crops.RainbowFlowerCrop;
import ic2.core.block.crops.crops.RedWheatCrop;
import ic2.core.block.crops.crops.SaplingCrop;
import ic2.core.block.crops.crops.SeaGrassCrop;
import ic2.core.block.crops.crops.SeaPickleCrop;
import ic2.core.block.crops.crops.StickReedCrop;
import ic2.core.block.crops.crops.SugarCaneCrop;
import ic2.core.block.crops.crops.TeaCrop;
import ic2.core.block.crops.crops.TerraWartCrop;
import ic2.core.block.crops.crops.VegetableCrop;
import ic2.core.block.crops.crops.VenomiliaCrop;
import ic2.core.block.crops.crops.WeedCrop;
import ic2.core.block.crops.crops.WheatCrop;
import ic2.core.block.crops.renderer.DefaultCropRenderer;
import ic2.core.block.crops.renderer.StickRenderer;
import ic2.core.block.crops.soils.BaseFarmland;
import ic2.core.block.crops.soils.BaseSubSoil;
import ic2.core.block.crops.soils.Farmland;
import ic2.core.block.crops.soils.Mangrowth;
import ic2.core.block.crops.soils.PlanterPotFarmland;
import ic2.core.block.rendering.block.CropEntry;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags.Biomes;
import net.minecraftforge.common.Tags.Blocks;

public class CropRegistry extends ICropRegistry {
   public static final CropRegistry REGISTRY = new CropRegistry();
   Map<ResourceLocation, ICrop> crops = CollectionUtils.<ResourceLocation, ICrop>createLinkedMap();
   Map<ResourceLocation, ItemStack> displayItems = CollectionUtils.<ResourceLocation, ItemStack>createLinkedMap();
   Map<ResourceLocation, ICropRenderer> customRenders = CollectionUtils.<ResourceLocation, ICropRenderer>createLinkedMap();
   Object2IntMap<TagKey<Biome>> humidity = new Object2IntOpenHashMap();
   Object2IntMap<TagKey<Biome>> nutrients = new Object2IntOpenHashMap();
   Object2IntMap<TagKey<Biome>> water = new Object2IntOpenHashMap();
   Map<Block, IFarmland> farmlands = CollectionUtils.<Block, IFarmland>createMap();
   Map<Block, ISubSoil> soils = CollectionUtils.<Block, ISubSoil>createMap();
   Map<Item, BaseSeed> seeds = CollectionUtils.<Item, BaseSeed>createMap();
   Map<ResourceLocation, ItemStack> cropSeeds = CollectionUtils.<ResourceLocation, ItemStack>createMap();
   ICropRenderer stickRenderer = null;
   public static final ICrop GLOWSHROOM = new GlowshroomCrop();
   public static final ICrop NETHERWART = new NetherWartCrop();
   public static final ICrop TERRAWART = new TerraWartCrop();
   public static final ICrop SUGARCANE = new SugarCaneCrop();
   public static final ICrop STICKREED = new StickReedCrop();
   public static final ICrop BAMBOO = new BambooCrop();
   public static final ICrop WHEAT = new WheatCrop();
   public static final ICrop HOPS = new HopsCrop();
   public static final ICrop BLUEWHEAT = new BlueWheatCrop();
   public static final ICrop REDWHEAT = new RedWheatCrop();
   public static final ICrop ALLIUM = new DyeFlowerCrop("allium", DyeColor.MAGENTA, BaseCrop.MOJANG, "Magenta");
   public static final ICrop BLACKTHORN = new DyeFlowerCrop("blackthorn", DyeColor.BLACK, BaseCrop.ALBLAKA, "Black", "Rose");
   public static final ICrop BLUE_ORCHID = new DyeFlowerCrop("blue_orchid", DyeColor.LIGHT_BLUE, BaseCrop.MOJANG, "Blue");
   public static final ICrop CORNFLOWER = new DyeFlowerCrop("cornflower", DyeColor.BLUE, BaseCrop.ALBLAKA, "Blue");
   public static final ICrop DANDELION = new DyeFlowerCrop("dandelion", DyeColor.YELLOW, BaseCrop.NOTCH, "Yellow");
   public static final ICrop ORANGE_TULIP = new DyeFlowerCrop("orange_tulip", DyeColor.ORANGE, BaseCrop.MOJANG, "Orange", "Tulip");
   public static final ICrop OXEYE_DAISY = new DyeFlowerCrop("oxeye_daisy", DyeColor.WHITE, BaseCrop.RAZZOKK, "White", "Rose");
   public static final ICrop PINK_TULIP = new DyeFlowerCrop("pink_tulip", DyeColor.PINK, BaseCrop.MOJANG, "Pink", "Tulip");
   public static final ICrop POPPY = new DyeFlowerCrop("poppy", DyeColor.RED, BaseCrop.NOTCH, "Red", "Rose");
   public static final ICrop PURPLE_TULIP = new DyeFlowerCrop("purple_tulip", DyeColor.PURPLE, BaseCrop.ALBLAKA, "Purple", "Tulip");
   public static final ICrop AZURE_BLUET = new DyeFlowerCrop("azure_bluet", DyeColor.LIGHT_GRAY, BaseCrop.MOJANG, "Gray");
   public static final ICrop LILY_OF_THE_VALLEY = new DyeFlowerCrop("lily_of_the_valley", DyeColor.LIGHT_GRAY, BaseCrop.MOJANG, "Gray");
   public static final ICrop FORGET_ME_NOT = new DyeFlowerCrop("forget_me_not", DyeColor.CYAN, BaseCrop.NANO_HEART, "Cyan");
   public static final ICrop RED_TULIP = new DyeFlowerCrop("red_tulip", DyeColor.RED, BaseCrop.MOJANG, "Red", "Tulip");
   public static final ICrop WHITE_TULIP = new DyeFlowerCrop("white_tulip", DyeColor.LIGHT_GRAY, BaseCrop.MOJANG, "White", "Tulip");
   public static final ICrop VENOMILIA = new VenomiliaCrop();
   public static final ICrop RAINBOW_FLOWER = new RainbowFlowerCrop();
   public static ICrop BRAIN_CORAL;
   public static ICrop BUBBLE_CORAL;
   public static ICrop FIRE_CORAL;
   public static ICrop HORN_CORAL;
   public static ICrop TUBE_CORAL;
   public static final ICrop SEA_PICKLE = new SeaPickleCrop();
   public static final ICrop KELP = new KelpCrop();
   public static final ICrop BONE_FLOWER = new BoneFlowerCrop();
   public static final ICrop INKBERGINE = new InkbergineCrop();
   public static final ICrop ACACIA_SAPLING = new SaplingCrop("acacia", Items.ACACIA_LOG, Items.ACACIA_SAPLING);
   public static final ICrop BIRCH_SAPLING = new SaplingCrop("birch", Items.BIRCH_LOG, Items.BIRCH_SAPLING);
   public static final ICrop DARK_OAK_SAPLING = new SaplingCrop("dark_oak", Items.DARK_OAK_LOG, Items.DARK_OAK_SAPLING);
   public static final ICrop OAK_SAPLING = new SaplingCrop("oak", Items.OAK_LOG, Items.OAK_SAPLING);
   public static final ICrop JUNGLE_SAPLING = new SaplingCrop("jungle", Items.JUNGLE_LOG, Items.JUNGLE_SAPLING);
   public static final ICrop SPRUCE_SAPLING = new SaplingCrop("spruce", Items.SPRUCE_LOG, Items.SPRUCE_SAPLING);
   public static ICrop RUBBERWOOD_SAPLING;
   public static ICrop ADAMANTUM;
   public static ICrop ARGENTUM;
   public static ICrop AURELIA;
   public static ICrop FERRU;
   public static ICrop MALACHITE;
   public static ICrop STANNUM;
   public static ICrop ALUMEN;
   public static final ICrop COFFEE = new CoffeeCrop();
   public static final ICrop LUMILIA = new LumiliaCrop();
   public static final ICrop CACTUS = new CactusCrop();
   public static ICrop BEETROOT;
   public static ICrop CARROT;
   public static ICrop POTATO;
   public static final ICrop RED_MUSHROOM = new MushroomCrop(true);
   public static final ICrop BROWN_MUSHROOM = new MushroomCrop(false);
   public static final ICrop CRIMSON_FUNGUS = new FungusCrop(true);
   public static final ICrop WARPED_FUNGUS = new FungusCrop(false);
   public static final ICrop HEMP = new HempCrop();
   public static final ICrop TEA = new TeaCrop();
   public static final ICrop COCOA = new CocoaCrop();
   public static final ICrop MELON = new MelonCrop();
   public static final ICrop PUMPKIN = new PumpkinCrop();
   public static final ICrop SWEET_BERRY_BUSH = new BerryBushCrop();
   public static final ICrop GLOW_BERRY = new GlowBerryCrop();
   private long globalSeed = RandomSource.create().nextLong();
   private int tickDelay = 0;

   public CropRegistry() {
      INSTANCE = this;
   }

   public void tickSeed() {
      if (this.tickDelay++ >= 3) {
         this.tickDelay = 0;
         this.globalSeed = RandomSource.create().nextLong();
      }
   }

   public void register() {
      this.registerCrop(GLOWSHROOM);
      this.registerCrop(NETHERWART);
      this.registerBaseSeed(Items.NETHER_WART, new BaseSeed(NETHERWART, 1, 1, 1, 1, 1));
      this.registerCrop(TERRAWART);
      this.registerBaseSeed(IC2Items.TERRA_WART, new BaseSeed(TERRAWART, 1, 1, 1, 1, 1));
      this.registerCrop(SUGARCANE);
      this.registerBaseSeed(Items.SUGAR_CANE, new BaseSeed(SUGARCANE, 1, 3, 0, 2, 1));
      this.registerCrop(STICKREED);
      this.registerCrop(BAMBOO);
      this.registerBaseSeed(Items.BAMBOO, new BaseSeed(BAMBOO, 1, 1, 1, 1, 1));
      this.registerCrop(WHEAT);
      this.registerBaseSeed(Items.WHEAT_SEEDS, new BaseSeed(WHEAT, 1, 1, 1, 1, 1));
      this.registerCrop(HOPS);
      this.registerCrop(BLUEWHEAT);
      this.registerCrop(REDWHEAT);
      this.registerCrop(ALLIUM);
      this.registerBaseSeed(Items.ALLIUM, new BaseSeed(ALLIUM, 4, 1, 1, 1, 4));
      this.registerCrop(BLACKTHORN);
      this.registerCrop(BLUE_ORCHID);
      this.registerBaseSeed(Items.BLUE_ORCHID, new BaseSeed(BLUE_ORCHID, 4, 1, 1, 1, 4));
      this.registerCrop(CORNFLOWER);
      this.registerBaseSeed(Items.CORNFLOWER, new BaseSeed(CORNFLOWER, 4, 1, 1, 1, 4));
      this.registerCrop(DANDELION);
      this.registerBaseSeed(Items.DANDELION, new BaseSeed(DANDELION, 4, 1, 1, 1, 4));
      this.registerCrop(ORANGE_TULIP);
      this.registerBaseSeed(Items.ORANGE_TULIP, new BaseSeed(ORANGE_TULIP, 4, 1, 1, 1, 4));
      this.registerCrop(OXEYE_DAISY);
      this.registerBaseSeed(Items.OXEYE_DAISY, new BaseSeed(OXEYE_DAISY, 4, 1, 1, 1, 4));
      this.registerCrop(PINK_TULIP);
      this.registerBaseSeed(Items.PINK_TULIP, new BaseSeed(PINK_TULIP, 4, 1, 1, 1, 4));
      this.registerCrop(POPPY);
      this.registerBaseSeed(Items.POPPY, new BaseSeed(POPPY, 4, 1, 1, 1, 4));
      this.registerCrop(RAINBOW_FLOWER);
      this.registerCrop(RED_TULIP);
      this.registerBaseSeed(Items.RED_TULIP, new BaseSeed(RED_TULIP, 4, 1, 1, 1, 4));
      this.registerCrop(PURPLE_TULIP);
      this.registerCrop(VENOMILIA);
      this.registerCrop(WHITE_TULIP);
      this.registerBaseSeed(Items.WHITE_TULIP, new BaseSeed(WHITE_TULIP, 4, 1, 1, 1, 4));
      this.registerCrop(AZURE_BLUET);
      this.registerBaseSeed(Items.AZURE_BLUET, new BaseSeed(AZURE_BLUET, 4, 1, 1, 1, 4));
      this.registerCrop(LILY_OF_THE_VALLEY);
      this.registerBaseSeed(Items.LILY_OF_THE_VALLEY, new BaseSeed(LILY_OF_THE_VALLEY, 4, 1, 1, 1, 4));
      this.registerCrop(FORGET_ME_NOT);
      BRAIN_CORAL = new CoralCrop("brain", Items.BRAIN_CORAL, Items.BRAIN_CORAL_FAN, Items.BRAIN_CORAL_BLOCK, "Pink");
      BUBBLE_CORAL = new CoralCrop("bubble", Items.BUBBLE_CORAL, Items.BUBBLE_CORAL_FAN, Items.BUBBLE_CORAL_BLOCK, "Magenta");
      FIRE_CORAL = new CoralCrop("fire", Items.FIRE_CORAL, Items.FIRE_CORAL_FAN, Items.FIRE_CORAL_BLOCK, "Red");
      HORN_CORAL = new CoralCrop("horn", Items.HORN_CORAL, Items.HORN_CORAL_FAN, Items.HORN_CORAL_BLOCK, "Yellow");
      TUBE_CORAL = new CoralCrop("tube", Items.TUBE_CORAL, Items.TUBE_CORAL_FAN, Items.TUBE_CORAL_BLOCK, "Blue");
      this.registerCrop(BRAIN_CORAL);
      this.registerBaseSeed(Items.BRAIN_CORAL, new BaseSeed(BRAIN_CORAL, 3, 1, 1, 1, 1));
      this.registerCrop(BUBBLE_CORAL);
      this.registerBaseSeed(Items.BUBBLE_CORAL, new BaseSeed(BUBBLE_CORAL, 3, 1, 1, 1, 1));
      this.registerCrop(FIRE_CORAL);
      this.registerBaseSeed(Items.FIRE_CORAL, new BaseSeed(FIRE_CORAL, 3, 1, 1, 1, 1));
      this.registerCrop(HORN_CORAL);
      this.registerBaseSeed(Items.HORN_CORAL, new BaseSeed(HORN_CORAL, 3, 1, 1, 1, 1));
      this.registerCrop(TUBE_CORAL);
      this.registerBaseSeed(Items.TUBE_CORAL, new BaseSeed(TUBE_CORAL, 3, 1, 1, 1, 1));
      SEA_WEED = new SeaGrassCrop();
      this.registerCrop(SEA_PICKLE);
      this.registerBaseSeed(Items.SEA_PICKLE, new BaseSeed(SEA_PICKLE, 4, 1, 1, 1, 4));
      this.registerCrop(KELP);
      this.registerBaseSeed(Items.KELP, new BaseSeed(KELP, 1, 1, 1, 1, 1));
      this.registerCrop(SEA_WEED);
      this.registerCrop(BONE_FLOWER);
      this.registerCrop(INKBERGINE);
      this.registerCrop(ACACIA_SAPLING);
      this.registerBaseSeed(Items.ACACIA_SAPLING, new BaseSeed(ACACIA_SAPLING, 1, 1, 1, 1, 1));
      this.registerCrop(BIRCH_SAPLING);
      this.registerBaseSeed(Items.BIRCH_SAPLING, new BaseSeed(BIRCH_SAPLING, 1, 1, 1, 1, 1));
      this.registerCrop(DARK_OAK_SAPLING);
      this.registerBaseSeed(Items.DARK_OAK_SAPLING, new BaseSeed(DARK_OAK_SAPLING, 1, 1, 1, 1, 1));
      this.registerCrop(OAK_SAPLING);
      this.registerBaseSeed(Items.OAK_SAPLING, new BaseSeed(OAK_SAPLING, 1, 1, 1, 1, 1));
      this.registerCrop(JUNGLE_SAPLING);
      this.registerBaseSeed(Items.JUNGLE_SAPLING, new BaseSeed(JUNGLE_SAPLING, 1, 1, 1, 1, 1));
      this.registerCrop(SPRUCE_SAPLING);
      this.registerBaseSeed(Items.SPRUCE_SAPLING, new BaseSeed(SPRUCE_SAPLING, 1, 1, 1, 1, 1));
      RUBBERWOOD_SAPLING = new SaplingCrop("rubberwood", IC2Blocks.RUBBERWOOD_LOG, IC2Blocks.RUBBER_SAPLING);
      this.registerCrop(RUBBERWOOD_SAPLING);
      this.registerBaseSeed(IC2Blocks.RUBBER_SAPLING.asItem(), new BaseSeed(RUBBERWOOD_SAPLING, 1, 0, 0, 0, 1));
      ADAMANTUM = new OreCrop(
         "adamantum",
         new CropProperties(10, 2, 0, 0, 2, 0),
         CollectionUtils.<TagKey<Block>>asList(Blocks.ORES_DIAMOND, Blocks.STORAGE_BLOCKS_DIAMOND),
         IC2Items.DUST_DIAMOND,
         BaseCrop.DAENARA,
         "Cyan"
      );
      ARGENTUM = new OreCrop(
         "argentum",
         new CropProperties(10, 2, 0, 0, 2, 0),
         CollectionUtils.<TagKey<Block>>asList(IC2Tags.ORE_SILVER_BLOCK, IC2Tags.STORAGE_SILVER_BLOCK),
         IC2Items.DUST_SILVER,
         BaseCrop.SPEIGER,
         "Silver"
      );
      AURELIA = new OreCrop(
         "aurelia",
         new CropProperties(10, 2, 0, 0, 2, 0),
         CollectionUtils.<TagKey<Block>>asList(Blocks.ORES_GOLD, Blocks.STORAGE_BLOCKS_GOLD),
         IC2Items.DUST_GOLD,
         BaseCrop.ALBLAKA,
         "Gold"
      );
      FERRU = new OreCrop(
         "ferru",
         new CropProperties(6, 2, 0, 0, 1, 0),
         CollectionUtils.<TagKey<Block>>asList(Blocks.ORES_IRON, Blocks.STORAGE_BLOCKS_IRON),
         IC2Items.DUST_IRON,
         BaseCrop.ALBLAKA,
         "Gray"
      );
      MALACHITE = new OreCrop(
         "malachite",
         new CropProperties(6, 2, 0, 0, 1, 0),
         CollectionUtils.<TagKey<Block>>asList(Blocks.ORES_COPPER, IC2Tags.STORAGE_COPPER_BLOCK),
         IC2Items.DUST_COPPER,
         BaseCrop.SPEIGER,
         "Orange"
      );
      STANNUM = new OreCrop(
         "stannum",
         new CropProperties(6, 2, 0, 0, 1, 0),
         CollectionUtils.<TagKey<Block>>asList(IC2Tags.ORE_TIN_BLOCK, IC2Tags.STORAGE_TIN_BLOCK),
         IC2Items.DUST_TIN,
         BaseCrop.SPEIGER,
         "White"
      );
      ALUMEN = new OreCrop(
         "alumen",
         new CropProperties(8, 2, 0, 0, 2, 0),
         CollectionUtils.<TagKey<Block>>asList(IC2Tags.ORE_ALUMINIUM_BLOCK, IC2Tags.STORAGE_ALUMINIUM_BLOCK),
         IC2Items.DUST_ALUMINIUM,
         BaseCrop.DAENARA,
         "Silver"
      );
      this.registerCrop(ADAMANTUM);
      this.registerCrop(ARGENTUM);
      this.registerCrop(AURELIA);
      this.registerCrop(FERRU);
      this.registerCrop(MALACHITE);
      this.registerCrop(STANNUM);
      this.registerCrop(ALUMEN);
      this.registerCrop(COFFEE);
      this.registerBaseSeed(IC2Items.COFFEE_BEANS, new BaseSeed(COFFEE, 1, 1, 1, 1, 1));
      this.registerCrop(LUMILIA);
      this.registerCrop(CACTUS);
      this.registerBaseSeed(Items.CACTUS, new BaseSeed(CACTUS, 1, 1, 1, 1, 1));
      BEETROOT = new BeetRootCrop();
      CARROT = new VegetableCrop("carrot", "Orange", Items.CARROT);
      POTATO = new VegetableCrop("potato", "Yellow", Items.POTATO);
      this.registerCrop(BEETROOT);
      this.registerBaseSeed(Items.BEETROOT_SEEDS, new BaseSeed(BEETROOT, 1, 1, 1, 1, 1));
      this.registerCrop(CARROT);
      this.registerBaseSeed(Items.CARROT, new BaseSeed(CARROT, 1, 1, 1, 1, 1));
      this.registerCrop(POTATO);
      this.registerBaseSeed(Items.POTATO, new BaseSeed(POTATO, 1, 1, 1, 1, 1));
      this.registerCrop(RED_MUSHROOM);
      this.registerBaseSeed(Items.RED_MUSHROOM, new BaseSeed(RED_MUSHROOM, 3, 1, 1, 1, 4));
      this.registerCrop(BROWN_MUSHROOM);
      this.registerBaseSeed(Items.BROWN_MUSHROOM, new BaseSeed(BROWN_MUSHROOM, 3, 1, 1, 1, 4));
      this.registerCrop(CRIMSON_FUNGUS);
      this.registerBaseSeed(Items.CRIMSON_FUNGUS, new BaseSeed(CRIMSON_FUNGUS, 3, 1, 1, 1, 4));
      this.registerCrop(WARPED_FUNGUS);
      this.registerBaseSeed(Items.WARPED_FUNGUS, new BaseSeed(WARPED_FUNGUS, 3, 1, 1, 1, 4));
      this.registerCrop(HEMP);
      this.registerBaseSeed(IC2Items.HEMP_SEEDS, new BaseSeed(HEMP, 1, 1, 1, 1, 1));
      this.registerCrop(TEA);
      this.registerCrop(COCOA);
      this.registerBaseSeed(Items.COCOA_BEANS, new BaseSeed(COCOA, 1, 0, 0, 0, 1));
      this.registerCrop(MELON);
      this.registerBaseSeed(Items.MELON_SEEDS, new BaseSeed(MELON, 1, 1, 1, 1, 1));
      this.registerCrop(PUMPKIN);
      this.registerBaseSeed(Items.PUMPKIN_SEEDS, new BaseSeed(PUMPKIN, 1, 1, 1, 1, 1));
      this.registerCrop(SWEET_BERRY_BUSH);
      this.registerBaseSeed(Items.SWEET_BERRIES, new BaseSeed(SWEET_BERRY_BUSH, 1, 1, 1, 1, 1));
      this.registerCrop(GLOW_BERRY);
      this.registerBaseSeed(Items.GLOW_BERRIES, new BaseSeed(GLOW_BERRY, 1, 1, 1, 1, 1));
      WEED = this.registerCrop(new WeedCrop());
      this.registerFarmland(new Farmland(), net.minecraft.world.level.block.Blocks.FARMLAND);
      this.registerFarmland(new BaseFarmland(0, -2), net.minecraft.world.level.block.Blocks.SPONGE);
      this.registerFarmland(new BaseFarmland(5, -2), net.minecraft.world.level.block.Blocks.WET_SPONGE);
      this.registerFarmland(new BaseFarmland(0, 1), net.minecraft.world.level.block.Blocks.DIRT);
      this.registerFarmland(new PlanterPotFarmland(), IC2Blocks.PLANTER_POT);
      this.registerFarmland(new BaseFarmland(0, -4), net.minecraft.world.level.block.Blocks.COARSE_DIRT);
      this.registerFarmland(new BaseFarmland(0, 0), net.minecraft.world.level.block.Blocks.PODZOL);
      this.registerFarmland(new BaseFarmland(-3, 6), net.minecraft.world.level.block.Blocks.SOUL_SAND);
      this.registerFarmland(new BaseFarmland(-2, 5), net.minecraft.world.level.block.Blocks.SOUL_SOIL);
      this.registerFarmland(new BaseFarmland(-2, -1), net.minecraft.world.level.block.Blocks.SAND, net.minecraft.world.level.block.Blocks.RED_SAND);
      this.registerFarmland(new BaseFarmland(1, 2), net.minecraft.world.level.block.Blocks.MYCELIUM);
      this.registerFarmland(new BaseFarmland(2, 1), net.minecraft.world.level.block.Blocks.MUD);
      this.registerFarmland(new BaseFarmland(0, 3), net.minecraft.world.level.block.Blocks.WARPED_NYLIUM, net.minecraft.world.level.block.Blocks.CRIMSON_NYLIUM);
      this.registerFarmland(new BaseFarmland(1, -1), net.minecraft.world.level.block.Blocks.ROOTED_DIRT);
      this.registerSubSoil(new BaseSubSoil(-1, 3), net.minecraft.world.level.block.Blocks.SOUL_SAND);
      this.registerSubSoil(new BaseSubSoil(0, 2), net.minecraft.world.level.block.Blocks.SOUL_SOIL);
      this.registerSubSoil(new BaseSubSoil(3, -1), net.minecraft.world.level.block.Blocks.WET_SPONGE);
      this.registerSubSoil(new BaseSubSoil(-1, 0), net.minecraft.world.level.block.Blocks.SAND, net.minecraft.world.level.block.Blocks.RED_SAND);
      this.registerSubSoil(new BaseSubSoil(0, -1), net.minecraft.world.level.block.Blocks.COARSE_DIRT);
      this.registerSubSoil(new BaseSubSoil(0, 1), net.minecraft.world.level.block.Blocks.DIRT);
      this.registerSubSoil(new BaseSubSoil(2, 0), net.minecraft.world.level.block.Blocks.PRISMARINE);
      this.registerSubSoil(new BaseSubSoil(-1, 1), net.minecraft.world.level.block.Blocks.ROOTED_DIRT);
      this.registerSubSoil(new Mangrowth(), net.minecraft.world.level.block.Blocks.MANGROVE_ROOTS);
      this.addBiomeNutrientBonus(BiomeTags.IS_JUNGLE, 10);
      this.addBiomeNutrientBonus(Biomes.IS_SWAMP, 10);
      this.addBiomeNutrientBonus(Biomes.IS_MUSHROOM, 5);
      this.addBiomeNutrientBonus(BiomeTags.IS_FOREST, 5);
      this.addBiomeNutrientBonus(BiomeTags.IS_RIVER, 2);
      this.addBiomeNutrientBonus(Biomes.IS_PLAINS, 0);
      this.addBiomeNutrientBonus(BiomeTags.IS_SAVANNA, -2);
      this.addBiomeNutrientBonus(BiomeTags.IS_HILL, -5);
      this.addBiomeNutrientBonus(BiomeTags.IS_MOUNTAIN, -5);
      this.addBiomeNutrientBonus(Biomes.IS_WASTELAND, -8);
      this.addBiomeNutrientBonus(BiomeTags.IS_END, -10);
      this.addBiomeNutrientBonus(BiomeTags.IS_NETHER, -10);
      this.addBiomeNutrientBonus(Biomes.IS_DEAD, -10);
      this.addBiomeHumidityBonus(Biomes.IS_SWAMP, 5);
      this.addBiomeHumidityBonus(Biomes.IS_WATER, 10);
      this.addBiomeHumidityBonus(BiomeTags.IS_SAVANNA, -8);
      this.addBiomeHumidityBonus(Biomes.IS_WASTELAND, -5);
      this.addBiomeHumidityBonus(BiomeTags.IS_END, -7);
      this.addBiomeHumidityBonus(BiomeTags.IS_NETHER, -10);
      this.addBiomeWaterQualityBonus(BiomeTags.IS_OCEAN, 10);
      this.addBiomeWaterQualityBonus(BiomeTags.IS_RIVER, 7);
      this.addBiomeWaterQualityBonus(Biomes.IS_HOT, 5);
      this.addBiomeWaterQualityBonus(BiomeTags.IS_BEACH, 4);
      this.addBiomeWaterQualityBonus(Biomes.IS_SWAMP, -3);
      this.addBiomeWaterQualityBonus(Biomes.IS_COLD, -6);
      this.addBiomeWaterQualityBonus(Biomes.IS_DEAD, -10);
      IC2.EVENT_BUS.post(new ICropRegistry.CropRegisterEvent());
   }

   @Override
   public void registerCustomCropRenderer(ResourceLocation location, ICropRenderer render) {
      if (location == null || !this.crops.containsKey(location)) {
         IC2.LOGGER.info("Crop not found");
      } else if (render == null) {
         this.customRenders.remove(location);
      } else {
         this.customRenders.put(location, render);
      }
   }

   @Override
   public void registerCustomStickRenderer(ICropRenderer render) {
      this.stickRenderer = render;
   }

   @Override
   public <T extends ICrop> T registerCrop(T crop) {
      ResourceLocation id = crop.id();
      if (id == null) {
         return null;
      } else {
         this.crops.put(id, crop);
         ItemStack display = crop.getDisplayItem();
         if (!display.isEmpty()) {
            this.displayItems.put(id, display.copy());
         }

         return crop;
      }
   }

   @Override
   public ICrop getCrop(ResourceLocation location) {
      return this.crops.get(location);
   }

   @Override
   public ICrop getRandomCrop(OptionalLong seed) {
      return (ICrop)Util.getRandom(new ObjectArrayList(this.crops.values()), RandomSource.create(seed.orElse(this.globalSeed)));
   }

   @Override
   public ICrop getCrop(ItemStack stack) {
      return stack.getItem() instanceof ICropSeed ? ((ICropSeed)stack.getItem()).getCrop(stack) : null;
   }

   @Override
   public void registerBaseSeed(Item item, BaseSeed seed) {
      if (item != Items.AIR && seed != null) {
         this.seeds.put(item, seed);
         this.cropSeeds.put(seed.crop.id(), new ItemStack(item, seed.stack_size));
      }
   }

   @Override
   public ItemStack getSeedForCrop(ICrop crop) {
      return this.cropSeeds.getOrDefault(crop.id(), new ItemStack(IC2Items.CROP_STICKS));
   }

   @Override
   public BaseSeed getSeedForStack(ItemStack stack) {
      return this.seeds.get(stack.getItem());
   }

   public List<Block> getAllFarmlands() {
      List<Block> blocks = new ObjectArrayList();

      for (Entry<Block, IFarmland> entry : this.farmlands.entrySet()) {
         if (entry.getValue() instanceof BaseFarmland) {
            blocks.add(entry.getKey());
         }
      }

      return blocks;
   }

   @Override
   public List<ICrop> getCrops() {
      return new ObjectArrayList(this.crops.values());
   }

   @Override
   public void registerDisplayItem(ResourceLocation cropID, ItemStack stack) {
      if (cropID != null && !stack.isEmpty()) {
         this.displayItems.put(cropID, stack.copy());
      }
   }

   @Override
   public ItemStack getDisplayItem(ResourceLocation cropID) {
      return this.displayItems.getOrDefault(cropID, ItemStack.EMPTY);
   }

   @Override
   public void addBiomeHumidityBonus(TagKey<Biome> type, int bonus) {
      this.humidity.put(type, bonus);
   }

   @Override
   public int getHumidityBonus(Holder<Biome> biome) {
      int result = 0;
      ObjectIterator var3 = Object2IntMaps.fastIterable(this.humidity).iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>>)var3.next();
         if (biome.is((TagKey)entry.getKey())) {
            result = Math.max(result, entry.getIntValue());
         }
      }

      return result;
   }

   @Override
   public void addBiomeNutrientBonus(TagKey<Biome> type, int bonus) {
      this.nutrients.put(type, bonus);
   }

   @Override
   public int getNutrientBonus(Holder<Biome> biome) {
      int result = 0;
      ObjectIterator var3 = Object2IntMaps.fastIterable(this.nutrients).iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>>)var3.next();
         if (biome.is((TagKey)entry.getKey())) {
            result = Math.max(result, entry.getIntValue());
         }
      }

      return result;
   }

   @Override
   public void addBiomeWaterQualityBonus(TagKey<Biome> type, int bonus) {
      this.water.put(type, bonus);
   }

   @Override
   public int getWaterQualityBonus(Holder<Biome> biome) {
      int result = 0;
      ObjectIterator var3 = Object2IntMaps.fastIterable(this.water).iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<TagKey<Biome>>)var3.next();
         if (biome.is((TagKey)entry.getKey())) {
            result = Math.max(result, entry.getIntValue());
         }
      }

      return result;
   }

   @Override
   public void registerFarmland(IFarmland land, Block... blocks) {
      for (Block block : blocks) {
         this.farmlands.put(block, land);
      }
   }

   @Override
   public IFarmland getFarmland(Block block) {
      return this.farmlands.get(block);
   }

   @Override
   public void registerSubSoil(ISubSoil subSoil, Block... blocks) {
      for (Block block : blocks) {
         this.soils.put(block, subSoil);
      }
   }

   @Override
   public ISubSoil getSubSoil(Block block) {
      return this.soils.get(block);
   }

   @OnlyIn(Dist.CLIENT)
   public Map<CropEntry, List<BakedQuad>> createQuads(boolean fancy) {
      if (this.stickRenderer == null) {
         this.stickRenderer = new StickRenderer();
      }

      Map<CropEntry, List<BakedQuad>> quads = CollectionUtils.<CropEntry, List<BakedQuad>>createMap();
      List<BakedQuad> stickQuads;
      quads.put(new CropEntry(null, 0), stickQuads = this.stickRenderer.createQuadsForStage(0, fancy, QuadBaker.BAKER));
      quads.put(new CropEntry(null, 1), this.stickRenderer.createQuadsForStage(1, fancy, QuadBaker.BAKER));

      for (ICrop crop : this.crops.values()) {
         ICropRenderer render = this.customRenders.get(crop.id());
         if (render == null) {
            render = new DefaultCropRenderer(crop);
         }

         int i = 0;

         for (int m = crop.getGrowthSteps(); i < m; i++) {
            List<BakedQuad> resultQuads = CollectionUtils.<BakedQuad>createList();
            resultQuads.addAll(stickQuads);
            List<BakedQuad> list = render.createQuadsForStage(i, fancy, QuadBaker.BAKER);
            if (list != null) {
               resultQuads.addAll(list);
            }

            quads.put(new CropEntry(crop, i), resultQuads);
         }
      }

      return quads;
   }
}
