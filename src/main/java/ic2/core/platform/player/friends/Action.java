package ic2.core.platform.player.friends;

import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Action {
   static final SortedMap<String, Action> ACTION_MAP = Object2ObjectSortedMaps.synchronize(CollectionUtils.createLinkedMap());
   public static final Action PERSONAL_BLOCKS_ACCESS = createAction(
      "personal_blocks_access", () -> new ItemStack(IC2Blocks.PERSONAL_CHEST), () -> new ItemStack(Items.ENDER_EYE)
   );
   public static final Action PERSONAL_BLOCKS_EDIT = createAction(
      "personal_blocks_edit", () -> new ItemStack(IC2Blocks.PERSONAL_CHEST), () -> new ItemStack(IC2Items.WRENCH)
   );
   public static final Action PERSONAL_BLOCKS_ADMIN = createAction(
      "personal_blocks_admin", () -> new ItemStack(IC2Blocks.PERSONAL_CHEST), () -> new ItemStack(Items.GOLDEN_SWORD)
   );
   public static final Action TRADE_SETTINGS = createAction(
      "trade_settings", () -> new ItemStack(IC2Blocks.TRADE_O_MAT), () -> new ItemStack(Items.GOLDEN_SWORD)
   );
   public static final Action TELEPORT_NETWORK = createAction(
      "teleport_network", () -> new ItemStack(IC2Blocks.TELEPORT_TUBE), () -> new ItemStack(Items.HOPPER)
   );
   public static final Action TELEPORT_SETTINGS = createAction(
      "teleport_settings", () -> new ItemStack(IC2Blocks.TELEPORT_TUBE), () -> new ItemStack(Items.GOLDEN_SWORD)
   );
   public static final Action PLAYER_DETECTOR_SETTINGS = createAction(
      "player_detector_settings", () -> new ItemStack(IC2Blocks.PLAYER_DETECTOR), () -> new ItemStack(IC2Items.WRENCH)
   );
   public static final Action PLAYER_DETECTOR_DETECTION = createAction(
      "player_detector_detection", () -> new ItemStack(IC2Blocks.PLAYER_DETECTOR), () -> new ItemStack(Items.ENDER_EYE)
   );
   public static final Action BREAK_IRIDIUM = createAction(
      "player_break_iridium", () -> new ItemStack(IC2Blocks.IRIDIUM_STONE), () -> new ItemStack(Items.DIAMOND_PICKAXE)
   );
   public static final Action CHUNKLOADER_ACCESS = createAction(
      "chunkloader_access", () -> new ItemStack(IC2Blocks.CHUNKLOADER), () -> new ItemStack(Items.ENDER_EYE)
   );
   public static final Action CHUNKLOADER_EDIT = createAction(
      "chunkloader_edit", () -> new ItemStack(IC2Blocks.CHUNKLOADER), () -> new ItemStack(IC2Items.WRENCH)
   );
   String name;
   Component local;
   Supplier<ItemStack> mainIcon;
   Supplier<ItemStack> subIcon;

   private Action(String name, Supplier<ItemStack> mainIcon, Supplier<ItemStack> subIcon) {
      this.name = name;
      this.local = Component.translatable("gui.ic2.friends." + name);
      this.mainIcon = mainIcon;
      this.subIcon = subIcon;
   }

   public static Action createAction(String name, Supplier<ItemStack> main, Supplier<ItemStack> sub) {
      return ACTION_MAP.computeIfAbsent(name, n -> new Action(n, main, sub));
   }

   public static Action getAction(String name) {
      return ACTION_MAP.get(name);
   }

   public static List<Action> getAllActions() {
      return new ObjectArrayList(ACTION_MAP.values());
   }

   public String getName() {
      return this.name;
   }

   public Component getLocal() {
      return this.local;
   }

   public ItemStack getMainIcon() {
      return this.mainIcon == null ? ItemStack.EMPTY : this.getOrDefault(this.mainIcon.get());
   }

   public ItemStack getSubIcon() {
      return this.subIcon == null ? ItemStack.EMPTY : this.getOrDefault(this.subIcon.get());
   }

   private ItemStack getOrDefault(ItemStack stack) {
      return stack == null ? ItemStack.EMPTY : stack;
   }
}
