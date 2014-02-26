package uk.ac.soton.ecs.comp3005.l5;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.datasets.AMToolsSampleDataset;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.ArrayBackedVideo;
import org.openimaj.video.VideoDisplay;

public class PDMDatasetVideoDemo implements Slide {
	private VideoDisplay<MBFImage> display;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final ShapeModelDataset<MBFImage> dataset = AMToolsSampleDataset.load(ImageUtilities.MBFIMAGE_READER);

		final MBFImage[] frames = new MBFImage[dataset.size()];
		int i = 0;
		for (final IndependentPair<PointList, MBFImage> p : dataset) {
			final MBFImage image = p.getSecondObject();
			image.drawPoints(p.getFirstObject(), RGBColour.WHITE, 5);
			for (final Line2d line : p.getFirstObject().getLines(dataset.getConnections())) {
				image.drawLine(line, 3, RGBColour.WHITE);
			}

			frames[i++] = image;
		}
		final ArrayBackedVideo<MBFImage> vc = new ArrayBackedVideo<MBFImage>(frames, 5, true);
		display = VideoDisplay.createVideoDisplay(vc, base);

		return base;
	}

	@Override
	public void close() {
		display.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new PDMDatasetVideoDemo(), 1024, 768);
	}
}
