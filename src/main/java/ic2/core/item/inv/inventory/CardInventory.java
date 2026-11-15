package ic2.core.item.inv.inventory;

import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.components.CardComponent;
import ic2.core.item.inv.container.CardContainer;
import ic2.core.item.tool.infos.BaseTileInfoProvider;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CardInventory extends PortableInventory {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_energy_card.png");
   public List<BaseTileInfoProvider.SettingEntry> settings;
   public int[] selectedColors;
   public int flags = 0;
   public String name;
   public String defaultName;

   public CardInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot, int colorCount, List<BaseTileInfoProvider.SettingEntry> settings) {
      super(owner, held, stack, slot);
      this.selectedColors = new int[colorCount];
      this.settings = settings;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CardContainer(this, player, this.getID(), windowID);
   }

   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   public void onGuiLoaded(IC2Screen screen) {
      screen.setYSize(187);
      screen.setPlayerInventoryOffset(110, 0);
   }

   public int getPlayerYOffset() {
      return 21;
   }

   public void addComponents(Consumer<GuiWidget> components) {
      components.accept(new CardComponent(this));
   }

   public void updateName(String name) {
      this.name = name;
      this.markDirty();
   }

   public void onDataReceived(int key, int value) {
      if (key == 1) {
         this.flags ^= 1 << value;
      } else if (key == 2) {
         this.flags ^= value;
      } else if (key >= 3) {
         this.selectedColors[key - 3] = value;
      }

      this.markDirty();
   }

   public DyeColor getColor(int index) {
      return DyeColor.values()[this.selectedColors[index]];
   }

   @Override
   public int getSlotCount() {
      return 0;
   }

   @Override
   public void save(CompoundTag nbt) {
      NBTUtils.putIntArray(nbt, "colors", this.selectedColors, new int[this.selectedColors.length]);
      NBTUtils.putInt(nbt, "flags", this.flags, 0);
      if (this.name.isEmpty()) {
         nbt.remove("name");
      } else {
         NBTUtils.putString(nbt, "name", this.serialize(this.name), this.defaultName);
      }
   }

   @Override
   public void load(CompoundTag nbt) {
      int[] array = NBTUtils.getIntArray(nbt, "colors", new int[this.selectedColors.length]);
      System.arraycopy(array, 0, this.selectedColors, 0, Math.min(this.selectedColors.length, array.length));
      this.flags = nbt.getInt("flags");
      if (nbt.contains("base_name")) {
         this.defaultName = this.serialize(this.deserialize(nbt.getString("base_name")));
         this.name = nbt.contains("name") ? this.deserialize(nbt.getString("name")) : this.deserialize(this.defaultName);
      } else {
         this.defaultName = "";
         this.name = nbt.contains("name") ? this.deserialize(nbt.getString("name")) : "";
      }
   }

   protected String serialize(String input) {
      return Serializer.toJson(Component.literal(input));
   }

   protected String deserialize(String input) {
      return Serializer.fromJson(input).getString();
   }
}
