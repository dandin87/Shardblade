package tk.valoeghese.shardblade.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import tk.valoeghese.shardblade.registry.RegistryInfo;

@RegistryInfo("shardblade")
public class Shardblade extends ShardbladeBase {
	public Shardblade() {
		super(new Item.Settings().maxCount(1).group(ItemGroup.TOOLS));
	}

	@Override
	public float getAttackSpeedModifier() {
		return -2.85f;
	}
}
