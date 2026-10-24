package bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicDoor;
import net.minecraft.core.block.BlockLogicFence;
import net.minecraft.core.block.BlockLogicFenceGate;
import net.minecraft.core.block.BlockLogicFenceThin;
import net.minecraft.core.block.BlockLogicTrapDoor;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.WorldSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// ported from 1.2.5 deobf code (PathFinder/PathPoint)
public class EntityPathFinding {
	private static final int BLOCKED = 0;
	private static final int OPEN = 1;
	private static final int OPEN_SPECIAL = 2;
	private static final int WATER = -1;
	private static final int LAVA = -2;
	private static final int FENCE = -3;
	private static final int TRAPDOOR_WALL = -4;
	private static final int ACID = -5;
	private static final int MAX_FALL = 4;

	private final WorldSource world;
	private final boolean avoidWater;
	private final boolean canSwim;
	private final Map<Long, PathNode> nodes = new HashMap<>();
	private final PriorityQueue<QueueEntry> open = new PriorityQueue<>((a, b) -> Float.compare(a.cost, b.cost));
	private boolean avoidWaterNow;

	public EntityPathFinding(WorldSource world, boolean avoidWater, boolean canSwim) {
		this.world = world;
		this.avoidWater = avoidWater;
		this.canSwim = canSwim;
	}

	boolean matches(WorldSource world, boolean avoidWater) {
		return this.world == world && this.avoidWater == avoidWater;
	}

	@Nullable
	public int[][] findPath(Entity entity, Entity target, float range) {
		return findPath(entity, target.x, target.bb.minY, target.z, range);
	}

	@Nullable
	public int[][] findPath(Entity entity, int x, int y, int z, float range) {
		return findPath(entity, x + 0.5, y + 0.5, z + 0.5, range);
	}

	@Nullable
	private int[][] findPath(Entity entity, double tx, double ty, double tz, float range) {
		this.nodes.clear();
		this.open.clear();
		this.avoidWaterNow = this.avoidWater;

		int startY;
		if (this.canSwim && entity.isInWater()) {
			startY = (int) entity.bb.minY;
			while (isWater(MathHelper.floor(entity.x), startY, MathHelper.floor(entity.z))) {
				++startY;
			}
			this.avoidWaterNow = false;
		} else {
			startY = MathHelper.floor(entity.bb.minY + 0.5);
		}

		PathNode start = node(MathHelper.floor(entity.bb.minX), startY, MathHelper.floor(entity.bb.minZ));
		PathNode goal = node(MathHelper.floor(tx - entity.bbWidth / 2.0F), MathHelper.floor(ty), MathHelper.floor(tz - entity.bbWidth / 2.0F));
		int sizeX = MathHelper.floor_float(entity.bbWidth + 1.0F);
		int sizeY = MathHelper.floor_float(entity.bbHeight + 1.0F);

		int[][] result = search(entity, start, goal, sizeX, sizeY, range);
		this.avoidWaterNow = this.avoidWater;
		this.nodes.clear();
		this.open.clear();
		return result;
	}

	@Nullable
	private int[][] search(Entity entity, PathNode start, PathNode goal, int sizeX, int sizeY, float range) {
		start.g = 0.0F;
		start.h = start.distanceTo(goal);
		start.f = start.h;
		start.queued = true;
		this.open.add(new QueueEntry(start, start.f));
		PathNode closest = start;

		while (!this.open.isEmpty()) {
			QueueEntry entry = this.open.poll();
			PathNode current = entry.node;
			if (current.closed || entry.cost > current.f) {
				continue;
			}
			if (current == goal) {
				return build(goal);
			}
			if (current.distanceTo(goal) < closest.distanceTo(goal)) {
				closest = current;
			}
			current.closed = true;

			int climb = canStand(entity, current.x, current.y + 1, current.z, sizeX, sizeY) == OPEN ? 1 : 0;
			tryNeighbour(entity, current, current.x, current.y, current.z + 1, climb, sizeX, sizeY, goal, range);
			tryNeighbour(entity, current, current.x - 1, current.y, current.z, climb, sizeX, sizeY, goal, range);
			tryNeighbour(entity, current, current.x + 1, current.y, current.z, climb, sizeX, sizeY, goal, range);
			tryNeighbour(entity, current, current.x, current.y, current.z - 1, climb, sizeX, sizeY, goal, range);
		}
		return closest == start ? null : build(closest);
	}

	private void tryNeighbour(Entity entity, PathNode from, int x, int y, int z, int climb, int sizeX, int sizeY, PathNode goal, float range) {
		PathNode next = walkable(entity, x, y, z, climb, sizeX, sizeY);
		if (next == null || next.closed || next.distanceTo(goal) >= range) {
			return;
		}
		float g = from.g + from.distanceTo(next);
		if (!next.queued || g < next.g) {
			next.parent = from;
			next.g = g;
			next.h = next.distanceTo(goal);
			next.f = g + next.h;
			next.queued = true;
			this.open.add(new QueueEntry(next, next.f));
		}
	}

	@Nullable
	private PathNode walkable(Entity entity, int x, int y, int z, int climb, int sizeX, int sizeY) {
		int here = canStand(entity, x, y, z, sizeX, sizeY);
		if (here == OPEN_SPECIAL) {
			return node(x, y, z);
		}
		PathNode result = here == OPEN ? node(x, y, z) : null;
		if (result == null && climb > 0 && here != FENCE && here != TRAPDOOR_WALL
			&& canStand(entity, x, y + climb, z, sizeX, sizeY) == OPEN) {
			y += climb;
			result = node(x, y, z);
		}
		if (result != null) {
			int fall = 0;
			int below = 0;
			while (y > 0) {
				below = canStand(entity, x, y - 1, z, sizeX, sizeY);
				if (this.avoidWaterNow && below == WATER) {
					return null;
				}
				if (below != OPEN) {
					break;
				}
				if (++fall >= MAX_FALL) {
					return null;
				}
				--y;
				if (y > 0) {
					result = node(x, y, z);
				}
			}
			if (below == LAVA || below == ACID) {
				return null;
			}
		}
		return result;
	}

	private int canStand(Entity entity, int x0, int y0, int z0, int sizeX, int sizeY) {
		boolean special = false;
		for (int x = x0; x < x0 + sizeX; ++x) {
			for (int y = y0; y < y0 + sizeY; ++y) {
				for (int z = z0; z < z0 + sizeX; ++z) {
					int id = this.world.getBlockId(x, y, z);
					if (id <= 0) {
						continue;
					}
					Block<?> block = Blocks.blocksList[id];
					if (block == null) {
						continue;
					}
					int meta = this.world.getBlockMetadata(x, y, z);
					Material material = block.getMaterial();

					if (Block.hasLogicClass(block, BlockLogicTrapDoor.class)) {
						if (BlockLogicTrapDoor.isTrapdoorOpen(meta) || BlockLogicTrapDoor.isUpperHalf(meta)) {
							return TRAPDOOR_WALL;
						}
						special = true;
						continue;
					}
					if (material == Materials.WATER) {
						if (this.avoidWaterNow) {
							return WATER;
						}
						special = true;
						continue;
					}
					if (Block.hasLogicClass(block, BlockLogicDoor.class)) {
						if (!BlockLogicDoor.isOpen(meta)) {
							return BLOCKED;
						}
						continue;
					}
					if (Block.hasLogicClass(block, BlockLogicFenceGate.class)) {
						if (!BlockLogicFenceGate.isOpen(meta)) {
							return FENCE;
						}
						continue;
					}
					if (Block.hasLogicClass(block, BlockLogicFence.class) || Block.hasLogicClass(block, BlockLogicFenceThin.class)) {
						return FENCE;
					}
					if (material == Materials.LAVA) {
						if (!entity.isInLava()) {
							return LAVA;
						}
						continue;
					}
					if (material == Materials.ACID) {
						if (!entity.isInAcid()) {
							return ACID;
						}
					}
					if (material.blocksMotion()) {
						return BLOCKED;
					}
				}
			}
		}
		return special ? OPEN_SPECIAL : OPEN;
	}

	private boolean isWater(int x, int y, int z) {
		Block<?> block = Blocks.blocksList[this.world.getBlockId(x, y, z)];
		return block != null && block.getMaterial() == Materials.WATER;
	}

	private PathNode node(int x, int y, int z) {
		long key = ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | ((long) y & 0xFFFL);
		PathNode node = this.nodes.get(key);
		if (node == null) {
			node = new PathNode(x, y, z);
			this.nodes.put(key, node);
		}
		return node;
	}

	private static int[][] build(PathNode end) {
		int length = 0;
		for (PathNode n = end; n != null; n = n.parent) {
			++length;
		}
		int[][] path = new int[length][];
		for (PathNode n = end; n != null; n = n.parent) {
			path[--length] = new int[]{n.x, n.y, n.z};
		}
		return path;
	}

	private static final class QueueEntry {
		final PathNode node;
		final float cost;

		QueueEntry(PathNode node, float cost) {
			this.node = node;
			this.cost = cost;
		}
	}

	private static final class PathNode {
		final int x, y, z;
		float g, h, f;
		boolean closed, queued;
		PathNode parent;

		PathNode(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		float distanceTo(PathNode other) {
			float dx = other.x - this.x;
			float dy = other.y - this.y;
			float dz = other.z - this.z;
			return MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
		}
	}
}
