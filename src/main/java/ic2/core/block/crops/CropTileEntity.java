package ic2.core.block.crops;

import ic2.api.crops.BaseSeed;
import ic2.api.crops.CropDifficulty;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropSeed;
import ic2.api.crops.ICropTile;
import ic2.api.crops.IFarmland;
import ic2.api.crops.ISeedCrop;
import ic2.api.crops.ISubSoil;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkEventListener;
import ic2.api.network.tile.PacketRange;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ICollideable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.block.CropEntry;
import ic2.core.block.rendering.props.CropProperty;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.misc.CropSeedItem;
import ic2.core.platform.player.CropStatistics;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.config.ic2.CropParticles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CropTileEntity
   extends BaseTileEntity
   implements ICropTile,
   ITickListener,
   IClickable,
   IRedstoneListener,
   IRedstoneProvider,
   ICollideable,
   INetworkEventListener {
   @NetworkInfo
   ICrop crop;
   @NetworkInfo
   public boolean breeding;
   @NetworkInfo
   public int stage;
   @NetworkInfo
   public CompoundTag customData = new CompoundTag();
   public int pittyCounter = 0;
   boolean overgrowth = false;
   int overgrowthTicks = 0;
   boolean restored = false;
   private byte humidity = -1;
   private byte nutrients = -1;
   private byte airQuality = -1;
   private byte scanLevel = 0;
   private IFarmland land = null;
   private int growthPoints = 0;
   private int nutrientStorage = 0;
   private int waterStorage = 0;
   private int exStorage = 0;
   private byte growth = 0;
   private byte gain = 0;
   private byte resistance = 0;
   CropDifficulty diff;
   boolean isSet = false;
   int cachedSpeed = -1;
   UUID breeder = null;
   int lastLight = -1;

   public CropTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.diff = new CropDifficulty(((ic2.core.utils.config.ic2.CropDifficulty)IC2.CONFIG.cropDifficulty.get()).ordinal());
      this.clearNetworkFields();
      this.addNetworkFields(new String[]{"crop", "breeding", "stage", "customData"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CROP_STICK;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.remove("active");
      compound.remove("facing");
      compound.remove("customName");
      NBTUtils.putString(compound, "crop", this.crop == null ? "empty" : this.crop.id().toString(), "empty");
      NBTUtils.putBoolean(compound, "breeding", this.breeding, false);
      NBTUtils.putByte(compound, "stage", this.stage, 0);
      NBTUtils.put(compound, "customdata", this.customData);
      NBTUtils.putBoolean(compound, "overgrowth", this.overgrowth, false);
      NBTUtils.putInt(compound, "ticks", this.overgrowthTicks, 0);
      NBTUtils.putBoolean(compound, "restored", this.restored, false);
      NBTUtils.putByte(compound, "scan", this.scanLevel, 0);
      NBTUtils.putInt(compound, "points", this.growthPoints, 0);
      NBTUtils.putShort(compound, "nutrients", this.nutrientStorage, 0);
      NBTUtils.putShort(compound, "water", this.waterStorage, 0);
      NBTUtils.putShort(compound, "ex", this.exStorage, 0);
      NBTUtils.putByte(compound, "growth", this.growth, 0);
      NBTUtils.putByte(compound, "gain", this.gain, 0);
      NBTUtils.putByte(compound, "resistance", this.resistance, 0);
      NBTUtils.putInt(compound, "pitty", this.pittyCounter, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      String s = NBTUtils.getString(compound, "crop", "empty");
      this.crop = s.equalsIgnoreCase("empty") ? null : CropRegistry.INSTANCE.getCrop(new ResourceLocation(s));
      this.breeding = compound.getBoolean("breeding");
      this.stage = compound.getByte("stage");
      this.customData = compound.getCompound("customdata");
      this.overgrowth = compound.getBoolean("overgrowth");
      this.overgrowthTicks = compound.getInt("ticks");
      this.restored = compound.getBoolean("restored");
      this.scanLevel = compound.getByte("scan");
      this.growthPoints = compound.getInt("points");
      this.nutrientStorage = compound.getInt("nutrients");
      this.waterStorage = compound.getInt("water");
      this.exStorage = compound.getInt("ex");
      this.growth = compound.getByte("growth");
      this.gain = compound.getByte("gain");
      this.resistance = compound.getByte("resistance");
      this.pittyCounter = compound.getInt("pitty");
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      this.land = null;
      this.getFarmland();
      if (this.land == null) {
         this.pickCrop();
         Block.popResource(this.getLevel(), this.getBlockPos(), new ItemStack(IC2Items.CROP_STICKS));
         this.getLevel().removeBlock(this.getBlockPos(), false);
         this.getLevel().playSound(null, this.getBlockPos(), SoundType.GRASS.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
      } else {
         if (this.isWaterLogged()) {
            this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
         }

         if (this.crop != null && this.isSimulating()) {
            this.crop.onBlockUpdate(this);
         }
      }
   }

   @Override
   public void onEntityCollided(Entity entity) {
      if (this.crop != null && this.isSimulating()) {
         this.crop.onEntityCollision(this, entity);
      }
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return this.crop != null && this.crop.canEmitRedstone(this) ? this.crop.getRedstoneLevel(this) : 0;
   }

   public int getCropLight() {
      return this.crop == null ? 0 : this.crop.getEmittedLight(this);
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return this.crop != null && this.crop.canEmitRedstone(this);
   }

   @Override
   public IClickable.ClickAction getRequiredActions() {
      return IClickable.ClickAction.BOTH;
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (!stack.isEmpty()) {
         if (this.crop == null) {
            if (!this.breeding && stack.getItem() == IC2Items.CROP_STICKS) {
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               this.breeding = true;
               this.breeder = player.getUUID();
               this.updateTileField("breeding");
               return true;
            }

            if (this.applyBaseSeed(stack, player)) {
               return true;
            }
         }

         if (!this.overgrowth && stack.getItem() == IC2Items.OVERGROWTH_FERTILIZER) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            player.awardStat(IC2Stats.OVERGROWTH_USED);
            this.overgrowth = true;
            this.overgrowthTicks = 6000;
            return true;
         }

         if (this.overgrowth && this.overgrowthTicks <= 0 && !this.restored && stack.getItem() == IC2Items.RESTORING_FERTILIZER) {
            this.restored = true;
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            player.awardStat(IC2Stats.REVIVED_USED);
            return true;
         }

         if (stack.getItem() == IC2Items.CROP_ANALYZER) {
            if (this.isSimulating()) {
               if (player.isShiftKeyDown()) {
                  if (this.crop != null) {
                     if (this.scanLevel >= 4) {
                        player.displayClientMessage(this.translate("info.item.ic2.crop_analyzer.scan_level_increase.failed_max"), false);
                     } else if (ElectricItem.MANAGER.use(stack, ICropSeed.SCAN_COST[this.scanLevel], player)) {
                        this.scanLevel++;
                        player.displayClientMessage(
                           this.translate("info.item.ic2.crop_analyzer.scan_level_increase.success", new Object[]{this.scanLevel}), false
                        );
                     } else {
                        player.displayClientMessage(
                           this.translate("info.item.ic2.crop_analyzer.scan_level_increase.failed_energy", new Object[]{ICropSeed.SCAN_COST[this.scanLevel]}),
                           false
                        );
                     }
                  }
               } else if (this.crop == null || this.stage <= 0 || this.scanLevel < 1 && this.stage < this.crop.getGrowthSteps()) {
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.name", new Object[]{this.translate("info.crop.ic2.data.unknown", new ChatFormatting[]{ChatFormatting.YELLOW})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.qualities.nutrients",
                        new Object[]{this.string(this.getNutrients() + " / 20", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.qualities.humidity",
                        new Object[]{this.string(this.getHumidity() + " / 20", new ChatFormatting[]{ChatFormatting.AQUA})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        this.isWaterLogged() ? "info.crop.ic2.data.qualities.water" : "info.crop.ic2.data.qualities.air",
                        new Object[]{this.string(this.getEnvironmentQuality() + " / 10", new ChatFormatting[]{ChatFormatting.YELLOW})}
                     ),
                     false
                  );
                  player.displayClientMessage(this.translate("info.crop.ic2.data.storage", this.createStorageStats()), false);
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.light",
                        new Object[]{
                           this.string(this.getLevel().getMaxLocalRawBrightness(this.getBlockPos()) + " / 15", new ChatFormatting[]{ChatFormatting.YELLOW})
                        }
                     ),
                     false
                  );
               } else {
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.name",
                        new Object[]{
                           this.string()
                              .append(this.crop.getName())
                              .append(" (")
                              .append(this.translate("info.crop.ic2.data.info.discovered", new Object[]{this.crop.discoveredBy().copy()}))
                              .append(")")
                              .withStyle(ChatFormatting.YELLOW)
                        }
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.size",
                        new Object[]{this.string(this.stage + " / " + this.crop.getGrowthSteps(), new ChatFormatting[]{ChatFormatting.GREEN})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.growth",
                        new Object[]{
                           this.string(this.growthPoints + " / " + this.crop.getGrowthDuration(this) + " (")
                              .append(this.translate("info.crop.ic2.data.info.rate", new Object[]{this.calculateGrowthSpeed()}))
                              .append(")")
                              .withStyle(ChatFormatting.AQUA)
                        }
                     ),
                     false
                  );
                  player.displayClientMessage(this.translate("info.crop.ic2.data.stats", this.createCropStats()), false);
                  int need = (this.crop.getProperties().getTier() - 1) * 4 + this.growth + this.gain + this.resistance;
                  int have = this.crop.getStatInfluence(this, this.getHumidity(), this.getNutrients(), this.getEnvironmentQuality()) * 5;
                  player.displayClientMessage(this.translate("info.crop.ic2.data.needs", new Object[]{need, have}), false);
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.qualities.nutrients",
                        new Object[]{this.string(this.getNutrients() + " / 20", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.qualities.humidity",
                        new Object[]{this.string(this.getHumidity() + " / 20", new ChatFormatting[]{ChatFormatting.AQUA})}
                     ),
                     false
                  );
                  player.displayClientMessage(
                     this.translate(
                        this.isWaterLogged() ? "info.crop.ic2.data.qualities.water" : "info.crop.ic2.data.qualities.air",
                        new Object[]{this.string(this.getEnvironmentQuality() + " / 10", new ChatFormatting[]{ChatFormatting.YELLOW})}
                     ),
                     false
                  );
                  player.displayClientMessage(this.translate("info.crop.ic2.data.storage", this.createStorageStats()), false);
                  player.displayClientMessage(
                     this.translate(
                        "info.crop.ic2.data.info.light",
                        new Object[]{
                           this.string(this.getLevel().getMaxLocalRawBrightness(this.getBlockPos()) + " / 15", new ChatFormatting[]{ChatFormatting.YELLOW})
                        }
                     ),
                     false
                  );
               }
            }

            return true;
         }

         if (stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.MELON_SEEDS || stack.getItem() == Items.PUMPKIN_SEEDS) {
            if (this.nutrientStorage <= 50) {
               this.nutrientStorage += 25;
               this.isSet = false;
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            } else {
               return false;
            }
         }

         if (stack.getItem() != IC2Items.FERTILIZER && stack.getItem() != Items.BONE_MEAL) {
            if (stack.getItem() == IC2Items.HYDRATION_CELL) {
               return this.applyManualHydration(stack, player, hand);
            }

            if (stack.getItem() == IC2Items.WEEDEX && this.applyWeedEx(true)) {
               if (!player.isCreative()) {
                  stack.hurtAndBreak(1, player, IC2Item.get(hand));
                  if (stack.getMaxDamage() - stack.getDamageValue() <= 1) {
                     IItemTransporter transporter = TransporterManager.getTransporter(player);
                     if (transporter != null && !transporter.removeItem(new SimpleFilter(IC2Items.GRIN_POWDER), null, 1, false).isEmpty()) {
                        stack.setDamageValue(0);
                     }
                  }
               }

               return true;
            }
         } else if (this.applyFertilizer(true)) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }
      }

      return this.crop != null && this.crop.onRightClick(this, player, hand);
   }

   private Object[] createStorageStats() {
      return new Component[]{
         this.string()
            .append(this.translate("info.crop.ic2.data.storage.fertilizer"))
            .append(this.string(": " + this.nutrientStorage + "/300", new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE})),
         this.string()
            .append(this.translate("info.crop.ic2.data.storage.water"))
            .append(this.string(": " + this.waterStorage + "/200", new ChatFormatting[]{ChatFormatting.BLUE})),
         this.string()
            .append(this.translate("info.crop.ic2.data.storage.ex"))
            .append(this.string(": " + this.exStorage + "/150", new ChatFormatting[]{ChatFormatting.GREEN}))
      };
   }

   private Object[] createCropStats() {
      return new Component[]{
         this.string()
            .append(this.translate("info.crop.ic2.data.stat.growth"))
            .append(this.string(": " + this.growth + "/31", new ChatFormatting[]{ChatFormatting.GREEN})),
         this.string()
            .append(this.translate("info.crop.ic2.data.stat.gain"))
            .append(this.string(": " + this.gain + "/31", new ChatFormatting[]{ChatFormatting.YELLOW})),
         this.string()
            .append(this.translate("info.crop.ic2.data.stat.resistance"))
            .append(this.string(": " + this.resistance + "/31", new ChatFormatting[]{ChatFormatting.BLUE}))
      };
   }

   private boolean applyBaseSeed(ItemStack stack, Player player) {
      BaseSeed seed = CropRegistry.INSTANCE.getSeedForStack(stack);
      int scanLevel = 1;
      if (seed == null && stack.getItem() instanceof ICropSeed) {
         seed = new BaseSeed(stack);
         scanLevel = ((ICropSeed)stack.getItem()).getScanLevel(stack);
      }

      if (seed == null) {
         return false;
      } else if (stack.getCount() < seed.stack_size) {
         player.displayClientMessage(this.translate("info.crop.ic2.plant.placement_error.not_enough", new Object[]{seed.stack_size}), false);
         return false;
      } else if (this.tryPlantSeed(seed, scanLevel, player)) {
         if (!player.isCreative()) {
            if (stack.hasCraftingRemainingItem()) {
               stack = stack.getCraftingRemainingItem();
            }

            stack.shrink(seed.stack_size);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean tryPlantSeed(BaseSeed seed, int scan, Player player) {
      if (this.crop != null || seed.crop == null || seed.crop == CropRegistry.WEED || seed.crop == CropRegistry.SEA_WEED) {
         player.displayClientMessage(this.translate("info.crop.ic2.plant.placement_error.error"), false);
         return false;
      } else if (this.breeding) {
         player.displayClientMessage(this.translate("info.crop.ic2.plant.placement_error.breeding"), false);
         return false;
      } else {
         this.requestStateUpdate();
         if (seed.crop.canGrow(this) && seed.crop.getCropType().isCompatible(this.isWaterLogged())) {
            this.removeCrop();
            this.crop = seed.crop;
            this.stage = seed.stage;
            this.growth = (byte)Mth.clamp(seed.growth, 0, 31);
            this.gain = (byte)Mth.clamp(seed.gain, 0, 31);
            this.resistance = (byte)Mth.clamp(seed.resistance, 0, 31);
            this.scanLevel = (byte)scan;
            this.breeding = false;
            this.updateTileFields(new String[]{"breeding", "crop", "stage"});
            return true;
         } else {
            seed.crop.onCropPlaceFailed(seed, this, player);
            return false;
         }
      }
   }

   public boolean applyWeedEx(boolean manual) {
      if ((this.exStorage < 100 || !manual) && this.exStorage < 150) {
         this.exStorage += 50;
         boolean trigger = this.getLevel().random.nextInt(3) == 0;
         if (manual) {
            trigger = this.getLevel().random.nextInt(5) == 0;
         }

         if (this.crop != null && this.exStorage >= 75 && trigger) {
            switch (this.getLevel().random.nextInt(5)) {
               case 0:
                  if (this.growth > 0) {
                     this.growth--;
                  }
               case 1:
                  if (this.gain > 0) {
                     this.gain--;
                  }
               default:
                  if (this.resistance > 0) {
                     this.resistance--;
                  }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean applyManualHydration(ItemStack stack, Player player, InteractionHand hand) {
      if (this.waterStorage >= 200) {
         return false;
      } else {
         int apply = Math.min(stack.getMaxDamage() - stack.getDamageValue(), 200 - this.waterStorage);
         if (stack.hurt(apply, this.getLevel().random, null)) {
            if (stack.hasCraftingRemainingItem()) {
               player.setItemInHand(hand, stack.getCraftingRemainingItem());
            } else {
               stack.shrink(1);
            }
         }

         this.waterStorage += apply;
         this.humidity = -1;
         this.isSet = false;
         return true;
      }
   }

   public boolean applyAutomatedHydration(ItemStack stack) {
      if (this.waterStorage >= 180) {
         return false;
      } else {
         int apply = Math.min(180 - this.waterStorage, stack.getMaxDamage() - stack.getDamageValue());
         if (stack.hurt(apply, this.getLevel().random, null)) {
            stack.shrink(1);
         }

         this.waterStorage += apply;
         this.humidity = -1;
         this.isSet = false;
         return true;
      }
   }

   public boolean applyAutomatedHydration(FluidTank tank) {
      if (this.waterStorage >= 180) {
         return false;
      } else {
         int added = tank.drain(180 - this.waterStorage, FluidAction.EXECUTE).getAmount();
         this.waterStorage += added;
         this.humidity = -1;
         this.isSet = false;
         return added > 0;
      }
   }

   public boolean applyFertilizer(boolean manual) {
      if (this.nutrientStorage > 200) {
         return false;
      } else {
         this.nutrientStorage += manual ? 100 : 90;
         if (manual) {
            this.nutrients = -1;
         }

         this.isSet = false;
         return true;
      }
   }

   @Override
   public boolean onLeftClick(Player player, BlockPos pos) {
      if (this.crop != null && this.crop.onLeftClick(this, player)) {
         return true;
      } else if (this.breeding) {
         this.breeding = false;
         this.updateTileField("breeding");
         if (this.isSimulating()) {
            Block.popResource(this.getLevel(), this.getBlockPos(), new ItemStack(IC2Items.CROP_STICKS));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("crop") || fields.contains("stage") || fields.contains("breeding")) {
         this.requestModelDataUpdate();
         this.getLevel().getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onServerDataReceived(int key, int value) {
      CropParticles settings = (CropParticles)IC2.CONFIG.cropParticles.get();
      if (settings != CropParticles.DISABLED) {
         this.getLevel().levelEvent(2005, this.worldPosition, settings == CropParticles.REDUCED ? (value == 1 ? 0 : 4) : 0);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.crop == null
         ? ModelData.builder().with(CropProperty.CROP, new CropEntry(null, this.breeding ? 1 : 0)).build()
         : ModelData.builder().with(CropProperty.CROP, new CropEntry(this.crop, this.stage - 1)).build();
   }

   @Override
   public void onTick() {
      if (this.overgrowth) {
         if (this.overgrowthTicks > 0) {
            this.update(8);
            this.overgrowthTicks--;
            int newLight = this.getCropLight();
            if (this.lastLight != newLight) {
               this.lastLight = newLight;
               this.onStateChanged();
            }
         }
      } else if (this.clock(256)) {
         this.update(256);
         int newLight = this.getCropLight();
         if (this.lastLight != newLight) {
            this.lastLight = newLight;
            this.onStateChanged();
         }
      }
   }

   protected void update(int tickRate) {
      if (this.clock(tickRate * 4)) {
         this.humidity = this.updateHumidity();
      }

      if (this.clock(tickRate * 3)) {
         this.nutrients = this.updateNutrients();
      }

      if (this.clock(tickRate * 2)) {
         this.airQuality = this.updateAirQuality();
      }

      if (this.crop == null && (!this.breeding || !this.attemptCrossing())) {
         if (this.getLevel().random.nextInt(this.diff.getWeedChance()) != 0 || this.hasWeedEx()) {
            if (this.exStorage > 0 && this.getLevel().random.nextInt(10) == 0) {
               this.exStorage--;
            }

            return;
         }

         this.removeCrop();
         this.crop = this.isWaterLogged() ? CropRegistry.SEA_WEED : CropRegistry.WEED;
         this.stage = 1;
      }

      if (this.crop != null) {
         this.crop.onTick(this);
         if (this.crop != null) {
            if (!this.crop.getCropType().isCompatible(this.isWaterLogged())) {
               if (this.getLevel().random.nextInt(20) > this.resistance) {
                  this.removeCrop();
                  return;
               }
            } else if (this.crop.canGrow(this) && this.stage < this.crop.getGrowthSteps()) {
               this.growthPoints = this.growthPoints + this.calculateGrowthSpeed();
               this.isSet = false;
               boolean grown = this.crop != null && this.growthPoints >= this.crop.getGrowthDuration(this);
               if (this.clock(tickRate < 32 ? tickRate * 2 : tickRate)) {
                  this.sendToClient(0, grown ? 1 : 0, PacketRange.SHORT_RANGE);
               }

               if (grown) {
                  this.growthPoints = 0;
                  boolean canPerform = this.crop.canEmitRedstone(this);
                  this.stage++;
                  if (canPerform || this.crop.canEmitRedstone(this)) {
                     this.notifyChanges(true, DirectionList.ALL);
                  }

                  if (this.stage >= this.crop.getGrowthSteps() && this.scanLevel == 0) {
                     this.scanLevel = 1;
                  }

                  this.updateTileField("stage");
               } else if (this.crop == null) {
                  return;
               }
            } else if (this.stage > this.crop.getGrowthSteps()) {
               this.stage = this.crop.getGrowthSteps();
               this.updateTileField("stage");
            }

            if (this.nutrientStorage > 0) {
               this.nutrientStorage--;
            }

            if (this.waterStorage > 0) {
               this.waterStorage--;
            }

            if (this.crop.isWeed(this) && this.getLevel().random.nextInt(this.diff.getWeedChance()) - this.growth <= 2) {
               this.generateWeed();
            }
         }
      }
   }

   public boolean attemptCrossing() {
      if (this.getLevel().random.nextInt(3) != 0) {
         return false;
      } else {
         List<ICropTile> attempt = CollectionUtils.<ICropTile>createList();
         List<ICropTile> pitty = CollectionUtils.<ICropTile>createList();
         int cropsThere = 0;
         int unlucky = 0;

         for (Direction facing : DirectionList.HORIZONTAL) {
            CropTileEntity.BreedResult result = this.attemptCrossing(this.getBlockPos().relative(facing), attempt, pitty);
            cropsThere += result == CropTileEntity.BreedResult.NONE_FOUND ? 0 : 1;
            unlucky += result == CropTileEntity.BreedResult.FOUND_BREEDABLE ? 1 : 0;
         }

         if (attempt.size() < 2) {
            if (!attempt.isEmpty() && attempt.size() == cropsThere) {
               this.copyCrop(attempt.get(0));
               return true;
            }

            if (!IC2.CONFIG.cropPittySystem.get()) {
               return false;
            }

            if (unlucky < 2 || pitty.size() < 2) {
               return false;
            }

            this.pittyCounter++;
            if (this.pittyCounter < 100) {
               return false;
            }

            attempt.clear();
            attempt.addAll(pitty);
         }

         this.pittyCounter = 0;
         List<ICrop> crops = CropRegistry.INSTANCE.getCrops();
         crops.remove(CropRegistry.WEED);
         crops.remove(CropRegistry.SEA_WEED);
         int[] ratios = new int[crops.size()];
         int total = 0;
         boolean waterlogged = this.isWaterLogged();

         for (int i = 0; i < crops.size(); i++) {
            ICrop crop = crops.get(i);
            if (crop.canGrow(this) && crop.getCropType().isCompatible(waterlogged)) {
               for (int j = 0; j < attempt.size(); j++) {
                  int chance = calculateRatioFor(crop, attempt.get(j).getCrop());
                  ratios[i] += chance;
                  total += chance;
               }
            }
         }

         total = this.getLevel().random.nextInt(total);

         for (int ix = 0; ix < ratios.length; ix++) {
            if (ratios[ix] > 0 && ratios[ix] > total) {
               total = ix;
               break;
            }

            total -= ratios[ix];
         }

         if (total >= crops.size()) {
            return false;
         } else {
            this.breeding = false;
            this.crop = crops.get(total);
            this.stage = 1;
            this.growth = 0;
            this.resistance = 0;
            this.gain = 0;
            this.customData = new CompoundTag();
            int[] value = this.diff.getCropStats(attempt, this.getLevel().random);
            this.growth = (byte)value[0];
            this.resistance = (byte)value[1];
            this.gain = (byte)value[2];
            this.updateTileFields(new String[]{"crop", "stage", "customData", "breeding"});
            if (this.breeder != null && (!this.overgrowth || this.getLevel().random.nextInt(256) == 0)) {
               Player player = IC2.PLATFORM.getServer().getPlayerList().getPlayer(this.breeder);
               if (player != null) {
                  player.awardStat(IC2Stats.CROPS_BREED);
                  CropStatistics stats = CropStatistics.getStats();
                  if (stats != null) {
                     stats.onCropReceived(this.breeder, this.crop, this.growth, this.gain, this.resistance);
                  }
               }
            }

            return true;
         }
      }
   }

   @Override
   public int calculateGrowthSpeed() {
      if (this.isSet) {
         return this.cachedSpeed;
      } else {
         this.isSet = true;
         if (this.crop == null) {
            return this.cachedSpeed = -1;
         } else {
            int base = 3 + this.getLevel().random.nextInt(7) + this.growth;
            int need = (this.crop.getProperties().getTier() - 1) * 4 + this.growth + this.gain + this.resistance;
            if (need < 0) {
               need = 0;
            }

            int have = this.crop.getStatInfluence(this, this.getHumidity(), this.getNutrients(), this.getEnvironmentQuality()) * 5;
            if (have >= need) {
               base = base * (100 + (have - need)) / 100;
            } else {
               int neg = (need - have) * 4;
               if (neg > 100 && this.getLevel().random.nextInt(32) > this.resistance) {
                  this.removeCrop();
                  base = 0;
               } else {
                  base = base * (100 - neg) / 100;
                  if (base < 0) {
                     base = 0;
                  }
               }
            }

            return this.cachedSpeed = this.diff.getCropGrowth(base);
         }
      }
   }

   public CropTileEntity.BreedResult attemptCrossing(BlockPos pos, List<ICropTile> list, List<ICropTile> pitty) {
      if (!this.getLevel().isLoaded(pos)) {
         return CropTileEntity.BreedResult.NONE_FOUND;
      } else if (this.getLevel().getBlockEntity(pos) instanceof ICropTile other) {
         ICrop crop = other.getCrop();
         if (crop == null) {
            return CropTileEntity.BreedResult.NONE_FOUND;
         } else if (crop.canGrow(this) && other.canBreed()) {
            int base = this.diff.getBreedingDifficulty();
            if (other.getGrowthStat() >= 16) {
               base++;
            }

            if (other.getGrowthStat() >= 30) {
               base++;
            }

            if (other.getResistanceStat() >= 28) {
               base += 27 - other.getResistanceStat();
            }

            if (base >= 0) {
               pitty.add(other);
            }

            if (base >= this.getLevel().random.nextInt(20)) {
               list.add(other);
            }

            return base >= 0 ? CropTileEntity.BreedResult.FOUND_BREEDABLE : CropTileEntity.BreedResult.FOUND_CROP;
         } else {
            return CropTileEntity.BreedResult.FOUND_CROP;
         }
      } else {
         return CropTileEntity.BreedResult.NONE_FOUND;
      }
   }

   public void generateWeed() {
      BlockPos offset = this.getBlockPos();
      boolean stop = false;

      for (Direction last = null; !stop; offset = offset.relative(last)) {
         stop = last != null;
         int rand = this.getLevel().random.nextInt(4);
         Direction dir = last;
         last = Direction.from2DDataValue(stop ? 3 - rand : rand);
         if (dir == last || dir == last.getOpposite()) {
            break;
         }
      }

      if (this.getLevel().getBlockEntity(offset) instanceof CropTileEntity cropTile) {
         if (cropTile.crop == null || !cropTile.crop.isWeed(cropTile) && this.getLevel().random.nextInt(32) > cropTile.resistance && !cropTile.hasWeedEx()) {
            byte newGrowth = (byte)Math.max(cropTile.growth, this.growth);
            if (newGrowth < 31 && this.getLevel().random.nextBoolean()) {
               newGrowth++;
            }

            cropTile.removeCrop();
            cropTile.crop = cropTile.isWaterLogged() ? CropRegistry.SEA_WEED : CropRegistry.WEED;
            cropTile.stage = 1;
            cropTile.growth = newGrowth;
            if (this.overgrowth) {
               cropTile.overgrowth = true;
               cropTile.overgrowthTicks = 1000;
            }
         }
      } else if (this.getLevel().isEmptyBlock(offset)) {
         Block block = this.getLevel().getBlockState(offset.below()).getBlock();
         if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND) {
            this.getLevel().setBlockAndUpdate(offset.below(), Blocks.GRASS_BLOCK.defaultBlockState());
            this.getLevel().setBlockAndUpdate(offset, Blocks.GRASS.defaultBlockState());
         }
      }
   }

   public void copyCrop(ICropTile prov) {
      this.breeding = false;
      this.crop = prov.getCrop();
      this.stage = 1;
      this.customData = new CompoundTag();
      this.growth = (byte)Mth.clamp(prov.getGrowthStat(), 0, 31);
      this.resistance = (byte)Mth.clamp(prov.getResistanceStat(), 0, 31);
      this.gain = (byte)Mth.clamp(prov.getGainStat(), 0, 31);
      this.scanLevel = (byte)prov.getScanLevel();
      this.updateTileFields(new String[]{"crop", "stage", "customData", "breeding"});
   }

   public boolean hasWeedEx() {
      if (this.exStorage > 0) {
         this.exStorage = Math.max(0, this.exStorage - 5);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean canBreed() {
      return this.crop == null ? false : this.crop.canBreed(this);
   }

   @Override
   public IFarmland getFarmland() {
      if (this.land == null) {
         this.land = CropRegistry.INSTANCE.getFarmland(this.getLevel().getBlockState(this.getBlockPos().below()).getBlock());
         if (this.land == null) {
            return new IFarmland() {
               @Override
               public int getNutrients(BlockState state) {
                  return 0;
               }

               @Override
               public int getHumidity(BlockState state) {
                  return 0;
               }
            };
         }
      }

      return this.land;
   }

   @Override
   public boolean isWaterLogged() {
      return (Boolean)this.getBlockState().getValue(BlockStateProperties.WATERLOGGED);
   }

   @Override
   public void setWaterlogged(boolean water) {
      this.withState(BlockStateProperties.WATERLOGGED, Boolean.valueOf(water));
   }

   @Override
   public ICrop getCrop() {
      return this.crop;
   }

   @Override
   public void setCrop(ICrop crop) {
      this.crop = crop;
      this.updateTileField("crop");
   }

   @Override
   public int getGrowthStage() {
      return this.stage;
   }

   @Override
   public void setGrowthStage(int stage) {
      this.stage = stage;
      this.updateTileField("stage");
   }

   @Override
   public int getGrowthPoints() {
      return this.growthPoints;
   }

   @Override
   public void setGrowthPoints(int points) {
      this.growthPoints = points;
      this.isSet = false;
   }

   @Override
   public int getScanLevel() {
      return this.scanLevel;
   }

   @Override
   public void setScanLevel(int level) {
      this.scanLevel = (byte)level;
   }

   @Override
   public boolean isCrossBreeding() {
      return this.breeding;
   }

   @Override
   public void setCrossBreeding(boolean breed) {
      this.breeding = breed;
      this.updateTileField("breeding");
   }

   @Override
   public int getGainStat() {
      return this.gain;
   }

   @Override
   public void setGainStat(int level) {
      this.gain = (byte)Mth.clamp(level, 0, 31);
   }

   @Override
   public int getGrowthStat() {
      return this.growth;
   }

   @Override
   public void setGrowthStat(int level) {
      this.growth = (byte)Mth.clamp(level, 0, 31);
   }

   @Override
   public int getResistanceStat() {
      return this.resistance;
   }

   @Override
   public void setResistanceStat(int level) {
      this.resistance = (byte)Mth.clamp(level, 0, 31);
   }

   @Override
   public void requestStateUpdate() {
      this.airQuality = -1;
      this.humidity = -1;
      this.nutrients = -1;
   }

   @Override
   public void onCustomDataChanged() {
      this.updateTileField("customData");
   }

   @Override
   public boolean performManualHarvest() {
      List<ItemStack> list = this.performHarvest(true);
      if (list == null || list.isEmpty()) {
         return false;
      } else if (this.isRendering()) {
         return true;
      } else {
         for (ItemStack stack : list) {
            Block.popResource(this.getLevel(), this.getBlockPos(), stack);
         }

         return true;
      }
   }

   @Override
   public List<ItemStack> performHarvest(boolean manual) {
      if (this.crop != null && this.crop.canBeHarvested(this)) {
         double chance = this.crop.getDropChance(this);

         for (int i = 0; i < this.gain; i++) {
            chance *= manual ? 1.1 : 1.03;
         }

         chance -= this.getLevel().random.nextDouble();

         int drops;
         for (drops = 0; chance > 0.0; chance -= this.getLevel().random.nextDouble()) {
            drops++;
         }

         ISeedCrop seed = this.crop instanceof ISeedCrop ? (ISeedCrop)this.crop : null;
         boolean doesDropSeed = seed != null && seed.isDroppingSeeds(this);
         List<ItemStack> results = CollectionUtils.<ItemStack>createList();

         for (int i = 0; i < drops; i++) {
            ItemStack[] result = doesDropSeed ? seed.getSeedDrops(this) : this.crop.getDrops(this);
            if (result != null) {
               for (ItemStack entry : result) {
                  if (this.getLevel().random.nextInt(100) <= this.gain) {
                     entry.grow(1);
                  }

                  results.add(entry.copy());
               }
            }
         }

         boolean canPerform = this.crop.canEmitRedstone(this);
         this.stage = this.crop.getAfterHarvestStage(this);
         if (canPerform || this.crop.canEmitRedstone(this)) {
            this.notifyChanges(true, DirectionList.ALL);
         }

         this.updateTileField("stage");
         return results;
      } else {
         return null;
      }
   }

   @Override
   public boolean pickCrop() {
      if (this.crop == null) {
         return false;
      } else {
         boolean bonus = this.crop.canBeHarvested(this);
         float firstChance = this.crop.getSeedDropChance(this);

         for (int i = 0; i < this.resistance; i++) {
            firstChance *= 1.1F;
         }

         int drop = 0;
         if (bonus) {
            if (this.getLevel().random.nextFloat() <= (firstChance + 1.0F) * 0.8F) {
               drop++;
            }

            float chance = this.crop.getSeedDropChance(this) + (float)this.growth / 100.0F;

            for (int i = 23; i < this.gain; i++) {
               chance *= 0.95F;
            }

            if (this.getLevel().random.nextFloat() <= chance) {
               drop++;
            }
         } else if (this.getLevel().random.nextFloat() <= firstChance * 1.5F) {
            drop++;
         }

         ItemStack[] result = new ItemStack[drop];

         for (int i = 0; i < drop; i++) {
            result[i] = this.crop.getSeeds(this);
         }

         this.removeCrop();
         if (this.isSimulating()) {
            for (ItemStack itemStack : result) {
               Block.popResource(this.getLevel(), this.getBlockPos(), itemStack);
            }
         }

         return true;
      }
   }

   @Override
   public void removeCrop() {
      this.crop = null;
      this.stage = 0;
      this.customData = new CompoundTag();
      this.gain = 0;
      this.resistance = 0;
      this.growth = 0;
      this.growthPoints = 0;
      this.breeding = false;
      this.scanLevel = 0;
      this.notifyChanges(true, DirectionList.ALL);
      this.updateTileFields(new String[]{"crop", "stage", "customData", "breeding"});
   }

   @Override
   public Set<Block> getBlocksBelow() {
      Set<Block> result = CollectionUtils.<Block>createLinkedSet();

      for (int i = 2; i < 6; i++) {
         BlockPos down = this.getBlockPos().below(i);
         if (this.getLevel().isEmptyBlock(down)) {
            return result;
         }

         result.add(this.getLevel().getBlockState(down).getBlock());
      }

      return result;
   }

   @Override
   public boolean isBlockBelow(BlockState state) {
      for (int i = 2; i < 6; i++) {
         BlockPos down = this.getBlockPos().below(i);
         if (this.getLevel().isEmptyBlock(down)) {
            return false;
         }

         if (this.getLevel().getBlockState(down) == state) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isBlockBelow(Block block) {
      for (int i = 2; i < 6; i++) {
         BlockPos down = this.getBlockPos().below(i);
         if (this.getLevel().isEmptyBlock(down)) {
            return false;
         }

         if (this.getLevel().getBlockState(down).getBlock() == block) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isBlockBelow(TagKey<Block> tag) {
      for (int i = 2; i < 6; i++) {
         BlockPos down = this.getBlockPos().below(i);
         if (this.getLevel().isEmptyBlock(down)) {
            return false;
         }

         if (this.getLevel().getBlockState(down).is(tag)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public CompoundTag getCustomData() {
      return this.customData;
   }

   @Override
   public int getLightLevel() {
      return this.getLevel().getMaxLocalRawBrightness(this.getBlockPos());
   }

   @Override
   public int getEnvironmentQuality() {
      if (this.airQuality == -1) {
         this.airQuality = this.updateAirQuality();
      }

      return this.airQuality;
   }

   public byte updateAirQuality() {
      MutableBlockPos target = new MutableBlockPos();
      if (this.isWaterLogged()) {
         int value = ICropRegistry.INSTANCE.getWaterQualityBonus(this.getLevel().getBiome(this.getBlockPos()));

         for (int x = this.worldPosition.getX() - 1; x < this.worldPosition.getX() + 1; x++) {
            for (int z = this.worldPosition.getZ() - 1; z < this.worldPosition.getZ() + 1; z++) {
               target.set(x, this.worldPosition.getY(), z);
               BlockState state = this.getLevel().getBlockState(target);
               FluidState fluid = state.getFluidState();
               if ((!(state.getBlock() instanceof BubbleColumnBlock) || !fluid.is(FluidTags.WATER)) && state.getBlock() != Blocks.CLAY) {
                  if (fluid.is(FluidTags.WATER) && this.getLevel().getBlockState(target.below()).getBlock() == Blocks.CLAY) {
                     value++;
                  }

                  BlockEntity tile = this.getLevel().getBlockEntity(target);
                  if (tile instanceof CropTileEntity && ((CropTileEntity)tile).isWaterCleaningCrop()) {
                     value++;
                  } else if (!fluid.is(FluidTags.WATER) && state.getBlock() != Blocks.WET_SPONGE && !state.isAir()) {
                     value--;
                  }
               } else {
                  value++;
               }
            }
         }

         return (byte)Mth.clamp(value, 0, 10);
      } else {
         BlockPos pos = this.getBlockPos();
         int value = Mth.clamp((pos.getY() - 64) / 15, 0, 5) + (this.getLevel().canSeeSkyFromBelowWater(pos.above()) ? 2 : 0);
         int fresh = 9;

         for (int x = pos.getX() - 1; x < pos.getX() + 1 && fresh > 0; x++) {
            for (int zx = pos.getZ() - 1; zx < pos.getZ() + 1 && fresh > 0; zx++) {
               target.set(x, pos.getY(), zx);
               if (!this.getLevel().isEmptyBlock(pos)
                  && (
                     this.getLevel().getBlockState(pos).isRedstoneConductor(this.getLevel(), target)
                        || this.getLevel().getBlockEntity(target) instanceof CropTileEntity
                  )) {
                  fresh--;
               }
            }
         }

         return (byte)Mth.clamp(value + fresh / 2, 0, 10);
      }
   }

   @Override
   public int getNutrients() {
      if (this.nutrients == -1) {
         this.nutrients = this.updateNutrients();
      }

      return this.nutrients;
   }

   public byte updateNutrients() {
      int value = ICropRegistry.INSTANCE.getNutrientBonus(this.getLevel().getBiome(this.getBlockPos()));
      value += this.getFarmland().getNutrients(this.getLevel(), this.getBlockPos().below());

      for (int i = 2; i < 5; i++) {
         ISubSoil soil = ICropRegistry.INSTANCE.getSubSoil(this.getLevel().getBlockState(this.getBlockPos().below(i)).getBlock());
         if (soil == null) {
            break;
         }

         value += soil.getNutrients(this.getLevel(), this.getBlockPos().below(i));
      }

      value += Math.min(10, (this.nutrientStorage + 19) / 20);
      return (byte)Mth.clamp(value, 0, 20);
   }

   @Override
   public int getHumidity() {
      if (this.humidity == -1) {
         this.humidity = this.updateHumidity();
      }

      return this.humidity;
   }

   public byte updateHumidity() {
      if (this.isWaterLogged()) {
         return 20;
      } else {
         int value = ICropRegistry.INSTANCE.getHumidityBonus(this.getLevel().getBiome(this.getBlockPos()));
         value += this.getFarmland().getHumidity(this.getLevel(), this.getBlockPos().below());

         for (int i = 2; i < 5; i++) {
            ISubSoil soil = ICropRegistry.INSTANCE.getSubSoil(this.getLevel().getBlockState(this.getBlockPos().below(i)).getBlock());
            if (soil == null) {
               break;
            }

            value += soil.getHumidity(this.getLevel(), this.getBlockPos().below(i));
         }

         if (this.waterStorage >= 5) {
            value += 2;
         }

         value += (this.waterStorage + 24) / 25;
         return (byte)Mth.clamp(value, 0, 20);
      }
   }

   protected boolean isWaterCleaningCrop() {
      return this.crop != null && this.crop.isWaterCleaningCrop(this);
   }

   @Override
   public int getWaterStorage() {
      return this.waterStorage;
   }

   @Override
   public void setWaterStorage(int water) {
      this.waterStorage = water;
   }

   @Override
   public int getFertilizerStorage() {
      return this.nutrientStorage;
   }

   @Override
   public void setFertilizerStorage(int fertilizer) {
      this.nutrientStorage = fertilizer;
   }

   @Override
   public int getWeedExStorage() {
      return this.exStorage;
   }

   @Override
   public void setWeedExStorage(int weedEx) {
      this.exStorage = weedEx;
   }

   public static int calculateRatioFor(ICrop a, ICrop b) {
      if (a == b) {
         return 500;
      } else {
         int value = 0;
         int i = 0;
         int[] aStats = a.getProperties().getAllProperties();

         for (int[] bStats = b.getProperties().getAllProperties(); i < 5; i++) {
            int c = Math.abs(aStats[i] - bStats[i]);
            switch (c) {
               default:
                  value--;
               case 0:
                  value += 2;
               case 1:
                  value++;
                  break;
               case 2:
            }
         }

         String[] first = a.getAttributes();
         String[] second = b.getAttributes();

         for (int var12 = 0; var12 < first.length; var12++) {
            for (String s : second) {
               if (first[var12].equalsIgnoreCase(s)) {
                  value += 5;
               }
            }
         }

         if (b.getProperties().getTier() < a.getProperties().getTier() - 1) {
            value -= 2 * (a.getProperties().getTier() - b.getProperties().getTier());
         }

         if (b.getProperties().getTier() - 3 > a.getProperties().getTier()) {
            value -= b.getProperties().getTier() - a.getProperties().getTier();
         }

         if (value < 0) {
            value = 0;
         }

         return value;
      }
   }

   public static int[] calculateRatiosFor(ICrop a, ICrop b, boolean waterlogged) {
      if (!a.getCropType().isCompatible(waterlogged)) {
         return new int[]{0, 0, 0, 0};
      } else if (a == b) {
         return new int[]{500, 0, 0, 0};
      } else {
         int value = 0;
         int i = 0;
         int properties = 0;
         int attributes = 0;
         int tier = 0;
         int[] aStats = a.getProperties().getAllProperties();

         for (int[] bStats = b.getProperties().getAllProperties(); i < 5; i++) {
            int c = Math.abs(aStats[i] - bStats[i]);
            switch (c) {
               default:
                  value--;
                  properties--;
               case 0:
                  value += 2;
                  properties += 2;
               case 1:
                  value++;
                  properties++;
                  break;
               case 2:
            }
         }

         String[] first = a.getAttributes();
         String[] second = b.getAttributes();

         for (int var16 = 0; var16 < first.length; var16++) {
            for (String s : second) {
               if (first[var16].equalsIgnoreCase(s)) {
                  value += 5;
                  attributes += 5;
               }
            }
         }

         if (b.getProperties().getTier() < a.getProperties().getTier() - 1) {
            int temp = 2 * (a.getProperties().getTier() - b.getProperties().getTier());
            value -= temp;
            tier -= temp;
         }

         if (b.getProperties().getTier() - 3 > a.getProperties().getTier()) {
            int temp = b.getProperties().getTier() - a.getProperties().getTier();
            value -= temp;
            tier -= temp;
         }

         if (value < 0) {
            value = 0;
         }

         return new int[]{value, properties, attributes, tier};
      }
   }

   @Override
   public ItemStack createSeeds(ICrop crop, int growthStat, int gainStat, int resistanceStat, int scanLevel) {
      return CropSeedItem.createStack(crop, growthStat, gainStat, resistanceStat, scanLevel);
   }

   public static enum BreedResult {
      NONE_FOUND,
      FOUND_CROP,
      FOUND_BREEDABLE;
   }
}
