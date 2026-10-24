package bradleywoodrs.betterwithocelots.entities.ocelot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.world.World;

@Environment(EnvType.CLIENT)
public class MobCatGuideEntry extends MobOcelot {
	private static final long CYCLE_MILLIS = 2000L;
	private int clickOffset;

	public MobCatGuideEntry(World world) {
		super(world);
		this.setTamed(true);
	}

	@Override
	public int getCatType() {
		long step = System.currentTimeMillis() / CYCLE_MILLIS + this.clickOffset;
		return 1 + (int) (step % CAT_VARIANTS);
	}

	@Override
	public boolean cycleVariant() {
		this.clickOffset++;
		return true;
	}
}
