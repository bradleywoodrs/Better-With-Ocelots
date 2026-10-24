package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding.EntityTargeting;
import net.minecraft.core.entity.player.Player;

// ported from 1.2.5 deobf code (EntityAIAvoidEntity)
public class AIAvoidPlayer extends AITask {
	private static final int RETRY_TICKS = 10;
	private Player threat;
	private int retryTicks;

	public AIAvoidPlayer(MobOcelot cat) {
		super(cat, 1);
	}

	@Override
	boolean shouldExecute() {
		if (this.retryTicks > 0) this.retryTicks--;
		if (this.cat.isTamed()) return false;
		this.threat = this.cat.world.getClosestPlayerToEntity(this.cat, 16.0);
		if (this.threat == null || !this.cat.canEntityBeSeen(this.threat) || this.retryTicks > 0) return false;
		this.retryTicks = RETRY_TICKS;
		int[] dest = EntityTargeting.findRandomTargetAwayFrom(this.cat, 16, 7,
			this.threat.x, this.threat.y, this.threat.z, this.cat::getBlockPathWeight);
		if (dest == null) return false;
		if (this.threat.distanceToSqr(dest[0], dest[1], dest[2]) < this.threat.distanceToSqr(this.cat)) return false;
		int[][] path = this.cat.getNavigator().findPath(dest[0], dest[1], dest[2]);
		if (path == null || path[path.length - 1][0] != dest[0] || path[path.length - 1][2] != dest[2]) return false;
		if (!this.cat.getNavigator().setPath(path, MobOcelot.SPEED_WALK)) return false;
		this.retryTicks = 0;
		return true;
	}

	@Override
	boolean continueExecuting() {
		return !this.cat.getNavigator().noPath();
	}

	@Override
	void stop() {
		this.threat = null;
	}

	@Override
	void update() {
		this.cat.getNavigator().setSpeed(this.cat.distanceToSqr(this.threat) < 49.0 ? MobOcelot.SPEED_SPRINT : MobOcelot.SPEED_WALK);
	}
}
