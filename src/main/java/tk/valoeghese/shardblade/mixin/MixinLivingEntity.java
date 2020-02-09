package tk.valoeghese.shardblade.mixin;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import tk.valoeghese.shardblade.mechanics.IShardbladeAffectedEntity;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements IShardbladeAffectedEntity {
	@Unique
	private boolean incapacitatedByShardblade = false;

	@Inject(at = @At("RETURN"), method = "readCustomDataFromTag")
	private void injectReadData(CompoundTag tag, CallbackInfo info) {
		if (tag.contains("shardblade")) {
			CompoundTag shardbladeData = tag.getCompound("shardblade");
			int schema = shardbladeData.getInt("schema");

			switch (schema) {
			case 0:
				this.incapacitatedByShardblade = shardbladeData.getBoolean("incapacitatedByShardblade");
				break;
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "writeCustomDataToTag")
	private void injectWriteData(CompoundTag tag, CallbackInfo info) {
		CompoundTag shardbladeData = new CompoundTag();
		shardbladeData.putInt("schema", SHARDBLADE_SCHEMA_LATEST);
		shardbladeData.putBoolean("incapacitatedByShardblade", this.incapacitatedByShardblade);

		tag.put("shardblade", tag);
	}

	@Override
	public boolean isIncapacitatedByShardblade() {
		return this.incapacitatedByShardblade;
	}

	private static final int SHARDBLADE_SCHEMA_LATEST = 0;
}
