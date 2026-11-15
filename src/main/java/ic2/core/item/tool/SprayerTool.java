package ic2.core.item.tool;

import ic2.api.events.FoamEvent;
import ic2.api.items.IRepairable;
import ic2.api.items.armor.IFoamSupplier;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class SprayerTool extends IC2Item implements ISimpleItemModel, IToolModel, IRepairable {
   public SprayerTool() {
      this("foam_sprayer", null);
   }

   public SprayerTool(String itemName, @Nullable PropertiesBuilder properties) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).maxDamage(1600).group(IC2.IC2_MAIN_GROUP).setNoRepair());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/cf_sprayer").get("manual");
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         items.add(new ItemStack(this));
         ItemStack stack = new ItemStack(this);
         stack.setDamageValue(1600);
         items.add(stack);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (!IC2.PLATFORM.isRendering() && context.getPlayer() != null) {
         int sprayCount = this.getSprayLimit(stack);
         if (sprayCount <= 0) {
            return InteractionResult.FAIL;
         } else {
            BlockPlaceContext data = new BlockPlaceContext(context);
            Level world = data.getLevel();
            BlockPos pos = data.getClickedPos();
            BlockState state = world.getBlockState(pos);
            FoamEvent.Check check = this.postEvent(new FoamEvent.Check(world, pos));
            if (check.isCanceled()) {
               return InteractionResult.FAIL;
            } else if ((state.canBeReplaced(data) || !check.isCanceled()) && (check.isCustomPlacement() || IC2Blocks.CFOAM_WET.canSurvive(state, world, pos))) {
               ItemStack pack = getFoamProvider(context.getPlayer(), sprayCount);
               if (pack.isEmpty() && this.getStoredFoam(stack) < sprayCount) {
                  return InteractionResult.FAIL;
               } else {
                  FoamEvent.TargetType result = check.getType();
                  DirectionList validDirections = this.calculateDirectionsFromPlayer(context.getPlayer());
                  PriorityQueue<BlockPos> queue = new ObjectArrayFIFOQueue();
                  Set<BlockPos> toPlace = new ObjectLinkedOpenHashSet();
                  toPlace.add(pos);
                  this.addNextPositions(pos, queue, toPlace, check.isCustomPlacement() ? DirectionList.ALL : validDirections);
                  sprayCount--;

                  while (!queue.isEmpty() && sprayCount > 0) {
                     BlockPos next = (BlockPos)queue.dequeue();
                     check = this.postEvent(new FoamEvent.Check(world, next));
                     if (!check.isCanceled()
                        && (result == FoamEvent.TargetType.ANY || result == check.getType())
                        && (check.isCustomPlacement() || IC2Blocks.CFOAM_WET.canSurvive(world.getBlockState(next), world, next))
                        && toPlace.add(next)) {
                        this.addNextPositions(next, queue, toPlace, check.isCustomPlacement() ? DirectionList.ALL : validDirections);
                        sprayCount--;
                     }
                  }

                  for (BlockPos placePos : toPlace) {
                     FoamEvent.Place place = this.postEvent(new FoamEvent.Place(world, placePos));
                     if (!place.isCanceled() || place.shouldPlaceFoam()) {
                        world.setBlockAndUpdate(placePos, IC2Blocks.CFOAM_WET.defaultBlockState());
                     }
                  }

                  if (!toPlace.isEmpty()) {
                     if (!pack.isEmpty()) {
                        ((IFoamSupplier)pack.getItem()).useFoam(context.getPlayer(), pack, toPlace.size());
                     } else {
                        this.useFoam(stack, context.getPlayer(), toPlace.size());
                     }

                     this.onFoamUsed(stack, context.getPlayer(), toPlace.size());
                     return InteractionResult.SUCCESS;
                  } else {
                     return InteractionResult.FAIL;
                  }
               }
            } else {
               return InteractionResult.FAIL;
            }
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public void addNextPositions(BlockPos pos, PriorityQueue<BlockPos> queue, Set<BlockPos> alreadyVisited, DirectionList dirs) {
      for (Direction dir : dirs.getRandomIterator()) {
         BlockPos offset = pos.relative(dir);
         if (!alreadyVisited.contains(offset)) {
            queue.enqueue(offset);
         }
      }
   }

   public DirectionList calculateDirectionsFromPlayer(Player player) {
      DirectionList dirs = DirectionList.ALL;
      float yaw = player.getYRot() % 360.0F;
      float pitch = player.getXRot() % 360.0F;
      if (pitch >= -65.0F && pitch <= 65.0F) {
         if (yaw >= 300.0F && yaw <= 360.0F || yaw >= 0.0F && yaw <= 60.0F) {
            dirs = dirs.remove(Direction.NORTH);
         }

         if (yaw >= 30.0F && yaw <= 150.0F) {
            dirs = dirs.remove(Direction.EAST);
         }

         if (yaw >= 120.0F && yaw <= 240.0F) {
            dirs = dirs.remove(Direction.SOUTH);
         }

         if (yaw >= 210.0F && yaw <= 330.0F) {
            dirs = dirs.remove(Direction.WEST);
         }
      }

      if (pitch <= -40.0F) {
         dirs = dirs.remove(Direction.DOWN);
      }

      if (pitch >= 40.0F) {
         dirs = dirs.remove(Direction.UP);
      }

      return dirs;
   }

   private <T extends Event> T postEvent(T event) {
      MinecraftForge.EVENT_BUS.post(event);
      return event;
   }

   public int getSprayLimit(ItemStack stack) {
      return 13;
   }

   public int getStoredFoam(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   @Override
   public boolean repairDamage(ItemStack stack, int amount) {
      if (stack.getDamageValue() > 0) {
         stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
         return true;
      } else {
         return false;
      }
   }

   public void useFoam(ItemStack stack, Player player, int amount) {
      if (!player.isCreative()) {
         stack.setDamageValue(stack.getDamageValue() + amount);
      }
   }

   public void onFoamUsed(ItemStack stack, Player player, int amount) {
      player.awardStat(IC2Stats.FOAM_SPRAYED, amount);
   }

   public static ItemStack getFoamProvider(Player player, int amount) {
      Inventory inv = player.getInventory();
      int i = 0;

      for (int m = inv.armor.size(); i < m; i++) {
         ItemStack stack = (ItemStack)inv.armor.get(i);
         if (stack.getItem() instanceof IFoamSupplier
            && ((IFoamSupplier)stack.getItem()).canProvideFoam(player, stack, IFoamSupplier.InventoryType.ARMOR, amount)) {
            return stack;
         }
      }

      if (IC2.CURIO_PLUGIN != null) {
         ItemStack stack = IC2.CURIO_PLUGIN.getFoamSupplier(player, amount);
         if (!stack.isEmpty()) {
            return stack;
         }
      }

      i = 0;

      for (int mx = inv.items.size(); i < mx; i++) {
         ItemStack stack = (ItemStack)inv.items.get(i);
         if (stack.getItem() instanceof IFoamSupplier
            && ((IFoamSupplier)stack.getItem()).canProvideFoam(player, stack, IFoamSupplier.InventoryType.HOTBAR, amount)) {
            return stack;
         }
      }

      i = 0;

      for (int mxx = inv.offhand.size(); i < mxx; i++) {
         ItemStack stack = (ItemStack)inv.offhand.get(i);
         if (stack.getItem() instanceof IFoamSupplier
            && ((IFoamSupplier)stack.getItem()).canProvideFoam(player, stack, IFoamSupplier.InventoryType.OFFHAND, amount)) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }
}
