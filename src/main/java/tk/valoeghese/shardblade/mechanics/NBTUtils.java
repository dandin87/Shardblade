package tk.valoeghese.shardblade.mechanics;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;

public class NBTUtils {
	@Deprecated
	public static <T> Optional<T> getOptionalData(CompoundTag tag, String name, Function<String, T> retrieval) {
		if (tag.contains(name)) {
			return Optional.of(retrieval.apply(name));
		} else {
			return Optional.empty();
		}
	}

	@Deprecated
	public static <T> T getOrDefault(CompoundTag tag, String name, Function<String, T> retrieval, T defaultValue) {
		if (tag.contains(name)) {
			try {
				return retrieval.apply(name);
			} catch (Throwable t) {
				System.out.println("e");
				return defaultValue;
			}
		} else {
			System.out.println("f");
			return defaultValue;
		}
	}
}
