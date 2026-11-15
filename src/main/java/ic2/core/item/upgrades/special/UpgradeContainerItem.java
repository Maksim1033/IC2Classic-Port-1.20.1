package ic2.core.item.upgrades.special;

import ic2.api.items.IUpgradeItem;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.inv.inventory.UpgradeContainerInventory;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UpgradeContainerItem extends IC2Item implements IHasHeldSlotInventory, IUpgradeItem, ISimpleItemModel {
   public UpgradeContainerItem() {
      super("upgrade_container", new PropertiesBuilder().maxStackSize(1));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("upgrades/machines").get("container");
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new UpgradeContainerInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new UpgradeContainerInventory(player, this, stack, slot).load(stack);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      IntSet valid = new IntOpenHashSet(StackUtil.getNbtData(stack).getIntArray("valid"));
      List<ItemStack> items = this.getItems(stack);
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         ItemStack entry = items.get(i);
         if (!entry.isEmpty()) {
            helper.addSimpleToolTip("tooltip.item.ic2.upgrade_container." + (valid.contains(i) ? "valid" : "invalid"), entry.getCount(), entry.getHoverName());
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
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.CUSTOM_MOD;
   }

   @Override
   public EnumSet<IUpgradeItem.Functions> getFunctions(ItemStack stack) {
      EnumSet<IUpgradeItem.Functions> functions = EnumSet.noneOf(IUpgradeItem.Functions.class);
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (stack.getItem() instanceof IUpgradeItem) {
            functions.addAll(((IUpgradeItem)entry.getItem()).getFunctions(entry));
         }
      }

      return functions;
   }

   @Override
   public void onInstall(ItemStack stack, IMachine machine) {
      IntList upgrades = new IntArrayList();
      NonNullList<ItemStack> items = this.getItems(stack);
      EnumSet<IUpgradeItem.UpgradeType> types = machine.getSupportedUpgradeTypes();
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         ItemStack entry = (ItemStack)items.get(i);
         Item var10 = entry.getItem();
         if (var10 instanceof IUpgradeItem) {
            IUpgradeItem item = (IUpgradeItem)var10;
            if (types.contains(item.getType(entry))) {
               item.onInstall(entry, machine);
               upgrades.add(i);
            }
         }
      }

      NBTUtils.saveItems(stack.getOrCreateTag(), items);
      this.setValidItems(stack, upgrades.toIntArray());
   }

   @Override
   public double getProcessingSpeedMultiplier(ItemStack stack, IMachine machine) {
      double multiplier = 1.0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            multiplier *= Math.pow(((IUpgradeItem)entry.getItem()).getProcessingSpeedMultiplier(entry, machine), (double)entry.getCount());
         }
      }

      return multiplier;
   }

   @Override
   public int getExtraProcessingSpeed(ItemStack stack, IMachine machine) {
      int extra = 0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            extra += ((IUpgradeItem)entry.getItem()).getExtraProcessingSpeed(entry, machine) * entry.getCount();
         }
      }

      return extra;
   }

   @Override
   public double getProcessingTimeMultiplier(ItemStack stack, IMachine machine) {
      double multiplier = 1.0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            multiplier *= Math.pow(((IUpgradeItem)entry.getItem()).getProcessingTimeMultiplier(entry, machine), (double)entry.getCount());
         }
      }

      return multiplier;
   }

   @Override
   public int getExtraProcessingTime(ItemStack stack, IMachine machine) {
      int extra = 0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            extra += ((IUpgradeItem)entry.getItem()).getExtraProcessingTime(entry, machine) * entry.getCount();
         }
      }

      return extra;
   }

   @Override
   public double getEnergyDemandMultiplier(ItemStack stack, IMachine machine) {
      double multiplier = 1.0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            multiplier *= Math.pow(((IUpgradeItem)entry.getItem()).getEnergyDemandMultiplier(entry, machine), (double)entry.getCount());
         }
      }

      return multiplier;
   }

   @Override
   public int getExtraEnergyDemand(ItemStack stack, IMachine machine) {
      int extra = 0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            extra += ((IUpgradeItem)entry.getItem()).getExtraEnergyDemand(entry, machine) * entry.getCount();
         }
      }

      return extra;
   }

   @Override
   public double getEnergyStorageMultiplier(ItemStack stack, IMachine machine) {
      double multiplier = 1.0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            multiplier *= Math.pow(((IUpgradeItem)entry.getItem()).getEnergyStorageMultiplier(entry, machine), (double)entry.getCount());
         }
      }

      return multiplier;
   }

   @Override
   public int getExtraEnergyStorage(ItemStack stack, IMachine machine) {
      int extra = 0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            extra += ((IUpgradeItem)entry.getItem()).getExtraEnergyStorage(entry, machine) * entry.getCount();
         }
      }

      return extra;
   }

   @Override
   public int getExtraTier(ItemStack stack, IMachine machine) {
      int extra = 0;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            extra += ((IUpgradeItem)entry.getItem()).getExtraTier(entry, machine) * entry.getCount();
         }
      }

      return extra;
   }

   @Override
   public float getSoundMultiplier(ItemStack stack, IMachine machine) {
      float multiplier = 1.0F;
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem) {
            multiplier *= (float)Math.pow((double)((IUpgradeItem)entry.getItem()).getSoundMultiplier(entry, machine), (double)entry.getCount());
         }
      }

      return multiplier;
   }

   @Override
   public boolean useRedstoneInvertion(ItemStack stack, IMachine machine) {
      List<ItemStack> list = this.getValidItems(stack);
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack entry = list.get(i);
         if (entry.getItem() instanceof IUpgradeItem && ((IUpgradeItem)entry.getItem()).useRedstoneInvertion(entry, machine)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      UpgradeContainerItem.ValidContainer container = this.getValidItemContainer(stack);
      if (container != null) {
         for (ItemStack entry : container) {
            if (entry.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)entry.getItem()).onTick(entry, machine);
            }
         }

         container.close();
      }
   }

   @Override
   public void onMachineFinishedRecipePre(ItemStack stack, IMachine machine, IRecipeOutput output, CompoundTag recipeFlags) {
      UpgradeContainerItem.ValidContainer container = this.getValidItemContainer(stack);
      if (container != null) {
         for (ItemStack entry : container) {
            if (entry.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)entry.getItem()).onMachineFinishedRecipePre(entry, machine, output, recipeFlags);
            }
         }

         container.close();
      }
   }

   @Override
   public void onMachineFinishedRecipePost(ItemStack stack, IMachine machine, IMachineRecipeList.RecipeEntry recipe, List<IStackOutput> drops) {
      UpgradeContainerItem.ValidContainer container = this.getValidItemContainer(stack);
      if (container != null) {
         for (ItemStack entry : container) {
            if (entry.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)entry.getItem()).onMachineFinishedRecipePost(entry, machine, recipe, drops);
            }
         }

         container.close();
      }
   }

   @Override
   public void onMachineProcessed(ItemStack stack, IMachine machine) {
      UpgradeContainerItem.ValidContainer container = this.getValidItemContainer(stack);
      if (container != null) {
         for (ItemStack entry : container) {
            if (entry.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)entry.getItem()).onMachineProcessed(entry, machine);
            }
         }

         container.close();
      }
   }

   public void setValidItems(ItemStack stack, int[] slots) {
      if (slots.length == 0) {
         stack.getOrCreateTag().remove("valid");
      } else {
         stack.getOrCreateTag().putIntArray("valid", slots);
      }
   }

   public List<ItemStack> getValidItems(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      int[] validSlots = nbt.getIntArray("valid");
      if (validSlots.length == 0) {
         return ObjectLists.emptyList();
      } else {
         NonNullList<ItemStack> inv = NonNullList.withSize(3, ItemStack.EMPTY);
         NBTUtils.loadItems(nbt, inv);
         NonNullList<ItemStack> result = NonNullList.create();

         for (int validSlot : validSlots) {
            ItemStack entry = (ItemStack)inv.get(validSlot);
            if (!entry.isEmpty()) {
               result.add(entry);
            }
         }

         return result;
      }
   }

   protected UpgradeContainerItem.ValidContainer getValidItemContainer(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      int[] validSlots = nbt.getIntArray("valid");
      return validSlots.length == 0 ? null : new UpgradeContainerItem.ValidContainer(validSlots, nbt);
   }

   public NonNullList<ItemStack> getItems(ItemStack stack) {
      return NBTUtils.getItems(stack);
   }

   protected static class ValidContainer implements Iterable<ItemStack> {
      NonNullList<ItemStack> inv = NonNullList.withSize(3, ItemStack.EMPTY);
      List<ItemStack> validObjects = new ObjectArrayList();
      CompoundTag owner;

      protected ValidContainer(int[] valid, CompoundTag owner) {
         this.owner = owner;
         NBTUtils.loadItems(owner, this.inv);

         for (int j : valid) {
            ItemStack entry = (ItemStack)this.inv.get(j);
            if (!entry.isEmpty()) {
               this.validObjects.add(entry);
            }
         }
      }

      @Override
      public Iterator<ItemStack> iterator() {
         return this.validObjects.iterator();
      }

      public void close() {
         NBTUtils.saveItems(this.owner, this.inv);
      }
   }
}
