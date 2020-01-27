package tk.valoeghese.shardblade.item;

import com.mojang.minecraft.level.block.BlockState;
import com.mojang.minecraft.player.item.Item;

public abstract class ShardbladeBase extends Item implements IShardblade {
	public ShardbladeBase(Settings settings) {
		super(settings);
		
	}

	public boolean isEffectiveOn(BlockState state) {
		return true;
	}
}
