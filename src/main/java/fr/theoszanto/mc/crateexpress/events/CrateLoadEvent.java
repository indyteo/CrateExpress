package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrateLoadEvent extends CrateEvent {
	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateLoadEvent(@NotNull Crate crate) {
		super(crate);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
