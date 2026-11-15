package ic2.core.block.rendering.block.base;

import org.joml.Vector3f;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.platform.rendering.models.blocks.SimpleBlockModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

public class CandleCakeModel extends SimpleBlockModel {
   static final ShapeBuilder CANDLE = new ShapeBuilder()
      .newQuad(7.0, 8.0, 7.0, 9.0, 14.0, 9.0)
      .addFaces(DirectionList.HORIZONTAL, 0.0F, 8.0F, 2.0F, 14.0F)
      .addFace(Direction.UP, 0.0F, 6.0F, 2.0F, 8.0F)
      .addFace(Direction.DOWN, 0.0F, 14.0F, 2.0F, 16.0F)
      .finish();
   static final ShapeBuilder CANDLE_WIRE = new ShapeBuilder()
      .newQuad(7.5, 14.0, 8.0, 8.5, 15.0, 8.0)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 5.0F, 1.0F, 6.0F)
      .finish();
   ResourceLocation candleTexture;

   public CandleCakeModel(BlockState state, IBlockModel model, ResourceLocation candleTexture) {
      super(state, model);
      this.candleTexture = candleTexture;
   }

   @Override
   public void init() {
      super.init();
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.candleTexture);
      CANDLE.buildQuads(sprite, BlockModelRotation.X0_Y0, null, true, this.quads[6]);
      CANDLE_WIRE.buildQuads(
         sprite, BlockModelRotation.X0_Y0, new BlockElementRotation(new Vector3f(0.5F, 0.875F, 0.5F), Axis.Y, -45.0F, false), true, this.quads[6]
      );
      CANDLE_WIRE.buildQuads(
         sprite, BlockModelRotation.X0_Y0, new BlockElementRotation(new Vector3f(0.5F, 0.875F, 0.5F), Axis.Y, 45.0F, false), true, this.quads[6]
      );
   }
}
