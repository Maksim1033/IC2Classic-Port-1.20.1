package ic2.core.block.base.cache;

import ic2.api.util.DirectionList;
import ic2.core.block.base.features.personal.IPersonalTile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class PersonalCapabilityCache<T> extends BaseCache<T> {
   IPersonalTile pos;
   Capability<T> capability;

   public PersonalCapabilityCache(IPersonalTile position, DirectionList validChecks, Capability<T> capability) {
      super(position, validChecks);
      this.pos = position;
      this.capability = capability;
   }

   @Override
   public void update() {
      UUID owner = this.pos.getOwner();
      if (owner != null) {
         Level world = this.pos.getWorldObj();
         BlockPos worldPos = this.pos.getPosition();

         for (Direction dir : this.present.invert().remove(this.validSides.invert())) {
            IPersonalTile tile = DirectionList.getNeighborInterface(world, worldPos, dir, IPersonalTile.class);
            if (tile != null) {
               LazyOptional<T> cap = tile.getPersonalCapability(owner, this.capability, dir.getOpposite());
               if (cap.isPresent()) {
                  this.addCache(dir, (T)cap.orElse(null));
                  cap.addListener(T -> this.clearCache(dir));
               }
            }
         }
      }
   }
}
