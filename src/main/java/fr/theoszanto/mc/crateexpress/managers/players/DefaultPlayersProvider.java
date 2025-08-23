package fr.theoszanto.mc.crateexpress.managers.players;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class DefaultPlayersProvider extends PluginObject implements PlayersProvider {
	public DefaultPlayersProvider(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public boolean exists(@NotNull OfflinePlayer player) {
		return player.hasPlayedBefore();
	}

	@Override
	public @NotNull Stream<@NotNull OfflinePlayer> all() {
		return Arrays.stream(Bukkit.getOfflinePlayers());
	}
}
