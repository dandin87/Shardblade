package tk.valoeghese.shardblade.registry;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.util.registry.Registry;

enum RegistryType {
	ITEM(Registry.ITEM),
	BLOCK(Registry.BLOCK),
	BIOME(Registry.BIOME);

	private RegistryType(Registry<?> registry) {
		this.registry = registry;
	}

	final Registry<?> registry;
	final Byte2ObjectMap<List<RegistryDataEntry>> registerables = new Byte2ObjectArrayMap<>();
	final Map<String, Field> fields = new HashMap<>();
}
