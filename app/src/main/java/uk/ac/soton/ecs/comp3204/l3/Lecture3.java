package uk.ac.soton.ecs.comp3204.l3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

@Lecture(title = "L3: Covariance and Principal Components",
		handoutsURL = "http://jonhare.github.io/COMP3204/handouts/pdf/L3-covariance.pdf",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L3-covariance.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture3 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 10; i++)
			slides.add(new PictureSlide(Lecture3.class.getResource(String.format("l3.%03d.jpg", i))));

		slides.add(new CovarianceDemo());

		for (int i = 11; i <= 28; i++)
			slides.add(new PictureSlide(Lecture3.class.getResource(String.format("l3.%03d.jpg", i))));

		slides.add(new EigenDecompositionDemo());

		for (int i = 29; i <= 34; i++)
			slides.add(new PictureSlide(Lecture3.class.getResource(String.format("l3.%03d.jpg", i))));

		slides.add(new PCADemo());

		for (int i = 35; i <= 41; i++)
			slides.add(new PictureSlide(Lecture3.class.getResource(String.format("l3.%03d.jpg", i))));

		slides.add(new FaceDatasetDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.042.jpg")));

		slides.add(new MeanFaceDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.043.jpg")));

		slides.add(new MeanCenteredFacesDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.044.jpg")));

		slides.add(new FacePrincipleComponentsDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.045.jpg")));

		slides.add(new EigenFaceApproximationDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.046.jpg")));

		slides.add(new EigenFaceReconstructionDemo());

		slides.add(new PictureSlide(Lecture3.class.getResource("l3.047.jpg")));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
