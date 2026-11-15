package ic2.core.block.cables;

import ic2.core.block.rendering.block.tubes.SuperCableModel;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SuperCableBlock extends CableBlock {
   public static final CableBlock.CableInstance INSTANCE = new CableBlock.CableInstance(
      false, false, 0, new float[]{3.0F}, "electric/cable", Cables::getSuperConductor
   );

   public SuperCableBlock() {
      super("super_cable", IC2Tiles.SUPER_CABLE);
      this.setOverrideRarity(Rarity.UNCOMMON);
   }

   @Override
   public CableBlock.CableInstance createInstance() {
      return INSTANCE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("electric/cable/super").get(side.getAxis().isHorizontal() ? "super_side" : "super_top");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return (BaseModel)(state.getValue(FOAMED) == 0 ? new SuperCableModel(this, state) : super.getForCustomState(state));
   }
}
