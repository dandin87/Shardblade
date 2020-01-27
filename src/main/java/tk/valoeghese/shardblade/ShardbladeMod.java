package tk.valoeghese.shardblade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class ShardbladeMod implements ModInitializer {
	public static final Logger logger = LogManager.getLogger("Shardblade");

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String name) {
		return new Identifier("shardblade", name);
	}
}
