package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.interfaces.IMobCowMixin;
import bradleywoodrs.betterwithocelots.items.ItemMilkOfEternalYouth;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.pos.TilePos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Mob.class, remap = false)
public class MobMixin {
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void betterwithocelots$milkYouthfulCow(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (!(this instanceof IMobCowMixin)) return;
		IMobCowMixin cow = (IMobCowMixin) this;
		if (!cow.betterwithocelots$isYouthful()) return;
		ItemStack held = player.inventory.getCurrentItem();
		boolean isEmpty = ItemMilkOfEternalYouth.isEmptyBucket(held);
		ItemBucket bucket = ItemMilkOfEternalYouth.isMilkOfEternalYouth(held) ? ItemMilkOfEternalYouth.getBucket(held) : null;
		boolean canTopUpMilk = bucket != null && ItemMilkOfEternalYouth.getCharges(held) < bucket.maxCharges;
		if (!isEmpty && !canTopUpMilk) return;
		if (isEmpty) {
			if (!player.world.isClientSide) {
				ItemStack milk = ItemMilkOfEternalYouth.createItemStack(held.getItem(), 1);
				if (held.stackSize > 1) {
					--held.stackSize;
					player.inventory.insertItem(milk, true);
					if (milk.stackSize > 0) {
						player.world.dropItem(new TilePos(player), milk);
					}
				} else {
					player.inventory.setItem(player.inventory.getCurrentSlot(), milk);
				}
			}
		} else {
			ItemBucket.setCharges(held, ItemMilkOfEternalYouth.getCharges(held) + 1);
		}
		if (!player.world.isClientSide) {
			cow.betterwithocelots$setYouthful(false);
		}
		cir.setReturnValue(true);
	}
}
