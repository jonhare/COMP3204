/**
 * Copyright (c) 2013, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.soton.ecs.comp3204.l9;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactByteAssigner;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

/**
 * Demo showing SIFT BoVW histograms
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demonstration(title = "SIFT BoVW Histograms")
public class BoVWHistogramDemo implements Slide, VideoDisplayListener<MBFImage> {

	private VideoCaptureComponent capture;
	private VideoDisplay<MBFImage> videoDisplay;
	private ImageComponent modelFrame;
	private MBFImage histogramImage;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel outer = new JPanel(new GridBagLayout());
		outer.setSize(width, height);
		outer.setPreferredSize(new Dimension(width, height));

		final JPanel base = new JPanel();
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		this.capture = new VideoCaptureComponent(640, 480);

		this.videoDisplay = capture.getDisplay();
		this.videoDisplay.addVideoListener(this);
		base.add(this.capture);

		this.modelFrame = new ImageComponent(true, false);
		this.modelFrame.setShowPixelColours(false);
		this.modelFrame.setShowXYPosition(false);
		this.modelFrame.removeMouseListener(modelFrame);
		this.modelFrame.removeMouseMotionListener(modelFrame);
		base.add(this.modelFrame);
		this.histogramImage = new MBFImage(1000, 200, ColourSpace.RGB);
		this.modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(this.histogramImage));

		outer.add(base);

		return outer;
	}

	@Override
	public void close() {
		capture.close();
	}

	@Override
	public void afterUpdate(final VideoDisplay<MBFImage> display) {

	}

	@Override
	public synchronized void beforeUpdate(final MBFImage frame) {
		final DoubleFV histogram = createFeature(frame);

		this.drawHistogramImage(histogram);
		this.modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(this.histogramImage));
	}

	private void drawHistogramImage(DoubleFV histogram) {
		histogram = histogram.normaliseFV();

		final int width = this.histogramImage.getWidth();
		final int height = this.histogramImage.getHeight();

		final int bw = width / histogram.length();

		this.histogramImage.fill(RGBColour.WHITE);
		final MBFImageRenderer renderer = this.histogramImage.createRenderer();
		final Rectangle s = new Rectangle();
		s.width = bw;
		for (int i = 0; i < histogram.values.length; i++) {
			final int rectHeight = (int) (histogram.values[i] * height);
			final int remHeight = height - rectHeight;

			s.x = i * bw;
			s.y = remHeight;
			s.height = rectHeight;
			renderer.drawShapeFilled(s, RGBColour.RED);
		}
	}

	ExactByteAssigner rabc = null;
	DoubleFV fv = null;
	DoGSIFTEngine engine = new DoGSIFTEngine();

	DoubleFV createFeature(final MBFImage image) {
		if (this.rabc == null) {
			try {
				final ByteCentroidsResult clusterer = IOUtils.read(BoVWHistogramDemo.class
						.getResourceAsStream("random-100-highfield-codebook.voc"),
						ByteCentroidsResult.class);

				this.rabc = new ExactByteAssigner(clusterer);
				this.fv = new DoubleFV(clusterer.numClusters());
				this.engine.getOptions().setDoubleInitialImage(false);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		FImage img = Transforms.calculateIntensity(image);
		img = ResizeProcessor.halfSize(img);
		final List<Keypoint> keys = this.engine.findFeatures(img);

		for (final Keypoint keypoint : keys) {
			image.drawPoint(new Point2dImpl(keypoint.x * 2f, keypoint.y * 2f), RGBColour.RED, 3);
		}

		Arrays.fill(this.fv.values, 0);

		for (final Keypoint k : keys) {
			this.fv.values[this.rabc.assign(k.ivec)]++;
		}

		return this.fv;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new BoVWHistogramDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
