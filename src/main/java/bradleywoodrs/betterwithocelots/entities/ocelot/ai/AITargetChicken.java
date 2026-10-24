package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.util.helper.MathHelper;
import org.joml.primitives.AABBd;

import java.util.Comparator;
import java.util.List;

// ported from 1.2.5 deobf code (EntityAITargetNonTamed/EntityAINearestAttackableTarget)
public class AITargetChicken extends AITask {
	private final AABBd searchBox = new AABBd();

	public AITargetChicken(MobOcelot cat) {
		super(cat, 1);
	}

	@Override
	boolean shouldExecute() {
		if (this.cat.isTamed() || this.cat.getRandom().nextInt(750) != 0) return false;
		List<MobChicken> chickens = this.cat.world.getEntitiesWithinAABB(MobChicken.class, MathHelper.aabbGrow(this.cat.bb, 14.0, 4.0, 14.0, this.searchBox));
		chickens.sort(Comparator.comparingDouble(c -> this.cat.distanceToSqr(c)));
		for (MobChicken chicken : chickens) {
			if (chicken.isAlive() && chicken.bb.maxY > this.cat.bb.minY && chicken.bb.minY < this.cat.bb.maxY) {
				this.cat.setAttackTarget(chicken);
				return true;
			}
		}
		return false;
	}

	@Override
	boolean continueExecuting() {
		Entity target = this.cat.getAttackTarget();
		return target != null && target.isAlive() && this.cat.distanceToSqr(target) <= 196.0;
	}

	@Override
	void stop() {
		this.cat.setAttackTarget(null);
	}
}
