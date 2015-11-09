package uk.ac.soton.ecs.comp3204.l6;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Difference of Gaussian response")
public class DoGResponseDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage>, ChangeListener {
	static Font FONT = Font.decode("Monaco-32");
	private JSlider scaleSlider;
	private JTextField scaleField;
	private JSlider kSlider;
	private JTextField kField;

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel c = super.getComponent(width, height);

		this.vc.getDisplay().addVideoListener(this);

		final JPanel controls = new JPanel(new GridBagLayout());
		controls.setOpaque(false);

		final JLabel scaleLbl = new JLabel("<html>Scale (\u03C3<sup>2</sup>): </html>");
		scaleLbl.setFont(FONT);
		controls.add(scaleLbl);

		scaleSlider = new JSlider(1, 500, 1);
		controls.add(scaleSlider);

		scaleField = new JTextField(5);
		scaleField.setEditable(false);
		scaleField.setFont(FONT);
		scaleField.setHorizontalAlignment(JTextField.RIGHT);
		controls.add(scaleField);

		final JLabel kLbl = new JLabel("<html>k: </html>");
		kLbl.setFont(FONT);
		controls.add(kLbl);

		kSlider = new JSlider(10, 50, 16);
		controls.add(kSlider);

		kField = new JTextField(5);
		kField.setEditable(false);
		kField.setFont(FONT);
		kField.setHorizontalAlignment(JTextField.RIGHT);
		controls.add(kField);

		scaleSlider.addChangeListener(this);
		kSlider.addChangeListener(this);

		stateChanged(null);

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
		final float k = this.kSlider.getValue() / 10f;
		final float sigma2 = this.scaleSlider.getValue();

		final float ksigma2 = sigma2 * (k * k - 1);

		final FImage gimg = frame.flatten();

		final FImage blur1 = gimg.processInplace(new FGaussianConvolve((float) Math.sqrt(sigma2)));
		final FImage blur2 = gimg.process(new FGaussianConvolve((float) Math.sqrt(ksigma2)));

		blur1.subtractInplace(blur2);
		blur1.normalise();

		frame.internalAssign(blur1.toRGB());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		final float k = this.kSlider.getValue() / 10f;
		final float sigma2 = this.scaleSlider.getValue();

		this.kField.setText(String.format("%2.2f", k));
		this.scaleField.setText(String.format("%2.2f", sigma2));
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new DoGResponseDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

}
