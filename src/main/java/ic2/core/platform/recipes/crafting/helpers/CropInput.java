package ic2.core.platform.recipes.crafting.helpers;

import com.google.gson.JsonObject;
import ic2.api.crops.BaseSeed;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.item.misc.CropSeedItem;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class CropInput implements IInput {
   ICrop crop;
   int size;
   int growth;
   int gain;
   int resistance;
   int scan;
   boolean exact;

   public CropInput(JsonObject obj) {
      this.crop = ICropRegistry.INSTANCE.getCrop(ResourceLocation.tryParse(obj.get("crop").getAsString()));
      this.size = obj.get("size").getAsInt();
      this.exact = GsonHelper.getAsBoolean(obj, "exact", false);
      if (this.exact) {
         this.growth = Mth.clamp(obj.get("growth").getAsInt(), 0, 31);
         this.gain = Mth.clamp(obj.get("gain").getAsInt(), 0, 31);
         this.resistance = Mth.clamp(obj.get("resistance").getAsInt(), 0, 31);
         this.scan = Mth.clamp(obj.get("scan").getAsInt(), 0, 4);
      }
   }

   public CropInput(ICrop crop) {
      this(crop, 1);
   }

   public CropInput(ICrop crop, int size) {
      this.crop = crop;
      this.size = size;
      this.exact = false;
   }

   public CropInput(ICrop crop, int size, int growth, int gain, int resistance, int scan) {
      this.crop = crop;
      this.size = size;
      this.growth = growth;
      this.gain = gain;
      this.resistance = resistance;
      this.scan = scan;
      this.exact = true;
   }

   public CropInput(BaseSeed seed, boolean exact) {
      this.crop = seed.crop;
      this.size = seed.stack_size;
      this.exact = exact;
      if (exact) {
         this.growth = seed.growth;
         this.gain = seed.gain;
         this.resistance = seed.resistance;
         this.scan = 4;
      }
   }

   public CropInput(FriendlyByteBuf buffer) {
      this.crop = ICropRegistry.INSTANCE.getCrop(buffer.readResourceLocation());
      this.size = buffer.readByte();
      this.exact = buffer.readBoolean();
      if (this.exact) {
         this.growth = buffer.readByte();
         this.gain = buffer.readByte();
         this.resistance = buffer.readByte();
         this.size = buffer.readByte();
      }
   }

   @Override
   public List<ItemStack> getComponents() {
      return this.exact
         ? ObjectLists.singleton(this.create(this.crop, this.growth, this.gain, this.resistance, this.scan))
         : ObjectLists.singleton(this.create(this.crop, 1, 1, 1, 4));
   }

   private ItemStack create(ICrop crop, int growth, int gain, int resistance, int scan) {
      ItemStack stack = CropSeedItem.createStack(crop, growth, gain, resistance, scan);
      stack.setCount(this.size);
      return stack;
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return this.crop == ICropRegistry.INSTANCE.getCrop(stack);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeResourceLocation(this.crop.id());
      buffer.writeByte(this.size);
      buffer.writeBoolean(this.exact);
      if (this.exact) {
         buffer.writeByte(this.growth);
         buffer.writeByte(this.gain);
         buffer.writeByte(this.resistance);
         buffer.writeByte(this.scan);
      }
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("crop", this.crop.id().toString());
      obj.addProperty("size", this.size);
      obj.addProperty("exact", this.exact);
      if (this.exact) {
         obj.addProperty("growth", this.growth);
         obj.addProperty("gain", this.gain);
         obj.addProperty("resistance", this.resistance);
         obj.addProperty("scan", this.scan);
      }

      return obj;
   }
}
