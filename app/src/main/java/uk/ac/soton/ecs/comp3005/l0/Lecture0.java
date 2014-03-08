package uk.ac.soton.ecs.comp3005.l0;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "Fork Me!",
		handoutsURL = "",
		slidesURL = "http://jonhare.github.io/COMP3005/lectures/pdf/L0-forkme.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture0 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 4; i++)
			slides.add(new PictureSlide(Lecture0.class.getResource(String.format("l0.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}

}
