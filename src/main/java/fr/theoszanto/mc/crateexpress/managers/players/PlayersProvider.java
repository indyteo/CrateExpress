package fr.theoszanto.mc.crateexpress.managers.players;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface PlayersProvider {
	boolean exists(@NotNull OfflinePlayer player);

	@NotNull Stream<@NotNull OfflinePlayer> all();
}
