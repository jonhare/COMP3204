package uk.ac.soton.ecs.comp3005.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.statistics.distribution.CachingMultivariateGaussian;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;
import Jama.Matrix;

/**
 * Demonstration of 2D covariance
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "2D Covariance Matrix Demo")
public class CovarianceDemo implements Slide {
	protected static final Font FONT = Font.decode("Monaco-48");

	protected Matrix covariance;
	protected BufferedImage bimg;
	protected MBFImage image;
	protected ImageComponent imageComp;
	protected JSlider xxSlider;
	protected JSlider yySlider;
	protected JSlider xySlider;
	protected JTextField xxField;
	protected JTextField xyField;
	protected JTextField yxField;
	protected JTextField yyField;
	protected boolean drawData = true;
	protected boolean drawEllipse = false;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		covariance = Matrix.identity(2, 2);

		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		image = new MBFImage(400, 400, ColourSpace.RGB);
		imageComp = new DisplayUtilities.ImageComponent(true, false);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		base.add(imageComp);

		final JPanel sep = new JPanel();
		sep.setOpaque(false);
		sep.setPreferredSize(new Dimension(80, 450));
		base.add(sep);

		xxSlider = new JSlider();
		yySlider = new JSlider();
		xySlider = new JSlider();
		xySlider.setMinimum(-100);
		xySlider.setMaximum(100);
		xxSlider.setValue(100);
		xySlider.setValue(0);
		yySlider.setValue(100);

		xxSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setXX();
			}
		});

		yySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setYY();
			}
		});

		xySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setXY();
			}
		});

		final GridBagConstraints c = new GridBagConstraints();
		final JPanel matrix = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix.add(xxField = new JTextField(5), c);
		xxField.setFont(FONT);
		xxField.setEditable(false);
		c.gridx = 1;
		matrix.add(xyField = new JTextField(5), c);
		xyField.setFont(FONT);
		xyField.setEditable(false);
		c.gridy = 1;
		c.gridx = 0;
		matrix.add(yxField = new JTextField(5), c);
		yxField.setFont(FONT);
		yxField.setEditable(false);
		c.gridx = 1;
		matrix.add(yyField = new JTextField(5), c);
		yyField.setFont(FONT);
		yyField.setEditable(false);

		final JPanel controls = new JPanel(new GridBagLayout());
		controls.setOpaque(false);
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		controls.add(matrix, c);

		c.gridwidth = 2;
		c.gridy = 2;
		c.gridx = 0;
		controls.add(new JSeparator(), c);
		c.gridy = 3;
		c.gridx = 0;
		controls.add(new JSeparator(), c);
		c.gridwidth = 1;

		c.gridy = 4;
		c.gridx = 0;
		final JLabel xxLabel = new JLabel("XX:");
		xxLabel.setFont(FONT);
		controls.add(xxLabel, c);
		c.gridx = 1;
		controls.add(xxSlider, c);

		c.gridy = 5;
		c.gridx = 0;
		final JLabel yyLabel = new JLabel("YY:");
		yyLabel.setFont(FONT);
		controls.add(yyLabel, c);
		c.gridx = 1;
		controls.add(yySlider, c);

		c.gridy = 6;
		c.gridx = 0;
		final JLabel xyLabel = new JLabel("XY:");
		xyLabel.setFont(FONT);
		xyLabel.setHorizontalAlignment(JLabel.RIGHT);
		controls.add(xyLabel, c);
		c.gridx = 1;
		controls.add(xySlider, c);

		c.gridwidth = 2;
		c.gridy = 7;
		c.gridx = 0;
		controls.add(new JSeparator(), c);
		c.gridy = 5;
		c.gridx = 0;
		controls.add(new JSeparator(), c);

		c.gridwidth = 1;
		c.gridy = 8;
		c.gridx = 0;
		controls.add(new JLabel("Show data points:"), c);
		c.gridx = 1;
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(drawData);
		checkBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				drawData = ((JCheckBox) e.getSource()).isSelected();
				updateImage();
			}
		});
		controls.add(checkBox, c);

		c.gridwidth = 1;
		c.gridy = 9;
		c.gridx = 0;
		controls.add(new JLabel("Show ellipse:"), c);
		c.gridx = 1;
		final JCheckBox checkBox2 = new JCheckBox();
		checkBox2.setSelected(drawEllipse);
		checkBox2.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				drawEllipse = ((JCheckBox) e.getSource()).isSelected();
				updateImage();
			}
		});
		controls.add(checkBox2, c);
		base.add(controls);

		updateImage();

		return base;
	}

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

			if (drawEllipse)
				image.createRenderer(RenderHints.ANTI_ALIASED).drawShape(e, 3, RGBColour.RED);
		}

		this.imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
	}

	protected void setYY() {
		covariance.set(1, 1, yySlider.getValue() / 100d);
		updateImage();
	}

	protected void setXY() {
		covariance.set(1, 0, xySlider.getValue() / 100d);
		covariance.set(0, 1, xySlider.getValue() / 100d);
		updateImage();
	}

	protected void setXX() {
		covariance.set(0, 0, xxSlider.getValue() / 100d);
		updateImage();
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new CovarianceDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
