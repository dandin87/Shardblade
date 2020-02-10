package tk.valoeghese.shardblade.mechanics.surgebinding;

public enum SurgebindingOrder {
	NONE(null, null, "None", 0),
	BONDSMITH(Surge.TENSION, Surge.ADHESION, "Ishar", 1),
	WINDRUNNER(Surge.ADHESION, Surge.GRAVITATION, "Jezrien", 2),
	SKYBREAKER(Surge.GRAVITATION, Surge.DIVISION, "Nalan", 3),
	DUSTBRINGER(Surge.DIVISION, Surge.ABRASION, "Chanarach", 4),
	EDGEDANCER(Surge.ABRASION, Surge.PROGRESSION, "Vedel", 5),
	TRUTHWATCHER(Surge.PROGRESSION, Surge.ILLUMINATION, "Paliah", 6),
	LIGHTWEAVER(Surge.ILLUMINATION, Surge.TRANSFORMATION, "Shalash", 7),
	ELSECALLER(Surge.TRANSFORMATION, Surge.TRANSPORTATION, "Batar", 8),
	WILLSHAPER(Surge.TRANSPORTATION, Surge.COHESION, "Kalak", 9),
	STONEWARD(Surge.COHESION, Surge.TENSION, "Talenel", 10);

	private SurgebindingOrder(Surge surge0, Surge surge1, String herald, int id) {
		this.surge0 = surge0;
		this.surge1 = surge1;
		this.herald = herald;
		this.id = (byte) id;
		SurgebindingData.ORDER_BY_ID.put(this.id, this);
	}

	public boolean hasSurge(Surge surge) {
		return this.surge0 == surge || this.surge1 == surge;
	}

	public final Surge surge0, surge1;
	public final String herald;
	public final byte id;

	public static SurgebindingOrder byId(byte id) {
		return SurgebindingData.ORDER_BY_ID.getOrDefault(id, NONE);
	}

	public static boolean isNone(SurgebindingOrder order) {
		return order == NONE || order == null;
	}
}
