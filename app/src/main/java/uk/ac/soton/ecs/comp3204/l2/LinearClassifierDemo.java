package uk.ac.soton.ecs.comp3204.l2;

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
import java.util.HashSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.l1.ColourSpacesDemo;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

/**
 * Demonstration of Linear classification (with the Perceptron) using simple
 * average colour features
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "Perceptron Linear Classification Demo")
public class LinearClassifierDemo implements Slide, ActionListener, VideoDisplayListener<MBFImage> {
	/**
	 * A really simple perceptron
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	private static class SimplePerceptron {
		double alpha = 0.01;
		double[] w;

		/**
		 * Train the perceptron
		 * 
		 * @param pts
		 *            the data points (2d)
		 * @param classes
		 *            the classes (0/1)
		 */
		public void train(List<double[]> pts, List<Integer> classes) {
			this.w = new double[] { 1, 0, 0 };

			for (int i = 0; i < 1000; i++) {
				iteration(pts, classes);

				final double error = error(pts, classes);
				if (error < 0.01)
					break;
			}
		}

		private double error(List<double[]> pts, List<Integer> classes) {
			double error = 0;

			for (int i = 0; i < pts.size(); i++) {
				error += Math.abs(predict(pts.get(i)) - classes.get(i));
			}

			return error / pts.size();
		}

		private void iteration(List<double[]> pts, List<Integer> classes) {
			for (int i = 0; i < pts.size(); i++)
				update(pts.get(i), classes.get(i));
		}

		private void update(double[] pt, int clazz) {
			final int y = predict(pt);

			w[0] = w[0] + alpha * (clazz - y);
			w[1] = w[1] + alpha * (clazz - y) * pt[0];
			w[2] = w[2] + alpha * (clazz - y) * pt[1];
		}

		/**
		 * Predict the class of the given point
		 * 
		 * @param pt
		 *            the point
		 * @return 0 or 1
		 */
		public int predict(double[] pt) {
			return w[0] + pt[0] * w[1] + pt[1] * w[2] > 0 ? 1 : 0;
		}

		/**
		 * Compute y-ordinate of a point on the hyperplane given the x-ordinate
		 * 
		 * @param x
		 *            the x-ordinate
		 * @return the y-ordinate
		 */
		public double computeHyperplanePoint(double x) {
			return (w[0] + w[1] * x) / -w[2];
		}
	}

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
	private JComboBox classType;
	private double[] lastMean;
	private JTextField guess;

	private volatile List<double[]> points;
	private volatile List<Integer> classes;
	private volatile SimplePerceptron classifier;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		points = new ArrayList<double[]>();
		classes = new ArrayList<Integer>();
		classifier = new SimplePerceptron();

		vc = new VideoCaptureComponent(VIDEO_WIDTH, VIDEO_HEIGHT);
		vc.getDisplay().addVideoListener(this);

		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		// left hand side (video, features)
		final Box videoCtrls = Box.createVerticalBox();
		videoCtrls.setOpaque(false);
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
		image = new MBFImage(GRAPH_WIDTH, GRAPH_HEIGHT, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		imageComp = new DisplayUtilities.ImageComponent(true, false);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		rightPanel.add(imageComp);
		final JPanel classCtrlsCnt = new JPanel(new GridLayout(1, 2));

		// learning controls
		final JPanel learnCtrls = new JPanel(new GridLayout(0, 1));
		classType = new JComboBox();
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
		guess = new JTextField(8);
		guess.setOpaque(false);
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
		final HashSet<Integer> clzCount = new HashSet<Integer>();
		clzCount.addAll(classes);

		if (points.size() > 0 && clzCount.size() == 2) {
			final double[] p1 = new double[] { 0, 0 };
			p1[1] = (float) classifier.computeHyperplanePoint(0);

			final double[] p2 = new double[] { 1, 0 };
			p2[1] = (float) classifier.computeHyperplanePoint(1);

			image.drawLine(projectPoint(p1), projectPoint(p2), 3, RGBColour.BLACK);

			imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));

			guess.setText((String) this.classType.getItemAt(classifier.predict(mean)));
			return;
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

		final HashSet<Integer> clzCount = new HashSet<Integer>();
		clzCount.addAll(classes);

		if (points.size() > 0 && clzCount.size() == 2) {
			classifier.train(points, classes);
		}

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

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new LinearClassifierDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
