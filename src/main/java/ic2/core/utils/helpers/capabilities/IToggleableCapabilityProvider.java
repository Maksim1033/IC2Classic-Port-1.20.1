package ic2.core.utils.helpers.capabilities;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IToggleableCapabilityProvider extends ICapabilityProvider {
   boolean isActive();

   void setActive(boolean var1);
}
