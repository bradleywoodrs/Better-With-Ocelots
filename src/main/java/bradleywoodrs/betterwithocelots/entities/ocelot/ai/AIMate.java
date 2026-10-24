package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.util.helper.MathHelper;
import org.joml.primitives.AABBd;

// ported from 1.2.5 deobf code (EntityAIMate)
public class AIMate extends AITask {
	private final AABBd searchBox = new AABBd();
	private MobOcelot mate;
	private int breedTicks;
	private int repathTimer;

	public AIMate(MobOcelot cat) {
		super(cat, 3);
	}

	@Override
	boolean shouldExecute() {
		if (!this.cat.isInLove()) return false;
		for (MobOcelot other : this.cat.world.getEntitiesWithinAABB(MobOcelot.class, MathHelper.aabbGrow(this.cat.bb, 8.0, 8.0, 8.0, this.searchBox))) {
			if (this.cat.canMateWith(other)) {
				this.mate = other;
				return true;
			}
		}
		return false;
	}

	@Override
	boolean continueExecuting() {
		return this.mate.isAlive() && this.mate.isInLove() && this.breedTicks < 60;
	}

	@Override
	void stop() {
		this.mate = null;
		this.breedTicks = 0;
		this.repathTimer = 0;
	}

	@Override
	void update() {
		this.cat.setLook(this.mate, 10.0F, 40.0F);
		if (--this.repathTimer <= 0 || this.cat.getNavigator().noPath()) {
			this.repathTimer = REPATH_TICKS;
			this.cat.getNavigator().tryMoveToEntity(this.mate, MobOcelot.SPEED_WALK);
		}
		++this.breedTicks;
		if (this.breedTicks == 60) {
			this.cat.breedWith(this.mate);
		}
	}
}
