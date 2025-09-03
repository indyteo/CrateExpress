package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.RawStatsRecord;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ReplicationCrateStorage extends PluginObject implements CrateStorage {
	private final @NotNull CrateStorage from;
	private final @NotNull CrateStorage to;
	private final boolean initializeStatsAndClaimedRewards;

	public ReplicationCrateStorage(
			@NotNull CrateExpress plugin,
			@NotNull String fromClass,
			@NotNull String toClass,
			@NotNull ArrayList<@Nullable Object> fromOptions,
			@NotNull ArrayList<@Nullable Object> toOptions,
			@NotNull Boolean initializeStatsAndClaimedRewards
	) {
		super(plugin);
		this.from = (CrateStorage) this.instanciate(fromClass, fromOptions);
		this.to = (CrateStorage) this.instanciate(toClass, toOptions);
		this.initializeStatsAndClaimedRewards = initializeStatsAndClaimedRewards;
	}

	private void replicate(@NotNull Runnable action) {
		if (this.from.isAsync() == this.to.isAsync())
			action.run();
		else if (this.to.isAsync())
			this.async(action);
		else {
			synchronized (this.to) {
				action.run();
			}
		}
	}

	@Override
	public boolean isAsync() {
		return this.from.isAsync();
	}

	@Override
	public void initialize() throws IllegalStateException {
		this.from.initialize();
		this.to.initialize();
		if (this.initializeStatsAndClaimedRewards) {
			this.log("Initializing Stats and Claimed Rewards. It may take several minutes.");
			this.saveRawStats(this.listRawStats());
			this.players().all().forEach(this::listRewards);
			this.log("Done!");
		}
	}

	@Override
	public @NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException {
		List<CrateNamespace> namespaces = this.from.loadNamespaces();
		this.replicate(() -> namespaces.forEach(this.to::saveNamespace));
		return namespaces;
	}

	@Override
	public void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException {
		this.from.saveNamespace(namespace);
		this.replicate(() -> this.to.saveNamespace(namespace));
	}

	@Override
	public void deleteNamespace(@NotNull String path) throws IllegalStateException {
		this.from.deleteNamespace(path);
		this.replicate(() -> this.to.deleteNamespace(path));
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		this.from.loadCrates(registry);
		this.replicate(() -> this.to.saveCrates(registry));
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		this.from.saveCrate(crate);
		this.replicate(() -> this.to.saveCrate(crate));
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		this.from.deleteCrate(id);
		this.replicate(() -> this.to.deleteCrate(id));
	}

	@Override
	public void saveReward(@NotNull OfflinePlayer player, @NotNull CrateReward reward) throws IllegalStateException {
		this.from.saveReward(player, reward);
		this.replicate(() -> this.to.saveReward(player, reward));
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull OfflinePlayer player) throws IllegalStateException {
		List<ClaimableReward> rewards = this.from.listRewards(player);
		this.replicate(() -> {
			this.to.clearRewards(player);
			for (ClaimableReward reward : rewards)
				this.to.saveReward(player, reward.reward());
		});
		return rewards;
	}

	@Override
	public int countRewards(@NotNull OfflinePlayer player) throws IllegalStateException {
		return this.from.countRewards(player);
	}

	@Override
	public void deleteReward(@NotNull OfflinePlayer player, @NotNull String id) throws IllegalStateException {
		this.from.deleteReward(player, id);
		this.replicate(() -> this.to.deleteReward(player, id));
	}

	@Override
	public void clearRewards(@NotNull UUID uuid) throws IllegalStateException {
		this.from.clearRewards(uuid);
		this.replicate(() -> this.to.clearRewards(uuid));
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.from.migrateRewards(from, to);
		this.replicate(() -> this.to.migrateRewards(from, to));
	}

	@Override
	public int getOpenStats(@NotNull Crate crate) throws IllegalStateException {
		return this.from.getOpenStats(crate);
	}

	@Override
	public void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException {
		this.from.updateStats(stats);
		this.replicate(() -> this.to.updateStats(stats));
	}

	@Override
	public @NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException {
		return this.from.listHistory(player, date);
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.from.migratePlayerStats(from, to);
		this.replicate(() -> this.to.migratePlayerStats(from, to));
	}

	@Override
	public void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException {
		this.from.clearPlayerStats(uuid);
		this.replicate(() -> this.to.clearPlayerStats(uuid));
	}

	@Override
	public void invalidateCache(@NotNull UUID uuid) throws IllegalStateException {
		this.from.invalidateCache(uuid);
		this.replicate(() -> this.to.invalidateCache(uuid));
	}

	@Override
	public @NotNull List<@NotNull RawStatsRecord> listRawStats() throws IllegalStateException {
		return this.from.listRawStats();
	}

	@Override
	public void saveRawStats(@NotNull List<@NotNull RawStatsRecord> stats) throws IllegalStateException {
		this.to.saveRawStats(stats);
	}
}
