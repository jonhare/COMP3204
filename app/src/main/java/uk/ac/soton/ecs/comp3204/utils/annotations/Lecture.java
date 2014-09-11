package uk.ac.soton.ecs.comp3204.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describing a complete lecture. Should be applied to the main class
 * for that lecture.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Lecture {
	/**
	 * The title of the lecture
	 * 
	 * @return the title
	 */
	String title();

	/**
	 * The (online) URL to the handouts
	 * 
	 * @return the URL
	 */
	String handoutsURL();

	/**
	 * The (online) URL to the PDF version of the slides
	 * 
	 * @return the URL
	 */
	String slidesURL();

	/**
	 * The (online) URL to the handouts
	 * 
	 * @return the URL
	 */

	/**
	 * The author
	 * 
	 * @return the author's name
	 */
	String author() default "Jonathon Hare <jsh2@ecs.soton.ac.uk>";
}
