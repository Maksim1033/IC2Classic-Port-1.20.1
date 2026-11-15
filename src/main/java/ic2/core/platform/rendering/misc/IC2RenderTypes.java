package ic2.core.platform.rendering.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ic2.core.item.misc.WindmillBladeItem;
import java.util.Objects;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class IC2RenderTypes extends RenderType {
   public static final RenderType FLUID_RENDER = create(
      "ic2_fluid",
      DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
      Mode.TRIANGLES,
      131072,
      false,
      false,
      CompositeState.builder()
         .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
         .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
         .setLightmapState(LIGHTMAP)
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setOutputState(TRANSLUCENT_TARGET)
         .createCompositeState(true)
   );
   public static final RenderType TUBE_ITEM_HIGHLIGHT = create(
      "tube_item_highlight",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.TRIANGLES,
      131072,
      false,
      false,
      CompositeState.builder()
         .setShaderState(POSITION_COLOR_SHADER)
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setOutputState(TRANSLUCENT_TARGET)
         .createCompositeState(true)
   );
   public static final RenderType BLOCK_POS_TEX = create(
      "ic2_pos_texture",
      DefaultVertexFormat.POSITION_TEX,
      Mode.TRIANGLES,
      256,
      true,
      false,
      CompositeState.builder()
         .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setLightmapState(LIGHTMAP)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .createCompositeState(true)
   );
   public static final RenderType POS_COLOR_TRANSLUCENT = create(
      "ic2_pos_color",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.TRIANGLES,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(POSITION_COLOR_SHADER)
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setOutputState(TRANSLUCENT_TARGET)
         .createCompositeState(true)
   );
   public static final RenderType WINDMILL_ROTORS = RenderType.entityCutout(WindmillBladeItem.ROTOR_SHEET);
   public static final RenderType LINES = create(
      "lines_everywhere",
      DefaultVertexFormat.POSITION_COLOR_NORMAL,
      Mode.LINES,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(RENDERTYPE_LINES_SHADER)
         .setLineState(new LineStateShard(OptionalDouble.empty()))
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setOutputState(ITEM_ENTITY_TARGET)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setDepthTestState(NO_DEPTH_TEST)
         .setCullState(NO_CULL)
         .createCompositeState(false)
   );

   public IC2RenderTypes(
      String nameIn,
      VertexFormat formatIn,
      Mode drawModeIn,
      int bufferSizeIn,
      boolean useDelegateIn,
      boolean needsSortingIn,
      Runnable setupTaskIn,
      Runnable clearTaskIn
   ) {
      super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
      throw new IllegalStateException("ONLY USED FOR CUSTOM RENDER TYPES");
   }

   public static RenderType getShieldType(ResourceLocation locationIn, float scale, float rotation, float translation) {
      return create(
         "energy_shield",
         DefaultVertexFormat.POSITION_TEX,
         Mode.QUADS,
         256,
         false,
         false,
         CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(new TextureStateShard(locationIn, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(new IC2RenderTypes.ShieldState(scale, rotation, translation))
            .setOutputState(MAIN_TARGET)
            .createCompositeState(false)
      );
   }

   public static class ShieldState extends TexturingStateShard {
      float scale;
      float rotation;
      float translation;

      public ShieldState(float scale, float rotation, float translation) {
         super("offset_texturing", () -> {
            RenderSystem.resetTextureMatrix();
            Matrix4f matrix = RenderSystem.getTextureMatrix();
            matrix.scale(scale, scale, scale);
            matrix.rotate(Axis.ZP.rotationDegrees(rotation * 25.0F));
            Vector4f value = new Vector4f(0.0F, translation * (0.001F + rotation * 0.005F) * 20.0F, 0.0F, 0.0F);
            value.rotate(Axis.ZP.rotationDegrees(rotation * 25.0F));
            matrix.translate(value.x(), value.y(), value.z());
         }, RenderSystem::resetTextureMatrix);
         this.scale = scale;
         this.rotation = rotation;
         this.translation = translation;
      }

      public boolean equals(Object obj) {
         return !(obj instanceof IC2RenderTypes.ShieldState state)
            ? false
            : Float.compare(state.scale, this.scale) == 0
               && Float.compare(state.rotation, this.rotation) == 0
               && Float.compare(state.translation, this.translation) == 0;
      }

      public int hashCode() {
         return Objects.hash(this.scale, this.rotation, this.translation);
      }
   }
}
