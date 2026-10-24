package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding.EntityTargeting;

// ported from 1.2.5 deobf code (EntityAIWander)
public class AIWander extends AITask {
	public AIWander(MobOcelot cat) {
		super(cat, 1);
	}

	@Override
	boolean shouldExecute() {
		if (this.cat.getIdleTicks() >= 100 || this.cat.getRandom().nextInt(120) != 0) return false;
		int[] dest = EntityTargeting.findRandomTarget(this.cat, 10, 7, this.cat::getBlockPathWeight);
		return dest != null && this.cat.getNavigator().tryMoveToXYZ(dest[0], dest[1], dest[2], MobOcelot.SPEED_WALK);
	}

	@Override
	boolean continueExecuting() {
		return !this.cat.getNavigator().noPath();
	}
}
