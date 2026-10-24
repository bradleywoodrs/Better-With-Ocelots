package bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicDoor;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

// ported from 1.2.5 deobf code (PathNavigate)
public class EntityNavigation {
	private static final float PATH_RANGE = 16.0F;
	private static final int STUCK_CHECK_TICKS = 100;
	private static final double STUCK_DIST_SQ = 1.5 * 1.5;

	private final MobOcelot cat;
	private double[][] points;
	private int index;
	private float speed;
	private int ticks;
	private int lastCheckTicks;
	private double lastX, lastY, lastZ;
	private boolean avoidingWater = true;
	@Nullable private EntityPathFinding pathFinding;

	public EntityNavigation(MobOcelot cat) {
		this.cat = cat;
	}

	public boolean noPath() {
		return this.points == null || this.index >= this.points.length;
	}

	public void clearPath() {
		this.points = null;
	}

	public boolean isAvoidingWater() {
		return this.avoidingWater;
	}

	public void setAvoidingWater(boolean avoidingWater) {
		this.avoidingWater = avoidingWater;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	private boolean canNavigate() {
		if (this.cat.isPassenger()) return false;
		return this.cat.onGround || this.cat.isInWater() || this.cat.isInLava();
	}

	private EntityPathFinding pathFinder() {
		if (this.pathFinding == null || !this.pathFinding.matches(this.cat.world, this.avoidingWater)) {
			this.pathFinding = new EntityPathFinding(this.cat.world, this.avoidingWater, true);
		}
		return this.pathFinding;
	}

	@Nullable
	public int[][] findPath(int x, int y, int z) {
		return pathFinder().findPath(this.cat, x, y, z, PATH_RANGE);
	}

	public boolean tryMoveToXYZ(double x, double y, double z, float speed) {
		if (!canNavigate()) return false;
		return setPath(pathFinder().findPath(this.cat, MathHelper.floor(x), (int) y, MathHelper.floor(z), PATH_RANGE), speed);
	}

	public boolean tryMoveToEntity(Entity entity, float speed) {
		if (!canNavigate()) return false;
		return setPath(pathFinder().findPath(this.cat, entity, PATH_RANGE), speed);
	}

	public boolean setPath(@Nullable int[][] nodes, float speed) {
		if (nodes == null || nodes.length == 0) {
			this.points = null;
			return false;
		}
		double offset = (int) (this.cat.bbWidth + 1.0F) * 0.5;
		double[][] newPoints = new double[nodes.length][];
		for (int i = 0; i < nodes.length; ++i) {
			newPoints[i] = new double[]{nodes[i][0] + offset, nodes[i][1], nodes[i][2] + offset};
		}
		if (this.points == null || !Arrays.deepEquals(this.points, newPoints)) {
			this.points = newPoints;
			this.index = 0;
		}
		this.speed = speed;
		this.lastCheckTicks = this.ticks;
		this.lastX = this.cat.x;
		this.lastY = getPathableY();
		this.lastZ = this.cat.z;
		return true;
	}

	public void update() {
		++this.ticks;
		if (noPath()) return;
		if (canNavigate()) pathFollow();
		if (noPath()) return;
		double[] p = this.points[this.index];
		this.cat.setMoveTo(p[0], p[1], p[2], this.speed);
	}

	private int getPathableY() {
		if (!this.cat.isInWater()) {
			return (int) (this.cat.bb.minY + 0.5);
		}

		int y = (int) this.cat.bb.minY;
		for (int i = 0; i < 16; ++i) {
			Block<?> block = Blocks.blocksList[this.cat.world.getBlockId(MathHelper.floor(this.cat.x), y, MathHelper.floor(this.cat.z))];
			if (block == null || block.getMaterial() != Materials.WATER) {
				return y;
			}
			++y;
		}
		return (int) this.cat.bb.minY;
	}

	private void pathFollow() {
		double ex = this.cat.x;
		int ey = getPathableY();
		double ez = this.cat.z;
		int end = this.points.length;
		for (int i = this.index; i < this.points.length; ++i) {
			if ((int) this.points[i][1] != ey) {
				end = i;
				break;
			}
		}

		double w2 = this.cat.bbWidth * this.cat.bbWidth;
		for (int i = this.index; i < end; ++i) {
			double[] p = this.points[i];
			double dx = p[0] - ex, dy = p[1] - ey, dz = p[2] - ez;
			if (dx * dx + dy * dy + dz * dz < w2) {
				this.index = i + 1;
			}
		}

		int sizeX = (int) Math.ceil(this.cat.bbWidth);
		int sizeY = (int) this.cat.bbHeight + 1;
		for (int i = end - 1; i >= this.index; --i) {
			if (isDirectPath(ex, ey, ez, this.points[i][0], this.points[i][2], sizeX, sizeY, sizeX)) {
				this.index = i;
				break;
			}
		}

		if (this.ticks - this.lastCheckTicks > STUCK_CHECK_TICKS) {
			double dx = ex - this.lastX, dy = ey - this.lastY, dz = ez - this.lastZ;
			if (dx * dx + dy * dy + dz * dz < STUCK_DIST_SQ) {
				clearPath();
			}
			this.lastCheckTicks = this.ticks;
			this.lastX = ex;
			this.lastY = ey;
			this.lastZ = ez;
		}
	}

	private boolean isDirectPath(double ax, int ay, double az, double bx, double bz, int sizeX, int sizeY, int sizeZ) {
		int cx = MathHelper.floor(ax);
		int cz = MathHelper.floor(az);
		double dx = bx - ax;
		double dz = bz - az;
		double lenSq = dx * dx + dz * dz;
		if (lenSq < 1.0E-8) return false;
		double inv = 1.0 / Math.sqrt(lenSq);
		dx *= inv;
		dz *= inv;
		if (!isSafeToStandAt(cx, ay, cz, sizeX + 2, sizeY, sizeZ + 2, ax, az, dx, dz)) return false;
		double stepX = 1.0 / Math.abs(dx);
		double stepZ = 1.0 / Math.abs(dz);
		double tX = cx - ax;
		double tZ = cz - az;
		if (dx >= 0.0) ++tX;
		if (dz >= 0.0) ++tZ;
		tX /= dx;
		tZ /= dz;
		int dirX = dx < 0.0 ? -1 : 1;
		int dirZ = dz < 0.0 ? -1 : 1;
		int endX = MathHelper.floor(bx);
		int endZ = MathHelper.floor(bz);
		int remX = endX - cx;
		int remZ = endZ - cz;
		while (remX * dirX > 0 || remZ * dirZ > 0) {
			if (tX < tZ) {
				tX += stepX;
				cx += dirX;
				remX = endX - cx;
			} else {
				tZ += stepZ;
				cz += dirZ;
				remZ = endZ - cz;
			}
			if (!isSafeToStandAt(cx, ay, cz, sizeX, sizeY, sizeZ, ax, az, dx, dz)) return false;
		}
		return true;
	}

	private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, double ox, double oz, double dx, double dz) {
		int x0 = x - sizeX / 2;
		int z0 = z - sizeZ / 2;
		if (!isPositionClear(x0, y, z0, sizeX, sizeY, sizeZ, ox, oz, dx, dz)) return false;
		for (int i = x0; i < x0 + sizeX; ++i) {
			for (int k = z0; k < z0 + sizeZ; ++k) {
				if ((i + 0.5 - ox) * dx + (k + 0.5 - oz) * dz >= 0.0) {
					int id = this.cat.world.getBlockId(i, y - 1, k);
					if (id <= 0) return false;
					Block<?> floor = Blocks.blocksList[id];
					if (floor == null) return false;
					Material material = floor.getMaterial();
					if (material == Materials.WATER && !this.cat.isInWater()) return false;
					if (material == Materials.LAVA) return false;
				}
			}
		}
		return true;
	}

	private boolean isPositionClear(int x0, int y0, int z0, int sizeX, int sizeY, int sizeZ, double ox, double oz, double dx, double dz) {
		for (int i = x0; i < x0 + sizeX; ++i) {
			for (int j = y0; j < y0 + sizeY; ++j) {
				for (int k = z0; k < z0 + sizeZ; ++k) {
					if ((i + 0.5 - ox) * dx + (k + 0.5 - oz) * dz >= 0.0) {
						int id = this.cat.world.getBlockId(i, j, k);
						if (id > 0 && !isPassable(id, i, j, k)) return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isPassable(int id, int x, int y, int z) {
		Block<?> block = Blocks.blocksList[id];
		if (block == null) return true;
		if (Block.hasLogicClass(block, BlockLogicDoor.class)) {
			return BlockLogicDoor.isOpen(this.cat.world.getBlockMetadata(x, y, z));
		}
		return !block.getMaterial().blocksMotion();
	}
}
