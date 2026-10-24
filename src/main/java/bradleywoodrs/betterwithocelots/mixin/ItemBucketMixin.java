package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.items.ItemMilkOfEternalYouth;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = ItemBucket.class, remap = false)
public class ItemBucketMixin {
	@Inject(method = "onUseByActivator", at = @At("HEAD"), cancellable = true)
	private void betterwithocelots$activatorMilkYouthfulCow(ItemStack stack, World world, TileEntityActivator activator, Random random, TilePosc pos, Direction direction, double offX, double offY, double offZ, CallbackInfo ci) {
		if (ItemMilkOfEternalYouth.isEmptyBucket(stack) && ItemMilkOfEternalYouth.handleActivatorFill(stack, world, activator, pos, direction)) {
			ci.cancel();
		}
	}
}
