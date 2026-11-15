package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.IC2;
import ic2.core.platform.rendering.IC2Models;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class MultiBlock {
   Long2ObjectMap<BlockState> formedStates = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<BlockState> unformedStates = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<BlockEntity> tileCache = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<ModelData> modelData = new Long2ObjectLinkedOpenHashMap();
   LongSet disabledTileEntities = new LongOpenHashSet();
   AABB bounds = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   Vec3 offset = new Vec3(0.0, 0.0, 0.0);
   int limit;

   public MultiBlock() {
      this.modelData.defaultReturnValue(ModelData.EMPTY);
   }

   public MultiBlock addBlock(Block block, int x, int y, int z) {
      return this.addBlock(block.defaultBlockState(), new BlockPos(x, y, z));
   }

   public MultiBlock addBlock(BlockState state, int x, int y, int z) {
      return this.addBlock(state, new BlockPos(x, y, z));
   }

   public MultiBlock addBlock(Block block, BlockPos pos) {
      return this.addBlock(block.defaultBlockState(), pos);
   }

   public MultiBlock addBlock(BlockState state, BlockPos pos) {
      this.formedStates.put(pos.asLong(), state);
      this.bounds = this.bounds.minmax(new AABB(pos));
      return this;
   }

   public MultiBlock addUnformedBlock(Block block, int x, int y, int z) {
      return this.addUnformedBlock(block.defaultBlockState(), new BlockPos(x, y, z));
   }

   public MultiBlock addUnformedBlock(BlockState state, int x, int y, int z) {
      return this.addUnformedBlock(state, new BlockPos(x, y, z));
   }

   public MultiBlock addUnformedBlock(Block block, BlockPos pos) {
      return this.addUnformedBlock(block.defaultBlockState(), pos);
   }

   public MultiBlock addUnformedBlock(BlockState state, BlockPos pos) {
      this.unformedStates.put(pos.asLong(), state);
      return this;
   }

   public MultiBlock setOffset(double x, double y, double z) {
      this.offset = new Vec3(x, y, z);
      return this;
   }

   public <T> MultiBlock withModelData(ModelProperty<T> prop, T value) {
      return this.withModelData(ModelData.builder().with(prop, value).build());
   }

   public MultiBlock withModelData(ModelData modelData) {
      this.modelData.defaultReturnValue(modelData);
      return this;
   }

   public <T> MultiBlock withPositionedModelData(ModelProperty<T> prop, T value, BlockPos pos) {
      return this.withPositionedModelData(ModelData.builder().with(prop, value).build(), pos.getX(), pos.getY(), pos.getZ());
   }

   public <T> MultiBlock withPositionedModelData(ModelProperty<T> prop, T value, int x, int y, int z) {
      return this.withPositionedModelData(ModelData.builder().with(prop, value).build(), x, y, z);
   }

   public MultiBlock withPositionedModelData(ModelData modelData, BlockPos pos) {
      return this.withPositionedModelData(modelData, pos.getX(), pos.getY(), pos.getZ());
   }

   public MultiBlock withPositionedModelData(ModelData modelData, int x, int y, int z) {
      this.modelData.put(BlockPos.asLong(x, y, z), modelData);
      return this;
   }

   public MultiBlock build() {
      ObjectIterator var1 = Long2ObjectMaps.fastIterable(this.formedStates).iterator();

      while (var1.hasNext()) {
         Entry<BlockState> entry = (Entry<BlockState>)var1.next();
         this.unformedStates.putIfAbsent(entry.getLongKey(), ((BlockState)entry.getValue()).getBlock().defaultBlockState());
      }

      return this;
   }

   public MultiBlock copy() {
      MultiBlock block = new MultiBlock();
      block.bounds = this.bounds;
      block.offset = this.offset;
      block.modelData.putAll(this.modelData);
      block.modelData.defaultReturnValue((ModelData)this.modelData.defaultReturnValue());
      block.formedStates.putAll(this.formedStates);
      block.unformedStates.putAll(this.unformedStates);
      return block;
   }

   public void setYLimit(int limit) {
      this.limit = limit;
   }

   public AABB getBounds() {
      return this.bounds;
   }

   public Vec3 getOffset() {
      return this.offset;
   }

   private boolean isEmptyBlock(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.isAir() || !state.blocksMotion();
   }

   public boolean isFullyBuild(Level world, BlockPos pos) {
      MutableBlockPos mutable = new MutableBlockPos();
      ObjectIterator var4 = Long2ObjectMaps.fastIterable(this.formedStates).iterator();

      while (var4.hasNext()) {
         Entry<BlockState> entry = (Entry<BlockState>)var4.next();
         if (this.isEmptyBlock(world, mutable.set(entry.getLongKey()).move(pos))) {
            return false;
         }
      }

      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderMultiBlock(PoseStack stack, VertexConsumer helper, Level world, BlockPos location, Rotation rotate, boolean formed) {
      if (!this.formedStates.isEmpty()) {
         BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
         BlockEntityRenderDispatcher entityDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
         MutableBlockPos pos = new MutableBlockPos();
         RandomSource source = RandomSource.create();
         MultiBlock.TintGetter tint = new MultiBlock.TintGetter(world);

         for (RenderType type : RenderType.chunkBufferLayers()) {
            ObjectIterator entry = Long2ObjectMaps.fastIterable(formed ? this.formedStates : this.unformedStates).iterator();

            while (entry.hasNext()) {
               Entry<BlockState> entryx = (Entry<BlockState>)entry.next();
               BlockState state = (BlockState)entryx.getValue();
               long longPos = entryx.getLongKey();
               pos.set(pos.set(longPos).rotate(rotate)).move(location);
               source.setSeed(state.getSeed(pos));
               if (IC2Models.getRenderTypes(state, source).contains(type)
                  && this.isEmptyBlock(world, pos)
                  && !((double)(pos.getY() - location.getY()) >= this.bounds.maxY - (double)this.limit)) {
                  stack.pushPose();
                  stack.translate((double)(pos.getX() - location.getX()), (double)(pos.getY() - location.getY()), (double)(pos.getZ() - location.getZ()));
                  if (state.getRenderShape() == RenderShape.MODEL) {
                     dispatcher.renderBatched(
                        state.rotate(world, pos, rotate),
                        pos,
                        tint,
                        stack,
                        helper,
                        false,
                        world.random,
                        formed ? (ModelData)this.modelData.get(longPos) : ModelData.EMPTY,
                        type
                     );
                     this.disabledTileEntities.add(longPos);
                  }

                  stack.popPose();
               }
            }
         }

         if (!Tesselator.getInstance().getBuilder().building()) {
            BufferSource impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            ObjectIterator var23 = Long2ObjectMaps.fastIterable(formed ? this.formedStates : this.unformedStates).iterator();

            while (var23.hasNext()) {
               Entry<BlockState> entry = (Entry<BlockState>)var23.next();
               BlockState state = (BlockState)entry.getValue();
               long longPos = entry.getLongKey();
               Block tile = state.getBlock();
               if (tile instanceof EntityBlock) {
                  EntityBlock entity = (EntityBlock)tile;
                  if (!this.disabledTileEntities.contains(longPos)) {
                     pos.set(pos.set(longPos).rotate(rotate));
                     if (!((double)pos.getY() >= this.bounds.maxY - (double)this.limit)) {
                        if (location.getY() <= 256) {
                           pos.move(location);
                        }

                        BlockEntity tilex = (BlockEntity)this.tileCache
                           .computeIfAbsent(longPos, T -> entity.newBlockEntity(pos.set(pos.set(longPos).rotate(rotate)).move(location).immutable(), state));
                        if (tilex != null) {
                           try {
                              stack.pushPose();
                              stack.translate((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                              entityDispatcher.getRenderer(tilex).render(tilex, 0.0F, stack, impl, 15728880, OverlayTexture.NO_OVERLAY);
                              stack.popPose();
                           } catch (Exception var21) {
                              IC2.LOGGER.catching(var21);
                              this.disabledTileEntities.add(longPos);
                              stack.popPose();
                           }
                        }
                     }
                  }
               }
            }

            impl.endBatch();
         }
      }
   }

   private static class TintGetter implements BlockAndTintGetter {
      Level level;

      public TintGetter(Level level) {
         this.level = level;
      }

      public BlockEntity getBlockEntity(BlockPos p_45570_) {
         return this.level.getBlockEntity(p_45570_);
      }

      public BlockState getBlockState(BlockPos p_45571_) {
         return this.level.getBlockState(p_45571_);
      }

      public FluidState getFluidState(BlockPos p_45569_) {
         return this.level.getFluidState(p_45569_);
      }

      public int getHeight() {
         return this.level.getHeight();
      }

      public int getMinBuildHeight() {
         return this.level.getMinBuildHeight();
      }

      public int getBrightness(LightLayer p_45518_, BlockPos p_45519_) {
         return 15;
      }

      public int getRawBrightness(BlockPos p_45525_, int p_45526_) {
         return 15;
      }

      public float getShade(Direction p_45522_, boolean p_45523_) {
         return this.level.getShade(p_45522_, p_45523_);
      }

      public int getLightEmission(BlockPos p_45572_) {
         return this.level.getLightEmission(p_45572_);
      }

      public LevelLightEngine getLightEngine() {
         return this.level.getLightEngine();
      }

      public int getBlockTint(BlockPos p_45520_, ColorResolver p_45521_) {
         return this.level.getBlockTint(p_45520_, p_45521_);
      }
   }
}
