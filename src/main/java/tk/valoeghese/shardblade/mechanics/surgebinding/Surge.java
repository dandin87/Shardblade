package tk.valoeghese.shardblade.mechanics.surgebinding;

public enum Surge {
	ADHESION("Adhesion"),
	GRAVITATION("Gravitation"),
	DIVISION("Division"),
	ABRASION("Abrasion"),
	PROGRESSION("Progression"),
	ILLUMINATION("Illumination"),
	TRANSFORMATION("Transformation"),
	TRANSPORTATION("Transportation"),
	COHESION("Cohesion"),
	TENSION("Tension");

	private Surge(String name) {
		this.surgeName = name;
	}

	public final String surgeName;
}
