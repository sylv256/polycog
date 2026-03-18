package gay.sylv.polycog.impl.client.wheel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Indicates that the annotated parameter or field is automatically moved to the
/// object for which the passed object applies. Note that this is invalid for
/// static methods.
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface AutomaticallyMoved {
}
