package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin {
	@Inject(method = {"setPlayerSleeping", "handlePortal"}, at = @At("HEAD"))
	private void betterwithocelots$dropShoulderCat(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (!player.world.isClientSide && player.passenger instanceof MobOcelot) {
			player.ejectRider();
		}
	}
}
