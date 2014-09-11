package uk.ac.soton.ecs.comp3204.utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.util.function.Operation;

/**
 * A reusable swing component for video input
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PixelDrawingComponent extends Box {
	private static final long serialVersionUID = 1L;
	private ImageComponent display;
	private FImage image;
	private BufferedImage bimage;
	private int sf;
	private List<Operation<FImage>> actionListeners = new ArrayList<Operation<FImage>>();

	public PixelDrawingComponent(int displaywidth, final int imwidth) {
		super(BoxLayout.Y_AXIS);
		this.setOpaque(false);

		image = new FImage(imwidth, imwidth);
		bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage);

		display = new ImageComponent(false, false);
		display.setPreferredSize(new Dimension(displaywidth, displaywidth));
		display.setMaximumSize(new Dimension(displaywidth, displaywidth));
		display.setMinimumSize(new Dimension(displaywidth, displaywidth));
		display.setShowPixelColours(false);
		display.setShowXYPosition(false);
		display.setAllowPanning(false);
		display.setAllowZoom(false);
		sf = displaywidth / imwidth;
		display.zoom(sf);
		display.setImage(bimage);

		final MouseAdapter adaptor = new MouseAdapter() {
			FValuePixel lastDragged;

			@Override
			public void mouseClicked(MouseEvent e) {
				final int x = e.getX() / sf;
				final int y = e.getY() / sf;

				if (x < 0 || x >= image.width || y < 0 || y >= image.height)
					return;

				image.pixels[y][x] = image.pixels[y][x] == 0 ? 1 : 0;

				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				lastDragged = null;
				fire();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				final int x = e.getX() / sf;
				final int y = e.getY() / sf;

				if (x < 0 || x >= image.width || y < 0 || y >= image.height)
					return;

				if (lastDragged != null && lastDragged.x == x && lastDragged.y == y)
					return;

				float c;
				if (lastDragged == null)
					c = image.pixels[y][x] == 0 ? 1 : 0;
				else
					c = lastDragged.value;

				image.pixels[y][x] = c;

				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				lastDragged = new FValuePixel(x, y, c);
				fire();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				lastDragged = null;
			}
		};

		display.addMouseListener(adaptor);
		display.addMouseMotionListener(adaptor);
		add(display);

		final JPanel ctrlsPanel = new JPanel(new WrapLayout());
		ctrlsPanel.setOpaque(false);
		ctrlsPanel.setSize(displaywidth + 100, 1);

		final JButton clrBtn = new JButton("Clear");
		clrBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image.fill(0);
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}
		});
		ctrlsPanel.add(clrBtn);

		final JButton rotBtn = new JButton("Rotate 90");
		rotBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int j = 0; j < image.height; j++) {
					for (int i = j; i < image.width; i++) {
						final float tmp = image.pixels[j][i];
						image.pixels[j][i] = image.pixels[i][j];
						image.pixels[i][j] = tmp;
					}
				}
				image.flipX();
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}
		});
		ctrlsPanel.add(rotBtn);

		final JButton nlBtn = new JButton("Nudge Left");
		nlBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image = image.shiftLeft();
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}
		});
		ctrlsPanel.add(nlBtn);

		final JButton nrBtn = new JButton("Nudge Right");
		nrBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image = image.shiftRight();
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}

		});
		ctrlsPanel.add(nrBtn);

		final JButton nuBtn = new JButton("Nudge Up");
		nuBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image = image.shiftUp();
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}
		});
		ctrlsPanel.add(nuBtn);

		final JButton ndBtn = new JButton("Nudge Down");
		ndBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				image = image.shiftDown();
				display.setImage(bimage = ImageUtilities.createBufferedImageForDisplay(image, bimage));
				fire();
			}

		});
		ctrlsPanel.add(ndBtn);

		add(ctrlsPanel);
	}

	private void fire() {
		for (final Operation<FImage> op : actionListeners)
			op.perform(image);

	}

	public void addActionListener(Operation<FImage> actionListener) {
		this.actionListeners.add(actionListener);
	}

	public FImage getImage() {
		return this.image;
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame();
		f.setSize(600, 600);
		f.add(new PixelDrawingComponent(400, 10));
		f.setVisible(true);
	}
}
