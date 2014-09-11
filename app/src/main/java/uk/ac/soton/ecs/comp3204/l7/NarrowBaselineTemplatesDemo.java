package uk.ac.soton.ecs.comp3204.l7;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Narrow baseline matching with templates")
public class NarrowBaselineTemplatesDemo implements Slide, VideoDisplayListener<MBFImage> {
	private static final int bufferSize = 10;
	private VideoCaptureComponent vc;
	private KLTTracker tracker;
	private ArrayBlockingDroppingQueue<MBFImage> prevFramesC = new ArrayBlockingDroppingQueue<MBFImage>(bufferSize);

	public NarrowBaselineTemplatesDemo() {
		final int nFeatures = 10;
		final TrackingContext tc = new TrackingContext();
		final FeatureList fl = new FeatureList(nFeatures);
		tracker = new KLTTracker(tc, fl);
		tc.setSequentialMode(false);
		tc.setWriteInternalImages(false);
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		vc = new VideoCaptureComponent(320, 240);
		vc.getDisplay().getScreen().setPreferredSize(new Dimension(640, 240));

		for (int i = 0; i < bufferSize; i++) {
			final MBFImage pf = vc.getDisplay().getVideo().getNextFrame();
			try {
				prevFramesC.put(pf);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		vc.getDisplay().addVideoListener(this);

		outer.add(vc);

		return outer;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final MBFImage currentFrameC = frame.extractROI(0, 0, 320, 240);
		final FImage currentFrame = currentFrameC.flatten();
		final MBFImage prevFrameC = prevFramesC.poll();
		final FImage prevFrame = prevFrameC.flatten();

		tracker.selectGoodFeatures(currentFrame);
		final FeatureList cfl = tracker.getFeatureList().clone();
		tracker.trackFeatures(currentFrame, prevFrame);
		final FeatureList pfl = tracker.getFeatureList();

		try {
			this.prevFramesC.put(currentFrameC.clone());
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		final MBFImage out = new MBFImage(320 * 2, 240, 3);
		out.drawImage(currentFrameC, 0, 0);
		out.drawImage(prevFrameC, 320, 0);

		for (int i = 0; i < pfl.features.length; i++) {
			final Feature pf = pfl.features[i];
			final Feature cf = cfl.features[i];

			if (pf.val >= 0) {
				out.drawPoint(cf, RGBColour.GREEN, 5);

				pf.x += 320;
				out.drawPoint(pf, RGBColour.GREEN, 5);

				out.drawLine(cf, pf, 1, RGBColour.RED);
			}
		}

		frame.internalAssign(out);
	}

	@Override
	public void close() {
		vc.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new NarrowBaselineTemplatesDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
