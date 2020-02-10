package tk.valoeghese.shardblade.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	private SurgebindingOrder surgebindingOrder;

	@Shadow
	private CompoundTag getOrCreateTag() {
		throw new RuntimeException("Error failed in shadowing method ItemStack#getOrCreateTag");
	}

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
		if (this.item instanceof HonorBlade) {
			if (this.surgebindingOrder == null) {
				SurgebindingOrder result = SurgebindingOrder.byId(this.getOrCreateTag().getByte("order"));
				this.surgebindingOrder = result;
				return this.surgebindingOrder;
			} else {
				return this.surgebindingOrder;
			}
		}

		return null;
	}

	@Override
	public void setOrder(SurgebindingOrder order) {
		if (this.item instanceof HonorBlade) {
			if (order == null) {
				order = SurgebindingOrder.NONE;
			}

			this.getOrCreateTag().putByte("order", order.id);
			this.surgebindingOrder = order;
		}
	}
}
