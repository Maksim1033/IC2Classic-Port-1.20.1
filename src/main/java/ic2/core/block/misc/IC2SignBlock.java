package ic2.core.block.misc;

import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.misc.IAtlasProvider;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2SignBlock extends StandingSignBlock implements IBlockModel, IAtlasProvider, EntityBlock, IRegistryProvider, IToolProvider {
   private IBlockDropProvider drops;
   private ITextureProvider provider;
   private ResourceLocation texture;
   private BlockEntityType<? extends BlockEntity> tile;
   private ResourceLocation id;
   private Tool tool;

   public IC2SignBlock(
      String name,
      WoodType type,
      Tool tool,
      Properties properties,
      BlockEntityType<? extends BlockEntity> tile,
      ITextureProvider provider,
      IBlockDropProvider drops,
      ResourceLocation texture
   ) {
      super(properties.noCollission().requiresCorrectToolForDrops(), type);
      this.id = GameData.checkPrefix(name, false);
      this.tool = tool;
      this.tile = tile;
      this.drops = drops;
      this.provider = provider;
      this.texture = texture;
   }

   @Override
   public void registerTools() {
      this.tool.register(this);
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return Objects.requireNonNull(this.tile.create(pos, state));
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ResourceLocation getAtlas() {
      return Sheets.SIGN_SHEET;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ResourceLocation getAtlasTexture() {
      return this.texture;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return this.provider.getTexture(state, side);
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> result = new ArrayList<>();
         result.add(this.drops.createDrop(state, builder.getOptionalParameter(LootContextParams.TOOL), builder.getLevel().random, null, false));
         return result;
      }
   }

   public static class IC2WallSignBlock extends WallSignBlock implements IBlockModel, IAtlasProvider, EntityBlock, IRegistryProvider, IToolProvider {
      private IBlockDropProvider drops;
      private ITextureProvider provider;
      private ResourceLocation texture;
      private BlockEntityType<? extends BlockEntity> tile;
      private ResourceLocation id;
      private Tool tool;

      public IC2WallSignBlock(
         String name,
         WoodType type,
         Tool tool,
         Properties properties,
         BlockEntityType<? extends BlockEntity> tile,
         ITextureProvider provider,
         IBlockDropProvider drops,
         ResourceLocation texture
      ) {
         super(properties.noCollission().requiresCorrectToolForDrops(), type);
         this.id = GameData.checkPrefix(name, false);
         this.tool = tool;
         this.tile = tile;
         this.drops = drops;
         this.provider = provider;
         this.texture = texture;
      }

      @Override
      public void registerTools() {
         this.tool.register(this);
      }

      public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
         return Objects.requireNonNull(this.tile.create(pos, state));
      }

      @Override
      public ResourceLocation getRegistryName() {
         return this.id;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public ResourceLocation getAtlasTexture() {
         return this.texture;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public ResourceLocation getAtlas() {
         return Sheets.SIGN_SHEET;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
         return this.provider.getTexture(state, side);
      }

      @Override
      public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
         LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
         if (loottable != LootTable.EMPTY) {
            return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
         } else {
            List<ItemStack> result = new ArrayList<>();
            result.add(this.drops.createDrop(state, builder.getOptionalParameter(LootContextParams.TOOL), builder.getLevel().random, null, false));
            return result;
         }
      }
   }
}
