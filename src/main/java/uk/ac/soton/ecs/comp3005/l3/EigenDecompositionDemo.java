package uk.ac.soton.ecs.comp3005.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.statistics.distribution.CachingMultivariateGaussian;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class EigenDecompositionDemo extends CovarianceDemo {
	private JTextField evec11;
	private JTextField evec10;
	private JTextField evec01;
	private JTextField evec00;
	private JTextField eval00;
	private JTextField eval01;
	private JTextField eval10;
	private JTextField eval11;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		final GridBagConstraints c = new GridBagConstraints();
		final JPanel matrix1 = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix1.add(evec00 = new JTextField(5), c);
		evec00.setFont(FONT);
		evec00.setHorizontalAlignment(JTextField.RIGHT);
		evec00.setEditable(false);
		c.gridx = 1;
		matrix1.add(evec01 = new JTextField(5), c);
		evec01.setFont(FONT);
		evec01.setHorizontalAlignment(JTextField.RIGHT);
		evec01.setEditable(false);
		c.gridy = 1;
		c.gridx = 0;
		matrix1.add(evec10 = new JTextField(5), c);
		evec10.setFont(FONT);
		evec10.setHorizontalAlignment(JTextField.RIGHT);
		evec10.setEditable(false);
		c.gridx = 1;
		matrix1.add(evec11 = new JTextField(5), c);
		evec11.setFont(FONT);
		evec11.setHorizontalAlignment(JTextField.RIGHT);
		evec11.setEditable(false);

		final JPanel matrix2 = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix2.add(eval00 = new JTextField(5), c);
		eval00.setFont(FONT);
		eval00.setHorizontalAlignment(JTextField.RIGHT);
		eval00.setEditable(false);
		c.gridx = 1;
		matrix2.add(eval01 = new JTextField(5), c);
		eval01.setFont(FONT);
		eval01.setHorizontalAlignment(JTextField.RIGHT);
		eval01.setEditable(false);
		c.gridy = 1;
		c.gridx = 0;
		matrix2.add(eval10 = new JTextField(5), c);
		eval10.setFont(FONT);
		eval10.setHorizontalAlignment(JTextField.RIGHT);
		eval10.setEditable(false);
		c.gridx = 1;
		matrix2.add(eval11 = new JTextField(5), c);
		eval11.setFont(FONT);
		eval11.setHorizontalAlignment(JTextField.RIGHT);
		eval11.setEditable(false);

		base.add(super.getComponent(width, height - 400));

		final JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		final JLabel vecLabel = new JLabel("v = ");
		vecLabel.setFont(Font.decode("Times-58"));
		p.add(vecLabel);
		p.add(matrix1);
		final JSeparator sep = new JSeparator();
		sep.setPreferredSize(new Dimension(100, 10));
		p.add(sep);
		final JLabel valLabel = new JLabel("λ=");
		valLabel.setFont(Font.decode("Times-58"));
		p.add(valLabel);
		p.add(matrix2);
		base.add(p);

		return base;
	}

	@Override
	protected void updateImage() {
		xxField.setText(String.format("%2.2f", covariance.get(0, 0)));
		xyField.setText(String.format("%2.2f", covariance.get(0, 1)));
		yxField.setText(String.format("%2.2f", covariance.get(1, 0)));
		yyField.setText(String.format("%2.2f", covariance.get(1, 1)));

		image.fill(RGBColour.WHITE);

		image.drawLine(image.getWidth() / 2, 0, image.getWidth() / 2, image.getHeight(), 3, RGBColour.BLACK);
		image.drawLine(0, image.getHeight() / 2, image.getWidth(), image.getHeight() / 2, 3, RGBColour.BLACK);

		Ellipse e = EllipseUtilities.ellipseFromCovariance(image.getWidth() / 2, image.getHeight() / 2, covariance,
				100);
		e = e.transformAffine(TransformUtilities.scaleMatrixAboutPoint(1, -1, image.getWidth() / 2, image.getHeight() / 2));

		if (!Double.isNaN(e.getMajor()) && !Double.isNaN(e.getMinor()) && covariance.rank() == 2) {
			final EigenvalueDecomposition decomp = this.covariance.eig();

			evec00.setText(String.format("%2.2f", decomp.getV().get(0, 0)));
			evec01.setText(String.format("%2.2f", decomp.getV().get(0, 1)));
			evec10.setText(String.format("%2.2f", decomp.getV().get(1, 0)));
			evec11.setText(String.format("%2.2f", decomp.getV().get(1, 1)));

			eval00.setText(String.format("%2.2f", decomp.getD().get(0, 0)));
			eval01.setText(String.format("%2.2f", decomp.getD().get(0, 1)));
			eval10.setText(String.format("%2.2f", decomp.getD().get(1, 0)));
			eval11.setText(String.format("%2.2f", decomp.getD().get(1, 1)));

			final Matrix mean = new Matrix(new double[][] { { image.getWidth() / 2, image.getHeight() / 2 } });
			final CachingMultivariateGaussian gauss = new CachingMultivariateGaussian(mean, covariance);

			if (drawData) {
				final Random rng = new Random();
				for (int i = 0; i < 1000; i++) {
					final double[] sample = gauss.sample(rng);
					Point2dImpl pt = new Point2dImpl((float) sample[0], (float) sample[1]);
					pt = pt.transform(TransformUtilities.scaleMatrixAboutPoint(40, -40, image.getWidth() / 2,
							image.getHeight() / 2));
					image.drawPoint(pt, RGBColour.BLUE, 3);
				}
			}

			final MBFImageRenderer renderer = image.createRenderer(RenderHints.ANTI_ALIASED);
			renderer.drawShape(e, 3, RGBColour.RED);
			int x = (int) (100 * decomp.getV().get(0, 0) * Math.sqrt(decomp.getRealEigenvalues()[0]));
			int y = (int) (100 * decomp.getV().get(1, 0) * Math.sqrt(decomp.getRealEigenvalues()[0]));
			renderer.drawLine(image.getWidth() / 2, image.getHeight() / 2, x + image.getWidth() / 2,
					image.getHeight() / 2 - y, 3, RGBColour.GREEN);

			x = (int) (100 * decomp.getV().get(0, 1) * Math.sqrt(decomp.getRealEigenvalues()[1]));
			y = (int) (100 * decomp.getV().get(1, 1) * Math.sqrt(decomp.getRealEigenvalues()[1]));
			renderer.drawLine(image.getWidth() / 2, image.getHeight() / 2, x + image.getWidth() / 2,
					image.getHeight() / 2 - y, 3, RGBColour.GREEN);
		} else {
			evec00.setText("");
			evec01.setText("");
			evec10.setText("");
			evec11.setText("");

			eval00.setText("");
			eval01.setText("");
			eval10.setText("");
			eval11.setText("");
		}

		this.imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new EigenDecompositionDemo(), 1024, 768);
	}
}
