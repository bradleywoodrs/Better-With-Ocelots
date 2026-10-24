package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;
import java.util.Random;

class AIGlance {
	private final MobOcelot cat;
	private final AABBd searchBox = new AABBd();
	@Nullable private Entity glanceEntity;
	private double glanceX, glanceY, glanceZ;
	private int glanceTicks;

	AIGlance(MobOcelot cat) {
		this.cat = cat;
	}

	void reset() {
		this.glanceEntity = null;
		this.glanceTicks = 0;
	}

	void update(@Nullable Entity ignore) {
		if (this.glanceTicks > 0) {
			--this.glanceTicks;
			if (this.glanceEntity != null && this.glanceEntity.isAlive()) {
				this.cat.setLook(this.glanceEntity, 10.0F, 20.0F);
			} else {
				this.cat.setLook(this.glanceX, this.glanceY, this.glanceZ, 10.0F, 20.0F);
			}
		} else if (this.cat.getRandom().nextFloat() < 0.02F) {
			pick(ignore);
		}
	}

	private void pick(@Nullable Entity ignore) {
		Random random = this.cat.getRandom();
		this.glanceTicks = 40 + random.nextInt(60);
		this.glanceEntity = null;
		if (random.nextBoolean()) {
			List<Mob> nearby = this.cat.world.getEntitiesWithinAABB(Mob.class, MathHelper.aabbGrow(this.cat.bb, 8.0, 4.0, 8.0, this.searchBox));
			nearby.removeIf(e -> e == this.cat || e == ignore || !e.isAlive());
			if (!nearby.isEmpty()) {
				this.glanceEntity = nearby.get(random.nextInt(nearby.size()));
				return;
			}
		}
		float yaw = (this.cat.yBodyRot + (random.nextFloat() * 2.0F - 1.0F) * 70.0F) * MathHelper.DEG_TO_RAD;
		float pitch = (random.nextFloat() * 50.0F - 20.0F) * MathHelper.DEG_TO_RAD;
		this.glanceX = this.cat.x - MathHelper.sin(yaw) * 4.0;
		this.glanceY = this.cat.y + this.cat.getHeadHeight() - Math.tan(pitch) * 4.0;
		this.glanceZ = this.cat.z + MathHelper.cos(yaw) * 4.0;
	}
}
