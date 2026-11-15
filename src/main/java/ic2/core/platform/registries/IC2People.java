package ic2.core.platform.registries;

import ic2.core.utils.MinecraftPlayer;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.UUID;

public class IC2People {
   private static final List<MinecraftPlayer> DEV_TEAM = CollectionUtils.<MinecraftPlayer>createList();
   private static final List<MinecraftPlayer> HELPERS = CollectionUtils.<MinecraftPlayer>createList();
   public static MinecraftPlayer SPEIGER = addTeamMember("721f4109b6a2483789559a4d9d37c9a8", "Speiger");
   public static MinecraftPlayer MEDURIS = addTeamMember("5680934c7620476d822f4bcce051fa98", "MedurisDev");
   public static MinecraftPlayer DAENARA = addTeamMember("0859a3aff06242ae820cf8c0eb6bd471", "Daenara");
   public static MinecraftPlayer RAZZOKK = addTeamMember("2eea59074eaa45158ef68560094891e3", "Razzokk");
   public static MinecraftPlayer QUIZZI = addTeamMember("e7c7670c964b47df8ef47b046ae9fe8d", "quizzi");
   public static MinecraftPlayer MINESASHA = addTeamMember("fcf721ea2e9848eba3c00a7e88e7a9b8", "MineSasha");
   public static MinecraftPlayer CHOCOHEAD = addTeamMember("66580d8e19ad4564a8f1448d021be321", "Chocohead");
   public static MinecraftPlayer DR0N1K = addTeamMember("d2b63da992c64352b92603634dbb652a", "dr0n1k");

   static MinecraftPlayer addTeamMember(String UUID, String name) {
      MinecraftPlayer player = new MinecraftPlayer(UUID, name);
      DEV_TEAM.add(player);
      return player;
   }

   static MinecraftPlayer addHelper(String UUID, String name) {
      MinecraftPlayer player = new MinecraftPlayer(UUID, name);
      HELPERS.add(player);
      return player;
   }

   public static void init() {
      addTeamMember("380df991-f603-344c-a090-369bad2a924a", "Dev");
      helpers();
   }

   public static void helpers() {
      addHelper("68af6d40a79e4a2998469d4a319ec774", "GregoriusTechneticies");
      addHelper("1964e3d1650040e79ff2e6161d41a8c2", "Bear989");
      addHelper("cafb70c2d8e240d68dcc5c977530be9c", "MainFreak");
      addHelper("8c36e7a2faba4bbe89b56bc6564ec0d5", "Lyra");
      addHelper("ad3e5ba2e0214141b567827ede0b806a", "ToAsgaard");
      addHelper("b2ac8c03d99448059e0f57fede63c04d", "KitsuneAlexx");
   }

   public static boolean isDevTeam(UUID id) {
      for (MinecraftPlayer player : DEV_TEAM) {
         if (id.equals(player.getUUID())) {
            return true;
         }
      }

      return false;
   }

   public static MinecraftPlayer findHelper(String name) {
      for (MinecraftPlayer player : HELPERS) {
         if (player.getName().equals(name)) {
            return player;
         }
      }

      return null;
   }
}
