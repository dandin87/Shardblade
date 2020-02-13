package tk.valoeghese.shardblade.mechanics;

import tk.valoeghese.shardblade.mechanics.surgebinding.ISurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;

public interface ISurgebindingItem extends ISurgebinder {
	void setOrder(SurgebindingOrder order);
}
