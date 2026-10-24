package bradleywoodrs.betterwithocelots.items;

import bradleywoodrs.betterwithocelots.BetterWithOcelotsConfig;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.jetbrains.annotations.Nullable;
import turniplabs.halplibe.helper.ItemBuilder;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;

import static bradleywoodrs.betterwithocelots.BetterWithOcelots.MOD_ID;

public final class BetterWithOcelotsItems {
	private BetterWithOcelotsItems() {}

	public static Item MILK_OF_ETERNAL_YOUTH;
	public static Item YOUTHFUL_FLOWER_BLEND;

	public static void register() {
		YOUTHFUL_FLOWER_BLEND = new ItemBuilder(MOD_ID)
			.setCreativeInventoryPlacement(new CreativeInventoryPlacement.After(() -> Items.BOWL))
			.build(new ItemYouthfulFlowerBlend("youthful_flower_blend", itemKey("youthful_flower_blend"), BetterWithOcelotsConfig.CFG.getInt("IDs.youthfulFlowerBlendID")));
		MILK_OF_ETERNAL_YOUTH = new ItemBuilder(MOD_ID)
			.setStackSize(1)
			.setCreativeInventoryPlacement(new CreativeInventoryPlacement.After(() -> Items.BOWL)
				.setCustomSupplier(ItemMilkOfEternalYouth::milkBucketVariants))
			.build(new ItemMilkOfEternalYouth("milk_of_eternal_youth", itemKey("milk_of_eternal_youth"), BetterWithOcelotsConfig.CFG.getInt("IDs.milkOfEternalYouthID")));
	}

	private static String itemKey(String name) {
		return MOD_ID + ":item/" + name;
	}

	public static void consumeHeldItem(Player player, ItemStack stack) {
		if (stack.consumeItem(player) && stack.stackSize <= 0) {
			player.inventory.setItem(player.inventory.getCurrentSlot(), null);
		}
	}

	public static void giveItem(Player player, @Nullable ItemStack stack) {
		if (stack == null) return;
		player.inventory.insertItem(stack, true);
		if (stack.stackSize > 0) {
			player.dropPlayerItem(stack);
		}
	}
}
