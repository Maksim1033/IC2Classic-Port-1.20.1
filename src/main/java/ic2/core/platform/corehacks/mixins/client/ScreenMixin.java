package ic2.core.platform.corehacks.mixins.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({AbstractContainerScreen.class})
public interface ScreenMixin {
   @Accessor("lastClickButton")
   void setLastClickButton(int var1);
}
