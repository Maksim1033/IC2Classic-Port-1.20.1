package ic2.api.energy.impl;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LinkedSink implements IEnergyAcceptor {
   Level world;
   BlockPos pos;
   int directions;

   public LinkedSink(Level world, BlockPos pos, Iterable<Direction> directions) {
      this.world = world;
      this.pos = pos;

      for (Direction dir : directions) {
         this.directions = this.directions | 1 << dir.get3DDataValue();
      }
   }

   public LinkedSink(Level world, BlockPos pos, Direction... directions) {
      this.world = world;
      this.pos = pos;

      for (Direction direction : directions) {
         this.directions = this.directions | 1 << direction.get3DDataValue();
      }
   }

   public LinkedSink(Level world, BlockPos pos, int directions) {
      this.world = world;
      this.pos = pos;
      this.directions = directions;
   }

   public LinkedSink(Level world, BlockPos pos) {
      this.world = world;
      this.pos = pos;
      this.directions = 63;
   }

   public LinkedSink(BlockEntity tile, int directions) {
      this.world = tile.getLevel();
      this.pos = tile.getBlockPos();
      this.directions = directions;
   }

   public LinkedSink(BlockEntity tile) {
      this.world = tile.getLevel();
      this.pos = tile.getBlockPos();
      this.directions = 63;
   }

   @Override
   public Level getWorldObj() {
      return this.world;
   }

   @Override
   public BlockPos getPosition() {
      return this.pos;
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return (this.directions & 1 << side.get3DDataValue()) != 0;
   }
}
