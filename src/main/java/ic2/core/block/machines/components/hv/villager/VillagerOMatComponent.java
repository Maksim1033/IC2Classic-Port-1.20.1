package ic2.core.block.machines.components.hv.villager;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.machines.logic.villager.VillagerTrade;
import ic2.core.block.machines.logic.villager.VillagerTradeList;
import ic2.core.block.machines.tiles.hv.VillagerOMatTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.networking.buffers.data.VillagerBuffer;
import ic2.core.platform.corehacks.mixins.server.LevelMixin;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerOMatComponent extends GuiWidget {
   private static final Box2i XP_BOX = new Box2i(30, 25, 102, 5);
   VillagerOMatTileEntity tile;
   VillagerSelectorComponent.VillagerEntry target;
   List<VillagerOMatComponent.TradeEntry> entries = CollectionUtils.<VillagerOMatComponent.TradeEntry>createList();
   SliderComponent slider = this.addChild(new SliderComponent(new Box2i(135, 35, 12, 80), new Box2i(0, 241, 12, 15))).setNonEmptyRows(1);

   public VillagerOMatComponent(VillagerOMatTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   public void setVillager(VillagerSelectorComponent.VillagerEntry target) {
      boolean changed = this.target != target;
      this.target = target;
      List<VillagerTrade> trades = CollectionUtils.<VillagerTrade>createList();

      for (Villager entry : this.getVillagers()) {
         if (target.target().matches(entry)) {
            trades.addAll(this.tile.trades.getTrade(entry.getUUID()).getKnownTrades());
         }
      }

      this.entries.clear();
      if (target.target().isGroup()) {
         Map<VillagerOMatComponent.SimpleTrade, List<VillagerTrade>> mappedTrades = CollectionUtils.<VillagerOMatComponent.SimpleTrade, List<VillagerTrade>>createLinkedMap();

         for (VillagerTrade trade : trades) {
            mappedTrades.computeIfAbsent(new VillagerOMatComponent.SimpleTrade(trade), this::create).add(trade);
         }

         for (List<VillagerTrade> trade : mappedTrades.values()) {
            this.entries.add(new VillagerOMatComponent.TradeEntry(trade));
         }
      } else {
         for (VillagerTrade trade : trades) {
            this.entries.add(new VillagerOMatComponent.TradeEntry(trade));
         }
      }

      if (changed) {
         this.slider.setCurrent(0);
      }

      this.slider.setMax(this.entries.size());
   }

   private List<VillagerTrade> create(VillagerOMatComponent.SimpleTrade ignored) {
      return CollectionUtils.<VillagerTrade>createList();
   }

   private List<Villager> getVillagers() {
      LevelEntityGetter<Entity> getter = ((LevelMixin)this.gui.getMinecraft().level).getEntityAccessor();
      List<Villager> villagers = CollectionUtils.<Villager>createList();

      for (VillagerTradeList list : this.tile.trades.getVisibleVillagerTrades()) {
         Entity entity = (Entity)getter.get(list.getOwner());
         if (entity instanceof Villager villager) {
            villagers.add(villager);
         }
      }

      return villagers;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();

      for (int i = 0; i < 4; i++) {
         int index = i;
         ((ToolTipButton)gui.addRenderableWidget(10 + i, new ToolTipButton(x + 30, y + 35 + i * 20, 102, 20, this.string(), T -> this.trigger(index))))
            .setToolTip("gui.ic2.villager.toggle");
      }

      String s = "DUNSWE";

      for (int i = 0; i < 6; i++) {
         int index = i;
         ((ToolTipButton)gui.addRenderableWidget(
               20 + i,
               new ToolTipButton(
                  x + 4 + i % 2 * 12, y + 80 + i / 2 * 12, 12, 12, this.string(Character.toString(s.charAt(i))), T -> this.setTarget(index, true)
               )
            ))
            .setToolTip(DirectionList.getName(Direction.from3DDataValue(i)));
      }

      for (int i = 0; i < 6; i++) {
         int index = i;
         ((ToolTipButton)gui.addRenderableWidget(
               30 + i,
               new ToolTipButton(
                  x + 149 + i % 2 * 12, y + 80 + i / 2 * 12, 12, 12, this.string(Character.toString(s.charAt(i))), T -> this.setTarget(index, false)
               )
            ))
            .setToolTip(DirectionList.getName(Direction.from3DDataValue(i)));
      }

      gui.addRenderableWidget(
         new IconButton(x + 135, y + 20, 12, 12, new ItemStack(Items.EXPERIENCE_BOTTLE), T -> this.drainXP()).setToolTip("gui.ic2.villager.drain")
      );
   }

   private void drainXP() {
      this.tile.sendToServer(2, 0);
   }

   private void trigger(int i) {
      VillagerOMatComponent.TradeEntry entry = this.entries.get(i + this.slider.getCurrent());
      VillagerTrade main = entry.getMainTrade();
      boolean result = !main.getState();

      for (VillagerTrade trade : entry.trades()) {
         VillagerTradeList list = this.tile.trades.getTrade(trade.getOwner());
         if (list != null) {
            int index = list.findIndex(trade);
            if (index != -1) {
               trade.setState(result);
               this.tile.sendToServer("info", new VillagerBuffer(trade.getOwner(), index, result));
            }
         }
      }
   }

   private void setTarget(int dir, boolean input) {
      if (input) {
         this.tile.importDir = Direction.from3DDataValue(dir);
      } else {
         this.tile.exportDir = Direction.from3DDataValue(dir);
      }

      this.tile.sendToServer(input ? 0 : 1, dir);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      this.gui
         .drawTextureRegion(matrix, (float)(x + XP_BOX.getX()), (float)(y + XP_BOX.getY()), 0.0F, 222.0F, (float)XP_BOX.getWidth(), (float)XP_BOX.getHeight());
      if (this.tile.storedXP > 0) {
         float progress = (float)this.tile.storedXP / 100000.0F;
         this.gui
            .drawTextureRegion(
               matrix, (float)(x + XP_BOX.getX()), (float)(y + XP_BOX.getY()), 0.0F, 227.0F, (float)XP_BOX.getWidth() * progress, (float)XP_BOX.getHeight()
            );
      }

      for (int i = 0; i < 4; i++) {
         this.gui.getButton(10 + i).visible = i + this.slider.getCurrent() < this.entries.size();
      }

      for (int i = 0; i < 6; i++) {
         this.gui.getButton(20 + i).active = this.tile.importDir.get3DDataValue() != i;
         this.gui.getButton(30 + i).active = this.tile.exportDir.get3DDataValue() != i;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      for (int i = 0; i < 4 && i + this.slider.getCurrent() < this.entries.size(); i++) {
         VillagerTrade entry = this.entries.get(i + this.slider.getCurrent()).getMainTrade();
         this.gui.drawItemStackWithDeco(matrix, 30.0F, (float)(34 + i * 20), this.adjustStack(entry.getMainItem()));
         this.gui.drawItemStackWithDeco(matrix, 60.0F, (float)(34 + i * 20), this.adjustStack(entry.getSubItem()));
         this.gui.drawItemStackWithDeco(matrix, 98.0F, (float)(34 + i * 20), this.adjustStack(entry.getOutputItem()));
         this.gui.bindDefaultTexture();
         this.gui.drawTextureRegion(matrix, 85.0F, (float)(40 + i * 20), entry.isOutOfStock() ? 25.0F : 15.0F, 232.0F, 10.0F, 9.0F, -1);
         if (!this.target.target().isGroup() && entry.isDifferentPrice()) {
            this.gui.drawTextureRegion(matrix, 38.0F, (float)(43 + i * 20), 0.0F, 232.0F, 9.0F, 9.0F, -1);
            this.gui.drawCenterString(matrix, Component.literal(Integer.toString(entry.getRealPrice())), 56, 45 + i * 20, -1);
         }

         this.gui.bindTexture(InventoryMenu.BLOCK_ATLAS);
         IC2Screen.drawTexture(
            matrix,
            119.0F,
            (float)(40 + i * 20),
            (float)this.gui.getBlitOffset(),
            IC2Textures.getMappedEntriesIC2("misc/gui").get(entry.getState() ? "yes" : "no"),
            10.0F,
            10.0F
         );
      }

      this.gui.drawCenterString(matrix, this.translate("gui.ic2.villager.in"), 17, 52, 4210752);
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.villager.out"), 162, 52, 4210752);
      this.gui.drawItemStack(matrix, 6.0F, 60.0F, this.findBlock(this.tile.importDir));
      this.gui.drawItemStack(matrix, 151.0F, 60.0F, this.findBlock(this.tile.exportDir));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (XP_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.villager.xp", new Object[]{this.tile.storedXP}));
      }
   }

   private ItemStack findBlock(Direction dir) {
      Block block = this.tile.getLevel().getBlockState(this.tile.getBlockPos().relative(dir)).getBlock();
      return !block.defaultBlockState().isAir() && block.asItem() != Items.AIR ? new ItemStack(block) : new ItemStack(Items.BARRIER);
   }

   private ItemStack adjustStack(ItemStack stack) {
      return this.target.target().isGroup() ? (stack.isEmpty() ? stack : StackUtil.copyWithSize(stack, 1)) : stack;
   }

   static record SimpleTrade(Item main, Item sub, Item out) {
      SimpleTrade(VillagerTrade trade) {
         this(trade.getMainItem().getItem(), trade.getSubItem().getItem(), trade.getOutputItem().getItem());
      }
   }

   static record TradeEntry(List<VillagerTrade> trades) {
      TradeEntry(VillagerTrade trade) {
         this(ObjectLists.singleton(trade));
      }

      public VillagerTrade getMainTrade() {
         return this.trades.get(0);
      }
   }
}
