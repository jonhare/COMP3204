package uk.ac.soton.ecs.comp3005.l1;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Demonstrate different colour spaces
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "Colour Spaces Demo")
public class ColourSpacesDemo extends SimpleCameraDemo implements Slide, VideoDisplayListener<MBFImage>, ActionListener {
	protected ColourSpace[] colourSpaces;
	protected volatile ColourSpace colourSpace = ColourSpace.RGB;

	protected ColourSpacesDemo() {
		this.colourSpaces = new ColourSpace[] {
				ColourSpace.RGB, ColourSpace.HSV, ColourSpace.HUE, ColourSpace.H1H2
		};
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel base = super.getComponent(width, height);

		// add a listener for new video frames
		vc.getDisplay().addVideoListener(this);

		// the controls panel
		final JPanel controlsPanel = new JPanel(new GridLayout(0, 1));
		final ButtonGroup group = new ButtonGroup();

		for (final ColourSpace c : colourSpaces)
			createRadioButton(controlsPanel, group, c);

		base.add(controlsPanel);

		return base;
	}

	/**
	 * Create a radio button
	 * 
	 * @param controlsPanel
	 *            the panel to add the button too
	 * @param group
	 *            the radio button group
	 * @param cs
	 *            the colourSpace that the button represents
	 */
	private void createRadioButton(final JPanel controlsPanel, final ButtonGroup group, final ColourSpace cs) {
		final String name = cs.name();
		final JRadioButton button = new JRadioButton(name);
		button.setActionCommand(name);
		controlsPanel.add(button);
		group.add(button);
		button.setSelected(cs == ColourSpace.RGB);
		button.addActionListener(this);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// Do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		convertColours(frame, colourSpace);
	}

	/**
	 * Convert the colour space of an image, maintaining three bands so it can
	 * be displayed
	 * 
	 * @param frame
	 * @param colourSpace
	 */
	public static void convertColours(MBFImage frame, ColourSpace colourSpace) {
		// update the frame from the camera by converting to the selected colour
		// space before display
		final MBFImage cvt = colourSpace.convert(frame);

		// if the converted image has fewer than 3 bands, add more so it can be
		// displayed as RGB.
		if (cvt.numBands() == 1) {
			// this makes a three-band grey-level image, where all the bands are
			// the same
			cvt.bands.add(cvt.getBand(0).clone());
			cvt.bands.add(cvt.getBand(0).clone());
		} else if (cvt.numBands() == 2) {
			// this adds a third zero band to a two-band image
			cvt.bands.add(new FImage(cvt.getWidth(), cvt.getHeight()));
		}

		// this sets the frame to the colour converted version
		frame.internalAssign(cvt);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// change the colour space to the one selected
		this.colourSpace = ColourSpace.valueOf(e.getActionCommand());
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new ColourSpacesDemo(), 1024, 768);
	}
}
