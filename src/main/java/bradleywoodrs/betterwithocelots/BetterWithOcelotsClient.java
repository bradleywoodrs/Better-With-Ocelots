package bradleywoodrs.betterwithocelots;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobCatGuideEntry;
import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import bradleywoodrs.betterwithocelots.entities.ocelot.MobRendererOcelot;
import bradleywoodrs.betterwithocelots.items.BetterWithOcelotsItems;
import bradleywoodrs.betterwithocelots.items.ItemMilkOfEternalYouth;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.guidebook.mobs.MobInfoRegistry;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.ItemStack;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.util.dependency.Key;

import static bradleywoodrs.betterwithocelots.BetterWithOcelots.MOD_ID;

public class BetterWithOcelotsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Key key = Key.of(MOD_ID);

		ClientEvents.BEFORE_CLIENT_START.listen(key, () -> SoundRepository.namespaceAdded(MOD_ID));

		ClientEvents.AFTER_CLIENT_START.listen(key, () -> {
			MobInfoRegistry.register(MobOcelot.class, "betterwithocelots.ocelot.name", "betterwithocelots.ocelot.desc", 10, 50, null);
			MobInfoRegistry.register(MobCatGuideEntry.class, "betterwithocelots.cat.name", "betterwithocelots.cat.desc", 10, 0, null);
		});

		ClientEvents.ENTITY_RENDERER_RELOAD.listen(key, dispatcher ->
			dispatcher.assignRenderer(MobOcelot.class, new MobRendererOcelot())
		);

		ClientEvents.ITEM_MODEL_RELOAD.listen(key, dispatcher -> {
			dispatcher.addDispatch(new ItemModelStandard(BetterWithOcelotsItems.MILK_OF_ETERNAL_YOUTH) {
				private final IconCoordinate ironIcon = TextureRegistry.getTexture(MOD_ID + ":item/bucket_iron/milk_of_eternal_youth_1");
				private final IconCoordinate[] steelIcons = {
					TextureRegistry.getTexture(MOD_ID + ":item/bucket_steel/milk_of_eternal_youth_1"),
					TextureRegistry.getTexture(MOD_ID + ":item/bucket_steel/milk_of_eternal_youth_2"),
					TextureRegistry.getTexture(MOD_ID + ":item/bucket_steel/milk_of_eternal_youth_3")
				};

				@Override
				public IconCoordinate getIcon(Entity entity, ItemStack stack) {
					if (!ItemMilkOfEternalYouth.isInSteelBucket(stack)) return this.ironIcon;
					int bucketAmount = Math.max(1, Math.min(ItemMilkOfEternalYouth.getCharges(stack), this.steelIcons.length));
					return this.steelIcons[bucketAmount - 1];
				}
			});
			dispatcher.addDispatch(new ItemModelStandard(BetterWithOcelotsItems.YOUTHFUL_FLOWER_BLEND));
		});
	}
}
