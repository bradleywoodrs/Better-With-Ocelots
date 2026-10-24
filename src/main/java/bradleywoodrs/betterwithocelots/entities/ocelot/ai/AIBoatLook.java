package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.Entity;

public class AIBoatLook extends AITask {
	private final AIGlance glance;

	public AIBoatLook(MobOcelot cat) {
		super(cat, 5);
		this.glance = new AIGlance(cat);
	}

	@Override
	boolean allowedWhileRiding() {
		return true;
	}

	@Override
	boolean shouldExecute() {
		return this.cat.isBoatRiding();
	}

	@Override
	void stop() {
		this.glance.reset();
	}

	@Override
	void update() {
		this.glance.update((Entity) this.cat.vehicle);
	}
}
