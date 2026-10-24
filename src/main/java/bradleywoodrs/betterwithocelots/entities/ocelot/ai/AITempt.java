package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.BetterWithOcelotsTags;
import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;

// ported from 1.2.5 deobf code (EntityAITempt)
public class AITempt extends AITask {
	private Player player;
	private double startX, startY, startZ, lastPitch, lastYaw;
	private int cooldown;
	private int repathTimer;
	private boolean savedAvoidWater;

	public AITempt(MobOcelot cat) {
		super(cat, 3);
	}

	@Override
	boolean shouldExecute() {
		if (this.cooldown > 0) {
			--this.cooldown;
			return false;
		}
		this.player = this.cat.world.getClosestPlayerToEntity(this.cat, 10.0);
		if (this.player == null) return false;
		ItemStack held = this.player.inventory.getCurrentItem();
		return Item.hasTag(held, BetterWithOcelotsTags.CATS_FAVOURITE_ITEM) || Item.hasTag(held, BetterWithOcelotsTags.CATS_SPECIAL_ITEM);
	}

	@Override
	boolean continueExecuting() {
		if (this.cat.distanceToSqr(this.player) < 36.0) {
			if (this.player.distanceToSqr(this.startX, this.startY, this.startZ) > 0.010000000000000002) return false;
			if (Math.abs(this.player.xRot - this.lastPitch) > 5.0 || Math.abs(this.player.yRot - this.lastYaw) > 5.0) return false;
		} else {
			this.startX = this.player.x;
			this.startY = this.player.y;
			this.startZ = this.player.z;
		}
		this.lastPitch = this.player.xRot;
		this.lastYaw = this.player.yRot;
		return shouldExecute();
	}

	@Override
	void start() {
		this.startX = this.player.x;
		this.startY = this.player.y;
		this.startZ = this.player.z;
		this.savedAvoidWater = this.cat.getNavigator().isAvoidingWater();
		this.cat.getNavigator().setAvoidingWater(false);
	}

	@Override
	void stop() {
		this.player = null;
		this.repathTimer = 0;
		this.cat.getNavigator().clearPath();
		this.cooldown = 100;
		this.cat.getNavigator().setAvoidingWater(this.savedAvoidWater);
	}

	@Override
	void update() {
		this.cat.setLook(this.player, 30.0F, 40.0F);
		if (this.cat.distanceToSqr(this.player) < 6.25) {
			this.cat.getNavigator().clearPath();
		} else if (--this.repathTimer <= 0 || this.cat.getNavigator().noPath()) {
			this.repathTimer = REPATH_TICKS;
			this.cat.getNavigator().tryMoveToEntity(this.player, MobOcelot.SPEED_SNEAK);
		}
	}
}
