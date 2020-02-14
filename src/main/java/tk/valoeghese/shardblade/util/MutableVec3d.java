package tk.valoeghese.shardblade.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MutableVec3d {
	public MutableVec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double x, y, z;

	public Vec3d immutable() {
		return new Vec3d(this.x, this.y, this.z);
	}

	public double length() {
		return MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public MutableVec3d withAdded(MutableVec3d vec3d) {
		return this.withAdded(vec3d.x, vec3d.y, vec3d.z);
	}

	public MutableVec3d withAdded(double x, double y, double z) {
		return new MutableVec3d(this.x + x, this.y + y, this.z + z);
	}

	public MutableVec3d add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public static MutableVec3d make() {
		return new MutableVec3d(0, 0, 0);
	}
}
