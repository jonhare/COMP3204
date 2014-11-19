package uk.ac.soton.ecs.comp3204.l11;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.camera.calibration.CameraCalibrationZhang;
import org.openimaj.image.camera.calibration.ChessboardCornerFinder;
import org.openimaj.image.camera.calibration.ChessboardCornerFinder.Options;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;

public class CalibrationDemo implements Slide, VideoDisplayListener<MBFImage>, ActionListener {
	protected static final Font FONT = Font.decode("Monaco-24");

	private VideoCaptureComponent vc;
	private ChessboardCornerFinder chessboard;
	private List<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>> corners = new ArrayList<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>>();
	private int patternWidth = 9;
	private int patternHeight = 6;
	private List<Point2dImpl> model = buildModel(patternWidth, patternHeight, 10);
	private boolean capture;
	private JLabel captureCount;
	private JTextField field11;
	private JTextField field12;
	private JTextField field21;
	private JTextField field22;
	private JTextField field13;
	private JTextField field23;
	private JTextField field31;
	private JTextField field32;
	private JTextField field33;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());
		vc = new VideoCaptureComponent(640, 480);
		base.add(vc);

		final GridBagConstraints c = new GridBagConstraints();
		final JPanel matrix = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix.add(field11 = new JTextField(5), c);
		field11.setFont(FONT);
		field11.setEditable(false);
		c.gridx = 1;
		matrix.add(field12 = new JTextField(5), c);
		field12.setFont(FONT);
		field12.setEditable(false);
		c.gridx = 2;
		matrix.add(field13 = new JTextField(5), c);
		field13.setFont(FONT);
		field13.setEditable(false);
		c.gridy = 1;
		c.gridx = 0;
		matrix.add(field21 = new JTextField(5), c);
		field21.setFont(FONT);
		field21.setEditable(false);
		c.gridx = 1;
		matrix.add(field22 = new JTextField(5), c);
		field22.setFont(FONT);
		field22.setEditable(false);
		c.gridx = 2;
		matrix.add(field23 = new JTextField(5), c);
		field23.setFont(FONT);
		field23.setEditable(false);
		c.gridy = 2;
		c.gridx = 0;
		matrix.add(field31 = new JTextField(5), c);
		field31.setFont(FONT);
		field31.setEditable(false);
		c.gridx = 1;
		matrix.add(field32 = new JTextField(5), c);
		field32.setFont(FONT);
		field32.setEditable(false);
		c.gridx = 2;
		matrix.add(field33 = new JTextField(5), c);
		field33.setFont(FONT);
		field33.setEditable(false);

		final JPanel controls = new JPanel(new GridBagLayout());
		controls.setOpaque(false);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;

		final JButton captureBtn = new JButton("Capture");
		captureBtn.setActionCommand("Capture");
		captureBtn.addActionListener(this);
		captureBtn.setFont(FONT);
		controls.add(captureBtn, c);

		c.gridx = 1;
		captureCount = new JLabel("0");
		captureCount.setFont(FONT);
		controls.add(captureCount, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		controls.add(matrix, c);

		base.add(controls);

		vc.getDisplay().addVideoListener(this);

		chessboard = new ChessboardCornerFinder(patternWidth, patternHeight,
				Options.FILTER_QUADS, Options.FAST_CHECK, Options.ADAPTIVE_THRESHOLD);

		return base;
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub

	}

	List<Point2dImpl> buildModel(int width, int height, double d) {
		final List<Point2dImpl> pts = new ArrayList<Point2dImpl>();
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				pts.add(new Point2dImpl(j * d, i * d));
		return pts;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		chessboard.analyseImage(frame.flatten());
		if (chessboard.isFound()) {
			chessboard.drawChessboardCorners(frame);

			if (capture) {
				capture = false;

				corners.add(IndependentPair.pairList(model, chessboard.getCorners()));
				captureCount.setText(corners.size() + "");

				if (corners.size() >= 2) {
					final CameraCalibrationZhang calib = new CameraCalibrationZhang(corners, 640, 480);
					// System.out.println(calib.getIntrisics());
					field11.setText("" + calib.getIntrisics().calibrationMatrix.get(0, 0));
					field12.setText("" + calib.getIntrisics().calibrationMatrix.get(0, 1));
					field13.setText("" + calib.getIntrisics().calibrationMatrix.get(0, 2));
					field21.setText("" + calib.getIntrisics().calibrationMatrix.get(1, 0));
					field22.setText("" + calib.getIntrisics().calibrationMatrix.get(1, 1));
					field23.setText("" + calib.getIntrisics().calibrationMatrix.get(1, 2));
					field31.setText("" + calib.getIntrisics().calibrationMatrix.get(2, 0));
					field32.setText("" + calib.getIntrisics().calibrationMatrix.get(2, 1));
					field33.setText("" + calib.getIntrisics().calibrationMatrix.get(2, 2));

				}
			}
		}

	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new CalibrationDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Capture") {
			capture = true;
		}
	}
}
