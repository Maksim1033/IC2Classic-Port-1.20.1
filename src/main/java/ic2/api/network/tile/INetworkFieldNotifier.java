package ic2.api.network.tile;

import ic2.api.network.IPlayerPacket;
import java.util.Set;
import net.minecraft.world.entity.player.Player;

public interface INetworkFieldNotifier extends IPlayerPacket {
   void onNetworkFieldChanged(Set<String> var1, Player var2);

   void onGuiFieldChanged(Set<String> var1, Player var2);
}
