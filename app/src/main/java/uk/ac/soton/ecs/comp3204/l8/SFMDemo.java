package uk.ac.soton.ecs.comp3204.l8;

import gnu.trove.list.array.TIntArrayList;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.math.geometry.Vector3D;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.matrix.PseudoInverse;
import org.openimaj.math.matrix.ThinSingularValueDecomposition;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.FeatureTable;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import uk.ac.soton.ecs.comp3204.utils.Simple3D;
import uk.ac.soton.ecs.comp3204.utils.Simple3D.Point3D;
import uk.ac.soton.ecs.comp3204.utils.Simple3D.Scene;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

@Demonstration(title = "3D structure recovery from matchined 2d points")
public class SFMDemo implements Slide {
	private static int NUM_FEATURES = 400;

	private ImageComponent modelIC;
	private BufferedImage modelFrameB;
	private BufferedImage videoFrameB;
	private ImageComponent videoIC;

	private Matrix R;
	private Matrix S;
	private VideoDisplay<MBFImage> modelVideoDisplay;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel();
		outer.setOpaque(false);
		outer.setPreferredSize(new Dimension(width, height));
		outer.setLayout(new GridBagLayout());

		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		final JPanel imagesPanel = new JPanel();
		imagesPanel.setOpaque(false);
		imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.X_AXIS));

		final MBFImage modelFrame = new MBFImage(512, 480, 3);
		modelFrameB = ImageUtilities.createBufferedImageForDisplay(modelFrame);
		modelIC = new ImageComponent(modelFrameB);
		modelIC.setAllowPanning(false);
		modelIC.setAllowZoom(false);
		modelIC.setShowPixelColours(false);
		modelIC.setShowXYPosition(false);

		final MBFImage videoFrame = new MBFImage(512, 480, 3);
		videoFrameB = ImageUtilities.createBufferedImageForDisplay(videoFrame);
		videoIC = new ImageComponent(videoFrameB);
		videoIC.setAllowPanning(false);
		videoIC.setAllowZoom(false);
		videoIC.setShowPixelColours(false);
		videoIC.setShowXYPosition(false);

		imagesPanel.add(videoIC);
		imagesPanel.add(modelIC);

		base.add(imagesPanel);

		final JPanel controlsPanel = new JPanel();
		controlsPanel.setOpaque(false);
		final JButton runBtn = new JButton("Run");
		runBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runBtn.setEnabled(false);

				new Thread(new Runnable() {
					@Override
					public void run() {
						if (modelVideoDisplay != null) {
							modelVideoDisplay.close();
							modelVideoDisplay = null;
						}

						try {
							// Display the video and track points
							final TrackingContext tc = new TrackingContext();
							final FeatureList fl = new FeatureList(NUM_FEATURES);
							FeatureTable features = new FeatureTable(NUM_FEATURES);
							final KLTTracker tracker = new KLTTracker(tc, fl);

							tc.setSequentialMode(true);
							tc.setWriteInternalImages(false);
							tc.setAffineConsistencyCheck(-1);

							FImage prev = ImageUtilities.readF(SFMDemo.class.getResource("sfm/frame00000001.jpg"));
							tracker.selectGoodFeatures(prev);

							final MBFImage prevRGB = prev.toRGB();
							for (int i = 0; i < fl.features.length; i++)
								if (fl.features[i].val >= 0)
									prevRGB.drawPoint(fl.features[i], RGBColour.RED, 5);
							videoIC.setImage(videoFrameB = ImageUtilities.createBufferedImageForDisplay(prevRGB));

							features.storeFeatureList(fl, 0);

							for (int i = 1; i <= 101; i++) {
								final FImage next = ImageUtilities.readF(
										SFMDemo.class.getResource(String.format("sfm/frame%08d.jpg", i)));
								tracker.trackFeatures(prev, next);

								prev = next;

								final MBFImage nextRGB = next.toRGB();
								for (int j = 0; j < fl.features.length; j++)
									if (fl.features[j].val >= 0)
										nextRGB.drawPoint(fl.features[j], RGBColour.RED, 5);
								videoIC.setImage(videoFrameB = ImageUtilities.createBufferedImageForDisplay(nextRGB));

								features.storeFeatureList(fl, i);
							}

							// build the model
							features = filterNonTracked(features);

							final Matrix w = buildMeasurementMatrix(features);
							factorise(w);
							applyMetricConstraint();
							alignWithFrame(0);

							modelVideoDisplay = VideoDisplay.createVideoDisplay(makeVideo(makeScene()), modelIC);
						} catch (final IOException e) {
							e.printStackTrace();
						}

						runBtn.setEnabled(true);
					}
				}).start();
			}
		});
		controlsPanel.add(runBtn);

		base.add(controlsPanel);

		outer.add(base);

		return outer;
	}

	private Scene makeScene() {
		final Scene s = new Scene();

		for (int i = 0; i < S.getColumnDimension(); i++) {
			s.addPrimative(new Point3D(S.get(0, i), S.get(1, i), S.get(2, i), RGBColour.RED, 3));
		}

		return s;
	}

	AnimatedVideo<MBFImage> makeVideo(final Scene scene) {
		return new AnimatedVideo<MBFImage>(new MBFImage(500, 500, ColourSpace.RGB)) {
			float angle = 0;

			@Override
			protected void updateNextFrame(MBFImage frame) {
				frame.fill(RGBColour.BLACK);
				scene.renderOrtho(
						Simple3D.euler2Rot(3 * Math.PI / 4, angle, 0),
						frame);
				angle += (2 * Math.PI / 360);
				if (angle >= Math.PI * 2)
					angle -= 2 * Math.PI;
			}
		};
	}

	private void applyMetricConstraint() {
		final Matrix Q = calculateOrthometricConstraint(R);

		R = R.times(Q);
		S = Q.inverse().times(S);
	}

	private void alignWithFrame(int frame) {
		Vector3D i1 = new Vector3D(R.get(frame, 0), R.get(frame, 1), R.get(frame, 2));
		i1 = i1.scalarMultiply(1 / i1.getNorm());

		final int f = R.getRowDimension() / 2;
		Vector3D j1 = new Vector3D(R.get(frame + f, 0), R.get(frame + f, 1), R.get(frame + f, 2));
		j1 = j1.scalarMultiply(1 / j1.getNorm());

		final Vector3D k1 = Vector3D.crossProduct(i1, j1);
		k1.scalarMultiply(1 / k1.getNorm());

		final Matrix R0 = new Matrix(new double[][] {
				{ i1.getX(), j1.getX(), k1.getX() },
				{ i1.getY(), j1.getY(), k1.getY() },
				{ i1.getZ(), j1.getZ(), k1.getZ() },
		});

		R = R.times(R0);
		S = R0.inverse().times(S);
	}

	private void factorise(Matrix w) {
		final ThinSingularValueDecomposition svd = new ThinSingularValueDecomposition(w, 3);

		final Matrix s_sqrt = svd.getSmatrixSqrt();
		this.R = svd.U.times(s_sqrt);
		this.S = s_sqrt.times(svd.Vt);
	}

	FeatureTable filterNonTracked(FeatureTable ft) {
		final int nFrames = ft.features.size();
		final TIntArrayList tracksToRemove = new TIntArrayList();

		for (int i = 0; i < ft.nFeatures; i++) {
			int sum = 0;

			for (int f = 1; f < nFrames; f++) {
				sum += ft.features.get(f).get(i).val;
			}

			if (sum != 0) {
				tracksToRemove.add(i);
			}
		}

		final FeatureTable filtered = new FeatureTable(ft.nFeatures - tracksToRemove.size());
		for (int f = 0; f < nFrames; f++) {
			final FeatureList fl = new FeatureList(filtered.nFeatures);

			for (int i = 0, j = 0; i < ft.nFeatures; i++) {
				if (!tracksToRemove.contains(i))
					fl.features[j++] = ft.features.get(f).get(i);
			}
			filtered.storeFeatureList(fl, f);
		}

		return filtered;
	}

	Matrix buildMeasurementMatrix(FeatureTable ft) {
		final int p = ft.nFeatures; // number of features tracked
		final int f = ft.features.size(); // number of frames

		final Matrix W = new Matrix(2 * f, p);
		final double[][] Wdata = W.getArray();

		for (int r = 0; r < f; r++) {
			for (int c = 0; c < p; c++) {
				Wdata[r][c] = ft.features.get(r).get(c).x;
				Wdata[r + f][c] = ft.features.get(r).get(c).y;
			}
		}

		for (int r = 0; r < 2 * f; r++) {
			double mean = 0;

			for (int c = 0; c < p; c++) {
				mean += Wdata[r][c];
			}

			mean /= p;

			for (int c = 0; c < p; c++) {
				Wdata[r][c] -= mean;
			}
		}

		return W;
	}

	double[] gT(double[] a, double[] b) {
		return new double[] {
				a[0] * b[0],
				a[0] * b[1] + a[1] * b[0],
				a[0] * b[2] + a[2] * b[0],
				a[1] * b[1],
				a[1] * b[2] + a[2] * b[1],
				a[2] * b[2] };
	}

	Matrix calculateOrthometricConstraint(Matrix R) {
		final int f = R.getRowDimension() / 2;

		final double[][] ihT = R.getMatrix(0, f - 1, 0, 2).getArray();
		final double[][] jhT = R.getMatrix(f, (2 * f) - 1, 0, 2).getArray();

		final Matrix G = new Matrix(3 * f, 6);
		final Matrix c = new Matrix(3 * f, 1);
		for (int i = 0; i < f; i++) {
			G.getArray()[i] = gT(ihT[i], ihT[i]);
			G.getArray()[i + f] = gT(jhT[i], jhT[i]);
			G.getArray()[i + 2 * f] = gT(ihT[i], jhT[i]);

			c.set(i, 0, 1);
			c.set(i + f, 0, 1);
		}

		final Matrix I = PseudoInverse.pseudoInverse(G).times(c);

		final Matrix L = new Matrix(new double[][] {
				{ I.get(0, 0), I.get(1, 0), I.get(2, 0) },
				{ I.get(1, 0), I.get(3, 0), I.get(4, 0) },
				{ I.get(2, 0), I.get(4, 0), I.get(5, 0) }
		});

		// enforcing positive definiteness
		// see
		// http://www-cse.ucsd.edu/classes/sp04/cse252b/notes/lec16/lec16.pdf
		final Matrix Lsym = L.plus(L.transpose()).times(0.5);

		final EigenvalueDecomposition eigs = Lsym.eig();

		final Matrix Dsqrt = eigs.getD();
		final double[][] Darr = Dsqrt.getArray();
		for (int r = 0; r < Darr.length; r++) {
			if (Darr[r][r] < 0)
				Darr[r][r] = 0.0000001;

			Darr[r][r] = Math.sqrt(Darr[r][r]);
		}

		return eigs.getV().times(Dsqrt);
	}

	@Override
	public void close() {
		// ignore
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new SFMDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
