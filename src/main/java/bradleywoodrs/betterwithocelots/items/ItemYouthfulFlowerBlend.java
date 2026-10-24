package bradleywoodrs.betterwithocelots.items;

import bradleywoodrs.betterwithocelots.interfaces.IMobCowMixin;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.TextFormatting;

public class ItemYouthfulFlowerBlend extends Item {
	public ItemYouthfulFlowerBlend(String name, String namespaceId, int id) {
		super(name, namespaceId, id);
	}

	@Override
	public String getTranslatedName(ItemStack stack) {
		return TextFormatting.YELLOW + super.getTranslatedName(stack);
	}

	@Override
	public boolean useOnEntity(ItemStack stack, Player player, Mob target) {
		if (!(target instanceof IMobCowMixin) || ((IMobCowMixin) target).betterwithocelots$isYouthful()) return false;
		if (player.getGamemode().hasBlockConsumption()) {
			BetterWithOcelotsItems.consumeHeldItem(player, stack);
			if (!player.world.isClientSide) {
				BetterWithOcelotsItems.giveItem(player, new ItemStack(Items.BOWL));
			}
		}
		if (!player.world.isClientSide && player.world.rand.nextInt(3) == 0) {
			((IMobCowMixin) target).betterwithocelots$setYouthful(true);
		}
		return true;
	}
}
