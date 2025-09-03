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

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NoopCrateStorage extends PluginObject implements CrateStorage {
	public NoopCrateStorage(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@Override
	public void initialize() throws IllegalStateException {
		this.notice();
	}

	@Override
	public @NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException {
		this.notice();
		return List.of();
	}

	@Override
	public void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void deleteNamespace(@NotNull String path) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void saveCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void saveCrate(@NotNull Crate crate) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void deleteCrate(@NotNull String id) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void saveReward(@NotNull OfflinePlayer player, @NotNull CrateReward reward) throws IllegalStateException {
		this.notice();
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull OfflinePlayer player) throws IllegalStateException {
		this.notice();
		return List.of();
	}

	@Override
	public int countRewards(@NotNull OfflinePlayer player) throws IllegalStateException {
		this.notice();
		return 0;
	}

	@Override
	public void deleteReward(@NotNull OfflinePlayer player, @NotNull String id) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void clearRewards(@NotNull UUID uuid) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.notice();
	}

	@Override
	public int getOpenStats(@NotNull Crate crate) throws IllegalStateException {
		this.notice();
		return 0;
	}

	@Override
	public void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException {
		this.notice();
	}

	@Override
	public @NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException {
		this.notice();
		return List.of();
	}

	@Override
	public void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void invalidateCache(@NotNull UUID uuid) throws IllegalStateException {
		this.notice();
	}

	@Override
	public @NotNull List<@NotNull RawStatsRecord> listRawStats() throws IllegalStateException {
		this.notice();
		return List.of();
	}

	@Override
	public void saveRawStats(@NotNull List<@NotNull RawStatsRecord> stats) throws IllegalStateException {
		this.notice();
	}

	private void notice() {
		this.warn("NoopStorage is not able to save/load any data!");
	}
}
