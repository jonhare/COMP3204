package uk.ac.soton.ecs.comp3005.utils;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Closeable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * A reusable swing component for video input
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoCaptureComponent extends Box implements ItemListener, Closeable {
	private static final long serialVersionUID = 1L;
	private VideoDisplay<MBFImage> display;
	private JComboBox sources;
	private int width;
	private int height;
	private Device currentDevice;

	/**
	 * Cosntruct with the given dimensions
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @throws VideoCaptureException
	 */
	public VideoCaptureComponent(int width, int height) throws VideoCaptureException {
		super(BoxLayout.Y_AXIS);

		this.setOpaque(false);

		this.width = width;
		this.height = height;
		final List<Device> devices = VideoCapture.getVideoDevices();
		currentDevice = devices.get(0);

		final VideoCapture vc = new VideoCapture(width, height, currentDevice);
		final JPanel videoDisplayPanel = new JPanel();
		videoDisplayPanel.setOpaque(false);
		display = VideoDisplay.createVideoDisplay(vc, videoDisplayPanel);
		add(videoDisplayPanel);

		final JPanel sourcesPanel = new JPanel();
		sourcesPanel.setOpaque(false);
		sources = new JComboBox();
		sources.setOpaque(false);
		for (final Device s : devices)
			sources.addItem(s.getNameStr());
		sources.addItemListener(this);
		sourcesPanel.add(sources);
		add(sourcesPanel);
	}

	/**
	 * Get the underlying video display
	 * 
	 * @return the display
	 */
	public VideoDisplay<MBFImage> getDisplay() {
		return display;
	}

	@Override
	public void close() {
		this.display.close();
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			final String item = (String) event.getItem();
			final Device d = VideoCapture.getVideoDevices().get(sources.getSelectedIndex());
			if (d.getNameStr().equals(item) && !currentDevice.equals(d)) {
				try {
					currentDevice = d;
					display.setMode(Mode.STOP);
					display.getVideo().close();
					display.changeVideo(new VideoCapture(width, height, currentDevice));
					display.setMode(Mode.PLAY);
				} catch (final VideoCaptureException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
