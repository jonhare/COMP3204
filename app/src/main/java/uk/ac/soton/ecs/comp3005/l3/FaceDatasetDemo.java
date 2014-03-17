package uk.ac.soton.ecs.comp3005.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Visualise the ATT face dataset
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demonstration(title = "Face dataset demo")
public class FaceDatasetDemo implements Slide {
	public static class FaceDatasetProvider {
		static File tmpFile;
		static {
			try {
				tmpFile = File.createTempFile("faces", ".zip");
				FileUtils.copyURLToFile(FaceDatasetDemo.class.getResource("att_faces.zip"), tmpFile);
				tmpFile.deleteOnExit();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public static VFSGroupDataset<FImage> getDataset() throws FileSystemException {
			return new VFSGroupDataset<FImage>("zip:" + tmpFile.toURI(), ImageUtilities.FIMAGE_READER);
		}
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final VFSGroupDataset<FImage> dataset = FaceDatasetProvider.getDataset();

		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height - 50));
		base.setLayout(new FlowLayout());

		for (int i = 0; i < 60; i++) {
			final FImage img = dataset.getRandomInstance();
			final ImageComponent ic = new ImageComponent(true, false);
			ic.setAllowPanning(false);
			ic.setAllowZoom(false);
			ic.setShowPixelColours(false);
			ic.setShowXYPosition(false);
			ic.setImage(ImageUtilities.createBufferedImageForDisplay(img));
			base.add(ic);
		}
		outer.add(base);

		return outer;
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new FaceDatasetDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
