package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.utils.Simple3D;
import uk.ac.soton.ecs.comp3005.utils.Simple3D.Scene;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;

public class ImageHistogramDemo implements Slide, VideoDisplayListener<MBFImage> {

	private VideoCaptureComponent vc;
	private MBFImage histogramImage;
	private BufferedImage bHistogramImage;
	private ImageComponent histogramIC;
	private ImageComponent plotIC;
	private MBFImage plotImage;
	private BufferedImage bPlotImage;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		final JPanel videoContainer = new JPanel();
		vc = new VideoCaptureComponent(640, 480);
		videoContainer.add(vc);

		plotIC = new ImageComponent(true, false);
		plotIC.setShowPixelColours(false);
		plotIC.setShowXYPosition(false);
		plotIC.setAllowPanning(false);
		plotIC.setAllowZoom(false);
		plotImage = new MBFImage(350, 350, ColourSpace.RGB);
		plotIC.setImage(bPlotImage = ImageUtilities.createBufferedImage(plotImage, bPlotImage));
		videoContainer.add(plotIC);
		base.add(videoContainer);

		vc.getDisplay().addVideoListener(this);

		final JPanel histogramContainer = new JPanel();
		histogramContainer.setOpaque(false);
		histogramIC = new ImageComponent(true, false);
		histogramIC.setShowPixelColours(false);
		histogramIC.setShowXYPosition(false);
		histogramIC.setAllowPanning(false);
		histogramIC.setAllowZoom(false);
		this.histogramImage = new MBFImage(1024, 200, ColourSpace.RGB);
		histogramIC.setImage(bHistogramImage = ImageUtilities.createBufferedImage(histogramImage, bHistogramImage));
		histogramContainer.add(histogramIC);
		base.add(histogramContainer);

		return base;
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final HistogramModel hm = new HistogramModel(4, 4, 4);
		hm.estimateModel(frame);

		final int imHeight = histogramImage.getHeight();
		histogramImage.fill(RGBColour.WHITE);
		plotImage.fill(RGBColour.WHITE);
		final double max = hm.histogram.max();

		final Scene s = new Scene();
		for (int i = 0; i < 64; i++) {
			final int h = (int) (imHeight * hm.histogram.values[i] / max);
			final int[] coords = hm.histogram.getCoordinates(i);
			final Float[] colour = new Float[] { coords[0] / 4f + 0.125f, coords[1] / 4f + 0.125f, coords[2] / 4f + 0.125f };

			histogramImage.drawShapeFilled(new Rectangle(32 * i, imHeight - h, 32, imHeight), colour);

			final int size = Math.max(3, (int) (20 * hm.histogram.values[i] / max));
			s.addPrimative(new Simple3D.Point3D(coords[0] / 4f * 200 + 25, coords[1] / 4f * 200 + 25,
					coords[2] / 4f * 200 + 25,
					colour, size));
		}
		s.addPrimative(new Simple3D.Line3D(0, 0, 0, 200, 0, 0, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(0, 0, 0, 0, 200, 0, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(0, 0, 0, 0, 0, 200, RGBColour.GRAY, 1));

		s.addPrimative(new Simple3D.Line3D(0, 200, 0, 200, 200, 0, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(200, 200, 0, 200, 200, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(200, 0, 0, 200, 0, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(200, 0, 0, 200, 200, 0, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(0, 0, 200, 200, 0, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(200, 0, 200, 200, 200, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(200, 200, 200, 0, 200, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(0, 200, 200, 0, 0, 200, RGBColour.GRAY, 1));
		s.addPrimative(new Simple3D.Line3D(0, 200, 200, 0, 200, 0, RGBColour.GRAY, 1));

		s.translate(-100, -100, -100);
		s.renderOrtho(Simple3D.euler2Rot(Math.PI / 4, 0, Math.PI / 8), plotImage);

		histogramIC.setImage(bHistogramImage = ImageUtilities.createBufferedImage(histogramImage, bHistogramImage));
		plotIC.setImage(bPlotImage = ImageUtilities.createBufferedImage(plotImage, bPlotImage));
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new ImageHistogramDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
