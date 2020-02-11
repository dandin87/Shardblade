package tk.valoeghese.shardblade.debug;

import net.minecraft.util.math.Vec3d;

public class Yeeter {
	public static <T> void yeet(T t) {
		if (t instanceof Vec3d) {
			if (((Vec3d) t).getX() == 0) {
				return;
			}
		}
		t.getClass();
	}
}
