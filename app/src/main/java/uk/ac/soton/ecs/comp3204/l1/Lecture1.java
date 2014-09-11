package uk.ac.soton.ecs.comp3204.l1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.content.slideshow.VideoSlide;
import org.openimaj.video.VideoDisplay.EndAction;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

@Lecture(title = "L1: Building machines that see",
		handoutsURL = "http://jonhare.github.io/COMP3204/handouts/pdf/L1-machines-that-see.pdf",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L1-machines-that-see.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture1 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 25; i++)
			slides.add(new PictureSlide(Lecture1.class.getResource(String.format("l1.%03d.jpg", i))));

		slides.add(new ColourSpacesDemo());

		for (int i = 26; i <= 27; i++)
			slides.add(new PictureSlide(Lecture1.class.getResource(String.format("l1.%03d.jpg", i))));

		slides.add(new SimpleCameraDemo());

		for (int i = 28; i <= 28; i++)
			slides.add(new PictureSlide(Lecture1.class.getResource(String.format("l1.%03d.jpg", i))));

		slides.add(new VideoSlide(Lecture1.class.getResource("grader.mp4"), Utils.BACKGROUND_IMAGE_URL,
				EndAction.PAUSE_AT_END));

		for (int i = 30; i <= 34; i++)
			slides.add(new PictureSlide(Lecture1.class.getResource(String.format("l1.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
