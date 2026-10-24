package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding.EntityTargeting;
import bradleywoodrs.betterwithocelots.interfaces.IMobCreeperMixin;
import net.minecraft.core.entity.monster.MobCreeper;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pathfinder.Path;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// ported from 1.2.5 deobf code (EntityCreeper/EntityAIAvoidEntity)
@Mixin(value = MobCreeper.class, remap = false)
public abstract class MobCreeperMixin extends MobMonster implements IMobCreeperMixin {
	@Unique private static final float FLEE_SPEED_FAR = 0.625F;
	@Unique private static final float FLEE_SPEED_NEAR = 0.9F;
	@Unique private static final int FLEE_RETRY_TICKS = 10;
	@Unique private boolean betterwithocelots$fleeing;
	@Unique private MobOcelot betterwithocelots$fleeFrom;
	@Unique private float betterwithocelots$savedMoveSpeed;
	@Unique private int betterwithocelots$fleeRetryTicks;
	@Unique private final AABBd betterwithocelots$searchBox = new AABBd();

	@Shadow int timeSinceIgnited;

	@Shadow private int getCreeperState() {
		throw new AssertionError();
	}

	@Shadow private void setCreeperState(int state) {
	}

	protected MobCreeperMixin(World world) {
		super(world);
	}

	@Override
	public boolean betterwithocelots$isFleeing() {
		return this.betterwithocelots$fleeing;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void betterwithocelots$avoidOcelots(CallbackInfo ci) {
		if (this.world.isClientSide) return;

		if (this.betterwithocelots$fleeing) {
			Player close = this.world.getClosestPlayerToEntity(this, 3.0);
			if (this.pathToEntity == null || !this.betterwithocelots$fleeFrom.isAlive() || close != null) {
				betterwithocelots$stopFleeing();
				return;
			}
			this.target = null;
			this.moveSpeed = this.distanceToSqr(this.betterwithocelots$fleeFrom) < 49.0 ? FLEE_SPEED_NEAR : FLEE_SPEED_FAR;
			if (this.timeSinceIgnited > 0) {
				this.setCreeperState(-1);
				this.timeSinceIgnited--;
			}
			return;
		}
		if (this.betterwithocelots$fleeRetryTicks > 0) this.betterwithocelots$fleeRetryTicks--;
		if (this.getCreeperState() > 0 || (this.target != null && this.distanceToSqr(this.target) < 9.0)) return;
		List<MobOcelot> ocelots = this.world.getEntitiesWithinAABB(MobOcelot.class, MathHelper.aabbGrow(this.bb, 6.0, 3.0, 6.0, this.betterwithocelots$searchBox));
		if (ocelots.isEmpty()) return;
		MobOcelot ocelot = ocelots.get(0);
		if (!this.canEntityBeSeen(ocelot) || this.betterwithocelots$fleeRetryTicks > 0) return;
		this.betterwithocelots$fleeRetryTicks = FLEE_RETRY_TICKS;
		int[] dest = EntityTargeting.findRandomTargetAwayFrom(this, 16, 7, ocelot.x, ocelot.y, ocelot.z, this::getBlockPathWeight);
		if (dest == null) return;
		if (ocelot.distanceToSqr(dest[0], dest[1], dest[2]) < ocelot.distanceToSqr(this)) return;
		Path path = this.world.getEntityPathToXYZ(this, dest[0], dest[1], dest[2], 16.0F);
		if (!EntityTargeting.pathEndsAt(path, dest[0], dest[2])) return;
		this.betterwithocelots$fleeRetryTicks = 0;
		this.betterwithocelots$fleeing = true;
		this.betterwithocelots$fleeFrom = ocelot;
		this.betterwithocelots$savedMoveSpeed = this.moveSpeed;
		this.target = null;
		this.doRandomWalk = false;
		this.setPathToEntity(path);
		this.moveSpeed = FLEE_SPEED_FAR;
	}

	@Unique
	private void betterwithocelots$stopFleeing() {
		this.betterwithocelots$fleeing = false;
		this.betterwithocelots$fleeFrom = null;
		this.moveSpeed = this.betterwithocelots$savedMoveSpeed;
		this.doRandomWalk = true;
	}
}
