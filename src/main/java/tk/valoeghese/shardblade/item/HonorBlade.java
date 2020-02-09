package tk.valoeghese.shardblade.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import tk.valoeghese.shardblade.item.util.ITaggedItem;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;
import tk.valoeghese.shardblade.registry.RegistryInfo;

@RegistryInfo("honorblade")
public class HonorBlade extends ShardbladeBase implements ITaggedItem {
	public HonorBlade() {
		super(new Item.Settings().maxCount(1));
	}

	private SurgebindingOrder order;

	public SurgebindingOrder getOrder() {
		return this.order;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		if (this.order != null) {
			tooltip.add(new LiteralText("§6" + "Of " + "§l" + this.order.herald));
			tooltip.add(new LiteralText("§o" + this.order.surge0.surgeName + ", " + this.order.surge1.surgeName));
		}
	}

	@Override
	public float getAttackSpeedModifier() {
		return -2.9f;
	}

	@Override
	public void loadFromTag(CompoundTag tag) {
		this.order = SurgebindingOrder.byId(tag.getByte("order"));
	}

	@Override
	public void writeToTag(CompoundTag tag) {
		if (this.order != null) {
			tag.putByte("order", this.order.id);
		}
	}
}
