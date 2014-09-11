package uk.ac.soton.ecs.comp3204.l5;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.datasets.AMToolsSampleDataset;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.util.pair.IndependentPair;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Dataset for building a shape model")
public class PDMDatasetDemo implements Slide {
	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final int imWidth = width / 5;

		final JPanel inner = new JPanel();
		inner.setOpaque(false);
		inner.setPreferredSize(new Dimension(imWidth * 5, height - 3));
		inner.setLayout(new GridLayout(0, 5));

		final ShapeModelDataset<MBFImage> dataset = AMToolsSampleDataset.load(ImageUtilities.MBFIMAGE_READER);

		final ResizeProcessor rp = new ResizeProcessor(imWidth);
		for (final IndependentPair<PointList, MBFImage> p : dataset) {
			final MBFImage image = p.getSecondObject();
			image.drawPoints(p.getFirstObject(), RGBColour.WHITE, 10);
			for (final Line2d line : p.getFirstObject().getLines(dataset.getConnections())) {
				image.drawLine(line, 5, RGBColour.WHITE);
			}
			image.processInplace(rp);

			final ImageComponent ic = new ImageComponent();
			ic.setShowPixelColours(false);
			ic.setShowXYPosition(false);
			ic.setImage(ImageUtilities.createBufferedImageForDisplay(image));
			inner.add(ic);
		}
		base.add(inner);
		return base;
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new PDMDatasetDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
