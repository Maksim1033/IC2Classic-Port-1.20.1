package ic2.core.wiki.base.managers;

import ic2.core.wiki.helper.PageVisit;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IWikiProvider {
   @OnlyIn(Dist.CLIENT)
   boolean handleClickEvent(ClickEvent var1);

   @OnlyIn(Dist.CLIENT)
   boolean openPage(PageVisit var1, boolean var2);
}
