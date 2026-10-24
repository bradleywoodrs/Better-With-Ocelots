package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import org.joml.primitives.AABBd;

import java.util.List;

public class AIShoulder extends AITask {
	private static final double SIT_CHANCE = 0.0065;

	private Player owner;
	private int sitTicks, maxSitTicks, cooldown;
	private final AIGlance glance;
	private final AABBd searchBox = new AABBd();

	public AIShoulder(MobOcelot cat) {
		super(cat, 5);
		this.glance = new AIGlance(cat);
	}

	@Override
	boolean allowedWhileRiding() {
		return true;
	}

	@Override
	boolean shouldExecute() {
		if (this.cooldown > 0) {
			--this.cooldown;
			return false;
		}
		if (!this.cat.isKitten() || !this.cat.isTamed() || this.cat.isSittingAny() || this.cat.getRandom().nextDouble() > SIT_CHANCE) return false;
		Player p = this.cat.getOwnerEntity();
		if (p == null || p.isSneaking() || p.isPlayerSleeping() || this.cat.distanceToSqr(p) >= 4.0) return false;
		if (hasShoulderRider(p)) {
			this.cooldown = 600;
			return false;
		}
		this.owner = p;
		return true;
	}

	private boolean hasShoulderRider(Player p) {
		if (p.passenger != null) return true;
		List<MobOcelot> nearby = this.cat.world.getEntitiesWithinAABB(MobOcelot.class, MathHelper.aabbGrow(p.bb, 2.0, 2.0, 2.0, this.searchBox));
		for (MobOcelot other : nearby) {
			if (other != this.cat && other.vehicle == p) return true;
		}
		return false;
	}

	@Override
	boolean continueExecuting() {
		return this.cat.vehicle == this.owner && this.sitTicks < this.maxSitTicks;
	}

	@Override
	void start() {
		this.cat.getNavigator().clearPath();
		this.cat.setSitOrder(false);
		this.sitTicks = 0;
		this.maxSitTicks = 1200 + this.cat.getRandom().nextInt(2401);
		if (hasShoulderRider(this.owner)) return;
		this.cat.startRiding(this.owner);
	}

	@Override
	void stop() {
		if (this.cat.vehicle == this.owner) {
			this.owner.ejectRider();
		}
		this.owner = null;
		this.glance.reset();
		this.cooldown = 600;
	}

	@Override
	void update() {
		++this.sitTicks;
		this.glance.update(this.owner);
	}
}
