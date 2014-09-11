package uk.ac.soton.ecs.comp3204.l5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.JvmArgs;
import uk.ac.soton.ecs.comp3204.utils.annotations.Lecture;

@Lecture(title = "L5: Shape description and modelling",
		handoutsURL = "http://jonhare.github.io/COMP3204/handouts/pdf/L5-shapedescription.pdf",
		slidesURL = "http://jonhare.github.io/COMP3204/lectures/pdf/L5-shapedescription.pdf")
@JvmArgs(vmArguments = "-Xmx1G")
public class Lecture5 {
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		for (int i = 1; i <= 12; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new ScalarShapeFeaturesDemo());

		for (int i = 13; i <= 17; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new MomentFeaturesDemo());

		for (int i = 18; i <= 19; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new HuMomentsVideoDemo());

		for (int i = 20; i <= 39; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new RAGDemo());

		for (int i = 40; i <= 43; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new PDMDatasetDemo());
		slides.add(new PDMDatasetVideoDemo());

		for (int i = 44; i <= 45; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new AlignmentDemo());

		for (int i = 46; i <= 48; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new PDMDemo());

		for (int i = 49; i <= 50; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		slides.add(new CLMDemo());
		slides.add(new PuppeteerDemo());

		for (int i = 51; i <= 52; i++)
			slides.add(new PictureSlide(Lecture5.class.getResource(String.format("l5.%03d.jpg", i))));

		new SlideshowApplication(slides, 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
