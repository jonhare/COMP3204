package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Adaptive thresholding
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demonstration(title = "Adaptive thresholding on a still image")
public class AdaptiveThresholdDemo implements Slide {
	final static Font FONT = Font.decode("Monaco-28");
	private FImage oimage;
	private BufferedImage bimg;
	private ImageComponent ic;
	private JCheckBox cb;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openimaj.content.slideshow.Slide#getComponent(int, int)
	 */
	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		oimage = ImageUtilities.readF(this.getClass().getResource("threshold.jpg"));
		bimg = ImageUtilities.createBufferedImageForDisplay(oimage, bimg);

		ic = new ImageComponent(true, true);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.setImage(bimg);
		base.add(ic);

		final Box container = new Box(BoxLayout.Y_AXIS);
		container.setOpaque(false);

		// threshold
		final JPanel threshPanel = new JPanel();
		threshPanel.setOpaque(false);
		final JLabel label = new JLabel("Threshold:");
		label.setFont(FONT);
		threshPanel.add(label);

		cb = new JCheckBox();
		threshPanel.add(cb);
		container.add(threshPanel);

		// size
		final JPanel sizePanel = new JPanel();
		sizePanel.setOpaque(false);

		final JLabel label2 = new JLabel("  Size:");
		label2.setFont(FONT);
		sizePanel.add(label2);

		final JSlider sizeSlider = new JSlider(2, 50, 5);
		sizeSlider.setPreferredSize(new Dimension(sizeSlider.getPreferredSize().width + 250, sizeSlider
				.getPreferredSize().height));
		sizePanel.add(sizeSlider);

		final JTextField sizeField = new JTextField(5);
		sizeField.setOpaque(false);
		sizeField.setHorizontalAlignment(JTextField.RIGHT);
		sizeField.setFont(FONT);
		sizeField.setEditable(false);
		sizeField.setBorder(null);
		sizeField.setText("5");
		sizePanel.add(sizeField);
		container.add(sizePanel);

		// offset
		final JPanel offsetPanel = new JPanel();
		offsetPanel.setOpaque(false);

		final JLabel label3 = new JLabel("Offset:");
		label3.setFont(FONT);
		offsetPanel.add(label3);

		final JSlider offsetSlider = new JSlider(0, 100, 0);
		offsetSlider.setPreferredSize(new Dimension(offsetSlider.getPreferredSize().width + 250, offsetSlider
				.getPreferredSize().height));
		offsetPanel.add(offsetSlider);

		final JTextField offsetField = new JTextField(5);
		offsetField.setOpaque(false);
		offsetField.setHorizontalAlignment(JTextField.RIGHT);
		offsetField.setFont(FONT);
		offsetField.setEditable(false);
		offsetField.setBorder(null);
		offsetField.setText("0.000");
		offsetPanel.add(offsetField);

		container.add(offsetPanel);

		// listeners...
		sizeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int size = sizeSlider.getValue();
				final float offset = (float) (offsetSlider.getValue() / 1000.0);
				sizeField.setText(String.format("%d", size));

				if (cb.isSelected()) {
					ic.setImage(bimg = ImageUtilities
							.createBufferedImageForDisplay(threshold(oimage, size, offset), bimg));
				}
			}
		});

		offsetSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int size = sizeSlider.getValue();
				final float offset = (float) (offsetSlider.getValue() / 1000.0);
				offsetField.setText(String.format("%1.3f", offset));

				if (cb.isSelected()) {
					ic.setImage(bimg = ImageUtilities
							.createBufferedImageForDisplay(threshold(oimage, size, offset), bimg));
				}
			}
		});

		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cb.isSelected()) {
					final int size = sizeSlider.getValue();
					final float offset = (float) (offsetSlider.getValue() / 1000.0);

					ic.setImage(bimg = ImageUtilities
							.createBufferedImageForDisplay(threshold(oimage, size, offset), bimg));
				} else {
					ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(oimage, bimg));
				}
			}
		});

		base.add(container);
		outer.add(base);

		return outer;
	}

	private FImage threshold(FImage oimage, int size, float offset) {
		return oimage.process(new AdaptiveLocalThresholdMean(size, offset));
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new AdaptiveThresholdDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
