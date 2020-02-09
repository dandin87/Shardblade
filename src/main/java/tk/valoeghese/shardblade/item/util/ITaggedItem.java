package tk.valoeghese.shardblade.item.util;

import net.minecraft.nbt.CompoundTag;

public interface ITaggedItem {
	void loadFromTag(CompoundTag tag);
	void writeToTag(CompoundTag tag);
}
