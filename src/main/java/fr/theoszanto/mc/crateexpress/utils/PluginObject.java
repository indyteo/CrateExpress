package fr.theoszanto.mc.crateexpress.utils;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.managers.MoneyManager;
import fr.theoszanto.mc.crateexpress.managers.SpigotManager;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public abstract class PluginObject implements Logged {
	protected final @NotNull CrateExpress plugin;

	public PluginObject(@NotNull CrateExpress plugin) {
		this.plugin = plugin;
	}

	public final @NotNull CrateExpress getPlugin() {
		return this.plugin;
	}

	@Override
	public final @NotNull Logger getLogger() {
		return this.plugin.getLogger();
	}

	public final @NotNull String prefix() {
		return this.i18n("prefix");
	}

	public final @NotNull String i18n(@NotNull String key, @Nullable Object @NotNull... format) {
		return this.plugin.i18n(key, format);
	}

	public final void i18nMessage(@NotNull CommandSender sender, @NotNull String key, @Nullable Object @NotNull... format) {
		sender.sendMessage(this.prefix() + this.i18n(key, format));
	}

	public final void i18nRawMessage(@NotNull CommandSender sender, @NotNull String key, @Nullable Object @NotNull... format) {
		sender.sendMessage(this.i18n(key, format));
	}

	public final @NotNull String @NotNull[] i18nLines(@NotNull String key, @Nullable Object @NotNull... format) {
		String lines = this.i18n(key, format);
		return lines.isEmpty() ? ItemUtils.NO_LORE : lines.split("\n|\\\\n");
	}

	public final @NotNull Storage storage() {
		return this.plugin.storage();
	}

	public final @NotNull CrateRegistry crates() {
		return this.plugin.crates();
	}

	public final @NotNull MoneyManager money() {
		return this.plugin.money();
	}

	public final @NotNull SpigotManager spigot() {
		return this.plugin.spigot();
	}

	public final boolean event(@NotNull Event event) {
		this.plugin.getServer().getPluginManager().callEvent(event);
		return !(event instanceof Cancellable) || !((Cancellable) event).isCancelled();
	}

	public final void run(@NotNull Runnable task) {
		Bukkit.getScheduler().runTask(this.plugin, task);
	}
}
