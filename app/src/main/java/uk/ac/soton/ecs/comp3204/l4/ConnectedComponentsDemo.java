package uk.ac.soton.ecs.comp3204.l4;

import gnu.trove.map.hash.TIntIntHashMap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
import org.openimaj.util.function.Function;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Connected components demo")
public class ConnectedComponentsDemo implements Slide {

	private FImage oimage;
	private BufferedImage bimg;
	private ImageComponent ic;
	private MBFImage ccimg;
	private List<Float[]> colours = new ArrayList<Float[]>();
	volatile boolean isRunning = false;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openimaj.content.slideshow.Slide#getComponent(int, int)
	 */
	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new GridBagLayout());

		oimage = ImageUtilities.readF(this.getClass().getResource("threshold.jpg"));
		oimage = oimage.processInplace(new AdaptiveLocalThresholdMean(50, 0.01f)).inverse()
				.extractROI(100, 25, 200, 200);
		bimg = ImageUtilities.createBufferedImageForDisplay(oimage, bimg);

		ic = new ImageComponent(false, false);
		ic.setPreferredSize(new Dimension(600, 600));
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.zoom(4.0);
		ic.setImage(bimg);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		base.add(ic, gbc);

		colours.add(RGBColour.GRAY); // bg colour
		ccimg = new MBFImage(oimage.width, oimage.height, ColourSpace.RGB);

		final JComboBox<String> modeCtl = new JComboBox<String>();
		modeCtl.setPreferredSize(new Dimension(200, modeCtl.getPreferredSize().height));
		modeCtl.addItem("4-Connectivity");
		modeCtl.addItem("8-Connectivity");
		gbc.gridwidth = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		base.add(modeCtl, gbc);

		final JButton cnclBtn = new JButton("Cancel");
		cnclBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isRunning = false;
			}
		});
		cnclBtn.setEnabled(false);
		cnclBtn.setPreferredSize(new Dimension(200, cnclBtn.getPreferredSize().height));
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		base.add(cnclBtn, gbc);

		final JButton runBtn = new JButton("Run");
		runBtn.setPreferredSize(new Dimension(200, runBtn.getPreferredSize().height));
		runBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isRunning = true;

				new Thread(new Runnable() {
					@Override
					public void run() {
						final ConnectMode mode = ((String) modeCtl.getSelectedItem()).startsWith("4") ? ConnectMode.CONNECT_4
								: ConnectMode.CONNECT_8;

						runBtn.setEnabled(false);
						modeCtl.setEnabled(false);
						cnclBtn.setEnabled(true);

						findComponents(oimage, 0, mode, new Function<FImage, Boolean>() {
							@Override
							public Boolean apply(FImage in) {
								for (int y = 0; y < in.height; y++) {
									for (int x = 0; x < in.width; x++) {
										final int val = (int) in.pixels[y][x];

										while (colours.size() <= val + 1) {
											colours.add(RGBColour.randomColour());
										}

										ccimg.setPixel(x, y, colours.get(val));
									}
								}
								ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(ccimg, bimg));

								return isRunning;
							}
						});

						isRunning = false;
						runBtn.setEnabled(true);
						modeCtl.setEnabled(true);
						cnclBtn.setEnabled(false);
					}
				}).start();
			}
		});
		gbc.gridx = 2;
		gbc.gridy = 1;
		base.add(runBtn, gbc);

		return base;
	}

	public FImage findComponents(FImage image, float bgThreshold, ConnectMode mode,
			Function<FImage, Boolean> callback)
	{
		final TIntIntHashMap linked = new TIntIntHashMap();
		final FImage labels = new FImage(image.width, image.height);
		int nextLabel = 1;

		// first pass
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float element = image.pixels[y][x];

				if (element > bgThreshold) {
					final List<Pixel> neighbours = mode.getNeighbours(image, x, y, bgThreshold);
					final List<Integer> L = new ArrayList<Integer>();

					for (final Pixel p : neighbours)
						if (labels.pixels[p.y][p.x] != 0)
							L.add((int) labels.pixels[p.y][p.x]);

					if (L.size() == 0) {
						linked.put(nextLabel, nextLabel);
						labels.pixels[y][x] = nextLabel;
						nextLabel++;
					} else {
						int min = Integer.MAX_VALUE;
						for (final int i : L)
							if (i < min)
								min = i;
						labels.pixels[y][x] = min;

						for (final int i : L) {
							merge(linked, i, min);
						}
					}

					if (!callback.apply(labels))
						return labels;
				}
			}
		}

		// second pass
		for (int i = 1; i <= linked.size(); i++) {
			int min = linked.get(i);

			while (true) {
				final int m = linked.get(min);

				if (m == min)
					break;
				else
					min = m;
			}
			linked.put(i, min);
		}

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (labels.pixels[y][x] != 0) {
					final int min = linked.get((int) labels.pixels[y][x]);
					labels.pixels[y][x] = min;

					if (!callback.apply(labels))
						return labels;
				}
			}
		}

		return labels;
	}

	private void merge(TIntIntHashMap linked, int start, int target) {
		if (start == target)
			return;

		final int old = linked.get(start);

		if (old > target) {
			linked.put(start, target);
			merge(linked, old, target);
		} else {
			merge(linked, target, old);
		}
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new ConnectedComponentsDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
