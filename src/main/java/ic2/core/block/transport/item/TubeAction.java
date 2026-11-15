package ic2.core.block.transport.item;

import ic2.core.item.tool.WikiItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class TubeAction {
   private static final Map<String, TubeAction> MAPPED_ACTIONS = CollectionUtils.<String, TubeAction>createMap();
   private static final List<TubeAction> LISTED_ACTIONS = CollectionUtils.<TubeAction>createList();
   public static final TubeAction BLOCK_OUTPUT = new TubeAction("block_output", List.of(IC2Blocks.DIRECTIONAL_TUBE));
   public static final TubeAction EXTRA_EXTRACTION_SIDE = new TubeAction(
      "extra_extraction", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE)
   );
   public static final TubeAction REDSTONE_CONTROL = new TubeAction(
      "redstone_control", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE, IC2Blocks.REQUEST_TUBE)
   );
   public static final TubeAction OUTPUT_PRIORITY = new TubeAction(
      "out_priority", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE, IC2Blocks.PROVIDER_TUBE, IC2Blocks.STICKY_TUBE)
   );
   public static final TubeAction ONLY_EXISTING = new TubeAction("only_existing", List.of(IC2Blocks.INSERTION_TUBE));
   public static final TubeAction SPECIAL_EXTRACTION_CONNECTIVITY = new TubeAction(
      "special_connection", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE, IC2Blocks.STICKY_TUBE)
   );
   public static final TubeAction PULSE = new TubeAction("pulse", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE));
   public static final TubeAction COMPARATOR = new TubeAction("comparator", List.of(IC2Blocks.EXTRACTOR_TUBE, IC2Blocks.FILTERED_EXTRACTION_TUBE));
   public static final TubeAction LARGE_PICKUP_RADIUS = new TubeAction("pickup_radius", List.of(IC2Blocks.PICKUP_TUBE));
   public static final TubeAction VISIBLE_ITEMS = new TubeAction("visible_items", Collections.emptyList());
   private static final String TOOLTIP_PATH = "tooltip.item.ic2.tube_tool.";
   private static final MutableComponent MODEL_LIST = buildList(
         Component.translatable("tooltip.item.ic2.tube_tool.mode_list.header"), getAll().stream().map(TubeAction::getName).toList(), false
      )
      .withStyle(ChatFormatting.GRAY);
   String id;
   List<MutableComponent> applicableItems;
   MutableComponent name;
   MutableComponent desc;
   MutableComponent applicable;

   public TubeAction(String id, List<Block> applicableTubes) {
      this.id = id;
      this.name = Component.translatable("tooltip.item.ic2.tube_tool." + id);
      this.desc = Component.translatable("tooltip.item.ic2.tube_tool." + id + ".desc").withStyle(ChatFormatting.DARK_GRAY);
      this.applicableItems = applicableTubes.stream().map(this::createBlock).collect(Collectors.toList());
      if (this.applicableItems.isEmpty()) {
         this.applicableItems.add(Component.translatable("tooltip.item.ic2.tube_tool.tube_list.all"));
      }

      this.applicable = Component.literal("\n")
         .append(buildList(Component.translatable("tooltip.item.ic2.tube_tool.tube_list.header"), this.applicableItems, false))
         .withStyle(ChatFormatting.GRAY);
      MAPPED_ACTIONS.put(id, this);
      LISTED_ACTIONS.add(this);
   }

   public String getId() {
      return this.id;
   }

   public MutableComponent getName() {
      return this.name.copy();
   }

   public MutableComponent getDesc() {
      return this.desc.copy();
   }

   public MutableComponent getApplicable() {
      return this.applicable.copy();
   }

   public MutableComponent getWikiApplicable() {
      return Component.literal("\n")
         .append(buildList(Component.translatable("tooltip.item.ic2.tube_tool.tube_list.header"), this.applicableItems, true))
         .withStyle(ChatFormatting.BLACK);
   }

   private MutableComponent createBlock(Block block) {
      MutableComponent text = block.getName();
      Style style = Style.EMPTY
         .withClickEvent(new ClickEvent(Action.CHANGE_PAGE, "redirect;" + WikiItem.getPreviewLocation(ForgeRegistries.ITEMS.getKey(block.asItem()))));
      return text.withStyle(
         style.withHoverEvent(
            new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("tooltip.ic2.open").append(text.copy()))
         )
      );
   }

   private static MutableComponent buildList(MutableComponent headline, List<MutableComponent> items, boolean bold) {
      if (items.isEmpty()) {
         return Component.empty();
      } else {
         MutableComponent result = Component.empty().withStyle(bold ? ChatFormatting.BOLD : ChatFormatting.RESET);
         result.append(headline.plainCopy().withStyle(ChatFormatting.BOLD));
         items.forEach(i -> result.append("\n - ").append(i.copy()));
         return result;
      }
   }

   public static MutableComponent getModeList() {
      return MODEL_LIST;
   }

   public static TubeAction byId(String id) {
      return MAPPED_ACTIONS.get(id);
   }

   public static List<TubeAction> getAll() {
      return ObjectArrayList.wrap(LISTED_ACTIONS.toArray(TubeAction[]::new));
   }

   public static TubeAction byIdOrDefault(String id) {
      return id != null && !id.trim().isEmpty() ? MAPPED_ACTIONS.get(id) : LISTED_ACTIONS.get(0);
   }

   public static TubeAction getNextAction(String id) {
      if (id != null && !id.trim().isEmpty()) {
         TubeAction action = MAPPED_ACTIONS.get(id);
         return LISTED_ACTIONS.get((LISTED_ACTIONS.indexOf(action) + 1) % LISTED_ACTIONS.size());
      } else {
         return LISTED_ACTIONS.get(0);
      }
   }
}
