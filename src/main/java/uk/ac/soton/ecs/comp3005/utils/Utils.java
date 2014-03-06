package uk.ac.soton.ecs.comp3005.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utils {
	public static BufferedImage BACKGROUND_IMAGE = null;
	static {
		try {
			BACKGROUND_IMAGE = ImageIO.read(Utils.class.getResource("/uk/ac/soton/ecs/comp3005/background.png"));
		} catch (final IOException e) {
		}
	}

	private Utils() {
	}
}
