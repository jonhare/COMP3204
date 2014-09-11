package uk.ac.soton.ecs.comp3204.l5;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;

import uk.ac.soton.ecs.comp3204.utils.PixelDrawingComponent;
import uk.ac.soton.ecs.comp3204.utils.Utils;
import uk.ac.soton.ecs.comp3204.utils.annotations.Demonstration;

@Demonstration(title = "Simple scalar shape features demo")
public class ScalarShapeFeaturesDemo implements Slide, Operation<FImage> {
	static Font FONT = Font.decode("Monaco-30");

	private PixelDrawingComponent pdc;
	private JTextField area;
	private JTextField perimeter;
	private JTextField compactness;
	private JTextField is;
	private JTextField irs;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		pdc = new PixelDrawingComponent(400, 20);
		pdc.addActionListener(this);
		base.add(pdc);

		final JPanel features = new JPanel(new GridBagLayout());
		makeLabel("Area:", features, 0);
		area = makeTextField(features, 0);

		makeLabel("Perimeter:", features, 1);
		perimeter = makeTextField(features, 1);

		makeLabel("Compactness:", features, 2);
		compactness = makeTextField(features, 2);

		makeLabel("Irregularity I(s):", features, 3);
		is = makeTextField(features, 3);

		makeLabel("Irregularity IR(s):", features, 4);
		irs = makeTextField(features, 4);

		base.add(features);

		return base;
	}

	private void makeLabel(String string, JPanel features, int row) {
		final JLabel label = new JLabel(string);
		label.setFont(FONT);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.EAST;
		features.add(label, gbc);
	}

	private JTextField makeTextField(JPanel features, int row) {
		final JTextField f = new JTextField(5);
		f.setHorizontalAlignment(SwingUtilities.RIGHT);
		f.setFont(FONT);
		f.setText("");
		f.setEditable(false);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = row;
		features.add(f, gbc);

		return f;
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public void perform(FImage image) {
		final List<ConnectedComponent> comps = ConnectedComponentLabeler.Algorithm.TWO_PASS.findComponents(image, 0,
				ConnectMode.CONNECT_8);

		if (comps.size() == 1) {
			final ConnectedComponent cc = comps.get(0);

			final double p = perimeter(cc.getOuterBoundary());
			final double a = cc.pixels.size();
			final double c = (4 * Math.PI * a) / (p * p);
			final double is = computeIS(cc);
			final double irs = computeIRS(cc);

			this.area.setText(String.format("%2.2f", a));
			this.perimeter.setText(String.format("%2.2f", p));
			this.compactness.setText(String.format("%2.2f", c));
			this.is.setText(String.format("%2.2f", is));
			this.irs.setText(String.format("%2.2f", irs));
		} else {
			this.area.setText("");
			this.perimeter.setText("");
			this.compactness.setText("");
			this.is.setText("");
			this.irs.setText("");
		}
	}

	private double computeIRS(ConnectedComponent cc) {
		final double[] centroid = cc.calculateCentroid();

		double max = 0;
		double min = Double.MAX_VALUE;
		for (final Pixel p : cc.getOuterBoundary()) {
			final double dx = p.x - centroid[0];
			final double dy = p.y - centroid[1];

			max = Math.max(Math.sqrt(dx * dx + dy * dy), max);
			min = Math.min(Math.sqrt(dx * dx + dy * dy), min);
		}

		return min == 0 ? 0 : max / min;
	}

	private double computeIS(ConnectedComponent cc) {
		final double[] centroid = cc.calculateCentroid();

		double max = 0;
		for (final Pixel p : cc.getOuterBoundary()) {
			final double dx = p.x - centroid[0];
			final double dy = p.y - centroid[1];

			max = Math.max(dx * dx + dy * dy, max);
		}

		return Math.PI * max / cc.pixels.size();
	}

	private double perimeter(final List<Pixel> boundary) {
		double p = Math.sqrt(((boundary.get(boundary.size() - 1).x - boundary.get(0).x) *
				(boundary.get(boundary.size() - 1).x - boundary.get(0).x)) +
				((boundary.get(boundary.size() - 1).y - boundary.get(0).y) *
				(boundary.get(boundary.size() - 1).y - boundary.get(0).y)));
		for (int i = 1; i < boundary.size(); i++) {
			p += Math.sqrt(((boundary.get(i - 1).x - boundary.get(i).x) *
					(boundary.get(i - 1).x - boundary.get(i).x)) +
					((boundary.get(i - 1).y - boundary.get(i).y) *
					(boundary.get(i - 1).y - boundary.get(i).y)));
		}
		return p;
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new ScalarShapeFeaturesDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
