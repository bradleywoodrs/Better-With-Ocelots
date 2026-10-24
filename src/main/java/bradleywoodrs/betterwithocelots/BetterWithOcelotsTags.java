package bradleywoodrs.betterwithocelots;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.Items;
import net.minecraft.core.item.tag.ItemTags;

public final class BetterWithOcelotsTags {
	private BetterWithOcelotsTags() {}

	public static final Tag<Item> CATS_FAVOURITE_ITEM = Tag.of("cats_favourite_item");
	public static final Tag<Item> CATS_SPECIAL_ITEM = Tag.of("cats_special_item");

	static {
		ItemTags.TAG_LIST.add(CATS_FAVOURITE_ITEM);
		ItemTags.TAG_LIST.add(CATS_SPECIAL_ITEM);
	}

	public static void tagItems() {
		CATS_FAVOURITE_ITEM.tag(Items.FOOD_FISH_RAW);
		tagModItems(CATS_FAVOURITE_ITEM, "stardew", "luke.stardew.items.StardewItems",
			"FOOD_SALMON_RAW", "FOOD_BASS_RAW", "FOOD_SNAPPER_RAW");
		tagModItems(CATS_FAVOURITE_ITEM, "btf", "imbacrian.btf.items.ItemsList",
			"FOOD_ANCHOVY_RAW", "FOOD_GOLDEN_CARP_RAW", "FOOD_KNIFEFISH_RAW", "FOOD_SARDINE_RAW",
			"FOOD_SUNFISH_RAW", "FOOD_TUNA_RAW", "FOOD_CATFISH_RAW", "FOOD_BOOTFISH_RAW",
			"FOOD_RED_MULLET_RAW", "FOOD_HERRING_RAW", "FOOD_HALIBUT_RAW", "FOOD_SEA_BASS_RAW",
			"FOOD_PERCH_RAW", "FOOD_TROUT_RAW", "FOOD_EEL_RAW");
		tagModItems(CATS_SPECIAL_ITEM, "stardew", "luke.stardew.items.StardewItems",
			"FISH_SWORD", "FISH_GHOST", "FISH_STONE", "FISH_EEL_LAVA");
	}

	private static void tagModItems(Tag<Item> tag, String modId, String className, String... fieldNames) {
		if (!FabricLoader.getInstance().isModLoaded(modId)) return;
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			BetterWithOcelots.LOGGER.warn("Failed to load modded food items from mod {}", modId, e);
			return;
		}
		for (String fieldName : fieldNames) {
			try {
				Object value = clazz.getField(fieldName).get(null);
				if (value instanceof Item) {
					tag.tag((Item) value);
				}
			} catch (ReflectiveOperationException e) {
				BetterWithOcelots.LOGGER.warn("Failed to load food item {} from mod {}", fieldName, modId);
			}
		}
	}
}
