package tk.valoeghese.shardblade.registry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import tk.valoeghese.shardblade.ShardbladeMod;

public class AutoRegistry {
	public static void register() {
		collectRegistrables();
		registerItems();
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

	private static void registerItems() {
		byte[] priorities = RegistryType.ITEM.registerables.keySet().toByteArray();
		Arrays.sort(priorities);

		for (byte b : priorities) {
			List<RegistryDataEntry> list = RegistryType.ITEM.registerables.get(b);
			for (RegistryDataEntry registerable : list) {
				registerable.register(RegistryType.ITEM);
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

class RegistryDataEntry {
	RegistryDataEntry(String id, Class<?> clazz) {
		this.id = id;
		this.modid = AutoRegistry.computeModid(clazz);
		this.clazz = clazz;
	}

	void register(RegistryType registryType) {
		Identifier identifier = new Identifier(this.modid, this.id);
		Object instance;
		try {
			instance = clazz.newInstance();
			register.invoke(null, registryType.registry, identifier, instance);
			Field toSet = registryType.fields.get(identifier.toString());

			if (toSet != null) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(toSet, toSet.getModifiers() & ~Modifier.FINAL);
				toSet.set(null, instance);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	Class<?> clazz;
	String id;
	String modid;

	private static final Method register;

	static {
		try {
			register = Registry.class.getMethod("register", Registry.class, Identifier.class, Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
