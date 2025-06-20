package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.CrateNamespaceRegistry;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MigrationCrateStorage extends PluginObject implements CrateStorage {
	private final @NotNull CrateStorage from;
	private final @NotNull CrateStorage to;

	public MigrationCrateStorage(@NotNull CrateExpress plugin, @NotNull String fromClass, @NotNull String toClass, @NotNull List<@Nullable Object> fromOptions, @NotNull List<@Nullable Object> toOptions) {
		super(plugin);
		this.from = (CrateStorage) this.instanciate(fromClass, fromOptions);
		this.to = (CrateStorage) this.instanciate(toClass, toOptions);
	}

	@Override
	public void initialize() throws IllegalStateException {
		this.from.initialize();
		this.to.initialize();
	}

	@Override
	public @NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException {
		return this.from.loadNamespaces();
	}

	@Override
	public void saveNamespaces(@NotNull CrateNamespaceRegistry registry) throws IllegalStateException {
		this.to.saveNamespaces(registry);
	}

	@Override
	public void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException {
		this.to.saveNamespace(namespace);
	}

	@Override
	public void deleteNamespace(@NotNull String path) throws IllegalStateException {
		this.to.deleteNamespace(path);
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		this.from.loadCrates(registry);
	}

	@Override
	public void saveCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		this.to.saveCrates(registry);
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		this.to.saveCrate(crate);
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		this.to.deleteCrate(id);
	}

	@Override
	public void saveReward(@NotNull OfflinePlayer player, @NotNull CrateReward reward) throws IllegalStateException {
		this.to.saveReward(player, reward);
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull Player player) throws IllegalStateException {
		return this.from.listRewards(player);
	}

	@Override
	public int countRewards(@NotNull Player player) throws IllegalStateException {
		return this.from.countRewards(player);
	}

	@Override
	public void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException {
		this.to.deleteReward(player, id);
	}

	@Override
	public void clearRewards(@NotNull UUID uuid) throws IllegalStateException {
		this.to.clearRewards(uuid);
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.to.migrateRewards(from, to);
	}

	@Override
	public int getOpenStats(@NotNull Crate crate) throws IllegalStateException {
		return this.from.getOpenStats(crate);
	}

	@Override
	public void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException {
		this.to.updateStats(stats);
	}

	@Override
	public @NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException {
		return this.from.listHistory(player, date);
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.to.migratePlayerStats(from, to);
	}

	@Override
	public void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException {
		this.to.clearPlayerStats(uuid);
	}
}
