package uk.ac.soton.ecs.comp3005.l7;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

@Demonstration(title = "Gradients and Orientations")
public class GradOriDemo implements Slide, VideoDisplayListener<MBFImage> {
	private FSobel sobel = new FSobel();
	private VideoCaptureComponent vc;
	private FImage dyImage;
	private ImageComponent dyImageIC;
	private BufferedImage dyImageB;
	private FImage oriImage;
	private BufferedImage oriImageB;
	private ImageComponent oriImageIC;
	private FImage magImage;
	private BufferedImage magImageB;
	private ImageComponent magImageIC;

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final FImage f = frame.flatten();

		f.analyseWith(sobel);
		sobel.dx.normalise();
		dyImage = sobel.dy.normalise();

		FImageGradients.gradientMagnitudesAndOrientations(f, magImage, oriImage);
		magImage.normalise();
		oriImage.normalise();

		frame.internalAssign(sobel.dx.toRGB());
		dyImageIC.setImage(dyImageB = ImageUtilities.createBufferedImage(dyImage));
		magImageIC.setImage(magImageB = ImageUtilities.createBufferedImage(magImage));
		oriImageIC.setImage(oriImageB = ImageUtilities.createBufferedImage(oriImage));
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new GridLayout(2, 2));

		vc = new VideoCaptureComponent(320, 240);
		vc.setBorder(BorderFactory.createTitledBorder("dx"));
		base.add(vc);

		final JPanel dyImageContainer = new JPanel();
		dyImage = new FImage(320, 240);
		dyImageB = ImageUtilities.createBufferedImageForDisplay(dyImage);
		dyImageIC = new ImageComponent(dyImageB);
		dyImageIC.setShowPixelColours(false);
		dyImageIC.setShowXYPosition(false);
		dyImageIC.setAllowPanning(false);
		dyImageIC.setAllowZoom(false);
		dyImageContainer.setBorder(BorderFactory.createTitledBorder("dy"));
		dyImageContainer.add(dyImageIC);
		base.add(dyImageContainer);

		final JPanel oriImageContainer = new JPanel();
		oriImage = new FImage(320, 240);
		oriImageB = ImageUtilities.createBufferedImageForDisplay(oriImage);
		oriImageIC = new ImageComponent(oriImageB);
		oriImageIC.setShowPixelColours(false);
		oriImageIC.setShowXYPosition(false);
		oriImageIC.setAllowPanning(false);
		oriImageIC.setAllowZoom(false);
		oriImageContainer.setBorder(BorderFactory.createTitledBorder("Orientation"));
		oriImageContainer.add(oriImageIC);
		base.add(oriImageContainer);

		final JPanel magImageContainer = new JPanel();
		magImage = new FImage(320, 240);
		magImageB = ImageUtilities.createBufferedImageForDisplay(magImage);
		magImageIC = new ImageComponent(magImageB);
		magImageIC.setShowPixelColours(false);
		magImageIC.setShowXYPosition(false);
		magImageIC.setAllowPanning(false);
		magImageIC.setAllowZoom(false);
		magImageContainer.setBorder(BorderFactory.createTitledBorder("Magnitude"));
		magImageContainer.add(magImageIC);
		base.add(magImageContainer);

		outer.add(base);

		vc.getDisplay().addVideoListener(this);

		return outer;
	}

	@Override
	public void close() {
		vc.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new GradOriDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
