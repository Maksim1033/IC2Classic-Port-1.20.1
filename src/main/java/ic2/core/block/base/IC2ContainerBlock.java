package ic2.core.block.base;

import ic2.api.blocks.IAdvancedComparable;
import ic2.api.blocks.IWrenchable;
import ic2.core.IC2;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.drops.IRarityProvider;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ICollideable;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.IParticleSpawner;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.features.personal.IPersonalWrenchable;
import ic2.core.block.base.features.redstone.IComparatorProvider;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.base.IHasGui;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.helper.ITooltipProvider;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public abstract class IC2ContainerBlock
   extends BaseEntityBlock
   implements IAdvancedComparable,
   IAutoCreator,
   IWrenchable,
   IRarityProvider,
   IRegistryProvider,
   ILangHelper,
   IToolProvider {
   protected boolean animations;
   protected Rarity rarityOverride = null;
   List<ITooltipProvider> tooltips = null;
   IBlockDropProvider provider = IBlockDropProvider.SELF;
   EnumMap<Tool.ToolType, Tool> harvestMap = new EnumMap<>(Tool.ToolType.class);
   protected ResourceLocation id;

   public IC2ContainerBlock(String blockName, Properties properties) {
      super(properties);
      this.id = GameData.checkPrefix(blockName, false);
   }

   @Override
   public void registerTools() {
      for (Tool tool : this.harvestMap.values()) {
         tool.register(this);
      }
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public IC2ContainerBlock enableAnimations() {
      this.animations = true;
      return this;
   }

   public IC2ContainerBlock setDropProvider(IBlockDropProvider provider) {
      this.provider = provider;
      return this;
   }

   public IC2ContainerBlock setOverrideRarity(Rarity rarity) {
      this.rarityOverride = rarity;
      return this;
   }

   public IC2ContainerBlock setHarvestTool(Tool tier) {
      this.harvestMap.put(tier.getType(), tier);
      return this;
   }

   public IC2ContainerBlock setTooltips(ITooltipProvider tooltips) {
      this.tooltips = CollectionUtils.<ITooltipProvider>asList(tooltips);
      return this;
   }

   public IC2ContainerBlock addTooltip(ITooltipProvider tooltips) {
      if (this.tooltips == null) {
         this.tooltips = CollectionUtils.<ITooltipProvider>createList();
      }

      this.tooltips.add(tooltips);
      return this;
   }

   @Override
   public Rarity getRarity(ItemStack stack) {
      return this.rarityOverride;
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public abstract BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
      return ITickListener::update;
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      if (this.tooltips != null) {
         int i = 0;

         for (int m = this.tooltips.size(); i < m; i++) {
            this.tooltips.get(i).addInformation(stack, worldIn, tooltip, flagIn);
         }
      }
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      BlockEntity te = worldIn.getBlockEntity(pos);
      if (te instanceof IClickable click && click.getRequiredActions().canDoRightClick() && click.onRightClick(player, handIn, hit.getDirection(), hit)) {
         return InteractionResult.SUCCESS;
      }

      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else {
         return !(te instanceof IHasGui)
               || (!IC2.PLATFORM.isRendering() || !((IHasGui)te).hasGui(player, handIn, hit.getDirection()))
                  && !IC2.PLATFORM.launchGui(player, handIn, hit.getDirection(), (IHasGui)te)
            ? InteractionResult.PASS
            : InteractionResult.SUCCESS;
      }
   }

   public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
      if (worldIn.getBlockEntity(pos) instanceof IClickable click) {
         if (click.getRequiredActions().canDoLeftClick()) {
            click.onLeftClick(player, pos);
         }
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos, Direction side) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IComparatorProvider ? ((IComparatorProvider)tile).getSignalStrength(side) : 0;
   }

   public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
      return this.getComparatorInputOverride(blockState, worldIn, pos, null);
   }

   public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
      BlockEntity tile = blockAccess.getBlockEntity(pos);
      return tile instanceof IRedstoneProvider ? ((IRedstoneProvider)tile).getStrongSignalStrength(side) : 0;
   }

   public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
      BlockEntity tile = blockAccess.getBlockEntity(pos);
      return tile instanceof IRedstoneProvider ? ((IRedstoneProvider)tile).getWeakSignalStrength(side) : 0;
   }

   public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IRedstoneListener ? ((IRedstoneListener)tile).allowWeakSignal(side) : super.shouldCheckWeakPower(state, world, pos, side);
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IRedstoneListener ? ((IRedstoneListener)tile).canConnectToRedstone(side) : super.canConnectRedstone(state, world, pos, side);
   }

   public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
      BlockEntity tile = level.getBlockEntity(pos);
      if (tile instanceof BaseTileEntity) {
         ((BaseTileEntity)tile).onComparatorUpdate(neighbor);
      }
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
         stack.setHoverName(((Nameable)tile).getCustomName());
      }

      return stack;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
         ItemStack stack = this.createDrop(state, tool != null ? tool : ItemStack.EMPTY, builder.getLevel().random, tile, false);
         if (!stack.isEmpty()) {
            if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
               stack.setHoverName(((Nameable)tile).getCustomName());
            }

            drops.add(stack);
         }

         this.addExtraDrops(drops, state, stack, builder.getLevel().getRandom());
         if (tile instanceof IDropProvider) {
            ((IDropProvider)tile).addDrops(drops);
         }

         return drops;
      }
   }

   public ItemStack createDrop(BlockState state, ItemStack stack, RandomSource rand, @Nullable BlockEntity tile, boolean wrench) {
      return this.provider.createDrop(state, stack, rand, tile, wrench);
   }

   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      BlockEntity tile = worldIn.getBlockEntity(pos);
      if (tile instanceof BaseTileEntity) {
         ((BaseTileEntity)tile).onBlockUpdate(blockIn, fromPos);
      }
   }

   public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
      BlockEntity tile = worldIn.getBlockEntity(pos);
      if (tile instanceof ICollideable) {
         ((ICollideable)tile).onEntityCollided(entityIn);
      }
   }

   @Override
   public Direction getFacing(BlockState state, Level world, BlockPos pos) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof BaseTileEntity ? ((BaseTileEntity)tile).getFacing() : Direction.NORTH;
   }

   @Override
   public boolean canSetFacing(BlockState state, Level world, BlockPos pos, Player player, Direction side) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IPersonalWrenchable && ((IPersonalWrenchable)tile).canSetFacing(side, player)
         || tile instanceof IWrenchableTile && ((IWrenchableTile)tile).canSetFacing(side);
   }

   @Override
   public boolean setFacing(BlockState state, Level world, BlockPos pos, Player player, Direction side) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof IPersonalWrenchable base) {
         if (base.canSetFacing(side, player)) {
            base.setFacing(side);
            return true;
         }
      } else if (tile instanceof IWrenchableTile basex && basex.canSetFacing(side)) {
         basex.setFacing(side);
         return true;
      }

      return false;
   }

   @Override
   public AABB hasSpecialAction(BlockState state, Level world, BlockPos pos, Direction dir, Player player, Vec3 hit) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IWrenchableTile ? ((IWrenchableTile)tile).hasSpecialAction(dir, hit, player) : null;
   }

   @Override
   public boolean doSpecialAction(BlockState state, Level world, BlockPos pos, Direction dir, Player player, Vec3 hit) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IWrenchableTile ? ((IWrenchableTile)tile).doSpecialAction(dir, hit, player) : false;
   }

   @Override
   public boolean canRemoveBlock(BlockState state, Level world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IWrenchableTile ? ((IWrenchableTile)tile).canRemoveBlock(player) : false;
   }

   @Override
   public double getDropRate(BlockState state, Level world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof IWrenchableTile ? ((IWrenchableTile)tile).getDropRate(player) : 0.0;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, Level world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      List<ItemStack> drops = new ArrayList<>();
      ItemStack stack = this.createDrop(state, ItemStack.EMPTY, world.random, tile, true);
      if (!stack.isEmpty()) {
         if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
            stack.setHoverName(((Nameable)tile).getCustomName());
         }

         drops.add(stack);
      }

      if (tile instanceof IDropProvider) {
         ((IDropProvider)tile).addDrops(drops);
      }

      return drops;
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
      if (this.animations) {
         BlockEntity tile = worldIn.getBlockEntity(pos);
         if (tile instanceof IParticleSpawner) {
            ((IParticleSpawner)tile).animationTick(rand);
         }
      }
   }
}
