package ic2.core.entity.misc;

import ic2.api.events.LaserEvent;
import ic2.core.IC2;
import ic2.core.entity.explosion.IC2Explosion;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.network.NetworkHooks;

public class MiningLaserEntity extends Entity {
   LivingEntity shooter;
   public float range = 0.0F;
   public float power = 0.0F;
   public int blockBreaks = 0;
   public boolean explosive = false;
   public boolean smelt = false;
   public int ticksInAir;
   Predicate<Entity> predicate = T -> T.isPickable() && (T != this.shooter || this.ticksInAir >= 5);

   public MiningLaserEntity(EntityType<?> type, Level worldIn) {
      super(type, worldIn);
   }

   public MiningLaserEntity(EntityType<?> type, Level world, LivingEntity entityLiving, float range, float power, int blockBreaks, boolean explosive) {
      this(type, world, entityLiving, range, power, blockBreaks, explosive, (double)entityLiving.getYRot(), (double)entityLiving.getXRot());
   }

   public MiningLaserEntity(
      EntityType<?> type, Level world, LivingEntity entityLiving, float range, float power, int blockBreaks, boolean explosive, boolean isSmelt
   ) {
      this(type, world, entityLiving, range, power, blockBreaks, explosive, (double)entityLiving.getYRot(), (double)entityLiving.getXRot());
      this.smelt = isSmelt;
   }

   public MiningLaserEntity(
      EntityType<?> type, Level world, LivingEntity entityLiving, float range, float power, int blockBreaks, boolean explosive, double yawDeg, double pitchDeg
   ) {
      this(type, world, entityLiving, range, power, blockBreaks, explosive, yawDeg, pitchDeg, entityLiving.getY() + (double)entityLiving.getEyeHeight() - 0.1);
   }

   public MiningLaserEntity(
      EntityType<?> type,
      Level world,
      LivingEntity shooter,
      float flyRange,
      float laserPower,
      int blockBreakLimit,
      boolean isExplosive,
      double yawDeg,
      double pitchDeg,
      double y
   ) {
      this(type, world);
      this.shooter = shooter;
      double yaw = Math.toRadians(yawDeg);
      double pitch = Math.toRadians(pitchDeg);
      this.setPos(shooter.getX() - Math.cos(yaw) * 0.16, y, shooter.getZ() - Math.sin(yaw) * 0.16);
      this.setLaserHeading(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch), 1.0);
      this.range = flyRange;
      this.power = laserPower;
      this.blockBreaks = blockBreakLimit;
      this.explosive = isExplosive;
   }

   public void setLaserHeading(Vec3 motion, double speed) {
      this.setLaserHeading(motion.x(), motion.y(), motion.z(), speed);
   }

   public void setLaserHeading(double newMotionX, double newMotionY, double newMotionZ, double speed) {
      double newSpeed = 1.0 / Math.sqrt(newMotionX * newMotionX + newMotionY * newMotionY + newMotionZ * newMotionZ) * speed;
      this.setDeltaMovement(newMotionX * newSpeed, newMotionY * newSpeed, newMotionZ * newSpeed);
      Vec3 motion = this.getDeltaMovement();
      this.setYRot(this.yRotO = (float)Math.toDegrees(Math.atan2(motion.x(), motion.z())));
      this.setXRot(this.xRotO = (float)Math.toDegrees(Math.atan2(motion.y(), Math.sqrt(motion.x() * motion.x() + motion.z() * motion.z()))));
   }

   protected void defineSynchedData() {
   }

   public void tick() {
      super.tick();
      boolean simulating = IC2.PLATFORM.isSimulating();
      if (!simulating || !(this.range < 1.0F) && !(this.power <= 0.0F) && this.blockBreaks > 0 && this.shooter != null) {
         this.ticksInAir++;
         if (simulating) {
            Vec3 oldPos = new Vec3(this.getX(), this.getY(), this.getZ());
            HitResult result = this.level().clip(new ClipContext(oldPos, oldPos.add(this.getDeltaMovement()), Block.COLLIDER, Fluid.NONE, this));
            Vec3 newPos = result.getType() == Type.MISS ? oldPos.add(this.getDeltaMovement()) : result.getLocation();
            HitResult entityResult = ProjectileUtil.getEntityHitResult(
               this.level(), this, oldPos, newPos, this.getBoundingBox().expandTowards(this.getDeltaMovement()), this.predicate
            );
            if (entityResult != null) {
               result = entityResult;
            }

            if (result.getType() != Type.MISS) {
               if (this.explosive) {
                  if (this.canHitPlayer(result)) {
                     this.explode();
                  }

                  this.remove(RemovalReason.DISCARDED);
                  return;
               }

               switch (result.getType()) {
                  case ENTITY:
                     LaserEvent.LaserEntityHitEvent entityHitEvent = new LaserEvent.LaserEntityHitEvent(
                        this.level(),
                        this,
                        this.shooter,
                        this.range,
                        this.power,
                        this.blockBreaks,
                        this.explosive,
                        this.smelt,
                        ((EntityHitResult)result).getEntity()
                     );
                     MinecraftForge.EVENT_BUS.post(entityHitEvent);
                     label142:
                     if (this.loadEventData(entityHitEvent)) {
                        int damage = (int)this.power;
                        if (damage > 0 && this.canHitPlayer(result)) {
                           Entity target = entityHitEvent.hitEntity;
                           if (target == null) {
                              break label142;
                           }

                           target.setSecondsOnFire(damage * (this.smelt ? 2 : 1));
                           if (target instanceof EnderMan) {
                              target.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARROW), this), (float)damage);
                           } else if (target.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARROW), this, this.shooter), (float)damage)
                              && this.shooter instanceof Player) {
                              EnderDragon dragon = this.getDragon(target);
                              if (dragon != null && dragon.getHealth() <= 1.0F) {
                                 ((Player)this.shooter).awardStat(IC2Stats.DRAGONS_SHOT);
                                 IC2Advancements.ID_TRIGGER.onTrigger((Player)this.shooter, IC2Stats.DRAGONS_SHOT);
                              }
                           }

                           if (!target.isAlive() && this.shooter instanceof Player) {
                              ((Player)this.shooter).awardStat(IC2Stats.PLAYERS_SHOT);
                           }
                        }

                        this.remove(RemovalReason.DISCARDED);
                     }
                     break;
                  case BLOCK:
                     LaserEvent.LaserBlockHitEvent blockHitEvent = new LaserEvent.LaserBlockHitEvent(
                        this.level(),
                        this,
                        this.shooter,
                        this.range,
                        this.power,
                        this.blockBreaks,
                        this.explosive,
                        this.smelt,
                        (BlockHitResult)result,
                        true,
                        true,
                        0.9F
                     );
                     MinecraftForge.EVENT_BUS.post(blockHitEvent);
                     if (this.loadEventData(blockHitEvent)) {
                        BlockPos pos = blockHitEvent.pos;
                        BlockState state = this.level().getBlockState(pos);
                        if (!state.isAir() && state.getBlock().asItem() != Items.AIR && !state.is(Blocks.GLASS) && !state.is(Blocks.GLASS_PANES)) {
                           if (this.canMine(pos, state)) {
                              this.power = this.power
                                 - 0.15F
                                    * state.getExplosionResistance(
                                       this.level(),
                                       pos,
                                       new Explosion(
                                          this.level(),
                                          this,
                                          this.level().damageSources().generic(),
                                          null,
                                          (double)pos.getX() + 0.5,
                                          (double)pos.getY() + 0.5,
                                          (double)pos.getZ() + 0.5,
                                          blockHitEvent.power,
                                          false,
                                          BlockInteraction.DESTROY
                                       )
                                    );
                              if (this.power >= 0.0F) {
                                 if (state.isAir()) {
                                    state.onBlockExploded(
                                       this.level(),
                                       pos,
                                       new Explosion(
                                          this.level(),
                                          this,
                                          this.level().damageSources().generic(),
                                          null,
                                          (double)pos.getX() + 0.5,
                                          (double)pos.getY() + 0.5,
                                          (double)pos.getZ() + 0.5,
                                          1.0F,
                                          false,
                                          BlockInteraction.DESTROY
                                       )
                                    );
                                 } else if (this.smelt) {
                                    if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS)) {
                                       blockHitEvent.dropBlock = false;
                                    }

                                    LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.level());
                                    builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
                                    builder.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                       .withOptionalParameter(LootContextParams.BLOCK_ENTITY, this.level().getBlockEntity(pos));
                                    List<ItemStack> resultDrops = state.getDrops(builder);
                                    Container inv = new SimpleContainer(1);
                                    int i = 0;

                                    for (int m = resultDrops.size(); i < m; i++) {
                                       ItemStack stack = resultDrops.get(i);
                                       inv.setItem(0, StackUtil.copyWithSize(stack, 1));
                                       Optional<SmeltingRecipe> recipe = IC2.PLATFORM.getRecipes().getRecipeFor(RecipeType.SMELTING, inv, this.level());
                                       if (recipe.isPresent()) {
                                          ItemStack resultStack = recipe.get().assemble(inv, this.level().registryAccess());
                                          net.minecraft.world.level.block.Block resultBlock = net.minecraft.world.level.block.Block.byItem(
                                             resultStack.getItem()
                                          );
                                          if (resultBlock != net.minecraft.world.level.block.Blocks.AIR && resultBlock != state.getBlock()) {
                                             blockHitEvent.removeBlock = false;
                                             this.level().setBlockAndUpdate(pos, resultBlock.defaultBlockState());
                                          } else {
                                             net.minecraft.world.level.block.Block.popResource(
                                                this.level(), pos, StackUtil.copyWithSize(resultStack, resultStack.getCount() * stack.getCount())
                                             );
                                          }

                                          blockHitEvent.dropBlock = false;
                                          this.power = 0.0F;
                                       }

                                       resultDrops.remove(i--);
                                       m--;
                                    }
                                 }

                                 if (blockHitEvent.removeBlock && !state.isAir()) {
                                    if (blockHitEvent.dropBlock) {
                                       net.minecraft.world.level.block.Block.dropResources(state, this.level(), pos, this.level().getBlockEntity(pos));
                                    }

                                    this.level().removeBlock(pos, false);
                                    if (this.shooter instanceof Player player) {
                                       player.awardStat(IC2Stats.BLOCKS_LASERED);
                                    }

                                    if (this.level().random.nextInt(10) == 0 && (state.ignitedByLava() || state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS))) {
                                       this.level().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                                    }
                                 }

                                 this.blockBreaks--;
                              }
                           } else {
                              this.remove(RemovalReason.DISCARDED);
                           }
                        }
                     }
                     break;
                  default:
                     this.power -= 0.5F;
               }
            }
         }

         Vec3 motion = this.getDeltaMovement();
         this.setPos(this.getX() + motion.x(), this.getY() + motion.y(), this.getZ() + motion.z());
         this.range -= 0.02F;
         if (this.isInWater()) {
            this.remove(RemovalReason.DISCARDED);
         }
      } else {
         if (this.explosive) {
            this.explode();
         }

         this.remove(RemovalReason.DISCARDED);
      }
   }

   protected void explode() {
      if (IC2.PLATFORM.isSimulating()) {
         LaserEvent.LaserExplodeEvent event = new LaserEvent.LaserExplodeEvent(
            this.level(), this, this.shooter, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0F, 0.85F
         );
         MinecraftForge.EVENT_BUS.post(event);
         if (this.loadEventData(event)) {
            new IC2Explosion(this.level(), this, this.position(), event.explosionPower, event.explosionDropRate, this.level().damageSources().generic())
               .setIgniter(this.shooter instanceof Player ? (Player)this.shooter : null)
               .doExplosion();
         }
      }
   }

   public boolean canHitPlayer(HitResult result) {
      if (this.shooter instanceof Player && result instanceof EntityHitResult) {
         Entity hit = ((EntityHitResult)result).getEntity();
         return !(hit instanceof Player) || ((Player)this.shooter).canHarmPlayer((Player)hit);
      } else {
         return true;
      }
   }

   public boolean canMine(BlockPos pos, BlockState state) {
      return !MinecraftForge.EVENT_BUS
            .post(
               new BreakEvent(
                  this.level(),
                  pos,
                  state,
                  (Player)(this.shooter instanceof Player ? (Player)this.shooter : FakePlayerFactory.getMinecraft((ServerLevel)this.level()))
               )
            )
         && !state.is(IC2Tags.MINING_LASER_UNBREAKABLE);
   }

   public EnderDragon getDragon(Entity entity) {
      if (entity instanceof EnderDragon) {
         return (EnderDragon)entity;
      } else {
         return entity instanceof EnderDragonPart ? ((EnderDragonPart)entity).parentMob : null;
      }
   }

   public boolean loadEventData(LaserEvent event) {
      this.shooter = event.source;
      this.range = event.range;
      this.power = event.power;
      this.blockBreaks = event.blockBreaks;
      this.explosive = event.explosive;
      this.smelt = event.smelt;
      if (event.isCanceled()) {
         this.remove(RemovalReason.DISCARDED);
         return false;
      } else {
         return true;
      }
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      if (compound.hasUUID("source")) {
         if (this.level() instanceof ServerLevel) {
            UUID id = compound.getUUID("source");
            this.shooter = (LivingEntity)((ServerLevel)this.level()).getEntity(id);
            if (this.shooter == null) {
               Player player = FakePlayerFactory.getMinecraft((ServerLevel)this.level());
               if (player.getUUID().equals(id)) {
                  this.shooter = player;
               }
            }
         } else {
            this.shooter = null;
         }

         this.range = compound.getFloat("range");
         this.power = compound.getFloat("power");
         this.blockBreaks = compound.getInt("broken");
         this.ticksInAir = compound.getInt("in_air");
         this.explosive = compound.getBoolean("explosive");
         this.smelt = compound.getBoolean("smelt");
      }
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      if (this.shooter != null) {
         compound.putUUID("source", this.shooter.getUUID());
         compound.putFloat("range", this.range);
         compound.putFloat("power", this.power);
         compound.putInt("broken", this.blockBreaks);
         compound.putInt("in_air", this.ticksInAir);
         compound.putBoolean("explosive", this.explosive);
         compound.putBoolean("smelt", this.smelt);
      }
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
