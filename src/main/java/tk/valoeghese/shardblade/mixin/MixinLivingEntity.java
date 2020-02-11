package tk.valoeghese.shardblade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tk.valoeghese.shardblade.mechanics.IShardbladeAffectedEntity;
import tk.valoeghese.shardblade.mechanics.surgebinding.windrunning.IWindrunnerGravity;
import tk.valoeghese.shardblade.mechanics.surgebinding.windrunning.WindrunningSurgeImpl;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements IShardbladeAffectedEntity, IWindrunnerGravity {
	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private boolean incapacitatedByShardblade = false;

	private float wrGravityX = 0.0f; // wr = windrunner, since 3d gravity was made for windrunning
	private float wrGravityY = -0.08f;
	private float wrGravityZ = 0.0f;
	private float wrCurrentGravity = 0.08f;
	private float wrCurrentYaw = 0;
	private float wrCurrentPitch = 90;

	@Shadow private float lastLimbDistance;
	@Shadow private float limbDistance;
	@Shadow private float limbAngle;
	@Shadow private boolean jumping;
	@Shadow private int pushCooldown;
	@Shadow private int jumpingCooldown;
	@Shadow private float sidewaysSpeed;
	@Shadow private float forwardSpeed;
	@Shadow private float upwardSpeed;
	@Shadow private int bodyTrackingIncrements;
	@Shadow private double serverX;
	@Shadow private double serverY;
	@Shadow private double serverZ;
	@Shadow private double serverYaw;
	@Shadow private double serverPitch;
	@Shadow private double serverHeadYaw;
	@Shadow private int headTrackingIncrements;
	@Shadow private float headYaw;

	@Inject(at = @At("RETURN"), method = "readCustomDataFromTag")
	private void injectReadData(CompoundTag tag, CallbackInfo info) {
		if (tag.contains("Shardblade")) {
			CompoundTag shardbladeData = tag.getCompound("Shardblade");
			int schema = shardbladeData.getInt("Schema");

			switch (schema) {
			case 0:
				this.incapacitatedByShardblade = shardbladeData.getBoolean("IncapacitatedByShardblade");
				this.wrGravityX = shardbladeData.getFloat("GravityX");
				this.wrGravityY = shardbladeData.getFloat("GravityY");
				this.wrGravityZ = shardbladeData.getFloat("GravityZ");
				this.wrCurrentGravity = shardbladeData.getFloat("ScalarGravity");
				this.wrCurrentPitch = shardbladeData.getFloat("GravityPitch");
				this.wrCurrentYaw = shardbladeData.getFloat("GravityYaw");
				break;
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "writeCustomDataToTag")
	private void injectWriteData(CompoundTag tag, CallbackInfo info) {
		CompoundTag shardbladeData = new CompoundTag();
		shardbladeData.putInt("Schema", SHARDBLADE_SCHEMA_LATEST);
		shardbladeData.putBoolean("IncapacitatedByShardblade", this.incapacitatedByShardblade);
		shardbladeData.putFloat("GravityX", this.wrGravityX);
		shardbladeData.putFloat("GravityY", this.wrGravityY);
		shardbladeData.putFloat("GravityZ", this.wrGravityZ);
		shardbladeData.putFloat("ScalarGravity", this.wrCurrentGravity);
		shardbladeData.putFloat("GravityPitch", this.wrCurrentPitch);
		shardbladeData.putFloat("GravityYaw", this.wrCurrentYaw);

		tag.put("Shardblade", shardbladeData);
	}

	@Override
	public boolean isIncapacitatedByShardblade() {
		return this.incapacitatedByShardblade;
	}

	@Override
	public void setIncapacitatedByShardblade(boolean value) {
		this.incapacitatedByShardblade = value;
	}

	@Overwrite
	public void travel(Vec3d rawMovementInput) {
		Vec3d movementInput = WindrunningSurgeImpl.rotate(rawMovementInput, this.wrCurrentPitch, this.wrCurrentYaw, this.wrCurrentGravity);
		LivingEntity self = (LivingEntity) (Object) this;
		double vanillaGravity;
		double gravityMultiplier = 1D;
		float g;

		if (this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement()) {
			vanillaGravity = 0.08D;
			boolean bl = this.getVelocity().y <= 0.0D;
			if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
				vanillaGravity = 0.01D;
				gravityMultiplier = 0.125D;
				this.fallDistance = 0.0F;
			}

			double e;
			float horizontalMovementMultiplier;
			double swimUpVelocity;

			if (!this.isTouchingWater() || self instanceof PlayerEntity && ((PlayerEntity) self).abilities.flying) {
				if (this.isInLava() && (!(self instanceof PlayerEntity) || !((PlayerEntity) self).abilities.flying)) {
					e = this.getY();
					this.updateVelocity(0.02F, movementInput);
					this.move(MovementType.SELF, this.getVelocity());
					this.setVelocity(this.getVelocity().multiply(0.5D));

					if (!this.hasNoGravity()) {
						this.setVelocity(this.getVelocity().add(this.wrGravityX, this.wrGravityY, this.wrGravityZ).multiply(1D / 4.0D));
					}

					Vec3d vec3d4 = this.getVelocity();
					if (this.horizontalCollision && this.doesNotCollide(vec3d4.x, vec3d4.y + 0.6000000238418579D - this.getY() + e, vec3d4.z)) {
						this.setVelocity(vec3d4.x, 0.30000001192092896D, vec3d4.z);
					}
				} else if (this.isFallFlying()) {
					Vec3d vec3d5 = this.getVelocity();

					if (vec3d5.y > -0.5D) {
						this.fallDistance = 1.0F;
					}

					Vec3d vec3d6 = this.getRotationVector();
					horizontalMovementMultiplier = this.pitch * 0.017453292F;
					double m = Math.sqrt(vec3d6.x * vec3d6.x + vec3d6.z * vec3d6.z);
					double n = Math.sqrt(squaredHorizontalLength(vec3d5));
					swimUpVelocity = vec3d6.length();
					float p = MathHelper.cos(horizontalMovementMultiplier);
					p = (float)((double)p * (double)p * Math.min(1.0D, swimUpVelocity / 0.4D));
					vec3d5 = this.getVelocity().add(0.0D, vanillaGravity * (-1.0D + (double)p * 0.75D), 0.0D);
					double s;
					if (vec3d5.y < 0.0D && m > 0.0D) {
						s = vec3d5.y * -0.1D * (double)p;
						vec3d5 = vec3d5.add(vec3d6.x * s / m, s, vec3d6.z * s / m);
					}

					if (horizontalMovementMultiplier < 0.0F && m > 0.0D) {
						s = n * (double)(-MathHelper.sin(horizontalMovementMultiplier)) * 0.04D;
						vec3d5 = vec3d5.add(-vec3d6.x * s / m, s * 3.2D, -vec3d6.z * s / m);
					}

					if (m > 0.0D) {
						vec3d5 = vec3d5.add((vec3d6.x / m * n - vec3d5.x) * 0.1D, 0.0D, (vec3d6.z / m * n - vec3d5.z) * 0.1D);
					}

					this.setVelocity(vec3d5.multiply(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
					this.move(MovementType.SELF, this.getVelocity());
					if (this.horizontalCollision && !this.world.isClient) {
						s = Math.sqrt(squaredHorizontalLength(this.getVelocity()));
						double t = n - s;
						float u = (float)(t * 10.0D - 3.0D);
						if (u > 0.0F) {
							this.playSound(this.getFallSound((int)u), 1.0F, 1.0F);
							this.damage(DamageSource.FLY_INTO_WALL, u);
						}
					}

					if (this.onGround && !this.world.isClient) {
						this.setFlag(7, false);
					}
				} else { // This is the main falling code
					BlockPos blockPos = this.getVelocityAffectingPos();
					float blockSlipperiness = this.world.getBlockState(blockPos).getBlock().getSlipperiness();
					horizontalMovementMultiplier = this.onGround ? blockSlipperiness * 0.91F : 0.91F;
					this.updateVelocity(this.getMovementSpeed(blockSlipperiness), movementInput);
					this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
					this.move(MovementType.SELF, this.getVelocity());
					Vec3d vec3d7 = this.getVelocity();

					if ((this.horizontalCollision || this.jumping) && this.isClimbing()) {
						vec3d7 = new Vec3d(vec3d7.x, 0.2D, vec3d7.z);
					}

					double vectorY = vec3d7.y;

					if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
						vectorY += (0.05D * (double)(this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - vec3d7.y) * 0.2D;
						this.fallDistance = 0.0F;
					} else if (this.world.isClient && !this.world.isChunkLoaded(blockPos)) {
						if (this.getY() > 0.0D) {
							vectorY = -0.1D;
						} else {
							vectorY = 0.0D;
						}
					}

					this.setVelocity((vec3d7.x + (this.wrGravityX * gravityMultiplier)) * (double)horizontalMovementMultiplier, (vectorY + (this.wrGravityY * gravityMultiplier)) * 0.98D, (vec3d7.z + (this.wrGravityZ * gravityMultiplier)) * (double)horizontalMovementMultiplier);
				}
			} else {
				e = this.getY();
				horizontalMovementMultiplier = this.isSprinting() ? 0.9F : this.getBaseMovementSpeedMultiplier();
				g = 0.02F;
				float h = (float) EnchantmentHelper.getDepthStrider(self);
				if (h > 3.0F) {
					h = 3.0F;
				}

				if (!this.onGround) {
					h *= 0.5F;
				}

				if (h > 0.0F) {
					horizontalMovementMultiplier += (0.54600006F - horizontalMovementMultiplier) * h / 3.0F;
					g += (this.getMovementSpeed() - g) * h / 3.0F;
				}

				if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
					horizontalMovementMultiplier = 0.96F;
				}

				this.updateVelocity(g, movementInput);
				this.move(MovementType.SELF, this.getVelocity());
				Vec3d vec3d = this.getVelocity();
				if (this.horizontalCollision && this.isClimbing()) {
					vec3d = new Vec3d(vec3d.x, 0.2D, vec3d.z);
				}

				this.setVelocity(vec3d.multiply((double)horizontalMovementMultiplier, 0.800000011920929D, (double)horizontalMovementMultiplier));
				Vec3d vec3d2;
				if (!this.hasNoGravity() && !this.isSprinting()) {
					vec3d2 = this.getVelocity(); // also this
					if (bl && Math.abs(vec3d2.y - 0.005D) >= 0.003D && Math.abs(vec3d2.y - vanillaGravity / 16.0D) < 0.003D) {
						swimUpVelocity = -0.003D;
					} else {
						swimUpVelocity = vec3d2.y - vanillaGravity / 16.0D;
					}

					this.setVelocity(vec3d2.x, swimUpVelocity, vec3d2.z);
				}

				vec3d2 = this.getVelocity();
				if (this.horizontalCollision && this.doesNotCollide(vec3d2.x, vec3d2.y + 0.6000000238418579D - this.getY() + e, vec3d2.z)) {
					this.setVelocity(vec3d2.x, 0.30000001192092896D, vec3d2.z);
				}
			}
		}

		this.lastLimbDistance = this.limbDistance;
		vanillaGravity = this.getX() - this.prevX;
		double z = this.getZ() - this.prevZ;
		double aa = this instanceof Flutterer ? this.getY() - this.prevY : 0.0D;
		g = MathHelper.sqrt(vanillaGravity * vanillaGravity + aa * aa + z * z) * 4.0F;
		if (g > 1.0F) {
			g = 1.0F;
		}

		this.limbDistance += (g - this.limbDistance) * 0.4F;
		this.limbAngle += this.limbDistance;
	}

	@Overwrite
	public void jump() {
		float jumpVelocity = this.getJumpVelocity();
		if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
			jumpVelocity += 0.1F * (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1);
		}

		jumpVelocity *= -12.5f;

		Vec3d currentVelocity = this.getVelocity();
		Vec3d jumpVector = new Vec3d(this.wrGravityX * jumpVelocity, this.wrGravityY * jumpVelocity, this.wrGravityZ * jumpVelocity);
		WindrunningSurgeImpl.jump(currentVelocity, jumpVector, (LivingEntity) (Object) this);

		this.velocityDirty = true;
	}

	public void tickMovement() {
		if (this.jumpingCooldown > 0) {
			--this.jumpingCooldown;
		}

		if (this.isLogicalSideForUpdatingMovement()) {
			this.bodyTrackingIncrements = 0;
			this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
		}

		if (this.bodyTrackingIncrements > 0) {
			double newX = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
			double newY = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
			double newZ = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
			double newYaw = MathHelper.wrapDegrees(this.serverYaw - (double)this.yaw);
			this.yaw = (float)((double)this.yaw + newYaw / (double)this.bodyTrackingIncrements);
			this.pitch = (float)((double)this.pitch + (this.serverPitch - (double)this.pitch) / (double)this.bodyTrackingIncrements);
			--this.bodyTrackingIncrements;
			this.updatePosition(newX, newY, newZ);
			this.setRotation(this.yaw, this.pitch);
		} else if (!this.canMoveVoluntarily()) {
			this.setVelocity(this.getVelocity().multiply(0.98D));
		}

		if (this.headTrackingIncrements > 0) {
			this.headYaw = (float)((double)this.headYaw + MathHelper.wrapDegrees(this.serverHeadYaw - (double)this.headYaw) / (double)this.headTrackingIncrements);
			--this.headTrackingIncrements;
		}

		Vec3d currentVelocity = this.getVelocity();
		double velocityX = currentVelocity.x;
		double velocityY = currentVelocity.y;
		double velocityZ = currentVelocity.z;
		if (Math.abs(currentVelocity.x) < 0.003D) {
			velocityX = 0.0D;
		}

		if (Math.abs(currentVelocity.y) < 0.003D) {
			velocityY = 0.0D;
		}

		if (Math.abs(currentVelocity.z) < 0.003D) {
			velocityZ = 0.0D;
		}

		this.setVelocity(velocityX, velocityY, velocityZ);
		this.world.getProfiler().push("ai");

		if (this.isImmobile()) {
			this.jumping = false;
			this.sidewaysSpeed = 0.0F;
			this.forwardSpeed = 0.0F;
		} else if (this.canMoveVoluntarily()) {
			this.world.getProfiler().push("newAi");
			this.tickNewAi();
			this.world.getProfiler().pop();
		}

		this.world.getProfiler().pop();
		this.world.getProfiler().push("jump");
		if (this.jumping) {
			if (this.waterHeight <= 0.0D || this.onGround && this.waterHeight <= 0.4D) {
				if (this.isInLava()) {
					this.swimUpward(FluidTags.LAVA);
				} else if ((this.onGround || this.waterHeight > 0.0D && this.waterHeight <= 0.4D) && this.jumpingCooldown == 0) {
					this.jump();
					this.jumpingCooldown = 10;
				}
			} else {
				this.swimUpward(FluidTags.WATER);
			}
		} else {
			this.jumpingCooldown = 0;
		}

		this.world.getProfiler().pop();
		this.world.getProfiler().push("travel");
		this.sidewaysSpeed *= 0.98F;
		this.forwardSpeed *= 0.98F;
		this.initAi();
		Box box = this.getBoundingBox();
		this.travel(new Vec3d((double)this.sidewaysSpeed, (double)this.upwardSpeed, (double)this.forwardSpeed));
		this.world.getProfiler().pop();
		this.world.getProfiler().push("push");
		if (this.pushCooldown > 0) {
			--this.pushCooldown;
			this.push(box, this.getBoundingBox());
		}

		this.tickCramming();
		this.world.getProfiler().pop();
	}

	@Shadow
	private void tickNewAi() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private boolean isImmobile() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private void initAi() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private void push(Box a, Box b) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private void swimUpward(Tag<Fluid> fluidTag) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private void tickCramming() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private float getJumpVelocity() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private float getBaseMovementSpeedMultiplier() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
 	}
	@Shadow
	private float getMovementSpeed() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private StatusEffectInstance getStatusEffect(StatusEffect levitation) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private boolean isClimbing() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private SoundEvent getFallSound(int u) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private boolean isFallFlying() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private boolean hasStatusEffect(StatusEffect status) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private Vec3d applyClimbingSpeed(Vec3d velocity) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private float getMovementSpeed(float slipperiness) {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}
	@Shadow
	private boolean canMoveVoluntarily() {
		throw new RuntimeException("[Shardblade] Failed @Shadow in MixinLivingEntity");
	}

	@Override
	public void setGravitation(float x, float y, float z, float gravityCache, float yawCache, float pitchCache) {
		this.wrGravityX = x;
		this.wrGravityY = y;
		this.wrGravityZ = z;
		this.wrCurrentGravity = gravityCache;
		this.wrCurrentYaw = yawCache;
		this.wrCurrentPitch = pitchCache;
	}

	@Override
	public float[] getGravitation() {
		return new float[] {this.wrGravityX, this.wrGravityY, this.wrGravityZ};
	}

	private static final int SHARDBLADE_SCHEMA_LATEST = 0;
}
