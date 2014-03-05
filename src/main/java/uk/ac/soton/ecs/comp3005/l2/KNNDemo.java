package uk.ac.soton.ecs.comp3005.l2;

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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

import uk.ac.soton.ecs.comp3005.l1.ColourSpacesDemo;
import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Demonstration of KNN classification using simple average colour features
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "K-Nearest-Neighbour Classification Demo")
public class KNNDemo implements Slide, ActionListener, VideoDisplayListener<MBFImage>, ChangeListener {
	private static final int CIRCLE_THICKNESS = 4;
	private static final int CIRCLE_SIZE = 15;
	private static final int POINT_SIZE = 10;
	private static final int VIDEO_HEIGHT = 240;
	private static final int VIDEO_WIDTH = 320;

	private static final int GRAPH_WIDTH = 600;
	private static final int GRAPH_HEIGHT = 600;
	private static final int AXIS_WIDTH = 500;
	private static final int AXIS_HEIGHT = 500;
	private static final int AXIS_OFFSET_X = 50;
	private static final int AXIS_OFFSET_Y = 50;
	private static final int AXIS_EXTENSION = 5;

	private static final String[] CLASSES = { "RED", "BLUE" };
	private static final Float[][] COLOURS = { RGBColour.RED, RGBColour.BLUE };

	private VideoCaptureComponent vc;
	private ColourSpace colourSpace = ColourSpace.HS;
	private JTextField featureField;
	private MBFImage image;
	private ImageComponent imageComp;
	private BufferedImage bimg;
	private JComboBox<String> classType;
	private double[] lastMean;
	private JTextField guess;

	private volatile List<double[]> points = new ArrayList<double[]>();
	private volatile List<Integer> classes = new ArrayList<Integer>();
	private volatile int k = 1;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		vc = new VideoCaptureComponent(VIDEO_WIDTH, VIDEO_HEIGHT);
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
		image = new MBFImage(GRAPH_WIDTH, GRAPH_HEIGHT, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		imageComp = new DisplayUtilities.ImageComponent(true);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
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
		final JSpinner kField = new JSpinner(new SpinnerNumberModel(k, 1, 10, 1));
		kField.addChangeListener(this);
		cnt.add(kField);
		classCtrls.add(cnt);
		guess = new JTextField(8);
		guess.setFont(Font.decode("Monaco-24"));
		guess.setHorizontalAlignment(JTextField.CENTER);
		guess.setEditable(false);
		classCtrls.add(guess);
		classCtrlsCnt.add(classCtrls);

		rightPanel.add(classCtrlsCnt);

		base.add(rightPanel);

		redraw();
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
			this.classes.clear();
			this.points.clear();
		} else if (rawcmd.startsWith("button.")) {
			if (cmd.equals("learn")) {
				doLearn(lastMean, this.classType.getSelectedIndex());
			}
		}
	}

	private void doClassify(double[] mean) {
		if (points.size() > 0) {
			final DoubleNearestNeighboursExact nn = new DoubleNearestNeighboursExact(
					points.toArray(new double[points.size()][]));
			final List<IntDoublePair> neighbours = nn.searchKNN(mean, k);

			final int[] counts = new int[CLASSES.length];
			for (final IntDoublePair p : neighbours) {
				counts[this.classes.get(p.first)]++;

				final double[] pt = this.points.get(p.first);
				final Point2dImpl pti = projectPoint(pt);
				image.drawPoint(pti, RGBColour.MAGENTA, POINT_SIZE);

				image.drawShape(new Circle(pti, CIRCLE_SIZE), CIRCLE_THICKNESS, RGBColour.GREEN);
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

	/**
	 * Project a point to draw it on the graph
	 * 
	 * @param pt
	 *            the point
	 * @return the projected point in image coords
	 */
	private Point2dImpl projectPoint(final double[] pt) {
		final Point2dImpl pti = new Point2dImpl();

		pti.x = AXIS_OFFSET_X + (float) (AXIS_WIDTH * pt[0]);
		if (pt.length == 2)
			pti.y = (AXIS_OFFSET_Y + AXIS_HEIGHT) - (float) (AXIS_HEIGHT * pt[1]);
		else
			pti.y = AXIS_OFFSET_Y + AXIS_HEIGHT;

		return pti;
	}

	private void doLearn(double[] mean, int clazz) {
		this.points.add(mean);
		this.classes.add(clazz);
		redraw();
	}

	private void redraw() {
		this.image.fill(RGBColour.WHITE);

		// draw saved points
		for (int i = 0; i < points.size(); i++) {
			final double[] pt = points.get(i);
			image.drawPoint(projectPoint(pt), COLOURS[classes.get(i)], POINT_SIZE);
		}

		// draw current point
		if (lastMean != null) {
			image.drawPoint(projectPoint(lastMean), RGBColour.MAGENTA, POINT_SIZE);
		}

		// draw y-axes
		image.drawLine(AXIS_OFFSET_X, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X, AXIS_OFFSET_Y + AXIS_HEIGHT
				+ AXIS_EXTENSION, RGBColour.BLACK);
		image.drawLine(AXIS_OFFSET_X + 1 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 1 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 2 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 2 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 3 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 3 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 4 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 4 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);

		// draw x-axes
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + AXIS_HEIGHT,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + AXIS_HEIGHT, RGBColour.BLACK);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 0 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 0 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 1 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 1 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 2 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 2 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 3 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 3 * AXIS_HEIGHT / 4, RGBColour.GRAY);

		// update the image
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

	@Override
	public void stateChanged(ChangeEvent e) {
		this.k = (Integer) ((JSpinner) e.getSource()).getValue();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new KNNDemo(), 1024, 768);
	}
}
