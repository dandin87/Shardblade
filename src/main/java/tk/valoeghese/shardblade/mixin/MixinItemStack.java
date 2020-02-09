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
import tk.valoeghese.shardblade.item.util.ITaggedItem;

@Mixin(ItemStack.class)
public class MixinItemStack {
	@Shadow
	@Final
	private Item item;

	@Inject(method = "fromTag", at = @At("RETURN"))
	private static void loadTagData(CompoundTag tag, CallbackInfoReturnable<ItemStack> info) {
		ItemStack result = info.getReturnValue();

		if (!result.isEmpty()) {
			Item item = result.getItem();

			if (item instanceof ITaggedItem) {
				if (tag.contains("tag", 10)) {
					CompoundTag itemTag = tag.getCompound("tag");
					((ITaggedItem) item).loadFromTag(itemTag);
				}
			}
		}
	}

	@Inject(method = "setTag", at = @At("RETURN"))
	private void setTag(CompoundTag tag, CallbackInfo info) {
		if (this.item instanceof ITaggedItem) {
			((ITaggedItem) item).loadFromTag(tag);
		}
	}
}
