package tk.valoeghese.shardblade.mechanics;

import tk.valoeghese.shardblade.mechanics.surgebinding.ISurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;

public interface IItemstackSurgebinder extends ISurgebinder {
	void setOrder(SurgebindingOrder order);
}
