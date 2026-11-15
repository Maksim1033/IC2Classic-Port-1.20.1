package ic2.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IC2Feature {
   String key();

   String name();

   String comment();

   String[] depends();

   boolean requireAll() default true;

   IC2Feature.LoadingStage loadingStage() default IC2Feature.LoadingStage.PRE;

   String version() default "1.0";

   int requiredApiVersion() default 0;

   public static enum LoadingStage {
      PRE,
      POST;
   }
}
