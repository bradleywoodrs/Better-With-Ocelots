package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;

// ported from 1.2.5 deobf code (EntityAIFollowOwner)
public class AIFollowOwner extends AITask {
	private Player owner;
	private int repathTimer;
	private boolean savedAvoidWater;

	public AIFollowOwner(MobOcelot cat) {
		super(cat, 3);
	}

	@Override
	boolean shouldExecute() {
		Player p = this.cat.getOwnerEntity();
		if (p == null || this.cat.isSittingAny() || this.cat.hasSitOrder() || this.cat.distanceToSqr(p) < 100.0) return false;
		this.owner = p;
		return true;
	}

	@Override
	boolean continueExecuting() {
		return !this.cat.getNavigator().noPath() && this.owner.isAlive() && this.cat.distanceToSqr(this.owner) > 25.0 && !this.cat.isSittingAny() && !this.cat.hasSitOrder();
	}

	@Override
	void start() {
		this.repathTimer = 0;
		this.savedAvoidWater = this.cat.getNavigator().isAvoidingWater();
		this.cat.getNavigator().setAvoidingWater(false);
	}

	@Override
	void stop() {
		this.owner = null;
		this.cat.getNavigator().clearPath();
		this.cat.getNavigator().setAvoidingWater(this.savedAvoidWater);
	}

	@Override
	void update() {
		this.cat.setLook(this.owner, 10.0F, 40.0F);
		if (this.cat.isSittingAny() || this.cat.hasSitOrder() || --this.repathTimer > 0) return;
		this.repathTimer = REPATH_TICKS;
		if (this.cat.getNavigator().tryMoveToEntity(this.owner, MobOcelot.SPEED_FOLLOW)) return;
		if (this.cat.distanceToSqr(this.owner) < 144.0) return;

		int baseX = MathHelper.floor(this.owner.x) - 2;
		int baseZ = MathHelper.floor(this.owner.z) - 2;
		int y = MathHelper.floor(this.owner.bb.minY);
		for (int i = 0; i <= 4; ++i) {
			for (int j = 0; j <= 4; ++j) {
				if ((i < 1 || j < 1 || i > 3 || j > 3)
					&& (this.cat.world.isBlockNormalCube(baseX + i, y - 1, baseZ + j) || isWater(baseX + i, y - 1, baseZ + j) || isWater(baseX + i, y, baseZ + j))
					&& !this.cat.world.isBlockNormalCube(baseX + i, y, baseZ + j)
					&& !this.cat.world.isBlockNormalCube(baseX + i, y + 1, baseZ + j)) {
					this.cat.moveTo(baseX + i + 0.5F, y, baseZ + j + 0.5F, this.cat.yRot, this.cat.xRot);
					this.cat.fallDistance = 0.0F;
					this.cat.getNavigator().clearPath();
					return;
				}
			}
		}
	}

	private boolean isWater(int x, int y, int z) {
		Block<?> block = Blocks.blocksList[this.cat.world.getBlockId(x, y, z)];
		return block != null && block.getMaterial() == Materials.WATER;
	}
}
