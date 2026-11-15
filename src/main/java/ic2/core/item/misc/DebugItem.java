package ic2.core.item.misc;

import ic2.api.tiles.tubes.ITube;
import ic2.core.IC2;
import ic2.core.block.base.features.IProfileListener;
import ic2.core.block.personal.tile.TradeOMatTileEntity;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.item.base.IC2Item;
import ic2.core.networking.NetworkManager;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.DebugPacket;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DebugItem extends IC2Item implements ISimpleItemModel {
   public DebugItem(String itemName) {
      super(itemName);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("no_use");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating() && !playerIn.isShiftKeyDown()) {
         CompoundTag nbt = stack.getOrCreateTag();
         boolean value = !nbt.getBoolean("active");
         nbt.putBoolean("active", value);
         playerIn.displayClientMessage(this.translate("tooltip.item.ic2.debug.tile." + (value ? "enabled" : "disabled")), false);
      }

      return InteractionResultHolder.success(stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
         BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
         if (tile instanceof ITube) {
            TubeNet.INSTANCE.printLag((ITube)tile, context.getPlayer());
            return InteractionResult.SUCCESS;
         }

         if (tile instanceof TradeOMatTileEntity) {
            if (IC2.PLATFORM.isSimulating()) {
               TradeOMatTileEntity trade = (TradeOMatTileEntity)tile;
               CompoundTag data = stack.getOrCreateTag();
               if (data.contains("trades")) {
                  trade.getTrade().load(data.getCompound("trades"));
                  data.remove("trades");
                  context.getPlayer().displayClientMessage(this.string("Inserted Data"), false);
               } else {
                  data.put("trades", trade.getTrade().save(new CompoundTag()));
                  context.getPlayer().displayClientMessage(this.string("Copied Data"), false);
               }
            }

            return InteractionResult.SUCCESS;
         }

         if (tile instanceof IProfileListener) {
            context.getPlayer().displayClientMessage(((IProfileListener)tile).showResults(), false);
            return InteractionResult.SUCCESS;
         }
      }

      return super.onItemUseFirst(stack, context);
   }

   public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
      if (!IC2.PLATFORM.isRendering() && entityIn instanceof Player && worldIn.getGameTime() % 5L != 0L) {
         if (stack.getOrCreateTag().getBoolean("active")) {
            Vec3 start = entityIn.getEyePosition(1.0F);
            Vec3 end = start.add(entityIn.getViewVector(1.0F).multiply(10.0, 10.0, 10.0));
            BlockHitResult result = worldIn.clip(new ClipContext(start, end, Block.OUTLINE, Fluid.NONE, entityIn));
            BlockEntity tile = worldIn.getBlockEntity(result.getBlockPos());
            long[] data = new long[]{
               NetworkManager.countNetworkBytes(tile, true, false),
               NetworkManager.countNetworkBytes(tile, false, false),
               NetworkManager.countNetworkBytes(tile, false, true)
            };
            PacketManager.INSTANCE
               .sendToPlayer(
                  new DebugPacket(worldIn.getBlockState(result.getBlockPos()), tile == null ? null : tile.saveWithFullMetadata(), data), (Player)entityIn
               );
         }
      }
   }
}
