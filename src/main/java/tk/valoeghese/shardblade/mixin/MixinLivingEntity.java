package tk.valoeghese.shardblade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tk.valoeghese.shardblade.item.HonorBlade;
import tk.valoeghese.shardblade.mechanics.IShardbladeAffectedEntity;
import tk.valoeghese.shardblade.mechanics.gravity.Gravitation3;
import tk.valoeghese.shardblade.mechanics.gravity.I3DGravitation;
import tk.valoeghese.shardblade.mechanics.surgebinding.ISurgebinder;
import tk.valoeghese.shardblade.mechanics.surgebinding.Surge;
import tk.valoeghese.shardblade.mechanics.surgebinding.SurgebindingOrder;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements IShardbladeAffectedEntity, I3DGravitation {
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
	private Vec3d wrGravityVec = new Vec3d(0, -0.08, 0);

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
	public void handle3DFallDamage(Vec3d motion, boolean xCol, boolean yCol, boolean zCol, BlockState blockState, BlockPos blockPos) {
		// someone pls fix my 3d falldamage code
		double fallDamageSpeed = Gravitation3.fallDamageSpeed(motion, this.wrGravityVec, xCol, yCol, zCol);
		this.fall(fallDamageSpeed, this.collided, blockState, blockPos);
	}
	
//	private static final double FDDCoefficient = 9.81 / (2 * 0.08); // fall damage distance coefficient (inverse, since it goes in denominator)

	@Override
	public double calculateFallDamageDistance(double fallDamageSpeed) {
		//return (fallDamageSpeed * fallDamageSpeed) / (FDDCoefficient * this.wrCurrentGravity);
		return fallDamageSpeed * fallDamageSpeed; // assume everything is to do with gravity because yeet
	}

	@Inject(at = @At("HEAD"), method = "handleFallDamage", cancellable = true)
	private void reduceSurgebinderFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (self instanceof PlayerEntity) {
			ItemStack stack = self.getStackInHand(Hand.MAIN_HAND);

			if (stack.getItem() instanceof HonorBlade) {
				float newDamageMultiplier = damageMultiplier / 3.5f;
				SurgebindingOrder order = ((ISurgebinder) (Object) stack).getOrder();

				if (!SurgebindingOrder.isNone(order)) {
					if (order.hasSurge(Surge.GRAVITATION)) {
						// TODO stormlight check, when stormlight is added
						newDamageMultiplier = damageMultiplier / 12.5f;
					}
				}

				boolean superResult = super.handleFallDamage(fallDistance, newDamageMultiplier);
				int i = this.computeFallDamage(fallDistance, newDamageMultiplier);

				if (i > 0) {
					this.playSound(this.getFallSound(i), 1.0F, 1.0F);
					this.playBlockFallSound();
					this.damage(DamageSource.FALL, (float)i);
					cir.setReturnValue(true);
				} else {
					cir.setReturnValue(superResult);
				}
			}
		}
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
		Vec3d movementInput = Gravitation3.rotateAligned(rawMovementInput, this.wrCurrentPitch, this.wrCurrentYaw, this.wrCurrentGravity);
		LivingEntity self = (LivingEntity) (Object) this;
		double vanillaGravity;
		double gravityMultiplier = 1D;
		float yaYeet;

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
				} else if (this.isFallFlying()) { // Elytra
					Vec3d entityVelocity = this.getVelocity();

					if (entityVelocity.y > -0.5D) {
						this.fallDistance = 1.0F;
					}

					Vec3d entityRotation = this.getRotationVector();
					horizontalMovementMultiplier = this.pitch * 0.017453292F;
					double horizontalRotationLength = Math.sqrt(entityRotation.x * entityRotation.x + entityRotation.z * entityRotation.z);
					double n = Math.sqrt(squaredHorizontalLength(entityVelocity));
					swimUpVelocity = entityRotation.length();
					float p = MathHelper.cos(horizontalMovementMultiplier);
					p = (float)((double)p * (double)p * Math.min(1.0D, swimUpVelocity / 0.4D));
					entityVelocity = this.getVelocity().add(0.0D, vanillaGravity * (-1.0D + (double)p * 0.75D), 0.0D);
					double s;
					if (entityVelocity.y < 0.0D && horizontalRotationLength > 0.0D) {
						s = entityVelocity.y * -0.1D * (double)p;
						entityVelocity = entityVelocity.add(entityRotation.x * s / horizontalRotationLength, s, entityRotation.z * s / horizontalRotationLength);
					}

					if (horizontalMovementMultiplier < 0.0F && horizontalRotationLength > 0.0D) {
						s = n * (double)(-MathHelper.sin(horizontalMovementMultiplier)) * 0.04D;
						entityVelocity = entityVelocity.add(-entityRotation.x * s / horizontalRotationLength, s * 3.2D, -entityRotation.z * s / horizontalRotationLength);
					}

					if (horizontalRotationLength > 0.0D) {
						entityVelocity = entityVelocity.add((entityRotation.x / horizontalRotationLength * n - entityVelocity.x) * 0.1D, 0.0D, (entityRotation.z / horizontalRotationLength * n - entityVelocity.z) * 0.1D);
					}

					this.setVelocity(entityVelocity.multiply(0.9900000095367432D, 0.9800000190734863D, 0.9900000095367432D));
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
				yaYeet = 0.02F;
				float h = (float) EnchantmentHelper.getDepthStrider(self);
				if (h > 3.0F) {
					h = 3.0F;
				}

				if (!this.onGround) {
					h *= 0.5F;
				}

				if (h > 0.0F) {
					horizontalMovementMultiplier += (0.54600006F - horizontalMovementMultiplier) * h / 3.0F;
					yaYeet += (this.getMovementSpeed() - yaYeet) * h / 3.0F;
				}

				if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
					horizontalMovementMultiplier = 0.96F;
				}

				this.updateVelocity(yaYeet, movementInput);
				this.move(MovementType.SELF, this.getVelocity());
				Vec3d vec3d = this.getVelocity();
				if (this.horizontalCollision && this.isClimbing()) {
					vec3d = new Vec3d(vec3d.x, 0.2D, vec3d.z);
				}

				this.setVelocity(vec3d.multiply((double)horizontalMovementMultiplier, 0.800000011920929D, (double)horizontalMovementMultiplier));
				Vec3d velocity;
				if (!this.hasNoGravity() && !this.isSprinting()) {
					velocity = this.getVelocity(); // also this
					if (bl && Math.abs(velocity.y - 0.005D) >= 0.003D && Math.abs(velocity.y - vanillaGravity / 16.0D) < 0.003D) {
						swimUpVelocity = -0.003D;
					} else {
						swimUpVelocity = velocity.y - (this.wrCurrentGravity * gravityMultiplier) / 16.0D; // vanilla uses velocity.y - vanillaGravity * 16
					}

					Direction rotationSwimGrav = Gravitation3.getRotation(this.wrCurrentYaw, this.wrCurrentPitch);
					velocity = Gravitation3.rotateAligned(velocity, rotationSwimGrav, this.wrCurrentGravity);
					this.setVelocity(Gravitation3.revertAlignedRotation(new Vec3d(velocity.x, swimUpVelocity, velocity.z), rotationSwimGrav, this.wrCurrentGravity));
				}

				velocity = this.getVelocity();
				Direction rotationGrav = Gravitation3.getRotation(this.wrCurrentYaw, this.wrCurrentPitch);
				velocity = Gravitation3.rotateAligned(this.getVelocity(), rotationGrav, this.wrCurrentGravity);
				Vec3d velocityToCheck = Gravitation3.revertAlignedRotation(velocity, rotationGrav, this.wrCurrentGravity); // vanilla velocity.x, velocity.y + 0.6D - this.getY() + e, velocity.z
				if (this.horizontalCollision && this.doesNotCollide(velocityToCheck.x, velocityToCheck.y, velocityToCheck.z)) { // vanilla velocity.x, velocity.y + 0.6000000238418579D - this.getY() + e, velocity.z
					velocity = new Vec3d(velocity.x, 0.3D, velocity.z);
					this.setVelocity(Gravitation3.revertAlignedRotation(velocity, rotationGrav, this.wrCurrentGravity));// vanilla this.setVelocity(velocity.x, 0.30000001192092896D, velocity.z);
				}
			}
		}

		this.lastLimbDistance = this.limbDistance;
		vanillaGravity = this.getX() - this.prevX;
		gravityMultiplier = (vanillaGravity / 0.08D);
		double z = this.getZ() - this.prevZ;
		double aa = this instanceof Flutterer ? this.getY() - this.prevY : 0.0D;
		yaYeet = MathHelper.sqrt(vanillaGravity * vanillaGravity + aa * aa + z * z) * 4.0F;
		if (yaYeet > 1.0F) {
			yaYeet = 1.0F;
		}

		this.limbDistance += (yaYeet - this.limbDistance) * 0.4F;
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
		Gravitation3.jump(currentVelocity, jumpVector, (LivingEntity) (Object) this);

		this.velocityDirty = true;
	}

	/* Might need to mess with this when altering camera and model for rotations
	@Overwrite
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
	} //*/

	@Shadow abstract protected void playBlockFallSound();
	@Shadow abstract protected void tickNewAi();
	@Shadow abstract protected boolean isImmobile();
	@Shadow abstract protected void initAi();
	@Shadow abstract protected void push(Box a, Box b);
	@Shadow abstract protected void swimUpward(Tag<Fluid> fluidTag);
	@Shadow abstract protected void tickCramming();
	@Shadow abstract protected float getJumpVelocity();
	@Shadow abstract protected float getBaseMovementSpeedMultiplier();
	@Shadow abstract protected float getMovementSpeed();
	@Shadow abstract protected StatusEffectInstance getStatusEffect(StatusEffect levitation);
	@Shadow abstract protected boolean isClimbing();
	@Shadow abstract protected SoundEvent getFallSound(int u);
	@Shadow abstract protected boolean isFallFlying();
	@Shadow abstract protected boolean hasStatusEffect(StatusEffect status);
	@Shadow abstract protected Vec3d applyClimbingSpeed(Vec3d velocity);
	@Shadow abstract protected float getMovementSpeed(float slipperiness);
	@Shadow abstract protected boolean canMoveVoluntarily();
	@Shadow abstract protected int computeFallDamage(float fallDistance, float damageMultiplier);

	@Override
	public void setGravitation(float x, float y, float z, float gravityCache, float yawCache, float pitchCache) {
		this.wrGravityX = x;
		this.wrGravityY = y;
		this.wrGravityZ = z;
		this.wrCurrentGravity = gravityCache;
		this.wrCurrentYaw = yawCache;
		this.wrCurrentPitch = pitchCache;
		this.wrGravityVec = new Vec3d(x, y, z);
//		this.setRotation(yawCache * 0.017453292F, pitchCache * 0.017453292F);
	}

	@Override
	public float[] getGravitation() {
		return new float[] {this.wrGravityX, this.wrGravityY, this.wrGravityZ};
	}

	@Override
	public float getCachedYaw() {
		return this.wrCurrentYaw;
	}

	@Override
	public float getCachedPitch() {
		return this.wrCurrentPitch;
	}

	@Override
	public float getCachedGravitationalStrength() {
		return this.wrCurrentGravity;
	}

	private static final int SHARDBLADE_SCHEMA_LATEST = 0;
}
