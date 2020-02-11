package tk.valoeghese.shardblade.mechanics.surgebinding.windrunning;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WindrunningSurgeImpl {
	public static void changeGravity(PlayerEntity user, Vec3d lookVector) {
		float x = (float) fixApproximations(lookVector.x);
		float y = (float) fixApproximations(lookVector.y);
		float z = (float) fixApproximations(lookVector.z);
		x = (float) (lookVector.x * GRAVITATIONAL_CONSTANT);
		y = (float) (lookVector.y * GRAVITATIONAL_CONSTANT);
		z = (float) (lookVector.z * GRAVITATIONAL_CONSTANT);
		((IWindrunnerGravity) (Object) user).setGravitation(x, y, z);
	}

	private static double fixApproximations(double in) {
		if (Math.abs(in) < 0.15) {
			return 0;
		} else if (Math.abs(in - 1) < 0.15) {
			return 1;
		} else if (Math.abs(in + 1) < 0.15) {
			return -1;
		}

		return in;
	}

	public static void jump(Vec3d currentVelocity, Vec3d jumpVector, LivingEntity entity) {
		entity.setVelocity(currentVelocity.x + jumpVector.x, currentVelocity.y + jumpVector.y, currentVelocity.z + jumpVector.z);
		entity.setVelocity(currentVelocity.x + jumpVector.x, currentVelocity.y + jumpVector.y, currentVelocity.z + jumpVector.z);

		// todo use 3d
		if (entity.isSprinting()) {
			float yawForTrig = entity.yaw * 0.017453292F;
			entity.setVelocity(entity.getVelocity().add((double)(-MathHelper.sin(yawForTrig) * 0.2F), 0.0D, (double)(MathHelper.cos(yawForTrig) * 0.2F)));
		}
	}

	public static boolean onGround(Vec3d collision, Vec3d movement) {
		return !(Math.abs(strength(movement.subtract(collision))) < 9.999999747378752E-6D);
	}

	private static double strength(Vec3d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
	}

	private static final float GRAVITATIONAL_CONSTANT = 0.08f;
}
