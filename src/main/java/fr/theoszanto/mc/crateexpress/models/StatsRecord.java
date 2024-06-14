package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class StatsRecord {
	private final @NotNull Date date;
	private final @NotNull Player player;
	private final @NotNull Crate crate;
	private final @NotNull List<@NotNull CrateReward> rewards;

	public StatsRecord(@NotNull Date date, @NotNull Player player, @NotNull Crate crate, @NotNull List<@NotNull CrateReward> rewards) {
		this.date = date;
		this.player = player;
		this.crate = crate;
		this.rewards = rewards;
	}

	public @NotNull Date getDate() {
		return this.date;
	}

	public @NotNull Player getPlayer() {
		return this.player;
	}

	public @NotNull Crate getCrate() {
		return this.crate;
	}

	public @NotNull List<@NotNull CrateReward> getRewards() {
		return this.rewards;
	}
}
