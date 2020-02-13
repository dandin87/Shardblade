package tk.valoeghese.shardblade.mechanics.gravity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface I3DGravitation {
	void setGravitation(float x, float y, float z, float gravityCache, float yawCache, float pitchCache);
	float[] getGravitation();
	float getCachedYaw();
	float getCachedPitch();
	float getCachedGravitationalStrength();
	void handle3DFall(Vec3d motion, boolean onGround, BlockState blockState, BlockPos blockPos);
}
