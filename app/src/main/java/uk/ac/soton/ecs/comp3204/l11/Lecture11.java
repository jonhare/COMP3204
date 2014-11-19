package uk.ac.soton.ecs.comp3204.l11;

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

@Lecture(title = "L11: Towards 3D vision",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L11-towards3d.pdf", handoutsURL = "")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture11 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 19; i++)
			slides.add(new PictureSlide(Lecture11.class.getResource(String.format("l11.%03d.jpg", i))));

		slides.add(new CalibrationDemo());

		for (int i = 20; i <= 28; i++)
			slides.add(new PictureSlide(Lecture11.class.getResource(String.format("l11.%03d.jpg", i))));

		slides.add(new VideoSlide(Lecture11.class.getResource("sanmarco.avi"), Utils.BACKGROUND_IMAGE_URL,
				EndAction.STOP_AT_END));

		for (int i = 30; i <= 48; i++)
			slides.add(new PictureSlide(Lecture11.class.getResource(String.format("l11.%03d.jpg", i))));

		slides.add(new KinectDemo());

		slides.add(new PictureSlide(Lecture11.class.getResource(String.format("l11.%03d.jpg", 49))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
