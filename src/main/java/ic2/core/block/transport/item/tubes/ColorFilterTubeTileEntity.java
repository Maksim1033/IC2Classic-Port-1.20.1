package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.ColorFilterTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.SynchronizedList;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColorFilterTubeTileEntity extends TubeTileEntity implements ICamouflageGui, ICopyableSettings {
   @NetworkInfo
   public int[] colorDirections = new int[16];
   @NetworkInfo
   public int emptyDirections = 63;
   @NetworkInfo
   public boolean invPriority = false;
   @NetworkInfo
   public SynchronizedList<DyeColor> usedColors = new SynchronizedList<>(IOutputBuffer::writeEnum, T -> T.readEnum(DyeColor.class));

   public ColorFilterTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"colorDirections", "emptyDirections", "invPriority", "usedColors"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.COLOR_FILTER_TUBE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "invPriority", this.invPriority, false);
      compound.putIntArray("color_dirs", this.colorDirections);
      byte[] dirs = new byte[this.usedColors.size() * 2];
      int i = 0;

      for (int m = this.usedColors.size(); i < m; i++) {
         DyeColor color = (DyeColor)this.usedColors.get(i);
         dirs[i * 2] = (byte)color.getId();
         dirs[i * 2 + 1] = (byte)this.colorDirections[color.getId()];
      }

      compound.putByteArray("filters", dirs);
      compound.putInt("emptyDirs", this.emptyDirections);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      byte[] dirs = compound.getByteArray("filters");
      int i = 0;

      for (int m = dirs.length / 2; i < m; i++) {
         DyeColor color = DyeColor.byId(dirs[i * 2]);
         this.usedColors.add(color);
         this.colorDirections[color.getId()] = dirs[i * 2 + 1];
      }

      this.invPriority = compound.getBoolean("invPriority");
      this.emptyDirections = compound.contains("emptyDirs") ? compound.getInt("emptyDirs") : this.generateEmptyDirs();
   }

   @Override
   public void saveSettings(CompoundTag tag) {
      NBTUtils.putBoolean(tag, "invPriority", this.invPriority, false);
      tag.putIntArray("color_dirs", this.colorDirections);
      tag.putInt("emptyDirs", this.emptyDirections);
      byte[] dirs = new byte[this.usedColors.size() * 2];
      int i = 0;

      for (int m = this.usedColors.size(); i < m; i++) {
         DyeColor color = (DyeColor)this.usedColors.get(i);
         dirs[i * 2] = (byte)color.getId();
         dirs[i * 2 + 1] = (byte)this.colorDirections[color.getId()];
      }

      tag.putByteArray("filters", dirs);
   }

   @Override
   public void loadSettings(CompoundTag tag) {
      this.colorDirections = new int[16];
      this.usedColors.clear();
      byte[] dirs = tag.getByteArray("filters");
      int i = 0;

      for (int m = dirs.length / 2; i < m; i++) {
         DyeColor color = DyeColor.byId(dirs[i * 2]);
         this.usedColors.add(color);
         this.colorDirections[color.getId()] = dirs[i * 2 + 1];
      }

      this.invPriority = tag.getBoolean("invPriority");
      this.emptyDirections = tag.contains("emptyDirs") ? tag.getInt("emptyDirs") : this.generateEmptyDirs();
      this.updateGuiFields(new String[]{"colorDirections", "emptyDirections", "invPriority", "usedColors"});
   }

   private int generateEmptyDirs() {
      DirectionList list = DirectionList.ALL;

      for (int i = 0; i < 16; i++) {
         list = list.remove(DirectionList.ofNumber(this.colorDirections[i]));
      }

      return list.getCode();
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ColorFilterTubeContainer(this, player, windowID);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 1) {
         if (value >= 0 && value < this.usedColors.size()) {
            this.colorDirections[((DyeColor)this.usedColors.remove(value)).getId()] = 0;
            this.updateGuiFields(new String[]{"usedColors", "colorDirections"});
         }
      } else if (key == 2) {
         this.invPriority = !this.invPriority;
         this.updateGuiField("invPriority");
      } else if (key == 3) {
         DyeColor color = DyeColor.byId(value);
         if (this.usedColors.contains(color)) {
            return;
         }

         this.usedColors.add(color);
         this.colorDirections[color.getId()] = DirectionList.ALL.getCode();
         this.updateGuiFields(new String[]{"usedColors", "colorDirections"});
      } else if (key >= 10 && key < 16) {
         if (value >= 0 && value < this.usedColors.size()) {
            DyeColor color = (DyeColor)this.usedColors.get(value);
            this.colorDirections[color.getId()] ^= 1 << key - 10;
            this.updateGuiField("colorDirections");
         } else if (value == -1) {
            this.emptyDirections = this.emptyDirections ^ 1 << Direction.from3DDataValue(key - 10).get3DDataValue();
            this.updateGuiField("emptyDirections");
         }
      }
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      DirectionList list = super.getValidDirections(item);
      if (this.isSimulating() && !list.isEmpty()) {
         if (item.getColor() != null) {
            DirectionList filter = list.keep(DirectionList.ofNumber(this.colorDirections[item.getColor().getId()]));
            list = filter.isEmpty() ? list.keep(DirectionList.ofNumber(this.emptyDirections)) : filter;
         } else {
            list = list.keep(DirectionList.ofNumber(this.emptyDirections));
         }

         if (this.invPriority && item.getRequestId() == null && !list.keep(this.inventories.getPresentSides()).isEmpty()) {
            list = list.keep(this.inventories.getPresentSides());
         }
      }

      return list;
   }
}
