package uk.ac.soton.ecs.comp3005.l2;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.math.util.FloatArrayStatsUtils;

import uk.ac.soton.ecs.comp3005.l1.ColourSpacesDemo;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Demonstrate different colour spaces
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "Simple Mean Colour Feature")
public class SimpleMeanColourFeatureDemo extends ColourSpacesDemo
{
	private JTextField featureField;

	protected SimpleMeanColourFeatureDemo() {
		this.colourSpaces = new ColourSpace[] {
				ColourSpace.RGB, ColourSpace.HSV, ColourSpace.H1H2, ColourSpace.HS, ColourSpace.H2S_2
		};
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel base = super.getComponent(width, height);

		featureField = new JTextField();
		featureField.setOpaque(false);
		featureField.setBorder(null);
		featureField.setFont(Font.decode("Monaco-48"));
		featureField.setHorizontalAlignment(JTextField.CENTER);
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		base.add(featureField, c);

		return base;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		super.beforeUpdate(frame);

		final double[] vector = computeMean(frame, colourSpace);

		featureField.setText(formatVector(vector));
	}

	/**
	 * Compute the mean of the image
	 * 
	 * @param frame
	 * @param colourSpace
	 * @return
	 */
	public static double[] computeMean(MBFImage frame, ColourSpace colourSpace) {
		final double[] vector = new double[colourSpace.getNumBands()];

		for (int b = 0; b < colourSpace.getNumBands(); b++) {
			final float[][] pix = frame.getBand(b).pixels;

			vector[b] = FloatArrayStatsUtils.mean(pix);
		}
		return vector;
	}

	/**
	 * Format vector as a string
	 * 
	 * @param vector
	 * @return
	 */
	public static String formatVector(double[] vector) {
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(String.format("%1.3f", vector[0]));
		for (int i = 1; i < vector.length; i++)
			sb.append(String.format(", %1.3f", vector[i]));
		sb.append("]");
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new SimpleMeanColourFeatureDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
