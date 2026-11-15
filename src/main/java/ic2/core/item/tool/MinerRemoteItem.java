package ic2.core.item.tool;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.item.base.IC2Item;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.MultiHighlightPacket;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MinerRemoteItem extends IC2Item implements ISimpleItemModel {
   public MinerRemoteItem() {
      super("miner_remote");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("miner_remote");
   }

   public static BlockPos getPos(ItemStack stack) {
      CompoundTag tag = StackUtil.getNbtData(stack);
      return tag.contains("target_pos") ? BlockPos.of(tag.getLong("target_pos")) : BlockPos.ZERO;
   }

   public static void setPos(ItemStack stack, BlockPos pos) {
      stack.getOrCreateTag().putLong("target_pos", pos.asLong());
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (!playerIn.isShiftKeyDown()) {
         this.markMiners(worldIn, playerIn.blockPosition(), playerIn);
         return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public static void requestRocketMinerBack(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("rocketminer")
            .then(
               Commands.literal("home")
                  .then(
                     Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(
                           T -> {
                              if (!(
                                 ((CommandSourceStack)T.getSource())
                                    .getLevel()
                                    .getBlockEntity(((Coordinates)T.getArgument("pos", Coordinates.class)).getBlockPos((CommandSourceStack)T.getSource())) instanceof RocketMinerTileEntity rocket
                              )) {
                                 ((CommandSourceStack)T.getSource())
                                    .sendFailure(Component.literal("Couldn't find Rocket Miner at " + T.getArgument("pos", BlockPos.class)));
                                 return 0;
                              } else {
                                 if (rocket.owner != null
                                    && !rocket.owner.equals(((CommandSourceStack)T.getSource()).getPlayerOrException().getUUID())
                                    && !IC2.PLATFORM.isOp(((CommandSourceStack)T.getSource()).getPlayerOrException().getUUID())) {
                                    ((CommandSourceStack)T.getSource()).sendSystemMessage(Component.literal("You don't own the miner"));
                                 } else {
                                    rocket.sendHome();
                                    ((CommandSourceStack)T.getSource()).sendSystemMessage(Component.literal("Requested Miner home"));
                                 }

                                 return 0;
                              }
                           }
                        )
                  )
            )
      );
   }

   private void markMiners(Level world, BlockPos pos, Player player) {
      LongList list = new LongArrayList();
      AABBUtil.getBlockEntities(
         world,
         T -> {
            if (T instanceof MinerTileEntity miner) {
               BlockPos relative = T.getBlockPos().subtract(pos);
               int dist = (int)Math.sqrt(T.getBlockPos().distSqr(pos));
               boolean rocket = false;
               if (T instanceof RocketMinerTileEntity rockMiner) {
                  if (rockMiner.owner != null && !player.getUUID().equals(rockMiner.owner) && !IC2.PLATFORM.isOp(player.getUUID())) {
                     return;
                  }

                  rocket = true;
               }

               Object[] args = new Object[rocket ? 5 : 4];
               args[0] = this.pos(T.getBlockPos()).withStyle(ChatFormatting.AQUA);
               args[1] = this.pos(relative).withStyle(ChatFormatting.GREEN);
               args[2] = ChatFormatting.RED + Integer.toString(dist);
               args[rocket ? 4 : 3] = Formatters.EU_FORMAT.format((double)miner.getPowerLeft());
               if (rocket) {
                  args[0] = this.pos(T.getBlockPos())
                     .withStyle(
                        Style.EMPTY
                           .withColor(ChatFormatting.AQUA)
                           .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.literal("Click to request the miner back home")))
                           .withClickEvent(
                              new ClickEvent(
                                 net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND,
                                 "/rocketminer home " + T.getBlockPos().getX() + " " + T.getBlockPos().getY() + " " + T.getBlockPos().getZ()
                              )
                           )
                     );
                  args[3] = ((RocketMinerTileEntity)T).state.getState().withStyle(ChatFormatting.GOLD);
               }

               player.displayClientMessage(this.translate("tooltip.item.ic2.mining_remote.location." + (rocket ? "rocket_miner" : "miner"), args), false);
               if (dist < 100) {
                  list.add(T.getBlockPos().asLong());
               }
            }
         }
      );
      if (!list.isEmpty() && IC2.PLATFORM.isSimulating()) {
         PacketManager.INSTANCE.sendToPlayer(new MultiHighlightPacket(list, 1200, ColorUtils.GREEN & 16777215 | -2147483648, false), player);
      }

      if (world instanceof ServerLevel server) {
         server.getEntities()
            .get(
               IC2Entities.ROCKET_MINER,
               AbortableIterationConsumer.forConsumer(T -> {
                  BlockPos relative = T.blockPosition().subtract(pos);
                  Object[] args = new Object[]{
                     this.pos(T.blockPosition()).withStyle(ChatFormatting.AQUA),
                     this.pos(relative).withStyle(ChatFormatting.GREEN),
                     ChatFormatting.RED + Integer.toString((int)Math.sqrt(T.blockPosition().distSqr(pos))),
                     this.translate("tooltip.item.ic2.mining_remote.location.rocket_miner.flying").withStyle(ChatFormatting.GOLD)
                  };
                  player.displayClientMessage(this.translate("tooltip.item.ic2.mining_remote.location.rocket_miner", args), false);
               })
            );
      }
   }

   private boolean hasRocketMinerNearby(Level level, BlockPos pos) {
      boolean[] result = new boolean[1];
      AABBUtil.getBlockEntities(level, T -> {
         if (T instanceof RocketMinerTileEntity) {
            result[0] |= Box.fromPos(pos, true).expand(DirectionList.HORIZONTAL, 20).expand(DirectionList.VERTICAL, 200).intersectsWith(T.getBlockPos());
         }
      });
      return result[0];
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (context.isSecondaryUseActive()) {
         if (this.hasRocketMinerNearby(context.getLevel(), context.getClickedPos())) {
            if (IC2.PLATFORM.isSimulating()) {
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.mining_remote.location.to_close"), false);
            }

            return InteractionResult.FAIL;
         } else {
            setPos(stack, context.getClickedPos());
            if (IC2.PLATFORM.isSimulating()) {
               context.getPlayer()
                  .displayClientMessage(
                     this.translate("tooltip.item.ic2.mining_remote.location.refuel_set", new Object[]{this.pos(context.getClickedPos())}), false
                  );
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }
}
