package fr.theoszanto.mc.crateexpress.events.interaction;

import fr.theoszanto.mc.crateexpress.events.CrateInteractEvent;
import fr.theoszanto.mc.crateexpress.models.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrateOpenInteractEvent extends CrateInteractEvent {
	private boolean consumeKey = true;
	private boolean broadcastMessage = true;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateOpenInteractEvent(@NotNull Crate crate, @NotNull Player player) {
		super(crate, player);
	}

	public boolean doesConsumingKey() {
		return this.consumeKey;
	}

	public void setConsumeKey(boolean consume) {
		this.consumeKey = consume;
	}

	public boolean doesBroadcastMessage() {
		return this.broadcastMessage;
	}

	public void setBroadcastMessage(boolean display) {
		this.broadcastMessage = display;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
