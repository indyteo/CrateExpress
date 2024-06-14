package fr.theoszanto.mc.crateexpress.models.reward;

import fr.theoszanto.mc.crateexpress.models.Crate;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class HistoricalReward implements Comparable<HistoricalReward> {
	private final @NotNull Date date;
	private final @NotNull Crate crate;
	private final @NotNull CrateReward reward;

	public HistoricalReward(@NotNull Date date, @NotNull Crate crate, @NotNull CrateReward reward) {
		this.date = date;
		this.crate = crate;
		this.reward = reward;
	}

	public @NotNull Date getDate() {
		return this.date;
	}

	public @NotNull Crate getCrate() {
		return this.crate;
	}

	public @NotNull CrateReward getReward() {
		return this.reward;
	}

	@Override
	public int compareTo(@NotNull HistoricalReward o) {
		// Reverse natural date order (from most recent to most ancient)
		return o.date.compareTo(this.date);
	}
}
