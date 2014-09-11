package uk.ac.soton.ecs.comp3204.l3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.matrix.algorithm.pca.CovarPrincipalComponentAnalysis;
import org.openimaj.math.statistics.distribution.FullMultivariateGaussian;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;

import uk.ac.soton.ecs.comp3204.utils.Simple3D;
import uk.ac.soton.ecs.comp3204.utils.Simple3D.Line3D;
import uk.ac.soton.ecs.comp3204.utils.Simple3D.Scene;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;
import Jama.Matrix;

@Demonstration(title = "PCA Dimensionality reduction demo")
public class PCADemo implements Slide {
	double[][] createData(int nsamples) {
		final Random rng = new Random();
		final Matrix mean = new Matrix(new double[][] { { 100, 100, 100 } });
		final Matrix covar = new Matrix(new double[][] {
				{ 100, 50, 100 },
				{ 50, 100, 100 },
				{ 100, 100, 1000 }
		});
		final FullMultivariateGaussian gauss = new FullMultivariateGaussian(mean, covar);

		final double[][] data = new double[nsamples][];
		for (int i = 0; i < data.length; i++) {
			data[i] = gauss.sample(rng);
		}
		return data;
	}

	Scene makeScene(double[][] data, String[] labels) {
		final Scene scene = new Scene();

		scene.addPrimative(new Line3D(-200, 0, 0, 200, 0, 0, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(180, 10, 0, 200, 0, 0, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(180, -10, 0, 200, 0, 0, RGBColour.RED, 3));
		scene.addPrimative(new Simple3D.Text3D(200, -50, 0, RGBColour.RED, 40, labels[0]));

		scene.addPrimative(new Line3D(0, -200, 0, 0, 200, 0, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(10, 180, 0, 0, 200, 0, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(-10, 180, 0, 0, 200, 0, RGBColour.RED, 3));
		scene.addPrimative(new Simple3D.Text3D(-30, 180, 0, RGBColour.RED, 40, labels[1]));

		scene.addPrimative(new Line3D(0, 0, -200, 0, 0, 200, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(0, 10, 180, 0, 0, 200, RGBColour.RED, 3));
		scene.addPrimative(new Line3D(0, -10, 180, 0, 0, 200, RGBColour.RED, 3));
		scene.addPrimative(new Simple3D.Text3D(0, -70, 180, RGBColour.RED, 40, labels[2]));

		for (int i = 0; i < data.length; i++) {
			scene.addPrimative(new Simple3D.Point3D(data[i][0], data[i][1], data[i][2], RGBColour.GREEN, 3));
		}

		return scene;
	}

	AnimatedVideo<MBFImage> makeVideo(final Scene scene) {
		return new AnimatedVideo<MBFImage>(new MBFImage(500, 500, ColourSpace.RGB)) {
			float angle = 0;

			@Override
			protected void updateNextFrame(MBFImage frame) {
				frame.fill(RGBColour.BLACK);
				scene.renderOrtho(
						Simple3D.euler2Rot(Math.PI / 4, angle, 0),
						frame);
				angle += (2 * Math.PI / 360);
				if (angle >= Math.PI * 2)
					angle -= 2 * Math.PI;
			}
		};
	}

	MBFImage plot2d(double[][] data) {
		final MBFImage img = new MBFImage(500, 500, ColourSpace.RGB);

		img.drawLine(img.getWidth() / 2, img.getHeight() - 50, img.getWidth() / 2, 50, 3, RGBColour.RED);
		img.drawLine(img.getWidth() / 2 - 10, 70, img.getWidth() / 2, 50, 3, RGBColour.RED);
		img.drawLine(img.getWidth() / 2 + 10, 70, img.getWidth() / 2, 50, 3, RGBColour.RED);
		img.drawText("EV2", img.getWidth() / 2 - 80, 70, HersheyFont.ROMAN_DUPLEX, 40, RGBColour.RED);

		img.drawLine(50, img.getHeight() / 2, img.getWidth() - 50, img.getHeight() / 2, 3, RGBColour.RED);
		img.drawLine(img.getWidth() - 70, img.getHeight() / 2 + 10, img.getWidth() - 50, img.getHeight() / 2, 3,
				RGBColour.RED);
		img.drawLine(img.getWidth() - 70, img.getHeight() / 2 - 10, img.getWidth() - 50, img.getHeight() / 2, 3,
				RGBColour.RED);
		img.drawText("EV1", img.getWidth() - 70, img.getHeight() / 2 + 50, HersheyFont.ROMAN_DUPLEX, 40, RGBColour.RED);

		for (final double[] d : data) {
			img.drawPoint(new Point2dImpl(img.getWidth() / 2 - (float) d[0], img.getHeight() / 2 - (float) d[1]),
					RGBColour.GREEN, 3);
		}

		return img;
	}

	MBFImage plot1d(double[][] data) {
		final MBFImage img = new MBFImage(500, 500, ColourSpace.RGB);

		img.drawLine(50, img.getHeight() / 2, img.getWidth() - 50, img.getHeight() / 2, 3, RGBColour.RED);
		img.drawLine(img.getWidth() - 70, img.getHeight() / 2 + 10, img.getWidth() - 50, img.getHeight() / 2, 3,
				RGBColour.RED);
		img.drawLine(img.getWidth() - 70, img.getHeight() / 2 - 10, img.getWidth() - 50, img.getHeight() / 2, 3,
				RGBColour.RED);
		img.drawText("EV1", img.getWidth() - 70, img.getHeight() / 2 + 50, HersheyFont.ROMAN_DUPLEX, 40, RGBColour.RED);

		for (final double[] d : data) {
			img.drawPoint(new Point2dImpl(img.getWidth() / 2 - (float) d[0], img.getHeight() / 2),
					RGBColour.GREEN, 3);
		}

		return img;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final double[][] data = createData(1000);
		final Scene scene = makeScene(data, new String[] { "x", "y", "z" });
		final Matrix datam = new Matrix(data);
		final CovarPrincipalComponentAnalysis pca = new CovarPrincipalComponentAnalysis(3);
		pca.learnBasis(datam);
		final double[][] pdata = pca.project(datam).getArray();
		final Scene pscene = makeScene(pdata, new String[] { "EV1", "EV2", "EV3" });

		final JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		VideoDisplay.createVideoDisplay(makeVideo(scene), dataPanel);

		final JPanel dummyPanel = new JPanel();
		dataPanel.add(dummyPanel);
		base.add(dataPanel);

		final JPanel pcaPanel = new JPanel();
		pcaPanel.setLayout(new BoxLayout(pcaPanel, BoxLayout.Y_AXIS));
		final VideoDisplay<MBFImage> pcaVid = VideoDisplay.createVideoDisplay(makeVideo(pscene), pcaPanel);

		final JPanel controlsPanel = new JPanel();
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(3, 1, 3, 1));
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int val = (Integer) spinner.getValue();
				switch (val) {
				case 1:
					pcaVid.setMode(Mode.STOP);
					pcaVid.getScreen().setImage(ImageUtilities.createBufferedImage(plot1d(pdata)));
					break;
				case 2:
					pcaVid.setMode(Mode.STOP);
					pcaVid.getScreen().setImage(ImageUtilities.createBufferedImage(plot2d(pdata)));
					break;
				case 3:
					pcaVid.setMode(Mode.STOP);
					pcaVid.getVideo().reset();
					pcaVid.setMode(Mode.PLAY);
					break;
				}
			}
		});
		controlsPanel.add(spinner);
		pcaPanel.add(controlsPanel);
		base.add(pcaPanel);

		dummyPanel.setPreferredSize(controlsPanel.getPreferredSize());

		return base;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new PCADemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
