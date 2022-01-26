package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class CrateListener extends PluginObject implements Listener {
	public CrateListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}
}
