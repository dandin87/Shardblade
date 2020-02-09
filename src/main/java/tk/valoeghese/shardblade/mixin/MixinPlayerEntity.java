package tk.valoeghese.shardblade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import tk.valoeghese.shardblade.mechanics.ShardbladeMechanics;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
	@Inject(at = @At("RETURN"), method = "readCustomDataFromTag")
	private void injectReadData(CompoundTag tag, CallbackInfo info) {
	}

	@Inject(at = @At("RETURN"), method = "writeCustomDataToTag")
	private void injectWriteData(CompoundTag tag, CallbackInfo info) {
	}
	
	@Inject(at = @At("HEAD"), method = "attack", cancellable = true)
	private void onAttack(Entity target, CallbackInfo info) {
		if (ShardbladeMechanics.onAttack((PlayerEntity) (Object) this, target)) {
			info.cancel();
		}
	}
}
