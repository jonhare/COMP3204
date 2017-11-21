package uk.ac.soton.ecs.comp3204.l10;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
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

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

/**
 * Demonstration of KNN using simple average colour features for classify green
 * vs yellow vs red tomatoes.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "K-Nearest-Neighbour Classification Demo for Tomatoe ripeness")
public class TomatoKNNClassifierDemo implements Slide, ActionListener, VideoDisplayListener<MBFImage>, ChangeListener {
	private static final int CIRCLE_THICKNESS = 4;
	private static final int CIRCLE_SIZE = 15;
	private static final int POINT_SIZE = 10;

	private static final int GRAPH_WIDTH = 500;
	private static final int GRAPH_HEIGHT = 500;
	private static final int AXIS_WIDTH = GRAPH_WIDTH - 100;
	private static final int AXIS_HEIGHT = GRAPH_HEIGHT - 100;
	private static final int AXIS_OFFSET_X = 50;
	private static final int AXIS_OFFSET_Y = 50;
	private static final int AXIS_EXTENSION = 5;

	private static final int VIDEO_HEIGHT = 240;
	private static final int VIDEO_WIDTH = 320;

	private static final String[] CLASSES = { "RIPE", "YELLOW", "UNRIPE" };
	private static final Float[][] COLOURS = { RGBColour.RED, RGBColour.YELLOW, RGBColour.GREEN };

	private VideoCaptureComponent vc;
	private ColourSpace colourSpace = ColourSpace.H1H2;
	private JTextField featureField;
	private MBFImage image;
	private ImageComponent imageComp;
	private BufferedImage bimg;
	private JComboBox<String> classType;
	private double[] lastMean;
	private JTextField guess;

	private volatile List<double[]> points;
	private volatile List<Integer> classes;
	private volatile int k;
	private Circle circle;
	private BufferedImage bgImage;

	public TomatoKNNClassifierDemo(BufferedImage bgImage) throws IOException {
		this.bgImage = bgImage;
	}

	@Override
	public Component getComponent(final int width, final int height) throws IOException {
		points = new ArrayList<double[]>();
		classes = new ArrayList<Integer>();
		k = 1;

		circle = new Circle(VIDEO_WIDTH / 2, VIDEO_HEIGHT / 2, VIDEO_HEIGHT / 8);

		vc = new VideoCaptureComponent(VIDEO_WIDTH, VIDEO_HEIGHT);

		vc.getDisplay().addVideoListener(this);

		// the main panel
		final JPanel base = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				super.paintComponent(g);

				if (bgImage != null)
					g.drawImage(bgImage, 0, 0, width, height, null);
			}
		};
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
		rightPanel.setOpaque(false);

		guess = new JTextField(8);
		guess.setOpaque(false);
		guess.setFont(Font.decode("Monaco-48"));
		guess.setHorizontalAlignment(JTextField.CENTER);
		guess.setEditable(false);
		rightPanel.add(guess);

		image = new MBFImage(GRAPH_WIDTH, GRAPH_HEIGHT, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		imageComp = new DisplayUtilities.ImageComponent(true, false);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		rightPanel.add(imageComp);
		final JPanel classCtrlsCnt = new JPanel(new GridLayout(1, 2));
		classCtrlsCnt.setOpaque(false);

		// learning controls
		final JPanel learnCtrls = new JPanel(new GridLayout(0, 1));
		learnCtrls.setOpaque(false);
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
		classCtrls.setOpaque(false);
		final JPanel cnt = new JPanel();
		cnt.setOpaque(false);
		cnt.add(new JLabel("K:"));
		final JSpinner kField = new JSpinner(new SpinnerNumberModel(k, 1, 10, 1));
		kField.addChangeListener(this);
		cnt.add(kField);
		classCtrls.add(cnt);
		classCtrlsCnt.add(classCtrls);

		rightPanel.add(classCtrlsCnt);

		base.add(rightPanel);

		redraw();
		return base;
	}

	private void createFeatureField() {
		featureField = new JTextField();
		featureField.setOpaque(false);
		featureField.setFont(Font.decode("Monaco-24"));
		featureField.setHorizontalAlignment(JTextField.CENTER);
		featureField.setEditable(false);
		featureField.setBorder(null);
	}

	private JPanel createColourSpaceButtons() {
		final JPanel colourspacesPanel = new JPanel();
		colourspacesPanel.setOpaque(false);
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
		button.setSelected(cs == this.colourSpace);
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

	private Float[] doClassify(double[] mean) {
		if (points.size() > 0) {
			final DoubleNearestNeighboursExact nn = new DoubleNearestNeighboursExact(
					points.toArray(new double[points.size()][]));
			final List<IntDoublePair> neighbours = nn.searchKNN(mean, k);

			if (neighbours.get(0).second > 0.05) {
				guess.setText("unknown");
				return RGBColour.MAGENTA;
			}

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
				return COLOURS[indices[0]];
			}
		}
		guess.setText("unknown");
		return RGBColour.MAGENTA;
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

	/**
	 * Compute the mean of the image
	 *
	 * @param frame
	 * @param colourSpace
	 * @return
	 */
	public double[] computeMean(MBFImage frame, ColourSpace colourSpace) {
		final Circle hc = circle.clone();
		hc.scale(0.5f);
		final Rectangle bounds = hc.calculateRegularBoundingBox();

		frame = ResizeProcessor.halfSize(frame);

		final MBFImage cvt = colourSpace.convert(frame);
		final double[] vector = new double[colourSpace.getNumBands()];

		final SummaryStatistics stats = new SummaryStatistics();
		final Pixel pt = new Pixel();
		for (int b = 0; b < colourSpace.getNumBands(); b++) {
			stats.clear();

			final float[][] pix = cvt.getBand(b).pixels;

			for (pt.y = (int) bounds.y; pt.y < bounds.y + bounds.height; pt.y++) {
				for (pt.x = (int) bounds.x; pt.x < bounds.x + bounds.width; pt.x++) {
					if (hc.isInside(pt)) {
						stats.addValue(pix[pt.y][pt.x]);
					}
				}
			}

			vector[b] = stats.getMean();
		}
		return vector;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		frame.flipX();

		lastMean = computeMean(frame, colourSpace);
		featureField.setText(formatVector(lastMean));

		redraw();
		final Float[] col = doClassify(lastMean);

		frame.createRenderer(RenderHints.ANTI_ALIASED).drawShape(circle, 3, col);
	}

	/**
	 * Format vector as a string
	 *
	 * @param vector
	 * @return
	 */
	public static String formatVector(double[] vector) {
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(String.format("%1.3f", vector[0]));
		for (int i = 1; i < vector.length; i++)
			sb.append(String.format(", %1.3f", vector[i]));
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.k = (Integer) ((JSpinner) e.getSource()).getValue();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new TomatoKNNClassifierDemo(Utils.BACKGROUND_IMAGE), 1024, 768);
	}
}
