package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicBed;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntitySeat;
import net.minecraft.core.world.pos.TilePos;

// ported from 1.2.5 deobf code (EntityAIOcelotSit)
public class AISitOnBlock extends AITask {
	private static final double SIT_CHANCE = 0.0065;

	private int sitTicks, failTicks, maxSitTicks;
	private int bx, by, bz;

	public AISitOnBlock(MobOcelot cat) {
		super(cat, 5);
	}

	@Override
	boolean shouldExecute() {
		return this.cat.isTamed() && !this.cat.isSittingAny() && this.cat.getRandom().nextDouble() <= SIT_CHANCE && findBlock();
	}

	@Override
	boolean continueExecuting() {
		return this.sitTicks <= this.maxSitTicks && this.failTicks <= 60 && isSitBlock(this.bx, this.by, this.bz);
	}

	@Override
	void start() {
		this.cat.getNavigator().tryMoveToXYZ(this.bx + 0.5, this.by + 1, this.bz + 0.5, MobOcelot.SPEED_SPRINT);
		this.sitTicks = 0;
		this.failTicks = 0;
		this.maxSitTicks = this.cat.getRandom().nextInt(this.cat.getRandom().nextInt(1200) + 1200) + 1200;
		this.cat.setSitOrder(false);
	}

	@Override
	void stop() {
		this.cat.setFurniSitting(false);
	}

	@Override
	void update() {
		++this.sitTicks;
		this.cat.setSitOrder(false);
		if (this.cat.distanceToSqr(this.bx, this.by + 1, this.bz) > 1.0) {
			this.cat.setFurniSitting(false);
			this.cat.getNavigator().tryMoveToXYZ(this.bx + 0.5, this.by + 1, this.bz + 0.5, MobOcelot.SPEED_SPRINT);
			++this.failTicks;
		} else if (!this.cat.isFurniSitting()) {
			this.cat.setFurniSitting(true);
		} else {
			--this.failTicks;
		}
	}

	private boolean findBlock() {
		int y = (int) this.cat.y;
		double best = Integer.MAX_VALUE;
		for (int x = (int) this.cat.x - 8; x < this.cat.x + 8.0; ++x) {
			for (int z = (int) this.cat.z - 8; z < this.cat.z + 8.0; ++z) {
				if (isSitBlock(x, y, z) && this.cat.world.isAirBlock(x, y + 1, z)) {
					double d = this.cat.distanceToSqr(x, y, z);
					if (d < best) {
						this.bx = x;
						this.by = y;
						this.bz = z;
						best = d;
					}
				}
			}
		}
		return best < Integer.MAX_VALUE;
	}

	private boolean isSitBlock(int x, int y, int z) {
		Block<?> block = Blocks.blocksList[this.cat.world.getBlockId(x, y, z)];
		if (block == null) return false;
		if (block == Blocks.CHEST_PLANKS_OAK || block == Blocks.CHEST_PLANKS_OAK_PAINTED
			|| block == Blocks.CHEST_LEGACY || block == Blocks.CHEST_LEGACY_PAINTED) {
			return true;
		}
		if (block == Blocks.FURNACE_STONE_ACTIVE) {
			return true;
		}
		if (block == Blocks.SEAT) {
			TileEntity te = this.cat.world.getTileEntity(new TilePos(x, y, z));
			return !(te instanceof TileEntitySeat) || ((TileEntitySeat) te).getPassenger() == null;
		}
		return block == Blocks.BED && !BlockLogicBed.IS_HEAD.bool(this.cat.world.getBlockMetadata(x, y, z));
	}
}
