package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.CrateKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CrateGiveAllEvent extends Event implements Cancellable {
	private final @NotNull CommandSender source;
	private final @NotNull CrateKey key;
	private final int amount;
	private final @NotNull Collection<@NotNull Player> targets;
	private boolean cancelled = false;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateGiveAllEvent(@NotNull CommandSender source, @NotNull CrateKey key, int amount, @NotNull Collection<@NotNull Player> targets) {
		this.source = source;
		this.key = key;
		this.amount = amount;
		this.targets = targets;
	}

	public @NotNull CommandSender getSource() {
		return this.source;
	}

	public @NotNull CrateKey getKey() {
		return this.key;
	}

	public int getAmount() {
		return this.amount;
	}

	public @NotNull Collection<@NotNull Player> getTargets() {
		return this.targets;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}
}
