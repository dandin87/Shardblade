package tk.valoeghese.shardblade.mechanics.surgebinding;

public enum SurgebindingOrder {
	NONE(null, null, "None", 0),
	WINDRUNNER(Surge.ADHESION, Surge.GRAVITATION, "Jezrien", 1),
	SKYBREAKER(Surge.GRAVITATION, Surge.DIVISION, "Nalan", 2),
	DUSTBRINGER(Surge.DIVISION, Surge.ABRASION, "Chanarach", 3),
	EDGEDANCER(Surge.ABRASION, Surge.PROGRESSION, "Vedel", 4),
	TRUTHWATCHER(Surge.PROGRESSION, Surge.ILLUMINATION, "Paliah", 5),
	LIGHTWEAVER(Surge.ILLUMINATION, Surge.TRANSFORMATION, "Shalash", 6),
	ELSECALLER(Surge.TRANSFORMATION, Surge.TRANSPORTATION, "Batar", 7),
	WILLSHAPER(Surge.TRANSPORTATION, Surge.COHESION, "Kalak", 8),
	STONEWARD(Surge.COHESION, Surge.TENSION, "Talenel", 9),
	BONDSMITH(Surge.TENSION, Surge.ADHESION, "Ishar", 10);

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
