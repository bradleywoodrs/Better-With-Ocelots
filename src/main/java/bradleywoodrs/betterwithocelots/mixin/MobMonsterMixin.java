package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.interfaces.IMobCreeperMixin;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobMonster.class, remap = false)
public class MobMonsterMixin {
	@Inject(method = "findPlayerToAttack", at = @At("HEAD"), cancellable = true)
	private void betterwithocelots$noTargetWhileFleeing(CallbackInfoReturnable<Entity> cir) {
		if (this instanceof IMobCreeperMixin && ((IMobCreeperMixin) this).betterwithocelots$isFleeing()) {
			cir.setReturnValue(null);
		}
	}
}
