package ic2.core.networking.buffers;

import ic2.api.network.buffer.IBitLevelOverride;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkFieldProvider;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FieldBuffer {
   final Class<? extends BlockEntity> tile;
   Map<String, FieldBuffer.FieldInfo> fieldNameToIndex = CollectionUtils.<String, FieldBuffer.FieldInfo>createMap();
   Int2ObjectMap<FieldBuffer.FieldInfo> indexToField = new Int2ObjectOpenHashMap();

   public FieldBuffer(Class<? extends BlockEntity> clz) {
      this.tile = clz;
   }

   public void initClass(BlockEntity tileData) {
      try {
         IBitLevelOverride override = tileData instanceof IBitLevelOverride ? (IBitLevelOverride)tileData : null;
         Class<?> currentClass = this.tile;

         List<Field> toProcess;
         for (toProcess = CollectionUtils.<Field>createList();
            currentClass != null && currentClass != BlockEntity.class;
            currentClass = currentClass.getSuperclass()
         ) {
            Field[] fields = currentClass.getDeclaredFields();

            for (int i = fields.length - 1; i >= 0; i--) {
               Field field = fields[i];
               field.setAccessible(true);
               if (field.getDeclaredAnnotation(NetworkInfo.class) != null) {
                  toProcess.add(field);
               } else {
                  String name = field.getName();
                  this.fieldNameToIndex.put(name, new FieldBuffer.FieldInfo(field, name));
               }
            }
         }

         int ix = toProcess.size() - 1;

         for (int index = 0; ix >= 0; ix--) {
            Field field = toProcess.get(ix);
            NetworkInfo data = field.getDeclaredAnnotation(NetworkInfo.class);
            String name = field.getName();
            FieldBuffer.FieldInfo info = new FieldBuffer.FieldInfo(field, name, index++, data, override);
            this.fieldNameToIndex.put(name, info);
            this.indexToField.put(info.getIndex(), info);
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }
   }

   public FieldBuffer.FieldInfo getInfo(String id) {
      return this.fieldNameToIndex.get(id);
   }

   public FieldBuffer.FieldInfo getInfo(int index) {
      return (FieldBuffer.FieldInfo)this.indexToField.get(index);
   }

   public List<FieldBuffer.FieldInfo> getAllInfos() {
      return new ObjectArrayList(this.indexToField.values());
   }

   public static class FieldInfo {
      Field field;
      boolean isNew;
      int index;
      String fieldName;
      NetworkInfo.BitLevel compression;

      public FieldInfo(Field theField, String fieldID) {
         this.field = theField;
         this.fieldName = fieldID;
         this.index = -1;
         this.isNew = false;
         this.compression = NetworkInfo.BitLevel.BIT_0;
      }

      public FieldInfo(Field theField, String fieldID, int indexValue, NetworkInfo fieldData, IBitLevelOverride override) {
         this.field = theField;
         this.fieldName = fieldID;
         this.index = indexValue;
         this.isNew = this.index != -1;
         this.compression = fieldData.value();
         if (override != null && override.hasOverride(this.index, this.fieldName)) {
            this.compression = override.getOverride(this.index, this.fieldName);
         }
      }

      public int getIndex() {
         return this.index;
      }

      public boolean isNew() {
         return this.isNew;
      }

      public NetworkInfo.BitLevel getBits() {
         return this.compression;
      }

      public String getFieldID() {
         return this.fieldName;
      }

      public boolean isDefault(INetworkFieldProvider tile) {
         return tile.isDefaultData(this.fieldName);
      }

      public Object getData(BlockEntity tile) {
         try {
            return this.field.get(tile);
         } catch (Exception var3) {
            return null;
         }
      }

      public void setData(BlockEntity tile, Object obj) throws Exception {
         if (!this.isNew) {
            IC2.LOGGER
               .info(
                  "TileEntity: ({}.java:0) uses a deprecated IC2C Networking. Please Implement @NetworkInfo at field: {}",
                  tile.getClass().getSimpleName(),
                  this.fieldName
               );
         }

         if (this.field.getType().isEnum() && obj != null) {
            obj = Enum.valueOf((Class)this.field.getType(), (String)obj);
         }

         if (INetworkDataBuffer.class.isAssignableFrom(this.field.getType()) && obj instanceof InputBuffer) {
            ((INetworkDataBuffer)this.field.get(tile)).read((IInputBuffer)obj);
         } else {
            this.field.set(tile, obj);
         }
      }
   }
}
