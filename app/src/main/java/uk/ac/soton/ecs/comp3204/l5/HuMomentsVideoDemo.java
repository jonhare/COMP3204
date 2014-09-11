package uk.ac.soton.ecs.comp3204.l5;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.connectedcomponent.proc.HuMoments;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.WrapLayout;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

/**
 * Hum Moments of the largest CC from a video
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demonstration(title = "Hu Moments Demo")
public class HuMomentsVideoDemo implements Slide {
	final static Font FONT = Font.decode("Monaco-24");
	protected VideoCaptureComponent vc;
	protected volatile float threshold = 0;
	protected volatile boolean otsu = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openimaj.content.slideshow.Slide#getComponent(int, int)
	 */
	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
		vc = new VideoCaptureComponent(640, 480);
		base.add(vc);

		final JPanel container = new JPanel();
		container.setOpaque(false);

		final JTextField valueField = new JTextField(4);
		valueField.setOpaque(false);
		valueField.setHorizontalAlignment(JTextField.RIGHT);
		valueField.setFont(FONT);
		valueField.setEditable(false);
		valueField.setBorder(null);
		valueField.setText("0.00");

		final JLabel label = new JLabel("Threshold:");
		label.setFont(FONT);
		container.add(label);

		final JSlider slider = new JSlider(0, 255, 0);
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width + 250, slider.getPreferredSize().height));

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				threshold = slider.getValue() / 255f;
				valueField.setText(String.format("%1.2f", threshold));
			}
		});

		container.add(slider);
		container.add(valueField);

		container.add(new JSeparator(SwingConstants.VERTICAL));

		final JLabel label2 = new JLabel("Otsu:");
		label2.setFont(FONT);
		container.add(label2);

		final JCheckBox cb = new JCheckBox();
		container.add(cb);

		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				otsu = cb.isSelected();
				if (otsu) {
					slider.setEnabled(false);
				} else {
					slider.setEnabled(true);
				}
			}
		});
		cb.setSelected(otsu);
		base.add(container);

		final JPanel fvField = new JPanel(new WrapLayout());
		fvField.setSize(width, 1);

		final JTextField[] mFields = new JTextField[7];
		for (int i = 0; i < mFields.length; i++)
			mFields[i] = makeMomentField(i, fvField);

		base.add(fvField);

		outer.add(base);

		vc.getDisplay().addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage grey = frame.flatten();

				if (otsu) {
					threshold = OtsuThreshold.calculateThreshold(grey, 256);
					slider.setValue((int) (threshold * 255f));
				}
				final FImage tf = grey.threshold(threshold).inverse();

				final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_8);
				ccl.analyseImage(tf);
				final List<ConnectedComponent> comps = ccl.getComponents();

				ConnectedComponent best = null;
				float bestScore = 0;
				final Pixel imageCentre = new Pixel(frame.getWidth() / 2, frame.getHeight() / 2);
				for (final ConnectedComponent cc : comps) {
					final Pixel centroid = cc.calculateCentroidPixel();
					final float distanceFromCentre = (float) Line2d.distance(imageCentre, centroid);
					final float score = cc.calculateArea() / distanceFromCentre;

					if (score > bestScore) {
						bestScore = score;
						best = cc;
					}
				}

				final HuMoments hm = new HuMoments();
				hm.process(best);
				final double[] v = hm.getFeatureVectorArray();
				for (int i = 0; i < 7; i++) {
					mFields[i].setText(String.format("%2.2e", v[i]));
				}

				for (final Pixel p : best.pixels) {
					frame.setPixel(p.x, p.y, RGBColour.RED);
				}

			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// do nothing
			}
		});

		return outer;
	}

	private JTextField makeMomentField(int i, JPanel fvField) {
		final JPanel p = new JPanel();

		final JLabel l = new JLabel("M" + (i + 1) + "=");
		l.setFont(FONT);
		p.add(l);

		final JTextField t = new JTextField(9);
		t.setFont(FONT);
		t.setEditable(false);
		t.setHorizontalAlignment(JTextField.RIGHT);
		p.add(t);

		fvField.add(p);

		return t;
	}

	@Override
	public void close() {
		vc.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new HuMomentsVideoDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
