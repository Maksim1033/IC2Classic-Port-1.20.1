package ic2.core.item.misc;

import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2BoatItem extends IC2Item implements ISimpleItemModel {
   private static final Predicate<Entity> PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
   String texture;
   BiFunction<Level, Vec3, Boat> creator;

   public IC2BoatItem(String name, BiFunction<Level, Vec3, Boat> creator) {
      this(name, name, creator);
   }

   public IC2BoatItem(String name, String texture, BiFunction<Level, Vec3, Boat> creator) {
      super(name, new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).maxStackSize(1));
      this.texture = texture;
      this.creator = creator;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "boat").get(this.texture);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack itemstack = playerIn.getItemInHand(handIn);
      HitResult rayTraceResult = getPlayerPOVHitResult(worldIn, playerIn, Fluid.ANY);
      if (rayTraceResult.getType() == Type.MISS) {
         return new InteractionResultHolder(InteractionResult.PASS, itemstack);
      } else {
         Vec3 Vector3d = playerIn.getViewVector(1.0F);
         List<Entity> list = worldIn.getEntities(playerIn, playerIn.getBoundingBox().expandTowards(Vector3d.scale(5.0)).inflate(1.0), PREDICATE);
         if (!list.isEmpty()) {
            Vec3 vec3d1 = playerIn.getEyePosition(1.0F);

            for (Entity entity : list) {
               AABB axisAlignedBB = entity.getBoundingBox().inflate((double)entity.getPickRadius());
               if (axisAlignedBB.contains(vec3d1)) {
                  return new InteractionResultHolder(InteractionResult.PASS, itemstack);
               }
            }
         }

         if (rayTraceResult.getType() == Type.BLOCK) {
            Boat boatEntity = this.creator.apply(worldIn, rayTraceResult.getLocation());
            boatEntity.setYRot(playerIn.getYRot());
            if (!worldIn.noCollision(boatEntity, boatEntity.getBoundingBox().inflate(-0.1))) {
               return new InteractionResultHolder(InteractionResult.FAIL, itemstack);
            } else {
               if (!worldIn.isClientSide) {
                  worldIn.addFreshEntity(boatEntity);
               }

               if (!playerIn.isCreative()) {
                  itemstack.shrink(1);
               }

               playerIn.awardStat(Stats.ITEM_USED.get(this));
               return new InteractionResultHolder(InteractionResult.SUCCESS, itemstack);
            }
         } else {
            return new InteractionResultHolder(InteractionResult.PASS, itemstack);
         }
      }
   }
}
