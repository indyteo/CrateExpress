package fr.theoszanto.mc.crateexpress.storage;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateRegistry;
import fr.theoszanto.mc.crateexpress.models.reward.ClaimableReward;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NoopCrateStorage extends PluginObject implements CrateStorage {
	public NoopCrateStorage(@NotNull CrateExpress plugin) {
		super(plugin);
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
	public void saveReward(@NotNull Player player, @NotNull CrateReward reward) throws IllegalStateException {
		this.notice();
	}

	@Override
	public @NotNull List<@NotNull ClaimableReward> listRewards(@NotNull Player player) throws IllegalStateException {
		this.notice();
		return Collections.emptyList();
	}

	@Override
	public int countRewards(@NotNull Player player) throws IllegalStateException {
		this.notice();
		return 0;
	}

	@Override
	public void deleteReward(@NotNull Player player, @NotNull String id) throws IllegalStateException {
		this.notice();
	}

	@Override
	public void migrateRewards(@NotNull UUID from, @NotNull UUID to) throws IllegalStateException {
		this.notice();
	}

	private void notice() {
		this.warn("NoopStorage is not able to save/load any data!");
	}
}
