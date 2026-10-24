package bradleywoodrs.betterwithocelots.misc;

import bradleywoodrs.betterwithocelots.BetterWithOcelotsConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.holiday.Holiday;

import java.time.Month;

import static net.minecraft.client.holiday.Holidays.XMAS;

@Environment(EnvType.CLIENT)
public class Holidays {
	public static final Holiday SPOOKY_SEASON = new Holiday(Month.OCTOBER, 24, Month.OCTOBER, 31);

	public static final String HAT_WITCH = "hat_witch";
	public static final String HAT_SANTA = "hat_santa";
	public static final String HAT = getHat();

	private static String getHat() {
		if (!BetterWithOcelotsConfig.CFG.getBoolean("Cats.holidayFeatures")) {
			return null;
		}
		if (SPOOKY_SEASON.isActive()) {
			return HAT_WITCH;
		}
		if (XMAS.isActive()) {
			return HAT_SANTA;
		}
		return null;
	}
}
