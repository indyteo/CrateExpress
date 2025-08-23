package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.managers.players.DefaultPlayersProvider;
import fr.theoszanto.mc.crateexpress.managers.players.PlayersProvider;
import fr.theoszanto.mc.crateexpress.models.CrateConfig;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class PlayersManager extends PluginObject {
	private @NotNull PlayersProvider provider;

	public PlayersManager(@NotNull CrateExpress plugin) {
		super(plugin);
		this.provider = new DefaultPlayersProvider(plugin);
	}

	public boolean exists(@NotNull OfflinePlayer player) {
		return this.provider.exists(player);
	}

	public @NotNull Stream<@NotNull OfflinePlayer> all() {
		return this.provider.all();
	}

	public void load(@NotNull CrateConfig.Players config) throws IllegalStateException {
		if (config.isEmpty())
			return;
		this.provider = config.getProvider().instanciate();
	}

	public void reset() {
		this.provider = new DefaultPlayersProvider(this.plugin);
	}
}
