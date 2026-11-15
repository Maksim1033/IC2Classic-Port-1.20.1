package ic2.core.inventory.handler;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;

public final class SlotType {
   private static final Map<String, SlotType> SLOT_REGISTRY = Object2ObjectMaps.synchronize(CollectionUtils.createMap());
   public static final SlotType ROOT = getOrCreate("root", Component.translatable("misc.ic2.slots.root"), null);
   public static final SlotType IO = getOrCreate("io", Component.translatable("misc.ic2.slots.io"), ROOT);
   public static final SlotType UPGRADES = getOrCreate("upgrades", Component.translatable("misc.ic2.slots.upgrades"), ROOT);
   public static final SlotType INPUTS = getOrCreate("inputs", Component.translatable("misc.ic2.slots.inputs"), IO);
   public static final SlotType OUTPUTS = getOrCreate("outputs", Component.translatable("misc.ic2.slots.outputs"), IO);
   public static final SlotType FUELS = getOrCreate("energy", Component.translatable("misc.ic2.slots.energy"), IO);
   public static final SlotType INPUT = getOrCreate("input", Component.translatable("misc.ic2.slots.input"), INPUTS);
   public static final SlotType EXTRA_INPUT = getOrCreate("extra_input", Component.translatable("misc.ic2.slots.extra_input"), INPUTS);
   public static final SlotType CONTAINER = getOrCreate("container", Component.translatable("misc.ic2.slots.container"), INPUTS);
   public static final SlotType MINING_PIPES = getOrCreate("mining_pipes", Component.translatable("misc.ic2.slots.mining_pipes"), INPUTS);
   public static final SlotType TOOLS = getOrCreate("tools", Component.translatable("misc.ic2.slots.tools"), INPUTS);
   public static final SlotType OUTPUT = getOrCreate("output", Component.translatable("misc.ic2.slots.output"), OUTPUTS);
   public static final SlotType EXTRA_OUTPUT = getOrCreate("extra_output", Component.translatable("misc.ic2.slots.extra_output"), OUTPUTS);
   public static final SlotType FUEL = getOrCreate("fuel", Component.translatable("misc.ic2.slots.fuel"), FUELS);
   public static final SlotType BATTERY = getOrCreate("battery", Component.translatable("misc.ic2.slots.battery"), FUELS);
   public static final SlotType CHARGE = getOrCreate("charge", Component.translatable("misc.ic2.slots.charge"), FUELS);
   public static final SlotType FERTILIZER = getOrCreate("fertilizer", Component.translatable("misc.ic2.slots.fertilizer"), INPUTS);
   public static final SlotType HYDRATION = getOrCreate("hydration", Component.translatable("misc.ic2.slots.hydration"), INPUTS);
   public static final SlotType WEED_EX = getOrCreate("weed_ex", Component.translatable("misc.ic2.slots.weed-ex"), INPUTS);
   public static final SlotType SCANNER = getOrCreate("scanner", Component.translatable("misc.ic2.slots.scanner"), IO);
   public static final SlotType RECYCLER = getOrCreate("recycler", Component.translatable("misc.ic2.slots.recycler"), IO);
   public static final SlotType FILLER = getOrCreate("filler", Component.translatable("misc.ic2.slots.filler"), IO);
   public static final SlotType REFUEL_REMOTE = getOrCreate("refuel_remote", Component.translatable("misc.ic2.slots.refuel_remote"), IO);
   public static final SlotType CHUNKLOADER = getOrCreate("chunkloader", Component.translatable("misc.ic2.slots.chunkloader"), IO);
   public static final SlotType STORAGE = getOrCreate("storage", Component.translatable("misc.ic2.slots.storage"), IO);
   public static final SlotType REACTOR = getOrCreate("reactor", Component.translatable("misc.ic2.slots.reactor"), IO);
   private final String id;
   private final Component name;
   private final SlotType parent;
   private final Set<SlotType> children = new ObjectOpenHashSet();

   private SlotType(String id, Component name, SlotType parent) {
      this.id = id;
      this.name = name;
      this.parent = parent;
      if (parent != null) {
         parent.children.add(this);
      }
   }

   public static SlotType getOrCreate(String type, Component name, SlotType parent) {
      return SLOT_REGISTRY.computeIfAbsent(type, T -> new SlotType(type, name, parent));
   }

   public String getId() {
      return this.id;
   }

   public Component getName() {
      return this.name;
   }

   public SlotType getParent() {
      return this.parent;
   }

   public boolean isParent(SlotType type) {
      return type == this || type == this.parent || this.parent != null && this.parent.isParent(type);
   }

   public boolean containsChild(SlotType type) {
      return type == this || this.children.contains(type);
   }

   public boolean contains(SlotType type) {
      return this.children.contains(type);
   }
}
