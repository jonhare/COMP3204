package uk.ac.soton.ecs.comp3005.l2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "Machine Learning for Pattern Recognition",
		handoutsURL = "",
		slidesURL = "")
@JvmArgs(vmArguments = "-Xmx3G")
public class Lecture2 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		slides.add(new SimpleMeanColourFeatureDemo());
		slides.add(new LinearClassifierDemo());
		slides.add(new KNNDemo());
		slides.add(new KMeansDemo());

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
