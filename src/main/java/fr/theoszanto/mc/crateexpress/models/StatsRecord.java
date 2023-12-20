package fr.theoszanto.mc.crateexpress.models;

import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsRecord {
	private final @NotNull Date date;
	private final @NotNull UUID player;
	private final @NotNull String crate;
	private final @NotNull List<@NotNull String> rewards;

	public StatsRecord(@NotNull Date date, @NotNull UUID player, @NotNull String crate, @NotNull List<@NotNull String> rewards) {
		this.date = date;
		this.player = player;
		this.crate = crate;
		this.rewards = rewards;
	}

	public @NotNull Date getDate() {
		return this.date;
	}

	public @NotNull UUID getPlayer() {
		return this.player;
	}

	public @NotNull String getCrate() {
		return this.crate;
	}

	public @NotNull List<@NotNull String> getRewards() {
		return this.rewards;
	}

	public static @NotNull StatsRecord of(@NotNull Player player, @NotNull Crate crate, @NotNull List<@NotNull CrateReward> rewards) {
		return new StatsRecord(
				new Date(),
				player.getUniqueId(),
				crate.getId(),
				rewards.stream().map(CrateReward::getId).collect(Collectors.toList())
		);
	}
}
