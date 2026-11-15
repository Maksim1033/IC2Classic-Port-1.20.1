package ic2.core.item.renders.models;

import ic2.core.item.misc.TagItem;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.items.BaseItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public class TagModel extends BaseItemModel<TagModel> {
   @Override
   public void init() {
      this.setParticleTexture(
         (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("item/writable_book"))
      );
      this.quads.addAll(QuadBaker.createQuadsFromTexture(-1, this.getParticleIcon(), this.getTransformMap().gui));
      this.initOther(new TagModel());
   }

   @Override
   public ItemOverrides getOverrides() {
      return TagModel.TagOverride.INSTANCE;
   }

   public static class TagOverride extends ItemOverrides {
      public static final TagModel.TagOverride INSTANCE = new TagModel.TagOverride();

      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int entityId) {
         if (Screen.hasShiftDown()) {
            ResourceLocation location = TagItem.getTag(stack);
            if (location != null) {
               ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
               if (tags != null) {
                  ITag<Item> tag = tags.getTag(ItemTags.create(location));
                  if (tag != null) {
                     long time = Minecraft.getInstance().level.getGameTime() / 20L;
                     return Minecraft.getInstance()
                        .getItemRenderer()
                        .getModel(tag.getRandomElement(RandomSource.create(time)).orElse(Items.AIR).getDefaultInstance(), worldIn, entityIn, entityId);
                  }
               }
            }
         }

         return model;
      }
   }
}
