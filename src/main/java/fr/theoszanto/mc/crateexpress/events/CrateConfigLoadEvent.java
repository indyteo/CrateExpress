package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrateConfigLoadEvent extends Event {
	private final @NotNull CrateConfig config;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateConfigLoadEvent(@NotNull CrateConfig config) {
		this.config = config;
	}

	public @NotNull CrateConfig getConfig() {
		return this.config;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
