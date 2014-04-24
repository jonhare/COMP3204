package uk.ac.soton.ecs.comp3005.l7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "L7: Local features and matching",
		handoutsURL = "http://jonhare.github.io/COMP3005/handouts/pdf/L7-matching.pdf",
		slidesURL = "http://jonhare.github.io/COMP3005/lectures/pdf/L7-matching.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture4 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 24; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		slides.add(new NarrowBaselineTemplatesDemo());

		for (int i = 25; i <= 31; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		slides.add(new LocalisationSensitivityDemo());

		for (int i = 32; i <= 35; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		slides.add(new GradOriDemo());

		for (int i = 36; i <= 46; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		slides.add(new EucMatchingDemo());

		for (int i = 47; i <= 48; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		slides.add(new MatchingDemo());

		for (int i = 49; i <= 49; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l7.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
