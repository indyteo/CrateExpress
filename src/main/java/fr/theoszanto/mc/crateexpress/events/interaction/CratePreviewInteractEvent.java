package fr.theoszanto.mc.crateexpress.events.interaction;

import fr.theoszanto.mc.crateexpress.events.CrateInteractEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CratePreviewInteractEvent extends CrateInteractEvent {
	private final boolean fromPhysicalCrate;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CratePreviewInteractEvent(@NotNull Crate crate, @NotNull Player player, boolean fromPhysicalCrate) {
		super(crate, player);
		this.fromPhysicalCrate = fromPhysicalCrate;
	}

	public boolean isFromPhysicalCrate() {
		return this.fromPhysicalCrate;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
