package tk.valoeghese.shardblade.registry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
			register = Registry.class.getMethod(FabricLoader.getInstance().isDevelopmentEnvironment() ? "register" : "method_10231", Registry.class, Identifier.class, Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
