package ic2.core.block.misc.tiles;

import ic2.api.blocks.PainterHelper;
import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.misc.tiles.container.PlayerDetectorContainer;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.ColorUtils;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public class PlayerDetectorTileEntity
   extends BaseTileEntity
   implements ITickListener,
   IRedstoneListener,
   IRedstoneProvider,
   ITileGui,
   IPersonalTile,
   ITileActivityProvider,
   IAreaOfEffect,
   ICamouflagable,
   IClickable {
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int xRadius = 1;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int yRadius = 2;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int zRadius = 1;
   @NetworkInfo
   public UUID owner;
   @NetworkInfo
   public int mode;
   @NetworkInfo
   public byte foamed;
   @NetworkInfo
   CamouflageStorage storage = new CamouflageStorage(CamouflageShape.FULL_CUBE);
   public int networkId = -1;
   public Object2LongMap<String> trackerId = new Object2LongLinkedOpenHashMap();

   public PlayerDetectorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"xRadius", "yRadius", "zRadius"});
      this.addNetworkFields(new String[]{"mode", "owner", "foamed", "storage"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PLAYER_DETECTOR;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.xRadius = compound.getInt("xRadius");
      this.yRadius = compound.getInt("yRadius");
      this.zRadius = compound.getInt("zRadius");
      this.owner = compound.hasUUID("owner") ? compound.getUUID("owner") : null;
      this.mode = compound.getInt("mode");
      this.storage.load(compound.getCompound("camouflage"));
      this.foamed = compound.getByte("foamed");

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("tracker", 10), CompoundTag.class)) {
         this.trackerId.put(data.getString("name"), data.getLong("time"));
      }

      this.changeFoam(this.foamed, true);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "xRadius", (byte)this.xRadius, 0);
      NBTUtils.putByte(compound, "yRadius", (byte)this.yRadius, 0);
      NBTUtils.putByte(compound, "zRadius", (byte)this.zRadius, 0);
      if (this.owner != null) {
         compound.putUUID("owner", this.owner);
      }

      compound.putByte("mode", (byte)this.mode);
      NBTUtils.putByte(compound, "foamed", this.foamed, 0);
      NBTUtils.putIf(compound, "camouflage", this.storage.save(new CompoundTag()), this.foamed > 0);
      ListTag list = new ListTag();
      ObjectIterator var3 = this.trackerId.object2LongEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<String> entry = (Entry<String>)var3.next();
         CompoundTag data = new CompoundTag();
         data.putString("name", (String)entry.getKey());
         data.putLong("time", entry.getLongValue());
         list.add(data);
      }

      compound.put("tracked", list);
   }

   @Override
   public CamouflageStorage getStorage() {
      return this.storage;
   }

   @Override
   public boolean applyTexture(Direction dir, RetextureEvent.TextureContainer container) {
      if (this.foamed == 2 && this.storage.canApply(dir, container)) {
         this.storage.setTexture(DirectionList.ofFacing(dir), container);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   public boolean setColor(Direction dir, DyeColor color) {
      if (this.foamed == 2 && this.isSimulating() && this.storage.canApply(dir == null ? DirectionList.ALL : DirectionList.ofFacing(dir), color)) {
         this.storage.setColor(dir == null ? DirectionList.ALL : DirectionList.ofFacings(dir), color);
         this.updateTileField("storage");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isSideEnabled(Direction dir) {
      return this.foamed > 1;
   }

   @Override
   public boolean removeCamouflage() {
      return this.changeFoam((byte)0, false);
   }

   @Override
   public ItemLike getCamouflageDrop() {
      DyeColor color = this.storage.isSingleColor();
      return (ItemLike)(color == null ? Items.AIR : ColorMaps.CFOAM_BLOCKS.getBlock(color));
   }

   @OnlyIn(Dist.CLIENT)
   public ModelData getModelData() {
      return this.foamed != 2
         ? ModelData.EMPTY
         : ModelData.builder()
            .with(
               CamouflageProperty.INSTANCE,
               (Function<RenderType, CamouflageShape.QuadResults>)T -> this.storage.getQuads(this.getBlockState(), this.getLevel(), this.getBlockPos(), T)
            )
            .build();
   }

   @Override
   public void setOwner(UUID owner) {
      if (this.owner == null && owner != null) {
         this.owner = owner;
      }
   }

   @Override
   public UUID getOwner() {
      return this.owner;
   }

   @Override
   public boolean canDoAction(UUID user, Action action, boolean ignorePublic) {
      if (this.owner == null) {
         this.setOwner(user);
         return true;
      } else if (this.owner.equals(user) || !ignorePublic && this.mode == 0 || IC2.PLATFORM.isOp(user)) {
         return true;
      } else if (this.mode == 2) {
         return false;
      } else {
         Friend friend = FriendManager.getFriends().getFriend(this.owner, user);
         return friend != null && friend.canApplyAction(action);
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem() == IC2Blocks.CFOAM_WET.asItem()) {
         if (this.foamed == 0) {
            this.changeFoam((byte)1, false);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }
      } else {
         if (stack.is(ItemTags.SAND) && this.foamed == 1) {
            this.changeFoam((byte)2, false);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return true;
         }

         if (this.foamed == 1) {
            BlockState dustState = Block.byItem(stack.getItem()).defaultBlockState();
            if (PainterHelper.CONCRETE_DUST.contains(dustState.getBlock())) {
               this.storage.setColor(DirectionList.ALL, PainterHelper.INSTANCE.getColor(dustState));
               this.changeFoam((byte)2, false);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.canDoAction(player.getUUID(), Action.PLAYER_DETECTOR_SETTINGS, false)
         && (this.foamed < 2 || player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty());
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && this.canDoAction(player.getUUID(), Action.PLAYER_DETECTOR_SETTINGS, false);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new PlayerDetectorContainer(this, player, windowID);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("storage") || fields.contains("foamed")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 0) {
         this.xRadius = Mth.clamp(value, 0, 64);
         this.updateTileField("xRadius");
      } else if (key == 1) {
         this.yRadius = Mth.clamp(value, 0, 64);
         this.updateTileField("yRadius");
      } else if (key == 2) {
         this.zRadius = Mth.clamp(value, 0, 64);
         this.updateTileField("zRadius");
      } else if (key == 3) {
         if (entity.getUUID().equals(this.owner)) {
            this.mode = (this.mode + 1) % 3;
            this.updateTileField("mode");
         } else {
            entity.displayClientMessage(this.translate("gui.ic2.personal.perm"), false);
         }
      }

      super.onClientDataReceived(entity, key, value);
   }

   @Override
   public void onTick() {
      if (this.clock(5) && this.setActive(this.isPlayerNearby())) {
         this.notifyChanges(true, DirectionList.ALL, this.getBlockPos().relative(this.getFacing().getOpposite()));
      }
   }

   @Override
   public int getAreaOfEffectColor() {
      return ColorUtils.RED & -2130706433;
   }

   @Override
   public void setVisualizationId(int id) {
      this.networkId = id;
   }

   @Override
   public int getVisualizationId() {
      return this.networkId;
   }

   @Override
   public AABB getAreaOfEffect() {
      Direction dir = this.getFacing();
      return new AABB(this.getBlockPos())
         .inflate(
            (double)this.xRadius * (1.0 - (double)Math.abs(dir.getStepX())),
            (double)this.yRadius * (1.0 - (double)Math.abs(dir.getStepY())),
            (double)this.zRadius * (1.0 - (double)Math.abs(dir.getStepZ()))
         )
         .expandTowards((double)(dir.getStepX() * this.xRadius), (double)(dir.getStepY() * this.yRadius), (double)(dir.getStepZ() * this.zRadius));
   }

   public boolean isPlayerNearby() {
      List<? extends Player> players = this.getLevel().players();
      AABB box = this.getAreaOfEffect();
      Vec3 base = Vec3.atBottomCenterOf(this.getBlockPos());
      double closest = 1.7976931348623157E308;
      Player closest_player = null;
      int i = 0;

      for (int m = players.size(); i < m; i++) {
         Player player = players.get(i);
         if (box.contains(player.getX(), player.getY(), player.getZ()) && this.canDoAction(player.getUUID(), Action.PLAYER_DETECTOR_DETECTION, false)) {
            double dist = player.distanceToSqr(base);
            if (dist < closest) {
               closest = dist;
               closest_player = player;
            }

            this.trackerId.put(player.getGameProfile().getName(), this.getLevel().getGameTime());
         }
      }

      return closest_player != null;
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return this.isActive() ? 15 : 0;
   }

   @Override
   public boolean allowWeakSignal(Direction dir) {
      return false;
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.tickFoamed();
      }
   }

   public boolean changeFoam(byte level, boolean duringLoad) {
      if (this.foamed == level) {
         return false;
      } else {
         if (this.isSimulating()) {
            if ((this.foamed = level) == 1) {
               this.storage.setColor(DirectionList.ALL, DyeColor.LIGHT_GRAY);
               if (!duringLoad) {
                  this.updateTileField("storage");
               }

               this.tickFoamed();
            }

            if (!duringLoad) {
               this.updateTileField("foamed");
               this.onStateChanged();
            }
         }

         return true;
      }
   }

   private void tickFoamed() {
      if (this.foamed == 1) {
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), world -> {
            if (!this.isRemoved() && this.foamed == 1) {
               if (!world.dimension().equals(Level.NETHER)) {
                  if (world.getMaxLocalRawBrightness(this.worldPosition) * 6 > world.random.nextInt(1000)) {
                     this.changeFoam((byte)2, false);
                     return 0;
                  } else {
                     return 500;
                  }
               } else if (world.random.nextInt(5) == 0) {
                  this.changeFoam((byte)2, false);
                  return 0;
               } else {
                  return 20;
               }
            } else {
               return 0;
            }
         });
      }
   }
}
