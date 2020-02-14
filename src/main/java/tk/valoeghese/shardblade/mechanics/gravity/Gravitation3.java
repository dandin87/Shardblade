package tk.valoeghese.shardblade.mechanics.gravity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Gravitation3 {
	public static void changeGravity(LivingEntity user, Vec3d rawLookVector, float yaw, float pitch) {
		Vec3d lookVector = clampToAxes(rawLookVector, pitch, yaw);
		float x = (float) lookVector.x;
		float y = (float) lookVector.y;
		float z = (float) lookVector.z;
		x = (float) (lookVector.x * GRAVITATIONAL_CONSTANT);
		y = (float) (lookVector.y * GRAVITATIONAL_CONSTANT);
		z = (float) (lookVector.z * GRAVITATIONAL_CONSTANT);
		((I3DGravitation) (Object) user).setGravitation(x, y, z, GRAVITATIONAL_CONSTANT, yaw, pitch);
	}

	public static void jump(Vec3d currentVelocity, Vec3d jumpVector, LivingEntity entity) {
		entity.setVelocity(currentVelocity.x + jumpVector.x, currentVelocity.y + jumpVector.y, currentVelocity.z + jumpVector.z);
		entity.setVelocity(currentVelocity.x + jumpVector.x, currentVelocity.y + jumpVector.y, currentVelocity.z + jumpVector.z);

		// todo use 3
		if (entity.isSprinting()) {
			float yawRadians = entity.yaw * 0.017453292F;
			entity.setVelocity(entity.getVelocity().add((double)(-MathHelper.sin(yawRadians) * 0.2F), 0.0D, (double)(MathHelper.cos(yawRadians) * 0.2F)));
		}
	}

	public static boolean onGround(Vec3d collision, Vec3d movement) {
		return !(Math.abs(strength(movement.subtract(collision))) < 9.999999747378752E-6D);
	}

	public static double fallDamageSpeed(Vec3d rawMotion, Vec3d gravity, boolean xCol, boolean yCol, boolean zCol/*, Vec3d gravitation*/) {
		//return strength(gravitation.normalize().multiply(movement));
		Vec3d movement = rawMotion;//perhapsInvertFor3dGravity(rawMotion, gravity);
		return movement.subtract(xCol ? movement.x : 0, yCol ? movement.y : 0, zCol ? movement.z : 0).length();
	}

	/**
	 * @deprecated doesn't work properly lol but keeping this in case I need it and because its cursed
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static Vec3d perhapsInvertFor3dGravity(Vec3d motion, Vec3d gravity) {
		return new Vec3d(gravity.x < 0 ? -motion.x : motion.x, gravity.y < 0 ? -motion.y : motion.y, gravity.z < 0 ? -motion.z : motion.z);
	}

	// I just realised this is redundant because there's vec.length() ~ valo, 4:38pm 14/02/20 NZST
	public static double strength(Vec3d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
	}

	/**
	 * Will return the input Vec3d if down. Else will return a rotated Vec3d aligned to the axes.
	 */
	public static Vec3d rotateAligned(Vec3d raw, float pitch, float yaw, float gravity) {
		return rotateAligned(raw, getRotation(yaw, pitch), gravity);
	}

	public static Vec3d rotateAligned(Vec3d raw, Direction rotation, float gravity) {
		// will probably change when model and camera rotation are added
		switch (rotation) {
		case UP:
			return new Vec3d(raw.x, raw.y, raw.z).multiply(GRAVITATIONAL_CONSTANT / gravity);
		case DOWN:
			return raw.multiply(GRAVITATIONAL_CONSTANT / gravity);
		case NORTH:
			return new Vec3d(raw.x, raw.z, raw.y).multiply(GRAVITATIONAL_CONSTANT / gravity);
		case WEST:
			return new Vec3d(raw.x, raw.z, raw.y).multiply(GRAVITATIONAL_CONSTANT / gravity);
		case EAST:
			return new Vec3d(raw.y, raw.x, raw.z).multiply(GRAVITATIONAL_CONSTANT / gravity);
		case SOUTH:
			return new Vec3d(raw.y, raw.x, raw.z).multiply(GRAVITATIONAL_CONSTANT / gravity);
		default:
			throw new RuntimeException("Invalid Direction argument for rotateAligned!");
		}
	}

	// currently identical to rotateAligned
	public static Vec3d revertAlignedRotation(Vec3d raw, Direction rotation, float gravity) {
		// will probably change when model and camera rotation are added
		switch (rotation) {
		case UP:
			return new Vec3d(raw.x, raw.y, raw.z).multiply(gravity / GRAVITATIONAL_CONSTANT);
		case DOWN:
			return raw.multiply(gravity / GRAVITATIONAL_CONSTANT);
		case NORTH:
			return new Vec3d(raw.x, raw.z, raw.y).multiply(gravity / GRAVITATIONAL_CONSTANT);
		case WEST:
			return new Vec3d(raw.x, raw.z, raw.y).multiply(gravity / GRAVITATIONAL_CONSTANT);
		case EAST:
			return new Vec3d(raw.y, raw.x, raw.z).multiply(gravity / GRAVITATIONAL_CONSTANT);
		case SOUTH:
			return new Vec3d(raw.y, raw.x, raw.z).multiply(gravity / GRAVITATIONAL_CONSTANT);
		default:
			throw new RuntimeException("Invalid Direction argument for rotateAligned!");
		}
	}

	public static Direction getRotation(float yaw, float pitch) {
		if (pitch < -45) {
			return Direction.UP;
		} else if (pitch > 45) {
			return Direction.DOWN;
		} else if (yaw > 135 || yaw < -135) {
			return Direction.NORTH;
		} else if (yaw > 45) {
			return Direction.WEST;
		} else if (yaw < -45) {
			return Direction.EAST;
		} else {
			return Direction.WEST;
		}
	}

	public static Vec3d clampToAxes(Vec3d in, float pitch, float yaw) {
		if (pitch < CLAMP_RADIUS - 90) {
			return UP;
		} else if (pitch > 90 - CLAMP_RADIUS) {
			return DOWN;
		} else if (pitch < CLAMP_RADIUS && pitch > -CLAMP_RADIUS) {
			if (yaw > 180 - CLAMP_RADIUS || yaw < CLAMP_RADIUS - 180) {
				return NORTH;
			} else if (yaw < CLAMP_RADIUS && yaw > -CLAMP_RADIUS) {
				return SOUTH;
			} else if (yaw > 80 - CLAMP_RADIUS && yaw < 100 - CLAMP_RADIUS) {
				return WEST;
			} else if (yaw < CLAMP_RADIUS - 80 && yaw > CLAMP_RADIUS - 100) {
				return EAST;
			}
		}

		return in;
	}

	private static final float CLAMP_RADIUS = 10; // in degrees
	private static final Vec3d UP = new Vec3d(0, 1, 0);
	private static final Vec3d DOWN = new Vec3d(0, -1, 0);
	private static final Vec3d NORTH = new Vec3d(0, 0, -1);
	private static final Vec3d WEST = new Vec3d(-1, 0, 0);
	private static final Vec3d EAST = new Vec3d(1, 0, 0);
	private static final Vec3d SOUTH = new Vec3d(0, 0, 1);

	public static final float GRAVITATIONAL_CONSTANT = 0.08f;

	// [with radian yaw/pitch]
	//	Vec3d modifiedVector = new Vec3d(rawMovementInput.x, rawMovementInput.z, 0);
	//	modifiedVector = rawMovementInput.rotateX(yaw).rotateY(pitch);
	//	return new Vec3d(modifiedVector.x, modifiedVector.z, modifiedVector.y);
}
