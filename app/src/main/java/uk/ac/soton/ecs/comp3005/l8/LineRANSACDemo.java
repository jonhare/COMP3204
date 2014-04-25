package uk.ac.soton.ecs.comp3005.l8;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.model.LeastSquaresLinearModel;
import org.openimaj.util.pair.Pair;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;

/**
 * Demo showing robust line of best fit estimation
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "RANSAC Line Estimation")
public class LineRANSACDemo extends MouseAdapter implements Slide, ActionListener {

	private static final int TOL = 20;
	private MBFImage image;
	private ImageComponent ic;
	private BufferedImage bimg;
	private List<Point2dImpl> points = new ArrayList<Point2dImpl>();
	private JButton runBtn;
	private JButton clearBtn;
	private JButton cnclBtn;
	private volatile boolean isRunning;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		image = new MBFImage(width, height - 50, ColourSpace.RGB);
		resetImage();

		ic = new DisplayUtilities.ImageComponent(true, false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.setAllowPanning(false);
		ic.setAllowZoom(false);
		ic.addMouseListener(this);
		ic.addMouseMotionListener(this);
		base.add(ic);

		final JPanel controls = new JPanel();
		controls.setPreferredSize(new Dimension(width, 50));
		controls.setMaximumSize(new Dimension(width, 50));
		controls.setSize(new Dimension(width, 50));

		clearBtn = new JButton("Clear");
		clearBtn.setActionCommand("button.clear");
		clearBtn.addActionListener(this);
		controls.add(clearBtn);

		controls.add(new JSeparator(SwingConstants.VERTICAL));

		runBtn = new JButton("Run RANSAC Estimator");
		runBtn.setActionCommand("button.run");
		runBtn.addActionListener(this);
		controls.add(runBtn);

		controls.add(new JSeparator(SwingConstants.VERTICAL));

		cnclBtn = new JButton("Cancel");
		cnclBtn.setEnabled(false);
		cnclBtn.setActionCommand("button.cancel");
		cnclBtn.addActionListener(this);
		controls.add(cnclBtn);

		base.add(controls);

		updateImage();

		return base;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		mouseDragged(e);
	}

	private void resetImage() {
		image.fill(RGBColour.WHITE);
		points.clear();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isRunning) {
			final Point pt = e.getPoint();
			final Point2dImpl pti = new Point2dImpl(pt.x, pt.y);
			image.drawPoint(pti, RGBColour.MAGENTA, 10);
			points.add(pti);
			updateImage();
		}
	}

	private void redrawImageClean() {
		image.fill(RGBColour.WHITE);
		for (final Point2dImpl pti : points)
			image.drawPoint(pti, RGBColour.MAGENTA, 10);

		computeLS(points, RGBColour.RED);

		updateImage();
	}

	private void computeLS(List<Point2dImpl> pts, Float[] col) {
		final LeastSquaresLinearModel model = new LeastSquaresLinearModel(0);
		final ArrayList<Pair<Integer>> data = new ArrayList<Pair<Integer>>();
		for (final Point2dImpl p : pts)
			data.add(new Pair<Integer>((int) p.x, (int) p.y));
		model.estimate(data);

		final double m = model.getM();
		final double c = model.getC();
		final Point2dImpl p1 = new Point2dImpl(0, c);
		final Point2dImpl p2 = new Point2dImpl(image.getWidth(), m * image.getWidth() + c);
		image.drawLine(p1, p2, col);
	}

	private void updateImage() {
		ic.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
	}

	@Override
	public void close() {
		isRunning = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("button.clear")) {
			resetImage();
			updateImage();
		} else if (e.getActionCommand().equals("button.run")) {
			runBtn.setEnabled(false);
			clearBtn.setEnabled(false);
			cnclBtn.setEnabled(true);
			isRunning = true;

			new Thread(new Runnable() {
				@Override
				public void run() {
					List<Point2dImpl> inliers = null;
					for (int i = 0; i < 30 && isRunning; i++) {
						Pair<Point2dImpl> p = null;

						redrawImageClean();

						if (isRunning)
							p = randomSelectStep(true); // select a pair of
														// points

						Point2dImpl m = null;
						if (isRunning)
							m = computeModelStep(p); // fit line to points

						if (isRunning)
							inliers = computeInOutStep(m); // compute inliers

						if (inliers.size() / ((double) LineRANSACDemo.this.points.size()) > 0.75)
							break;
					}

					computeLS(inliers, RGBColour.BLUE);

					runBtn.setEnabled(true);
					clearBtn.setEnabled(true);
					cnclBtn.setEnabled(false);
					isRunning = false;
				}

			}).start();
		} else if (e.getActionCommand().equals("button.cancel")) {
			isRunning = false;
			cnclBtn.setEnabled(false);
		}
	}

	protected List<Point2dImpl> computeInOutStep(Point2dImpl model) {
		final float m = model.x;
		final float c = model.y;

		final List<Point2dImpl> inliers = new ArrayList<Point2dImpl>();
		for (final Point2dImpl pt : points) {
			final float py = m * pt.x + c;
			final float residual = Math.abs(pt.y - py);

			if (residual < TOL) {
				inliers.add(pt);
				image.drawPoint(pt, RGBColour.GREEN, 10);
			} else {
				image.drawPoint(pt, RGBColour.RED, 10);
			}
			updateImage();
		}

		return inliers;
	}

	private Point2dImpl computeModelStep(Pair<Point2dImpl> p) {
		final float m = (p.firstObject().y - p.secondObject().y) / (p.firstObject().x - p.secondObject().x);
		final float c = p.firstObject().y - (m * p.firstObject().x);

		final Point2dImpl p1 = new Point2dImpl(0, c);
		final Point2dImpl p2 = new Point2dImpl(image.getWidth(), m * image.getWidth() + c);
		image.drawLine(p1, p2, RGBColour.BLUE);
		updateImage();

		return new Point2dImpl(m, c);
	}

	private Pair<Point2dImpl> randomSelectStep(boolean b) {
		final Point2dImpl pt1 = points.get((int) (Math.random() * points.size()));
		final Point2dImpl pt2 = points.get((int) (Math.random() * points.size()));

		image.drawPoint(pt1, RGBColour.BLUE, 10);
		image.drawPoint(pt2, RGBColour.BLUE, 10);
		updateImage();

		return new Pair<Point2dImpl>(pt1, pt2);
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new LineRANSACDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
