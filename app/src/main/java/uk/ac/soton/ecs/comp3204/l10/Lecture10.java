package uk.ac.soton.ecs.comp3204.l10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

@Lecture(
		title = "L10: Image Classification and Auto Annotation",
		handoutsURL = "http://jonhare.github.io/COMP3204/handouts/pdf/L10-classification.pdf",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L10-classification.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture10 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 3; i++)
			slides.add(new PictureSlide(Lecture10.class.getResource(String.format("l10.%03d.jpg", i))));

		slides.add(new TomatoLinearClassifierDemo());
		slides.add(new PictureSlide(Lecture10.class.getResource(String.format("l10.%03d.jpg", 4))));
		slides.add(new TomatoKNNClassifierDemo(Utils.BACKGROUND_IMAGE));

		for (int i = 5; i <= 38; i++)
			slides.add(new PictureSlide(Lecture10.class.getResource(String.format("l10.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
