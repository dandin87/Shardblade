package tk.valoeghese.shardblade.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import tk.valoeghese.shardblade.item.HonorBlade;
import tk.valoeghese.shardblade.mechanics.IItemstackSurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;

@Mixin(ItemStack.class)
public class MixinItemStack implements IItemstackSurgebinder {
	@Shadow
	@Final
	private Item item;

	@Unique
	private SurgebindingOrder order = null;

	@Inject(method = "fromTag", at = @At("RETURN"))
	private static void loadTagData(CompoundTag tag, CallbackInfoReturnable<ItemStack> info) {
		ItemStack result = info.getReturnValue();

		if (!result.isEmpty()) {
			Item item = result.getItem();

			if (item instanceof HonorBlade) {
				if (tag.contains("tag", 10)) {
					CompoundTag itemTag = tag.getCompound("tag");
					((IItemstackSurgebinder) (Object) result).setOrder(SurgebindingOrder.byId(itemTag.getByte("order")));
				}
			}
		}
	}

	@Inject(method = "setTag", at = @At("RETURN"))
	private void setTag(CompoundTag tag, CallbackInfo info) {
		if (this.item instanceof HonorBlade) {
			this.setOrder(SurgebindingOrder.byId(tag.getByte("order")));
		}
	}

	@Override
	public SurgebindingOrder getOrder() {
		return this.order;
	}

	@Override
	public void setOrder(SurgebindingOrder order) {
		this.order = order;
	}
}
