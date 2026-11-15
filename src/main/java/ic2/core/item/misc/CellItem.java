package ic2.core.item.misc;

import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple.Consumable;

public class CellItem extends IC2Item implements ISimpleItemModel {
   private static final Map<Fluid, Item> FILL_MAP = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap());
   String texture;
   Fluid owner;

   public CellItem(String itemName, String texture, Fluid fluid) {
      super(itemName, fluid == Fluids.EMPTY ? null : new PropertiesBuilder().containerItem(IC2Items.CELL_EMPTY));
      this.texture = texture;
      this.owner = fluid;
      if (fluid != null) {
         registerFluidFilling(fluid, this, false);
      }
   }

   public CellItem(String itemName, PropertiesBuilder builder, String texture, Fluid fluid) {
      super(itemName, builder.containerItem(IC2Items.CELL_EMPTY));
      if (fluid == null) {
         throw new IllegalStateException("Fluid isn't allowed to be null");
      } else {
         this.texture = texture;
         this.owner = fluid;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("cells").get(this.texture);
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return new CellItem.FluidCellHandler(stack);
   }

   public static void registerFluidFilling(Fluid filled, Item item, boolean force) {
      if (force) {
         FILL_MAP.put(filled, item);
      } else {
         FILL_MAP.putIfAbsent(filled, item);
      }
   }

   public static void addCauldronInteraction(Item output, SoundEvent event, Predicate<BlockState> cauldronValidator, Map<Item, CauldronInteraction> map) {
      map.put(IC2Items.CELL_EMPTY, createInteraction(output, event, cauldronValidator));
   }

   public static CauldronInteraction createInteraction(Item output, SoundEvent sound, Predicate<BlockState> cauldronValidator) {
      return (state, level, pos, player, hand, input) -> fillCell(state, level, pos, player, hand, input, output, cauldronValidator, sound);
   }

   static InteractionResult fillCell(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      ItemStack input,
      Item output,
      Predicate<BlockState> filter,
      SoundEvent sound
   ) {
      if (!filter.test(state)) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            Item item = input.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(input, player, new ItemStack(output)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 2.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (this.owner != Fluids.EMPTY) {
         return InteractionResultHolder.pass(stack);
      } else {
         BlockHitResult block = getPlayerPOVHitResult(worldIn, playerIn, net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY);
         if (block.getType() == Type.MISS) {
            return InteractionResultHolder.pass(stack);
         } else {
            BlockPos pos = block.getBlockPos();
            if (worldIn.mayInteract(playerIn, pos) && playerIn.mayUseItemAt(pos.relative(block.getDirection()), block.getDirection(), stack)) {
               BlockState state = worldIn.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               ItemStack drop = this.getDrop(fluid.getType());
               if (drop.isEmpty() || !(state.getBlock() instanceof BucketPickup bucket)) {
                  return InteractionResultHolder.pass(stack);
               } else if (!playerIn.addItem(drop)) {
                  return InteractionResultHolder.pass(stack);
               } else {
                  bucket.pickupBlock(worldIn, pos, state);
                  stack.shrink(1);
                  playerIn.playSound(fluid.getType() == Fluids.WATER ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_FILL_LAVA, 1.0F, 2.0F);
                  return InteractionResultHolder.success(stack);
               }
            } else {
               return InteractionResultHolder.pass(stack);
            }
         }
      }
   }

   protected ItemStack getDrop(Fluid fluid) {
      Item item = FILL_MAP.get(fluid);
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   public static class FluidCellHandler extends Consumable {
      public FluidCellHandler(ItemStack itemstack) {
         super(itemstack, 1000);
      }

      public FluidStack getFluid() {
         return this.container.getItem() instanceof CellItem result ? new FluidStack(result.owner, 1000) : FluidStack.EMPTY;
      }

      public void setFluid(FluidStack resource) {
         Fluid fluid = resource.getFluid();
         Item item = CellItem.FILL_MAP.get(fluid);
         if (fluid != Fluids.EMPTY && item != null) {
            this.container = new ItemStack(item);
         } else {
            this.container = new ItemStack(IC2Items.CELL_EMPTY);
         }
      }

      public boolean canFillFluidType(FluidStack fluid) {
         return fluid != null && CellItem.FILL_MAP.containsKey(fluid.getFluid()) && fluid.getAmount() >= this.capacity;
      }

      public FluidStack drain(int maxDrain, FluidAction action) {
         return maxDrain < this.capacity ? FluidStack.EMPTY : super.drain(maxDrain, action);
      }

      protected void setContainerToEmpty() {
         this.setFluid(FluidStack.EMPTY);
      }
   }
}
