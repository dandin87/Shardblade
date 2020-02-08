package tk.valoeghese.shardblade.item;

import net.minecraft.item.Item;
import tk.valoeghese.shardblade.registry.RegistryInfo;

@RegistryInfo("honorblade")
public class HonorBlade extends ShardbladeBase implements IShardblade {
	public HonorBlade() {
		super(new Item.Settings());
	}
}
