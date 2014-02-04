package uk.ac.soton.ecs.comp3005;

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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;

public class KMeansDemo implements Slide, ActionListener, VideoDisplayListener<MBFImage> {

	private static final String[] CLASSES = { "RED", "BLUE" };
	private Float[][] COLOURS = { RGBColour.RED, RGBColour.BLUE };

	private VideoCaptureComponent vc;
	private ColourSpace colourSpace = ColourSpace.HS;
	private JTextField featureField;
	private MBFImage image;
	private ImageComponent imageComp;
	private BufferedImage bimg;
	private JTextField kField;
	private List<double[]> points = new ArrayList<double[]>();
	private List<Integer> classes = new ArrayList<Integer>();
	private JComboBox<String> classType;
	private double[] lastMean;
	private JTextField guess;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		vc = new VideoCaptureComponent(320, 240);
		vc.getDisplay().addVideoListener(this);

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		// left hand side (video, features)
		final Box videoCtrls = Box.createVerticalBox();
		videoCtrls.add(vc);
		videoCtrls.add(Box.createVerticalStrut(10));
		final JPanel colourspacesPanel = createColourSpaceButtons();
		videoCtrls.add(colourspacesPanel);
		createFeatureField();
		videoCtrls.add(Box.createVerticalStrut(10));
		videoCtrls.add(featureField);
		base.add(videoCtrls);

		// right hand box
		final Box rightPanel = Box.createVerticalBox();
		image = new MBFImage(400, 400, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		imageComp = new DisplayUtilities.ImageComponent(true);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		redraw();
		rightPanel.add(imageComp);
		final JPanel classCtrlsCnt = new JPanel(new GridLayout(1, 2));

		// learning controls
		final JPanel learnCtrls = new JPanel(new GridLayout(0, 1));
		classType = new JComboBox<String>();
		for (final String c : CLASSES)
			classType.addItem(c);
		learnCtrls.add(classType);
		final JButton learnButton = new JButton("Learn");
		learnButton.setActionCommand("button.learn");
		learnButton.addActionListener(this);
		learnCtrls.add(learnButton);
		classCtrlsCnt.add(learnCtrls);

		// classification controls
		final JPanel classCtrls = new JPanel(new GridLayout(0, 1));
		final JPanel cnt = new JPanel();
		cnt.add(new JLabel("K:"));
		cnt.add(kField = new JTextField(2));
		kField.setText("1");
		classCtrls.add(cnt);
		guess = new JTextField(8);
		guess.setFont(Font.decode("Monaco-24"));
		guess.setHorizontalAlignment(JTextField.CENTER);
		guess.setEditable(false);
		classCtrls.add(guess);
		classCtrlsCnt.add(classCtrls);

		rightPanel.add(classCtrlsCnt);

		base.add(rightPanel);

		return base;
	}

	private void createFeatureField() {
		featureField = new JTextField();
		featureField.setFont(Font.decode("Monaco-24"));
		featureField.setHorizontalAlignment(JTextField.CENTER);
		featureField.setEditable(false);
		featureField.setBorder(null);
	}

	private JPanel createColourSpaceButtons() {
		final JPanel colourspacesPanel = new JPanel();
		colourspacesPanel.setLayout(new BoxLayout(colourspacesPanel, BoxLayout.X_AXIS));
		colourspacesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		final ButtonGroup group = new ButtonGroup();
		createRadioButton(colourspacesPanel, group, ColourSpace.HUE);
		createRadioButton(colourspacesPanel, group, ColourSpace.HS);
		createRadioButton(colourspacesPanel, group, ColourSpace.H1H2);
		return colourspacesPanel;
	}

	/**
	 * Create a radio button
	 * 
	 * @param colourspacesPanel
	 *            the panel to add the button too
	 * @param group
	 *            the radio button group
	 * @param cs
	 *            the colourSpace that the button represents
	 */
	private void createRadioButton(final JPanel colourspacesPanel, final ButtonGroup group, final ColourSpace cs) {
		final String name = cs.name();
		final JRadioButton button = new JRadioButton(name);
		button.setActionCommand("ColourSpace." + name);
		colourspacesPanel.add(button);
		group.add(button);
		button.setSelected(cs == ColourSpace.HS);
		button.addActionListener(this);
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String rawcmd = e.getActionCommand();
		final String cmd = rawcmd.substring(rawcmd.indexOf(".") + 1);

		if (rawcmd.startsWith("ColourSpace.")) {
			// change the colour space to the one selected
			this.colourSpace = ColourSpace.valueOf(cmd);
		} else if (rawcmd.startsWith("button.")) {
			if (cmd.equals("learn")) {
				doLearn(lastMean, this.classType.getSelectedIndex());
			}
		}
	}

	private void doClassify(double[] mean) {
		if (points.size() > 0) {
			final int k = Integer.parseInt(kField.getText());

			final DoubleNearestNeighboursExact nn = new DoubleNearestNeighboursExact(
					points.toArray(new double[points.size()][]));
			final List<IntDoublePair> neighbours = nn.searchKNN(mean, k);

			final int[] counts = new int[CLASSES.length];
			for (final IntDoublePair p : neighbours) {
				counts[this.classes.get(p.first)]++;

				final double[] pt = this.points.get(p.first);
				final Point2dImpl pti = new Point2dImpl();
				pti.x = 50 + (float) (300 * pt[0]);
				if (pt.length == 2)
					pti.y = 350 - (float) (300 * pt[1]);
				image.drawPoint(pti, RGBColour.MAGENTA, 3);

				image.drawShape(new Circle(pti, 5), RGBColour.GREEN);
			}
			imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));

			final int[] indices = ArrayUtils.range(0, counts.length - 1);
			ArrayUtils.parallelQuicksortDescending(counts, indices);

			if (counts.length == 1 || counts[0] > counts[1]) {
				guess.setText(this.classType.getItemAt(indices[0]));
				return;
			}
		}
		guess.setText("unknown");
	}

	private void doLearn(double[] mean, int clazz) {
		this.points.add(mean);
		this.classes.add(clazz);
		redraw();
	}

	private void redraw() {
		this.image.fill(RGBColour.WHITE);

		// draw saved points
		final Point2dImpl pti = new Point2dImpl();
		for (int i = 0; i < points.size(); i++) {
			final double[] pt = points.get(i);

			pti.x = 50f + (float) (300 * pt[0]);
			if (pt.length == 2)
				pti.y = 350 - (float) (300 * pt[1]);

			image.drawPoint(pti, COLOURS[classes.get(i)], 3);
		}

		// draw current point
		if (lastMean != null) {
			pti.x = 50 + (float) (300 * lastMean[0]);
			if (lastMean.length == 2)
				pti.y = 350 - (float) (300 * lastMean[1]);
			image.drawPoint(pti, RGBColour.MAGENTA, 3);
		}

		image.drawLine(50, 45, 50, 355, RGBColour.BLACK);
		image.drawLine(125, 50, 125, 355, RGBColour.GRAY);
		image.drawLine(200, 50, 200, 355, RGBColour.GRAY);
		image.drawLine(275, 50, 275, 355, RGBColour.GRAY);
		image.drawLine(350, 50, 350, 355, RGBColour.GRAY);
		image.drawLine(45, 350, 355, 350, RGBColour.BLACK);
		image.drawLine(45, 50, 350, 50, RGBColour.GRAY);
		image.drawLine(45, 125, 350, 125, RGBColour.GRAY);
		image.drawLine(45, 200, 350, 200, RGBColour.GRAY);
		image.drawLine(45, 275, 350, 275, RGBColour.GRAY);

		imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		ColourSpacesDemo.convertColours(frame, colourSpace);

		lastMean = SimpleMeanColourFeatureDemo.computeMean(frame, colourSpace);
		featureField.setText(SimpleMeanColourFeatureDemo.formatVector(lastMean));

		redraw();
		doClassify(lastMean);
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new KMeansDemo(), 1024, 768);
	}
}
