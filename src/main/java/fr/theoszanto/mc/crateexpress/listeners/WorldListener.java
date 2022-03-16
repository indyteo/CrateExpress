package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.utils.UnloadableWorldLocation;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class WorldListener extends CrateListener {
	public WorldListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@EventHandler
	private void onWorldLoad(@NotNull WorldLoadEvent event) {
		World world = event.getWorld();
		String name = world.getName();
		for (Crate crate : this.crates()) {
			UnloadableWorldLocation location = crate.getLocation();
			if (location != null && location.getWorldName().equals(name))
				location.setWorld(world);
		}
	}
}
