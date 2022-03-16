package fr.theoszanto.mc.crateexpress.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {
	private LocationUtils() {
		throw new UnsupportedOperationException();
	}

	public static @NotNull UnloadableWorldLocation fromString(@NotNull String location) {
		String[] split = location.split(":");
		if (split.length != 2)
			throw new IllegalArgumentException("Unknown location expression: " + location);
		String worldName = split[0];
		String[] coords = split[1].split(",");
		float yaw = 0;
		float pitch = 0;
		if (coords.length == 5) {
			yaw = Float.parseFloat(coords[3]);
			pitch = Float.parseFloat(coords[4]);
		}
		if (coords.length >= 3) {
			double x = Double.parseDouble(coords[0]);
			double y = Double.parseDouble(coords[1]);
			double z = Double.parseDouble(coords[2]);
			return new UnloadableWorldLocation(worldName, x, y, z, yaw, pitch);
		} else
			throw new IllegalArgumentException("Unknown location expression: " + location);
	}

	public static @NotNull String toString(@NotNull Location location) {
		String worldName;
		if (location instanceof UnloadableWorldLocation)
			worldName = ((UnloadableWorldLocation) location).getWorldName();
		else {
			World world = location.getWorld();
			worldName = world == null ? "world" : world.getName();
		}
		return worldName + ":" + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
	}
}
