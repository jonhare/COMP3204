package uk.ac.soton.ecs.comp3005.l6;

import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import uk.ac.soton.ecs.comp3005.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Features that stick")
public class StickyFeaturesDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	private KLTTracker tracker;
	private boolean firstframe = true;
	private FImage prevFrame;

	public StickyFeaturesDemo() {
		final int nFeatures = 100;
		final TrackingContext tc = new TrackingContext();
		final FeatureList fl = new FeatureList(nFeatures);
		tracker = new KLTTracker(tc, fl);
		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel f = super.getComponent(width, height);
		this.vc.getDisplay().addVideoListener(this);
		return f;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final FImage currentFrame = frame.flatten();
		if (firstframe) {
			tracker.selectGoodFeatures(currentFrame);
			firstframe = false;
		} else {
			tracker.trackFeatures(prevFrame, currentFrame);
			tracker.replaceLostFeatures(currentFrame);
		}
		this.prevFrame = currentFrame.clone();
		for (final Feature f : tracker.getFeatureList()) {
			if (f.val >= 0) {
				frame.drawPoint(f, RGBColour.GREEN, 5);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new StickyFeaturesDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
