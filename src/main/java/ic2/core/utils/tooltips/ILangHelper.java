package ic2.core.utils.tooltips;

import com.google.common.collect.ObjectArrays;
import ic2.core.IC2;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.SanityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILangHelper {
   default MutableComponent string() {
      return Component.empty();
   }

   default MutableComponent string(String text) {
      return Component.literal(text);
   }

   default MutableComponent string(String text, ChatFormatting... formats) {
      return Component.literal(text).withStyle(formats);
   }

   default MutableComponent translate(String text) {
      return Component.translatable(text);
   }

   default MutableComponent translate(String text, ChatFormatting... formats) {
      return Component.translatable(text).withStyle(formats);
   }

   default MutableComponent translate(String text, Object... args) {
      return Component.translatable(text, args);
   }

   default MutableComponent translate(String text, ChatFormatting format, Object... args) {
      return Component.translatable(text, args).withStyle(format);
   }

   default MutableComponent error() {
      return Component.translatable("tooltip.item.ic2.error");
   }

   default MutableComponent pos(BlockPos pos) {
      return Component.translatable("tooltip.item.ic2.pos", new Object[]{pos.getX(), pos.getY(), pos.getZ()});
   }

   default MutableComponent pos(Level world, BlockPos pos) {
      return this.pos(world.dimension(), pos);
   }

   default MutableComponent pos(ResourceKey<Level> dimension, BlockPos pos) {
      return Component.translatable(
         "tooltip.item.ic2.dim_pos", new Object[]{SanityHelper.toPascalCase(dimension.location().getPath()), pos.getX(), pos.getY(), pos.getZ()}
      );
   }

   default MutableComponent bool(boolean value) {
      return Component.translatable(value ? "misc.ic2.yes" : "misc.ic2.no");
   }

   default MutableComponent eu(int amount) {
      return Component.translatable("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)amount)});
   }

   default MutableComponent buildKeyDescription(KeyHelper key, Object... args) {
      return this.buildKeyDescription(ObjectArrays.concat(IC2.KEYBOARD.getKeyName(key).withStyle(ChatFormatting.GOLD), args));
   }

   default MutableComponent buildKeyDescription(KeyHelper key, String translationKey, Object... args) {
      return this.buildKeyDescription(
         IC2.KEYBOARD.getKeyName(key).withStyle(ChatFormatting.GOLD), this.translate(translationKey, args).withStyle(ChatFormatting.UNDERLINE)
      );
   }

   default MutableComponent buildKeyDescription(KeyHelper key1, KeyHelper key2, Object... args) {
      return this.buildKeyDescription(ObjectArrays.concat(combineKeys(key1, key2), args));
   }

   default MutableComponent buildKeyDescription(KeyHelper key1, KeyHelper key2, String translationKey, Object... args) {
      return this.buildKeyDescription(combineKeys(key1, key2), this.translate(translationKey, args).withStyle(ChatFormatting.UNDERLINE));
   }

   default MutableComponent buildKeyDescription(KeyHelper[] keys, Object... args) {
      return this.buildKeyDescription(ObjectArrays.concat(combineKeys(keys), args));
   }

   default MutableComponent buildKeyDescription(KeyHelper[] keys, String translationKey, Object... args) {
      return this.buildKeyDescription(combineKeys(keys), this.translate(translationKey, args).withStyle(ChatFormatting.UNDERLINE));
   }

   default MutableComponent buildKeyDescription(Object... args) {
      return Component.translatable("tooltip.ic2.press_key_description", args);
   }

   @OnlyIn(Dist.CLIENT)
   default MutableComponent combineKeys(KeyMapping... keys) {
      MutableComponent text = Component.empty();
      int i = 0;

      for (int m = keys.length; i < m; i++) {
         if (i != 0) {
            text.append(" & ");
         }

         text.append(keys[i].getKey().getDisplayName().copy().withStyle(ChatFormatting.GOLD));
      }

      return text;
   }

   static MutableComponent combineKeys(KeyHelper... keys) {
      MutableComponent text = Component.empty();
      int i = 0;

      for (int m = keys.length; i < m; i++) {
         if (i != 0) {
            text.append(" & ");
         }

         text.append(IC2.KEYBOARD.getKeyName(keys[i]).withStyle(ChatFormatting.GOLD));
      }

      return text;
   }
}
