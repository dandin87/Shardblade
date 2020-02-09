package tk.valoeghese.shardblade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import tk.valoeghese.shardblade.item.ShardbladeItems;
import tk.valoeghese.shardblade.registry.AutoRegistry;

public class ShardbladeMod implements ModInitializer {
	public static final Logger logger = LogManager.getLogger("Shardblade");

	@Override
	public void onInitialize() {
		AutoRegistry.addListClass(ShardbladeItems.class);
		AutoRegistry.register();
	}

	public static Identifier id(String name) {
		return new Identifier("shardblade", name);
	}
}
