package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class CrateInteractEvent extends CancellableCrateEvent {
	private final @NotNull Player player;

	public CrateInteractEvent(@NotNull Crate crate, @NotNull Player player) {
		super(crate);
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return this.player;
	}
}
