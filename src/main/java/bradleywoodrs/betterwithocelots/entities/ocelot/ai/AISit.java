package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;

// ported from 1.2.5 deobf code (EntityAISit)
public class AISit extends AITask {
	public AISit(MobOcelot cat) {
		super(cat, 5);
	}

	@Override
	boolean shouldExecute() {
		if (!this.cat.isTamed() || this.cat.isInWater() || !this.cat.onGround) return false;
		return this.cat.getOwnerEntity() == null || this.cat.hasSitOrder();
	}

	@Override
	void start() {
		this.cat.getNavigator().clearPath();
		this.cat.setOcelotSitting(true);
	}

	@Override
	void stop() {
		this.cat.setOcelotSitting(false);
	}
}
