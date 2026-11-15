package ic2.core.platform.recipes.helpers;

import com.mojang.serialization.Codec;
import ic2.core.platform.registries.IC2Items;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.loot.IGlobalLootModifier;

public class LootOverride implements IGlobalLootModifier {
   public static final Codec<LootOverride> CODEC = Codec.unit(LootOverride::new);

   public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      if (context.hasParam(LootContextParams.BLOCK_STATE)) {
         BlockState state = (BlockState)context.getParamOrNull(LootContextParams.BLOCK_STATE);
         if ((state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.TALL_GRASS)
            && (!context.hasParam(LootContextParams.TOOL) || !((ItemStack)context.getParamOrNull(LootContextParams.TOOL)).is(Items.SHEARS))
            && context.getRandom().nextDouble() <= 0.1) {
            generatedLoot.add(new ItemStack(IC2Items.HEMP_SEEDS));
         }
      }

      return generatedLoot;
   }

   public Codec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }
}
