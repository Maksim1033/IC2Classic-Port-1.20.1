package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.IEnergyNet;
import ic2.api.energy.TransferStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.items.armor.IArmorModule;
import ic2.api.items.readers.IEUReader;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2Item;
import ic2.core.item.inv.container.EUReaderContainer;
import ic2.core.item.inv.inventory.EUReaderInventory;
import ic2.core.item.wearable.base.IBaseArmorModule;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.text.DecimalFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EUReaderTool extends IC2Item implements ISimpleItemModel, IHasHeldGui, INetworkItemBufferEvent<NBTBuffer>, IEUReader, IBaseArmorModule {
   public static final DecimalFormat EU_READER_FORMAT = new DecimalFormat("###,###.##");

   public EUReaderTool() {
      super("eu_reader");
   }

   @Override
   public boolean isEUReader(ItemStack stack) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addSimpleToolTip(
         this.translate(
               "tooltip.item.ic2.eu_reader.active_mode", new Object[]{EUReaderTool.ReaderMode.byIndex(StackUtil.getNbtData(stack).getInt("mode")).getName()}
            )
            .withStyle(ChatFormatting.GRAY)
      );
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.eu_reader.switch_mode", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.eu_reader.scan", new Object[0]));
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating() && IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
         CompoundTag nbt = stack.getOrCreateTag();
         EUReaderTool.ReaderMode mode = EUReaderTool.ReaderMode.byIndex(nbt.getInt("mode") + 1);
         nbt.getAllKeys().clear();
         nbt.putInt("mode", mode.getIndex());
         playerIn.displayClientMessage(this.translate("tooltip.item.ic2.eu_reader.active_mode", new Object[]{mode.getName()}), false);
      }

      return InteractionResultHolder.success(stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (IC2.PLATFORM.isSimulating()) {
         if (IC2.KEYBOARD.isModeSwitchKeyDown(context.getPlayer())) {
            return InteractionResult.FAIL;
         }

         EUReaderTool.ReaderMode mode = EUReaderTool.ReaderMode.byIndex(StackUtil.getNbtData(stack).getInt("mode"));
         if (mode == EUReaderTool.ReaderMode.POWER_FLOW) {
            IEnergyNet.GridTile tile = EnergyNet.INSTANCE.getTiles(context.getLevel(), context.getClickedPos());
            if (tile == null) {
               return InteractionResult.FAIL;
            }

            TransferStats stat = EnergyNet.INSTANCE.getStats(tile.getMainTile());
            CompoundTag nbt = stack.getOrCreateTag();
            BlockPos pos = tile.getPos();
            long time = context.getLevel().getGameTime();
            if (nbt.getLong("pos") == pos.asLong() && nbt.getString("dim").equals(context.getLevel().dimension().location().toString())) {
               long delta = Math.max(1L, time - nbt.getLong("last_time"));
               double deltaSend = (double)(stat.getEnergyOut() - nbt.getLong("last_out")) / (double)delta;
               double deltaReceived = (double)(stat.getEnergyIn() - nbt.getLong("last_in")) / (double)delta;
               context.getPlayer()
                  .displayClientMessage(
                     this.translate(
                        "tooltip.item.ic2.eu_reader.measurement",
                        new Object[]{
                           EU_READER_FORMAT.format(deltaReceived),
                           EU_READER_FORMAT.format(deltaSend),
                           EU_READER_FORMAT.format(deltaReceived - deltaSend),
                           stat.getEnergyLossOut(),
                           stat.getEnergyLossIn(),
                           delta
                        }
                     ),
                     false
                  );
            } else {
               nbt.putLong("pos", context.getClickedPos().asLong());
               nbt.putString("dim", context.getLevel().dimension().location().toString());
               context.getPlayer().displayClientMessage(this.translate("tooltip.item.ic2.eu_reader.new_measurement"), true);
            }

            nbt.putLong("last_in", stat.getEnergyIn());
            nbt.putLong("last_out", stat.getEnergyOut());
            nbt.putLong("last_time", time);
            return InteractionResult.SUCCESS;
         }

         if (mode == EUReaderTool.ReaderMode.POWER_STATS) {
            IEnergyTile tilex = EnergyNet.INSTANCE.getTile(context.getLevel(), context.getClickedPos());
            if (tilex == null) {
               return InteractionResult.FAIL;
            }

            if (tilex instanceof IEnergySource source) {
               context.getPlayer()
                  .displayClientMessage(this.translate("tooltip.item.ic2.eu_reader.source_buffer", new Object[]{source.getProvidedEnergy()}), false);
            }

            if (tilex instanceof IEnergySink sink) {
               context.getPlayer()
                  .displayClientMessage(
                     this.translate("tooltip.item.ic2.eu_reader.sink_input", new Object[]{EnergyNet.INSTANCE.getPowerFromTier(sink.getSinkTier())}), false
                  );
            }

            if (tilex instanceof IEnergySource source) {
               context.getPlayer()
                  .displayClientMessage(this.translate("tooltip.item.ic2.eu_reader.source_output", new Object[]{source.getMaxEnergyOutput()}), false);
               if (tilex instanceof IMultiEnergySource && ((IMultiEnergySource)tilex).hasMultiplePackets()) {
                  context.getPlayer()
                     .displayClientMessage(
                        this.translate("tooltip.item.ic2.eu_reader.source_packets", new Object[]{((IMultiEnergySource)tilex).getPacketCount()}), false
                     );
               }
            }

            if (tilex instanceof IEnergyConductor conductor) {
               double loss = conductor.getConductionLoss();
               context.getPlayer()
                  .displayClientMessage(this.translate("tooltip.item.ic2.eu_reader.conductor_loss", new Object[]{loss, (int)Math.ceil(1.0 / loss)}), false);
               context.getPlayer()
                  .displayClientMessage(
                     this.translate("tooltip.item.ic2.eu_reader.conductor_break", new Object[]{conductor.getConductorBreakdownEnergy() - 1}), false
                  );
            }

            return InteractionResult.SUCCESS;
         }

         if (mode == EUReaderTool.ReaderMode.REAL_TIME_STAT) {
            IEnergyTile tilexx = EnergyNet.INSTANCE.getTile(context.getLevel(), context.getClickedPos());
            if (tilexx == null) {
               return InteractionResult.FAIL;
            }

            CompoundTag nbt = stack.getOrCreateTag();
            nbt.putLong("track_pos", context.getClickedPos().asLong());
            IC2.PLATFORM
               .launchGui(context.getPlayer(), context.getHand(), context.getClickedFace(), this.getInventory(context.getPlayer(), context.getHand(), stack));
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
      if (IC2.PLATFORM.isSimulating() && entityIn instanceof Player player) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         if (player.containerMenu instanceof EUReaderContainer
            && nbt.contains("track_pos")
            && ((EUReaderContainer)player.containerMenu).getHolder().getID() == this.getGuiId(stack)) {
            EUReaderInventory.processEnergyNet(stack, player, BlockPos.of(nbt.getLong("track_pos")));
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("eu_reader");
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new EUReaderInventory(player, this, stack, null).load(stack);
   }

   public void onDataBufferReceived(ItemStack stack, Player player, String id, NBTBuffer buffer, Dist targetSide) {
      if (player.containerMenu instanceof EUReaderContainer && id.equals("EU_INFO")) {
         ((EUReaderContainer)player.containerMenu).getHolder().accept(buffer);
      }
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return IArmorModule.ModuleType.HUD;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.addAddModifier(armor, IArmorModule.ArmorMod.EU_READER, 1);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.EU_READER, 1);
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }

   public static enum ReaderMode implements CollectionUtils.IIndexEnum {
      POWER_FLOW(0, "power_flow"),
      POWER_STATS(1, "power_stats"),
      REAL_TIME_STAT(2, "real_time_stats");

      public static final EUReaderTool.ReaderMode[] MODES = CollectionUtils.createSortedArray(values());
      final int index;
      final String name;

      private ReaderMode(int index, String name) {
         this.index = index;
         this.name = name;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      public Component getName() {
         return Component.translatable("tooltip.item.ic2.eu_reader." + this.name);
      }

      public static EUReaderTool.ReaderMode byIndex(int index) {
         return MODES[index % MODES.length];
      }
   }
}
