package ic2.core.utils.codegen;

public class AutomatedCodeGen {
   public static void main(String[] args) {
      if (args.length < 1) {
         System.out.println("path to /src/main is needed as parameter");
      }

      AutomatedTextureGen.generateTextures(args[0]);
   }
}
