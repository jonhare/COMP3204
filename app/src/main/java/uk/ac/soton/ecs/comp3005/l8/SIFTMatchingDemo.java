package uk.ac.soton.ecs.comp3005.l8;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.io.FileUtils;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.demos.video.VideoSIFT;
import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.error.TransformError2d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;
import Jama.Matrix;

@Demonstration(title = "SIFT Matching")
public class SIFTMatchingDemo implements ActionListener, VideoDisplayListener<MBFImage>, Slide {
	enum RenderMode {
		SQUARE {
			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				renderer.drawShape(rectangle.transform(transform), 3, RGBColour.BLUE);
			}
		},
		PICTURE {
			MBFImage toRender = null;
			private Matrix renderToBounds;

			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				if (this.toRender == null) {
					try {
						this.toRender = ImageUtilities.readMBF(VideoSIFT.class
								.getResource("/org/openimaj/demos/OpenIMAJ.png"));
					} catch (final IOException e) {
						System.err.println("Can't load image to render");
					}
					this.renderToBounds = TransformUtilities.makeTransform(this.toRender.getBounds(), rectangle);
				}

				final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(this.renderToBounds));
				mbfPP.accumulate(this.toRender);
				mbfPP.performProjection(0, 0, renderer.getImage());

			}
		},
		VIDEO {
			private XuggleVideo toRender;
			private Matrix renderToBounds;

			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				if (this.toRender == null) {
					this.toRender = new XuggleVideo(MOVIE, true);
					this.renderToBounds = TransformUtilities.makeTransform(new Rectangle(0, 0, this.toRender.getWidth(),
							this.toRender.getHeight()), rectangle);
				}

				final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(this.renderToBounds));
				mbfPP.accumulate(this.toRender.getNextFrame());
				mbfPP.performProjection(0, 0, renderer.getImage());
			}
		};
		public abstract void render(MBFImageRenderer renderer, Matrix transform, Rectangle rectangle);
	}

	// private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private ImageComponent modelFrame;
	private ImageComponent matchFrame;

	private MBFImage modelImage;

	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private VideoCaptureComponent vidPanel;
	private JPanel modelPanel;
	private JPanel matchPanel;
	private RenderMode renderMode = RenderMode.SQUARE;
	private MBFImage currentFrame;

	private static URL MOVIE;
	static {
		try {
			final File tmp = File.createTempFile("movie", ".tmp");
			tmp.deleteOnExit();
			FileUtils.copyURLToFile(VideoSIFT.class.getResource("keyboardcat.flv"), tmp);
			MOVIE = tmp.toURI().toURL();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void init(JComponent window, int width, int height) throws Exception {
		this.polygonListener = new PolygonDrawingListener();

		GridBagConstraints gbc = new GridBagConstraints();

		this.vidPanel = new VideoCaptureComponent(width, height);
		this.vidPanel.setBorder(BorderFactory.createTitledBorder("Live Video"));
		this.videoFrame = vidPanel.getDisplay();

		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		window.add(this.vidPanel, gbc);

		this.modelPanel = new JPanel(new GridBagLayout());
		this.modelPanel.setBorder(BorderFactory.createTitledBorder("Model"));
		this.modelFrame = new ImageComponent(true, false);
		this.modelFrame.setShowPixelColours(false);
		this.modelFrame.setShowXYPosition(false);
		this.modelFrame.removeMouseListener(this.modelFrame);
		this.modelFrame.removeMouseMotionListener(this.modelFrame);
		this.modelFrame.setSize(width, height);
		this.modelFrame.setPreferredSize(new Dimension(width, height));
		this.modelPanel.add(this.modelFrame);
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridy = 1;
		gbc.gridx = 1;
		window.add(this.modelPanel, gbc);

		this.matchPanel = new JPanel(new GridBagLayout());
		this.matchPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
		this.matchFrame = new ImageComponent(true, false);
		this.matchFrame.setShowPixelColours(false);
		this.matchFrame.setShowXYPosition(false);
		this.matchFrame.removeMouseListener(this.matchFrame);
		this.matchFrame.removeMouseMotionListener(this.matchFrame);
		this.matchFrame.setSize(width * 2, height);
		this.matchFrame.setPreferredSize(new Dimension(width * 2, height));
		this.matchPanel.add(this.matchFrame);
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		window.add(this.matchPanel, gbc);

		final JPanel controlsPanel = new JPanel();
		final JButton pauseBtn = new JButton("Pause");
		pauseBtn.setActionCommand("pause");
		pauseBtn.addActionListener(this);
		controlsPanel.add(pauseBtn);

		final JButton capBtn = new JButton("Capture");
		capBtn.setActionCommand("capture");
		capBtn.addActionListener(this);
		controlsPanel.add(capBtn);

		final JRadioButton boxBtn = new JRadioButton("Box");
		boxBtn.setSelected(true);
		boxBtn.setActionCommand("sqMode");
		boxBtn.addActionListener(this);
		controlsPanel.add(boxBtn);

		final JRadioButton picBtn = new JRadioButton("Picture");
		picBtn.setActionCommand("picMode");
		picBtn.addActionListener(this);
		controlsPanel.add(picBtn);

		final JRadioButton vidBtn = new JRadioButton("Video");
		vidBtn.setActionCommand("vidMode");
		vidBtn.addActionListener(this);
		controlsPanel.add(vidBtn);

		final ButtonGroup bg = new ButtonGroup();
		bg.add(boxBtn);
		bg.add(picBtn);
		bg.add(vidBtn);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		window.add(controlsPanel, gbc);

		this.videoFrame.getScreen().addMouseListener(this.polygonListener);

		this.videoFrame.addVideoListener(this);
		this.engine = new DoGSIFTEngine();
		this.engine.getOptions().setDoubleInitialImage(false);
	}

	@Override
	public synchronized void actionPerformed(final ActionEvent action) {
		if (action.getActionCommand().equals("pause")) {
			this.videoFrame.togglePause();
			if (this.videoFrame.isPaused()) {
				((JButton) action.getSource()).setText("Resume");
			} else {
				((JButton) action.getSource()).setText("Pause");
			}
		}
		else if (action.getActionCommand().equals("capture") &&
				this.polygonListener.getPolygon().getVertices().size() > 2)
		{
			try {
				final Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				this.modelImage = this.currentFrame.process(
						new PolygonExtractionProcessor<Float[], MBFImage>(p,
								RGBColour.BLACK));

				if (this.matcher == null) {
					// configure the matcher
					final HomographyModel model = new HomographyModel();
					final RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d,
							Point2d>(model, new TransformError2d(), 10, 1500,
									new RANSAC.PercentageInliersStoppingCondition(0.6), true);
					this.matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
							new FastBasicKeypointMatcher<Keypoint>(8));
					this.matcher.setFittingModel(ransac);

					this.modelPanel.setPreferredSize(this.modelPanel.getSize());
				}

				this.modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(this.modelImage));

				final DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(false);

				final FImage modelF =
						Transforms.calculateIntensityNTSC(this.modelImage);
				this.matcher.setModelFeatures(engine.findFeatures(modelF));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else if (action.getActionCommand().equals("sqMode")) {
			this.renderMode = RenderMode.SQUARE;
		} else if (action.getActionCommand().equals("picMode")) {
			this.renderMode = RenderMode.PICTURE;
		} else if (action.getActionCommand().equals("vidMode")) {
			this.renderMode = RenderMode.VIDEO;
		}
	}

	@Override
	public synchronized void afterUpdate(final VideoDisplay<MBFImage> display) {
		if (this.matcher != null && !this.videoFrame.isPaused()) {
			final MBFImage capImg = this.currentFrame;
			final LocalFeatureList<Keypoint> kpl = this.engine.findFeatures(Transforms.calculateIntensityNTSC(capImg));

			final MBFImageRenderer renderer = capImg.createRenderer();
			renderer.drawPoints(kpl, RGBColour.MAGENTA, 3);

			MBFImage matches;
			if (this.matcher.findMatches(kpl)) {
				try {
					// Shape sh =
					// modelImage.getBounds().transform(((MatrixTransformProvider)
					// matcher.getModel()).getTransform().inverse());
					// renderer.drawShape(sh, 3, RGBColour.BLUE);
					final Matrix boundsToPoly = ((MatrixTransformProvider) this.matcher.getModel()).getTransform()
							.inverse();
					this.renderMode.render(renderer, boundsToPoly, this.modelImage.getBounds());
				} catch (final RuntimeException e) {
				}

				matches = MatchingUtilities
						.drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
			} else {
				matches = MatchingUtilities
						.drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
			}

			this.matchPanel.setPreferredSize(this.matchPanel.getSize());
			this.matchFrame.setImage(ImageUtilities.createBufferedImageForDisplay(matches));
		}
	}

	@Override
	public void beforeUpdate(final MBFImage frame) {
		if (!this.videoFrame.isPaused())
			this.currentFrame = frame.clone();
		else {
			frame.drawImage(currentFrame, 0, 0);
		}
		this.polygonListener.drawPoints(frame);
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel c = new JPanel();
		c.setOpaque(true);
		c.setPreferredSize(new Dimension(width, height));
		c.setLayout(new GridBagLayout());

		try {
			init(c, 320, 240);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return c;
	}

	@Override
	public void close() {
		this.vidPanel.close();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new SIFTMatchingDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
