package uk.ac.soton.ecs.comp3204.l6;

import java.awt.Font;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Difference-of-Gaussian blob detection")
public class DoGPointsDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	static Font FONT = Font.decode("Monaco-32");
	DoGSIFTEngine engine = new DoGSIFTEngine();

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel c = super.getComponent(width, height);

		engine.getOptions().setDoubleInitialImage(false);

		this.vc.getDisplay().addVideoListener(this);

		return c;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final List<Keypoint> features = engine.findFeatures(frame.flatten());

		for (final Keypoint k : features) {
			frame.drawShape(new Circle(k.x, k.y, 4 * k.scale), RGBColour.RED);
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new DoGPointsDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

}
