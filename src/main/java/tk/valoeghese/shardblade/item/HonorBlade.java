package tk.valoeghese.shardblade.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import tk.valoeghese.shardblade.mechanics.IItemstackSurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;
import tk.valoeghese.shardblade.registry.RegistryInfo;

@RegistryInfo("honorblade")
public class HonorBlade extends ShardbladeBase {
	public HonorBlade() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		SurgebindingOrder order = ((IItemstackSurgebinder) (Object) stack).getOrder();

		if (order != null) {
			tooltip.add(new LiteralText("§6" + "Of " + "§l" + order.herald));
			tooltip.add(new LiteralText("§o" + order.surge0.surgeName + ", " + order.surge1.surgeName));
		}
	}

	@Override
	public float getAttackSpeedModifier() {
		return -2.9f;
	}
}
