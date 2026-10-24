package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.MathHelper;

// ported from 1.2.5 deobf code (EntityAILeapAtTarget)
public class AILeap extends AITask {
	public AILeap(MobOcelot cat) {
		super(cat, 5);
	}

	@Override
	boolean shouldExecute() {
		Entity target = this.cat.getAttackTarget();
		if (target == null) return false;
		double d = this.cat.distanceToSqr(target);
		return d >= 4.0 && d <= 16.0 && this.cat.onGround && this.cat.getRandom().nextInt(5) == 0;
	}

	@Override
	boolean continueExecuting() {
		return !this.cat.onGround;
	}

	@Override
	void start() {
		Entity target = this.cat.getAttackTarget();
		double dx = target.x - this.cat.x;
		double dz = target.z - this.cat.z;
		double len = MathHelper.sqrt(dx * dx + dz * dz);
		this.cat.xd += dx / len * 0.5 * 0.800000011920929 + this.cat.xd * 0.20000000298023224;
		this.cat.zd += dz / len * 0.5 * 0.800000011920929 + this.cat.zd * 0.20000000298023224;
		this.cat.yd = 0.3F;
	}
}
