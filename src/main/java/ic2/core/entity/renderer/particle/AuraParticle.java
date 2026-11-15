package ic2.core.entity.renderer.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class AuraParticle extends SingleQuadParticle {
   public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
   TextureAtlasSprite sprite;

   public AuraParticle(ClientLevel world, double x, double y, double z, int maxAge, double[] velocity, float[] colour) {
      super(world, x, y, z, velocity[0], velocity[1], velocity[2]);
      this.rCol = colour[0];
      this.gCol = colour[1];
      this.bCol = colour[2];
      this.setSize(0.02F, 0.02F);
      this.quadSize = this.quadSize * (this.random.nextFloat() * 0.5F + 0.5F);
      this.yd *= 0.2;
      this.lifetime = (int)((double)maxAge / (Math.random() * 0.8 + 0.2));
      this.sprite = ((TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(LOCATION_PARTICLES))
         .getSprite(new ResourceLocation("minecraft:particle/generic_2"));
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.x = this.x + this.xd;
      this.y = this.y + this.yd;
      this.z = this.z + this.zd;
      this.xd *= 0.99;
      this.yd *= 0.99;
      this.zd *= 0.99;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   protected float getU0() {
      return this.sprite.getU0();
   }

   protected float getU1() {
      return this.sprite.getU1();
   }

   protected float getV0() {
      return this.sprite.getV0();
   }

   protected float getV1() {
      return this.sprite.getV1();
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }
}
