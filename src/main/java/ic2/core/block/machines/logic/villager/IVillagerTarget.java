package ic2.core.block.machines.logic.villager;

import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public interface IVillagerTarget {
   boolean matches(Villager var1);

   boolean isGroup();

   Component getGroupName();

   static IVillagerTarget simple(UUID id) {
      return new IVillagerTarget.Single(id);
   }

   static IVillagerTarget simple(Villager villager) {
      return new IVillagerTarget.Single(villager.getUUID());
   }

   static IVillagerTarget group(VillagerProfession profession) {
      return new IVillagerTarget.Group(profession);
   }

   static IVillagerTarget group(Villager villager) {
      return new IVillagerTarget.Group(villager.getVillagerData().getProfession());
   }

   static IVillagerTarget any() {
      return IVillagerTarget.All.INSTANCE;
   }

   public static class All implements IVillagerTarget {
      public static final IVillagerTarget INSTANCE = new IVillagerTarget.All();

      @Override
      public boolean matches(Villager target) {
         return true;
      }

      @Override
      public boolean isGroup() {
         return true;
      }

      @Override
      public Component getGroupName() {
         return Component.translatable("gui.ic2.villager.all");
      }
   }

   public static class Group implements IVillagerTarget {
      VillagerProfession profession;

      public Group(VillagerProfession profession) {
         this.profession = profession;
      }

      @Override
      public boolean matches(Villager target) {
         return target.getVillagerData().getProfession() == this.profession;
      }

      @Override
      public boolean isGroup() {
         return true;
      }

      @Override
      public Component getGroupName() {
         return Component.translatable("gui.ic2.villager.cat", new Object[]{this.getTypeName(this.profession)});
      }

      protected Component getTypeName(VillagerProfession profession) {
         ResourceLocation profName = ForgeRegistryWrapper.getVillageProfessionKey(profession);
         return Component.translatable(
            "entity.minecraft.villager." + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + "." : "") + profName.getPath()
         );
      }
   }

   public static class Single implements IVillagerTarget {
      UUID owner;

      public Single(UUID owner) {
         this.owner = owner;
      }

      @Override
      public boolean matches(Villager target) {
         return target.getUUID().equals(this.owner);
      }

      @Override
      public boolean isGroup() {
         return false;
      }

      @Override
      public Component getGroupName() {
         return Component.empty();
      }
   }
}
