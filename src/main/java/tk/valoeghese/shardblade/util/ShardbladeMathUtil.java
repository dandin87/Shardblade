package tk.valoeghese.shardblade.util;

public final class ShardbladeMathUtil {
	private ShardbladeMathUtil() {
	}

	/**
	 * @return negative of the absolute value of the given float
	 */
	public static float nAbs(float arg0) {
		return arg0 > 0 ? -arg0 : arg0;
	}
}
