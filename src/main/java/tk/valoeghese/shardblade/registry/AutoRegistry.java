package tk.valoeghese.shardblade.registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import tk.valoeghese.shardblade.ShardbladeMod;

public class AutoRegistry {
	public static void register() {
		collectRegistrables();
		registerAll(RegistryType.ITEM);
		registerAll(RegistryType.BLOCK);
		registerAll(RegistryType.BIOME);
	}

	public static void addListClass(Class<?> clazz) {
		String modid = computeModid(clazz);

		for (Field field : clazz.getFields()) {
			Class<?> fClazz = field.getType();

			if (fClazz.isAnnotationPresent(RegistryInfo.class)) {
				RegistryInfo i = fClazz.getAnnotation(RegistryInfo.class);
				getType(fClazz).fields.put(modid + ":" + i.value(), field);
				registerableClasses.add(fClazz);
			}
		}
	}

	private static void registerAll(RegistryType registry) {
		byte[] priorities = registry.registerables.keySet().toByteArray();
		Arrays.sort(priorities);

		for (byte b : priorities) {
			List<RegistryDataEntry> list = registry.registerables.get(b);
			for (RegistryDataEntry registerable : list) {
				registerable.register(registry);
			}
		}
	}

	private static void collectRegistrables() {
		ShardbladeMod.logger.info("Collecting registerables");

		for (Class<?> clazz : registerableClasses) {
			RegistryInfo info = clazz.getAnnotation(RegistryInfo.class);
			getType(clazz).registerables.computeIfAbsent(info.priority(), p -> new ArrayList<>()).add(new RegistryDataEntry(info.value(), clazz));
		}
	}

	static String computeModid(Class<?> clazz) {
		String className = clazz.getName();

		for (Map.Entry<String, String> entry : mods.entrySet()) {
			if (className.matches(entry.getKey())) {
				return entry.getValue();
			}
		}

		return "minecraft";
	}

	private static RegistryType getType(Class<?> clazz) {
		if (Item.class.isAssignableFrom(clazz)) {
			return RegistryType.ITEM;
		} else if (Block.class.isAssignableFrom(clazz)) {
			return RegistryType.BLOCK;
		} else if (Biome.class.isAssignableFrom(clazz)) {
			return RegistryType.BIOME;
		}

		return null;
	}

	private static final Map<String, String> mods;
	@SuppressWarnings("rawtypes")
	private static final Set<Class> registerableClasses = new HashSet<>();

	static {
		mods = new HashMap<>();

		FabricLoader.getInstance().getAllMods().forEach(container -> {
			ModMetadata meta = container.getMetadata();
			CustomValue pkg = meta.getCustomValue("modPackage");
			if (pkg != null) {
				mods.put(pkg.getAsString().replace(".", "\\.") + ".*", meta.getId());
			}
		});
	}
}
