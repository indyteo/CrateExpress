package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import fr.theoszanto.mc.express.utils.UnloadableWorldLocation;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorldListener extends ExpressListener<CrateExpress> {
	public WorldListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@EventHandler
	private void onWorldLoad(@NotNull WorldLoadEvent event) {
		World world = event.getWorld();
		String name = world.getName();
		for (Crate crate : this.plugin.crates()) {
			List<UnloadableWorldLocation> locations = crate.getLocations();
			if (locations != null) {
				for (UnloadableWorldLocation location : locations) {
					if (location.getWorldName().equals(name))
						location.setWorld(world);
				}
			}
		}
	}
}
