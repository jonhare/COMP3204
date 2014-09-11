package uk.ac.soton.ecs.comp3204.l5;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.SuzukiContourProcessor;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.VideoCaptureComponent;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Region Adjacency Graph Demo")
public class RAGDemo implements Slide, VideoDisplayListener<MBFImage> {
	private SuzukiContourProcessor proc = new SuzukiContourProcessor();
	private VideoCaptureComponent vc;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
		vc = new VideoCaptureComponent(640, 480);
		vc.getDisplay().addVideoListener(this);
		base.add(vc);

		return base;
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final FImage grey = frame.flatten();
		grey.threshold(OtsuThreshold.calculateThreshold(grey, 256));

		proc.analyseImage(grey);

		final List<Contour> targets = find(proc.root);

		for (final Contour t : targets) {
			frame.drawPolygon(t, 4, RGBColour.RED);
			final Point2d centre = t.calculateCentroid();
			final int size = (int) Math.sqrt(t.calculateArea());
			centre.setY(centre.getY() + 0.5f * size);
			final HersheyFontStyle<Float[]> fs = HersheyFont.ROMAN_SIMPLEX.createStyle(frame.createRenderer());
			fs.setStrokeWidth(4);
			fs.setHorizontalAlignment(HorizontalAlignment.HORIZONTAL_CENTER);
			fs.setColour(RGBColour.RED);
			fs.setFontSize(size);
			frame.drawText("" + t.children.get(0).children.size(), centre, fs);
		}
	}

	private static final int MAX_CHILDLESS_CHILDREN = 0;
	private static final int MAX_CHILDREN = 5;
	private static final int MIN_CHILDREN = 1;

	public List<Contour> find(Contour in) {
		final List<Contour> found = new ArrayList<Contour>();
		detect(in, found);
		return found;
	}

	private void detect(Contour root, List<Contour> found) {
		if (test(root)) {
			found.add(root);
		}
		else {
			for (final Contour child : root.children) {
				detect(child, found);
			}
		}
	}

	public boolean test(Contour in) {
		// has at least one child
		if (in.children.size() < MIN_CHILDREN || in.children.size() > MAX_CHILDREN) {
			return false;
		}
		int childlessChild = 0;
		// all grandchildren have no children
		for (final Contour child : in.children) {
			if (child.children.size() == 0)
				childlessChild++;

			if (childlessChild > MAX_CHILDLESS_CHILDREN)
				return false;
			for (final Contour grandchildren : child.children) {
				if (grandchildren.children.size() != 0)
					return false;
			}
		}
		return true;
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new RAGDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
