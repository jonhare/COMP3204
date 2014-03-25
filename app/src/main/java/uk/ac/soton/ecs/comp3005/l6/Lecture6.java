package uk.ac.soton.ecs.comp3005.l6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "L6: Local interest points",
		handoutsURL = "http://jonhare.github.io/COMP3005/handouts/pdf/L6-interestpoints.pdf",
		slidesURL = "http://jonhare.github.io/COMP3005/lectures/pdf/L6-interestpoints.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture6 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 4; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new StickyFeaturesDemo());

		for (int i = 5; i <= 14; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new StructureTensorDemo());

		for (int i = 15; i <= 18; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new HarrisResponseDemo());

		for (int i = 19; i <= 20; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new ThresholdedHarrisResponseDemo());

		for (int i = 21; i <= 21; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new HarrisPointsDemo());

		for (int i = 22; i <= 31; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new MultiscaleHarrisPointsDemo());

		for (int i = 32; i <= 38; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new DoGResponseDemo());

		for (int i = 39; i <= 40; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		slides.add(new DoGPointsDemo());

		for (int i = 41; i <= 41; i++)
			slides.add(new PictureSlide(Lecture6.class.getResource(String.format("l6.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
