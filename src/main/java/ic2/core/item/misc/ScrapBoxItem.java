package ic2.core.item.misc;

import ic2.api.events.ScrapBoxEvent;
import ic2.api.recipes.registries.IScrapBoxRegistry;
import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.platform.recipes.misc.FoodCanRegistry;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.collection.CollectionUtils;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;

public class ScrapBoxItem extends IC2Item implements ISimpleItemModel {
   public ScrapBoxItem() {
      super("scrapbox");
   }

   public static void loadBoxes(IScrapBoxRegistry registry) {
      LocalDate date = LocalDate.now();
      LocalDate start = LocalDate.of(date.getYear(), Month.APRIL, 1);
      LocalDate end = LocalDate.of(date.getYear(), Month.APRIL, 4);
      if (date.isAfter(start) && date.isBefore(end)) {
         registry.addDrop(Items.WOODEN_HOE, 9001.0F);
      } else {
         registry.addDrop(Items.WOODEN_HOE, 5.01F);
      }

      registry.addDrop(Blocks.DIRT, 5.0F);
      registry.addDrop(Blocks.GRASS, 3.0F);
      registry.addDrop(Blocks.GRAVEL, 3.0F);
      registry.addDrop(Blocks.NETHERRACK, 2.0F);
      registry.addDrop(Blocks.SOUL_SAND, 1.0F);
      registry.addDrop(Blocks.PUMPKIN, 0.9F);
      registry.addDrop(Blocks.IRON_ORE, 0.5F);
      registry.addDrop(Blocks.GOLD_ORE, 0.5F);
      registry.addDrop(Blocks.COPPER_ORE, 0.7F);
      registry.addDrop(IC2Blocks.TIN_ORE, 0.7F);
      registry.addDrop(IC2Blocks.SILVER_ORE, 0.6F);
      registry.addDrop(Items.REDSTONE, 0.9F);
      registry.addDrop(IC2Items.DUST_COAL, 0.8F);
      registry.addDrop(IC2Items.DUST_CHARCOAL, 0.8F);
      registry.addDrop(IC2Items.DUST_COPPER, 0.8F);
      registry.addDrop(IC2Items.DUST_TIN, 0.8F);
      registry.addDrop(IC2Items.DUST_SILVER, 0.7F);
      registry.addDrop(IC2Items.DUST_ALUMINIUM, 0.8F);
      registry.addDrop(IC2Items.DUST_DIAMOND, 0.5F);
      registry.addDrop(IC2Items.PLANT_BALL, 0.7F);
      registry.addDrop(IC2Items.DUST_IRON, 0.7F);
      registry.addDrop(IC2Items.DUST_GOLD, 0.7F);
      registry.addDrop(Items.DIAMOND, 0.1F);
      registry.addDrop(Items.EMERALD, 0.05F);
      registry.addDrop(Items.GLOWSTONE_DUST, 0.8F);
      registry.addDrop(IC2Items.RUBBER, 0.8F);
      registry.addDrop(Items.WOODEN_SWORD, 1.0F);
      registry.addDrop(Items.WOODEN_SHOVEL, 1.0F);
      registry.addDrop(Items.WOODEN_PICKAXE, 1.0F);
      registry.addDrop(Items.STICK, 4.0F);
      registry.addDrop(Items.ACACIA_SIGN, 0.2F);
      registry.addDrop(Items.BIRCH_SIGN, 0.2F);
      registry.addDrop(Items.DARK_OAK_SIGN, 0.2F);
      registry.addDrop(Items.JUNGLE_SIGN, 0.2F);
      registry.addDrop(Items.OAK_SIGN, 0.2F);
      registry.addDrop(Items.SPRUCE_SIGN, 0.2F);
      registry.addDrop(IC2Items.RUBBERWOOD_SIGN, 0.2F);
      registry.addDrop(Items.ROTTEN_FLESH, 2.0F);
      registry.addDrop(Items.APPLE, 1.5F);
      registry.addDrop(Items.BREAD, 1.5F);
      registry.addDrop(IC2Items.TIN_CAN_FILLED, 1.5F);
      registry.addDrop(FoodCanRegistry.INSTANCE.getItemForFood(new ItemStack(Items.GOLDEN_APPLE)), 0.05F);
      registry.addDrop(FoodCanRegistry.INSTANCE.getItemForFood(new ItemStack(Items.CHORUS_FRUIT)), 0.05F);
      registry.addDrop(FoodCanRegistry.INSTANCE.getItemForFood(new ItemStack(Items.ROTTEN_FLESH)), 0.25F);
      registry.addDrop(Items.CAKE, 0.5F);
      registry.addDrop(Items.COOKED_PORKCHOP, 0.9F);
      registry.addDrop(Items.COOKED_BEEF, 0.9F);
      registry.addDrop(Items.COOKED_CHICKEN, 0.9F);
      registry.addDrop(Items.COOKED_MUTTON, 0.9F);
      registry.addDrop(Items.COOKED_RABBIT, 0.9F);
      registry.addDrop(Items.LEATHER, 1.0F);
      registry.addDrop(Items.FEATHER, 1.0F);
      registry.addDrop(Items.BONE, 1.0F);
      registry.addDrop(Items.SLIME_BALL, 0.6F);
      registry.addDrop(Items.GOLDEN_HELMET, 0.05F);
      registry.addDrop(Items.GHAST_TEAR, 0.08F);
      registry.addDrop(Items.NETHERITE_SCRAP, 0.01F);
      registry.addDrop(Items.NETHER_STAR, 0.001F);
      registry.addDrop(Items.MINECART, 0.09F);
      registry.addDrop(IC2Items.SU_BATTERY, 0.7F);
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("matter").get("scrapbox");
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (IC2.PLATFORM.isSimulating()) {
         IScrapBoxRegistry.IDrop drop = IC2.RECIPES.get(true).scrapBoxes.getRandomDrop(stack, false);
         if (drop != null) {
            if (drop.getChance() <= 0.1F) {
               player.awardStat(IC2Stats.LUCKY_PERSON);
            }

            List<ItemStack> results = CollectionUtils.<ItemStack>createList();
            results.add(drop.getDrop().copy());
            MinecraftForge.EVENT_BUS.post(new ScrapBoxEvent.ScrapBoxPlayerUseEvent(results, stack, player, hand));

            for (ItemStack result : results) {
               player.drop(result, false);
            }
         }

         if (!player.isCreative()) {
            stack.shrink(1);
         }
      }

      return InteractionResultHolder.success(stack);
   }

   public static class DispenserScrapBoxBehavior extends DefaultDispenseItemBehavior {
      protected ItemStack execute(BlockSource source, ItemStack stack) {
         IScrapBoxRegistry.IDrop drop = IC2.RECIPES.get().scrapBoxes.getRandomDrop(stack, false);
         if (drop != null) {
            List<ItemStack> results = CollectionUtils.<ItemStack>createList();
            results.add(drop.getDrop().copy());
            MinecraftForge.EVENT_BUS.post(new ScrapBoxEvent.ScrapBoxDispenseEvent(results, stack, source));
            if (!results.isEmpty()) {
               Direction direction = (Direction)source.getBlockState().getValue(DispenserBlock.FACING);
               Position pos = DispenserBlock.getDispensePosition(source);
               Level world = source.getLevel();

               for (ItemStack dropItem : results) {
                  spawnItem(world, dropItem, 6, direction, pos);
               }

               this.playAnimation(source, (Direction)source.getBlockState().getValue(DispenserBlock.FACING));
            }
         }

         stack.shrink(1);
         return stack;
      }
   }
}
