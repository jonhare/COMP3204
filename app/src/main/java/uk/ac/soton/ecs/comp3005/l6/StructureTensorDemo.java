package uk.ac.soton.ecs.comp3005.l6;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.shape.Rectangle;

import uk.ac.soton.ecs.comp3005.utils.SlidingWindowComponent;
import uk.ac.soton.ecs.comp3005.utils.Utils;
import uk.ac.soton.ecs.comp3005.utils.annotations.Demonstration;
import Jama.Matrix;

@Demonstration(title = "The Structure Tensor")
public class StructureTensorDemo implements Slide {
	private static final Font FONT = Font.decode("Monaco-32");
	private static final Font TITLE_FONT = Font.decode("Monaco-24");
	private SlidingWindowComponent owindow;
	private SlidingWindowComponent dxwindow;
	private SlidingWindowComponent dywindow;
	private JTextField xxField;
	private JTextField xyField;
	private JTextField yxField;
	private JTextField yyField;
	private JTextField ev1Field;
	private JTextField ev2Field;
	private FImage image;
	private FImage dx;
	private FImage dy;

	public StructureTensorDemo() {
		this.image = new FImage(300, 300);
		final FImageRenderer r = image.createRenderer(RenderHints.ANTI_ALIASED);
		r.drawLine(0, 150, 75, 75, 3, 1f);
		r.drawLine(0, 0, 75, 75, 3, 1f);
		r.drawLine(0, 200, 100, 250, 3, 1f);
		r.drawLine(0, 300, 100, 250, 3, 1f);
		r.drawLine(200, 0, 200, 100, 3, 1f);
		r.drawLine(200, 100, 300, 100, 3, 1f);
		r.drawLine(250, 300, 200, 200, 3, 1f);
		r.drawLine(300, 250, 200, 200, 3, 1f);

		final FSobel fs = new FSobel();
		fs.analyseImage(image);
		this.dx = fs.dx;
		this.dy = fs.dy;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());
		final Rectangle initialRect = new Rectangle(100, 100, 50, 50);

		final JPanel oPanel = new JPanel();
		oPanel.setOpaque(false);
		oPanel.setBorder(BorderFactory.createTitledBorder("Image I"));
		((TitledBorder) oPanel.getBorder()).setTitleFont(TITLE_FONT);
		owindow = new SlidingWindowComponent(image, initialRect);
		oPanel.add(owindow);
		base.add(oPanel);

		final Box gradPanel = Box.createVerticalBox();

		final JPanel dxPanel = new JPanel();
		dxPanel.setEnabled(false);
		dxPanel.setOpaque(false);
		dxPanel.setBorder(BorderFactory.createTitledBorder("Partial Derivative Ix"));
		((TitledBorder) dxPanel.getBorder()).setTitleFont(TITLE_FONT);
		dxwindow = new SlidingWindowComponent(dx.clone().normalise(), initialRect);
		dxPanel.add(dxwindow);
		gradPanel.add(dxPanel);

		final JPanel dyPanel = new JPanel();
		dyPanel.setEnabled(false);
		dyPanel.setOpaque(false);
		dyPanel.setBorder(BorderFactory.createTitledBorder("Partial Derivative Iy"));
		((TitledBorder) dyPanel.getBorder()).setTitleFont(TITLE_FONT);
		dywindow = new SlidingWindowComponent(dy.clone().normalise(), initialRect);
		dyPanel.add(dywindow);
		gradPanel.add(dyPanel);

		base.add(gradPanel);

		final Box dataPanel = Box.createVerticalBox();
		final JPanel stPanel = new JPanel(new GridBagLayout());
		stPanel.setOpaque(false);
		stPanel.setBorder(BorderFactory.createTitledBorder("Structure Tensor"));
		((TitledBorder) stPanel.getBorder()).setTitleFont(TITLE_FONT);
		final GridBagConstraints c = new GridBagConstraints();
		final JPanel matrix = new JPanel(new GridBagLayout());
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 0;
		c.gridx = 0;
		matrix.add(xxField = new JTextField(7), c);
		xxField.setFont(FONT);
		xxField.setEditable(false);
		xxField.setHorizontalAlignment(JTextField.RIGHT);
		c.gridx = 1;
		matrix.add(xyField = new JTextField(7), c);
		xyField.setFont(FONT);
		xyField.setEditable(false);
		xyField.setHorizontalAlignment(JTextField.RIGHT);
		c.gridy = 1;
		c.gridx = 0;
		matrix.add(yxField = new JTextField(7), c);
		yxField.setFont(FONT);
		yxField.setEditable(false);
		yxField.setHorizontalAlignment(JTextField.RIGHT);
		c.gridx = 1;
		matrix.add(yyField = new JTextField(7), c);
		yyField.setFont(FONT);
		yyField.setEditable(false);
		yyField.setHorizontalAlignment(JTextField.RIGHT);
		stPanel.add(matrix);

		final JPanel evalPanel = new JPanel(new GridBagLayout());
		evalPanel.setOpaque(false);
		evalPanel.setBorder(BorderFactory.createTitledBorder("Eigenvalues"));
		((TitledBorder) evalPanel.getBorder()).setTitleFont(TITLE_FONT);

		evalPanel.add(ev1Field = new JTextField(7));
		ev1Field.setFont(FONT);
		ev1Field.setEditable(false);
		ev1Field.setHorizontalAlignment(JTextField.RIGHT);

		evalPanel.add(ev2Field = new JTextField(7));
		ev2Field.setFont(FONT);
		ev2Field.setEditable(false);
		ev2Field.setHorizontalAlignment(JTextField.RIGHT);

		dataPanel.add(stPanel);
		dataPanel.add(evalPanel);
		base.add(dataPanel);

		final MouseAdapter ml = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				update();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				update();
			}
		};
		owindow.addMouseListener(ml);
		owindow.addMouseMotionListener(ml);

		dxwindow.addMouseListener(ml);
		dxwindow.addMouseMotionListener(ml);

		dywindow.addMouseListener(ml);
		dywindow.addMouseMotionListener(ml);

		update();

		return base;
	}

	private void update() {
		final Rectangle rect = owindow.getRect();
		owindow.setRect(rect);
		dxwindow.setRect(rect);
		dywindow.setRect(rect);

		// simple box window rather than gaussian:
		float ix2 = 0;
		float iy2 = 0;
		float ixy = 0;
		for (int y = (int) rect.y; y < rect.y + rect.height; y++) {
			for (int x = (int) rect.x; x < rect.x + rect.width; x++) {
				final float ix = dx.pixels[y][x];
				final float iy = dy.pixels[y][x];

				ix2 += ix * ix;
				iy2 += iy * iy;
				ixy += ix * iy;
			}
		}

		this.xxField.setText(String.format("%5.1f", ix2));
		this.xyField.setText(String.format("%5.1f", ixy));
		this.yxField.setText(String.format("%5.1f", ixy));
		this.yyField.setText(String.format("%5.1f", iy2));

		final Matrix m = new Matrix(new double[][] { { ix2, ixy }, { ixy, iy2 } });
		final double[] evs = m.eig().getRealEigenvalues();
		this.ev1Field.setText(String.format("%5.1f", evs[0]));
		this.ev2Field.setText(String.format("%5.1f", evs[1]));
	}

	@Override
	public void close() {
		// do nothing
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new StructureTensorDemo(), 1024, 768, Utils.BACKGROUND_IMAGE);
	}
}
