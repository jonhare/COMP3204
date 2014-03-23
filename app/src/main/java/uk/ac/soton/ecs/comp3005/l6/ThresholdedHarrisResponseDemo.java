package uk.ac.soton.ecs.comp3005.l6;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Thresholded Harris and Stephens Response")
public class ThresholdedHarrisResponseDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	static Font FONT = Font.decode("Monaco-32");
	HarrisIPD harris = new HarrisIPD();
	private JSpinner windowSizeSpinner;
	private JSlider thresholdSpinner;

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel c = super.getComponent(width, height);

		this.vc.getDisplay().addVideoListener(this);

		final JPanel controls = new JPanel(new GridBagLayout());
		controls.setOpaque(false);

		windowSizeSpinner = new JSpinner(new SpinnerNumberModel(21, 7, 31, 2));
		((JSpinner.NumberEditor) windowSizeSpinner.getEditor()).getTextField().setFont(FONT);
		final JLabel lbl = new JLabel("Window Size (pixels): ");
		lbl.setFont(FONT);
		controls.add(lbl);
		controls.add(windowSizeSpinner);

		thresholdSpinner = new JSlider(0, 100);
		final JLabel lbl2 = new JLabel("Threshold: ");
		lbl2.setFont(FONT);
		controls.add(lbl2);
		controls.add(thresholdSpinner);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1;
		c.add(controls, gbc);

		return c;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		harris.setDetectionScale(0);
		harris.setImageBlurred(true);
		harris.setIntegrationScale(computeScale());

		harris.findInterestPoints(frame.flatten());

		final FImage out = harris.createInterestPointMap();
		out.threshold(thresholdSpinner.getValue() / 100000f);
		frame.internalAssign(out.toRGB());
	}

	private float computeScale() {
		final int size = (Integer) windowSizeSpinner.getValue();

		return (size - 1f) / 8f;
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new ThresholdedHarrisResponseDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
