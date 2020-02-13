package tk.valoeghese.shardblade.mechanics.surgebinding;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;

final class SurgebindingData {
	private SurgebindingData() {
	}

	static final Byte2ObjectMap<SurgebindingOrder> ORDER_BY_ID = new Byte2ObjectArrayMap<>();
}
