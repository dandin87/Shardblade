package tk.valoeghese.shardblade.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import tk.valoeghese.shardblade.mechanics.surgebinding.ISurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.Surge;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;
import tk.valoeghese.shardblade.mechanics.surgebinding.windrunning.WindrunningSurgeImpl;
import tk.valoeghese.shardblade.registry.RegistryInfo;

@RegistryInfo("honorblade")
public class HonorBlade extends ShardbladeBase {
	public HonorBlade() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		SurgebindingOrder order = ((ISurgebinder) (Object) stack).getOrder();

		if (!SurgebindingOrder.isNone(order)) {
			tooltip.add(new LiteralText("§6" + "Of " + "§l" + order.herald));
			tooltip.add(new LiteralText("§o" + order.surge0.surgeName + ", " + order.surge1.surgeName));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		SurgebindingOrder order = ((ISurgebinder) (Object) stack).getOrder();

		if (order != null) {
			if (order.hasSurge(Surge.GRAVITATION)) {
				// * 0.017453292F if doing builtin mc maths since mc uses radians
				WindrunningSurgeImpl.changeGravity(user, user.getRotationVector(), user.yaw, user.pitch);
			}
		}

		return super.use(world, user, hand);
	}

	@Override
	public float getAttackSpeedModifier() {
		return -2.7f;
	}
}
