package uk.ac.soton.ecs.comp3005.l4;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;

import uk.ac.soton.ecs.comp3005.utils.Utils;

public class FHSegmentationDemo implements Slide {

	private MBFImage oimage;
	private BufferedImage bimg;
	private ImageComponent ic;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new GridBagLayout());

		oimage = ImageUtilities.readMBF(this.getClass().getResource("beach.gif"));
		bimg = ImageUtilities.createBufferedImageForDisplay(oimage, bimg);

		ic = new ImageComponent(false, false);
		ic.setPreferredSize(new Dimension(400, 600));
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.setImage(bimg);
		ic.zoom(2.0);

		final GridBagConstraints gbc = new GridBagConstraints();
		base.add(ic, gbc);

		final JPanel ctlsPnl = new JPanel(new GridBagLayout());
		ctlsPnl.setOpaque(false);

		final JButton segmentBtn = new JButton("Perform segmentation");
		ctlsPnl.add(segmentBtn);

		gbc.gridy = 1;
		base.add(ctlsPnl, gbc);

		segmentBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				segmentBtn.setEnabled(false);

				new Thread(new Runnable() {
					@Override
					public void run() {
						ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(
								segmentImage(oimage),
								bimg));
						segmentBtn.setEnabled(true);
					}
				}).start();
			}
		});

		return base;
	}

	protected MBFImage segmentImage(MBFImage oimage) {
		final FelzenszwalbHuttenlocherSegmenter<MBFImage> seg = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();

		final List<? extends PixelSet> result = seg.segment(oimage);
		return SegmentationUtilities.renderSegments(oimage.getWidth(), oimage.getHeight(), result);
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new FHSegmentationDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
