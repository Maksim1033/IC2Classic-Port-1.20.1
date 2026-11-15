package ic2.core.block.misc.tiles;

import ic2.api.items.IDrinkContainer;
import ic2.api.items.readers.IThermometer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.registries.IPotionBrewRegistry;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.BaseComparator;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.special.BarrelComparator;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.food_and_drink.IC2FoodsAndDrinks;
import ic2.core.item.food_and_drink.drinks.Beer;
import ic2.core.item.food_and_drink.drinks.Rum;
import ic2.core.item.food_and_drink.drinks.Whisky;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class BarrelTileEntity extends BaseTileEntity implements ITickListener, IClickable, IDropProvider {
   public static final int[] POTION_DURATIONS = new int[]{1800, 1500, 1200, 960, 720, 600, 300, 150, 90, 30, 15};
   public static final int MAX_POTION_AMPLIFIERS = 20;
   public static final int FLUID_CAPACITY = 32000;
   public static final int POTION_FLUID_CAPACITY = 3000;
   private boolean hasTreeTap = false;
   @NetworkInfo
   public boolean opened = false;
   public int brewType = 0;
   public int fluidAmount = 0;
   public int wheatAmount = 0;
   public int hopsAmount = 0;
   public int age = 0;
   public int beerQuality = 0;
   public int alcoholLevel = 0;
   public int solidRatio = 0;
   public MobEffect potionType;

   public BarrelTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"opened"});
      this.addComparator(new BarrelComparator("stage", ComparatorNames.BREW_STAGE, this, true));
      this.addComparator(new BarrelComparator("progress", ComparatorNames.BREW_STAGE_PROGRESS, this, false));
   }

   public int getStageProgress() {
      switch (this.brewType) {
         case 1:
            return BaseComparator.value((float)this.age, (float)(24000.0 * Math.pow(3.0, this.beerQuality == 4 ? 6.0 : (double)this.beerQuality)), 15);
         case 2:
            return BaseComparator.value((float)this.age, (float)this.timeNeededForRum(), 15);
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return 0;
         case 5:
            return BaseComparator.value((float)this.age, 1728000.0F, 15);
         case 10:
            return BaseComparator.value((float)this.age, (float)(5000.0 * Math.pow(3.0, (double)this.beerQuality)), 15);
      }
   }

   public int getStage() {
      return this.beerQuality;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.BARREL;
   }

   @Override
   public void onTick() {
      if (!this.isEmpty() && !this.hasTreeTap && !this.opened) {
         this.age++;
         if (this.brewType == 1 && this.beerQuality < 5) {
            if ((double)this.age >= 24000.0 * Math.pow(3.0, this.beerQuality == 4 ? 6.0 : (double)this.beerQuality)) {
               this.age = 0;
               this.beerQuality++;
            }
         } else if (this.brewType == 10 && this.beerQuality < 6) {
            if ((double)this.age >= 5000.0 * Math.pow(3.0, (double)this.beerQuality)) {
               this.age = 0;
               this.beerQuality++;
            }
         } else if (this.brewType == 5 && this.age >= this.getWhiskBrewTime() && this.beerQuality < 50) {
            this.age = 0;
            this.beerQuality++;
         }
      }

      this.handleComparators();
   }

   public ItemStack getBarrelStack() {
      if (this.fluidAmount <= 0) {
         return new ItemStack(IC2Blocks.BARREL);
      } else {
         ItemStack stack = new ItemStack(IC2Blocks.BARREL);
         if (this.hasCustomName()) {
            stack.setHoverName(this.getCustomName());
         }

         CompoundTag data = stack.getOrCreateTag();
         data.putInt("type", this.brewType);
         switch (this.brewType) {
            case 0:
               return new ItemStack(IC2Blocks.BARREL);
            case 1:
               data.putInt("data", Beer.createData(this.beerQuality, this.getSolidRatio(), this.getAlcoholLevel()));
               data.putInt("hops", this.hopsAmount);
               data.putInt("wheat", this.wheatAmount);
               break;
            case 2:
               data.putInt("data", Mth.clamp(this.age * 100 / this.timeNeededForRum(), 0, 100));
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            default:
               break;
            case 5:
               data.putInt("data", this.opened ? this.alcoholLevel : this.calculateWhiskyYears());
               data.putInt("hops", this.hopsAmount);
               break;
            case 10:
               data.putString("potion", ForgeRegistryWrapper.getMobEffectKey(this.potionType).toString());
               data.putInt("data", this.calculatePotionData());
               data.putInt("red", this.hopsAmount);
               data.putInt("glow", this.wheatAmount);
               data.putInt("quality", this.beerQuality);
         }

         data.putInt("fluid", this.fluidAmount);
         return stack;
      }
   }

   protected int calculatePotionData() {
      if (this.opened) {
         return (this.solidRatio & 15) << 4 | this.alcoholLevel & 15;
      } else {
         int solidRatio = this.beerQuality > 0 && this.wheatAmount > 0
            ? (int)Mth.clamp((float)this.beerQuality * ((float)this.wheatAmount / 10.0F), 0.0F, (float)(POTION_DURATIONS.length - 1))
            : 0;
         int alcoholLevel = this.beerQuality > 0 && this.hopsAmount > 0
            ? (int)Mth.clamp((float)this.beerQuality * 0.5F * ((float)this.hopsAmount / 10.0F), 0.0F, 2.0F)
            : 0;
         switch (alcoholLevel) {
            case 1:
               solidRatio = (int)((float)solidRatio * 0.5F);
               break;
            case 2:
               solidRatio = (int)((float)solidRatio * 0.25F);
         }

         return (solidRatio & 15) << 4 | alcoholLevel & 15;
      }
   }

   public void loadItemData(ItemStack stack) {
      if (stack.hasCustomHoverName()) {
         this.setCustomName(stack.getHoverName());
      }

      CompoundTag data = StackUtil.getNbtData(stack);
      int amount = data.getInt("fluid");
      if (amount > 0) {
         this.opened = true;
         this.updateTileField("opened");
         this.fluidAmount = amount;
         this.brewType = data.getInt("type");
         switch (this.brewType) {
            case 1:
               this.beerQuality = Beer.getTime(data.getInt("data"));
               this.solidRatio = Beer.getSolid(data.getInt("data"));
               this.alcoholLevel = Beer.getHops(data.getInt("data"));
               this.hopsAmount = data.getInt("hops");
               this.wheatAmount = data.getInt("wheat");
               break;
            case 2:
               this.opened = false;
               this.age = (int)((double)this.timeNeededForRum() * ((double)data.getInt("data") / 100.0));
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            default:
               break;
            case 5:
               this.alcoholLevel = data.getInt("data");
               this.hopsAmount = data.getInt("hops");
               break;
            case 10:
               this.potionType = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(data.getString("potion")));
               this.solidRatio = data.getInt("data") >> 4 & 15;
               this.alcoholLevel = data.getInt("data") & 15;
               this.hopsAmount = data.getInt("red");
               this.wheatAmount = data.getInt("glow");
         }
      }
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      drops.add(new ItemStack(IC2Blocks.SCAFFOLD_WOOD));
   }

   @Override
   public IClickable.ClickAction getRequiredActions() {
      return IClickable.ClickAction.BOTH;
   }

   public Component getBrewType() {
      return this.translate(switch (this.brewType) {
         case 0 -> "ic2.probe.barrel.status.empty.name";
         case 1 -> "ic2.probe.barrel.status.beer.name";
         case 2 -> "ic2.probe.barrel.status.rum.name";
         default -> "I AM ERROR";
         case 5 -> "ic2.probe.barrel.status.whisky.name";
         case 10 -> "ic2.probe.barrel.status.potion.name";
      });
   }

   @Override
   public boolean onLeftClick(Player player, BlockPos pos) {
      if (this.hasTreeTap) {
         this.setHasTreeTap(false);
         this.reduceFluidAmount(1000);
         StackUtil.addOrDrop(player, new ItemStack(IC2Items.TREETAP));
         return true;
      } else {
         if (!this.opened) {
            this.openBarrel();
         }

         Block.popResource(this.getLevel(), this.getBlockPos(), this.getBarrelStack());
         this.getLevel().setBlockAndUpdate(this.worldPosition, IC2Blocks.SCAFFOLD_WOOD.defaultBlockState());
         return true;
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack currentItem = player.getItemInHand(hand);
      if (currentItem.isEmpty()) {
         if (player.isShiftKeyDown()) {
            this.setFacing(side);
            return true;
         } else {
            return false;
         }
      } else if (IThermometer.isThermometerImpl(currentItem)) {
         if (this.isSimulating()) {
            if (!player.isCreative()) {
               currentItem.hurt(1, player.getRandom(), (ServerPlayer)player);
            }

            player.displayClientMessage(this.getBrewType(), false);
            switch (this.brewType) {
               case 0:
               case 3:
               case 4:
               case 6:
               case 7:
               case 8:
               case 9:
               default:
                  break;
               case 1: {
                  player.displayClientMessage(this.translate("ic2.probe.barrel.beer.wheat.name", ChatFormatting.YELLOW, new Object[]{this.wheatAmount}), false);
                  player.displayClientMessage(this.translate("ic2.probe.barrel.beer.hops.name", ChatFormatting.GREEN, new Object[]{this.hopsAmount}), false);
                  player.displayClientMessage(
                     ((MutableComponent)new FluidStack(Fluids.WATER, 1).getDisplayName())
                        .append(": ")
                        .append(Formatters.EU_FORMAT.format((long)this.fluidAmount))
                        .append("/")
                        .append(Formatters.EU_FORMAT.format(32000L))
                        .append(" mB")
                        .withStyle(ChatFormatting.AQUA),
                     false
                  );
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.beer.quality." + this.beerQuality + ".name", new ChatFormatting[]{ChatFormatting.BLUE}), false
                  );
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.beer.alc." + this.getAlcoholLevel() + ".name", new ChatFormatting[]{ChatFormatting.GREEN}), false
                  );
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.beer.solid." + this.getSolidRatio() + ".name", new ChatFormatting[]{ChatFormatting.YELLOW}), false
                  );
                  double max = 24000.0 * Math.pow(3.0, this.beerQuality == 4 ? 6.0 : (double)this.beerQuality);
                  player.displayClientMessage(
                     this.translate(
                        "ic2.probe.barrel.status.progress.name", ChatFormatting.BLUE, new Object[]{Formatters.BREWING.format((double)this.age / max * 100.0)}
                     ),
                     false
                  );
                  break;
               }
               case 2: {
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.beer.sugar_cane.name", ChatFormatting.GREEN, new Object[]{this.fluidAmount / 1000}), false
                  );
                  int max = this.timeNeededForRum();
                  player.displayClientMessage(
                     this.translate(
                        "ic2.probe.barrel.status.progress.name",
                        ChatFormatting.BLUE,
                        new Object[]{Formatters.BREWING.format((double)Math.min(this.age, max) * 100.0 / (double)max)}
                     ),
                     false
                  );
                  break;
               }
               case 5:
                  player.displayClientMessage(this.translate("ic2.probe.barrel.whisky.grist.name", ChatFormatting.GREEN, new Object[]{this.hopsAmount}), false);
                  player.displayClientMessage(
                     ((MutableComponent)new FluidStack(Fluids.WATER, 1).getDisplayName())
                        .append(": ")
                        .append(Formatters.EU_FORMAT.format((long)this.fluidAmount))
                        .append("/")
                        .append(Formatters.EU_FORMAT.format(32000L))
                        .append(" mB")
                        .withStyle(ChatFormatting.AQUA),
                     false
                  );
                  player.displayClientMessage(this.translate("ic2.probe.barrel.whisky.years.name", ChatFormatting.BLUE, new Object[]{this.beerQuality}), false);
                  player.displayClientMessage(
                     this.translate(
                        "ic2.probe.barrel.status.progress.name",
                        ChatFormatting.BLUE,
                        new Object[]{Formatters.BREWING.format((double)this.age / ((double)this.getWhiskBrewTime() / 100.0))}
                     ),
                     false
                  );
                  break;
               case 10: {
                  player.displayClientMessage(this.translate("ic2.probe.barrel.beer.redstone.name", ChatFormatting.RED, new Object[]{this.wheatAmount}), false);
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.beer.glowstone.name", ChatFormatting.YELLOW, new Object[]{this.hopsAmount}), false
                  );
                  player.displayClientMessage(
                     ((MutableComponent)new FluidStack(Fluids.WATER, 1).getDisplayName())
                        .append(": ")
                        .append(Formatters.EU_FORMAT.format((long)this.fluidAmount))
                        .append("/")
                        .append(Formatters.EU_FORMAT.format(3000L))
                        .append(" mB")
                        .withStyle(ChatFormatting.AQUA),
                     false
                  );
                  player.displayClientMessage(this.translate("ic2.probe.barrel.status.output.name", new Object[]{this.getBrewedPotion()}), false);
                  player.displayClientMessage(
                     this.translate("ic2.probe.barrel.potion.quality." + this.beerQuality + ".name", new ChatFormatting[]{ChatFormatting.BLUE}), false
                  );
                  double max = 5000.0 * Math.pow(3.0, (double)this.beerQuality);
                  player.displayClientMessage(
                     this.translate(
                        "ic2.probe.barrel.status.progress.name", ChatFormatting.BLUE, new Object[]{Formatters.BREWING.format((double)this.age / max * 100.0)}
                     ),
                     false
                  );
               }
            }
         }

         return true;
      } else if (currentItem.is(Items.NAME_TAG)) {
         if (currentItem.hasCustomHoverName() && !currentItem.getHoverName().equals(this.getCustomName())) {
            this.setCustomName(currentItem.getHoverName());
            return true;
         } else if (!currentItem.hasCustomHoverName() && this.hasCustomName()) {
            this.setCustomName(null);
            return true;
         } else {
            return false;
         }
      } else if (currentItem.is(ItemTags.WOOL)) {
         int useAmount = this.calculateAmount(currentItem, player, this.fluidAmount / 1000, 0);
         this.reduceFluidAmount(useAmount * 1000);
         currentItem.shrink(useAmount);
         return true;
      } else if (currentItem.getItem() == Items.SPONGE) {
         this.reduceFluidAmount(this.getFluidCapacity());
         currentItem.shrink(1);
         StackUtil.addOrDrop(player, new ItemStack(Items.WET_SPONGE, 1));
         return true;
      } else if (!this.opened && !this.hasTreeTap) {
         if (currentItem.getItem() != Items.WATER_BUCKET
            || !this.canApplyType(1) && !this.canApplyType(5) && (!this.canApplyType(10) || this.potionType == null)) {
            if (currentItem.getItem() != IC2Items.CELL_WATER
               || !this.canApplyType(1) && !this.canApplyType(5) && (!this.canApplyType(10) || this.potionType == null)) {
               if (currentItem.getItem() == Items.WHEAT && this.canApplyType(1)) {
                  this.brewType = 1;
                  this.wheatAmount = this.wheatAmount + this.calculateAmount(currentItem, player, 64, this.wheatAmount);
                  this.alterComposition();
                  return true;
               } else if (currentItem.getItem() == IC2Items.HOPS && this.canApplyType(1)) {
                  this.brewType = 1;
                  this.hopsAmount = this.hopsAmount + this.calculateAmount(currentItem, player, 64, this.hopsAmount);
                  this.alterComposition();
                  return true;
               } else if (currentItem.getItem() == Items.SUGAR_CANE && this.canApplyType(2)) {
                  if (this.age <= 600 && this.brewType <= 0) {
                     this.brewType = 2;
                     this.fluidAmount = this.fluidAmount + this.calculateAmount(currentItem, player, 32, this.fluidAmount / 1000) * 1000;
                     return true;
                  } else {
                     return false;
                  }
               } else if (currentItem.getItem() == IC2Items.WHISKY_GRIST && this.canApplyType(5)) {
                  if (this.age <= 600 && this.beerQuality <= 0) {
                     this.brewType = 5;
                     this.hopsAmount = this.hopsAmount + this.calculateAmount(currentItem, player, 16, this.hopsAmount);
                     return true;
                  } else {
                     return false;
                  }
               } else if (currentItem.getItem() == Items.NETHER_WART && this.canApplyType(10)) {
                  if (this.brewType > 0) {
                     return false;
                  } else {
                     this.brewType = 10;
                     if (!player.isCreative()) {
                        currentItem.shrink(1);
                     }

                     return true;
                  }
               } else if (this.canApplyType(10)
                  && this.age <= 0
                  && RecipeRegistry.POTION_BREWING.get(this.isSimulating()).getEffect(currentItem.getItem(), this.potionType) != null) {
                  this.potionType = RecipeRegistry.POTION_BREWING.get(this.isSimulating()).getEffect(currentItem.getItem(), this.potionType);
                  if (!player.isCreative()) {
                     currentItem.shrink(1);
                  }

                  return true;
               } else if (!this.canApplyType(10)
                  || this.potionType == null
                  || this.age > 0
                  || !currentItem.is(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE)
                     && !currentItem.is(net.minecraftforge.common.Tags.Items.DUSTS_GLOWSTONE)) {
                  if (currentItem.getItem() == Items.WRITABLE_BOOK && this.brewType != 0 && (this.brewType != 10 || this.potionType != null)) {
                     MutableComponent text = this.string("Type: " + this.getBrewName()).append("\n");
                     CompoundTag info = new CompoundTag();
                     info.putInt("type", this.brewType);
                     switch (this.brewType) {
                        case 1:
                           text.append("Wheat: " + this.wheatAmount).append("\n");
                           text.append("Hops: " + this.hopsAmount).append("\n");
                           info.putInt("hops", this.hopsAmount);
                           info.putInt("wheat", this.wheatAmount);
                           break;
                        case 5:
                           text.append("Grist: " + this.hopsAmount).append("\n");
                           info.putInt("grist", this.hopsAmount);
                           break;
                        case 10:
                           text.append("Effect: ").append(this.potionType.getDisplayName()).append("\n");
                           text.append("Redstone: " + this.wheatAmount).append("\n");
                           text.append("Glowstone: " + this.hopsAmount).append("\n");
                           info.putString("effect", ForgeRegistryWrapper.getMobEffectKey(this.potionType).toString());
                           info.putInt("red", this.wheatAmount);
                           info.putInt("glow", this.hopsAmount);
                     }

                     text.append("Water: " + this.fluidAmount / 1000);
                     info.putInt("water", this.fluidAmount / 1000);
                     ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
                     CompoundTag data = new CompoundTag();
                     ListTag list = new ListTag();
                     list.add(StringTag.valueOf(Serializer.toJson(text)));
                     data.put("pages", list);
                     data.put("info", info);
                     data.putString("author", player.getName().getString());
                     data.putString("title", "Brew Recipe");
                     stack.setTag(data);
                     if (!player.isCreative()) {
                        currentItem.shrink(1);
                     }

                     player.addItem(stack);
                     return true;
                  } else if (currentItem.getItem() == Items.WRITTEN_BOOK) {
                     CompoundTag data = StackUtil.getNbtData(currentItem);
                     if (!data.contains("info")) {
                        return false;
                     } else {
                        data = data.getCompound("info");
                        int type = data.getInt("type");
                        if (type != 0 && this.canApplyType(type)) {
                           List<ItemStack> list = this.getItems(type, data);
                           if (list.isEmpty()) {
                              return false;
                           } else {
                              IItemTransporter transporter = TransporterManager.getTransporter(player);
                              if (transporter == null) {
                                 return false;
                              } else {
                                 List<ItemStack> leftOver = this.consumeItems(list, transporter, true);
                                 if (leftOver.isEmpty()) {
                                    this.consumeItems(list, transporter, player.isCreative());
                                    this.brewType = type;
                                    switch (this.brewType) {
                                       case 1:
                                          this.hopsAmount = data.getInt("hops");
                                          this.wheatAmount = data.getInt("wheat");
                                          this.fluidAmount = data.getInt("water") * 1000;
                                          break;
                                       case 2:
                                          this.fluidAmount = data.getInt("water") * 1000;
                                       case 3:
                                       case 4:
                                       case 6:
                                       case 7:
                                       case 8:
                                       case 9:
                                       default:
                                          break;
                                       case 5:
                                          this.hopsAmount = data.getInt("grist");
                                          this.fluidAmount = data.getInt("water") * 1000;
                                          break;
                                       case 10:
                                          this.potionType = (MobEffect)ForgeRegistries.MOB_EFFECTS
                                             .getValue(ResourceLocation.tryParse(data.getString("effect")));
                                          this.hopsAmount = data.getInt("glow");
                                          this.wheatAmount = data.getInt("red");
                                          this.fluidAmount = data.getInt("water") * 1000;
                                    }

                                    if (data.getInt("water") > 0) {
                                       transporter.addItem(new ItemStack(IC2Items.CELL_EMPTY, data.getInt("water")), Direction.DOWN, false);
                                    }

                                    return true;
                                 } else {
                                    player.displayClientMessage(this.string("Missing Items:"), false);

                                    for (ItemStack stack : leftOver) {
                                       player.displayClientMessage(stack.getDisplayName().copy().append(stack.getCount() + "x"), false);
                                    }

                                    return true;
                                 }
                              }
                           }
                        } else {
                           return false;
                        }
                     }
                  } else {
                     return false;
                  }
               } else {
                  if (currentItem.is(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE)) {
                     this.wheatAmount = this.wheatAmount + this.calculateAmount(currentItem, player, 20, this.wheatAmount);
                  } else {
                     this.hopsAmount = this.hopsAmount + this.calculateAmount(currentItem, player, 20, this.hopsAmount);
                  }

                  return true;
               }
            } else if (this.fluidAmount < this.getFluidCapacity() && this.age <= 600) {
               if (this.brewType == 0) {
                  this.brewType = 1;
               }

               ItemStack empty = new ItemStack(
                  IC2Items.CELL_EMPTY, this.calculateAmount(currentItem, player, this.getFluidCapacity() / 1000, this.fluidAmount / 1000)
               );
               if (empty.getCount() <= 0) {
                  return false;
               } else {
                  this.fluidAmount = this.fluidAmount + empty.getCount() * 1000;
                  StackUtil.addOrDrop(player, empty);
                  return true;
               }
            } else {
               return false;
            }
         } else if (this.fluidAmount < this.getFluidCapacity() && this.age <= 600) {
            if (this.brewType == 0) {
               this.brewType = 1;
            }

            this.fluidAmount += 1000;
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            return true;
         } else {
            return false;
         }
      } else if (this.hasTreeTap()
         && (
            this.isDrinkContainer(currentItem)
               || this.brewType == 10 && RecipeRegistry.POTION_BREWING.get(this.isSimulating()).getPotionContainer(currentItem.getItem()) != null
         )) {
         ItemStack result = this.drainAmount(this.brewType != 10 && PlayerHandler.getHandler(player).altKeyDown ? currentItem.getCount() : 1, currentItem);
         if (!result.isEmpty()) {
            int count = result.getCount();
            StackUtil.addOrDrop(player, result);
            if (!player.isCreative()) {
               currentItem.shrink(count);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private List<ItemStack> getItems(int type, CompoundTag data) {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      switch (type) {
         case 1:
            this.addIfEnough(data.getInt("hops"), IC2Items.HOPS, list);
            this.addIfEnough(data.getInt("wheat"), Items.WHEAT, list);
            this.addIfEnough(data.getInt("water"), IC2Items.CELL_WATER, list);
            break;
         case 2:
            this.addIfEnough(data.getInt("water"), Items.SUGAR_CANE, list);
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            break;
         case 5:
            this.addIfEnough(data.getInt("grist"), IC2Items.WHISKY_GRIST, list);
            this.addIfEnough(data.getInt("water"), IC2Items.CELL_WATER, list);
            break;
         case 10:
            List<Item> items = RecipeRegistry.POTION_BREWING
               .get(this.isSimulating())
               .getRequiredItems((MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(data.getString("effect"))));
            if (items.isEmpty()) {
               return ObjectLists.emptyList();
            }

            for (Item item : items) {
               list.add(new ItemStack(item));
            }

            list.add(new ItemStack(Items.NETHER_WART));
            this.addIfEnough(data.getInt("red"), Items.REDSTONE, list);
            this.addIfEnough(data.getInt("glow"), Items.GLOWSTONE_DUST, list);
            this.addIfEnough(data.getInt("water"), IC2Items.CELL_WATER, list);
      }

      return list;
   }

   private void addIfEnough(int amount, Item item, List<ItemStack> result) {
      if (amount > 0) {
         result.add(new ItemStack(item, amount));
      }
   }

   private List<ItemStack> consumeItems(List<ItemStack> results, IItemTransporter transporter, boolean simulate) {
      List<ItemStack> leftOver = CollectionUtils.<ItemStack>createList();

      for (ItemStack stack : results) {
         ItemStack found = transporter.removeItem(StackFilter.defaultCompare(stack), Direction.DOWN, stack.getCount(), simulate);
         if (stack.getCount() != found.getCount()) {
            leftOver.add(StackUtil.copyWithSize(stack, stack.getCount() - found.getCount()));
         }
      }

      return leftOver;
   }

   private String getBrewName() {
      switch (this.brewType) {
         case 1:
            return "Beer";
         case 2:
            return "Rum";
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return "Unknown";
         case 5:
            return "Whisky";
         case 10:
            return "Potion";
      }
   }

   private int getFluidCapacity() {
      return this.brewType == 10 ? 3000 : 32000;
   }

   private int calculateAmount(ItemStack currentItem, Player player, int capacity, int amount) {
      int useAmount = Math.min(player.isShiftKeyDown() ? 1 : currentItem.getCount(), capacity - amount);
      if (!player.isCreative()) {
         currentItem.shrink(useAmount);
      }

      return useAmount;
   }

   private void alterComposition() {
      switch (this.beerQuality) {
         case 0:
            this.age = 0;
            break;
         case 1:
            if (this.getLevel().random.nextBoolean()) {
               this.beerQuality = 0;
            } else if (this.getLevel().random.nextBoolean()) {
               this.beerQuality = 5;
            }
            break;
         case 2:
            if (this.getLevel().random.nextBoolean()) {
               this.beerQuality = 5;
            }
            break;
         default:
            this.beerQuality = 5;
      }
   }

   public void openBarrel() {
      if (!this.isEmpty()) {
         this.opened = true;
         this.updateTileField("opened");
         if (this.brewType == 1) {
            this.calculateAlcoholLevel();
            this.calculateSolidRatio();
         } else if (this.brewType == 10) {
            if (this.beerQuality == 6) {
               AreaEffectCloud cloud = new AreaEffectCloud(
                  this.getLevel(),
                  (double)((float)this.getBlockPos().getX() + 0.5F),
                  (double)((float)this.getBlockPos().getY() + 0.5F),
                  (double)((float)this.getBlockPos().getZ() + 0.5F)
               );
               cloud.setRadius(5.0F);
               cloud.setRadiusOnUse(0.0F);
               cloud.setWaitTime(10);
               cloud.setDuration(1200);
               cloud.setRadiusPerTick(0.0F);
               cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 3));
               this.getLevel().addFreshEntity(cloud);
               this.reduceFluidAmount(this.fluidAmount);
               return;
            }

            this.calculatePotionTime();
         } else if (this.brewType == 5) {
            this.fluidAmount = (int)((double)this.fluidAmount * (1.0 - (double)this.beerQuality / 100.0));
            this.fluidAmount = (int)((double)this.fluidAmount * ((double)this.hopsAmount / 16.0));
            this.alcoholLevel = this.calculateWhiskyYears();
         }
      }
   }

   public int calculateWhiskyYears() {
      int yearId = 0;
      if (this.beerQuality >= 50) {
         yearId = 5;
      } else if (this.beerQuality >= 25) {
         yearId = 4;
      } else if (this.beerQuality >= 15) {
         yearId = 3;
      } else if (this.beerQuality >= 12) {
         yearId = 2;
      } else if (this.beerQuality >= 10) {
         yearId = 1;
      }

      return yearId;
   }

   private void calculatePotionTime() {
      this.solidRatio = this.beerQuality > 0 && this.wheatAmount > 0
         ? (int)Mth.clamp((float)this.beerQuality * ((float)this.wheatAmount / 10.0F), 0.0F, (float)(POTION_DURATIONS.length - 1))
         : 0;
      this.alcoholLevel = this.beerQuality > 0 && this.hopsAmount > 0
         ? (int)Mth.clamp((float)this.beerQuality * 0.5F * ((float)this.hopsAmount / 10.0F), 0.0F, 2.0F)
         : 0;
      switch (this.alcoholLevel) {
         case 1:
            this.solidRatio = (int)((float)this.solidRatio * 0.5F);
            break;
         case 2:
            this.solidRatio = (int)((float)this.solidRatio * 0.25F);
      }
   }

   private void calculateAlcoholLevel() {
      float hopsToWheat = this.wheatAmount > 0 ? (float)this.hopsAmount / (float)this.wheatAmount : (this.hopsAmount > 0 ? 10.0F : 0.0F);
      this.alcoholLevel = 0;
      if (hopsToWheat >= 5.0F) {
         this.beerQuality = 5;
      } else if (hopsToWheat >= 4.0F) {
         this.alcoholLevel = 6;
      } else if (hopsToWheat >= 3.0F) {
         this.alcoholLevel = 5;
      } else if (hopsToWheat >= 2.0F) {
         this.alcoholLevel = 4;
      } else if (hopsToWheat > 0.5F) {
         this.alcoholLevel = 3;
      } else if (hopsToWheat > 0.33333334F) {
         this.alcoholLevel = 2;
      } else if (hopsToWheat > 0.25F) {
         this.alcoholLevel = 1;
      }
   }

   private ItemStack drainAmount(int amount, ItemStack stack) {
      if (!this.opened) {
         this.openBarrel();
      }

      switch (this.brewType) {
         case 1:
            return this.drainBeer(amount, stack);
         case 2:
            return this.drainRum(amount, stack);
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return ItemStack.EMPTY;
         case 5:
            return this.drainWhisky(amount, stack);
         case 10:
            return this.drainPotion(amount, stack);
      }
   }

   private ItemStack drainBeer(int amount, ItemStack stack) {
      if (this.isDrinkContainer(stack)) {
         IDrinkContainer container = (IDrinkContainer)stack.getItem();
         amount = Math.min(amount, this.fluidAmount / container.getCapacity());
         if (amount <= 0) {
            return ItemStack.EMPTY;
         } else {
            ItemStack result = container.fillWith(IC2FoodsAndDrinks.BEER, amount);
            if (result.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               Beer.createData(result, this.beerQuality, this.solidRatio, this.alcoholLevel);
               if (this.hasCustomName()) {
                  StackUtil.addTooltip(result, this.getCustomName().getString());
               }

               this.reduceFluidAmount(amount * container.getCapacity());
               return result;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack drainWhisky(int amount, ItemStack stack) {
      if (this.isDrinkContainer(stack)) {
         IDrinkContainer container = (IDrinkContainer)stack.getItem();
         amount = Math.min(amount, Math.max(1, this.fluidAmount / container.getCapacity()));
         if (amount > 0 && this.fluidAmount > 0) {
            ItemStack result = container.fillWith(IC2FoodsAndDrinks.WHISKY, amount);
            if (result.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               Whisky.createYear(result, this.alcoholLevel);
               if (this.hasCustomName()) {
                  StackUtil.addTooltip(result, this.getCustomName().getString());
               }

               this.reduceFluidAmount(amount * container.getCapacity());
               return result;
            }
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack drainRum(int amount, ItemStack stack) {
      if (this.isDrinkContainer(stack)) {
         IDrinkContainer container = (IDrinkContainer)stack.getItem();
         amount = Math.min(amount, this.fluidAmount / container.getCapacity());
         if (amount <= 0) {
            return ItemStack.EMPTY;
         } else {
            ItemStack result = container.fillWith(IC2FoodsAndDrinks.RUM, amount);
            if (result.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               if (this.hasCustomName()) {
                  StackUtil.addTooltip(result, this.getCustomName().getString());
               }

               int progress = this.age * 100 / this.timeNeededForRum();
               this.reduceFluidAmount(amount * container.getCapacity());
               this.age = (int)((float)progress / 100.0F * (float)this.timeNeededForRum());
               Rum.createData(result, progress);
               return result;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack drainPotion(int amount, ItemStack stack) {
      if (this.potionType != null) {
         IPotionBrewRegistry.PotionContainer output = RecipeRegistry.POTION_BREWING.get(this.isSimulating()).getPotionContainer(stack.getItem());
         if (output == null) {
            return ItemStack.EMPTY;
         } else {
            amount = Math.min(amount, this.fluidAmount / output.getFluidUsage());
            if (amount <= 0) {
               return ItemStack.EMPTY;
            } else {
               List<MobEffectInstance> effects = ObjectLists.singleton(
                  new MobEffectInstance(
                     this.potionType,
                     (int)Math.max(20.0F, (float)POTION_DURATIONS[10 - this.solidRatio] * 20.0F * output.getDurationEffectiveness()),
                     this.alcoholLevel
                  )
               );
               ItemStack result = PotionUtils.setCustomEffects(new ItemStack(output.getOutput()), effects);
               result.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(effects));
               Component name = this.translate(
                  output.getOutput().getDescriptionId() + ".effect." + RecipeRegistry.POTION_BREWING.get(this.isSimulating()).getName(this.potionType)
               );
               name.getStyle().withItalic(false);
               result.setHoverName(name);
               this.reduceFluidAmount(amount * output.getFluidUsage());
               if (this.hasCustomName()) {
                  StackUtil.addTooltip(result, this.getCustomName().getString());
               }

               return result;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static Component getDescription(MobEffectInstance effect, float durationFactor) {
      MutableComponent text = Component.translatable(effect.getDescriptionId());
      if (effect.getAmplifier() > 0) {
         text.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
      }

      if (effect.getDuration() > 20) {
         text.append(" (").append(effect.isInfiniteDuration() ? "**:**" : ticksToElapsedTime(Mth.floor((float)effect.getDuration() * durationFactor))).append(")");
      }

      text.getStyle().withItalic(false);
      return Component.translatable("tooltip.item.ic2.tipped_arrow.name", new Object[]{text.withStyle(effect.getEffect().getCategory().getTooltipFormatting())});
   }

   public static String ticksToElapsedTime(int ticks) {
      int i = ticks / 20;
      int j = i / 60;
      i %= 60;
      return i < 10 ? j + ":0" + i : j + ":" + i;
   }

   public int getWhiskBrewTime() {
      return FMLEnvironment.dist.isClient() && this.isSimulating() && IC2.CONFIG.reducedWhiskyBrewTime.get() ? 120960 : 1209600;
   }

   private boolean isDrinkContainer(ItemStack item) {
      return item.getItem() instanceof IDrinkContainer && !((IDrinkContainer)item.getItem()).hasContent(item);
   }

   private boolean canApplyType(int type) {
      return this.brewType == 0 || this.brewType == type;
   }

   private void reduceFluidAmount(int amount) {
      this.fluidAmount -= amount;
      if (this.fluidAmount <= 0) {
         if (this.brewType == 1) {
            this.hopsAmount = 0;
            this.wheatAmount = 0;
            this.alcoholLevel = 0;
            this.beerQuality = 0;
            this.solidRatio = 0;
         } else if (this.brewType == 10) {
            this.hopsAmount = 0;
            this.wheatAmount = 0;
            this.alcoholLevel = 0;
            this.beerQuality = 0;
            this.solidRatio = 0;
            this.potionType = null;
         }

         this.age = 0;
         this.opened = false;
         this.updateTileField("opened");
         this.fluidAmount = 0;
         this.brewType = 0;
      }
   }

   public int timeNeededForRum() {
      return (int)(1200.0 * (double)Math.max(1, this.fluidAmount / 1000) * Math.pow(0.95, (double)Math.max(0, this.fluidAmount / 1000 - 1)));
   }

   private void calculateSolidRatio() {
      float solid = this.fluidAmount > 0 ? (float)(this.hopsAmount + this.wheatAmount) / ((float)this.fluidAmount / 1000.0F) : 10.0F;
      this.solidRatio = 0;
      if (solid >= 4.0F) {
         this.beerQuality = 5;
      } else if (solid >= 2.4F) {
         this.solidRatio = 6;
      } else if (solid >= 2.0F) {
         this.solidRatio = 5;
      } else if (solid > 1.0F) {
         this.solidRatio = 4;
      } else if (solid == 1.0F) {
         this.solidRatio = 3;
      } else if (solid > 0.5F) {
         this.solidRatio = 2;
      } else if (solid > 0.4166667F) {
         this.solidRatio = 1;
      }
   }

   public int getSolidRatio() {
      if (this.opened) {
         return this.solidRatio;
      } else {
         float solid = this.fluidAmount > 0 ? (float)(this.hopsAmount + this.wheatAmount) / ((float)this.fluidAmount / 1000.0F) : 10.0F;
         if (solid >= 2.4F) {
            return 6;
         } else if (solid >= 2.0F) {
            return 5;
         } else if (solid > 1.0F) {
            return 4;
         } else if (solid == 1.0F) {
            return 3;
         } else if (solid > 0.5F) {
            return 2;
         } else {
            return solid > 0.4166667F ? 1 : 0;
         }
      }
   }

   public int getAlcoholLevel() {
      if (this.opened) {
         return this.alcoholLevel;
      } else {
         float hopsToWheat = this.wheatAmount > 0 ? (float)this.hopsAmount / (float)this.wheatAmount : (this.hopsAmount > 0 ? 10.0F : 0.0F);
         if (hopsToWheat >= 4.0F) {
            return 6;
         } else if (hopsToWheat >= 3.0F) {
            return 5;
         } else if (hopsToWheat >= 2.0F) {
            return 4;
         } else if (hopsToWheat > 0.5F) {
            return 3;
         } else if (hopsToWheat > 0.33333334F) {
            return 2;
         } else {
            return hopsToWheat > 0.25F ? 1 : 0;
         }
      }
   }

   public IFluidHandler getBeerStoredFluid() {
      FluidTank tank = new FluidTank(this.getFluidCapacity());
      tank.setFluid(new FluidStack(Fluids.WATER, this.fluidAmount));
      return tank;
   }

   public Component getBrewedPotion() {
      return (Component)(this.potionType == null ? this.translate("tooltip.block.ic2.barrel.unknown") : this.potionType.getDisplayName());
   }

   public boolean hasTreeTap() {
      return this.hasTreeTap;
   }

   public void setHasTreeTap(boolean hasTreeTap) {
      if (this.hasTreeTap != hasTreeTap) {
         this.hasTreeTap = hasTreeTap;
         this.onStateChanged();
      }
   }

   public boolean isEmpty() {
      return this.brewType == 0 || this.fluidAmount <= 0;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.hasTreeTap = compound.getBoolean("tab");
      this.opened = compound.getBoolean("opened");
      this.fluidAmount = compound.getInt("fluid");
      this.brewType = compound.getInt("type");
      this.age = compound.getInt("age");
      if (this.brewType == 1 || this.brewType == 10) {
         this.beerQuality = compound.getInt("quality");
         this.wheatAmount = compound.getInt("wheat");
         this.hopsAmount = compound.getInt("hops");
         if (this.opened) {
            this.alcoholLevel = compound.getInt("level");
            this.solidRatio = compound.getInt("solid");
         }

         if (this.brewType == 10) {
            String id = compound.getString("potion");
            this.potionType = !id.isEmpty() && !id.equals("empty") ? (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id)) : null;
         }
      } else if (this.brewType == 5) {
         this.beerQuality = compound.getInt("quality");
         this.hopsAmount = compound.getInt("hops");
         if (this.opened) {
            this.alcoholLevel = compound.getInt("level");
         }
      }
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "tab", this.hasTreeTap, false);
      NBTUtils.putBoolean(compound, "opened", this.opened, false);
      compound.putInt("fluid", this.fluidAmount);
      NBTUtils.putByte(compound, "type", this.brewType, 0);
      compound.putInt("age", this.age);
      if (this.brewType == 1 || this.brewType == 10) {
         NBTUtils.putByte(compound, "quality", this.beerQuality, 0);
         NBTUtils.putByte(compound, "wheat", this.wheatAmount, 0);
         NBTUtils.putByte(compound, "hops", this.hopsAmount, 0);
         if (this.opened) {
            NBTUtils.putByte(compound, "level", this.alcoholLevel, 0);
            NBTUtils.putByte(compound, "solid", this.solidRatio, 0);
         }

         if (this.brewType == 10) {
            compound.putString("potion", this.potionType == null ? "empty" : ForgeRegistryWrapper.getMobEffectKey(this.potionType).toString());
         }
      } else if (this.brewType == 5) {
         NBTUtils.putByte(compound, "quality", this.beerQuality, 0);
         NBTUtils.putByte(compound, "hops", this.hopsAmount, 0);
         if (this.opened) {
            NBTUtils.putByte(compound, "level", this.alcoholLevel, 0);
         }
      }
   }
}
