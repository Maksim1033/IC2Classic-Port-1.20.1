package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Villager.class})
public interface VillagerMixin {
   @Invoker("updateSpecialPrices")
   void updatePrices(Player var1);
}
