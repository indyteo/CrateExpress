package fr.theoszanto.mc.crateexpress.events;

import fr.theoszanto.mc.crateexpress.models.CrateKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateGiveEvent extends Event implements Cancellable {
	private final @NotNull OfflinePlayer player;
	private final boolean savingKey;
	private final @NotNull Cause cause;
	private @NotNull CrateKey key;
	private int amount;
	private final @Nullable CommandSender commandSource;
	private final @Nullable AdminGUIGiveButton adminSource;
	private final @Nullable JavaPlugin pluginSource;
	private boolean cancelled = false;

	private static final @NotNull HandlerList handlers = new HandlerList();

	public CrateGiveEvent(@NotNull OfflinePlayer player,
	                      boolean savingKey,
	                      @NotNull Cause cause,
	                      @NotNull CrateKey key,
	                      int amount,
	                      @Nullable CommandSender commandSource,
	                      @Nullable AdminGUIGiveButton adminSource,
	                      @Nullable JavaPlugin pluginSource) {
		this.player = player;
		this.savingKey = savingKey;
		this.cause = cause;
		this.key = key;
		this.amount = amount;
		this.commandSource = commandSource;
		this.adminSource = adminSource;
		this.pluginSource = pluginSource;
	}

	public @NotNull OfflinePlayer getPlayer() {
		return this.player;
	}

	public boolean isSavingKey() {
		return this.savingKey;
	}

	public @NotNull Cause getCause() {
		return this.cause;
	}

	public @NotNull CrateKey getKey() {
		return this.key;
	}

	public void setKey(@NotNull CrateKey key) {
		this.key = key;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public @Nullable CommandSender getCommandSource() {
		return this.commandSource;
	}

	public @Nullable AdminGUIGiveButton getAdminSource() {
		return this.adminSource;
	}

	public @Nullable JavaPlugin getPluginSource() {
		return this.pluginSource;
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

	public enum Cause {
		GIVE_TO_COMMAND, GIVE_ALL_COMMAND, ADMIN_GIVE, PLUGIN_GIVE;

		public boolean isGiveCommand() {
			return this == GIVE_TO_COMMAND || this == GIVE_ALL_COMMAND;
		}
	}

	public enum AdminGUIGiveButton {
		LIST, MANAGE, PREVIEW
	}
}
