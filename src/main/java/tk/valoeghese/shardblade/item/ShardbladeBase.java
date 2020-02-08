package tk.valoeghese.shardblade.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;

public abstract class ShardbladeBase extends Item implements IShardblade {
	public ShardbladeBase(Settings settings) {
		super(settings);
	}

	public boolean isEffectiveOn(BlockState state) {
		return true;
	}
}
