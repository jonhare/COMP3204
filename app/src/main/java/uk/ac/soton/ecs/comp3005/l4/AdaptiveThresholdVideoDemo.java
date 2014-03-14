package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
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
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
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
public class AdaptiveThresholdVideoDemo implements Slide, VideoDisplayListener<MBFImage> {
	final static Font FONT = Font.decode("Monaco-28");
	private JCheckBox cb;
	private VideoCaptureComponent vc;
	private volatile boolean doThresh = false;
	private JSlider offsetSlider;
	private JSlider sizeSlider;

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

		sizeSlider = new JSlider(2, 50, 5);
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

		offsetSlider = new JSlider(0, 100, 0);
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
				sizeField.setText(String.format("%d", size));
			}
		});

		offsetSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final float offset = (float) (offsetSlider.getValue() / 1000.0);
				offsetField.setText(String.format("%1.3f", offset));
			}
		});

		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				doThresh = cb.isSelected();
			}
		});

		base.add(container);
		outer.add(base);

		vc.getDisplay().addVideoListener(this);

		return outer;
	}

	private FImage threshold(FImage oimage, int size, float offset) {
		return oimage.process(new AdaptiveLocalThresholdMean(size, offset));
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
		final int size = sizeSlider.getValue();
		final float offset = (float) (offsetSlider.getValue() / 1000.0);

		if (doThresh) {
			frame.internalAssign(threshold(frame.flatten(), size, offset).toRGB());
		} else {
			frame.internalAssign(frame.flatten().toRGB());
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new AdaptiveThresholdVideoDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
