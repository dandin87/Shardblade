package tk.valoeghese.shardblade.mechanics.gravity;

public interface I3DGravitation {
	void setGravitation(float x, float y, float z, float gravityCache, float yawCache, float pitchCache);
	float[] getGravitation();
	float getCachedYaw();
	float getCachedPitch();
	float getCachedGravitationalStrength();
}
