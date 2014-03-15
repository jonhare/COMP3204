package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

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

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Simple global thresholding
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demonstration(title = "Global thresholding on a still image")
public class GlobalThresholdDemo implements Slide {
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

		final JPanel container = new JPanel();
		container.setOpaque(false);

		final JTextField valueField = new JTextField(4);
		valueField.setOpaque(false);
		valueField.setHorizontalAlignment(JTextField.RIGHT);
		valueField.setFont(FONT);
		valueField.setEditable(false);
		valueField.setBorder(null);
		valueField.setText("0.50");

		final JLabel label = new JLabel("Threshold:");
		label.setFont(FONT);
		container.add(label);

		cb = new JCheckBox();
		container.add(cb);

		final JSlider slider = new JSlider(0, 255, 128);
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width + 250, slider.getPreferredSize().height));

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final float threshold = slider.getValue() / 255f;
				valueField.setText(String.format("%1.2f", threshold));

				if (cb.isSelected()) {
					ic.setImage(bimg = ImageUtilities
							.createBufferedImageForDisplay(oimage.clone().threshold(threshold), bimg));
				}
			}
		});

		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cb.isSelected()) {
					final float threshold = slider.getValue() / 255f;
					ic.setImage(bimg = ImageUtilities
							.createBufferedImageForDisplay(oimage.clone().threshold(threshold), bimg));
				} else {
					ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(oimage, bimg));
				}
			}
		});

		container.add(slider);
		container.add(valueField);

		base.add(container);
		outer.add(base);

		return outer;
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new GlobalThresholdDemo(), 1024, 768,
				Utils.BACKGROUND_IMAGE);
	}
}
