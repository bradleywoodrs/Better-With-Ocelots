package bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pathfinder.Node;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

// ported from 1.2.5 deobf code (RandomPositionGenerator/PathEntity)
public final class EntityTargeting {
	private EntityTargeting() {}

	@FunctionalInterface
	public interface PathWeight {
		float get(TilePos pos);
	}

	@Nullable
	public static int[] findRandomTarget(Entity entity, int xz, int y, PathWeight weight) {
		return find(entity, xz, y, null, weight);
	}

	@Nullable
	public static int[] findRandomTargetAwayFrom(Entity entity, int xz, int y, double fromX, double fromY, double fromZ, PathWeight weight) {
		return find(entity, xz, y, new double[]{entity.x - fromX, entity.y - fromY, entity.z - fromZ}, weight);
	}

	@Nullable
	private static int[] find(Entity entity, int xz, int y, @Nullable double[] dir, PathWeight weight) {
		Random random = entity.world.rand;
		boolean found = false;
		int bestX = 0, bestY = 0, bestZ = 0;
		float best = -99999.0F;
		for (int i = 0; i < 10; ++i) {
			int dx = random.nextInt(2 * xz) - xz;
			int dy = random.nextInt(2 * y) - y;
			int dz = random.nextInt(2 * xz) - xz;
			if (dir == null || dx * dir[0] + dz * dir[2] >= 0.0) {
				int px = dx + MathHelper.floor(entity.x);
				int py = dy + MathHelper.floor(entity.y);
				int pz = dz + MathHelper.floor(entity.z);
				float w = weight.get(new TilePos(px, py, pz));
				if (w > best) {
					best = w;
					bestX = px;
					bestY = py;
					bestZ = pz;
					found = true;
				}
			}
		}
		return found ? new int[]{bestX, bestY, bestZ} : null;
	}

	public static boolean pathEndsAt(@Nullable Path path, int x, int z) {
		if (path == null) return false;
		Node last = path.last();
		return last != null && last.x == x && last.z == z;
	}
}
