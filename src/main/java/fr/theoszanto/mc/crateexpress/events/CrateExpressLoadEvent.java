package fr.theoszanto.mc.crateexpress.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrateExpressLoadEvent extends Event {
	private final boolean reload;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateExpressLoadEvent(boolean reload) {
		this.reload = reload;
	}

	public boolean isReload() {
		return this.reload;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
