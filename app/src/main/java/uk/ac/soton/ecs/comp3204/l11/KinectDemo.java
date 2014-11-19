package uk.ac.soton.ecs.comp3204.l11;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.hardware.kinect.KinectController;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

import uk.ac.soton.ecs.comp3204.utils.Utils;

public class KinectDemo extends Video<MBFImage> implements Slide, ActionListener {
	private static final String DEPTH = "DEPTH";
	private static final String IR = "IR";
	private static final String RGB = "RGB";
	private KinectController controller;
	private boolean depth = false;
	private MBFImage currentFrame;
	private VideoDisplay<MBFImage> display;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		try {
			controller = new KinectController(0, false, false);
			currentFrame = new MBFImage(640, 480, 3);

			final JPanel videoDisplayPanel = new JPanel();
			videoDisplayPanel.setOpaque(false);
			display = VideoDisplay.createVideoDisplay(this, videoDisplayPanel);
			base.add(videoDisplayPanel);

			final JPanel sourcesPanel = new JPanel();
			sourcesPanel.setOpaque(false);

			final JRadioButton rgbButton = new JRadioButton(RGB);
			rgbButton.setActionCommand(RGB);
			rgbButton.setSelected(true);
			rgbButton.addActionListener(this);
			sourcesPanel.add(rgbButton);

			final JRadioButton irButton = new JRadioButton(IR);
			irButton.setActionCommand(IR);
			irButton.addActionListener(this);
			sourcesPanel.add(irButton);

			final JRadioButton depthButton = new JRadioButton(DEPTH);
			depthButton.setActionCommand(DEPTH);
			depthButton.addActionListener(this);
			sourcesPanel.add(depthButton);

			// Group the radio buttons.
			final ButtonGroup group = new ButtonGroup();
			group.add(rgbButton);
			group.add(irButton);
			group.add(depthButton);

			base.add(sourcesPanel);
		} catch (final Throwable e) {
			// unable to connect to kinect...
			base.add(new JLabel("Unable to open Kinect"));
			return base;
		}

		return base;
	}

	@Override
	public void close() {
		if (display != null) {
			final VideoDisplay<MBFImage> tmp = display;
			display = null;
			tmp.close();
			controller.close();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == RGB) {
			depth = false;
			controller.setIRMode(false);
		} else if (e.getActionCommand() == IR) {
			depth = false;
			controller.setIRMode(true);
		} else if (e.getActionCommand() == DEPTH) {
			depth = true;
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new KinectDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

	@Override
	public MBFImage getNextFrame() {
		currentFrame.fill(RGBColour.BLACK);

		if (!depth) {
			MBFImage vid;
			final Image<?, ?> tmp = controller.videoStream.getNextFrame();

			if (tmp instanceof MBFImage) {
				vid = (MBFImage) tmp;
			} else {
				vid = new MBFImage((FImage) tmp, (FImage) tmp, (FImage) tmp);
			}

			currentFrame.drawImage(vid, 0, 0);
		} else {
			final FImage depthImage = controller.depthStream.getNextFrame().divide(2048f);
			currentFrame.drawImage(depthImage.toRGB(), 0, 0);
		}

		return currentFrame;
	}

	@Override
	public MBFImage getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return currentFrame.getWidth();
	}

	@Override
	public int getHeight() {
		return currentFrame.getHeight();
	}

	@Override
	public long getTimeStamp() {
		return (long) (super.currentFrame * 1000 / getFPS());
	}

	@Override
	public double getFPS() {
		return 30;
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		// nothing
	}
}
