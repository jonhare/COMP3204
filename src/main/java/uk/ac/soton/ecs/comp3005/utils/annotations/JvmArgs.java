package uk.ac.soton.ecs.comp3005.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describing a runtime jvm parameters required for running a demo or
 * lecture.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JvmArgs {
	/** Any arguments that need to be passed to the demo */
	String[] arguments() default {};

	/** Any JVM arguments that need to be passed to the demo */
	String[] vmArguments() default {};
}
