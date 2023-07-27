package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.express.utils.Logged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface CrateStorage extends Logged {
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

	void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException;
}
