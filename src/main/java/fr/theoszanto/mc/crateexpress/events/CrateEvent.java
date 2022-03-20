package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class CrateEvent extends Event {
	private final @NotNull Crate crate;

	public CrateEvent(@NotNull Crate crate) {
		this.crate = crate;
	}

	public @NotNull Crate getCrate() {
		return this.crate;
	}
}
