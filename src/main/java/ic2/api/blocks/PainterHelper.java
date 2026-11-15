package ic2.api.blocks;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class PainterHelper {
   public static final PainterHelper INSTANCE = new PainterHelper();
   Map<Block, PainterHelper.IPaintable> paintable = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap());
   public static final BiFunction<BlockState, BlockState, BlockState> DEFAULT_MAPPER = (S, T) -> T;
   public static final BiFunction<BlockState, BlockState, BlockState> DEFAULT_COPY_MAPPER = PainterHelper::copyProperties;
   public static DyeableMap BEDS;
   public static DyeableMap WOOL;
   public static DyeableMap CARPETS;
   public static DyeableMap CONCRETE;
   public static DyeableMap CONCRETE_DUST;
   public static DyeableMap GLASS;
   public static DyeableMap GLASS_PANE;
   public static DyeableMap TERRACOTTA;
   public static DyeableMap GLAZED_TERRACOTTA;
   public static DyeableMap SHULKER;

   public static void init() {
      INSTANCE.registerPaintHelper((BEDS = getBedColors()).getBlocks(), new PainterHelper.BedPainter());
      INSTANCE.registerPaintHelper(getSigns(), new PainterHelper.SignPainter());
      INSTANCE.registerPaintHelper(CARPETS = getCarpetColors(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper(CONCRETE = getConcreteColor(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper(CONCRETE_DUST = getConcreteDustColor(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper(GLASS = getGlassColors(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper(GLASS_PANE = getGlassPaneColors(), DEFAULT_COPY_MAPPER);
      INSTANCE.registerPaintHelper(GLAZED_TERRACOTTA = getGlazedTerracottaColors(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper((SHULKER = getShulkerColors()).getBlocks(), new PainterHelper.ShulkerPainter());
      INSTANCE.registerPaintHelper(TERRACOTTA = getTerracottaColors(), DEFAULT_MAPPER);
      INSTANCE.registerPaintHelper(WOOL = getWoolColors(), DEFAULT_MAPPER);
   }

   public void registerPaintHelper(DyeableMap map, BiFunction<BlockState, BlockState, BlockState> mapper) {
      this.registerPaintHelper(map.getBlocks(), new PainterHelper.Colorable(map, mapper));
   }

   public <T extends Block & PainterHelper.IPaintable> void registerPaintHelper(T entry) {
      this.paintable.put(entry, entry);
   }

   public void registerPaintHelper(Collection<Block> blocks, PainterHelper.IPaintable helper) {
      for (Block block : blocks) {
         this.paintable.put(block, helper);
      }
   }

   public void registerPaintHelper(Block block, PainterHelper.IPaintable helper) {
      this.paintable.put(block, helper);
   }

   public PainterHelper.IPaintable getPaintable(Block block) {
      PainterHelper.IPaintable paint = this.paintable.get(block);
      return paint == null ? (block instanceof PainterHelper.IPaintable iPaintable ? iPaintable : null) : paint;
   }

   public PainterHelper.IPaintable getPaintable(BlockState state) {
      return this.getPaintable(state.getBlock());
   }

   public DyeColor getColor(BlockState state) {
      PainterHelper.IPaintable paint = this.getPaintable(state);
      return paint == null ? null : paint.getColor(state);
   }

   public static BlockState copyProperties(BlockState source, BlockState target) {
      for (Property<?> prop : source.getProperties()) {
         if (target.hasProperty(prop)) {
            target = copyProperty(source, target, prop);
         }
      }

      return target;
   }

   private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<T> prop) {
      return target.setValue(prop, source.getValue(prop));
   }

   private static DyeableMap getWoolColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_WOOL, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_WOOL, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_WOOL, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_WOOL, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_WOOL, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_WOOL, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_WOOL, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_WOOL, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_WOOL, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_WOOL, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_WOOL, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_WOOL, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_WOOL, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_WOOL, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_WOOL, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_WOOL, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getTerracottaColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_TERRACOTTA, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_TERRACOTTA, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_TERRACOTTA, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_TERRACOTTA, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_TERRACOTTA, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_TERRACOTTA, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_TERRACOTTA, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_TERRACOTTA, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_TERRACOTTA, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_TERRACOTTA, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_TERRACOTTA, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_TERRACOTTA, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_TERRACOTTA, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_TERRACOTTA, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_TERRACOTTA, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_TERRACOTTA, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getGlazedTerracottaColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_GLAZED_TERRACOTTA, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_GLAZED_TERRACOTTA, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_GLAZED_TERRACOTTA, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_GLAZED_TERRACOTTA, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_GLAZED_TERRACOTTA, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_GLAZED_TERRACOTTA, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_GLAZED_TERRACOTTA, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_GLAZED_TERRACOTTA, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_GLAZED_TERRACOTTA, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_GLAZED_TERRACOTTA, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_GLAZED_TERRACOTTA, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_GLAZED_TERRACOTTA, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_GLAZED_TERRACOTTA, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getConcreteColor() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_CONCRETE, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_CONCRETE, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_CONCRETE, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_CONCRETE, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_CONCRETE, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_CONCRETE, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_CONCRETE, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_CONCRETE, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_CONCRETE, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_CONCRETE, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_CONCRETE, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_CONCRETE, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_CONCRETE, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_CONCRETE, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_CONCRETE, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_CONCRETE, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getConcreteDustColor() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_CONCRETE_POWDER, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_CONCRETE_POWDER, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_CONCRETE_POWDER, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_CONCRETE_POWDER, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_CONCRETE_POWDER, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_CONCRETE_POWDER, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_CONCRETE_POWDER, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_CONCRETE_POWDER, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_CONCRETE_POWDER, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_CONCRETE_POWDER, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_CONCRETE_POWDER, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_CONCRETE_POWDER, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_CONCRETE_POWDER, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_CONCRETE_POWDER, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_CONCRETE_POWDER, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_CONCRETE_POWDER, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getShulkerColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_SHULKER_BOX, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getBedColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_BED, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_BED, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_BED, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_BED, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_BED, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_BED, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_BED, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_BED, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_BED, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_BED, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_BED, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_BED, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_BED, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_BED, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_BED, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_BED, DyeColor.BLACK);
      return colors;
   }

   public static List<Block> getSigns() {
      List<Block> signs = new ObjectArrayList();
      signs.add(Blocks.ACACIA_SIGN);
      signs.add(Blocks.ACACIA_WALL_SIGN);
      signs.add(Blocks.BIRCH_SIGN);
      signs.add(Blocks.BIRCH_WALL_SIGN);
      signs.add(Blocks.DARK_OAK_SIGN);
      signs.add(Blocks.DARK_OAK_WALL_SIGN);
      signs.add(Blocks.JUNGLE_SIGN);
      signs.add(Blocks.JUNGLE_WALL_SIGN);
      signs.add(Blocks.OAK_SIGN);
      signs.add(Blocks.OAK_WALL_SIGN);
      signs.add(Blocks.SPRUCE_SIGN);
      signs.add(Blocks.SPRUCE_WALL_SIGN);
      signs.add(Blocks.CRIMSON_SIGN);
      signs.add(Blocks.CRIMSON_WALL_SIGN);
      signs.add(Blocks.WARPED_SIGN);
      signs.add(Blocks.WARPED_WALL_SIGN);
      return signs;
   }

   private static DyeableMap getGlassColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_STAINED_GLASS, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_STAINED_GLASS, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_STAINED_GLASS, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_STAINED_GLASS, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_STAINED_GLASS, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_STAINED_GLASS, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_STAINED_GLASS, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_STAINED_GLASS, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_STAINED_GLASS, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_STAINED_GLASS, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_STAINED_GLASS, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_STAINED_GLASS, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_STAINED_GLASS, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_STAINED_GLASS, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_STAINED_GLASS, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_STAINED_GLASS, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getGlassPaneColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_WOOL, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_STAINED_GLASS_PANE, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_STAINED_GLASS_PANE, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_STAINED_GLASS_PANE, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_STAINED_GLASS_PANE, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_STAINED_GLASS_PANE, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_STAINED_GLASS_PANE, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_STAINED_GLASS_PANE, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_STAINED_GLASS_PANE, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_STAINED_GLASS_PANE, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_STAINED_GLASS_PANE, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_STAINED_GLASS_PANE, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_STAINED_GLASS_PANE, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_STAINED_GLASS_PANE, DyeColor.BLACK);
      return colors;
   }

   private static DyeableMap getCarpetColors() {
      DyeableMap colors = new DyeableMap();
      colors.addBlock(Blocks.WHITE_CARPET, DyeColor.WHITE);
      colors.addBlock(Blocks.ORANGE_CARPET, DyeColor.ORANGE);
      colors.addBlock(Blocks.MAGENTA_CARPET, DyeColor.MAGENTA);
      colors.addBlock(Blocks.LIGHT_BLUE_CARPET, DyeColor.LIGHT_BLUE);
      colors.addBlock(Blocks.YELLOW_CARPET, DyeColor.YELLOW);
      colors.addBlock(Blocks.LIME_CARPET, DyeColor.LIME);
      colors.addBlock(Blocks.PINK_CARPET, DyeColor.PINK);
      colors.addBlock(Blocks.GRAY_CARPET, DyeColor.GRAY);
      colors.addBlock(Blocks.LIGHT_GRAY_CARPET, DyeColor.LIGHT_GRAY);
      colors.addBlock(Blocks.CYAN_CARPET, DyeColor.CYAN);
      colors.addBlock(Blocks.PURPLE_CARPET, DyeColor.PURPLE);
      colors.addBlock(Blocks.BLUE_CARPET, DyeColor.BLUE);
      colors.addBlock(Blocks.BROWN_CARPET, DyeColor.BROWN);
      colors.addBlock(Blocks.GREEN_CARPET, DyeColor.GREEN);
      colors.addBlock(Blocks.RED_CARPET, DyeColor.RED);
      colors.addBlock(Blocks.BLACK_CARPET, DyeColor.BLACK);
      return colors;
   }

   public static class BedPainter extends PainterHelper.Colorable {
      public BedPainter() {
         super(PainterHelper.getBedColors(), null);
      }

      @Override
      public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
         DyeColor currentColor = this.map.getColor(state.getBlock());
         if (currentColor != null && currentColor != color) {
            Block block = this.map.getBlock(color);
            if (block == null) {
               return false;
            } else {
               world.setBlock(pos, PainterHelper.copyProperties(state, block.defaultBlockState()), 17);
               pos = pos.relative(
                  state.getValue(BedBlock.PART) == BedPart.HEAD
                     ? ((Direction)state.getValue(BedBlock.FACING)).getOpposite()
                     : (Direction)state.getValue(BedBlock.FACING)
               );
               state = world.getBlockState(pos);
               world.setBlock(pos, PainterHelper.copyProperties(state, block.defaultBlockState()), 17);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public static class Colorable implements PainterHelper.IPaintable {
      DyeableMap map;
      BiFunction<BlockState, BlockState, BlockState> mapper;

      public Colorable(DyeableMap map, BiFunction<BlockState, BlockState, BlockState> mapper) {
         this.map = map;
         this.mapper = mapper;
      }

      @Override
      public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
         DyeColor currentColor = this.map.getColor(state.getBlock());
         if (currentColor != null && currentColor != color) {
            Block block = this.map.getBlock(color);
            return block != null && world.setBlockAndUpdate(pos, this.mapper.apply(state, block.defaultBlockState()));
         } else {
            return false;
         }
      }

      @Override
      public DyeColor getColor(BlockState state) {
         return this.map.getColor(state.getBlock());
      }
   }

   public interface IPaintable {
      boolean recolor(BlockState var1, Level var2, BlockPos var3, Vec3 var4, Direction var5, DyeColor var6);

      DyeColor getColor(BlockState var1);
   }

   public static class ShulkerPainter extends PainterHelper.Colorable {
      public ShulkerPainter() {
         super(PainterHelper.getShulkerColors(), null);
      }

      @Override
      public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
         DyeColor currentColor = this.map.getColor(state.getBlock());
         if (currentColor != null && currentColor != color) {
            Block block = this.map.getBlock(color);
            if (block == null) {
               return false;
            } else if (world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity sourceShulker) {
               BlockState newState = PainterHelper.copyProperties(state, block.defaultBlockState());
               if (!world.setBlockAndUpdate(pos, newState)) {
                  return false;
               } else if (world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity targetShulker) {
                  targetShulker.load(sourceShulker.saveWithoutMetadata());
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static class SignPainter implements PainterHelper.IPaintable {
      @Override
      public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
         if (world.getBlockEntity(pos) instanceof SignBlockEntity signEntity) {
            boolean changed = false;
            if (signEntity.getText(true).getColor() != color) {
               signEntity.updateText(text -> text.setColor(color), true);
               changed = true;
            }
            if (signEntity.getText(false).getColor() != color) {
               signEntity.updateText(text -> text.setColor(color), false);
               changed = true;
            }
            return changed;
         } else {
            return false;
         }
      }

      @Override
      public DyeColor getColor(BlockState state) {
         return null;
      }
   }
}
