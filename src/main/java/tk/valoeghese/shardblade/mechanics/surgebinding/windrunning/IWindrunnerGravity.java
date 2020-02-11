package tk.valoeghese.shardblade.mechanics.surgebinding.windrunning;

public interface IWindrunnerGravity {
	void setGravitation(float x, float y, float z, float gravityCache, float yawCache, float pitchCache);
	float[] getGravitation();
}
