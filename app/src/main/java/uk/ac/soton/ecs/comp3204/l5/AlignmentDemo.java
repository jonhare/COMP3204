package uk.ac.soton.ecs.comp3204.l5;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.datasets.AMToolsSampleDataset;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.shape.algorithm.GeneralisedProcrustesAnalysis;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Shape alignment using Procrustes Analysis")
public class AlignmentDemo implements Slide {
	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final JTabbedPane tabs = new JTabbedPane();
		tabs.setPreferredSize(new Dimension(width, height));

		final ShapeModelDataset<MBFImage> dataset = AMToolsSampleDataset.load(ImageUtilities.MBFIMAGE_READER);
		final Float[][] colours = RGBColour.randomColours(dataset.size()).toArray(new Float[dataset.size()][]);

		final List<PointList> points = dataset.getPointLists();
		final PointListConnections connections = dataset.getConnections();
		final ImageComponent unaligned = createShapes(width, height, points, connections, colours);

		final PointList mean = GeneralisedProcrustesAnalysis.alignPoints(points, 5, 10);
		for (final PointList pl : points) {
			pl.translate(width / 3, height / 3);
			pl.scaleCentroid(100);
		}
		final ImageComponent aligned = createShapes(width, height, points, connections, colours);

		final List<PointList> meanList = new ArrayList<PointList>(1);
		mean.translate(width / 3, height / 3);
		mean.scaleCentroid(100);
		meanList.add(mean);
		final ImageComponent meanComp = createShapes(width, height, meanList, connections,
				new Float[][] { RGBColour.WHITE });

		tabs.addTab("Unaligned", unaligned);
		tabs.addTab("Aligned", aligned);
		tabs.addTab("Mean", meanComp);
		base.add(tabs);

		return base;
	}

	private ImageComponent createShapes(int width, int height, List<PointList> points, PointListConnections connections,
			Float[][] colours)
	{
		final MBFImage image = new MBFImage(width, height - 20, ColourSpace.RGB).fill(RGBColour.BLACK);
		int i = 0;
		for (final PointList p : points) {
			final PointList pts = p.clone();
			pts.scale(1.5f);

			final Float[] c = colours[i++];
			image.drawPoints(pts, c, 5);
			for (final Line2d line : pts.getLines(connections)) {
				image.drawLine(line, 3, c);
			}
		}

		final ImageComponent ic = new ImageComponent(true, true);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.setImage(ImageUtilities.createBufferedImageForDisplay(image));
		return ic;
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new AlignmentDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
