package uk.ac.soton.ecs.comp3005.l4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "L4: Types of image feature and segmentation",
		handoutsURL = "http://jonhare.github.io/COMP3005/handouts/pdf/L4-imagefeatures.pdf",
		slidesURL = "http://jonhare.github.io/COMP3005/lectures/pdf/L4-imagefeatures.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture4 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 12; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new ImageHistogramDemo());

		for (int i = 13; i <= 18; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new GlobalThresholdVideoDemo());
		slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", 19))));
		slides.add(new GlobalThresholdDemo());

		for (int i = 20; i <= 22; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new AdaptiveThresholdDemo());

		for (int i = 23; i <= 24; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new AdaptiveThresholdVideoDemo());

		for (int i = 25; i <= 26; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new KMeansSegmentationDemo());

		for (int i = 27; i <= 28; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new KMeansSpatialSegmentationDemo());

		for (int i = 29; i <= 30; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new FHSegmentationDemo());

		for (int i = 31; i <= 36; i++)
			slides.add(new PictureSlide(Lecture4.class.getResource(String.format("l4.%03d.jpg", i))));

		slides.add(new ConnectedComponentsDemo());

		slides.add(new PictureSlide(Lecture4.class.getResource("l4.037.jpg")));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
