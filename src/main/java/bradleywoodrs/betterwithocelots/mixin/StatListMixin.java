package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.BetterWithOcelots;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.achievement.stat.StatList;
import net.minecraft.core.util.collection.NamespaceID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StatList.class, remap = false)
public class StatListMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private static void betterwithocelots$registerStats(CallbackInfo ci) {
		if (BetterWithOcelots.CATS_TAMED_STAT == null) {
			BetterWithOcelots.CATS_TAMED_STAT = new Stat(NamespaceID.getPermanent(BetterWithOcelots.MOD_ID, "tame_cat"), "stat.betterwithocelots.catsTamed").registerStat();
		}
	}
}
