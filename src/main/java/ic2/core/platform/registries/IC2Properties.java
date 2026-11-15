package ic2.core.platform.registries;

import ic2.api.util.DirectionList;
import ic2.core.utils.math.ConnectionState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class IC2Properties {
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   public static final IntegerProperty ACTIVE_0_2 = IntegerProperty.create("active", 0, 2);
   public static final IntegerProperty ACTIVE_0_3 = IntegerProperty.create("active", 0, 3);
   public static final BooleanProperty LAVA_LOGGED = BooleanProperty.create("lavalogged");
   public static final DirectionProperty ALL_FACINGS = DirectionProperty.create("facing", DirectionList.ALL);
   public static final DirectionProperty HORIZONTAL_FACINGS = DirectionProperty.create("facing", DirectionList.HORIZONTAL.toFacings());
   public static final EnumProperty<DyeColor> MAIN_COLOR = EnumProperty.create("main_color", DyeColor.class);
   public static final EnumProperty<DyeColor> SECOND_COLOR = EnumProperty.create("second_color", DyeColor.class);
   public static final BooleanProperty LIGHT = BooleanProperty.create("light");
   public static final IntegerProperty LIGHT_0_15 = IntegerProperty.create("light", 0, 15);
   public static final BooleanProperty REDSTONE = BooleanProperty.create("redstone");
   public static final BooleanProperty STRUCTURE_FORMED = BooleanProperty.create("formed");
   public static final IntegerProperty FORMED_STATE2X2 = IntegerProperty.create("state", 0, 7);
   public static final IntegerProperty FORMED_STATE3X3 = IntegerProperty.create("state", 0, 26);
   public static final IntegerProperty FORMED_STATE4X4 = IntegerProperty.create("state", 0, 63);
   public static final IntegerProperty FORMED_STATE3X3_FLAT = IntegerProperty.create("state", 0, 9);
   public static final IntegerProperty DYNAMIC_STATE = IntegerProperty.create("size", 0, 2);
   public static final float[][][][] UVS_2_4 = new float[][][][]{
      ConnectionState.createTextureCutting(2, 2), ConnectionState.createTextureCutting(3, 3), ConnectionState.createTextureCutting(4, 4)
   };
}
