package fr.theoszanto.mc.crateexpress.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {
	private LocationUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull Location fromString(@NotNull String location) {
		String[] split = location.split(":");
		if (split.length != 2)
			throw new IllegalArgumentException("Unknown location expression: " + location);
		World world = Bukkit.getWorld(split[0]);
		if (world == null)
			throw new IllegalStateException("Unknown (or unloaded) world: " + split[0]);
		return fromString(split[1], world);
	}

	public static @NotNull Location fromString(@NotNull String location, @NotNull World world) {
		String[] split = location.split(",");
		float yaw = 0;
		float pitch = 0;
		if (split.length == 5) {
			yaw = Float.parseFloat(split[3]);
			pitch = Float.parseFloat(split[4]);
		}
		if (split.length >= 3) {
			double x = Double.parseDouble(split[0]);
			double y = Double.parseDouble(split[1]);
			double z = Double.parseDouble(split[2]);
			return new Location(world, x, y, z, yaw, pitch);
		} else
			throw new IllegalArgumentException("Unknown location expression: " + location);
	}

	public static @NotNull String toString(@NotNull Location location) {
		World world = location.getWorld();
		return (world == null ? "" : world.getName() + ":") + location.getX() + "," + location.getY()
				+ "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
	}
}
