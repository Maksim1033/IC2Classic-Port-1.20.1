package ic2.core.utils.helpers;

public class SanityHelper {
   public static boolean check(CharSequence s) {
      return s == null || s.isEmpty() || s.charAt(0) <= ' ' || s.charAt(s.length() - 1) <= ' ';
   }

   public static String firstLetterUppercase(String string) {
      if (string != null && !string.isEmpty()) {
         String first = Character.toString(string.charAt(0));
         return string.replaceFirst(first, first.toUpperCase());
      } else {
         return string;
      }
   }

   public static String toPascalCase(String input) {
      StringBuilder builder = new StringBuilder();

      for (String s : input.replaceAll("_", " ").split(" ")) {
         builder.append(firstLetterUppercase(s)).append(" ");
      }

      return builder.substring(0, builder.length() - 1);
   }
}
