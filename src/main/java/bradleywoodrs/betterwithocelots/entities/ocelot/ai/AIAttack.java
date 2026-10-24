package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.DamageType;

// ported from 1.2.5 deobf code (EntityAIOcelotAttack)
public class AIAttack extends AITask {
	private Entity prey;
	private int cooldown;
	private int repathTimer;

	public AIAttack(MobOcelot cat) {
		super(cat, 3);
	}

	@Override
	boolean shouldExecute() {
		if (this.cat.getAttackTarget() == null) return false;
		this.prey = this.cat.getAttackTarget();
		return true;
	}

	@Override
	boolean continueExecuting() {
		return this.prey.isAlive() && this.cat.distanceToSqr(this.prey) <= 225.0 && (!this.cat.getNavigator().noPath() || shouldExecute());
	}

	@Override
	void stop() {
		this.prey = null;
		this.repathTimer = 0;
		this.cat.getNavigator().clearPath();
	}

	@Override
	void update() {
		this.cat.setLook(this.prey, 30.0F, 30.0F);
		double reach = this.cat.bbWidth * 2.0F * (this.cat.bbWidth * 2.0F);
		double d = this.cat.distanceToSqr(this.prey.x, this.prey.bb.minY, this.prey.z);
		float speed = MobOcelot.SPEED_WALK;
		if (d > reach && d < 16.0) {
			speed = MobOcelot.SPEED_SPRINT;
		} else if (d < 225.0) {
			speed = MobOcelot.SPEED_SNEAK;
		}
		if (--this.repathTimer <= 0 || this.cat.getNavigator().noPath()) {
			this.repathTimer = REPATH_TICKS;
			this.cat.getNavigator().tryMoveToEntity(this.prey, speed);
		} else {
			this.cat.getNavigator().setSpeed(speed);
		}
		this.cooldown = Math.max(this.cooldown - 1, 0);
		if (d > reach || this.cooldown > 0) return;
		this.cooldown = 20;
		this.prey.hurt(this.cat, 3, DamageType.COMBAT);
	}
}
