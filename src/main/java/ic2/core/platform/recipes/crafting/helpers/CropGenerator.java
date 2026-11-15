package ic2.core.platform.recipes.crafting.helpers;

import com.google.gson.JsonObject;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.core.item.misc.CropSeedItem;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class CropGenerator implements IOutputGenerator {
   ICrop crop;
   int size;
   int growth;
   int gain;
   int resistance;
   int scan;

   public CropGenerator(JsonObject obj) {
      this(
         ICropRegistry.INSTANCE.getCrop(ResourceLocation.tryParse(obj.get("crop").getAsString())),
         obj.get("size").getAsInt(),
         obj.get("growth").getAsInt(),
         obj.get("gain").getAsInt(),
         obj.get("resistance").getAsInt(),
         obj.get("scan").getAsInt()
      );
   }

   public CropGenerator(ICrop crop, int size, int growth, int gain, int resistance, int scan) {
      this.crop = crop;
      this.size = size;
      this.growth = Mth.clamp(growth, 0, 31);
      this.gain = Mth.clamp(gain, 0, 31);
      this.resistance = Mth.clamp(resistance, 0, 31);
      this.scan = Mth.clamp(scan, 0, 4);
   }

   @Override
   public void addItems(Consumer<ItemStack> output) {
      output.accept(CropSeedItem.createStack(this.crop, this.growth, this.gain, this.resistance, this.scan));
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("crop", this.crop.id().toString());
      obj.addProperty("size", this.size);
      obj.addProperty("growth", this.growth);
      obj.addProperty("gain", this.gain);
      obj.addProperty("resistance", this.resistance);
      obj.addProperty("scan", this.scan);
      return obj;
   }
}
