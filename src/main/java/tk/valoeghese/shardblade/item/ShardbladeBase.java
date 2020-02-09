package tk.valoeghese.shardblade.item;

import com.google.common.collect.Multimap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;

public abstract class ShardbladeBase extends Item implements IShardblade {
	public ShardbladeBase(Settings settings) {
		super(settings);
	}

	public boolean isEffectiveOn(BlockState state) {
		return true;
	}

	public Multimap<String, EntityAttributeModifier> getModifiers(EquipmentSlot slot) {
		Multimap<String, EntityAttributeModifier> multimap = super.getModifiers(slot);

		if (slot == EquipmentSlot.MAINHAND) {
			multimap.put(EntityAttributes.ATTACK_SPEED.getId(), new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Weapon modifier", (double) this.getAttackSpeedModifier(), EntityAttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
}
