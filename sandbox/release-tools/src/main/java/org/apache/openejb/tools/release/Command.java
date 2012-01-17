package org.apache.openejb.tools.release;

/**
 * @version $Rev$ $Date$
 */
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Command {
    String value() default "";

    Class[] dependsOn() default {};
}
