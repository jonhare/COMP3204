package uk.ac.soton.ecs.comp3005.l6;

import java.awt.Font;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Multiscale Harris and Stephens")
public class MultiscaleHarrisPointsDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	static Font FONT = Font.decode("Monaco-32");
	HarrisIPD harris = new HarrisIPD();

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel c = super.getComponent(width, height);

		this.vc.getDisplay().addVideoListener(this);

		return c;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		FImage gimg = frame.flatten();

		// bring to sigma = 1.0
		gimg.processInplace(new FGaussianConvolve(1f));

		for (int i = 0; i < 3; i++) {
			final int t = (int) Math.pow(2, i);
			final double sigma = Math.sqrt(t);
			final float sf = t;

			harris.setDetectionScale((float) sigma);
			harris.setImageBlurred(true);
			harris.findInterestPoints(gimg);

			final float iscale = harris.getIntegrationScale();
			final float samplesize = 4 * iscale + 1;

			for (final InterestPointData ipd : harris.getInterestPoints((float) (1e-5))) {
				ipd.x *= sf;
				ipd.y *= sf;
				frame.drawShape(new Circle(ipd, sf * samplesize), RGBColour.RED);
			}

			gimg.processInplace(new FGaussianConvolve((float) Math.sqrt((Math.pow(2, i + 1) - t))));
			gimg = ResizeProcessor.halfSize(gimg);
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new MultiscaleHarrisPointsDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
