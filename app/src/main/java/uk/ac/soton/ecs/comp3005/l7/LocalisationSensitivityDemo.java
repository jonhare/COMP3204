package uk.ac.soton.ecs.comp3005.l7;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;

import uk.ac.soton.ecs.comp3005.utils.SlidingWindowComponent;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Localisation Sensitivity")
public class LocalisationSensitivityDemo extends KeyAdapter implements Slide {

	private SlidingWindowComponent swc;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));

		final FImage image = ImageUtilities.readF(LocalisationSensitivityDemo.class.getResource("frame00000101.jpg"));
		swc = new SlidingWindowComponent(image, new Rectangle(350, 285, 11, 11));
		base.add(swc);

		final MBFImage hist = new MBFImage(640, 100, 3);
		final MBFImage histSmoothed = new MBFImage(640, 100, 3);
		final BufferedImage histB = ImageUtilities.createBufferedImageForDisplay(hist);
		final BufferedImage histSmoothedB = ImageUtilities.createBufferedImageForDisplay(histSmoothed);

		final JPanel histContainer = new JPanel();
		histContainer.setOpaque(false);
		final ImageComponent histIC = new ImageComponent(histB);
		histIC.setAllowPanning(false);
		histIC.setAllowZoom(false);
		histIC.setShowPixelColours(false);
		histIC.setShowXYPosition(false);
		histContainer.setBorder(BorderFactory.createTitledBorder("Unweighted Histogram"));
		histContainer.add(histIC);
		base.add(histContainer);

		final JPanel histSmoothedContainer = new JPanel();
		histSmoothedContainer.setOpaque(false);
		final ImageComponent histSmoothedIC = new ImageComponent(histSmoothedB);
		histSmoothedIC.setAllowPanning(false);
		histSmoothedIC.setAllowZoom(false);
		histSmoothedIC.setShowPixelColours(false);
		histSmoothedIC.setShowXYPosition(false);
		histSmoothedContainer.setBorder(BorderFactory.createTitledBorder("Gaussian weighted Histogram"));
		histSmoothedContainer.add(histSmoothedIC);
		base.add(histSmoothedContainer);

		swc.addRectangleMoveListener(new Operation<Rectangle>() {
			@Override
			public void perform(Rectangle rect) {
				final FImage sample = image.extractROI(rect);

				final float[] h1 = buildHist(sample);
				final float[] h2 = buildWeightedHist(sample);

				updateHist(histIC, histB, hist, h1);
				updateHist(histSmoothedIC, histSmoothedB, histSmoothed, h2);
			}

			private void updateHist(ImageComponent histIC, BufferedImage histB, MBFImage hist, float[] h) {
				updateHist(hist, h);
				histIC.setImage(histB = ImageUtilities.createBufferedImageForDisplay(hist));
			}

			private void updateHist(MBFImage hist, float[] h) {
				hist.zero();
				final int bw = hist.getWidth() / 64;
				final int bh = hist.getHeight();

				for (int i = 0; i < 64; i++) {
					final float hh = h[i] * bh;
					hist.drawShapeFilled(new Rectangle(i * bw, hist.getHeight() - hh, bw, hh), RGBColour.RED);
				}
			}

			private float[] buildWeightedHist(FImage sample) {
				final float[] hist = new float[64];

				final float sigma = (sample.width - 1) / 6f;
				final int h = (sample.width - 1) / 2;

				float sum = 0;
				for (int y = 0; y < sample.height; y++) {
					for (int x = 0; x < sample.width; x++) {
						final float pix = sample.pixels[y][x];
						int bin = (int) (pix * 64);
						if (bin >= 64)
							bin = 63;

						final double dist = ((x - h) * (x - h) + (y - h) * (y - h));
						final double weight = (1 / (sigma * Math.sqrt(2 * Math.PI)))
								* Math.exp(-dist / (2 * sigma * sigma));

						hist[bin] += weight;
						sum += weight;
					}
				}

				for (int i = 0; i < 64; i++)
					hist[i] /= sum;

				return hist;
			}

			private float[] buildHist(FImage sample) {
				final float[] hist = new float[64];

				float sum = 0;
				for (int y = 0; y < sample.height; y++) {
					for (int x = 0; x < sample.width; x++) {
						final float pix = sample.pixels[y][x];
						int bin = (int) (pix * 64);
						if (bin >= 64)
							bin = 63;

						hist[bin]++;
						sum++;
					}
				}

				for (int i = 0; i < 64; i++)
					hist[i] /= sum;

				return hist;
			}
		});

		swc.setRect(swc.getRect());

		return base;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		final Rectangle rect = swc.getRect();
		if (e.getKeyChar() == 'a') {
			rect.x--;
			swc.setRect(rect);
		}
		if (e.getKeyChar() == 'd') {
			rect.x++;
			swc.setRect(rect);
		}
		if (e.getKeyChar() == 'w') {
			rect.y--;
			swc.setRect(rect);
		}
		if (e.getKeyChar() == 's') {
			rect.y++;
			swc.setRect(rect);
		}
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new LocalisationSensitivityDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
