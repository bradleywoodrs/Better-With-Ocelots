package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.BetterWithOcelots;
import bradleywoodrs.betterwithocelots.entities.ocelot.MobCatGuideEntry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.guidebook.mobs.GuidebookPageMob;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.achievement.stat.StatsCounter;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuidebookPageMob.class, remap = false)
public class GuidebookPageMobMixin {
	@Shadow @Final private Class<? extends Entity> mobClass;

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/EntityDispatcher;createEntityInWorld(Ljava/lang/Class;Lnet/minecraft/core/world/World;)Lnet/minecraft/core/entity/Entity;"))
	private Entity betterwithocelots$createFakeCat(EntityDispatcher dispatcher, Class<? extends Entity> clazz, World world, Operation<Entity> original) {
		if (clazz == MobCatGuideEntry.class) {
			return new MobCatGuideEntry(world);
		}
		return original.call(dispatcher, clazz, world);
	}

	@WrapOperation(method = {"<init>", "renderForeground"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/achievement/stat/StatsCounter;readStat(Lnet/minecraft/core/achievement/stat/Stat;)I"), require = 0)
	private int betterwithocelots$catsTamed(StatsCounter counter, Stat stat, Operation<Integer> original) {
		return original.call(counter, this.mobClass == MobCatGuideEntry.class ? BetterWithOcelots.CATS_TAMED_STAT : stat);
	}
}
