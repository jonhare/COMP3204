package uk.ac.soton.ecs.comp3204.l5;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.comp3204.l1.SimpleCameraDemo;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "CLM used for swapping faces")
public class PuppeteerDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	private CLMFaceTracker tracker = new CLMFaceTracker();
	private List<IndependentPair<MBFImage, List<Triangle>>> puppets = new ArrayList<IndependentPair<MBFImage, List<Triangle>>>();
	private TObjectIntHashMap<TrackedFace> puppetAssignments = new TObjectIntHashMap<TrackedFace>();
	private int nextPuppet = 0;

	/**
	 * Default constructor.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public PuppeteerDemo() throws MalformedURLException, IOException {
		tracker.fcheck = true;

		final CLMFaceTracker ptracker = new CLMFaceTracker();

		final URL[] puppetUrls = {
				PuppeteerDemo.class.getResource("mark.jpg")
		};

		for (final URL url : puppetUrls) {
			final MBFImage image = ImageUtilities.readMBF(url);

			ptracker.track(image);

			final TrackedFace face = ptracker.getTrackedFaces().get(0);

			puppets.add(IndependentPair.pair(image, ptracker.getTriangles(face)));

			ptracker.reset();
		}
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		tracker.track(frame);

		final List<TrackedFace> tracked = tracker.getTrackedFaces();

		for (final TrackedFace face : tracked) {
			int asgn;

			if (puppetAssignments.contains(face)) {
				asgn = puppetAssignments.get(face);
			} else {
				asgn = nextPuppet;
				puppetAssignments.put(face, asgn);
				nextPuppet++;

				if (nextPuppet >= puppets.size())
					nextPuppet = 0;
			}

			final List<Triangle> triangles = tracker.getTriangles(face);
			final IndependentPair<MBFImage, List<Triangle>> puppetData = this.puppets.get(asgn);
			final List<Triangle> puppetTriangles = puppetData.secondObject();

			final List<Pair<Shape>> matches = computeMatches(puppetTriangles, triangles);

			final PiecewiseMeshWarp<Float[], MBFImage> pmw = new PiecewiseMeshWarp<Float[], MBFImage>(matches);

			final Rectangle bounds = face.redetectedBounds.clone();
			bounds.height += 10;
			bounds.width += 10;
			bounds.x -= 5;
			bounds.y -= 5;
			bounds.scale((float) (1.0 / tracker.scale));

			MBFImage puppet = puppetData.firstObject();
			final List<FImage> bands = puppet.bands;
			puppet = pmw.transform(puppet, frame.getWidth(), frame.getHeight());

			composite(frame, puppet, bounds);

			puppet.bands = bands;
		}

		final Set<TrackedFace> toRemove = new HashSet<TrackedFace>(puppetAssignments.keySet());
		toRemove.removeAll(tracked);
		for (final TrackedFace face : toRemove) {
			puppetAssignments.remove(face);
		}
	}

	private List<Pair<Shape>> computeMatches(List<Triangle> from, List<Triangle> to) {
		final List<Pair<Shape>> mtris = new ArrayList<Pair<Shape>>();

		for (int i = 0; i < from.size(); i++) {
			final Triangle t1 = from.get(i);
			Triangle t2 = to.get(i);

			if (t1 != null && t2 != null) {
				t2 = t2.clone();
				t2.scale((float) (1.0 / tracker.scale));
				mtris.add(new Pair<Shape>(t1, t2));
			}
		}

		return mtris;
	}

	private void composite(MBFImage back, MBFImage fore, Rectangle bounds) {
		final float[][] rin = fore.bands.get(0).pixels;
		final float[][] gin = fore.bands.get(1).pixels;
		final float[][] bin = fore.bands.get(2).pixels;

		final float[][] rout = back.bands.get(0).pixels;
		final float[][] gout = back.bands.get(1).pixels;
		final float[][] bout = back.bands.get(2).pixels;

		final int xmin = (int) Math.max(0, bounds.x);
		final int ymin = (int) Math.max(0, bounds.y);

		final int ymax = (int) Math.min(Math.min(fore.getHeight(), back.getHeight()), bounds.y + bounds.height);
		final int xmax = (int) Math.min(Math.min(fore.getWidth(), back.getWidth()), bounds.x + bounds.width);

		for (int y = ymin; y < ymax; y++) {
			for (int x = xmin; x < xmax; x++) {
				if (rin[y][x] != 0 && gin[y][x] != 0 && bin[y][x] != 0) {
					rout[y][x] = rin[y][x];
					gout[y][x] = gin[y][x];
					bout[y][x] = bin[y][x];
				}
			}
		}
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel c = super.getComponent(width, height);

		this.vc.getDisplay().addVideoListener(this);

		return c;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new PuppeteerDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
