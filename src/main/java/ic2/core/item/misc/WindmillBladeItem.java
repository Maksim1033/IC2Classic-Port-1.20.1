package ic2.core.item.misc;

import ic2.api.items.IWindmillBlade;
import ic2.core.block.base.misc.IAtlasProvider;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WindmillBladeItem extends IC2Item implements IWindmillBlade, ISimpleItemModel, IAtlasProvider {
   public static final ResourceLocation ROTOR_SHEET = InventoryMenu.BLOCK_ATLAS;
   int radius;
   float effectiveness;
   String type;
   ResourceLocation texture;

   public WindmillBladeItem(String itemName, String type, int radius, float effectiveness, int maxDurability, String texture) {
      this(itemName, type, radius, effectiveness, new ResourceLocation("ic2", texture), new PropertiesBuilder().maxDamage(maxDurability));
   }

   public WindmillBladeItem(String itemName, String type, int radius, float effectiveness, ResourceLocation texture, PropertiesBuilder properties) {
      super(itemName, properties.setNoRepair().maxStackSize(1));
      this.type = type;
      this.radius = radius;
      this.effectiveness = effectiveness;
      this.texture = texture;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addSimpleToolTip("tooltip.block.ic2.windmill_rotor.radius", this.radius);
      helper.addSimpleToolTip("tooltip.block.ic2.windmill_rotor.efficiency", this.effectiveness);
      if (this.getMaxDamage(stack) == 0) {
         helper.addSimpleToolTip("tooltip.block.ic2.windmill_rotor.indestructible");
      }
   }

   @Override
   public int getRadius(ItemStack stack) {
      return this.radius;
   }

   @Override
   public float getEffectiveness(ItemStack stack) {
      return this.effectiveness;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack) {
      return this.texture;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "blades/rotors").get(this.type);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ResourceLocation getAtlas() {
      return ROTOR_SHEET;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ResourceLocation getAtlasTexture() {
      return this.texture;
   }
}
