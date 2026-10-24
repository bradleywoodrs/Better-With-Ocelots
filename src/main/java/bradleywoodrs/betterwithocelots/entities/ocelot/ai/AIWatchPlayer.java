package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.player.Player;

// ported from 1.2.5 deobf code (EntityAIWatchClosest)
public class AIWatchPlayer extends AITask {
	private Player watched;
	private int ticks;

	public AIWatchPlayer(MobOcelot cat) {
		super(cat, 2);
	}

	@Override
	boolean shouldExecute() {
		if (this.cat.getRandom().nextFloat() >= 0.02F) return false;
		this.watched = this.cat.world.getClosestPlayerToEntity(this.cat, 10.0);
		return this.watched != null;
	}

	@Override
	boolean continueExecuting() {
		return this.watched.isAlive() && this.cat.distanceToSqr(this.watched) <= 100.0 && this.ticks > 0;
	}

	@Override
	void start() {
		this.ticks = 40 + this.cat.getRandom().nextInt(40);
	}

	@Override
	void stop() {
		this.watched = null;
	}

	@Override
	void update() {
		this.cat.setLook(this.watched, 10.0F, 40.0F);
		--this.ticks;
	}
}
