package ic2.probeplugin.override;

import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.override.styles.IIconStyleBuilder;
import ic2.probeplugin.override.styles.ILayoutStyleBuilder;
import ic2.probeplugin.override.styles.IProgressStyleBuilder;
import ic2.probeplugin.override.styles.ITextStyleBuilder;
import java.util.List;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IEntityStyle;
import mcjty.theoneprobe.api.IIconStyle;
import mcjty.theoneprobe.api.IItemStyle;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITextStyle;
import mcjty.theoneprobe.api.TankReference;
import mcjty.theoneprobe.apiimpl.elements.ElementEntity;
import mcjty.theoneprobe.apiimpl.elements.ElementIcon;
import mcjty.theoneprobe.apiimpl.elements.ElementItemLabel;
import mcjty.theoneprobe.apiimpl.elements.ElementItemStack;
import mcjty.theoneprobe.apiimpl.elements.ElementPadding;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.apiimpl.elements.ElementTank;
import mcjty.theoneprobe.apiimpl.elements.ElementText;
import mcjty.theoneprobe.apiimpl.styles.EntityStyle;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface IExpandedProbeInfo extends IProbeInfo {
   default ILayoutStyle defaultLayoutStyle() {
      return ILayoutStyleBuilder.create();
   }

   default IIconStyle defaultIconStyle() {
      return IIconStyleBuilder.create();
   }

   default IProgressStyle defaultProgressStyle() {
      return IProgressStyleBuilder.create();
   }

   default ITextStyle defaultTextStyle() {
      return ITextStyleBuilder.create();
   }

   default IItemStyle defaultItemStyle() {
      return new ItemStyle();
   }

   default IEntityStyle defaultEntityStyle() {
      return new EntityStyle();
   }

   default IExpandedProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h) {
      return this.icon(icon, u, v, w, h, this.defaultIconStyle());
   }

   default IExpandedProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h, IIconStyle style) {
      return this.element(new ElementIcon(icon, u, v, w, h, style));
   }

   default IExpandedProbeInfo entity(String entity) {
      return this.entity(entity, this.defaultEntityStyle());
   }

   default IExpandedProbeInfo entity(String entity, IEntityStyle style) {
      return this.element(new ElementEntity(entity, style));
   }

   default IExpandedProbeInfo entity(Entity entity) {
      return this.entity(entity, this.defaultEntityStyle());
   }

   default IExpandedProbeInfo entity(Entity entity, IEntityStyle style) {
      return this.element(new ElementEntity(entity, style));
   }

   default IExpandedProbeInfo text(String text) {
      return this.text(Component.translatable(text), this.defaultTextStyle());
   }

   default IExpandedProbeInfo text(String text, Object... args) {
      return this.text(Component.translatable(text, args), this.defaultTextStyle());
   }

   default IExpandedProbeInfo text(Component text) {
      return this.text(text, this.defaultTextStyle());
   }

   default IExpandedProbeInfo text(Component text, ITextStyle style) {
      return this.element(new ElementText(text, style));
   }

   default IExpandedProbeInfo text(CompoundText text) {
      return this.element(new ElementText(text.get(), ITextStyleBuilder.create()).setLegacy());
   }

   default IExpandedProbeInfo text(CompoundText text, ITextStyle style) {
      return this.element(new ElementText(text.get(), ITextStyleBuilder.create()).setLegacy());
   }

   default IProbeInfo mcText(Component text) {
      return this.element(new ElementText(text));
   }

   default IProbeInfo mcText(Component text, ITextStyle style) {
      return this.element(new ElementText(text, style));
   }

   default IExpandedProbeInfo item(ItemStack stack) {
      return this.item(stack, this.defaultItemStyle());
   }

   default IExpandedProbeInfo item(ItemStack stack, IItemStyle style) {
      return this.element(new ElementItemStack(stack, style));
   }

   default IExpandedProbeInfo itemLabel(ItemStack stack) {
      return this.itemLabel(stack, this.defaultTextStyle());
   }

   default IExpandedProbeInfo itemLabel(ItemStack stack, ITextStyle style) {
      return this.element(new ElementItemLabel(stack));
   }

   default IExpandedProbeInfo progress(int current, int max) {
      return this.progress(current, max, this.defaultProgressStyle());
   }

   default IExpandedProbeInfo progress(long current, long max) {
      return this.progress(current, max, this.defaultProgressStyle());
   }

   default IExpandedProbeInfo progress(long current, long max, IProgressStyle style) {
      return this.element(new ElementProgress(current, max, style));
   }

   default IExpandedProbeInfo progress(int current, int max, IProgressStyle style) {
      return this.element(new ElementProgress((long)current, (long)max, style));
   }

   default IExpandedProbeInfo tank(TankReference ref) {
      return this.tank(ref, IProgressStyleBuilder.create());
   }

   default IExpandedProbeInfo tank(TankReference ref, IProgressStyle style) {
      return this.element(new ElementTank(ref, style));
   }

   default IExpandedProbeInfo horizontal() {
      return this.horizontal(this.defaultLayoutStyle());
   }

   default IExpandedProbeInfo horizontal(ILayoutStyle style) {
      Panel panel = new Panel(style, false);
      this.element(panel);
      return panel;
   }

   default IExpandedProbeInfo vertical() {
      return this.vertical(this.defaultLayoutStyle());
   }

   default IExpandedProbeInfo vertical(ILayoutStyle style) {
      Panel panel = new Panel(style, true);
      this.element(panel);
      return panel;
   }

   default IExpandedProbeInfo padding(int xPadding, int yPadding) {
      return this.element(new ElementPadding(xPadding, yPadding));
   }

   List<IElement> getChildren();

   IExpandedProbeInfo element(IElement var1);
}
