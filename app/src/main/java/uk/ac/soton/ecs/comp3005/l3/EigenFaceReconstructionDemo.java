package uk.ac.soton.ecs.comp3005.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;
import org.openimaj.image.processing.resize.ResizeProcessor;

import uk.ac.soton.ecs.comp3005.utils.Utils;

/**
 * Demonstration of the effect of changing weights on principal components.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class EigenFaceReconstructionDemo implements Slide {
	private static final Font FONT = Font.decode("Monaco-28");

	private BufferedImage bimg;
	private double[] vector;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:"
				+ getClass().getResource("att_faces.zip"), ImageUtilities.FIMAGE_READER);

		final EigenImages eigen = EigenFaceApproximationDemo.loadEigen(dataset, "eigenbasis.bin");
		vector = new double[10];

		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new BoxLayout(outer, BoxLayout.X_AXIS));

		final ImageComponent ic = new ImageComponent(true, false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		final FImage image = eigen.reconstruct(vector);
		final float sf = (float) height / image.getHeight();
		image.processInplace(new ResizeProcessor(sf));
		ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
		outer.add(ic);

		final JPanel controls = new JPanel();
		controls.setOpaque(false);
		final Dimension dim = new Dimension(width - image.width, height);
		controls.setPreferredSize(dim);
		controls.setSize(dim);
		controls.setLayout(new GridLayout(0, 1));
		final List<JSlider> sliders = new ArrayList<JSlider>();
		for (int i = 0; i < vector.length; i++) {
			final JPanel container = new JPanel();
			container.setOpaque(false);
			container.setLayout(new GridBagLayout());

			final JLabel label = new JLabel("PC " + i + ":");
			label.setFont(FONT);
			container.add(label);

			final JTextField valField = new JTextField(6);
			valField.setOpaque(false);
			valField.setFont(FONT);
			valField.setBorder(null);
			valField.setEditable(false);
			valField.setHorizontalAlignment(JTextField.RIGHT);
			valField.setText("  0.00");

			final JSlider slider = new JSlider(-150, 150, 0);
			sliders.add(slider);

			final int index = i;
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					final FImage image = eigen.reconstruct(vector).process(new ResizeProcessor(sf));
					vector[index] = slider.getValue() / 10.0;
					valField.setText(String.format("%2.2f", slider.getValue() / 10.0));
					ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
				}
			});
			container.add(slider);
			container.add(valField);
			controls.add(container);
		}
		controls.add(new JSeparator(JSeparator.HORIZONTAL));
		final JButton reset = new JButton("Reset");
		reset.setFont(FONT);
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (final JSlider s : sliders)
					s.setValue(0);
			}
		});
		controls.add(reset);
		outer.add(controls);

		return outer;
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new EigenFaceReconstructionDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
