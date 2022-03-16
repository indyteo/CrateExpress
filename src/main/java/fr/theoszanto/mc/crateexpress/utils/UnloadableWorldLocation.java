package fr.theoszanto.mc.crateexpress.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnloadableWorldLocation extends Location {
	private @NotNull String worldName;

	public UnloadableWorldLocation(@NotNull String worldName, double x, double y, double z) {
		this(worldName, x, y, z, 0, 0);
	}

	public UnloadableWorldLocation(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
		super(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
		this.worldName = worldName;
	}

	public UnloadableWorldLocation(@NotNull World world, double x, double y, double z) {
		this(world, x, y, z, 0, 0);
	}

	public UnloadableWorldLocation(@NotNull World world, double x, double y, double z, float yaw, float pitch) {
		super(world, x, y, z, yaw, pitch);
		this.worldName = world.getName();
	}

	@Override
	public @Nullable World getWorld() {
		if (this.isWorldLoaded())
			return super.getWorld();
		World world = Bukkit.getWorld(this.worldName);
		if (world != null)
			super.setWorld(world);
		return world;
	}

	@Override
	public void setWorld(@Nullable World world) {
		if (world != null) {
			super.setWorld(world);
			this.worldName = world.getName();
		}
	}

	public @NotNull String getWorldName() {
		return this.worldName;
	}

	public void setWorldName(@NotNull String worldName) {
		this.worldName = worldName;
	}
}
