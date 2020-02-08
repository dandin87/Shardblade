package tk.valoeghese.shardblade.mixinimpl;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import tk.valoeghese.shardblade.item.IShardblade;

public final class ShardbladeMixinImpl {
	private ShardbladeMixinImpl() {
	}

	public static boolean onAttack(PlayerEntity self, Entity target) {
		if (target.isAttackable()) {
			Item itemHeld = self.getStackInHand(Hand.MAIN_HAND).getItem();

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

					if (self.world instanceof ServerWorld) {
						((ServerWorld)self.world).spawnParticles(ParticleTypes.SMOKE, x, y, z, 10, 0.0D, 0.1D, 0.0D, 0.02D);
					}
					target.kill();
				});

				return true;
			}
		}

		return false;
	}
}
