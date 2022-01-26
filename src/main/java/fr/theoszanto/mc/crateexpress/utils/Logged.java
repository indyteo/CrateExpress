package fr.theoszanto.mc.crateexpress.utils;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface Logged {
	@NotNull Logger getLogger();

	default void debug(@NotNull String message) {
		this.getLogger().info(message);
		TextComponent component = new TextComponent("§8[§cDEBUG§8]§r " + message);
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(JavaUtils.caller())));
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.isOp())
				player.spigot().sendMessage(component);
	}

	default void log(@NotNull String message) {
		this.getLogger().info(message);
	}

	default void warn(@NotNull String message) {
		this.getLogger().warning(message);
	}

	default void error(@NotNull String message) {
		this.getLogger().severe(message);
	}

	default void error(@NotNull String message, @NotNull Throwable e) {
		this.getLogger().log(Level.SEVERE, message, e);
	}
}
