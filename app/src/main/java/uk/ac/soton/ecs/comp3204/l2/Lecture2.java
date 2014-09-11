package uk.ac.soton.ecs.comp3204.l2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.AudioVideoSlide;
import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.video.VideoDisplay.EndAction;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

@Lecture(title = "L2: Machine Learning for Pattern Recognition",
		handoutsURL = "http://jonhare.github.io/COMP3204/handouts/pdf/L2-machine-learning.pdf",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L2-machine-learning.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture2 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		slides.add(new PictureSlide(Lecture2.class.getResource("l2.001.jpg")));
		slides.add(new AudioVideoSlide(Lecture2.class.getResource("pepper.m4v"), EndAction.PAUSE_AT_END));

		for (int i = 3; i <= 10; i++)
			slides.add(new PictureSlide(Lecture2.class.getResource(String.format("l2.%03d.jpg", i))));

		slides.add(new SimpleMeanColourFeatureDemo());

		for (int i = 11; i <= 23; i++)
			slides.add(new PictureSlide(Lecture2.class.getResource(String.format("l2.%03d.jpg", i))));

		slides.add(new LinearClassifierDemo());

		for (int i = 24; i <= 29; i++)
			slides.add(new PictureSlide(Lecture2.class.getResource(String.format("l2.%03d.jpg", i))));

		slides.add(new KNNDemo());

		for (int i = 30; i <= 34; i++)
			slides.add(new PictureSlide(Lecture2.class.getResource(String.format("l2.%03d.jpg", i))));

		slides.add(new KMeansDemo());

		slides.add(new PictureSlide(Lecture2.class.getResource("l2.035.jpg")));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
