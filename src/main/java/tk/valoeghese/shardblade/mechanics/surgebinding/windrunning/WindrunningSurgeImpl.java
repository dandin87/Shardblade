package tk.valoeghese.shardblade.mechanics.surgebinding.windrunning;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class WindrunningSurgeImpl {
	public static void changeGravity(PlayerEntity user, Vec3d lookVector) {
//		float y = GRAVITY_CONSTANT * MathHelper.sin(pitch);
//		float h = GRAVITY_CONSTANT * MathHelper.cos(pitch); // h horizontal plane
//		float x = h * -MathHelper.sin(yaw);
//		float z = h * MathHelper.cos(yaw);
		float x = (float) (lookVector.x * GRAVITY_CONSTANT);
		float y = (float) (lookVector.y * GRAVITY_CONSTANT);
		float z = (float) (lookVector.z * GRAVITY_CONSTANT);
		((IWindrunnerGravity) (Object) user).setGravitation(x, y, z);
	}

	private static final float GRAVITY_CONSTANT = 0.08f;
}
