package uk.ac.soton.ecs.comp3005.utils;

import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

public class Simple3D {
	public static Point2dImpl projectOrtho(Matrix pt) {
		final Point2dImpl po = new Point2dImpl();

		po.x = (float) pt.get(0, 0);
		po.y = (float) pt.get(1, 0);

		return po;
	}

	public static Matrix euler2Rot(final double pitch, final double yaw, final double roll)
	{
		Matrix R;
		R = new Matrix(3, 3);

		final double sina = Math.sin(pitch), sinb = Math.sin(yaw), sinc = Math
				.sin(roll);
		final double cosa = Math.cos(pitch), cosb = Math.cos(yaw), cosc = Math
				.cos(roll);
		R.set(0, 0, cosb * cosc);
		R.set(0, 1, -cosb * sinc);
		R.set(0, 2, sinb);
		R.set(1, 0, cosa * sinc + sina * sinb * cosc);
		R.set(1, 1, cosa * cosc - sina * sinb * sinc);
		R.set(1, 2, -sina * cosb);

		addOrthRow(R);

		return R;
	}

	static void addOrthRow(Matrix R) {
		assert ((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));

		R.set(2, 0, R.get(0, 1) * R.get(1, 2) - R.get(0, 2) * R.get(1, 1));
		R.set(2, 1, R.get(0, 2) * R.get(1, 0) - R.get(0, 0) * R.get(1, 2));
		R.set(2, 2, R.get(0, 0) * R.get(1, 1) - R.get(0, 1) * R.get(1, 0));
	}
}
