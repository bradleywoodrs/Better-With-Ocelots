package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;

// ported from 1.2.5 deobf code (EntityAIBase)
public abstract class AITask {
	static final int REPATH_TICKS = 10;

	protected final MobOcelot cat;
	final int mutex;
	int priority;
	boolean running;

	AITask(MobOcelot cat, int mutex) {
		this.cat = cat;
		this.mutex = mutex;
	}

	abstract boolean shouldExecute();

	boolean continueExecuting() {
		return shouldExecute();
	}

	boolean interruptible() {
		return true;
	}

	boolean allowedWhileRiding() {
		return false;
	}

	void start() {
	}

	void stop() {
	}

	void update() {
	}

	public boolean isRunning() {
		return this.running;
	}
}
