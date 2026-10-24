package bradleywoodrs.betterwithocelots;

import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

import static bradleywoodrs.betterwithocelots.BetterWithOcelots.MOD_ID;

public class BetterWithOcelotsConfig {
	private static final Toml TOML = new Toml("BetterWithOcelots TOML Config");
	public static final TomlConfigHandler CFG;


	static {
		TOML.addCategory("IDs")
			.addEntry("legacyOcelotID", "Default: 364", 364)
			.addEntry("milkOfEternalYouthID", "Default: 28500", 28500)
			.addEntry("youthfulFlowerBlendID", "Default: 28501", 28501);
		TOML.addCategory("Cats")
			.addEntry("growUpMinutes", "Time(in minutes), it takes for kittens to grow into adults. Set to -1 for kittens to never grow up. Default: 20", 20)
			.addEntry("spawnWeight", "Weight of ocelot spawns divided by weight of all animal spawns. Higher number will add more chance for ocelots to spawn. Set to 0 to disable natural spawns. Default: 24", 24)
			.addEntry("holidayFeatures", "Enables/disables holiday features. Default: true", true);
		CFG = new TomlConfigHandler(MOD_ID, TOML);
	}
}
