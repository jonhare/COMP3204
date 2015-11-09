package uk.ac.soton.ecs.comp3204.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;
import org.openimaj.io.IOUtils;

import uk.ac.soton.ecs.comp3204.l3.FaceDatasetDemo.FaceDatasetProvider;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

/**
 * Demonstrate the effect of low-dimensional approximation.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Demonstration(title = "Eigenface approximation demo ")
public class EigenFaceApproximationDemo implements Slide {
	private static final int NUM_IMAGES = 30;
	private static final Font FONT = Font.decode("Monaco-28");
	private EigenImages eigen;
	private FImage[] faces = new FImage[NUM_IMAGES];
	private FImage[] images = new FImage[NUM_IMAGES];
	private BufferedImage[] bimages = new BufferedImage[NUM_IMAGES];
	private ImageComponent[] components = new ImageComponent[NUM_IMAGES];

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// select random faces
		final VFSGroupDataset<FImage> dataset = FaceDatasetProvider.getDataset();
		for (int i = 0; i < faces.length; i++)
			faces[i] = dataset.getRandomInstance();

		// load basis
		eigen = loadEigen(dataset, "eigenbasis.bin");

		// setup display
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height - 50));
		base.setLayout(new FlowLayout());
		draw(10);
		createImageComponents(base);
		outer.add(base);

		final JPanel controls = new JPanel();
		controls.setOpaque(false);
		final JLabel label = new JLabel("Number of dimensions:");
		label.setFont(FONT);
		controls.add(label);

		final JTextField numDims = new JTextField(3);
		numDims.setFont(FONT);
		numDims.setEditable(false);
		numDims.setBorder(null);
		numDims.setText("10");

		final JSlider slider = new JSlider(0, eigen.getNumComponents(), 10);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int nd = ((JSlider) e.getSource()).getValue();
				if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
					draw(nd);
					update();
				}
				numDims.setText(nd + "");
			}
		});
		controls.add(slider);
		controls.add(numDims);
		outer.add(controls);

		return outer;
	}

	/**
	 * Load an EigenImages from a resource, or generate it if necessary
	 *
	 * @param dataset
	 * @param name
	 * @return the images
	 * @throws IOException
	 */
	public static EigenImages loadEigen(final VFSGroupDataset<FImage> dataset, String name) throws IOException {
		EigenImages eigen;
		if (EigenFaceApproximationDemo.class.getResource(name) == null) {
			eigen = new EigenImages(250);
			eigen.train(DatasetAdaptors.asList(dataset));
			IOUtils.writeBinary(new File("src/main/resources/uk/ac/soton/ecs/comp3204/l3/" + name), eigen);
		} else {
			InputStream is = null;
			try {
				is = EigenFaceApproximationDemo.class.getResourceAsStream(name);

				eigen = IOUtils.read(is, EigenImages.class);
			} finally {
				if (is != null)
					is.close();
			}
		}
		return eigen;
	}

	private void draw(int ndims) {
		for (int i = 0; i < faces.length; i++) {
			final DoubleFV vector = eigen.extractFeature(faces[i]).subvector(0, ndims);
			final FImage recon = eigen.reconstruct(vector);

			// clip the image to [0,1]
			for (int y = 0; y < recon.height; y++) {
				for (int x = 0; x < recon.width; x++) {
					if (recon.pixels[y][x] < 0)
						recon.pixels[y][x] = 0;
					if (recon.pixels[y][x] > 1)
						recon.pixels[y][x] = 1;
				}
			}

			if (images[i] == null) {
				images[i] = new FImage(faces[i].width * 2, faces[i].height);
			}
			images[i].drawImage(faces[i], 0, 0);
			images[i].drawImage(recon, faces[i].width, 0);
		}
	}

	private void update() {
		for (int i = 0; i < components.length; i++) {
			components[i].setImage(bimages[i] = ImageUtilities.createBufferedImageForDisplay(images[i], bimages[i]));
		}
	}

	private void createImageComponents(JPanel base) {
		for (int i = 0; i < components.length; i++) {
			final ImageComponent ic = new ImageComponent(true, false);
			ic.setAllowPanning(false);
			ic.setAllowZoom(false);
			ic.setShowPixelColours(false);
			ic.setShowXYPosition(false);
			ic.setImage(bimages[i] = ImageUtilities.createBufferedImageForDisplay(images[i], bimages[i]));
			base.add(ic);
			components[i] = ic;
		}
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws Exception {
		new SlideshowApplication(new EigenFaceApproximationDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
