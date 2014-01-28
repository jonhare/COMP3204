package uk.ac.soton.ecs.comp3005;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
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
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

import Jama.Matrix;

public class CovarianceDemo implements Slide {
	private Matrix covariance;
	private BufferedImage bimg;
	private MBFImage image;
	private ImageComponent imageComp;
	private JSlider xxSlider;
	private JSlider yySlider;
	private JSlider xySlider;
	private TextField xxField;
	private TextField xyField;
	private TextField yxField;
	private TextField yyField;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		covariance = Matrix.identity(2, 2);

		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		image = new MBFImage(400, 400, ColourSpace.RGB);
		imageComp = new DisplayUtilities.ImageComponent(true);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		base.add(imageComp);

		xxSlider = new JSlider();
		yySlider = new JSlider();
		xySlider = new JSlider();
		xySlider.setMinimum(-50);
		xySlider.setMaximum(50);
		xySlider.setValue(0);

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

		final Font fnt = Font.decode("Monaco-48");

		final GridBagConstraints c = new GridBagConstraints();
		final JPanel matrix = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix.add(xxField = new TextField(5), c);
		xxField.setFont(fnt);
		xxField.setEditable(false);
		c.gridx = 1;
		matrix.add(xyField = new TextField(5), c);
		xyField.setFont(fnt);
		xyField.setEditable(false);
		c.gridy = 1;
		c.gridx = 0;
		matrix.add(yxField = new TextField(5), c);
		yxField.setFont(fnt);
		yxField.setEditable(false);
		c.gridx = 1;
		matrix.add(yyField = new TextField(5), c);
		yyField.setFont(fnt);
		yyField.setEditable(false);

		final JPanel controls = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		controls.add(matrix, c);

		c.gridy = 1;
		c.gridx = 0;
		controls.add(new JLabel("XX"), c);
		c.gridx = 1;
		controls.add(xxSlider, c);

		c.gridy = 2;
		c.gridx = 0;
		controls.add(new JLabel("YY"), c);
		c.gridx = 1;
		controls.add(yySlider, c);

		c.gridy = 3;
		c.gridx = 0;
		controls.add(new JLabel("XY"), c);
		c.gridx = 1;
		controls.add(xySlider, c);

		base.add(controls);

		updateImage();

		return base;
	}

	private void updateImage() {
		xxField.setText(String.format("%2.3f", covariance.get(0, 0)));
		xyField.setText(String.format("%2.3f", covariance.get(0, 1)));
		yxField.setText(String.format("%2.3f", covariance.get(1, 0)));
		yyField.setText(String.format("%2.3f", covariance.get(1, 1)));

		image.fill(RGBColour.WHITE);

		final Ellipse e = EllipseUtilities.ellipseFromCovariance(image.getWidth() / 2, image.getHeight() / 2, covariance,
				100);
		image.createRenderer(RenderHints.ANTI_ALIASED).drawShape(e, 3, RGBColour.RED);

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
		new SlideshowApplication(new CovarianceDemo(), 1024, 768);
	}
}
