package ic2.core.block.base.features;

import net.minecraft.network.chat.MutableComponent;

public interface IProfileListener {
   void onProfile(long var1);

   long getLag();

   MutableComponent showResults();
}
