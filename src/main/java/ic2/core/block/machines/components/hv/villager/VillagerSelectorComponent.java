package ic2.core.block.machines.components.hv.villager;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.logic.villager.IVillagerTarget;
import ic2.core.block.machines.logic.villager.VillagerTradeList;
import ic2.core.block.machines.tiles.hv.VillagerOMatTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.platform.corehacks.mixins.server.LevelMixin;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerSelectorComponent extends GuiWidget {
   private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/hv/gui_villager_selector.png");
   private static final Box2i HIT_BOX = new Box2i(-112, 112, 16, 26);
   SliderComponent sliders = this.addChild(new SliderComponent(new Box2i(-19, 112, 12, 82), new Box2i(0, 201, 12, 15), 5).setCustomTexture(TEXTURE));
   VillagerOMatTileEntity tile;
   Object2IntMap<UUID> xpPoints = new Object2IntOpenHashMap();
   List<VillagerSelectorComponent.VillagerEntry> entries = CollectionUtils.<VillagerSelectorComponent.VillagerEntry>createList();
   Villager villager;
   VillagerSelectorComponent.VillagerEntry selected = null;
   VillagerSelectorComponent.VillagerEntry hovered = null;
   Consumer<VillagerSelectorComponent.VillagerEntry> result;

   public VillagerSelectorComponent(VillagerOMatTileEntity tile, Consumer<VillagerSelectorComponent.VillagerEntry> result) {
      super(new Box2i(-118, 0, 118, 201));
      this.tile = tile;
      this.result = result;
      this.villager = new Villager(EntityType.VILLAGER, tile.getLevel());
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   public void reloadVillagerData() {
      this.xpPoints.clear();
      LevelEntityGetter<Entity> getter = ((LevelMixin)this.gui.getMinecraft().level).getEntityAccessor();
      Map<VillagerProfession, List<Villager>> villagers = CollectionUtils.<VillagerProfession, List<Villager>>createLinkedMap();

      for (VillagerTradeList list : this.tile.trades.getVisibleVillagerTrades()) {
         Entity entity = (Entity)getter.get(list.getOwner());
         if (entity instanceof Villager villagerInstance) {
            villagers.computeIfAbsent(villagerInstance.getVillagerData().getProfession(), this::map).add(villagerInstance);
            this.xpPoints.put(list.getOwner(), list.getXp());
         }
      }

      this.entries.clear();
      if (!villagers.isEmpty()) {
         this.entries.add(new VillagerSelectorComponent.VillagerEntry(IVillagerTarget.any(), null, VillagerProfession.NONE));

         for (Entry<VillagerProfession, List<Villager>> entry : villagers.entrySet()) {
            this.entries.add(new VillagerSelectorComponent.VillagerEntry(IVillagerTarget.group(entry.getKey()), null, entry.getKey()));

            for (Villager villager : entry.getValue()) {
               this.entries
                  .add(new VillagerSelectorComponent.VillagerEntry(IVillagerTarget.simple(villager), villager, villager.getVillagerData().getProfession()));
            }
         }

         if (this.selected == null) {
            this.setSelected(this.entries.get(0));
         } else {
            this.setSelected(this.selected);
         }
      }

      this.sliders.setMax(this.entries.size());
   }

   private List<Villager> map(VillagerProfession ignored) {
      return CollectionUtils.<Villager>createList();
   }

   private void setSelected(VillagerSelectorComponent.VillagerEntry entry) {
      this.selected = entry;
      if (this.result != null) {
         this.result.accept(entry);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      this.reloadVillagerData();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft() - 118;
      int y = this.gui.getGuiTop();
      this.gui.bindTexture(TEXTURE);
      this.gui.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 0.0F, 118.0F, 201.0F);
      this.toggleHeadMode(true);
      boolean found = false;

      for (int i = 0; i < 15 && this.sliders.getCurrent() + i < this.entries.size(); i++) {
         VillagerSelectorComponent.VillagerEntry entry = this.entries.get(this.sliders.getCurrent() + i);
         this.villager.setVillagerData(this.getData(entry));
         int xOff = i % 5 * 18;
         int yOff = i / 5 * 28;
         this.renderVillager(HIT_BOX.getX() + xOff, HIT_BOX.getY() + 48 + yOff, true, matrix, entry);
         if (HIT_BOX.isInBox(mouseX - xOff, mouseY - yOff)) {
            this.hovered = entry;
            RenderSystem.disableDepthTest();
            this.gui
               .drawColoredRegion(
                  matrix,
                  (float)(this.gui.getGuiLeft() + HIT_BOX.getX() + xOff),
                  (float)(this.gui.getGuiTop() + HIT_BOX.getY() + yOff),
                  (float)HIT_BOX.getWidth(),
                  (float)HIT_BOX.getHeight(),
                  -2130706433
               );
            RenderSystem.enableDepthTest();
            found = true;
         }
      }

      if (!found) {
         this.hovered = null;
      }

      this.toggleHeadMode(false);
      this.villager.setVillagerData(this.getData());
      this.renderVillager(-60, 103, false, matrix, this.hovered != null ? this.hovered : (this.selected != null ? this.selected : null));
   }

   private VillagerData getData(VillagerSelectorComponent.VillagerEntry entry) {
      return new VillagerData(this.getType(entry), entry.profession(), this.getLevel(entry));
   }

   private VillagerData getData() {
      if (this.hovered != null) {
         return new VillagerData(this.getType(this.hovered), this.hovered.profession(), this.getLevel(this.hovered));
      } else {
         return this.selected != null
            ? new VillagerData(this.getType(this.selected), this.selected.profession(), this.getLevel(this.selected))
            : new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);
      }
   }

   private int getLevel(VillagerSelectorComponent.VillagerEntry entry) {
      return entry.villager() == null ? 1 : entry.villager().getVillagerData().getLevel();
   }

   private VillagerType getType(VillagerSelectorComponent.VillagerEntry entry) {
      return entry.villager() == null ? VillagerType.PLAINS : entry.villager().getVillagerData().getType();
   }

   private void toggleHeadMode(boolean enable) {
      VillagerRenderer entity = (VillagerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.villager);
      ModelPart root = ((VillagerModel)entity.getModel()).root();

      for (ModelPart part : IterableWrapper.wrap(root.getAllParts())) {
         part.visible = !enable;
      }

      if (enable) {
         root.visible = true;

         for (ModelPart part : IterableWrapper.wrap(((VillagerModel)entity.getModel()).getHead().getAllParts())) {
            part.visible = true;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void renderVillager(int x, int y, boolean scale, PoseStack stack, VillagerSelectorComponent.VillagerEntry entry) {
      int i = this.gui.getGuiLeft();
      int j = this.gui.getGuiTop();
      int renderScale = scale ? (int)(30 * 0.75F) : 30;
      int posX = i + x + (scale ? 8 : 0);
      int posY = j + y;
      if (this.gui.guiGraphics != null) {
         InventoryScreen.renderEntityInInventoryFollowsAngle(this.gui.guiGraphics, posX, posY, renderScale, 0.0F, 0.0F, this.villager);
      }
      if (entry != null && entry.villager() != null) {
         this.gui.bindTexture(TEXTURE);
         float progress = Mth.clamp(
            (float)this.xpPoints.getInt(entry.villager().getUUID()) / (float)VillagerData.getMaxXpPerLevel(entry.villager().getVillagerData().getLevel()),
            0.0F,
            1.0F
         );
         if (scale) {
            this.gui.drawTextureRegion(stack, (float)(i + x), (float)(j + y - 28), 0.0F, 216.0F, 16.0F, 5.0F);
            this.gui.drawTextureRegion(stack, (float)(i + x), (float)(j + y - 28), 0.0F, 221.0F, progress * 16.0F, 5.0F);
         } else {
            this.gui.drawTextureRegion(stack, (float)(i + x - 28), (float)(j + y - 72), 0.0F, 226.0F, 56.0F, 5.0F);
            this.gui.drawTextureRegion(stack, (float)(i + x - 28), (float)(j + y - 72), 0.0F, 231.0F, progress * 56.0F, 5.0F);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.hovered != null) {
         Component comp = this.hovered.target().isGroup()
            ? this.hovered.target().getGroupName()
            : (this.hovered.villager() == null ? this.getTypeName(this.villager.getVillagerData().getProfession()) : this.hovered.villager().getName());
         this.gui.drawCenterString(matrix, comp, -60, 20, -1);
      } else if (this.selected != null) {
         Component comp = this.selected.target().isGroup()
            ? this.selected.target().getGroupName()
            : (this.selected.villager() == null ? this.getTypeName(this.villager.getVillagerData().getProfession()) : this.selected.villager().getName());
         this.gui.drawCenterString(matrix, comp, -60, 20, -1);
      } else {
         this.gui.drawCenterString(matrix, this.getTypeName(this.villager.getVillagerData().getProfession()), -60, 20, -1);
      }

      this.gui.drawCenterString(matrix, this.translate("gui.ic2.villager.selector"), -60, 6, 4210752);
   }

   protected Component getTypeName(VillagerProfession profession) {
      ResourceLocation profName = ForgeRegistryWrapper.getVillageProfessionKey(profession);
      return Component.translatable(
         "entity.minecraft.villager." + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + "." : "") + profName.getPath()
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      for (int i = 0; i < 15 && this.sliders.getCurrent() + i < this.entries.size(); i++) {
         int xOff = i % 5 * 18;
         int yOff = i / 5 * 28;
         if (HIT_BOX.isInBox(mouseX - xOff, mouseY - yOff)) {
            VillagerSelectorComponent.VillagerEntry entry = this.entries.get(this.sliders.getCurrent() + i);
            if (entry.target().isGroup()) {
               tooltips.accept(entry.target().getGroupName());
            } else if (entry.villager() != null) {
               tooltips.accept(entry.villager().getName());
               tooltips.accept(Component.literal(" "));
               tooltips.accept(this.translate("gui.ic2.villager.level", new Object[]{entry.villager().getVillagerData().getLevel()}));
               if (entry.villager().getVillagerData().getLevel() < 5) {
                  tooltips.accept(
                     this.translate(
                        "gui.ic2.villager.level.xp",
                        new Object[]{
                           this.xpPoints.getInt(entry.villager().getUUID()), VillagerData.getMaxXpPerLevel(entry.villager().getVillagerData().getLevel())
                        }
                     )
                  );
               }

               tooltips.accept(this.translate("gui.ic2.villager.highlight"));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      for (int i = 0; i < 15 && this.sliders.getCurrent() + i < this.entries.size(); i++) {
         int xOff = i % 5 * 18;
         int yOff = i / 5 * 28;
         if (HIT_BOX.isInBox(mouseX - xOff, mouseY - yOff)) {
            if (Screen.hasShiftDown()) {
               Villager villager = this.entries.get(this.sliders.getCurrent() + i).villager;
               if (villager != null) {
                  this.tile.sendToServer(3, villager.getId());
               }
            } else {
               this.setSelected(this.entries.get(this.sliders.getCurrent() + i));
            }

            return true;
         }
      }

      return false;
   }

   public static record VillagerEntry(IVillagerTarget target, Villager villager, VillagerProfession profession) {
   }
}
