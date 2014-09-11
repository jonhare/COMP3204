package uk.ac.soton.ecs.comp3204.utils;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;

public class SlidingWindowComponent extends ImageComponent implements MouseListener {
	private List<Operation<Rectangle>> rectMoveListeners = new ArrayList<Operation<Rectangle>>();
	private static final long serialVersionUID = 1L;
	private Rectangle rect;
	private FImage bg;
	private MBFImage rgbBg;
	private Point last;

	public SlidingWindowComponent(FImage image, Rectangle initial) {
		super(false, false);
		this.rect = initial;

		this.bg = image;
		updateImage();
		final Dimension dim = new Dimension(image.width, image.height);
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		this.setMinimumSize(dim);

		this.setShowPixelColours(false);
		this.setShowXYPosition(false);
		this.setAllowPanning(false);
		this.setAllowZoom(false);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void addRectangleMoveListener(Operation<Rectangle> listener) {
		this.rectMoveListeners.add(listener);
	}

	private void updateImage() {
		if (this.rect.x < 0)
			this.rect.x = 0;
		if (this.rect.y < 0)
			this.rect.y = 0;
		if (this.rect.x + rect.width >= this.bg.width)
			this.rect.x = this.bg.width - rect.width - 1;
		if (this.rect.y + rect.height >= this.bg.height)
			this.rect.y = this.bg.height - rect.height - 1;

		this.rgbBg = this.bg.toRGB();
		this.rgbBg.drawShape(rect, RGBColour.RED);

		this.setImage(this.image = ImageUtilities.createBufferedImageForDisplay(rgbBg));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isEnabled())
			return;

		if (last != null) {
			final Point current = e.getPoint();

			this.rect.translate(current.x - last.x, current.y - last.y);
			this.updateImage();
			last = current;
			fireRectMoved();
		}
	}

	private void fireRectMoved() {
		for (final Operation<Rectangle> op : rectMoveListeners)
			op.perform(rect);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!isEnabled())
			return;

		final Point pt = e.getPoint();
		if (rect.isInside(new Point2dImpl(pt.x, pt.y))) {
			this.last = pt;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!isEnabled())
			return;

		last = null;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!isEnabled())
			return;

		this.rect.x = e.getX() - rect.width / 2;
		this.rect.y = e.getY() - rect.height / 2;
		fireRectMoved();
		this.updateImage();
	}

	public void setRect(Rectangle rect) {
		this.rect = rect;
		this.updateImage();
		fireRectMoved();
	}

	public Rectangle getRect() {
		return rect;
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame();
		f.setSize(600, 600);
		final FImage img = new FImage(400, 400);
		img.fill(0.5f);
		f.add(new SlidingWindowComponent(img, new Rectangle(10, 10, 20, 20)));
		f.setVisible(true);
	}
}
