package ic2.api.tiles.tubes;

import java.util.UUID;
import java.util.function.ObjIntConsumer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface IProviderTube extends ITube {
   int provideItems(ItemStack var1, int var2, DyeColor var3, UUID var4);

   void getItemsProvided(ObjIntConsumer<ItemStack> var1);

   long getProviderSource();
}
