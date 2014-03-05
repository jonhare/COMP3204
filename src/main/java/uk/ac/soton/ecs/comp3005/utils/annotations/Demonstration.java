package uk.ac.soton.ecs.comp3005.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describing a demonstration within a lecture. Should be applied to
 * the each demonstration class.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Demonstration {
	/**
	 * The title of the lecture
	 * 
	 * @return the title
	 */
	String title();

	/**
	 * The author
	 * 
	 * @return the author's name
	 */
	String author() default "Jonathon Hare <jsh2@ecs.soton.ac.uk>";
}
