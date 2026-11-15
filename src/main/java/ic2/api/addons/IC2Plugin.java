package ic2.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IC2Plugin {
   String name();

   String id();

   String version() default "1.0";

   int requiredAPIVersion() default 0;
}
