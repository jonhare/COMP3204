package uk.ac.soton.ecs.comp3204.l6;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Harris and Stephens Response Function")
public class HarrisResponseDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	static Font FONT = Font.decode("Monaco-32");
	HarrisIPD harris = new HarrisIPD();
	private JSpinner windowSizeSpinner;

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

		frame.internalAssign(ColourMap.Jet.apply(harris.createInterestPointMap().normalise()));
	}

	private float computeScale() {
		final int size = (Integer) windowSizeSpinner.getValue();

		return (size - 1f) / 8f;
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new HarrisResponseDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
