package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public abstract class CancellableCrateEvent extends CrateEvent implements Cancellable {
	private boolean cancelled = false;

	public CancellableCrateEvent(@NotNull Crate crate) {
		super(crate);
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
