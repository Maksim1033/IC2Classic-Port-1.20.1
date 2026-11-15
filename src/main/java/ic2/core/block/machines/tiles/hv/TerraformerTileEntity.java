package ic2.core.block.machines.tiles.hv;

import ic2.api.items.ITerraformerBP;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ITerraformer;
import ic2.core.IC2;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.logic.terraformer.ITerraformerLogic;
import ic2.core.block.machines.logic.terraformer.InOutTerraformerLogic;
import ic2.core.block.machines.logic.terraformer.RandomTerraformerLogic;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.BiomePacket;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TerraformerTileEntity extends BaseElectricTileEntity implements ITickListener, ITerraformer, IClickable, ISpecialWrenchable, ITileActivityProvider {
   ITerraformerLogic logic;
   int inactiveTime = 0;
   IAudioSource source;
   @NetworkInfo
   public boolean tfbp = false;

   public TerraformerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 1, 512, 100000);
      this.getInventoryHandler().setActive(false);
      this.addNetworkFields(new String[]{"tfbp"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TERRAFORMER;
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      CompoundTag data = compound.getCompound("logic");
      this.logic = this.createLogic(data.getInt("type"));
      if (this.logic != null) {
         this.logic.load(data);
      }

      this.tfbp = !((ItemStack)this.inventory.get(0)).isEmpty();
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (this.logic != null) {
         compound.put("logic", this.logic.save(new CompoundTag()));
      }
   }

   private ITerraformerLogic createLogic(int type) {
      switch (type) {
         case 1:
            return new InOutTerraformerLogic();
         case 2:
            return new RandomTerraformerLogic();
         default:
            return null;
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem() instanceof ITerraformerBP blueprint) {
         if (!blueprint.canInsert(stack, player, this.getLevel(), this.worldPosition)) {
            return false;
         } else if (this.isRendering()) {
            return true;
         } else {
            if (!((ItemStack)this.inventory.get(0)).isEmpty()) {
               this.ejectBluePrint();
            }

            blueprint.onInsert(stack, player, this.getLevel(), this.worldPosition);
            this.logic = this.createLogic(blueprint.isRandomized(stack) ? 2 : 1);
            this.inventory.set(0, StackUtil.copyWithSize(stack, 1));
            this.tfbp = true;
            this.updateTileField("tfbp");
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public AABB hasSpecialAction(Direction side, Vec3 hit, Player player) {
      return !this.tfbp ? null : new AABB(BlockPos.ZERO);
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      if (!((ItemStack)this.inventory.get(0)).isEmpty()) {
         this.ejectBluePrint();
         return true;
      } else {
         return false;
      }
   }

   public void ejectBluePrint() {
      if (this.isSimulating()) {
         Block.popResource(this.getLevel(), this.worldPosition, (ItemStack)this.inventory.get(0));
      }

      this.inventory.set(0, ItemStack.EMPTY);
      this.logic = null;
      this.tfbp = false;
      this.updateTileField("tfbp");
   }

   @Override
   public void onTick() {
      boolean active = false;
      ITerraformerBP terra = this.getBluePrint();
      if (terra != null && this.logic != null && this.logic.canRun()) {
         int energy = terra.getEnergyUsage((ItemStack)this.inventory.get(0));
         if (this.hasEnergy(energy)) {
            active = true;
            this.useEnergy(this.logic.run(this, (ItemStack)this.inventory.get(0), terra) ? energy : energy / 10);
         }
      }

      if (active) {
         this.setActive(true);
         this.inactiveTime = 0;
      } else if (this.isActive() && this.inactiveTime++ > 60) {
         this.setActive(false);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.source = null;
      IC2.AUDIO.removeSource(this);
      super.onUnloaded(chunk);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isActive")) {
         if (this.source == null || !this.source.isValid()) {
            this.source = IC2.AUDIO.createSource(this, IC2Sounds.TERRA_FORMER_PROCESSING);
            if (this.source == null) {
               return;
            }
         }

         this.source.playStop(this.isActive());
      }
   }

   @Override
   public boolean setBiome(Level world, BlockPos pos, ResourceLocation biomeKey) {
      ChunkAccess chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
      if (chunk == null) {
         return false;
      } else {
         Optional<Holder.Reference<Biome>> biomeRef = IC2.PLATFORM.getBiomeRegistry(world).getHolder(ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, biomeKey));
         if (biomeRef.isEmpty()) {
            return false;
         } else {
            Holder<Biome> biome = biomeRef.get();
            boolean changed = false;
            int i = 0;

            for (int m = world.getSectionsCount(); i < m; i++) {
               LevelChunkSection section = chunk.getSection(i);
               PalettedContainerRO<Holder<Biome>> palette = section.getBiomes();
               if (palette instanceof PalettedContainer) {
                  PalettedContainer<Holder<Biome>> chunkPalette = (PalettedContainer<Holder<Biome>>)palette;

                  for (int y = 0; y < 4; y++) {
                     if (((Holder)chunkPalette.getAndSet(pos.getX() & 3, y, pos.getZ() & 3, biome)).get() != biome.get()) {
                        changed = true;
                     }
                  }
               }
            }

            if (changed) {
               PacketManager.INSTANCE.sendToAllChunkWatchers((LevelChunk)chunk, new BiomePacket(pos, biomeKey));
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public ITerraformerBP getBluePrint() {
      ItemStack stack = (ItemStack)this.inventory.get(0);
      return stack.getItem() instanceof ITerraformerBP ? (ITerraformerBP)stack.getItem() : null;
   }
}
