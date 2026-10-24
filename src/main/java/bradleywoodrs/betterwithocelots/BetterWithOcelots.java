package bradleywoodrs.betterwithocelots;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import bradleywoodrs.betterwithocelots.items.BetterWithOcelotsItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.enums.MobCategory;
import net.minecraft.core.sound.SoundTypes;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.world.biome.Biomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class BetterWithOcelots implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("betterwithocelots", true);
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Stat CATS_TAMED_STAT;
	private static final int LEGACY_OCELOT_ID = BetterWithOcelotsConfig.CFG.getInt("IDs.legacyOcelotID");

	@Override
	public void onInitialize() {
		Key key = Key.of(MOD_ID);
		CommonEvents.BEFORE_GAME_START.listen(key, this::beforeGameStart);
		CommonEvents.AFTER_ITEM_INIT.listen(key, BetterWithOcelotsItems::register);
		CommonEvents.RECIPES_NAMESPACE_INIT.listen(key, BetterWithOcelotsRecipes::initNamespace);
		CommonEvents.RECIPES_READY.listen(key, BetterWithOcelotsRecipes::register);
		CommonEvents.AFTER_GAME_START.listen(key, BetterWithOcelotsTags::tagItems);
		int spawnWeight = BetterWithOcelotsConfig.CFG.getInt("Cats.spawnWeight");
		if (spawnWeight > 0) {
			Biomes.OVERWORLD_RAINFOREST.getSpawnableList(MobCategory.CREATURE).add(new SpawnListEntry(MobOcelot.class, spawnWeight));
			Biomes.OVERWORLD_LEGACY_RAINFOREST.getSpawnableList(MobCategory.CREATURE).add(new SpawnListEntry(MobOcelot.class, spawnWeight));
		}
		SoundTypes.loadSoundsJson(MOD_ID);
		LOGGER.info("Better With Ocelots loaded.");
	}

	public void beforeGameStart() {
		EntityDispatcher.getInstance().addMapping(MobOcelot.class, NamespaceID.getPermanent(MOD_ID, "ocelot"), MobOcelot::new, "betterwithocelots.ocelot.name");
		EntityDispatcher.Legacy.addMapping(MobOcelot.class, "Ocelot", LEGACY_OCELOT_ID);
	}
}
