package ic2.core.block.machines.logic.terraformer;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface ITerraformerLogic {
   boolean canRun();

   boolean run(ITerraformer var1, ItemStack var2, ITerraformerBP var3);

   CompoundTag save(CompoundTag var1);

   void load(CompoundTag var1);
}
