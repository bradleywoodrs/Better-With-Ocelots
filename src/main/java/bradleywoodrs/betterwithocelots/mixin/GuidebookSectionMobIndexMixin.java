package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.BetterWithOcelots;
import bradleywoodrs.betterwithocelots.entities.ocelot.MobCatGuideEntry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.guidebook.mobs.MobInfoRegistry;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.achievement.stat.StatsCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.guidebook.mobs.GuidebookSectionMob$Index", remap = false)
public class GuidebookSectionMobIndexMixin {
	@Shadow @Final private MobInfoRegistry.MobInfo mobInfo;

	@WrapOperation(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/achievement/stat/StatsCounter;readStat(Lnet/minecraft/core/achievement/stat/Stat;)I"), require = 0)
	private int betterwithocelots$catsTamed(StatsCounter counter, Stat stat, Operation<Integer> original) {
		return original.call(counter, this.mobInfo.getEntityClass() == MobCatGuideEntry.class ? BetterWithOcelots.CATS_TAMED_STAT : stat);
	}
}
