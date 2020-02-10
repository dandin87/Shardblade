package tk.valoeghese.shardblade.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Shadow private boolean noClip;
	@Shadow private World world;
	@Shadow private Vec3d movementMultiplier;
	@Shadow private int fireTicks;
	@Shadow private Random random;
	@Shadow private boolean horizontalCollision;
	@Shadow private boolean verticalCollision;
	@Shadow private boolean inLava;
	@Shadow private boolean onGround;
	@Shadow private boolean collided;
	@Shadow private float distanceTraveled;
	@Shadow private float nextFlySoundDistance;
	@Shadow private float horizontalSpeed;
	@Shadow private float nextStepSoundDistance;

	@Overwrite
	public void move(MovementType type, Vec3d movement) {
		Entity self = (Entity) (Object) this;

		if (this.noClip) {
			this.setBoundingBox(this.getBoundingBox().offset(movement));
			this.moveToBoundingBoxCenter();
		} else {
			if (type == MovementType.PISTON) {
				movement = this.adjustMovementForPiston(movement);
				if (movement.equals(Vec3d.ZERO)) {
					return;
				}
			}

			this.world.getProfiler().push("move");
			if (this.movementMultiplier.lengthSquared() > 1.0E-7D) {
				movement = movement.multiply(this.movementMultiplier);
				this.movementMultiplier = Vec3d.ZERO;
				this.setVelocity(Vec3d.ZERO);
			}

			movement = this.adjustMovementForSneaking(movement, type);
			Vec3d vec3d = this.adjustMovementForCollisions(movement);
			if (vec3d.lengthSquared() > 1.0E-7D) {
				this.setBoundingBox(this.getBoundingBox().offset(vec3d));
				this.moveToBoundingBoxCenter();
			}

			this.world.getProfiler().pop();
			this.world.getProfiler().push("rest");
			this.horizontalCollision = !MathHelper.approximatelyEquals(movement.x, vec3d.x) || !MathHelper.approximatelyEquals(movement.z, vec3d.z);
			this.verticalCollision = movement.y != vec3d.y;
			this.onGround = this.verticalCollision && movement.y < 0.0D;
			this.collided = this.horizontalCollision || this.verticalCollision;
			BlockPos blockPos = this.getLandingPos();
			BlockState blockState = this.world.getBlockState(blockPos);
			this.fall(vec3d.y, this.onGround, blockState, blockPos);
			Vec3d vec3d2 = this.getVelocity();
			if (movement.x != vec3d.x) {
				this.setVelocity(0.0D, vec3d2.y, vec3d2.z);
			}

			if (movement.z != vec3d.z) {
				this.setVelocity(vec3d2.x, vec3d2.y, 0.0D);
			}

			Block block = blockState.getBlock();
			if (movement.y != vec3d.y) {
				block.onEntityLand(this.world, self);
			}

			if (this.onGround && !this.bypassesSteppingEffects()) {
				block.onSteppedOn(this.world, blockPos, self);
			}

			if (this.canClimb() && !this.hasVehicle()) {
				double d = vec3d.x;
				double e = vec3d.y;
				double f = vec3d.z;
				if (block != Blocks.LADDER && block != Blocks.SCAFFOLDING) {
					e = 0.0D;
				}

				this.horizontalSpeed = (float)((double)this.horizontalSpeed + (double)MathHelper.sqrt(squaredHorizontalLength(vec3d)) * 0.6D);
				this.distanceTraveled = (float)((double)this.distanceTraveled + (double)MathHelper.sqrt(d * d + e * e + f * f) * 0.6D);
				if (this.distanceTraveled > this.nextStepSoundDistance && !blockState.isAir()) {
					this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
					if (this.isTouchingWater()) {
						Entity entity = this.hasPassengers() && this.getPrimaryPassenger() != null ? this.getPrimaryPassenger() : self;
						float g = entity == self ? 0.35F : 0.4F;
						Vec3d vec3d3 = entity.getVelocity();
						float h = MathHelper.sqrt(vec3d3.x * vec3d3.x * 0.20000000298023224D + vec3d3.y * vec3d3.y + vec3d3.z * vec3d3.z * 0.20000000298023224D) * g;
						if (h > 1.0F) {
							h = 1.0F;
						}

						this.playSwimSound(h);
					} else {
						this.playStepSound(blockPos, blockState);
					}
				} else if (this.distanceTraveled > this.nextFlySoundDistance && this.hasWings() && blockState.isAir()) {
					this.nextFlySoundDistance = this.playFlySound(this.distanceTraveled);
				}
			}

			try {
				this.inLava = false;
				this.checkBlockCollision();
			} catch (Throwable var18) {
				CrashReport crashReport = CrashReport.create(var18, "Checking entity block collision");
				CrashReportSection crashReportSection = crashReport.addElement("Entity being checked for collision");
				this.populateCrashReport(crashReportSection);
				throw new CrashException(crashReport);
			}

			this.setVelocity(this.getVelocity().multiply((double)this.getVelocityMultiplier(), 1.0D, (double)this.getVelocityMultiplier()));
			if (!this.world.doesAreaContainFireSource(this.getBoundingBox().contract(0.001D)) && this.fireTicks <= 0) {
				this.fireTicks = -this.getBurningDuration();
			}

			if (this.isWet() && this.isOnFire()) {
				this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
				this.fireTicks = -this.getBurningDuration();
			}

			this.world.getProfiler().pop();
		}
	}

	@Shadow
	private float calculateNextStepSoundDistance() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Entity getPrimaryPassenger() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean hasPassengers() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private float playFlySound(float distance) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void playSwimSound(float volume) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean isTouchingWater() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private static double squaredHorizontalLength(Vec3d vec3d) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean hasVehicle() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean canClimb() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean bypassesSteppingEffects() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void playStepSound(BlockPos blockPos, BlockState blockState) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean hasWings() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void populateCrashReport(CrashReportSection crashReportSection) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void setVelocity(double d, double y, double z) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void fall(double y, boolean onGround2, BlockState blockState, BlockPos blockPos) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private float getVelocityMultiplier() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private BlockPos getLandingPos() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	protected void checkBlockCollision() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void playSound(SoundEvent sound, float volume, float pitch) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Vec3d adjustMovementForCollisions(Vec3d movement) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean isWet() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private boolean isOnFire() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Vec3d getVelocity() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private int getBurningDuration() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void setVelocity(Vec3d vec) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void setBoundingBox(Box box) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Vec3d adjustMovementForPiston(Vec3d movement) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private void moveToBoundingBoxCenter() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
	@Shadow
	private Box getBoundingBox() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinEntity");
	}
}
