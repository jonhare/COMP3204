package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;

/**
 * Simple global thresholding
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class GlobalThresholdVideoDemo implements Slide {
	final static Font FONT = Font.decode("Monaco-28");
	protected VideoCaptureComponent vc;
	protected volatile float threshold = 0;
	protected volatile boolean otsu;

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
		vc = new VideoCaptureComponent(640, 480);
		base.add(vc);

		final JPanel container = new JPanel();
		container.setOpaque(false);

		final JTextField valueField = new JTextField(4);
		valueField.setOpaque(false);
		valueField.setHorizontalAlignment(JTextField.RIGHT);
		valueField.setFont(FONT);
		valueField.setEditable(false);
		valueField.setBorder(null);
		valueField.setText("0.00");

		final JLabel label = new JLabel("Threshold:");
		label.setFont(FONT);
		container.add(label);

		final JSlider slider = new JSlider(0, 255, 0);
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width + 250, slider.getPreferredSize().height));

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				threshold = slider.getValue() / 255f;
				valueField.setText(String.format("%1.2f", threshold));
			}
		});

		container.add(slider);
		container.add(valueField);

		container.add(new JSeparator(SwingConstants.VERTICAL));

		final JLabel label2 = new JLabel("Otsu:");
		label2.setFont(FONT);
		container.add(label2);

		final JCheckBox cb = new JCheckBox();
		container.add(cb);

		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				otsu = cb.isSelected();
				if (otsu) {
					slider.setEnabled(false);
				} else {
					slider.setEnabled(true);
				}
			}
		});

		base.add(container);
		outer.add(base);

		vc.getDisplay().addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage grey = frame.flatten();

				if (otsu) {
					threshold = OtsuThreshold.calculateThreshold(grey, 256);
					slider.setValue((int) (threshold * 255f));
				}
				final FImage tf = grey.threshold(threshold);
				frame.internalAssign(tf.toRGB());
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// do nothing
			}
		});

		return outer;
	}

	@Override
	public void close() {
		vc.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new GlobalThresholdVideoDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
