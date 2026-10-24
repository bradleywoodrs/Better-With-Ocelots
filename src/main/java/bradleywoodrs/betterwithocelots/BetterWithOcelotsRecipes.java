package bradleywoodrs.betterwithocelots;

import bradleywoodrs.betterwithocelots.items.BetterWithOcelotsItems;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.RecipeBuilder;

import static bradleywoodrs.betterwithocelots.BetterWithOcelots.MOD_ID;

public final class BetterWithOcelotsRecipes {
	private BetterWithOcelotsRecipes() {}

	public static void initNamespace() {
		RecipeBuilder.initNameSpace(MOD_ID);
	}

	public static void register() {
		RecipeBuilder.Shapeless(MOD_ID)
			.addInput(Blocks.FLOWER_YELLOW)
			.addInput(Blocks.FLOWER_PURPLE)
			.addInput(Items.DUST_GLOWSTONE)
			.addInput(Items.BOWL)
			.create("youthful_flower_blend", new ItemStack(BetterWithOcelotsItems.YOUTHFUL_FLOWER_BLEND));
	}
}
