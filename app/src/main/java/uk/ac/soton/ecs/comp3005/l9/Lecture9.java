package uk.ac.soton.ecs.comp3005.l9;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3005.utils.annotations.Lecture;

@Lecture(title = "L9: Image Search and Classification",
		handoutsURL = "http://jonhare.github.io/COMP3005/handouts/pdf/L9-imagesearchclass.pdf",
		slidesURL = "http://jonhare.github.io/COMP3005/lectures/pdf/L9-imagesearchclass.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture9 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		// for (int i = 1; i <= 33; i++)
		// slides.add(new
		// PictureSlide(Lecture9.class.getResource(String.format("l8.%03d.jpg",
		// i))));
		//
		// slides.add(new LineRANSACDemo());
		//
		// for (int i = 34; i <= 36; i++)
		// slides.add(new
		// PictureSlide(Lecture9.class.getResource(String.format("l8.%03d.jpg",
		// i))));
		//
		// slides.add(new SIFTMatchingDemo());
		//
		// for (int i = 37; i <= 38; i++)
		// slides.add(new
		// PictureSlide(Lecture9.class.getResource(String.format("l8.%03d.jpg",
		// i))));
		//
		// slides.add(new SFMDemo());
		//
		// for (int i = 39; i <= 40; i++)
		// slides.add(new
		// PictureSlide(Lecture9.class.getResource(String.format("l8.%03d.jpg",
		// i))));
		//
		// slides.add(new SpectrogramDemo());
		//
		// for (int i = 41; i <= 57; i++)
		// slides.add(new
		// PictureSlide(Lecture9.class.getResource(String.format("l8.%03d.jpg",
		// i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
