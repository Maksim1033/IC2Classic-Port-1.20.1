package ic2.api.items;

import ic2.api.tiles.display.IDisplayInfo;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface IDisplayProvider {
   void provideInfo(ItemStack var1, Consumer<IDisplayInfo> var2);
}
