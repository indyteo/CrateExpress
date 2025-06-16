package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateNamespace;
import fr.theoszanto.mc.crateexpress.models.CrateNamespaceRegistry;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.StatsRecord;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.express.utils.Logged;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CrateStorage extends Logged {
	default void initialize() throws IllegalStateException {}

	@NotNull List<@NotNull CrateNamespace> loadNamespaces() throws IllegalStateException;

	default void saveNamespaces(@NotNull CrateNamespaceRegistry registry) throws IllegalStateException {
		for (CrateNamespace namespace : registry) {
			try {
				this.saveNamespace(namespace);
			} catch (IllegalStateException e) {
				this.error("Could not save namespace: " + namespace.getPath(), e);
			}
		}
	}

	void saveNamespace(@NotNull CrateNamespace namespace) throws IllegalStateException;

	void deleteNamespace(@NotNull String path) throws IllegalStateException;

	void loadCrates(@NotNull CrateRegistry registry) throws IllegalStateException;

	default void saveCrates(@NotNull CrateRegistry registry) throws IllegalStateException {
		for (Crate crate : registry) {
			try {
				this.saveCrate(crate);
			} catch (IllegalStateException e) {
				this.error("Could not save crate: " + crate.getId(), e);
			}
		}
	}

	void saveCrate(@NotNull Crate crate) throws IllegalStateException;

	void deleteCrate(@NotNull String id) throws IllegalStateException;

	void saveReward(@NotNull Player player, @NotNull CrateReward reward) throws IllegalStateException;

	@NotNull List<@NotNull ClaimableReward> listRewards(@NotNull Player player) throws IllegalStateException;

	int countRewards(@NotNull Player player) throws IllegalStateException;

	void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException;

	default void clearRewards(@NotNull Player player) throws IllegalStateException {
		this.clearRewards(player.getUniqueId());
	}

	void clearRewards(@NotNull UUID uuid) throws IllegalStateException;

	void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException;

	int getOpenStats(@NotNull Crate crate) throws IllegalStateException;

	void updateStats(@NotNull List<@NotNull StatsRecord> stats) throws IllegalStateException;

	@NotNull List<@NotNull HistoricalReward> listHistory(@NotNull OfflinePlayer player, @NotNull Date date) throws IllegalStateException;

	void migratePlayerStats(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException;

	void clearPlayerStats(@NotNull UUID uuid) throws IllegalStateException;
}
