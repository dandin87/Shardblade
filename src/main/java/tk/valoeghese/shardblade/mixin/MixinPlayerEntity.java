package tk.valoeghese.shardblade.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.minecraft.level.entity.Entity;
import com.mojang.minecraft.level.entity.LivingEntity;
import com.mojang.minecraft.level.entity.player.PlayerEntity;
import com.mojang.minecraft.level.particle.ParticleEffect;
import com.mojang.minecraft.level.particle.ParticleTypes;
import com.mojang.minecraft.nbt.CompoundTag;
import com.mojang.minecraft.player.item.Item;
import com.mojang.minecraft.server.world.ServerWorld;
import com.mojang.minecraft.util.Hand;

import tk.valoeghese.shardblade.item.IShardblade;

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
		if (target.isAttackable()) {
			PlayerEntity self = (PlayerEntity) (Object) this;
			Item itemHeld = self.getStackInHand(Hand.field_5808).getItem();

			if (itemHeld instanceof IShardblade) {
//				IShardblade blade = (IShardblade) itemHeld;

				List<LivingEntity> list = self.world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0D, 0.25D, 1.0D));

				if (target instanceof LivingEntity) {
					list.add((LivingEntity) target);
				} else {
					target.kill();
				}

				list.forEach(le -> {
					double x = target.getX();
					double y = target.getEyeY();
					double z = target.getZ();

					((ServerWorld)self.world).spawnParticles(ParticleTypes.field_11209, x, y, z, 50, 0.0D, 0.1D, 0.0D, 0.2D);
					target.kill();
				});
			}
		}
	}
}
