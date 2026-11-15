package ic2.core.item.tool.electric;

import ic2.api.events.RetextureEvent;
import ic2.api.items.IFoamOverrider;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.renders.models.ObscuratorModel;
import ic2.core.networking.buffers.data.ObscuratorBuffer;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;

public class ObscuratorTool
   extends IC2ElectricItem
   implements INetworkItemBufferEvent<ObscuratorBuffer>,
   ICustomItemModel,
   IItemColorListener,
   IElectricEnchantable,
   IFoamOverrider {
   public static final int COPY_COST = 10000;
   public static final int DRAW_COST = 1500;

   public ObscuratorTool() {
      super("obscurator");
      this.capacity = 100000;
      this.tier = 2;
      this.transferLimit = 250;
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return 100;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      RetextureEvent.TextureContainer container = getContainer(stack);
      if (container != null) {
         int layer = this.getLayer(stack);
         helper.addStatTooltip("tooltip.item.ic2.obscurator.block", container.getState().getBlock().getName());
         helper.addStatTooltip("tooltip.item.ic2.obscurator.side", DirectionList.getName(container.getSide()));
         helper.addStatTooltip("tooltip.item.ic2.obscurator.layers", container.getRotations().length);
         helper.addStatTooltip("tooltip.item.ic2.obscurator.layer", layer);
         helper.addStatTooltip("tooltip.item.ic2.obscurator.rotation", container.getRotations()[layer].getRotation());
      }

      helper.addSimpleToolTip(this.translate("tooltip.item.ic2.painter.range", new Object[]{StackUtil.getNbtData(stack).getByte("range") * 2 + 1}));
      helper.addKeybindingTooltip(
         this.buildKeyDescription(KeyHelper.SNEAK_KEY, KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.obscurator.change.copy", new Object[0])
      );
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.obscurator.change.paste", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.obscurator.change.layer", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.obscurator.change.rotation", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.obscurator.change.paste_all_sides", new Object[0]));
      helper.addKeybindingTooltip(
         this.buildKeyDescription(KeyHelper.SNEAK_KEY, KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.obscurator.change.range", new Object[0])
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      RetextureEvent.TextureContainer container = getContainer(stack);
      return container != null && container.getColors().length != 0 ? container.getColors()[tintIndex % container.getColors().length] : -1;
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return 0;
   }

   @Override
   public List<ItemStack> getCustomTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getModel(ItemStack stack) {
      return new ObscuratorModel(IC2Textures.getMappedEntriesItemIC2("tools").get("obscurator"));
   }

   public int increaseLayer(ItemStack stack, int max) {
      CompoundTag nbt = stack.getOrCreateTag();
      int result = (nbt.getInt("layer") + 1) % max;
      nbt.putByte("layer", (byte)result);
      return result;
   }

   public int getLayer(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("layer");
   }

   public void setLayer(ItemStack stack, int layer) {
      stack.getOrCreateTag().putByte("layer", (byte)layer);
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      if (!worldIn.isClientSide) {
         ItemStack stack = playerIn.getItemInHand(handIn);
         if (IC2.KEYBOARD.isSneakKeyDown(playerIn)) {
            CompoundTag nbt = stack.getOrCreateTag();
            byte range = nbt.getByte("range");
            if (++range > 2) {
               range = 0;
            }

            nbt.putByte("range", range);
            playerIn.displayClientMessage(this.translate("tooltip.item.ic2.obscurator.range", new Object[]{range * 2 + 1}), false);
         } else if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
            if (!stack.getOrCreateTag().contains("block")) {
               return InteractionResultHolder.fail(playerIn.getItemInHand(handIn));
            }

            RetextureEvent.TextureContainer container = getContainer(stack);
            if (playerIn.isShiftKeyDown()) {
               playerIn.displayClientMessage(
                  this.translate("tooltip.item.ic2.obscurator.layer", new Object[]{this.increaseLayer(stack, container.getRotations().length)}), false
               );
            } else {
               RetextureEvent.Rotation[] rotations = container.getRotations();
               int layer = this.getLayer(stack);
               if (layer >= rotations.length) {
                  layer = rotations.length - 1;
                  this.setLayer(stack, layer);
               }

               rotations[layer] = rotations[layer].getNext();
               stack.getOrCreateTag().put("block", container.save());
               playerIn.displayClientMessage(this.translate("tooltip.item.ic2.obscurator.rotation", new Object[]{rotations[layer].getRotation()}), false);
            }
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null && !IC2.KEYBOARD.isModeSwitchKeyDown(player)) {
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         if (player.isShiftKeyDown() && IC2.PLATFORM.isRendering()) {
            if (world.isEmptyBlock(pos)) {
               return InteractionResult.FAIL;
            } else if (!ElectricItem.MANAGER.canUse(context.getItemInHand(), 10000)) {
               return InteractionResult.FAIL;
            } else {
               BlockState state = world.getBlockState(pos);
               if (state.getRenderShape() != RenderShape.MODEL) {
                  return InteractionResult.FAIL;
               } else if (!this.isModelValid(state, context.getClickedFace(), pos)) {
                  return InteractionResult.FAIL;
               } else {
                  int[] colors = this.getColors(world, pos, state, context.getClickedFace());
                  IC2.NETWORKING
                     .get(false)
                     .sendClientItemBuffer(
                        context.getItemInHand(),
                        "",
                        new ObscuratorBuffer(state, context.getClickedFace(), colors, context.getHand() != InteractionHand.OFF_HAND)
                     );
                  return InteractionResult.SUCCESS;
               }
            }
         } else if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
         } else {
            RetextureEvent.TextureContainer container = getContainer(context.getItemInHand());
            if (container == null) {
               return InteractionResult.PASS;
            } else if (IC2.PLATFORM.isRendering()) {
               return InteractionResult.SUCCESS;
            } else {
               Direction facing = context.getClickedFace();
               Box box = Box.withRange(pos, 0).expandSide(facing.getAxis(), StackUtil.getNbtData(stack).getByte("range"));
               DirectionList sides = IC2.KEYBOARD.isAltKeyDown(player) ? DirectionList.ALL : DirectionList.ofFacing(facing);
               InteractionResult result = InteractionResult.FAIL;

               for (BlockPos position : box) {
                  for (Direction side : sides) {
                     if (!ElectricItem.MANAGER.canUse(stack, 1500)) {
                        return result;
                     }

                     RetextureEvent event = new RetextureEvent(world, position, side, player, container);
                     MinecraftForge.EVENT_BUS.post(event);
                     if (event.isApplied()) {
                        player.awardStat(IC2Stats.TEXTURES_STOLEN);
                        if (!player.isCreative()) {
                           ElectricItem.MANAGER.use(stack, 1500, player);
                        }

                        result = InteractionResult.SUCCESS;
                     }
                  }
               }

               return result;
            }
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public int[] getColors(Level world, BlockPos position, BlockState state, Direction side) {
      List<BakedQuad> quads = Minecraft.getInstance()
         .getBlockRenderer()
         .getBlockModelShaper()
         .getBlockModel(state)
         .getQuads(state, side, RandomSource.create(state.getSeed(position)), ModelData.EMPTY, null);
      int[] colors = new int[quads.size()];
      BlockColors color = Minecraft.getInstance().getBlockColors();

      for (int i = 0; i < quads.size(); i++) {
         BakedQuad quad = quads.get(i);
         colors[i] = quad.isTinted() ? color.getColor(state, world, position, quad.getTintIndex()) : -1;
      }

      return colors;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isModelValid(BlockState state, Direction side, BlockPos position) {
      BlockModelShaper shape = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
      BakedModel model = shape.getBlockModel(state);
      if (!model.isCustomRenderer() && model != shape.getModelManager().getMissingModel()) {
         List<BakedQuad> quads = model.getQuads(state, side, RandomSource.create(state.getSeed(position)), ModelData.EMPTY, null);
         return !quads.isEmpty() && quads.size() <= 10;
      } else {
         return false;
      }
   }

   public void onDataBufferReceived(ItemStack stack, Player player, String id, ObscuratorBuffer buffer, Dist targetSide) {
      if (!targetSide.isClient()) {
         ItemStack target = buffer.getItem(player);
         if (!stack.isEmpty() && ItemStack.matches(stack, target)) {
            CompoundTag nbt = target.getOrCreateTag();
            if (nbt.contains("block")) {
               RetextureEvent.TextureContainer container = buffer.createContainer();
               if (!container.equals(new RetextureEvent.TextureContainer(nbt.getCompound("block")))) {
                  nbt.put("block", container.save());
                  nbt.putByte("layer", (byte)0);
               }
            } else {
               nbt.put("block", buffer.createContainer().save());
               if (!player.isCreative()) {
                  ElectricItem.MANAGER.use(target, 10000, player);
               }
            }
         }
      }
   }

   public static RetextureEvent.TextureContainer getContainer(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return nbt.contains("block") ? new RetextureEvent.TextureContainer(nbt.getCompound("block")) : null;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING
         ? InteractionResult.FAIL
         : (enchantment == Enchantments.UNBREAKING ? InteractionResult.SUCCESS : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }
}
