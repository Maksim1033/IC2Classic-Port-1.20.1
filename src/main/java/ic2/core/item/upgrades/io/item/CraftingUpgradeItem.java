package ic2.core.item.upgrades.io.item;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.network.item.INetworkItemEvent;
import ic2.api.tiles.IMachine;
import ic2.core.IC2;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.inv.IC2CraftingInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.inv.container.CraftingUpgradeContainer;
import ic2.core.item.inv.inventory.CraftingUpgradeInventory;
import ic2.core.item.upgrades.base.BaseDirectionalUpgrade;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayerFactory;

public class CraftingUpgradeItem extends BaseDirectionalUpgrade implements IHasHeldSlotInventory, INetworkItemEvent, INetworkItemBufferEvent<NBTBuffer> {
   static final CraftingContainer CRAFTING = new IC2CraftingInventory(3, 3);
   public static final String VALID = "valid";
   public static final String LAST_CRAFT = "last_craft";
   public static final String DELAY = "delay";
   public static final String QUEUE = "queue";
   public static final String ERROR = "error";
   public static final String TYPE_COUNT = "typeCount";
   public static final String FLAG = "flags";
   public static final String RECIPE_ID = "recipeId";

   public CraftingUpgradeItem() {
      super("crafting");
      this.functions.remove(IUpgradeItem.Functions.RECIPE);
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.TRANSPORT_MOD;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      CompoundTag nbt = StackUtil.getNbtData(stack);
      int data = nbt.getInt("error");
      if (data > 0) {
         helper.addSimpleToolTip("gui.ic2.crafting_upgrade.error." + data);
      }

      helper.addSimpleToolTip("gui.ic2.crafting_upgrade.delay", nbt.getInt("delay"));
      ListTag list = nbt.getList("queue", 10);
      if (!list.isEmpty()) {
         helper.addSimpleToolTip("gui.ic2.crafting_upgrade.items_stuck");
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            ItemStack cached = ItemStack.of(list.getCompound(i));
            if (!cached.isEmpty()) {
               helper.addSimpleToolTip(this.string(cached.getCount() + "x ").append(cached.getDisplayName()));
            }
         }
      }
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new CraftingUpgradeInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new CraftingUpgradeInventory(player, this, stack, slot).load(stack);
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer()) {
         CraftingUpgradeContainer container = this.getContainer(player, CraftingUpgradeContainer.class);
         if (container != null) {
            CraftingUpgradeInventory inv = container.getHolder();
            switch (key) {
               case 0:
                  inv.validateRecipe(player);
                  break;
               case 1:
                  inv.setFlag(value);
                  break;
               case 2:
                  inv.setFacing(value);
            }
         }
      }
   }

   public void onDataBufferReceived(ItemStack stack, Player player, String id, NBTBuffer buffer, Dist target) {
      if (target.isDedicatedServer() && id.equalsIgnoreCase("jei")) {
         CraftingUpgradeContainer container = this.getContainer(player, CraftingUpgradeContainer.class);
         if (container != null) {
            container.getHolder().injectRecipe(buffer.getNBT().getList("RecipeItems", 10), player);
         }
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (IC2.KEYBOARD.isSideInventoryKeyDown(playerIn)) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (IC2.PLATFORM.isSimulating()) {
            IC2.PLATFORM.launchGui(playerIn, handIn, null, this.getInventory(playerIn, handIn, stack));
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   @Override
   public void onInstall(ItemStack stack, IMachine machine) {
      CompoundTag nbt = stack.getOrCreateTag();
      if (!nbt.getBoolean("valid")) {
         nbt.putInt("delay", -1);
         nbt.putInt("error", 1);
      } else if (this.getFacing(stack) == null) {
         nbt.putInt("delay", -1);
         nbt.putInt("error", 2);
      } else if (!(machine instanceof IInventoryMachine)) {
         nbt.putInt("delay", -1);
         nbt.putInt("error", 3);
      } else {
         IHasInventory inventory = ((IInventoryMachine)machine).getOutputInventory();
         if (inventory == null) {
            nbt.putInt("delay", -1);
            nbt.putInt("error", 3);
         } else if (inventory.getSlotCount() < nbt.getInt("typeCount")) {
            nbt.putInt("delay", -1);
            nbt.putInt("error", 4);
         } else if (stack.getCount() > 1) {
            nbt.putInt("delay", -1);
            nbt.putInt("error", 5);
         } else {
            nbt.putInt("delay", 20);
            nbt.putInt("error", -1);
         }
      }
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      CompoundTag nbt = stack.getOrCreateTag();
      if (stack.getCount() > 1) {
         nbt.putInt("delay", -1);
         nbt.putInt("error", 5);
      } else if (nbt.getBoolean("valid")) {
         int delay = nbt.getInt("delay");
         if (delay >= 0) {
            long last = nbt.getLong("last_craft");
            long world = machine.getWorldObj().getGameTime();
            if (last + (long)delay <= world) {
               nbt.putLong("last_craft", world);
               CraftingRecipe recipe = (CraftingRecipe)IC2.PLATFORM.getRecipes().byKey(new ResourceLocation(nbt.getString("recipeId"))).orElse(null);
               if (recipe == null) {
                  nbt.putBoolean("valid", false);
               } else {
                  ListTag list = nbt.getList("queue", 10);
                  if (!list.isEmpty()) {
                     Direction dir = this.getFacing(stack);
                     if (dir == null) {
                        nbt.putInt("delay", -1);
                        nbt.putByte("error", (byte)2);
                     } else {
                        IItemTransporter transporter = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
                        if (transporter == null) {
                           transporter = TransporterManager.getTransporter(machine.getConnectedTube(dir));
                           if (transporter == null) {
                              changeDelay(nbt, 5);
                              return;
                           }
                        }

                        dir = dir.getOpposite();
                        ListTag newList = new ListTag();
                        int i = 0;

                        for (int m = list.size(); i < m; i++) {
                           ItemStack cached = ItemStack.of(list.getCompound(i));
                           if (!cached.isEmpty()) {
                              cached.shrink(transporter.addItem(cached, dir, false));
                              if (cached.getCount() > 0) {
                                 newList.add(cached.save(new CompoundTag()));
                              }
                           }
                        }

                        if (newList.isEmpty()) {
                           nbt.remove("queue");
                           changeDelay(nbt, -5);
                        } else {
                           nbt.put("queue", newList);
                           changeDelay(nbt, 5);
                        }
                     }
                  } else if (!(machine instanceof IInventoryMachine)) {
                     nbt.putInt("delay", -1);
                     nbt.putByte("error", (byte)3);
                  } else {
                     IHasInventory inv = ((IInventoryMachine)machine).getOutputInventory();
                     if (inv != null && inv.getSlotCount() >= nbt.getInt("typeCount")) {
                        Direction dir = this.getFacing(stack);
                        if (dir == null) {
                           nbt.putInt("delay", -1);
                           nbt.putByte("error", (byte)2);
                        } else {
                           IItemTransporter destination = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
                           if (destination == null) {
                              destination = TransporterManager.getTransporter(machine.getConnectedTube(dir));
                              if (destination == null) {
                                 changeDelay(nbt, 5);
                                 return;
                              }
                           }

                           dir = dir.getOpposite();
                           Object2ObjectMap<ItemStack, IntList> recipeItems = createMap(nbt);
                           if (recipeItems.isEmpty()) {
                              nbt.putInt("delay", -1);
                              nbt.putByte("error", (byte)4);
                           } else {
                              int flags = nbt.getInt("flags");
                              SimpleInventory wrapper = SimpleInventory.createCopy(inv);
                              IItemTransporter source = TransporterManager.getTransporter(wrapper);
                              CRAFTING.clearContent();
                              ObjectIterator output = Object2ObjectMaps.fastIterable(recipeItems).iterator();

                              while (output.hasNext()) {
                                 Entry<ItemStack, IntList> entry = (Entry<ItemStack, IntList>)output.next();
                                 int itemsNeeded = ((ItemStack)entry.getKey()).getCount();
                                 int needed = itemsNeeded * ((IntList)entry.getValue()).size();
                                 ItemStack fetched = source.removeItem(new StackFilter((ItemStack)entry.getKey(), flags | 4), dir, needed, false);
                                 if (fetched.getCount() < needed) {
                                    changeDelay(nbt, 5);
                                    return;
                                 }

                                 IntList slots = (IntList)entry.getValue();
                                 int i = 0;

                                 for (int mx = slots.size(); i < mx; i++) {
                                    int slot = slots.getInt(i);
                                    CRAFTING.setItem(slot, fetched.split(itemsNeeded));
                                 }
                              }

                              ItemStack outputx = recipe.assemble(CRAFTING, machine.getWorldObj().registryAccess()).copy();
                              if (outputx.isEmpty()) {
                                 changeDelay(nbt, 5);
                              } else {
                                 ForgeHooks.setCraftingPlayer(FakePlayerFactory.getMinecraft((ServerLevel)machine.getWorldObj()));
                                 NonNullList<ItemStack> remaining = recipe.getRemainingItems(CRAFTING);
                                 ForgeHooks.setCraftingPlayer(null);
                                 outputx.shrink(destination.addItem(outputx, dir, false));
                                 ListTag queue = nbt.getList("queue", 10);
                                 if (outputx.getCount() > 0) {
                                    queue.add(outputx.save(new CompoundTag()));
                                 }

                                 int i = 0;

                                 for (int mx = remaining.size(); i < mx; i++) {
                                    ItemStack entry = (ItemStack)remaining.get(i);
                                    if (!entry.isEmpty()) {
                                       entry.shrink(destination.addItem(entry, dir, false));
                                       if (entry.getCount() > 0) {
                                          queue.add(entry.save(new CompoundTag()));
                                       }
                                    }
                                 }

                                 if (!queue.isEmpty()) {
                                    nbt.put("queue", queue);
                                 }

                                 wrapper.pasteTo(inv);
                                 changeDelay(nbt, -5);
                              }
                           }
                        }
                     } else {
                        nbt.putInt("delay", -1);
                        nbt.putByte("error", (byte)4);
                     }
                  }
               }
            }
         }
      }
   }

   public static void changeDelay(CompoundTag nbt, int change) {
      nbt.putInt("delay", Mth.clamp(nbt.getInt("delay") + change, 5, 200));
   }

   public static Object2ObjectMap<ItemStack, IntList> createMap(CompoundTag nbt) {
      Object2ObjectMap<ItemStack, IntList> recipes = CollectionUtils.<ItemStack, IntList>createLinkedMap();

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("mappedItems", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(data);
         if (!stack.isEmpty()) {
            recipes.put(stack, IntArrayList.wrap(data.getIntArray("slots")));
         }
      }

      return recipes;
   }
}
