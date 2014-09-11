package uk.ac.soton.ecs.comp3204.l7;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Matching using threshold on ratio of distances to the 2 nearest neighbours")
public class MatchingDemo implements Slide, VideoDisplayListener<MBFImage>, ActionListener {

	private static final Font FONT = Font.decode("Monaco-28");
	private VideoCaptureComponent vc;
	private MBFImage modelFrameC;
	private MBFImage lastDrawnFrame;
	private DoGSIFTEngine engine = new DoGSIFTEngine();
	private LocalFeatureList<Keypoint> modelKeys;
	private FastBasicKeypointMatcher<Keypoint> matcher = new FastBasicKeypointMatcher<Keypoint>(10000);

	@Override
	public Component getComponent(int width, int height) throws IOException {
		engine.getOptions().setDoubleInitialImage(false);

		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		vc = new VideoCaptureComponent(320, 240);
		vc.getDisplay().getScreen().setPreferredSize(new Dimension(640, 240));

		vc.getDisplay().addVideoListener(this);
		base.add(vc);

		final JPanel controls1 = new JPanel();
		controls1.setOpaque(false);
		final JButton grab = new JButton("Grab");
		grab.setActionCommand("grab");
		grab.addActionListener(this);
		grab.setFont(FONT);
		controls1.add(grab);
		base.add(controls1);

		final JPanel controls = new JPanel();
		controls.setOpaque(false);
		final JLabel label = new JLabel("Threshold:");
		label.setFont(FONT);
		controls.add(label);
		final JSlider slider = new JSlider(0, 10, 8);
		matcher.setThreshold(slider.getValue());
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width + 250, slider.getPreferredSize().height));
		controls.add(slider);
		final JTextField tf = new JTextField(5);
		tf.setFont(FONT);
		tf.setEnabled(false);
		tf.setText(slider.getValue() + "");
		controls.add(tf);

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				tf.setText(slider.getValue() + "");
				matcher.setThreshold(slider.getValue());
			}
		});

		base.add(controls);

		outer.add(base);

		return outer;
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final MBFImage currentFrameC = frame.extractROI(0, 0, 320, 240);
		if (frame.getWidth() != 640) {
			final MBFImage out = new MBFImage(320 * 2, 240, 3);
			out.drawImage(currentFrameC, 0, 0);
			frame.internalAssign(out);
		}

		synchronized (this) {
			lastDrawnFrame = currentFrameC;
			if (modelFrameC != null) {
				frame.drawImage(modelFrameC, 320, 0);

				final LocalFeatureList<Keypoint> currentKeys = engine.findFeatures(currentFrameC.flatten());
				matcher.findMatches(currentKeys);
				for (final Pair<Keypoint> match : matcher.getMatches()) {
					final Keypoint pt1 = match.firstObject();
					final Keypoint pt2 = match.secondObject();
					frame.drawLine(pt1, pt2, 1, RGBColour.BLUE);
					frame.drawPoint(pt2, RGBColour.BLUE, 3);
					frame.drawPoint(pt1, RGBColour.BLUE, 3);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("grab")) {
			synchronized (this) {
				this.modelFrameC = lastDrawnFrame.clone();
				if (modelFrameC != null) {
					this.modelKeys = engine.findFeatures(modelFrameC.flatten());
					for (final Keypoint k : this.modelKeys)
						k.x += 320;
					matcher.setModelFeatures(modelKeys);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new MatchingDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

}
